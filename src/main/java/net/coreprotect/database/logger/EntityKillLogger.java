package net.coreprotect.database.logger;

import java.sql.PreparedStatement;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;

import net.coreprotect.CoreProtect;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.database.statement.BlockStatement;
import net.coreprotect.database.statement.EntityStatement;
import net.coreprotect.database.statement.UserStatement;
import net.coreprotect.event.CoreProtectPreLogEvent;
import net.coreprotect.utility.WorldUtils;

public class EntityKillLogger {

    private EntityKillLogger() {
        throw new IllegalStateException("Database class");
    }

    public static void log(PreparedStatement preparedStmt, PreparedStatement preparedStmt2, int batchCount, String user, Block block, String entityData, int type) {
        try {
            if (ConfigHandler.blacklist.get(user.toLowerCase(Locale.ROOT)) != null) {
                return;
            }

            CoreProtectPreLogEvent event = new CoreProtectPreLogEvent(user);
            if (Config.getGlobal().API_ENABLED && !Bukkit.isPrimaryThread()) {
                CoreProtect.getInstance().getServer().getPluginManager().callEvent(event);
            }

            if (event.isCancelled()) {
                return;
            }

            int userId = UserStatement.getId(preparedStmt, event.getUser(), true);
            int wid = WorldUtils.getWorldId(block.getWorld().getName());
            int time = (int) (System.currentTimeMillis() / 1000L);
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();
            int entity_key = EntityStatement.insert(preparedStmt2, time, entityData);

            BlockStatement.insert(preparedStmt, batchCount, time, userId, wid, x, y, z, type, entity_key, null, null, 3, 0);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
