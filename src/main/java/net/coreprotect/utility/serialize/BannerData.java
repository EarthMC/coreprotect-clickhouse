package net.coreprotect.utility.serialize;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.DyeColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public record BannerData(DyeColor baseColor, List<Pattern> patterns) {

    public static class Serializer implements JsonSerializer<BannerData>, JsonDeserializer<BannerData> {

        @Override
        public BannerData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            final JsonObject object = json.getAsJsonObject();
            final DyeColor baseColor = parseDyeColor(object.get("baseColor").getAsString());
            final List<Pattern> patterns = new ArrayList<>();

            if (object.has("patterns")) {
                final JsonArray patternArray = object.get("patterns").getAsJsonArray();

                for (final JsonElement patternElement : patternArray) {
                   final JsonObject patternObject = patternElement.getAsJsonObject();

                   final NamespacedKey key = NamespacedKey.fromString(patternObject.get("patternType").getAsString());
                   final PatternType patternType = Optional.ofNullable(key).map(k -> RegistryAccess.registryAccess().getRegistry(RegistryKey.BANNER_PATTERN).get(k)).orElse(null);
                   if (key == null || patternType == null) {
                       continue;
                   }

                   patterns.add(new Pattern(parseDyeColor(patternObject.get("color").getAsString()), patternType));
                }
            }

            return new BannerData(baseColor, patterns);
        }

        @Override
        public JsonElement serialize(BannerData src, Type typeOfSrc, JsonSerializationContext context) {
            final JsonObject object = new JsonObject();

            object.addProperty("baseColor", src.baseColor.name());

            if (!src.patterns.isEmpty()) {
                final JsonArray patterns = new JsonArray();

                for (final Pattern pattern : src.patterns) {
                    final NamespacedKey key = RegistryAccess.registryAccess().getRegistry(RegistryKey.BANNER_PATTERN).getKey(pattern.getPattern());
                    if (key == null) {
                        // unknown key, cannot serialize
                        continue;
                    }

                    final JsonObject patternData = new JsonObject();
                    patternData.addProperty("color", pattern.getColor().name());
                    patternData.addProperty("patternType", key.asMinimalString());
                    patterns.add(patternData);
                }

                if (!patterns.isEmpty()) {
                    object.add("patterns", patterns);
                }
            }

            return object;
        }

        private static DyeColor parseDyeColor(String name) {
            if (name == null) {
                return DyeColor.WHITE;
            }

            if (name.equalsIgnoreCase("silver")) {
                return DyeColor.LIGHT_GRAY;
            }

            try {
                return DyeColor.valueOf(name.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return DyeColor.WHITE;
            }
        }
    }
}
