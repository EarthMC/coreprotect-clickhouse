package net.coreprotect.consumer.process;

import java.sql.PreparedStatement;

import com.destroystokyo.paper.MaterialSetTag;
import com.destroystokyo.paper.MaterialTags;
import net.coreprotect.utility.serialize.SerializedBlockMeta;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockState;

import net.coreprotect.consumer.data.QueuedBlockState;
import net.coreprotect.database.logger.BlockPlaceLogger;
import net.coreprotect.database.logger.SkullPlaceLogger;
import net.coreprotect.utility.BlockUtils;
import net.coreprotect.utility.MaterialUtils;

class BlockPlaceProcess {

    static void process(PreparedStatement preparedStmt, PreparedStatement preparedStmtSkulls, int batchCount, Material blockType, int blockData, Material replaceType, int replaceData, int forceData, String user, Object object, String newBlockData, String replacedBlockData) {
        if (object instanceof QueuedBlockState queuedBlock) {
            //if (blockType != null && MaterialTags.SKULLS.isTagged(blockType)) { // log as block place
            //    SkullPlaceLogger.log(preparedStmt, preparedStmtSkulls, batchCount, user, block, MaterialUtils.getBlockId(replaceType), replaceData);
            //}
            if (forceData == 1) {
                BlockPlaceLogger.log(preparedStmt, batchCount, user, queuedBlock, MaterialUtils.getBlockId(replaceType), replaceData, blockType, blockData, true, newBlockData, replacedBlockData);
            }
            else {
                BlockPlaceLogger.log(preparedStmt, batchCount, user, queuedBlock, MaterialUtils.getBlockId(replaceType), replaceData, blockType, blockData, false, newBlockData, replacedBlockData);
            }
        }
        else if (object instanceof BlockState) {
            BlockState block = (BlockState) object;
            SerializedBlockMeta meta = BlockUtils.processMeta(block);
            if (blockType != null && MaterialTags.SKULLS.isTagged(blockType)) {
                SkullPlaceLogger.log(preparedStmt, preparedStmtSkulls, batchCount, user, block, MaterialUtils.getBlockId(replaceType), replaceData);
            }
            else if (forceData == 1) {
                BlockPlaceLogger.log(preparedStmt, batchCount, user, block, MaterialUtils.getBlockId(replaceType), replaceData, blockType, blockData, true, meta, newBlockData, replacedBlockData);
            }
            else {
                BlockPlaceLogger.log(preparedStmt, batchCount, user, block, MaterialUtils.getBlockId(replaceType), replaceData, blockType, blockData, false, meta, newBlockData, replacedBlockData);
            }
        }
    }
}
