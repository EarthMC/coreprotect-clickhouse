package net.coreprotect.utility.serialize;

import ca.spottedleaf.moonrise.common.PlatformHooks;
import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import io.papermc.paper.entity.EntitySerializationFlag;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class EntitySerializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static @NotNull CompoundTag serializeEntityAsNBT(@NotNull final Entity entity, final @NotNull EntitySerializationFlag... serializationFlags) {
        Preconditions.checkArgument(entity != null, "entity must not be null");

        final CompoundTag nbt = serializeEntityToNbt(entity, serializationFlags);
        nbt.putInt("DataVersion", SharedConstants.getCurrentVersion().dataVersion().version());

        return nbt;
    }

    public static @NotNull Entity deserializeEntityFromNBT(@NotNull final CompoundTag tag, @NotNull World world) {
        Preconditions.checkArgument(tag != null, "nbt tag must not be null");
        Preconditions.checkArgument(world != null, "world must not be null");

        return deserializeEntityFromNbt(tag, world, false, false);
    }

    // Copied
    private static CompoundTag serializeEntityToNbt(org.bukkit.entity.Entity entity, EntitySerializationFlag... serializationFlags) {
        Preconditions.checkNotNull(entity, "null cannot be serialized");
        Preconditions.checkArgument(entity instanceof CraftEntity, "Only CraftEntities can be serialized");

        Set<EntitySerializationFlag> flags = Set.of(serializationFlags);
        final boolean serializePassengers = flags.contains(EntitySerializationFlag.PASSENGERS);
        final boolean forceSerialization = flags.contains(EntitySerializationFlag.FORCE);
        final boolean allowPlayerSerialization = flags.contains(EntitySerializationFlag.PLAYER);
        final boolean allowMiscSerialization = flags.contains(EntitySerializationFlag.MISC);
        final boolean includeNonSaveable = allowPlayerSerialization || allowMiscSerialization;

        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        (serializePassengers ? nmsEntity.getSelfAndPassengers() : Stream.of(nmsEntity)).forEach(e -> {
            // Ensure force flag is not needed
            Preconditions.checkArgument(
                    (e.getBukkitEntity().isValid() && e.getBukkitEntity().isPersistent()) || forceSerialization,
                    "Cannot serialize invalid or non-persistent entity %s(%s) without the FORCE flag",
                    e.getType().toShortString(),
                    e.getStringUUID()
            );

            if (e instanceof Player) {
                // Ensure player flag is not needed
                Preconditions.checkArgument(
                        allowPlayerSerialization,
                        "Cannot serialize player(%s) without the PLAYER flag",
                        e.getStringUUID()
                );
            } else {
                // Ensure misc flag is not needed
                Preconditions.checkArgument(
                        nmsEntity.getType().canSerialize() || allowMiscSerialization,
                        "Cannot serialize misc non-saveable entity %s(%s) without the MISC flag",
                        e.getType().toShortString(),
                        e.getStringUUID()
                );
            }
        });

        try (final ProblemReporter.ScopedCollector problemReporter = new ProblemReporter.ScopedCollector(
                () -> "serialiseEntity@" + entity.getUniqueId(), LOGGER
        )) {
            final TagValueOutput output = TagValueOutput.createWithContext(problemReporter, nmsEntity.registryAccess());
            if (serializePassengers) {
                if (!nmsEntity.saveAsPassenger(output, true, includeNonSaveable, forceSerialization)) {
                    throw new IllegalArgumentException("Couldn't serialize entity");
                }
            } else {
                List<net.minecraft.world.entity.Entity> pass = new ArrayList<>(nmsEntity.getPassengers());
                nmsEntity.passengers = com.google.common.collect.ImmutableList.of();
                boolean serialized = nmsEntity.saveAsPassenger(output, true, includeNonSaveable, forceSerialization);
                nmsEntity.passengers = com.google.common.collect.ImmutableList.copyOf(pass);
                if (!serialized) {
                    throw new IllegalArgumentException("Couldn't serialize entity");
                }
            }
            return output.buildResult();
        }
    }

    private static org.bukkit.entity.Entity deserializeEntityFromNbt(CompoundTag compound, World world, boolean preserveUUID, boolean preservePassengers) {
        int dataVersion = compound.getIntOr("DataVersion", 0);
        compound = PlatformHooks.get().convertNBT(References.ENTITY, MinecraftServer.getServer().fixerUpper, compound, dataVersion, SharedConstants.getCurrentVersion().dataVersion().version()); // Paper - possibly use dataconverter
        if (!preservePassengers) {
            compound.remove("Passengers");
        }
        net.minecraft.world.entity.Entity nmsEntity = deserializeEntity(compound, ((CraftWorld) world).getHandle(), preserveUUID);
        return nmsEntity.getBukkitEntity();
    }

    private static net.minecraft.world.entity.Entity deserializeEntity(CompoundTag compound, ServerLevel world, boolean preserveUUID) {
        if (!preserveUUID) {
            // Generate a new UUID, so we don't have to worry about deserializing the same entity twice
            compound.remove("UUID");
        }

        final net.minecraft.world.entity.Entity nmsEntity;
        try (final ProblemReporter.ScopedCollector problemReporter = new ProblemReporter.ScopedCollector(
                () -> "deserialiseEntity", LOGGER
        )) {
            nmsEntity = net.minecraft.world.entity.EntityType.create(
                    TagValueInput.create(problemReporter, world.registryAccess(), compound),
                    world,
                    net.minecraft.world.entity.EntitySpawnReason.LOAD
            ).orElseThrow(() -> new IllegalArgumentException("An ID was not found for the data. Did you downgrade?"));
        }

        compound.getList("Passengers").ifPresent(passengers -> {
            for (final Tag tag : passengers) {
                if (!(tag instanceof final CompoundTag serializedPassenger)) {
                    continue;
                }
                final net.minecraft.world.entity.Entity passengerEntity = deserializeEntity(serializedPassenger, world, preserveUUID);
                passengerEntity.startRiding(nmsEntity, true);
            }
        });
        return nmsEntity;
    }
}
