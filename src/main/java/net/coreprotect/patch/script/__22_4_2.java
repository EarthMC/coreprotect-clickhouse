package net.coreprotect.patch.script;

import net.coreprotect.CoreProtect;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.language.Phrase;
import net.coreprotect.language.Selector;
import net.coreprotect.utility.Chat;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class __22_4_2 {
    protected static boolean patch(Statement statement) {
        try {
            statement.executeUpdate("alter table " + ConfigHandler.prefix + "command add column cancelled Bool;");
        } catch (SQLException e) {
            Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "command", Selector.FIRST, Selector.FIRST));
            CoreProtect.getInstance().getSLF4JLogger().warn("Exception while adding 'cancelled' column to command table", e);
        }

        // Re-create the block, item and container tables in order to change the order by to add the y coordinate to it

        final Logger logger = CoreProtect.getInstance().getSLF4JLogger();
        final List<String> tables = List.of("block", "item", "container");

        final String partitionBy = "PARTITION BY " + Config.getGlobal().PARTITIONING;
        final String orderBy = "ORDER BY (wid, y, x, z, time, user, type)";

        final String createBlock = "(rowid UInt64, time UInt32, user UInt32, wid UInt32, x Int32, y Int16, z Int32, type UInt32, data UInt32, meta JSON, blockdata LowCardinality(String), action UInt8, rolled_back UInt8, version UInt8) ENGINE = ReplacingMergeTree(version) " + orderBy + partitionBy;
        final String createItem = "(rowid UInt64, time UInt32, user UInt32, wid UInt32, x Int32, y Int16, z Int32, type UInt32, data JSON, amount UInt32, action UInt8, rolled_back UInt8, version UInt8) ENGINE = ReplacingMergeTree(version) " + orderBy + partitionBy;
        final String createContainer = "(rowid UInt64, time UInt32, user UInt32, wid UInt32, x Int32, y Int16, z Int32, type UInt32, data UInt32, amount UInt32, metadata JSON, action UInt8, rolled_back UInt8, version UInt8) ENGINE = ReplacingMergeTree(version) " + orderBy + partitionBy;

        try {
            logger.info("Creating temporary block/item/container tables.");
            statement.execute("CREATE TABLE " + ConfigHandler.prefix + "temp_block" + createBlock);
            statement.execute("CREATE TABLE " + ConfigHandler.prefix + "temp_item" + createItem);
            statement.execute("CREATE TABLE " + ConfigHandler.prefix + "temp_container" + createContainer);

            for (final String table : tables) {
                logger.info("Copying data over from {} table...", table);

                statement.execute("INSERT INTO " + ConfigHandler.prefix + "temp_" + table + " SELECT * FROM " + ConfigHandler.prefix + table);
            }

            logger.info("Finished copying data to temporary tables, deleting existing tables");
            for (final String table : tables) {
                statement.execute("DROP TABLE " + ConfigHandler.prefix + table);
            }

            logger.info("Re-creating tables with updated order by");
            statement.execute("CREATE TABLE " + ConfigHandler.prefix + "block" + createBlock);
            statement.execute("CREATE TABLE " + ConfigHandler.prefix + "item" + createItem);
            statement.execute("CREATE TABLE " + ConfigHandler.prefix + "container" + createContainer);

            logger.info("Inserting data back into tables...");
            for (final String table : tables) {
                logger.info("Copying data over from temp_{} table...", table);

                statement.execute("INSERT INTO " + ConfigHandler.prefix + table + " SELECT * FROM " + ConfigHandler.prefix + "temp_" + table);
            }

            logger.info("Finished inserting data back into the re-created tables, you may manually delete the temporary tables if all data is correct.");
            return true;
        } catch (SQLException e) {
            logger.error("An unexpected SQL exception occurred", e);
            return false;
        }
    }
}
