package net.coreprotect.patch.script;

import net.coreprotect.CoreProtect;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.language.Phrase;
import net.coreprotect.language.Selector;
import net.coreprotect.utility.Chat;

import java.sql.Statement;

public class __22_4_1 {

    protected static boolean patch(Statement statement) {
        try {
            statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "chat ADD COLUMN cancelled Bool;");
        }
        catch (Exception e) {
            Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "skull", Selector.FIRST, Selector.FIRST));
            CoreProtect.getInstance().getSLF4JLogger().warn("Exception while adding 'cancelled' column to chat table", e);
        }

        return true;
    }
}
