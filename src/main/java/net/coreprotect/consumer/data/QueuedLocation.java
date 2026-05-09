package net.coreprotect.consumer.data;

import org.bukkit.Location;
import org.bukkit.World;

import net.coreprotect.utility.WorldUtils;

public class QueuedLocation extends Location {

    private final String worldName;
    private final int worldId;

    public QueuedLocation(Location location) {
        super(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        World world = location.getWorld();
        this.worldName = world != null ? world.getName() : "";
        this.worldId = this.worldName.isEmpty() ? -1 : WorldUtils.getWorldId(this.worldName);
    }

    public static Location capture(Location location) {
        if (location instanceof QueuedLocation) {
            return location;
        }

        return new QueuedLocation(location);
    }

    public String worldName() {
        return worldName;
    }

    public int worldId() {
        return worldId;
    }
}