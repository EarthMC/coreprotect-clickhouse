package net.coreprotect.patch.script;

import net.coreprotect.CoreProtect;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.sql.Statement;

public class __22_4_3 {
    protected static boolean patch(Statement statement) {
        // Move and re-create the entity table since we changed the data type from JSON to string, the old data was never really readable.

        final Logger logger = CoreProtect.getInstance().getSLF4JLogger();

        try {
            logger.info("Re-creating the entity table.");
            statement.execute("CREATE TABLE " + ConfigHandler.prefix + "entity_old_json(rowid UInt64, time UInt32, data String) ENGINE = MergeTree ORDER BY (rowid) PARTITION BY " + Config.getGlobal().PARTITIONING);
            statement.execute("EXCHANGE TABLES " + ConfigHandler.prefix + "entity AND " + ConfigHandler.prefix + "entity_old_json");

            logger.info("Successfully re-created and swapped the entity data table.");
            return true;
        } catch (SQLException e) {
            logger.error("An unexpected SQL exception occurred", e);
            return false;
        }
    }
}
