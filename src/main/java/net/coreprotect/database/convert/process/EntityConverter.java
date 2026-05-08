package net.coreprotect.database.convert.process;

import net.coreprotect.CoreProtect;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.database.convert.ClickhouseConverter;
import net.coreprotect.database.convert.table.EntityTable;
import net.coreprotect.thread.Scheduler;
import net.coreprotect.utility.EntityUtils;
import net.coreprotect.utility.WorldUtils;
import net.coreprotect.utility.entity.EntityUtil;
import net.coreprotect.utility.serialize.Bytes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.io.BukkitObjectInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EntityConverter implements ConvertProcess {
    private final EntityTable table;

    public EntityConverter(final EntityTable table) {
        this.table = table;
    }

    @SuppressWarnings({"deprecation", "unchecked"})
    @Override
    public void convertTable(ClickhouseConverter converter, ConvertOptions options, Connection connection) {
        CompletableFuture<Void> completionFuture = new CompletableFuture<>();

        Scheduler.runTask(CoreProtect.getInstance(), () -> {
            final long startTime = System.currentTimeMillis();
            long batchCount = 0;
            long deserializeNanos = 0L;
            long lookupNanos = 0L;
            long spawnSerializeNanos = 0L;
            final String sourceQuery = converter.formatSourceSelect(table, options, converter.formatSourceColumn("rowid", options) + ", time, hex(data)");

            converter.logger().info("Converting {}{} on the server thread...", table.fullName(), describeChunk(options));

            try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO " + table.fullName() + " (rowid, time, data) VALUES (?, ?, ?)");
                 PreparedStatement readStatement = connection.prepareStatement(sourceQuery);
                 PreparedStatement readTypeStatement = connection.prepareStatement("SELECT wid, x, y, z, type FROM " + ConfigHandler.prefix + "block WHERE data = ? LIMIT 1")) {

                final ResultSet rs = readStatement.executeQuery();
                while (converter.next(rs, insertStatement, batchCount)) {
                    final int rowId = rs.getInt(1);
                    insertStatement.setInt(1, rowId);
                    insertStatement.setInt(2, rs.getInt(2));

                    long timer = System.nanoTime();
                    final byte[] data = Bytes.fromHexString(rs.getString(3));
                    if (data == null) {
                        converter.logger().error("failed to deserialize entity data from hex at row {}", rowId);
                        continue;
                    }

                    List<Object> weirdData;

                    try (BukkitObjectInputStream bIn = new BukkitObjectInputStream(new ByteArrayInputStream(data))) {
                        weirdData = (List<Object>) bIn.readObject();
                    } catch (IOException | ClassNotFoundException e) {
                        converter.logger().error("failed to deserialize entity data via bukkit input stream at row {}", rowId, e);
                        continue;
                    }
                    deserializeNanos += System.nanoTime() - timer;

                    if (weirdData == null || weirdData.isEmpty()) {
                        converter.logger().info("Skipping empty entity data at row {}", rowId);
                        continue;
                    }

                    // this query assumes the co_block table is already converted to clickhouse
                    timer = System.nanoTime();
                    readTypeStatement.setInt(1, rowId);
                    try (ResultSet blockResult = readTypeStatement.executeQuery()) {
                        lookupNanos += System.nanoTime() - timer;

                        if (!blockResult.next()) {
                            converter.logger().warn("Could not find data for entity data at row id '{}', assuming obsolete (skipping)", rowId);
                            continue;
                        }

                        final int wid = blockResult.getInt(1);
                        final int x = blockResult.getInt(2);
                        final int y = blockResult.getInt(3);
                        final int z = blockResult.getInt(4);
                        final int type = blockResult.getInt(5);

                        final EntityType entityType = EntityUtils.getEntityType(type);
                        if (entityType == null) {
                            converter.logger().warn("Entity type id '{}' is not a valid entity type", type);
                            continue;
                        }

                        final String worldName = WorldUtils.getWorldName(wid);
                        final World world = Bukkit.getWorld(worldName);
                        if (world == null) {
                            converter.logger().warn("Could not find world with name '{}' (defined by world with id '{}')", worldName, wid);
                            continue;
                        }

                        timer = System.nanoTime();
                        final Location location = new Location(world, x, y, z);
                        final Entity entity = EntityUtil.spawnEntity(location, entityType, weirdData);

                        if (entity == null) {
                            converter.logger().warn("Failed to deserialize entity from legacy data at row id '{}'", rowId);
                            continue;
                        }

                        final String reserialized = EntityUtils.serializeEntity(entity);
                        entity.remove();
                        spawnSerializeNanos += System.nanoTime() - timer;
                        insertStatement.setString(3, reserialized);

                        insertStatement.addBatch();

                        if (++batchCount % 10000 == 0) {
                            insertStatement.executeBatch();
                            logProgress(converter, batchCount, startTime, deserializeNanos, lookupNanos, spawnSerializeNanos, options);
                        }
                    }
                }

                insertStatement.executeBatch();
                logProgress(converter, batchCount, startTime, deserializeNanos, lookupNanos, spawnSerializeNanos, options);
            } catch (SQLException e) {
                converter.logger().error("An sql exception occurred while running entity converter", e);
                completionFuture.completeExceptionally(e);
            } finally {
                if (!completionFuture.isCompletedExceptionally()) {
                    completionFuture.complete(null);
                }
            }
        });

        completionFuture.join();
    }

    private String describeChunk(ConvertOptions options) {
        if (!options.singleChunk()) {
            return options.offset() > 0 ? " from offset " + options.offset() : "";
        }

        return " chunk [" + options.sourceOffset() + ", " + (options.sourceOffset() + options.chunkSize()) + ")";
    }

    private void logProgress(ClickhouseConverter converter, long rows, long startTime, long deserializeNanos, long lookupNanos, long spawnSerializeNanos, ConvertOptions options) {
        long elapsedMillis = Math.max(1L, System.currentTimeMillis() - startTime);
        long rowsPerSecond = rows * 1000L / elapsedMillis;
        converter.logger().info(
                "Converted {} rows from {}{} ({} rows/sec, deserialize={}ms, block_lookup={}ms, spawn_serialize={}ms)",
                rows,
                table.fullName(),
                describeChunk(options),
                rowsPerSecond,
                deserializeNanos / 1_000_000L,
                lookupNanos / 1_000_000L,
                spawnSerializeNanos / 1_000_000L
        );
    }
}
