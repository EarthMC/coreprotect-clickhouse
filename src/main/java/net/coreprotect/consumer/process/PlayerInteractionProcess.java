package net.coreprotect.consumer.process;

import java.sql.PreparedStatement;

import org.bukkit.Material;
import org.bukkit.block.BlockState;

import net.coreprotect.consumer.data.QueuedBlockState;
import net.coreprotect.database.logger.PlayerInteractLogger;

class PlayerInteractionProcess {

    static void process(PreparedStatement preparedStmt, int batchCount, String user, Object object, Material type) {
        if (object instanceof QueuedBlockState queuedBlock) {
            PlayerInteractLogger.log(preparedStmt, batchCount, user, queuedBlock, type);
        }
        else if (object instanceof BlockState) {
            BlockState block = (BlockState) object;
            PlayerInteractLogger.log(preparedStmt, batchCount, user, block, type);
        }
    }
}
