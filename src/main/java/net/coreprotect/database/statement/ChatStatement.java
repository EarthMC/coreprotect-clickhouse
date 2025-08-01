package net.coreprotect.database.statement;

import net.coreprotect.CoreProtect;

import java.sql.PreparedStatement;

public class ChatStatement {

    private ChatStatement() {
        throw new IllegalStateException("Database class");
    }

    public static void insert(PreparedStatement preparedStmt, int batchCount, long time, int user, int wid, int x, int y, int z, String message, boolean cancelled) {
        try {
            preparedStmt.setLong(1, time);
            preparedStmt.setInt(2, user);
            preparedStmt.setInt(3, wid);
            preparedStmt.setInt(4, x);
            preparedStmt.setInt(5, y);
            preparedStmt.setInt(6, z);
            preparedStmt.setString(7, message);
            preparedStmt.setBoolean(8, cancelled);
            preparedStmt.setInt(9, CoreProtect.getInstance().rowNumbers().nextRowId("chat", preparedStmt.getConnection()));
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
