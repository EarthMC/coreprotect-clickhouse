package net.coreprotect.utility.serialize;

import com.google.common.collect.Iterables;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JsonSerializationTest {
    @Test
    void testEncodeDecodeKeysObject() {
        final JsonObject object = new JsonObject();
        object.addProperty("Bukkit.Aware", 1);

        JsonSerialization.encodeKeys(object);

        assertEquals("Bukkit\\u002eAware", Iterables.getFirst(object.asMap().keySet(), null));

        JsonSerialization.decodeKeys(object);

        assertEquals("Bukkit.Aware", Iterables.getFirst(object.asMap().keySet(), null));
    }

    @Test
    void testEncodeNestedArray() {
        final JsonObject object = new JsonObject();
        final JsonArray array = new JsonArray();

        final JsonObject nested = new JsonObject();
        nested.addProperty("a.b", 1);

        array.add(nested);
        object.add("array", array);

        assertEquals("a.b", Iterables.getFirst(object.get("array").getAsJsonArray().get(0).getAsJsonObject().asMap().keySet(), null));

        JsonSerialization.encodeKeys(object);
        assertEquals("a\\u002eb", Iterables.getFirst(object.get("array").getAsJsonArray().get(0).getAsJsonObject().asMap().keySet(), null));

        JsonSerialization.decodeKeys(object);
        assertEquals("a.b", Iterables.getFirst(object.get("array").getAsJsonArray().get(0).getAsJsonObject().asMap().keySet(), null));
    }

    @Test
    void testCheckEmptyArray() {
        final JsonArray array = new JsonArray();

        assertTrue(JsonSerialization.isEmpty(array));

        array.add(new JsonObject());
        array.add(new JsonObject());
        array.add(new JsonObject());
        array.add(new JsonObject());

        assertTrue(JsonSerialization.isEmpty(array));
    }
}
