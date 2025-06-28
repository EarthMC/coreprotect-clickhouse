package net.coreprotect.consumer.process;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import net.coreprotect.CoreProtect;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.Consumer;
import net.coreprotect.utility.MaterialUtils;

class RollbackUpdateProcess {

    static void process(Statement statement, int processId, int id, int action, int table) {
        final Map<Integer, List<Object[]>> updateLists = Consumer.consumerObjectArrayList.get(processId);
        final List<Object[]> list = updateLists.remove(id);

        if (list != null && !list.isEmpty()) {
            // adapted from Database#performUpdate
            String tableName;
            if (table == 1 || table == 3) { // the numbers mason
                tableName = "container";
            } else if (table == 2) {
                tableName = "item";
            } else {
                tableName = "block";
            }

            try (PreparedStatement preparedStatement = statement.getConnection().prepareStatement("ALTER TABLE " + ConfigHandler.prefix + tableName + " UPDATE rolled_back = ? WHERE rowid = ?")) {
                long batchCount = 0;

                for (Object[] listRow : list) {
                    long rowid = (Long) listRow[0];
                    int rb = (Integer) listRow[9];
                    if (MaterialUtils.rolledBack(rb, (table == 2 || table == 3 || table == 4)) == action) { // 1 = restore, 0 = rollback
                        int rolledBack = MaterialUtils.toggleRolledBack(rb, (table == 2 || table == 3 || table == 4)); // co_item, co_container, co_block

                        preparedStatement.setInt(1, rolledBack);
                        preparedStatement.setLong(2, rowid);
                        preparedStatement.addBatch();

                        // i hope such a large rollback never happens
                        if (++batchCount % 100_000 == 0) {
                            preparedStatement.executeBatch();
                        }
                    }
                }

                preparedStatement.executeBatch();
            } catch (SQLException e) {
                CoreProtect.getInstance().getSLF4JLogger().warn("Exception while batch updating rolled_back column for table {}", tableName, e);
            }
        }
    }
}
