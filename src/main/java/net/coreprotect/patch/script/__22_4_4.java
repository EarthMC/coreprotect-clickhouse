package net.coreprotect.patch.script;

import net.coreprotect.CoreProtect;
import net.coreprotect.config.ConfigHandler;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.sql.Statement;

public class __22_4_4 {
    protected static boolean patch(Statement statement) {
        final Logger logger = CoreProtect.getInstance().getSLF4JLogger();

        // The assumption of co_block.data never being negative is wrong due to armor stands using it to store their yaw in it
        try {
            statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "block ALTER COLUMN data TYPE Int32");
            return true;
        } catch (SQLException e) {
            logger.error("An unexpected exception happened while altering data type of the data column in the {}block table to Int32", ConfigHandler.prefix, e);
            return false;
        }
    }
}
