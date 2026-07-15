package net.coreprotect.patch.script;

import net.coreprotect.CoreProtect;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class __22_5_0 {
    protected static boolean patch(Statement statement) {
        // Re-create the block, item and container tables in order to change the replace

        final Logger logger = CoreProtect.getInstance().getSLF4JLogger();
        final List<String> tables = List.of("block", "item", "container");
        final String prefix = ConfigHandler.prefix;

        final String partitionBy = "PARTITION BY " + Config.getGlobal().PARTITIONING;
        final String engine = " ENGINE = ReplacingMergeTree(version) ";
        final String orderBy = "ORDER BY (wid, y, x, z, time, user, type) ";

        final String createBlock = "(rowid UInt64, time UInt32, user UInt32, wid UInt32, x Int32, y Int16, z Int32, type UInt32, data Int64, meta String, blockdata LowCardinality(String), action UInt8, rolled_back UInt8, version UInt8)" + engine + orderBy + partitionBy;
        final String createItem = "(rowid UInt64, time UInt32, user UInt32, wid UInt32, x Int32, y Int16, z Int32, type UInt32, data String, amount UInt32, action UInt8, rolled_back UInt8, version UInt8)" + engine + orderBy + partitionBy;
        final String createContainer = "(rowid UInt64, time UInt32, user UInt32, wid UInt32, x Int32, y Int16, z Int32, type UInt32, data UInt32, amount UInt32, metadata String, action UInt8, rolled_back UInt8, version UInt8)" + engine + orderBy + partitionBy;

        try {
            logger.info("Creating temporary block/item/container tables.");
            statement.execute("CREATE TABLE " + prefix + "swapped_block" + createBlock);
            statement.execute("CREATE TABLE " + prefix + "swapped_item" + createItem);
            statement.execute("CREATE TABLE " + prefix + "swapped_container" + createContainer);

            for (final String table : tables) {
                logger.info("Copying data over from {} table...", table);

                statement.execute("INSERT INTO " + ConfigHandler.prefix + "swapped_" + table + " SELECT * FROM " + ConfigHandler.prefix + table);
            }

            logger.info("Finished copying data to temporary tables, exchanging tables");
            statement.executeUpdate("EXCHANGE TABLES " + prefix + "block AND " + prefix + "swapped_block, " + prefix + "item AND " + prefix + "swapped_item, " + prefix + "container AND " + prefix + "swapped_container");

            logger.info("Finished exchanging tables, you may manually delete the temporary '{}swapped' tables if all data is correct.", prefix);
            return true;
        } catch (SQLException e) {
            logger.error("An unexpected SQL exception occurred", e);
            return false;
        }
    }

}
