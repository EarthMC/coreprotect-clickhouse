package net.coreprotect.consumer.process;

import java.sql.PreparedStatement;
import java.util.Map;

import org.bukkit.Location;

import net.coreprotect.consumer.Consumer;
import net.coreprotect.database.logger.CommandLogger;

class PlayerCommandProcess {

    static void process(PreparedStatement preparedStmt, int batchCount, int processId, int id, Object object, String user) {
        if (!(object instanceof Object[] data)) {
            return;
        }

        if (data[1] instanceof Location location) {
            Map<Integer, String> strings = Consumer.consumerStrings.get(processId);
            final String message = strings.remove(id);

            if (message != null) {
                Long timestamp = (Long) data[0];
                boolean cancelled = (boolean) data[2];
                CommandLogger.log(preparedStmt, batchCount, timestamp, location, user, message, cancelled);
            }
        }
    }
}
