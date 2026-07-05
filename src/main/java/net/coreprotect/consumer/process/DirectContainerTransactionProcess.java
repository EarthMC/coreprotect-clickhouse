package net.coreprotect.consumer.process;

import java.sql.PreparedStatement;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.coreprotect.database.logger.ContainerLogger;

final class DirectContainerTransactionProcess {

    private DirectContainerTransactionProcess() {
        throw new IllegalStateException("Consumer process class");
    }

    static void process(PreparedStatement preparedStatement, int batchCount, String user, Material type, Object object) {
        if (!(object instanceof Object[] snapshots) || snapshots.length != 3 || !(snapshots[0] instanceof Location) || !(snapshots[1] instanceof ItemStack[]) || !(snapshots[2] instanceof ItemStack[])) {
            return;
        }

        ContainerLogger.logSnapshot(preparedStatement, batchCount, user, type, (Location) snapshots[0], (ItemStack[]) snapshots[1], (ItemStack[]) snapshots[2]);
    }
}
