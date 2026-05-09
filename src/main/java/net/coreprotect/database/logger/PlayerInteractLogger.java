package net.coreprotect.database.logger;

import java.sql.PreparedStatement;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.block.BlockState;

import net.coreprotect.CoreProtect;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.data.QueuedBlockState;
import net.coreprotect.database.statement.BlockStatement;
import net.coreprotect.database.statement.UserStatement;
import net.coreprotect.event.CoreProtectPreLogEvent;
import net.coreprotect.utility.BlockTypeUtils;
import net.coreprotect.utility.MaterialUtils;
import net.coreprotect.utility.WorldUtils;

public class PlayerInteractLogger {

    private PlayerInteractLogger() {
        throw new IllegalStateException("Database class");
    }

    public static void log(PreparedStatement preparedStmt, int batchCount, String user, BlockState block, Material blockType) {
        log(preparedStmt, batchCount, user, block.getLocation(), block.getBlockData().getAsString(), blockType);
    }

    public static void log(PreparedStatement preparedStmt, int batchCount, String user, QueuedBlockState block, Material blockType) {
        log(preparedStmt, batchCount, user, block.location(), block.blockData(), blockType);
    }

    private static void log(PreparedStatement preparedStmt, int batchCount, String user, Location location, String blockData, Material blockType) {
        try {
            String blockKey = BlockTypeUtils.getBlockDataKey(blockData);
            if (blockKey.length() == 0 && blockType != null) {
                blockKey = blockType.getKey().toString();
            }
            if (blockKey.length() == 0) {
                return;
            }

            int type = MaterialUtils.getBlockId(blockKey, true);
            if (ConfigHandler.isBlacklisted(user) || BlockTypeUtils.isAir(blockKey)) {
                return;
            }

            CoreProtectPreLogEvent event = new CoreProtectPreLogEvent(user, location, CoreProtectPreLogEvent.Action.PLAYER_INTERACTION, 2, blockType, null, null);
            if (Config.getGlobal().API_ENABLED && !Bukkit.isPrimaryThread()) {
                CoreProtect.getInstance().getServer().getPluginManager().callEvent(event);
            }

            if (event.isCancelled()) {
                return;
            }

            int userId = UserStatement.getId(preparedStmt, event.getUser(), true);
            Location eventLocation = event.getLocation();
            int wid = WorldUtils.getWorldId(eventLocation);
            int time = (int) (System.currentTimeMillis() / 1000L);
            int x = eventLocation.getBlockX();
            int y = eventLocation.getBlockY();
            int z = eventLocation.getBlockZ();
            int data = 0;
            BlockStatement.insert(preparedStmt, batchCount, time, userId, wid, x, y, z, type, data, null, blockData, 2, 0);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
