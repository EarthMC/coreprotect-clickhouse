package net.coreprotect.utility;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.papermc.paper.entity.EntitySerializationFlag;
import net.coreprotect.utility.serialize.JsonEntitySerializer;
import net.coreprotect.utility.serialize.JsonSerialization;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.Queue;
import org.bukkit.entity.LivingEntity;

public class EntityUtils extends Queue {

    private EntityUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static int getEntityId(EntityType type) {
        if (type == null) {
            return -1;
        }

        return getEntityId(type.name(), true);
    }

    public static int getEntityId(String name, boolean internal) {
        name = name.toLowerCase(Locale.ROOT).trim();

        int id = ConfigHandler.entities.getOrDefault(name, -1);
        if (id == -1 && internal) {
            // Check if another server has already added this entity (multi-server setup)
            id = ConfigHandler.reloadAndGetId(ConfigHandler.CacheType.ENTITIES, name);
            if (id != -1) {
                return id;
            }

            id = ConfigHandler.MAX_ENTITY_ID.incrementAndGet();
            ConfigHandler.entities.put(name, id);
            ConfigHandler.entitiesReversed.put(id, name);
            Queue.queueEntityInsert(id, name);
        }

        return id;
    }

    public static Material getEntityMaterial(EntityType type) {
        switch (type.name()) {
            case "ARMOR_STAND":
                return Material.ARMOR_STAND;
            case "ITEM_FRAME":
                return Material.ITEM_FRAME;
            case "END_CRYSTAL":
            case "ENDER_CRYSTAL":
                return Material.END_CRYSTAL;
            case "ENDER_PEARL":
                return Material.ENDER_PEARL;
            case "POTION":
            case "SPLASH_POTION":
                return Material.SPLASH_POTION;
            case "EXPERIENCE_BOTTLE":
            case "THROWN_EXP_BOTTLE":
                return Material.EXPERIENCE_BOTTLE;
            case "TRIDENT":
                return Material.TRIDENT;
            case "FIREWORK_ROCKET":
            case "FIREWORK":
                return Material.FIREWORK_ROCKET;
            case "EGG":
                return Material.EGG;
            case "SNOWBALL":
                return Material.SNOWBALL;
            case "WIND_CHARGE":
                return Material.valueOf("WIND_CHARGE");
            default:
                return BukkitAdapter.ADAPTER.getFrameType(type);
        }
    }

    public static String getEntityName(int id) {
        // Internal ID pulled from DB
        String entityName = "";
        if (ConfigHandler.entitiesReversed.get(id) != null) {
            entityName = ConfigHandler.entitiesReversed.get(id);
        }
        return entityName;
    }

    public static EntityType getEntityType(int id) {
        // Internal ID pulled from DB
        EntityType entitytype = EntityType.UNKNOWN;
        if (ConfigHandler.entitiesReversed.get(id) != null) {
            String name = ConfigHandler.entitiesReversed.get(id);
            if (name.contains(":")) {
                name = name.split(":")[1];
            }
            entitytype = EntityType.valueOf(name.toUpperCase(Locale.ROOT));
        }
        return entitytype;
    }

    public static EntityType getEntityType(String name) {
        // Name entered by user
        EntityType type = null;
        name = name.toLowerCase(Locale.ROOT).trim();
        if (name.contains(":")) {
            name = (name.split(":"))[1];
        }

        if (ConfigHandler.entities.containsKey(name)) {
            type = EntityType.valueOf(name.toUpperCase(Locale.ROOT));
        }

        return type;
    }

    public static int getSpawnerType(EntityType type) {
        int result = getEntityId(type);
        if (result == -1) {
            result = 0; // default to pig
        }

        return result;
    }

    public static EntityType getSpawnerType(int type) {
        EntityType result = getEntityType(type);
        if (result == null) {
            result = EntityType.PIG;
        }

        return result;
    }

    private static final Map<String, Integer> REMOVABLE_DEFAULTS = Util.make(new HashMap<>(), map -> {
            map.put("AbsorptionAmount", 0);
            map.put("Age", 0);
            map.put("AgeLocked", 0);
            map.put("CanPickUpLoot", 0);
            map.put("ForcedAge", 0);
            map.put("Health", 0);
            map.put("PersistenceRequired", 0);
            map.put("LeftHanded", 0);
            map.put("Invulnerable", 0);
            map.put("BatFlags", 0);
            map.put("IsBaby", 0);
            map.put("Air", 300);
            map.put("Bukkit.Aware", 1);
            map.put("Bukkit.updateLevel", 2);
            map.put("FallDistance", 0);
            map.put("FromBucket", 0);
            map.put("DrownedConversionTime", -1);
            map.put("InWaterTime", 0);
    });

    private static final Set<String> SKIP_EMPTY_ELEMENTS = Set.of("ArmorItems", "HandItems");

    public static String serializeEntity(Entity entity) {
        final JsonObject object = JsonEntitySerializer.serializeEntityAsJson(entity, EntitySerializationFlag.FORCE);

        // remove things we do not care about
        object.remove("WorldUUIDLeast");
        object.remove("WorldUUIDMost");
        object.remove("Motion");
        object.remove("UUID");
        object.remove("HurtTime");
        object.remove("fall_distance");
        object.remove("HurtByTimestamp");
        object.remove("FallFlying");
        object.remove("OnGround");

        if (object.has("last_hurt_by_mob") && object.get("last_hurt_by_mob").equals(object.get("last_hurt_by_player"))) {
            object.remove("last_hurt_by_mob");
        }

        object.remove("last_hurt_by_player_memory_time");
        object.remove("ticks_since_last_hurt_by_mob");
        object.remove("PortalCooldown");
        object.remove("DeathTime");
        object.remove("Fire");
        object.remove("InLove"); // what is love?

        if (object.has("Paper.FireOverride") && "NOT_SET".equals(object.get("Paper.FireOverride").getAsString())) {
            object.remove("Paper.FireOverride");
        }

        for (final Map.Entry<String, Integer> entry : REMOVABLE_DEFAULTS.entrySet()) {
            if (object.get(entry.getKey()) instanceof JsonPrimitive primitive) {
                if (primitive.isNumber() && primitive.getAsInt() == entry.getValue()) {
                    object.remove(entry.getKey());
                }
            }
        }

        for (final String arrayName : SKIP_EMPTY_ELEMENTS) {
            final JsonElement element = object.get(arrayName);
            if (element != null && JsonSerialization.isEmpty(element)) {
                object.remove(arrayName);
            }
        }

        return JsonSerialization.DEFAULT_GSON.toJson(JsonSerialization.encodeKeys(object));
    }

    public static Entity deserializeEntity(String entityData, World world) {
        final JsonObject object = JsonSerialization.decodeKeys(JsonSerialization.DEFAULT_GSON.fromJson(entityData, JsonObject.class));
        final Entity entity = JsonEntitySerializer.deserializeEntityFromJson(object, world);

        if (entity instanceof LivingEntity livingEntity && livingEntity.getHealth() <= 0) {
            livingEntity.setHealth(Optional.ofNullable(livingEntity.getAttribute(Attribute.MAX_HEALTH)).map(AttributeInstance::getValue).orElse(20D));
        }

        return entity;
    }
}
