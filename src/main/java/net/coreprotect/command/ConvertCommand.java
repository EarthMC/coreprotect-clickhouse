package net.coreprotect.command;

import net.coreprotect.CoreProtect;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.database.Database;
import net.coreprotect.database.convert.ClickhouseConverter;
import net.coreprotect.database.convert.TableData;
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
            if (converter.mysqlAddress() == null || converter.mysqlDatabase() == null || converter.mysqlUser() == null || converter.mysqlPassword() == null) {
                sender.sendMessage(Component.text("Remote mysql database to import from has not been connected yet, use /co convert login <address> <database> <username> <password>", NamedTextColor.RED));
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
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "login" -> {
                if (args.length != 5) {
                    sender.sendMessage(Component.text("Usage: /co convert login <address> <database> <username> <password>", NamedTextColor.RED));
                    return;
                }

                converter.login(args[1], args[2], args[3], args[4]);
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
            case "prepare" -> {
                sender.sendMessage(Component.text("Preparing row numbers...", NamedTextColor.GREEN));

                Scheduler.runTaskAsynchronously(CoreProtect.getInstance(), () -> {
                    try (Connection connection = Database.getConnection(false)) {
                        Map<String, Long> counts = new HashMap<>();

                        for (final TableData table : converter.getTables().values()) {
                            try (PreparedStatement ps = connection.prepareStatement("select count(1) from " + converter.formatMysqlSource(table))) {
                                final ResultSet rs = ps.executeQuery();

                                if (rs.next()) {
                                    counts.put(table.getName(), rs.getLong(1) + 1);
                                    converter.logger().info("Max row number from table {}: {}", table.fullName(), rs.getLong(1) + 1);
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
                if (args.length != 1) {
                    sender.sendMessage(Component.text("Usage: /co convert <table name>", NamedTextColor.RED));
                    return;
                }

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

                final Thread migrationThread = new Thread(() -> {
                    final long startTime = System.currentTimeMillis();

                    try (Connection connection = Database.getConnection(true, 10000)) {
                        table.converter().convertTable(converter, connection);

                        converter.logger().info("Finished converting {}, took {}.", table.fullName(), DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - startTime));
                    } catch (SQLException e) {
                        converter.logger().error("SQL exception occurred while running converter", e);
                    }

                    ConfigHandler.converterRunning = false;
                });

                migrationThread.setName("CoreProtect ClickHouse Converter");
                migrationThread.start();
                migrationThread.setUncaughtExceptionHandler((thread, throwable) -> {
                    converter.logger().error("An exception occurred while running converter for {}", table.getName(), throwable);
                    ConfigHandler.converterRunning = false;
                });

                sender.sendMessage(Component.text("Started migrating table " + table.fullName() + ".", NamedTextColor.GREEN));
            }
        }
    }
}
