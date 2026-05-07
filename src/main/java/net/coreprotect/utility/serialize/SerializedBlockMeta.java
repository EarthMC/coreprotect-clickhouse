package net.coreprotect.utility.serialize;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;

public record SerializedBlockMeta(
    String command, // Used by command blocks
    Collection<SerializedItem> items, // Used by shulker boxes
    BannerData bannerData,
    byte[] providerData
) {

    public SerializedBlockMeta(String command, Collection<SerializedItem> items, BannerData bannerData) {
        this(command, items, bannerData, null);
    }

    @SuppressWarnings("deprecation")
    public static class Serializer implements JsonDeserializer<SerializedBlockMeta>, JsonSerializer<SerializedBlockMeta> {
        @Override
        public SerializedBlockMeta deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            final JsonObject object = json.getAsJsonObject();

            String command = null;
            Collection<SerializedItem> items = null;
            BannerData bannerData = null;
            byte[] providerData = null;

            if (object.has("command")) {
                command = object.get("command").getAsString();
            }

            if (object.has("items")) {
                items = new ArrayList<>();

                for (JsonElement itemElement : object.get("items").getAsJsonArray()) {
                    final JsonObject itemObject = itemElement.getAsJsonObject();

                    Integer slot = null;
                    if (itemObject.has("co_slot")) {
                        slot = itemObject.remove("co_slot").getAsInt();
                    }

                    final ItemStack item = Bukkit.getUnsafe().deserializeItemFromJson(itemObject);
                    items.add(new SerializedItem(item, slot, null));
                }
            }

            if (object.has("bannerData")) {
                bannerData = context.deserialize(object.get("bannerData"), BannerData.class);
            }

            if (object.has("providerData")) {
                providerData = Base64.getDecoder().decode(object.get("providerData").getAsString());
            }

            return new SerializedBlockMeta(command, items, bannerData, providerData);
        }

        @Override
        public JsonElement serialize(SerializedBlockMeta src, Type typeOfSrc, JsonSerializationContext context) {
            final JsonObject object = new JsonObject();

            if (src.command != null && !src.command.isEmpty()) {
                object.addProperty("command", src.command);
            }

            if (src.items != null && !src.items.isEmpty()) {
                final JsonArray itemsArray = new JsonArray();

                for (final SerializedItem item : src.items) {
                    if (item.itemStack() == null || item.itemStack().isEmpty()) {
                        continue;
                    }

                    final JsonObject itemObject = Bukkit.getUnsafe().serializeItemAsJson(item.itemStack());
                    if (item.slot() != null) {
                        itemObject.addProperty("co_slot", item.slot());
                    }

                    itemsArray.add(itemObject);
                }

                if (!itemsArray.isEmpty()) {
                    object.add("items", itemsArray);
                }
            }

            if (src.bannerData != null) {
                object.add("bannerData", context.serialize(src.bannerData, BannerData.class));
            }

            if (src.providerData != null && src.providerData.length > 0) {
                object.addProperty("providerData", Base64.getEncoder().encodeToString(src.providerData));
            }

            return object;
        }
    }
}
