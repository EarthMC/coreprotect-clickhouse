package net.coreprotect.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Tag;

public final class BlockGroup {

    public static Set<Material> TRACK_ANY = new HashSet<>(Arrays.asList(Material.PISTON_HEAD, Material.LEVER, Material.BELL));
    public static Set<Material> TRACK_TOP_BOTTOM = new HashSet<>();
    public static Set<Material> TRACK_TOP = new HashSet<>(Arrays.asList(Material.TORCH, Material.REDSTONE_TORCH, Material.BAMBOO, Material.BAMBOO_SAPLING, Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY, Material.WITHER_ROSE, Material.SWEET_BERRY_BUSH, Material.SCAFFOLDING, Material.OAK_SAPLING, Material.SPRUCE_SAPLING, Material.BIRCH_SAPLING, Material.JUNGLE_SAPLING, Material.ACACIA_SAPLING, Material.DARK_OAK_SAPLING, Material.POWERED_RAIL, Material.DETECTOR_RAIL, Material.FERN, Material.DEAD_BUSH, Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID, Material.ALLIUM, Material.AZURE_BLUET, Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP, Material.PINK_TULIP, Material.OXEYE_DAISY, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.REDSTONE_WIRE, Material.WHEAT, Material.ACACIA_SIGN, Material.BIRCH_SIGN, Material.DARK_OAK_SIGN, Material.JUNGLE_SIGN, Material.OAK_SIGN, Material.SPRUCE_SIGN, Material.WHITE_BANNER, Material.ORANGE_BANNER, Material.MAGENTA_BANNER, Material.LIGHT_BLUE_BANNER, Material.YELLOW_BANNER, Material.LIME_BANNER, Material.PINK_BANNER, Material.GRAY_BANNER, Material.LIGHT_GRAY_BANNER, Material.CYAN_BANNER, Material.PURPLE_BANNER, Material.BLUE_BANNER, Material.BROWN_BANNER, Material.GREEN_BANNER, Material.RED_BANNER, Material.BLACK_BANNER, Material.RAIL, Material.IRON_DOOR, Material.SNOW, Material.CACTUS, Material.SUGAR_CANE, Material.REPEATER, Material.PUMPKIN_STEM, Material.MELON_STEM, Material.CARROT, Material.POTATO, Material.COMPARATOR, Material.ACTIVATOR_RAIL, Material.SUNFLOWER, Material.LILAC, Material.TALL_GRASS, Material.LARGE_FERN, Material.ROSE_BUSH, Material.PEONY, Material.NETHER_WART, Material.CHORUS_PLANT, Material.CHORUS_FLOWER, Material.KELP, Material.SOUL_TORCH, Material.TWISTING_VINES, Material.CRIMSON_FUNGUS, Material.WARPED_FUNGUS, Material.CRIMSON_ROOTS, Material.WARPED_ROOTS, Material.NETHER_SPROUTS, Material.CRIMSON_SIGN, Material.WARPED_SIGN));
    public static Set<Material> TRACK_BOTTOM = new HashSet<>(List.of(Material.WEEPING_VINES));
    public static Set<Material> TRACK_SIDE = new HashSet<>(Arrays.asList(Material.WALL_TORCH, Material.REDSTONE_WALL_TORCH, Material.RAIL, Material.POWERED_RAIL, Material.DETECTOR_RAIL, Material.ACTIVATOR_RAIL, Material.WHITE_BED, Material.ORANGE_BED, Material.MAGENTA_BED, Material.LIGHT_BLUE_BED, Material.YELLOW_BED, Material.LIME_BED, Material.PINK_BED, Material.GRAY_BED, Material.LIGHT_GRAY_BED, Material.CYAN_BED, Material.PURPLE_BED, Material.BLUE_BED, Material.BROWN_BED, Material.GREEN_BED, Material.RED_BED, Material.BLACK_BED, Material.LADDER, Material.ACACIA_WALL_SIGN, Material.BIRCH_WALL_SIGN, Material.DARK_OAK_WALL_SIGN, Material.JUNGLE_WALL_SIGN, Material.OAK_WALL_SIGN, Material.SPRUCE_WALL_SIGN, Material.VINE, Material.COCOA, Material.TRIPWIRE_HOOK, Material.WHITE_WALL_BANNER, Material.ORANGE_WALL_BANNER, Material.MAGENTA_WALL_BANNER, Material.LIGHT_BLUE_WALL_BANNER, Material.YELLOW_WALL_BANNER, Material.LIME_WALL_BANNER, Material.PINK_WALL_BANNER, Material.GRAY_WALL_BANNER, Material.LIGHT_GRAY_WALL_BANNER, Material.CYAN_WALL_BANNER, Material.PURPLE_WALL_BANNER, Material.BLUE_WALL_BANNER, Material.BROWN_WALL_BANNER, Material.GREEN_WALL_BANNER, Material.RED_WALL_BANNER, Material.BLACK_WALL_BANNER, Material.SOUL_WALL_TORCH, Material.CRIMSON_WALL_SIGN, Material.WARPED_WALL_SIGN));
    public static Set<Material> SHULKER_BOXES = Tag.SHULKER_BOXES.getValues();
    public static Set<Material> CONTAINERS = new HashSet<>(Arrays.asList(Material.JUKEBOX, Material.DISPENSER, Material.CHEST, Material.FURNACE, Material.BREWING_STAND, Material.TRAPPED_CHEST, Material.HOPPER, Material.DROPPER, Material.ARMOR_STAND, Material.ITEM_FRAME, Material.SHULKER_BOX, Material.BLACK_SHULKER_BOX, Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.GRAY_SHULKER_BOX, Material.GREEN_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.LIME_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX, Material.ORANGE_SHULKER_BOX, Material.PINK_SHULKER_BOX, Material.PURPLE_SHULKER_BOX, Material.RED_SHULKER_BOX, Material.LIGHT_GRAY_SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX, Material.BARREL, Material.BLAST_FURNACE, Material.SMOKER, Material.LECTERN));
    public static Set<Material> DOORS = Tag.DOORS.getValues();
    public static Set<Material> BUTTONS = Tag.BUTTONS.getValues();
    public static Set<Material> PRESSURE_PLATES = Tag.PRESSURE_PLATES.getValues();
    public static Set<Material> VINES = new HashSet<>(Arrays.asList(Material.VINE, Material.WEEPING_VINES, Material.TWISTING_VINES));
    public static Set<Material> AMETHYST = new HashSet<>();
    public static Set<Material> LIGHTABLES = new HashSet<>(Arrays.asList(Material.CAMPFIRE, Material.SOUL_CAMPFIRE));
    public static Set<Material> CANDLES = Tag.CANDLES.getValues();
    public static Set<Material> FIRE = Tag.FIRE.getValues();
    public static Set<Material> LANTERNS = new HashSet<>(Arrays.asList(Material.LANTERN, Material.SOUL_LANTERN));
    public static Set<Material> SOUL_BLOCKS = new HashSet<>(Arrays.asList(Material.SOUL_SAND, Material.SOUL_SOIL));
    public static Set<Material> DIRECTIONAL_BLOCKS = new HashSet<>(Arrays.asList(Material.STICKY_PISTON, Material.PISTON, Material.REPEATER, Material.SKELETON_SKULL, Material.SKELETON_WALL_SKULL, Material.WITHER_SKELETON_SKULL, Material.WITHER_SKELETON_WALL_SKULL, Material.ZOMBIE_HEAD, Material.ZOMBIE_WALL_HEAD, Material.PLAYER_HEAD, Material.PLAYER_WALL_HEAD, Material.CREEPER_HEAD, Material.CREEPER_WALL_HEAD, Material.DRAGON_HEAD, Material.DRAGON_WALL_HEAD, Material.COMPARATOR));
    public static Set<Material> INTERACT_BLOCKS = new HashSet<>(Arrays.asList(Material.SPRUCE_FENCE_GATE, Material.BIRCH_FENCE_GATE, Material.JUNGLE_FENCE_GATE, Material.DARK_OAK_FENCE_GATE, Material.ACACIA_FENCE_GATE, Material.DISPENSER, Material.NOTE_BLOCK, Material.CHEST, Material.FURNACE, Material.LEVER, Material.REPEATER, Material.ACACIA_TRAPDOOR, Material.BIRCH_TRAPDOOR, Material.DARK_OAK_TRAPDOOR, Material.JUNGLE_TRAPDOOR, Material.SPRUCE_TRAPDOOR, Material.OAK_TRAPDOOR, Material.OAK_FENCE_GATE, Material.BREWING_STAND, Material.ANVIL, Material.CHIPPED_ANVIL, Material.DAMAGED_ANVIL, Material.ENDER_CHEST, Material.TRAPPED_CHEST, Material.COMPARATOR, Material.HOPPER, Material.DROPPER, Material.SHULKER_BOX, Material.BLACK_SHULKER_BOX, Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.GRAY_SHULKER_BOX, Material.GREEN_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.LIME_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX, Material.ORANGE_SHULKER_BOX, Material.PINK_SHULKER_BOX, Material.PURPLE_SHULKER_BOX, Material.RED_SHULKER_BOX, Material.LIGHT_GRAY_SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX, Material.BARREL, Material.BLAST_FURNACE, Material.GRINDSTONE, Material.LOOM, Material.SMOKER, Material.CRAFTING_TABLE, Material.CARTOGRAPHY_TABLE, Material.ENCHANTING_TABLE, Material.SMITHING_TABLE, Material.STONECUTTER, Material.CRIMSON_FENCE_GATE, Material.WARPED_FENCE_GATE, Material.CRIMSON_TRAPDOOR, Material.WARPED_TRAPDOOR));
    public static Set<Material> SAFE_INTERACT_BLOCKS = new HashSet<>(Arrays.asList(Material.LEVER, Material.ACACIA_TRAPDOOR, Material.BIRCH_TRAPDOOR, Material.DARK_OAK_TRAPDOOR, Material.JUNGLE_TRAPDOOR, Material.SPRUCE_TRAPDOOR, Material.OAK_TRAPDOOR, Material.OAK_FENCE_GATE, Material.SPRUCE_FENCE_GATE, Material.BIRCH_FENCE_GATE, Material.JUNGLE_FENCE_GATE, Material.DARK_OAK_FENCE_GATE, Material.ACACIA_FENCE_GATE, Material.CRIMSON_FENCE_GATE, Material.WARPED_FENCE_GATE, Material.CRIMSON_TRAPDOOR, Material.WARPED_TRAPDOOR));
    public static Set<Material> UPDATE_STATE = new HashSet<>(Arrays.asList(Material.TORCH, Material.WALL_TORCH, Material.REDSTONE_WIRE, Material.RAIL, Material.POWERED_RAIL, Material.DETECTOR_RAIL, Material.FURNACE, Material.BLAST_FURNACE, Material.SMOKER, Material.LEVER, Material.REDSTONE_TORCH, Material.REDSTONE_WALL_TORCH, Material.GLOWSTONE, Material.JACK_O_LANTERN, Material.REPEATER, Material.REDSTONE_LAMP, Material.BEACON, Material.COMPARATOR, Material.DAYLIGHT_DETECTOR, Material.REDSTONE_BLOCK, Material.HOPPER, Material.CHEST, Material.TRAPPED_CHEST, Material.ACTIVATOR_RAIL, Material.SOUL_TORCH, Material.SOUL_WALL_TORCH, Material.SHROOMLIGHT, Material.RESPAWN_ANCHOR, Material.CRYING_OBSIDIAN, Material.TARGET));
    public static Set<Material> NATURAL_BLOCKS = new HashSet<>(Arrays.asList(Material.STONE, Material.GOLD_ORE, Material.IRON_ORE, Material.COAL_ORE, Material.LAPIS_ORE, Material.SANDSTONE, Material.COBWEB, Material.FERN, Material.DEAD_BUSH, Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID, Material.ALLIUM, Material.AZURE_BLUET, Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP, Material.PINK_TULIP, Material.OXEYE_DAISY, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.OBSIDIAN, Material.DIAMOND_ORE, Material.WHEAT, Material.REDSTONE_ORE, Material.SNOW, Material.ICE, Material.CACTUS, Material.CLAY, Material.SUGAR_CANE, Material.PUMPKIN, Material.NETHERRACK, Material.SOUL_SAND, Material.MELON, Material.PUMPKIN_STEM, Material.MELON_STEM, Material.MYCELIUM, Material.LILY_PAD, Material.NETHER_WART, Material.END_STONE, Material.EMERALD_ORE, Material.CARROT, Material.POTATO, Material.KELP, Material.CHORUS_FLOWER, Material.CHORUS_PLANT, Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY, Material.WITHER_ROSE, Material.SWEET_BERRY_BUSH));
    public static Set<Material> SCULK = new HashSet<>();

