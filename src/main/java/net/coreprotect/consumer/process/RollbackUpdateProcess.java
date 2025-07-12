package net.coreprotect.consumer.process;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import net.coreprotect.CoreProtect;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.Consumer;
import net.coreprotect.data.rollback.RollbackRowUpdate;
import net.coreprotect.utility.MaterialUtils;

class RollbackUpdateProcess {

    @SuppressWarnings("unchecked")
    static void process(Statement statement, int processId, int id, int action, int table) {
        final Map<Integer, Object> updateLists = Consumer.consumerObjects.get(processId);
        final List<RollbackRowUpdate> list = (List<RollbackRowUpdate>) updateLists.remove(id);

        if (list != null && !list.isEmpty()) {
            // adapted from Database#performUpdate
            String tableName;
            String rows;
            if (table == 1 || table == 3) { // the numbers mason
                tableName = "container";
                rows = "rowid,time,user,wid,x,y,z,type,data,amount,metadata,action,? as rolled_back";
            } else if (table == 2) {
                tableName = "item";
                rows = "rowid,time,user,wid,x,y,z,type,data,amount,action,? as rolled_back";
            } else {
                tableName = "block";
                rows = "rowid,time,user,wid,x,y,z,type,data,meta,blockdata,action,? as rolled_back";
            }

            rows += ",version + 1 as version";
            tableName = ConfigHandler.prefix + tableName;

            // Re-insert the same row with an incremented version
            try (PreparedStatement preparedStatement = statement.getConnection().prepareStatement("INSERT INTO " + tableName + " SELECT " + rows + " FROM " + tableName + " WHERE wid = ? AND time = ? AND x = ? AND z = ? AND user = ? AND rowid = ? LIMIT 1")) {
                long batchCount = 0;

                for (RollbackRowUpdate listRow : list) {
                    long rowid = listRow.rowId();
                    int rb = listRow.rolledBack();
                    if (MaterialUtils.rolledBack(rb, (table == 2 || table == 3 || table == 4)) == action) { // 1 = restore, 0 = rollback
                        int rolledBack = MaterialUtils.toggleRolledBack(rb, (table == 2 || table == 3 || table == 4)); // co_item, co_container, co_block

                        preparedStatement.setInt(1, rolledBack);
                        preparedStatement.setInt(2, listRow.worldId());
                        preparedStatement.setLong(3, listRow.time());
                        preparedStatement.setInt(4, listRow.x());
                        preparedStatement.setInt(5, listRow.z());
                        preparedStatement.setInt(6, listRow.user());;
                        preparedStatement.setLong(7, rowid);
                        preparedStatement.addBatch();

                        if (++batchCount % 10_000 == 0) {
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
