package net.coreprotect.database.statement;

import java.sql.PreparedStatement;
import java.util.List;

import net.coreprotect.CoreProtect;
import net.coreprotect.utility.BlockUtils;
import net.coreprotect.utility.ItemUtils;
import net.coreprotect.utility.serialize.Bytes;

public class BlockStatement {

    private BlockStatement() {
        throw new IllegalStateException("Database class");
    }

    public static void insert(PreparedStatement preparedStmt, int batchCount, int time, int id, int wid, int x, int y, int z, int type, int data, List<Object> meta, String blockData, int action, int rolledBack) {
        try {
            byte[] byteData = null;

            if (meta != null) {
                byteData = ItemUtils.convertByteData(meta); // TODO CH
            }

            preparedStmt.setInt(1, time);
            preparedStmt.setInt(2, id);
            preparedStmt.setInt(3, wid);
            preparedStmt.setInt(4, x);
            preparedStmt.setInt(5, y);
            preparedStmt.setInt(6, z);
            preparedStmt.setInt(7, type);
            preparedStmt.setInt(8, data);
            preparedStmt.setString(9, Bytes.toBlobString(byteData));
            preparedStmt.setString(10, BlockUtils.stringToStringData(blockData, type));
            preparedStmt.setInt(11, action);
            preparedStmt.setInt(12, rolledBack);
            preparedStmt.setLong(13, CoreProtect.getInstance().rowNumbers().nextRowNumber("block", preparedStmt.getConnection()));
            preparedStmt.addBatch();

            if (batchCount > 0 && batchCount % 1000 == 0) {
                preparedStmt.executeBatch();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