    /* blocks that support vertical scanning */
    public static Set<Material> VERTICAL_TOP_BOTTOM = new HashSet<>();
    public static Set<Material> VERTICAL_TOP = new HashSet<>();
    public static Set<Material> VERTICAL_BOTTOM = new HashSet<>();
    public static Set<Material> VERTICAL = new HashSet<>();

    // These are blocks that an item frame or painting can't be attached to.
    // Same as non_solid_entity_blocks? >>Perform testing<<
    public static Set<Material> NON_ATTACHABLE = new HashSet<>(Arrays.asList(Material.AIR, Material.CAVE_AIR, Material.BARRIER, Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY, Material.WITHER_ROSE, Material.SWEET_BERRY_BUSH, Material.OAK_SAPLING, Material.SPRUCE_SAPLING, Material.BIRCH_SAPLING, Material.JUNGLE_SAPLING, Material.ACACIA_SAPLING, Material.DARK_OAK_SAPLING, Material.WATER, Material.LAVA, Material.POWERED_RAIL, Material.DETECTOR_RAIL, Material.FERN, Material.DEAD_BUSH, Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID, Material.ALLIUM, Material.AZURE_BLUET, Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP, Material.PINK_TULIP, Material.OXEYE_DAISY, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.TORCH, Material.WALL_TORCH, Material.REDSTONE_WIRE, Material.LADDER, Material.RAIL, Material.LEVER, Material.REDSTONE_TORCH, Material.REDSTONE_WALL_TORCH, Material.SNOW, Material.SUGAR_CANE, Material.NETHER_PORTAL, Material.REPEATER, Material.KELP, Material.CHORUS_FLOWER, Material.CHORUS_PLANT, Material.SOUL_TORCH, Material.SOUL_WALL_TORCH));

