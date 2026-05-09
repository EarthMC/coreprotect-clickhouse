package net.coreprotect.consumer.data;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;

import net.coreprotect.utility.BlockUtils;
import net.coreprotect.utility.serialize.SerializedBlockMeta;

public record QueuedBlockState(Location location, Material type, String blockData, SerializedBlockMeta meta) {

    public static QueuedBlockState capture(BlockState blockState) {
        return new QueuedBlockState(QueuedLocation.capture(blockState.getLocation()), blockState.getType(), blockState.getBlockData().getAsString(), BlockUtils.processMeta(blockState));
    }
}