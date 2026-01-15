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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ConvertCommand {
    private static final ClickhouseConverter converter = new ClickhouseConverter(CoreProtect.getInstance());

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

                sender.sendMessage(Component.text("Preparing row numbers...", NamedTextColor.GREEN));

                Scheduler.runTaskAsynchronously(CoreProtect.getInstance(), () -> {
                    try (Connection connection = Database.getConnection(false)) {
                        Map<String, Long> counts = new HashMap<>();

                        for (final TableData table : converter.getTables().values()) {
                            converter.logger().info("Querying row count for table {}...", table.fullName());

                            final PreparedStatement ps = connection.prepareStatement(switch (access) {
                                case MySQLLoginInformation(String address, String database, String user, String password) -> "SELECT AUTO_INCREMENT FROM mysql('" + address + "', 'information_schema', 'TABLES', '" + user + "', '" + password + "') WHERE TABLE_SCHEMA = '" + database + "' AND TABLE_NAME = '" + table.fullName() + "'";
                                case SQLiteDatabaseInformation ignored -> "SELECT MAX(rowid) + 1 FROM " + converter.formatMysqlSource(table);
                            });

                            try (ps; final ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                    counts.put(table.getName(), rs.getLong(1));
                                    converter.logger().info("Max row number from table {}: {}", table.fullName(), rs.getLong(1));
                                } else {
                                    converter.logger().error("Could not query row count for table {}", table.fullName());
                                }
                            }
                        }

                        CoreProtect.getInstance().rowNumbers().set(counts);
                        CoreProtect.getInstance().rowNumbers().save();
                        converter.logger().info("Saved row number counts to row-numbers.json");
                    } catch (SQLException e) {
                        converter.logger().error("An sql exception occurred while preparing row numbers", e);
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

                ConfigHandler.converterRunning = true;

                boolean truncate = false;
                long offset = 0L;

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
                        } else {
                            sender.sendMessage(Component.text("Unrecognized option: " + arg, NamedTextColor.RED));
                            return;
                        }
                    }
                }

                final ConvertOptions options = new ConvertOptions(truncate, offset);

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

                        try {
                            table.converter().convertTable(converter, options, connection);
                        } catch (CorruptResultRowException e) {
                            converter.logger().error("Encountered a corrupt row in resultset at offset {}, attempting self healing.", e.getRowNumber());

                            int retryCount = 0;
                            long newOffset = options.offset() + e.getRowNumber();
                            boolean finished = false;

                            while (retryCount <= 999 && !finished) {
                                try {
                                    table.converter().convertTable(converter, new ConvertOptions(false, newOffset), connection);
                                    finished = true;
                                } catch (CorruptResultRowException e2) {
                                    retryCount++;
                                    newOffset += e2.getRowNumber();
                                    converter.logger().error("Encountered another corrupt row at offset {}, re-attempting (remaining attempts: {})", newOffset, 10 - retryCount);
                                }
                            }
                        }

                        converter.logger().info("Finished converting {}, took {}.", table.fullName(), DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - startTime));
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
}
