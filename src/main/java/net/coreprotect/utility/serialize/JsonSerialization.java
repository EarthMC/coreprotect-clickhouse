package net.coreprotect.utility.serialize;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.Contract;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class JsonSerialization {
    public static final Gson DEFAULT_GSON = new Gson();

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(BannerData.class, new BannerData.Serializer())
            .registerTypeAdapter(SerializedBlockMeta.class, new SerializedBlockMeta.Serializer())
            .create();

    private static final Map<String, String> ENCODE_REPLACEMENTS = ImmutableMap.of(
            ".", "\\u002e" // Clickhouse uses . as path separator in json objects.
    );

    @Contract(mutates = "param1")
    public static JsonObject encodeKeys(JsonObject object) {
        if (object.isEmpty()) {
            return object;
        }

        final Set<String> keys = new HashSet<>(object.asMap().keySet());

        for (final String key : keys) {
            JsonElement value = object.get(key);

            final String escapedKey = encodeKey(key);

            if (value instanceof JsonObject obj) {
                encodeKeys(obj);
            } else if (value instanceof JsonArray array) {
                encodeKeys(array);
            }

            if (!escapedKey.equals(key)) {
                object.remove(key);
                object.add(escapedKey, value);
            }
        }

        return object;
    }

    @Contract(mutates = "param1")
    public static JsonArray encodeKeys(JsonArray array) {
        if (array.isEmpty()) {
            return array;
        }

        for (JsonElement next : array.asList()) {
            if (next instanceof JsonObject object) {
                encodeKeys(object);
            } else if (next instanceof JsonArray nestedArray) {
                encodeKeys(nestedArray);
            }
        }

        return array;
    }

    @Contract(mutates = "param1")
    public static JsonObject decodeKeys(JsonObject object) {
        if (object.isEmpty()) {
            return object;
        }

        final Set<String> keys = new HashSet<>(object.asMap().keySet());

        for (final String key : keys) {
            JsonElement value = object.get(key);

            final String decodedKey = decodeKey(key);

            if (value instanceof JsonObject obj) {
                decodeKeys(obj);
            } else if (value instanceof JsonArray array) {
                decodeKeys(array);
            }

            if (!decodedKey.equals(key)) {
                object.remove(key);
                object.add(decodedKey, value);
            }
        }

        return object;
    }

    @Contract(mutates = "param1")
    public static JsonArray decodeKeys(JsonArray array) {
        if (array.isEmpty()) {
            return array;
        }

        for (JsonElement next : array.asList()) {
            if (next instanceof JsonObject object) {
                decodeKeys(object);
            } else if (next instanceof JsonArray nestedArray) {
                decodeKeys(nestedArray);
            }
        }

        return array;
    }

    public static String encodeKey(String key) {
        for (Map.Entry<String, String> entry : ENCODE_REPLACEMENTS.entrySet()) {
            key = key.replace(entry.getKey(), entry.getValue());
        }

        return key;
    }

    public static String decodeKey(String key) {
        for (Map.Entry<String, String> entry : ENCODE_REPLACEMENTS.entrySet()) {
            key = key.replace(entry.getValue(), entry.getKey());
        }

        return key;
    }

    /**
     * Recursively checks whether the given element is an empty array or object
     *
     * @param element A json element
     * @return true if this element is a completely empty json array/object
     */
    public static boolean isEmpty(JsonElement element) {
        if (element instanceof JsonObject object) {
            for (JsonElement value : object.asMap().values()) {
                if (!isEmpty(value)) {
                    return false;
                }
            }

            return true;
        } else if (element instanceof JsonArray array) {
            for (final JsonElement value : array) {
                if (!isEmpty(value)) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }
}