    static {
        Material shortGrass = Material.getMaterial("SHORT_GRASS");
        if (shortGrass == null) {
            shortGrass = Material.getMaterial("GRASS");
        }
        if (shortGrass != null) {
            TRACK_TOP.add(shortGrass);
            NON_ATTACHABLE.add(shortGrass);
        }

        TRACK_ANY.addAll(BUTTONS);
        TRACK_TOP.addAll(DOORS);
        TRACK_TOP.addAll(PRESSURE_PLATES);
        TRACK_TOP.addAll(Tag.WOOL_CARPETS.getValues());
        TRACK_TOP_BOTTOM.addAll(LANTERNS);
        LIGHTABLES.addAll(CANDLES);
        INTERACT_BLOCKS.addAll(DOORS);
        INTERACT_BLOCKS.addAll(BUTTONS);
        SAFE_INTERACT_BLOCKS.addAll(DOORS);
        SAFE_INTERACT_BLOCKS.addAll(BUTTONS);
        NON_ATTACHABLE.addAll(BUTTONS);
        NON_ATTACHABLE.addAll(CANDLES);
        NON_ATTACHABLE.addAll(FIRE);
        UPDATE_STATE.addAll(LIGHTABLES);
        UPDATE_STATE.addAll(LANTERNS);

        /* add all blocks that support vertical scanning */
        VERTICAL.addAll(VERTICAL_TOP_BOTTOM);
        VERTICAL.addAll(VERTICAL_TOP);
        VERTICAL.addAll(VERTICAL_BOTTOM);

        /* Natural block group */
        NATURAL_BLOCKS.addAll(Tag.BAMBOO_PLANTABLE_ON.getValues());
        NATURAL_BLOCKS.addAll(Tag.VALID_SPAWN.getValues());
        NATURAL_BLOCKS.addAll(Tag.SAND.getValues());
        NATURAL_BLOCKS.addAll(Tag.LOGS.getValues());
        NATURAL_BLOCKS.addAll(Tag.LEAVES.getValues());
        NATURAL_BLOCKS.addAll(Tag.ICE.getValues());
        NATURAL_BLOCKS.addAll(Tag.UNDERWATER_BONEMEALS.getValues());
        NATURAL_BLOCKS.addAll(Tag.CORAL_BLOCKS.getValues());
        NATURAL_BLOCKS.addAll(Tag.ENDERMAN_HOLDABLE.getValues());
        NATURAL_BLOCKS.addAll(SOUL_BLOCKS);
        NATURAL_BLOCKS.addAll(VINES);
    }
}
