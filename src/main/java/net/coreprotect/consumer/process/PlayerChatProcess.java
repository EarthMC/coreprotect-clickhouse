package net.coreprotect.consumer.process;

import java.sql.PreparedStatement;
import java.util.Map;

import org.bukkit.Location;

import net.coreprotect.consumer.Consumer;
import net.coreprotect.database.logger.ChatLogger;

class PlayerChatProcess {

    static void process(PreparedStatement preparedStmt, int batchCount, int processId, int id, Object object, String user) {
        if (!(object instanceof Object[] data)) {
            return;
        }

        if (data[1] instanceof Location location) {
            Map<Integer, String> strings = Consumer.consumerStrings.get(processId);
            if (strings.get(id) != null) {
                String message = strings.get(id);
                Long timestamp = (Long) data[0];
                boolean cancelled = data.length > 2 && data[2] instanceof Boolean bool ? bool : false;

                ChatLogger.log(preparedStmt, batchCount, timestamp, location, user, message, cancelled);
                strings.remove(id);
            }
        }
    }
}
