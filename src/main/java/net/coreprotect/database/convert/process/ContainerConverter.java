package net.coreprotect.database.convert.process;

import net.coreprotect.database.convert.ClickhouseConverter;
import net.coreprotect.database.convert.table.ContainerTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ContainerConverter implements ConvertProcess {
    private final ContainerTable table;

    public ContainerConverter(ContainerTable table) {
        this.table = table;
    }

    @Override
    public void convertTable(ClickhouseConverter converter, Connection connection) throws SQLException {
        long batchCount = 0;

        try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO " + table.fullName() + " (rowid, time, user, wid, x, y, z, type, data, amount, metadata, action, rolled_back) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            PreparedStatement readStatement = connection.prepareStatement("SELECT rowid, time, user, wid, x, y, z, type, data, amount, hex(metadata), action, rolled_back FROM " + converter.formatMysqlSource(table))) {

            final ResultSet rs = readStatement.executeQuery();
            while (rs.next()) {
                final int rowId = rs.getInt(1);
                insertStatement.setInt(1, rowId); // rowid
                insertStatement.setInt(2, rs.getInt(2)); // time
                insertStatement.setInt(3, rs.getInt(3)); // user
                insertStatement.setInt(4, rs.getInt(4)); // wid
                insertStatement.setInt(5, rs.getInt(5)); // x
                insertStatement.setInt(6, rs.getInt(6)); // y
                insertStatement.setInt(7, rs.getInt(7)); // z
                insertStatement.setInt(8, rs.getInt(8)); // type
                insertStatement.setInt(9, rs.getInt(9)); // data
                insertStatement.setInt(10, rs.getInt(10)); // amount

                String jsonData = ItemConverter.convertItemHexToJson(converter, rowId, rs.getString(11), rs.getInt(8), rs.getInt(10));

                insertStatement.setString(11, jsonData); // metadata
                insertStatement.setInt(12, rs.getInt(12));
                insertStatement.setBoolean(13, rs.getBoolean(13));

                insertStatement.addBatch();

                if (++batchCount % 50_000 == 0) {
                    insertStatement.executeBatch();
                }
            }

            insertStatement.executeBatch();
        }
    }
}

