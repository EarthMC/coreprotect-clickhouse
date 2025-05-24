package net.coreprotect.database;

import java.sql.SQLException;

/**
 * Intended to reduce diff by simulating returning a generated key from the database.
 */
public class FakeRowNumberResultSet extends AbstractResultSet {
    private final int rowid;
    private int nextCount = 0;

    public FakeRowNumberResultSet(final int rowid) {
        this.rowid = rowid;
    }

    @Override
    public boolean next() {
        return nextCount++ == 0;
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        if (nextCount != 1) {
            throw new SQLException("next not called");
        }

        if (columnIndex != 1) {
            throw new SQLException("Invalid column index");
        }

        return rowid;
    }
}
