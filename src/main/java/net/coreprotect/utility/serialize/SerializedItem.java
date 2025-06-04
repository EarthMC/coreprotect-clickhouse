package net.coreprotect.utility.serialize;

import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

public record SerializedItem(ItemStack itemStack, @Nullable Integer slot, @Nullable BlockFace faceData) {
    public static SerializedItem of(ItemStack itemStack) {
        return new SerializedItem(itemStack, null, null);
    }
}
