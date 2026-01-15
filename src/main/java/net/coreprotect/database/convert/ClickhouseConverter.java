package net.coreprotect.database.convert;

import com.google.gson.JsonSyntaxException;
import net.coreprotect.CoreProtect;
import net.coreprotect.database.convert.process.CorruptResultRowException;
import net.coreprotect.database.convert.table.ArtTable;
import net.coreprotect.database.convert.table.BlockDataTable;
import net.coreprotect.database.convert.table.BlockTable;
import net.coreprotect.database.convert.table.ChatTable;
import net.coreprotect.database.convert.table.CommandTable;
import net.coreprotect.database.convert.table.ContainerTable;
import net.coreprotect.database.convert.table.EntityMapTable;
import net.coreprotect.database.convert.table.EntityTable;
import net.coreprotect.database.convert.table.ItemTable;
import net.coreprotect.database.convert.table.MaterialMapTable;
import net.coreprotect.database.convert.table.SessionTable;
import net.coreprotect.database.convert.table.SignTable;
import net.coreprotect.database.convert.table.SkullTable;
import net.coreprotect.database.convert.table.UserTable;
import net.coreprotect.database.convert.table.UsernameLogTable;
import net.coreprotect.database.convert.table.VersionTable;
import net.coreprotect.database.convert.table.WorldTable;
import net.coreprotect.utility.serialize.JsonSerialization;
import org.apache.hc.core5.http.MalformedChunkCodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class ClickhouseConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClickhouseConverter.class);

    private DatabaseAccess databaseAccess = null;

    private final Map<String, TableData> tables = new LinkedHashMap<>();
    private final Path credentialsPath;

    public ClickhouseConverter(final CoreProtect plugin) {
        addTable(new ArtTable());
        addTable(new EntityMapTable());
        addTable(new MaterialMapTable());
        addTable(new BlockDataTable());

        addTable(new WorldTable());
        addTable(new VersionTable());

        addTable(new BlockTable());
        addTable(new ChatTable());
        addTable(new CommandTable());
        addTable(new ContainerTable());
        addTable(new ItemTable());
        addTable(new EntityTable());
        addTable(new SessionTable());
        addTable(new SignTable());
        addTable(new SkullTable());
        addTable(new UserTable());
        addTable(new UsernameLogTable());

        this.credentialsPath = plugin.getDataPath().resolve("mysql-credentials.json");
        if (Files.exists(credentialsPath)) {
            try {
                this.databaseAccess = JsonSerialization.DEFAULT_GSON.fromJson(Files.readString(credentialsPath), MySQLLoginInformation.class);
            } catch (IOException | JsonSyntaxException e) {
                LOGGER.warn("Failed to read saved credentials file", e);
            }
        } else if (Files.exists(plugin.getDataPath().resolve("sqlite-database.json"))) {
            // Note: importing from sqlite is untested
            final Path path = plugin.getDataPath().resolve("sqlite-database.json");

            try {
                this.databaseAccess = JsonSerialization.DEFAULT_GSON.fromJson(Files.readString(path), SQLiteDatabaseInformation.class);
            } catch (IOException | JsonSyntaxException e) {
                LOGGER.warn("Failed to read sqlite database path", e);
            }
        }
    }

    public void addTable(TableData table) {
        this.tables.put(table.getName(), table);
    }

    public void login(DatabaseAccess databaseAccess) {
        this.databaseAccess = databaseAccess;
    }

    public DatabaseAccess databaseAccess() {
        return this.databaseAccess;
    }

    public String formatMysqlSource(TableData table) {
        return switch (this.databaseAccess) {
            case SQLiteDatabaseInformation sqlite -> "sqlite('" + sqlite.databasePath + "', " + table.fullName() + "')";
            case MySQLLoginInformation mysql -> "mysql('" + mysql.address + "', '" + mysql.database + "', '" + table.fullName() + "', '" + mysql.user + "', '" + mysql.password + "')";
            case null -> throw new IllegalStateException("No database source provided for the converter to use, use /co convert login ");
        };
    }

    public Logger logger() {
        return LOGGER;
    }

    public TableData getTable(String tableName) {
        return this.tables.get(tableName.toLowerCase(Locale.ROOT));
    }

    public Map<String, TableData> getTables() {
        return Collections.unmodifiableMap(this.tables);
    }

    public void saveCredentials() {
        try {
            if (this.databaseAccess == null) {
                Files.deleteIfExists(this.credentialsPath);
                return;
            }

            Files.writeString(this.credentialsPath, JsonSerialization.DEFAULT_GSON.toJson(this.databaseAccess));
        } catch (IOException e) {
            LOGGER.warn("Failed to write mysql credentials to disk", e);
        }
    }

    public boolean next(ResultSet resultSet, PreparedStatement batchStatement, long rowCount) throws SQLException {
        try {
            return resultSet.next();
        } catch (SQLException e) {
            Throwable cause = e;

            while ((cause = cause.getCause()) != null) {
                if (cause instanceof MalformedChunkCodingException) {
                    try {
                        batchStatement.executeBatch();
                    } catch (SQLException e2) {
                        LOGGER.warn("Failed to commit batch statement", e2);
                    }

                    throw new CorruptResultRowException(rowCount);
                }
            }

            throw e;
        }
    }

    public sealed interface DatabaseAccess permits MySQLLoginInformation, SQLiteDatabaseInformation {}

    public record MySQLLoginInformation(String address, String database, String user, String password) implements DatabaseAccess {}

    public record SQLiteDatabaseInformation(String databasePath) implements DatabaseAccess {}
}
