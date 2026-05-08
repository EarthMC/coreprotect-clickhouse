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
    public void convertTable(ClickhouseConverter converter, ConvertOptions options, Connection connection) throws SQLException {
        final long startTime = System.currentTimeMillis();
        long batchCount = 0;
        final Connection localSqliteConnection = converter.openLocalSqliteConnection();
        final Connection readConnection = localSqliteConnection != null ? localSqliteConnection : connection;
        final String sourceQuery = localSqliteConnection != null ?
                converter.formatLocalSqliteSourceSelect(table, options, "rowid, time, user, wid, x, y, z, type, data, amount, hex(metadata), action, rolled_back") :
                converter.formatSourceSelect(table, options, converter.formatSourceColumn("rowid", options) + ", time, user, wid, x, y, z, type, data, amount, hex(metadata), action, rolled_back");

        converter.logger().info("Converting {}{}...", table.fullName(), describeChunk(options));

        try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO " + table.fullName() + " (rowid, time, user, wid, x, y, z, type, data, amount, metadata, action, rolled_back) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            Connection ignored = localSqliteConnection;
            PreparedStatement readStatement = readConnection.prepareStatement(sourceQuery)) {

            final ResultSet rs = readStatement.executeQuery();
            while (converter.next(rs, insertStatement, batchCount)) {
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
                insertStatement.setInt(12, rs.getInt(12)); // action
                insertStatement.setInt(13, rs.getInt(13)); // rolled_back

                insertStatement.addBatch();

                if (++batchCount % 50_000 == 0) {
                    insertStatement.executeBatch();
                    logProgress(converter, batchCount, startTime, options);
                }
            }

            insertStatement.executeBatch();
            logProgress(converter, batchCount, startTime, options);
        }
    }

    private String describeChunk(ConvertOptions options) {
        if (!options.singleChunk()) {
            return options.offset() > 0 ? " from offset " + options.offset() : "";
        }

        return " chunk [" + options.sourceOffset() + ", " + (options.sourceOffset() + options.chunkSize()) + ")";
    }

    private void logProgress(ClickhouseConverter converter, long rows, long startTime, ConvertOptions options) {
        long elapsedMillis = Math.max(1L, System.currentTimeMillis() - startTime);
        long rowsPerSecond = rows * 1000L / elapsedMillis;
        converter.logger().info("Converted {} rows from {}{} ({} rows/sec)", rows, table.fullName(), describeChunk(options), rowsPerSecond);
    }
}

