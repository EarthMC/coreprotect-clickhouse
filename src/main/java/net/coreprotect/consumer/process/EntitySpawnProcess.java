package net.coreprotect.consumer.process;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import net.coreprotect.CoreProtect;
import net.coreprotect.thread.Scheduler;
import net.coreprotect.utility.EntityUtils;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import net.coreprotect.config.ConfigHandler;

class EntitySpawnProcess {

    static void process(Statement statement, Object object, int rowId) {
        if (object instanceof Object[]) {
            BlockState block = (BlockState) ((Object[]) object)[0];
            EntityType type = (EntityType) ((Object[]) object)[1];

            if (type == null) {
                return;
            }

            try (final PreparedStatement ps = statement.getConnection().prepareStatement("SELECT toString(data) as data FROM " + ConfigHandler.prefix + "entity WHERE rowid = ? LIMIT 1")) {
                ps.setInt(1, rowId);

                try (final ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return;
                    }

                    try {
                        final Entity deserialized = EntityUtils.deserializeEntity(rs.getString("data"), block.getWorld());
                        Scheduler.runTask(CoreProtect.getInstance(), () -> block.getWorld().addEntity(deserialized), block.getLocation());
                    } catch (Exception e) {
                        CoreProtect.getInstance().getSLF4JLogger().warn("Failed to spawn entity", e);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
