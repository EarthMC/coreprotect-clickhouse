package net.coreprotect.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import net.coreprotect.utility.Chat;

/**
 * Registry for managing BlockDataProvider instances.
 */
public final class BlockDataProviderRegistry {

    private static final Map<String, BlockDataProvider> providers = new ConcurrentHashMap<>();
    private static final Map<Material, List<BlockDataProvider>> materialProviders = new ConcurrentHashMap<>();

    private BlockDataProviderRegistry() {
        throw new IllegalStateException("Registry class");
    }

    public static boolean register(BlockDataProvider provider) {
        if (provider == null || provider.getProviderId() == null || provider.getProviderId().isEmpty()) {
            return false;
        }

        String providerId = provider.getProviderId().toLowerCase(Locale.ROOT);
        if (providers.containsKey(providerId)) {
            Chat.console("BlockDataProvider with ID '" + providerId + "' is already registered.");
            return false;
        }

        providers.put(providerId, provider);

        if (provider.getHandledMaterials() != null) {
            for (Material material : provider.getHandledMaterials()) {
                materialProviders.computeIfAbsent(material, key -> new ArrayList<>()).add(provider);
            }
        }

        Chat.console("Registered BlockDataProvider: " + providerId);
        return true;
    }

    public static boolean unregister(String providerId) {
        if (providerId == null || providerId.isEmpty()) {
            return false;
        }

        providerId = providerId.toLowerCase(Locale.ROOT);
        BlockDataProvider removed = providers.remove(providerId);

        if (removed != null) {
            if (removed.getHandledMaterials() != null) {
                for (Material material : removed.getHandledMaterials()) {
                    List<BlockDataProvider> list = materialProviders.get(material);
                    if (list != null) {
                        list.remove(removed);
                        if (list.isEmpty()) {
                            materialProviders.remove(material);
                        }
                    }
                }
            }
            Chat.console("Unregistered BlockDataProvider: " + providerId);
            return true;
        }

        return false;
    }

    public static boolean isRegistered(String providerId) {
        return providerId != null && providers.containsKey(providerId.toLowerCase(Locale.ROOT));
    }

    public static List<BlockDataProvider> getProvidersForMaterial(Material material) {
        return materialProviders.getOrDefault(material, new ArrayList<>());
    }

    public static boolean hasProvidersForMaterial(Material material) {
        List<BlockDataProvider> list = materialProviders.get(material);
        return list != null && !list.isEmpty();
    }

    public static byte[] serializeCustomData(BlockState blockState) {
        if (blockState == null) {
            return null;
        }

        List<BlockDataProvider> applicableProviders = getProvidersForMaterial(blockState.getType());
        if (applicableProviders.isEmpty()) {
            return null;
        }

        Map<String, byte[]> providerData = new HashMap<>();
        for (BlockDataProvider provider : applicableProviders) {
            try {
                byte[] data = provider.serialize(blockState);
                if (data != null && data.length > 0) {
                    providerData.put(provider.getProviderId(), data);
                }
            }
            catch (Exception e) {
                Chat.console("Error serializing data for provider '" + provider.getProviderId() + "': " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (providerData.isEmpty()) {
            return null;
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
            DataOutputStream dataOutput = new DataOutputStream(output)) {
            dataOutput.writeInt(providerData.size());
            for (Map.Entry<String, byte[]> entry : providerData.entrySet()) {
                dataOutput.writeUTF(entry.getKey());
                dataOutput.writeInt(entry.getValue().length);
                dataOutput.write(entry.getValue());
            }
            dataOutput.flush();
            return output.toByteArray();
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void restoreCustomData(Block block, byte[] customData) {
        if (block == null || customData == null || customData.length == 0) {
            return;
        }

        try (ByteArrayInputStream input = new ByteArrayInputStream(customData);
            DataInputStream dataInput = new DataInputStream(input)) {
            int providerCount = dataInput.readInt();

            for (int i = 0; i < providerCount; i++) {
                String providerId = dataInput.readUTF();
                int dataLength = dataInput.readInt();
                byte[] data = new byte[dataLength];
                dataInput.readFully(data);

                BlockDataProvider provider = providers.get(providerId.toLowerCase(Locale.ROOT));
                if (provider != null) {
                    try {
                        provider.restore(block, data);
                    }
                    catch (Exception e) {
                        Chat.console("Error restoring data for provider '" + providerId + "': " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getTooltip(byte[] customData) {
        if (customData == null || customData.length == 0) {
            return "";
        }

        StringBuilder tooltip = new StringBuilder();
        try (ByteArrayInputStream input = new ByteArrayInputStream(customData);
            DataInputStream dataInput = new DataInputStream(input)) {
            int providerCount = dataInput.readInt();

            for (int i = 0; i < providerCount; i++) {
                String providerId = dataInput.readUTF();
                int dataLength = dataInput.readInt();
                byte[] data = new byte[dataLength];
                dataInput.readFully(data);

                BlockDataProvider provider = providers.get(providerId.toLowerCase(Locale.ROOT));
                if (provider != null) {
                    try {
                        String providerTooltip = provider.getTooltip(data);
                        if (providerTooltip != null && !providerTooltip.isEmpty()) {
                            if (!tooltip.isEmpty()) {
                                tooltip.append("\n");
                            }
                            tooltip.append(providerTooltip);
                        }
                    }
                    catch (Exception ignored) {
                    }
                }
            }
        }
        catch (IOException ignored) {
        }

        return tooltip.toString();
    }

    public static void clear() {
        providers.clear();
        materialProviders.clear();
    }
}
