package net.coreprotect.listener.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import net.coreprotect.utility.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.projectiles.ProjectileSource;

import net.coreprotect.CoreProtect;
import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.config.Config;
import net.coreprotect.consumer.Queue;
import net.coreprotect.thread.Scheduler;

public final class EntityDeathListener extends Queue implements Listener {

    public static void parseEntityKills(String message) {
        message = message.trim().toLowerCase(Locale.ROOT);
        if (!message.contains(" ")) {
            return;
        }

        String[] args = message.split(" ");
        if (args.length < 2 || !args[0].replaceFirst("/", "").equals("kill") || !args[1].startsWith("@e")) {
            return;
        }

        List<LivingEntity> entityList = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            List<LivingEntity> livingEntities = world.getLivingEntities();
            for (LivingEntity entity : livingEntities) {
                if (entity instanceof Player) {
                    continue;
                }

                if (entity.isValid()) {
                    entityList.add(entity);
                }
            }
        }

        for (LivingEntity entity : entityList) {
            Scheduler.runTask(CoreProtect.getInstance(), () -> {
                if (entity != null && entity.isDead()) {
                    logEntityDeath(entity, "#command");
                }
            }, entity);
        }
    }

    protected static void logEntityDeath(LivingEntity entity, String e) {
        if (!Config.getConfig(entity.getWorld()).ENTITY_KILLS) {
            return;
        }

        EntityDamageEvent damage = entity.getLastDamageCause();
        if (damage == null) {
            return;
        }

        boolean isCommand = (damage.getCause() == DamageCause.VOID && entity.getLocation().getBlockY() >= BukkitAdapter.ADAPTER.getMinHeight(entity.getWorld()));
        if (e == null) {
            e = isCommand ? "#command" : "";
        }

        if (entity.getType().name().equals("GLOW_SQUID") && damage.getCause() == DamageCause.DROWNING) {
            return;
        }

        List<DamageCause> validDamageCauses = Arrays.asList(DamageCause.SUICIDE, DamageCause.POISON, DamageCause.THORNS, DamageCause.MAGIC, DamageCause.WITHER);

        boolean skip = true;
        EntityDamageEvent.DamageCause cause = damage.getCause();
        if (!Config.getConfig(entity.getWorld()).SKIP_GENERIC_DATA || (!(entity instanceof Zombie) && !(entity instanceof Skeleton)) || (validDamageCauses.contains(cause) || cause.name().equals("KILL"))) {
            skip = false;
        }

        if (damage instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent attack = (EntityDamageByEntityEvent) damage;
            Entity attacker = attack.getDamager();

            if (attacker instanceof Player) {
                Player player = (Player) attacker;
                e = player.getName();
            }
            else if (attacker instanceof AbstractArrow) {
                AbstractArrow arrow = (AbstractArrow) attacker;
                ProjectileSource shooter = arrow.getShooter();

                if (shooter instanceof Player) {
                    Player player = (Player) shooter;
                    e = player.getName();
                }
                else if (shooter instanceof LivingEntity) {
                    EntityType entityType = ((LivingEntity) shooter).getType();
                    if (entityType != null) { // Check for MyPet plugin
                        String name = entityType.name().toLowerCase(Locale.ROOT);
                        e = "#" + name;
                    }
                }
            }
            else if (attacker instanceof ThrownPotion) {
                ThrownPotion potion = (ThrownPotion) attacker;
                ProjectileSource shooter = potion.getShooter();

                if (shooter instanceof Player) {
                    Player player = (Player) shooter;
                    e = player.getName();
                }
                else if (shooter instanceof LivingEntity) {
                    EntityType entityType = ((LivingEntity) shooter).getType();
                    if (entityType != null) { // Check for MyPet plugin
                        String name = entityType.name().toLowerCase(Locale.ROOT);
                        e = "#" + name;
                    }
                }
            }
            else if (attacker.getType().name() != null) {
                e = "#" + attacker.getType().name().toLowerCase(Locale.ROOT);
            }
        }
        else {
            if (cause.equals(EntityDamageEvent.DamageCause.FIRE)) {
                e = "#fire";
            }
            else if (cause.equals(EntityDamageEvent.DamageCause.FIRE_TICK)) {
                if (!skip) {
                    e = "#fire";
                }
            }
            else if (cause.equals(EntityDamageEvent.DamageCause.LAVA)) {
                e = "#lava";
            }
            else if (cause.equals(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)) {
                e = "#explosion";
            }
            else if (cause.equals(EntityDamageEvent.DamageCause.MAGIC)) {
                e = "#magic";
            }
            else if (cause.equals(EntityDamageEvent.DamageCause.WITHER)) {
                e = "#wither_effect";
            }
            else if (!cause.name().contains("_")) {
                e = "#" + cause.name().toLowerCase(Locale.ROOT);
            }
        }

        if (entity instanceof ArmorStand) {
            Location entityLocation = entity.getLocation();
            if (!Config.getConfig(entityLocation.getWorld()).ITEM_TRANSACTIONS) {
                entityLocation.setY(entityLocation.getY() + 0.99);
                Block block = entityLocation.getBlock();
                Queue.queueBlockBreak(e, block.getState(), Material.ARMOR_STAND, null, (int) entityLocation.getYaw());
            }
            /*
            else if (isCommand) {
                entityLocation.setY(entityLocation.getY() + 0.99);
                Block block = entityLocation.getBlock();
                Database.containerBreakCheck(e, Material.ARMOR_STAND, entity, null, block.getLocation());
                Queue.queueBlockBreak(e, block.getState(), Material.ARMOR_STAND, null, (int) entityLocation.getYaw());
            }
            */
            return;
        }

        EntityType entity_type = entity.getType();
        if (e.length() == 0) {
            // assume killed self
            if (!skip) {
                if (!(entity instanceof Player) && entity_type.name() != null) {
                    // Player player = (Player)entity;
                    // e = player.getName();
                    e = "#" + entity_type.name().toLowerCase(Locale.ROOT);
                }
                else if (entity instanceof Player) {
                    e = entity.getName();
                }
            }
        }

        if (e.startsWith("#wither") && !e.equals("#wither_effect")) {
            e = "#wither";
        }

        if (e.startsWith("#enderdragon")) {
            e = "#enderdragon";
        }

        if (e.startsWith("#primedtnt") || e.startsWith("#tnt")) {
            e = "#tnt";
        }

        if (e.startsWith("#lightning")) {
            e = "#lightning";
        }

        if (e.isEmpty()) {
            return;
        }

        if (!(entity instanceof Player)) {
            Queue.queueEntityKill(e, entity.getLocation(), entity.getType(), EntityUtils.serializeEntity(entity));
        }
        else {
            Queue.queuePlayerKill(e, entity.getLocation(), entity.getName());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        /*
        System.out.println("ENTITY DEATH - " + event.getEntity().getName());
        if (event.getEntity().getKiller() != null) {
            System.out.println("^ (killer): " + event.getEntity().getKiller().getName());
        }
        else if (event.getEntity().getLastDamageCause() != null) {
            System.out.println("^ (damage cause): " + event.getEntity().getLastDamageCause().getEntity().getName());
        }
        */

        LivingEntity entity = event.getEntity();
        if (entity == null) {
            return;
        }

        logEntityDeath(entity, null);
    }
}
