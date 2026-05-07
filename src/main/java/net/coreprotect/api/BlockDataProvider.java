package net.coreprotect.api;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

/**
 * Interface for plugins to provide custom block data that CoreProtect
 * will save during logging and restore during rollbacks.
 */
public interface BlockDataProvider {

    /**
     * Gets the unique identifier for this provider.
     *
     * @return A unique string identifier for this provider
     */
    String getProviderId();

    /**
     * Gets the set of materials this provider handles.
     *
     * @return Set of materials this provider is interested in
     */
    Set<Material> getHandledMaterials();

    /**
     * Serializes custom block data when CoreProtect logs a block action.
     *
     * @param blockState
     *            The block state being logged
     * @return Serialized custom data as a byte array, or null if no data to store
     */
    byte[] serialize(BlockState blockState);

    /**
     * Restores custom block data when CoreProtect performs a rollback or restore.
     *
     * @param block
     *            The block that was just restored
     * @param customData
     *            The custom data that was previously serialized, or null if none was stored
     */
    void restore(Block block, byte[] customData);

    /**
     * Generates tooltip text for lookup results.
     *
     * @param customData
     *            The custom data that was previously serialized
     * @return Tooltip text to display, or null/empty if no tooltip should be shown
     */
    default String getTooltip(byte[] customData) {
        return null;
    }
}
