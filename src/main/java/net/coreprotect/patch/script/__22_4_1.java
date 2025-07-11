package net.coreprotect.patch.script;

import net.coreprotect.CoreProtect;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.language.Phrase;
import net.coreprotect.language.Selector;
import net.coreprotect.utility.Chat;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class __22_4_1 {

    protected static boolean patch(Statement statement) {
        try {
            statement.executeUpdate("alter table " + ConfigHandler.prefix + "chat add column cancelled Bool;");
        } catch (Exception e) {
            Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "chat", Selector.FIRST, Selector.FIRST));
            CoreProtect.getInstance().getSLF4JLogger().warn("Exception while adding 'cancelled' column to chat table", e);
        }

        final List<String> rolledBackUpdates = List.of("block", "container", "item");

        for (final String tableName : rolledBackUpdates) {
            try {
                statement.execute("alter table " + ConfigHandler.prefix + tableName + " modify column rolled_back UInt8");
            } catch (SQLException e) {
                Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + tableName, Selector.FIRST, Selector.FIRST));
                CoreProtect.getInstance().getSLF4JLogger().warn("Exception while modifying 'rolled_back' column for {} table", tableName, e);
            }
        }

        return true;
    }
}
