package net.coreprotect.database.convert;

import com.google.gson.JsonSyntaxException;
import net.coreprotect.CoreProtect;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class ClickhouseConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClickhouseConverter.class);

    private String mysqlAddress;
    private String mysqlDatabase;
    private String mysqlUser;
    private String mysqlPassword;

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
                final MySQLLoginInformation information = JsonSerialization.DEFAULT_GSON.fromJson(Files.readString(credentialsPath), MySQLLoginInformation.class);

                this.mysqlAddress = information.address;
                this.mysqlDatabase = information.database;
                this.mysqlUser = information.user;
                this.mysqlPassword = information.password;
            } catch (IOException | JsonSyntaxException e) {
                LOGGER.warn("Failed to read saved credentials file", e);
            }
        }
    }

    public void addTable(TableData table) {
        this.tables.put(table.getName(), table);
    }

    public String mysqlAddress() {
        return this.mysqlAddress;
    }

    public String mysqlUser() {
        return this.mysqlUser;
    }

    public String mysqlPassword() {
        return this.mysqlPassword;
    }

    public String mysqlDatabase() {
        return this.mysqlDatabase;
    }

    public void login(String address, String database, String username, String password) {
        this.mysqlAddress = address;
        this.mysqlDatabase = database;
        this.mysqlUser = username;
        this.mysqlPassword = password;
    }

    public String formatMysqlSource(TableData table) {
        return "mysql('" + mysqlAddress + "', '" + mysqlDatabase + "', '" + table.fullName() + "', '" + mysqlUser + "', '" + mysqlPassword + "')";
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
            Files.writeString(this.credentialsPath, JsonSerialization.DEFAULT_GSON.toJson(new MySQLLoginInformation(this.mysqlAddress, this.mysqlDatabase, this.mysqlUser, this.mysqlPassword)));
        } catch (IOException e) {
            LOGGER.warn("Failed to write mysql credentials to disk", e);
        }
    }

    private record MySQLLoginInformation(String address, String database, String user, String password) {}
}
