package net.coreprotect.database.logger;

import java.sql.PreparedStatement;
import java.util.Locale;

import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;

import net.coreprotect.config.ConfigHandler;
import net.coreprotect.database.statement.SkullStatement;
import net.coreprotect.paper.PaperAdapter;
import net.coreprotect.utility.MaterialUtils;

public class SkullBreakLogger {

    private SkullBreakLogger() {
        throw new IllegalStateException("Database class");
    }

    public static void log(PreparedStatement preparedStmt, PreparedStatement preparedStmt2, int batchCount, String user, BlockState block) {
        try {
            if (ConfigHandler.blacklist.get(user.toLowerCase(Locale.ROOT)) != null || block == null) {
                return;
            }
            int time = (int) (System.currentTimeMillis() / 1000L);
            int type = MaterialUtils.getBlockId(block.getType().name(), true);
            Skull skull = (Skull) block;
            int skullKey = 0;
            if (skull.hasOwner()) {
                String skullOwner = PaperAdapter.ADAPTER.getSkullOwner(skull);
                String skullSkin = PaperAdapter.ADAPTER.getSkullSkin(skull);
                skullKey = SkullStatement.insert(preparedStmt2, time, skullOwner, skullSkin);
            }

            BlockBreakLogger.log(preparedStmt, batchCount, user, block.getLocation(), type, skullKey, null, block.getBlockData().getAsString(), null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
