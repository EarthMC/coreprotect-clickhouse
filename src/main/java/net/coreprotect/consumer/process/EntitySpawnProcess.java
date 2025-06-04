package net.coreprotect.consumer.process;

import java.io.ByteArrayInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import net.coreprotect.CoreProtect;
import net.coreprotect.thread.Scheduler;
import net.coreprotect.utility.EntityUtils;
import net.coreprotect.utility.serialize.Bytes;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import net.coreprotect.config.ConfigHandler;
import net.coreprotect.utility.entity.EntityUtil;
import org.bukkit.util.io.BukkitObjectInputStream;

class EntitySpawnProcess {

    @SuppressWarnings({"unchecked", "deprecation"})
    static void process(Statement statement, Object object, int rowId) {
        if (object instanceof Object[]) {
            BlockState block = (BlockState) ((Object[]) object)[0];
            EntityType type = (EntityType) ((Object[]) object)[1];

            if (type == null) {
                return;
            }

            try (final PreparedStatement ps = statement.getConnection().prepareStatement("SELECT data, hex(data) as hexData FROM " + ConfigHandler.prefix + "entity WHERE rowid = ? LIMIT 1")) {
                ps.setInt(1, rowId);

                try (final ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return;
                    }

                    try {
                        final Entity deserialized = EntityUtils.deserializeEntity(rs.getString("data"), block.getWorld());
                        Scheduler.runTask(CoreProtect.getInstance(), () -> block.getWorld().addEntity(deserialized), block.getLocation());
                    } catch (Exception e) {
                        // attempt legacy spawn
                        final byte[] byteData = Bytes.fromHexString(rs.getString("hexData"));
                        if (byteData == null) {
                            return;
                        }

                        try (BukkitObjectInputStream bIn = new BukkitObjectInputStream(new ByteArrayInputStream(byteData))) {
                            List<Object> data = (List<Object>) bIn.readObject();
                            EntityUtil.spawnEntity(block, type, data);
                        } catch (Exception e2) {
                            CoreProtect.getInstance().getSLF4JLogger().warn("Failed to spawn entity", e2);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
