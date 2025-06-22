package net.coreprotect.database.convert.process;

import net.coreprotect.database.convert.ClickhouseConverter;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConvertProcess {
    void convertTable(ClickhouseConverter converter, ConvertOptions options, Connection connection) throws SQLException;
}
