package net.coreprotect.command;

import net.coreprotect.CoreProtect;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.database.Database;
import net.coreprotect.database.convert.ClickhouseConverter;
import net.coreprotect.database.convert.ClickhouseConverter.MySQLLoginInformation;
import net.coreprotect.database.convert.ClickhouseConverter.SQLiteDatabaseInformation;
import net.coreprotect.database.convert.TableData;
import net.coreprotect.database.convert.process.ConvertOptions;
import net.coreprotect.database.convert.process.CorruptResultRowException;
import net.coreprotect.thread.Scheduler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ConvertCommand {
    private static final ClickhouseConverter converter = new ClickhouseConverter(CoreProtect.getInstance());
    private static volatile boolean prepareRunning = false;

    protected static void runCommand(final CommandSender sender, boolean permission, String[] args) {
        if (!permission) {
            return;
        }

        String[] temp = new String[args.length - 1];
        System.arraycopy(args, 1, temp, 0, args.length - 1);
        args = temp;

        if (sender instanceof Player) {
            sender.sendMessage(Component.text("This command can only be executed by console.", NamedTextColor.RED));
            return;
        }

        if (args.length == 0) {
            if (converter.databaseAccess() == null) {
                sender.sendMessage(Component.text("Remote mysql database to import from has not been connected yet, use /co convert login [mysql|sqlite] [<address> <database> <username> <password>|<database path>]", NamedTextColor.RED));
                return;
            }

            final TableData versionTable = converter.getTable("version");

            try (Connection connection = Database.getConnection(false);
                 PreparedStatement preparedStatement = connection.prepareStatement("select version from " + converter.formatMysqlSource(versionTable) + " order by time desc")) {

                final ResultSet rs = preparedStatement.executeQuery();
                String version = "<unknown>";
                if (rs.next()) {
                    version = rs.getString("version");
                }

                sender.sendMessage(Component.text("Currently connected to the mysql database, database version: " + version, NamedTextColor.GREEN));
            } catch (SQLException e) {
                sender.sendMessage(Component.text("Failed to connect to the mysql database using the provided credentials.", NamedTextColor.RED));
                converter.logger().error("Failed to connect to the mysql database using the provided credentials.", e);
            }
            return;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "login" -> {
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /co convert login [mysql|sqlite] [<address> <database> <username> <password>|<database path>]", NamedTextColor.RED));
                    return;
                }

                final String databaseType = args[1];

                if ("mysql".equalsIgnoreCase(databaseType)) {
                    if (args.length != 6) {
                        sender.sendMessage(Component.text("Usage: /co convert login mysql <address> <database> <username> <password>", NamedTextColor.RED));
                        return;
                    }

                    converter.login(new MySQLLoginInformation(args[2], args[3], args[4], args[5]));
                } else if ("sqlite".equalsIgnoreCase(databaseType)) {
                    if (args.length != 3) {
                        sender.sendMessage(Component.text("Usage: /co convert login sqlite <database path>", NamedTextColor.RED));
                        return;
                    }

                    converter.login(new SQLiteDatabaseInformation(args[2]));
                } else {
                    sender.sendMessage(Component.text("Unknown database type '" + databaseType + "', valid options are mysql and sqlite.", NamedTextColor.RED));
                    return;
                }

                final TableData versionTable = converter.getTable("version");

                try (Connection connection = Database.getConnection(false);
                     PreparedStatement preparedStatement = connection.prepareStatement("select version from " + converter.formatMysqlSource(versionTable) + " order by time desc")) {

                    final ResultSet rs = preparedStatement.executeQuery();
                    String version = "<unknown>";
                    if (rs.next()) {
                        version = rs.getString("version");
                    }

                    sender.sendMessage(Component.text("Successfully connected to the mysql database, database version: " + version, NamedTextColor.GREEN));

                    converter.saveCredentials();
                } catch (SQLException e) {
                    sender.sendMessage(Component.text("Failed to connect to the mysql database using the provided credentials.", NamedTextColor.RED));
                    converter.logger().error("Failed to connect to the mysql database using the provided credentials.", e);
                }
            }
            case "logout" -> {
                if (converter.databaseAccess() == null) {
                    sender.sendMessage(Component.text("No login credentials are currently saved.", NamedTextColor.RED));
                    return;
                }

                converter.login(null);
                converter.saveCredentials();

                sender.sendMessage(Component.text("Saved credentials have successfully been deleted from memory & disk.", NamedTextColor.GREEN));
            }
            case "prepare" -> {
                final ClickhouseConverter.DatabaseAccess access = converter.databaseAccess();
                if (access == null) {
                    sender.sendMessage(Component.text("No remote database has been defined yet, use /co convert login first.", NamedTextColor.RED));
                    return;
                }

                if (prepareRunning) {
                    sender.sendMessage(Component.text("A row number preparation is already ongoing.", NamedTextColor.RED));
                    return;
                }

                prepareRunning = true;
                sender.sendMessage(Component.text("Preparing row numbers...", NamedTextColor.GREEN));

                Scheduler.runTaskAsynchronously(CoreProtect.getInstance(), () -> {
                    try {
                        Map<String, Long> counts = prepareRowNumbers(access);
                        CoreProtect.getInstance().rowNumbers().set(counts);
                        CoreProtect.getInstance().rowNumbers().save();
                        converter.logger().info("Saved row number counts to row-numbers.json");
                    } catch (SQLException e) {
                        converter.logger().error("An sql exception occurred while preparing row numbers", e);
                    } finally {
                        prepareRunning = false;
                    }
                });
            }
            default -> {
                final TableData table = converter.getTable(args[0]);
                if (table == null) {
                    sender.sendMessage(Component.text("Could not find data for table with name " + args[0] + ".", NamedTextColor.RED));
                    return;
                }

                if (ConfigHandler.converterRunning) {
                    sender.sendMessage(Component.text("Another conversion is already ongoing.", NamedTextColor.RED));
                    return;
                }

                boolean truncate = false;
                long offset = 0L;
                long chunkSize = ConvertOptions.UNBOUNDED_CHUNK_SIZE;
                int workers = ConvertOptions.DEFAULT_WORKERS;

                if (args.length > 1) {
                    for (int i = 1; i < args.length; i++) {
                        String arg = args[i].trim();

                        if (arg.isEmpty()) {
                            continue;
                        }

                        if (arg.equals("-t")) {
                            truncate = true;
                        } else if (arg.startsWith("o:") || arg.startsWith("offset:")) {
                            String value = arg.split(":", 2)[1];
                            try {
                                offset = Long.parseLong(value);
                            } catch (NumberFormatException e) {
                                sender.sendMessage(Component.text("Failed to parse offset value '" + value + "' as a number.", NamedTextColor.RED));
                                return;
                            }
                        } else if (arg.startsWith("chunk:")) {
                            String value = arg.split(":", 2)[1];
                            try {
                                chunkSize = Long.parseLong(value);
                            } catch (NumberFormatException e) {
                                sender.sendMessage(Component.text("Failed to parse chunk size '" + value + "' as a number.", NamedTextColor.RED));
                                return;
                            }
                        } else if (arg.startsWith("workers:")) {
                            String value = arg.split(":", 2)[1];
                            try {
                                workers = Integer.parseInt(value);
                            } catch (NumberFormatException e) {
                                sender.sendMessage(Component.text("Failed to parse worker count '" + value + "' as a number.", NamedTextColor.RED));
                                return;
                            }
                        } else {
                            sender.sendMessage(Component.text("Unrecognized option: " + arg, NamedTextColor.RED));
                            return;
                        }
                    }
                }

                if (chunkSize < 0) {
                    sender.sendMessage(Component.text("Chunk size cannot be negative.", NamedTextColor.RED));
                    return;
                }

                if (workers < 1) {
                    sender.sendMessage(Component.text("Worker count cannot be less than 1.", NamedTextColor.RED));
                    return;
                }

                if (workers > 1 && !canParallelize(table)) {
                    sender.sendMessage(Component.text("Parallel conversion is currently only supported for block, item, and container.", NamedTextColor.RED));
                    return;
                }

                if (chunkSize == ConvertOptions.UNBOUNDED_CHUNK_SIZE && canChunk(table)) {
                    chunkSize = ConvertOptions.DEFAULT_CHUNK_SIZE;
                }

                final ConvertOptions options = new ConvertOptions(truncate, offset, chunkSize, ConvertOptions.NO_CHUNK_START, workers);
                ConfigHandler.converterRunning = true;

                final Thread migrationThread = new Thread(() -> {
                    final long startTime = System.currentTimeMillis();

                    try (Connection connection = Database.getConnection(true, 10000)) {
                        if (options.truncate()) {
                            converter.logger().info("Truncating destination table {}...", table.fullName());

                            try (Statement statement = connection.createStatement()) {
                                statement.execute("TRUNCATE TABLE " + table.fullName());
                                converter.logger().info("Truncated table {}.", table.fullName());
                            } catch (SQLException e) {
                                converter.logger().error("Failed to truncate table {}", table.fullName(), e);
                            }
                        }

                        converter.logger().info("Started migrating {}...", table.fullName());

                        if (options.chunked()) {
                            convertChunked(table, options, connection);
                        } else {
                            convertWithRetries(table, options, connection);
                        }

                        converter.logger().info("Finished converting {}, took {}.", table.fullName(), DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - startTime));
                        if (reloadTypeCaches(table)) {
                            try (Statement statement = connection.createStatement()) {
                                ConfigHandler.loadTypes(statement);
                            }
                            converter.logger().info("Reloaded type caches after converting {}.", table.fullName());
                        }
                    } catch (SQLException e) {
                        converter.logger().error("SQL exception occurred while running converter", e);
                    }

                    ConfigHandler.converterRunning = false;
                });

                sender.sendMessage(Component.text("Started migrating table " + table.fullName() + ".", NamedTextColor.GREEN));

                migrationThread.setName("CoreProtect ClickHouse Converter");
                migrationThread.start();
                migrationThread.setUncaughtExceptionHandler((thread, throwable) -> {
                    converter.logger().error("An exception occurred while running converter for {}", table.getName(), throwable);
                    ConfigHandler.converterRunning = false;
                });
            }
        }
    }

    private static boolean reloadTypeCaches(TableData table) {
        return switch (table.getName()) {
            case "art_map", "blockdata_map", "entity_map", "material_map" -> true;
            default -> false;
        };
    }

    private static Map<String, Long> prepareRowNumbers(ClickhouseConverter.DatabaseAccess access) throws SQLException {
        if (access instanceof SQLiteDatabaseInformation sqlite) {
            Map<String, Long> localCounts = prepareLocalSqliteRowNumbers(sqlite);
            if (localCounts != null) {
                return localCounts;
            }

            converter.logger().info("Preparing row numbers through ClickHouse because local SQLite access is unavailable.");
        }

        try (Connection connection = Database.getConnection(false)) {
            Map<String, Long> counts = new HashMap<>();

            for (final TableData table : converter.getTables().values()) {
                converter.logger().info("Querying row count for table {}...", table.fullName());

                final PreparedStatement ps = connection.prepareStatement(converter.formatSourceRowCount(table));

                try (ps; final ResultSet rs = executePrepareRowCountQuery(table, ps)) {
                    if (rs.next()) {
                        counts.put(table.getName(), rs.getLong(1));
                        converter.logger().info("Max row number from table {}: {}", table.fullName(), rs.getLong(1));
                    } else {
                        converter.logger().error("Could not query row count for table {}", table.fullName());
                    }
                }
            }

            return counts;
        }
    }

    private static Map<String, Long> prepareLocalSqliteRowNumbers(SQLiteDatabaseInformation sqlite) throws SQLException {
        Path databasePath = Path.of(sqlite.databasePath()).toAbsolutePath();
        if (!Files.isRegularFile(databasePath)) {
            converter.logger().info("SQLite database file '{}' is not available locally; using ClickHouse row counting.", databasePath);
            return null;
        }

        Map<String, Long> counts = new HashMap<>();
        final String firstTableName = converter.getTables().values().iterator().next().fullName();

        try (Connection connection = DriverManager.getConnection(formatReadOnlySqliteJdbcUrl(databasePath))) {
            if (!sqliteTableExists(connection, firstTableName)) {
                converter.logger().warn("SQLite database at '{}' does not contain expected table '{}'; using ClickHouse row counting.", databasePath, firstTableName);
                return null;
            }

            converter.logger().info("Preparing row numbers directly from local SQLite database '{}'.", databasePath);
            for (final TableData table : converter.getTables().values()) {
                converter.logger().info("Querying max SQLite rowid for table {}...", table.fullName());

                try (PreparedStatement ps = connection.prepareStatement("SELECT COALESCE(MAX(rowid), 0) + 1 FROM " + quoteSqliteIdentifier(table.fullName()));
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        counts.put(table.getName(), rs.getLong(1));
                        converter.logger().info("Max row number from table {}: {}", table.fullName(), rs.getLong(1));
                    } else {
                        converter.logger().error("Could not query max SQLite rowid for table {}", table.fullName());
                    }
                }
            }
        }

        return counts;
    }

    private static String formatReadOnlySqliteJdbcUrl(Path databasePath) {
        return "jdbc:sqlite:" + databasePath.toUri() + "?mode=ro";
    }

    private static boolean sqliteTableExists(Connection connection, String tableName) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ? LIMIT 1")) {
            ps.setString(1, tableName);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static String quoteSqliteIdentifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    private static ResultSet executePrepareRowCountQuery(TableData table, PreparedStatement preparedStatement) throws SQLException {
        final long startTime = System.currentTimeMillis();
        ScheduledExecutorService progressLogger = Executors.newSingleThreadScheduledExecutor();

        try {
            progressLogger.scheduleAtFixedRate(() -> converter.logger().info(
                    "Still querying row count for table {} after {}...",
                    table.fullName(),
                    DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - startTime)
            ), 30, 30, TimeUnit.SECONDS);

            return preparedStatement.executeQuery();
        } finally {
            progressLogger.shutdownNow();
        }
    }

    private static boolean canChunk(TableData table) {
        return switch (table.getName()) {
            case "block", "item", "container" -> true;
            default -> false;
        };
    }

    private static boolean canParallelize(TableData table) {
        return canChunk(table);
    }

    private static long querySourceLimit(TableData table, Connection connection) throws SQLException {
        Path localSqliteDatabasePath = converter.localSqliteDatabasePath();
        if (localSqliteDatabasePath != null) {
            converter.logger().info("Querying max local SQLite rowid for table {}...", table.fullName());
            try (Connection sqliteConnection = DriverManager.getConnection(formatReadOnlySqliteJdbcUrl(localSqliteDatabasePath));
                 PreparedStatement preparedStatement = sqliteConnection.prepareStatement("SELECT COALESCE(MAX(rowid), 0) FROM " + quoteSqliteIdentifier(table.fullName()));
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    return 0L;
                }

                long maxRowId = Math.max(0L, resultSet.getLong(1));
                converter.logger().info("Max local SQLite rowid for table {}: {}", table.fullName(), maxRowId);
                return maxRowId;
            }
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(converter.formatSourceChunkLimit(table));
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (!resultSet.next()) {
                return 0L;
            }

            return Math.max(0L, resultSet.getLong(1));
        }
    }

    private static void convertChunked(TableData table, ConvertOptions options, Connection connection) throws SQLException {
        final long sourceLimit = querySourceLimit(table, connection);
        if (options.offset() >= sourceLimit) {
            converter.logger().info("Skipping {}, offset {} is at or beyond source limit {}.", table.fullName(), options.offset(), sourceLimit);
            return;
        }

        if (options.workers() == 1) {
            for (long chunkStart = 0; options.offset() + chunkStart < sourceLimit; chunkStart += options.chunkSize()) {
                convertWithRetries(table, options.withChunk(chunkStart), connection);
            }
            return;
        }

        convertChunksInParallel(table, options, sourceLimit);
    }

    private static void convertChunksInParallel(TableData table, ConvertOptions options, long sourceLimit) throws SQLException {
        converter.logger().info("Converting {} with {} workers and chunk size {}.", table.fullName(), options.workers(), options.chunkSize());

        ExecutorService executorService = Executors.newFixedThreadPool(options.workers());
        List<Future<?>> futures = new ArrayList<>();

        try {
            for (long chunkStart = 0; options.offset() + chunkStart < sourceLimit; chunkStart += options.chunkSize()) {
                final ConvertOptions chunkOptions = options.withChunk(chunkStart);
                futures.add(executorService.submit(() -> {
                    try (Connection workerConnection = Database.getConnection(true, 10000)) {
                        convertWithRetries(table, chunkOptions, workerConnection);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }));
            }

            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("Interrupted while waiting for converter workers", e);
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof RuntimeException runtimeException && runtimeException.getCause() instanceof SQLException sqlException) {
                        throw sqlException;
                    }

                    throw new SQLException("Converter worker failed", cause);
                }
            }
        } finally {
            executorService.shutdownNow();
        }
    }

    private static void convertWithRetries(TableData table, ConvertOptions options, Connection connection) throws SQLException {
        ConvertOptions currentOptions = options;
        int retryCount = 0;

        while (true) {
            try {
                table.converter().convertTable(converter, currentOptions, connection);
                return;
            } catch (CorruptResultRowException e) {
                retryCount++;
                if (retryCount > 999) {
                    throw e;
                }

                final long newOffset = currentOptions.sourceOffset() + e.getRowNumber();
                final long remainingChunkSize = currentOptions.singleChunk() && currentOptions.chunked() ? Math.max(0L, currentOptions.sourceOffset() + currentOptions.chunkSize() - newOffset) : currentOptions.chunkSize();
                if (currentOptions.singleChunk() && remainingChunkSize == 0) {
                    converter.logger().warn("Finished {} chunk after recovering from a corrupt result row at offset {}.", table.fullName(), newOffset);
                    return;
                }

                converter.logger().error("Encountered a corrupt row in resultset at offset {}, re-attempting (remaining attempts: {})", newOffset, 999 - retryCount);
                currentOptions = new ConvertOptions(false, newOffset, remainingChunkSize, currentOptions.chunked() ? 0L : ConvertOptions.NO_CHUNK_START, 1);
            }
        }
    }
}
