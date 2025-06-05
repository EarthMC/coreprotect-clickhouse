package net.coreprotect.utility;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.papermc.paper.entity.EntitySerializationFlag;
import net.coreprotect.utility.serialize.JsonEntitySerializer;
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

    public static final Gson DEFAULT_GSON = new Gson();

    private static final String NAMESPACE = "minecraft:";

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
        int id = -1;
        name = name.toLowerCase(Locale.ROOT).trim();

        if (ConfigHandler.entities.get(name) != null) {
            id = ConfigHandler.entities.get(name);
        }
        else if (internal) {
            int entityID = ConfigHandler.entityId + 1;
            ConfigHandler.entities.put(name, entityID);
            ConfigHandler.entitiesReversed.put(entityID, name);
            ConfigHandler.entityId = entityID;
            Queue.queueEntityInsert(entityID, name);
            id = ConfigHandler.entities.get(name);
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
            if (name.contains(NAMESPACE)) {
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
        if (name.contains(NAMESPACE)) {
            name = (name.split(":"))[1];
        }

        if (ConfigHandler.entities.get(name) != null) {
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

    private static final Set<String> REMOVE_IF_ZERO = Set.of(
            "AbsorptionAmount",
            "Age",
            "AgeLocked",
            "CanPickUpLoot",
            "ForcedAge",
            "Health",
            "OnGround",
            "PersistenceRequired",
            "LeftHanded",
            "Invulnerable",
            "BatFlags"
    );

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

        for (final String removeIfZero : REMOVE_IF_ZERO) {
            if (object.has(removeIfZero)) {
                final JsonElement element = object.get(removeIfZero);

                if (element != null && element.isJsonPrimitive() && element.getAsInt() == 0) {
                    object.remove(removeIfZero);
                }
            }
        }

        // TODO: escape . characters in keys

        return DEFAULT_GSON.toJson(object);
    }

    public static Entity deserializeEntity(String entityData, World world) {
        final JsonObject object = DEFAULT_GSON.fromJson(entityData, JsonObject.class);
        final Entity entity = JsonEntitySerializer.deserializeEntityFromJson(object, world);

        if (entity instanceof LivingEntity livingEntity && livingEntity.getHealth() <= 0) {
            livingEntity.setHealth(Optional.ofNullable(livingEntity.getAttribute(Attribute.MAX_HEALTH)).map(AttributeInstance::getValue).orElse(20D));
        }

        return entity;
    }
}
