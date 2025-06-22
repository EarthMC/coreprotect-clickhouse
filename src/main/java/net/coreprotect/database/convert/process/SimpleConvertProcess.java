package net.coreprotect.database.convert.process;

import net.coreprotect.config.ConfigHandler;
import net.coreprotect.database.convert.ClickhouseConverter;
import net.coreprotect.database.convert.TableData;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A simple converter that directly inserts data after mapping tables.
 */
public class SimpleConvertProcess implements ConvertProcess {
    private final TableData table;

    public SimpleConvertProcess(TableData table) {
        this.table = table;
    }

    @Override
    public void convertTable(ClickhouseConverter converter, ConvertOptions options, Connection connection) throws SQLException {
        if (table.getColumnMapping().getMapping().isEmpty()) {
            throw new IllegalStateException("Cannot convert with an empty TableMapping.");
        }

        final List<String> columns = new ArrayList<>();
        final List<String> values = new ArrayList<>();

        for (final Map.Entry<String, String> entry : table.getColumnMapping().getMapping().entrySet()) {
            columns.add(entry.getKey());
            values.add(entry.getValue());
        }

        String query = "INSERT INTO " + ConfigHandler.prefix + table.getName() + " (" + String.join(", ", columns) + ")" +
                "SELECT " + String.join(", ", values) + " FROM " + converter.formatMysqlSource(table) + " OFFSET " + options.offset() + ";";

        try (Statement statement = connection.createStatement()) {
            statement.execute(query);
        }
    }
}
