package net.coreprotect.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.coreprotect.CoreProtect;
import net.coreprotect.data.lookup.LookupResult;
import net.coreprotect.data.lookup.result.ChatLookupResult;
import net.coreprotect.data.lookup.result.CommonLookupResult;
import net.coreprotect.data.lookup.result.SessionLookupResult;
import net.coreprotect.data.lookup.result.SignLookupResult;
import net.coreprotect.data.lookup.result.UsernameHistoryLookupResult;
import net.coreprotect.data.lookup.type.ChatLookupData;
import net.coreprotect.data.lookup.type.CommonLookupData;
import net.coreprotect.data.lookup.type.SessionLookupData;
import net.coreprotect.data.lookup.type.SignLookupData;
import net.coreprotect.data.lookup.type.UsernameHistoryData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.Queue;
import net.coreprotect.database.logger.ItemLogger;
import net.coreprotect.database.statement.UserStatement;
import net.coreprotect.utility.EntityUtils;
import net.coreprotect.utility.MaterialUtils;
import net.coreprotect.utility.WorldUtils;
import org.jetbrains.annotations.Nullable;

public class LookupRaw extends Queue {

    protected static LookupResult<?> performLookup(Statement statement, CommandSender user, List<String> checkUuids, List<String> checkUsers, List<Object> restrictList, Map<Object, Boolean> excludeList, List<String> excludeUserList, List<Integer> actionList, Location location, Integer[] radius, Long[] rowData, long startTime, long endTime, int limitOffset, int limitCount, boolean restrictWorld, boolean lookup, boolean countRows) {
        List<Integer> invalidRollbackActions = new ArrayList<>();
        invalidRollbackActions.add(2);

        if (!Config.getGlobal().ROLLBACK_ENTITIES && !actionList.contains(3)) {
            invalidRollbackActions.add(3);
        }

        if (actionList.contains(4) && actionList.contains(11)) {
            invalidRollbackActions.clear();
        }

        try (final ResultSet results = rawLookupResultSet(statement, user, checkUuids, checkUsers, restrictList, excludeList, excludeUserList, actionList, location, radius, rowData, startTime, endTime, limitOffset, limitCount, restrictWorld, lookup, countRows)) {
            if (results == null) {
                return null;
            }

            long rowCount = 0;

            if (actionList.contains(6) || actionList.contains(7)) { // chat/command
                final List<ChatLookupData> data = new ArrayList<>();

                while (results.next()) {
                    if (countRows) {
                        rowCount = results.getLong("count");
                    }
                    long resultId = results.getLong("id");
                    int resultTime = results.getInt("time");
                    int resultUserId = results.getInt("user");
                    String resultMessage = results.getString("message");

                    int resultWorldId = results.getInt("wid");
                    int resultX = results.getInt("x");
                    int resultY = results.getInt("y");
                    int resultZ = results.getInt("z");

                    data.add(new ChatLookupData(resultId, resultTime, resultUserId, resultMessage, resultWorldId, resultX, resultY, resultZ));
                }

                return new ChatLookupResult(rowCount, data);
            } else if (actionList.contains(8)) {
                final List<SessionLookupData> data = new ArrayList<>();

                while (results.next()) {
                    if (countRows) {
                        rowCount = results.getLong("count");
                    }
                    long resultId = results.getLong("id");
                    long resultTime = results.getLong("time");
                    int resultUserId = results.getInt("user");
                    int resultWorldId = results.getInt("wid");
                    int resultX = results.getInt("x");
                    int resultY = results.getInt("y");
                    int resultZ = results.getInt("z");
                    int resultAction = results.getInt("action");

                    data.add(new SessionLookupData(resultId, resultTime, resultUserId, resultWorldId, resultX, resultY, resultZ, resultAction));
                }

                return new SessionLookupResult(rowCount, data);
            } else if (actionList.contains(9)) {
                final List<UsernameHistoryData> data = new ArrayList<>();

                while (results.next()) {
                    if (countRows) {
                        rowCount = results.getLong("count");
                    }
                    long resultId = results.getLong("id");
                    long resultTime = results.getLong("time");
                    String resultUuid = results.getString("uuid");
                    String resultUser = results.getString("user");

                    data.add(new UsernameHistoryData(resultId, resultTime, resultUuid, resultUser));
                }

                return new UsernameHistoryLookupResult(rowCount, data);
            } else if (actionList.contains(10)) {
                final List<SignLookupData> data = new ArrayList<>();

                while (results.next()) {
                    if (countRows) {
                        rowCount = results.getLong("count");
                    }
                    long resultId = results.getLong("id");
                    int resultTime = results.getInt("time");
                    int resultUserId = results.getInt("user");
                    int resultWorldId = results.getInt("wid");
                    int resultX = results.getInt("x");
                    int resultY = results.getInt("y");
                    int resultZ = results.getInt("z");
                    boolean isFront = results.getInt("face") == 0;
                    String line1 = results.getString("line_1");
                    String line2 = results.getString("line_2");
                    String line3 = results.getString("line_3");
                    String line4 = results.getString("line_4");
                    String line5 = results.getString("line_5");
                    String line6 = results.getString("line_6");
                    String line7 = results.getString("line_7");
                    String line8 = results.getString("line_8");

                    StringBuilder message = new StringBuilder();
                    if (isFront && line1 != null && !line1.isEmpty()) {
                        message.append(line1);
                        if (!line1.endsWith(" ")) {
                            message.append(" ");
                        }
                    }
                    if (isFront && line2 != null && !line2.isEmpty()) {
                        message.append(line2);
                        if (!line2.endsWith(" ")) {
                            message.append(" ");
                        }
                    }
                    if (isFront && line3 != null && !line3.isEmpty()) {
                        message.append(line3);
                        if (!line3.endsWith(" ")) {
                            message.append(" ");
                        }
                    }
                    if (isFront && line4 != null && !line4.isEmpty()) {
                        message.append(line4);
                        if (!line4.endsWith(" ")) {
                            message.append(" ");
                        }
                    }
                    if (!isFront && line5 != null && !line5.isEmpty()) {
                        message.append(line5);
                        if (!line5.endsWith(" ")) {
                            message.append(" ");
                        }
                    }
                    if (!isFront && line6 != null && !line6.isEmpty()) {
                        message.append(line6);
                        if (!line6.endsWith(" ")) {
                            message.append(" ");
                        }
                    }
                    if (!isFront && line7 != null && !line7.isEmpty()) {
                        message.append(line7);
                        if (!line7.endsWith(" ")) {
                            message.append(" ");
                        }
                    }
                    if (!isFront && line8 != null && !line8.isEmpty()) {
                        message.append(line8);
                        if (!line8.endsWith(" ")) {
                            message.append(" ");
                        }
                    }

                    data.add(new SignLookupData(resultId, resultTime, resultUserId, resultWorldId, resultX, resultY, resultZ, message.toString()));
                }

                return new SignLookupResult(rowCount, data);
            } else {
                List<CommonLookupData> data = new ArrayList<>();

                while (results.next()) {
                    if (countRows) {
                        rowCount = results.getLong("count");
                    }
                    int resultData = 0;
                    int resultAmount = -1;
                    Integer resultTable = null;
                    String resultMeta;
                    String resultBlockData = null;
                    long resultId = results.getLong("id");
                    int resultUserId = results.getInt("user");
                    int resultAction = results.getInt("action");
                    int resultRolledBack = results.getInt("rolled_back");
                    int resultType = results.getInt("type");
                    long resultTime = results.getLong("time");
                    int resultX = results.getInt("x");
                    int resultY = results.getInt("y");
                    int resultZ = results.getInt("z");
                    int resultWorldId = results.getInt("wid");

                    if ((lookup && actionList.isEmpty()) || actionList.contains(4) || actionList.contains(5) || actionList.contains(11)) {
                        resultData = results.getInt("data");
                        resultAmount = results.getInt("amount");
                        resultMeta = results.getString("metadata");
                        resultTable = results.getInt("tbl");
                    } else {
                        resultData = results.getInt("data");
                        resultMeta = results.getString("meta");
                        resultBlockData = results.getString("blockdata");
                    }

                    boolean valid = true;
                    if (!lookup && invalidRollbackActions.contains(resultAction)) {
                        valid = false;
                    }

                    if (valid) {
                        data.add(new CommonLookupData(resultId, resultTime, resultUserId, resultX, resultY, resultZ, resultType, resultData, resultAction, resultRolledBack, resultWorldId, resultAmount, resultMeta, resultBlockData, resultTable));
                    }
                }

                return new CommonLookupResult(rowCount, data);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    static @Nullable ResultSet rawLookupResultSet(Statement statement, CommandSender user, List<String> checkUuids, List<String> checkUsers, List<Object> restrictList, Map<Object, Boolean> excludeList, List<String> excludeUserList, List<Integer> actionList, Location location, Integer[] radius, Long[] rowData, long startTime, long endTime, int limitOffset, int limitCount, boolean restrictWorld, boolean lookup, boolean countRows) {
        String query = "";

        try {
            List<Integer> validActions = Arrays.asList(0, 1, 2, 3);
            if (radius != null) {
                restrictWorld = true;
            }

            boolean inventoryQuery = (actionList.contains(4) && actionList.contains(11));
            boolean validAction = false;
            String queryBlock = "";
            String queryEntity = "";
            String queryLimit = "";
            String queryLimitOffset = "";
            String queryTable = "block";
            String action = "";
            String actionExclude = "";
            String includeBlock = "";
            String includeEntity = "";
            String excludeBlock = "";
            String excludeEntity = "";
            String users = "";
            String uuids = "";
            String excludeUsers = "";
            String unionLimit = "";
            String index = "";

            if (!checkUuids.isEmpty()) {
                String list = "";

                for (String value : checkUuids) {
                    if (list.isEmpty()) {
                        list = "'" + value + "'";
                    }
                    else {
                        list += ",'" + value + "'";
                    }
                }

                uuids = list;
            }

            if (!checkUsers.contains("#global")) {
                final StringBuilder checkUserText = new StringBuilder();

                for (String checkUser : checkUsers) {
                    if (!checkUser.equals("#container")) {
                        int userId = UserStatement.getId(statement.getConnection(), checkUser, true);

                        if (checkUserText.isEmpty()) {
                            checkUserText.append(userId);
                        }
                        else {
                            checkUserText.append(",").append(userId);
                        }
                    }
                }
                users = checkUserText.toString();
            }

            if (!restrictList.isEmpty()) {
                final StringBuilder includeListMaterial = new StringBuilder();
                final StringBuilder includeListEntity = new StringBuilder();

                for (Object restrictTarget : restrictList) {
                    String targetName = "";

                    if (restrictTarget instanceof Material) {
                        targetName = ((Material) restrictTarget).name();
                        if (includeListMaterial.isEmpty()) {
                            includeListMaterial.append(MaterialUtils.getBlockId(targetName, false));
                        }
                        else {
                            includeListMaterial.append(",").append(MaterialUtils.getBlockId(targetName, false));
                        }

                        /* Include legacy IDs */
                        int legacyId = BukkitAdapter.ADAPTER.getLegacyBlockId((Material) restrictTarget);
                        if (legacyId > 0) {
                            includeListMaterial.append(",").append(legacyId);
                        }
                    }
                    else if (restrictTarget instanceof EntityType) {
                        targetName = ((EntityType) restrictTarget).name();
                        if (includeListEntity.isEmpty()) {
                            includeListEntity.append(EntityUtils.getEntityId(targetName, false));
                        }
                        else {
                            includeListEntity.append(",").append(EntityUtils.getEntityId(targetName, false));
                        }
                    }
                }

                includeBlock = includeListMaterial.toString();
                includeEntity = includeListEntity.toString();
            }

            if (!excludeList.isEmpty()) {
                final StringBuilder excludeListMaterial = new StringBuilder();
                final StringBuilder excludeListEntity = new StringBuilder();

                for (Object restrictTarget : excludeList.keySet()) {
                    String targetName = "";

                    if (restrictTarget instanceof Material) {
                        targetName = ((Material) restrictTarget).name();
                        if (excludeListMaterial.isEmpty()) {
                            excludeListMaterial.append(MaterialUtils.getBlockId(targetName, false));
                        }
                        else {
                            excludeListMaterial.append(",").append(MaterialUtils.getBlockId(targetName, false));
                        }

                        /* Include legacy IDs */
                        int legacyId = BukkitAdapter.ADAPTER.getLegacyBlockId((Material) restrictTarget);
                        if (legacyId > 0) {
                            excludeListMaterial.append(",").append(legacyId);
                        }
                    }
                    else if (restrictTarget instanceof EntityType) {
                        targetName = ((EntityType) restrictTarget).name();
                        if (excludeListEntity.isEmpty()) {
                            excludeListEntity.append(EntityUtils.getEntityId(targetName, false));
                        }
                        else {
                            excludeListEntity.append(",").append(EntityUtils.getEntityId(targetName, false));
                        }
                    }
                }

                excludeBlock = excludeListMaterial.toString();
                excludeEntity = excludeListEntity.toString();
            }

            if (!excludeUserList.isEmpty()) {
                final StringBuilder excludeUserText = new StringBuilder();

                for (String excludeTarget : excludeUserList) {
                    int userId = UserStatement.getId(statement.getConnection(), excludeTarget, true);

                    if (excludeUserText.isEmpty()) {
                        excludeUserText.append(userId);
                    }
                    else {
                        excludeUserText.append(",").append(userId);
                    }
                }

                excludeUsers = excludeUserText.toString();
            }

            // Specify actions to exclude from a:item
            if ((lookup && actionList.isEmpty()) || (actionList.contains(11) && actionList.size() == 1)) {
                actionExclude = ItemLogger.ITEM_BREAK +
                        "," + ItemLogger.ITEM_DESTROY +
                        "," + ItemLogger.ITEM_CREATE +
                        "," + ItemLogger.ITEM_SELL +
                        "," + ItemLogger.ITEM_BUY;
            }

            if (!actionList.isEmpty()) {
                StringBuilder actionText = new StringBuilder();
                for (Integer actionTarget : actionList) {
                    if (validActions.contains(actionTarget)) {
                        // If just looking up drops/pickups, remap the actions to the correct values
                        if (actionList.contains(11) && !actionList.contains(4)) {
                            if (actionTarget == ItemLogger.ITEM_REMOVE && !actionList.contains(ItemLogger.ITEM_DROP)) {
                                actionTarget = ItemLogger.ITEM_DROP;
                            }
                            else if (actionTarget == ItemLogger.ITEM_ADD && !actionList.contains(ItemLogger.ITEM_PICKUP)) {
                                actionTarget = ItemLogger.ITEM_PICKUP;
                            }
                        }

                        if (actionText.isEmpty()) {
                            actionText = actionText.append(actionTarget);
                        }
                        else {
                            actionText.append(",").append(actionTarget);
                        }

                        // If selecting from co_item & co_container, add in actions for both transaction types
                        if (actionList.contains(11) && actionList.contains(4)) {
                            if (actionTarget == ItemLogger.ITEM_REMOVE) {
                                actionText.append(",").append(ItemLogger.ITEM_PICKUP);
                                actionText.append(",").append(ItemLogger.ITEM_REMOVE_ENDER);
                                actionText.append(",").append(ItemLogger.ITEM_CREATE);
                                actionText.append(",").append(ItemLogger.ITEM_BUY);
                            }
                            if (actionTarget == ItemLogger.ITEM_ADD) {
                                actionText.append(",").append(ItemLogger.ITEM_DROP);
                                actionText.append(",").append(ItemLogger.ITEM_ADD_ENDER);
                                actionText.append(",").append(ItemLogger.ITEM_THROW);
                                actionText.append(",").append(ItemLogger.ITEM_SHOOT);
                                actionText.append(",").append(ItemLogger.ITEM_BREAK);
                                actionText.append(",").append(ItemLogger.ITEM_DESTROY);
                                actionText.append(",").append(ItemLogger.ITEM_SELL);
                            }
                        }
                        // If just looking up drops/pickups, include ender chest transactions
                        else if (actionList.contains(11) && !actionList.contains(4)) {
                            if (actionTarget == ItemLogger.ITEM_DROP) {
                                actionText.append(",").append(ItemLogger.ITEM_ADD_ENDER);
                                actionText.append(",").append(ItemLogger.ITEM_THROW);
                                actionText.append(",").append(ItemLogger.ITEM_SHOOT);
                            }
                            if (actionTarget == ItemLogger.ITEM_PICKUP) {
                                actionText.append(",").append(ItemLogger.ITEM_REMOVE_ENDER);
                            }
                        }
                    }
                }

                action = actionText.toString();
            }

            for (Integer value : actionList) {
                if (validActions.contains(value)) {
                    validAction = true;
                    break;
                }
            }

            if (restrictWorld) {
                int wid = WorldUtils.getWorldId(location.getWorld().getName());
                queryBlock = queryBlock + " wid=" + wid + " AND";
            }

            if (radius != null) {
                Integer xmin = radius[1];
                Integer xmax = radius[2];
                Integer ymin = radius[3];
                Integer ymax = radius[4];
                Integer zmin = radius[5];
                Integer zmax = radius[6];
                String queryY = "";

                if (ymin != null && ymax != null) {
                    queryY = " y >= '" + ymin + "' AND y <= '" + ymax + "' AND";
                }

                queryBlock = queryBlock + " x >= '" + xmin + "' AND x <= '" + xmax + "' AND z >= '" + zmin + "' AND z <= '" + zmax + "' AND" + queryY;
            }
            else if (actionList.contains(5)) {
                int worldId = WorldUtils.getWorldId(location.getWorld().getName());
                int x = (int) Math.floor(location.getX());
                int z = (int) Math.floor(location.getZ());
                int x2 = (int) Math.ceil(location.getX());
                int z2 = (int) Math.ceil(location.getZ());

                queryBlock = queryBlock + " wid=" + worldId + " AND (x = '" + x + "' OR x = '" + x2 + "') AND (z = '" + z + "' OR z = '" + z2 + "') AND y = '" + location.getBlockY() + "' AND";
            }

            if (validAction) {
                queryBlock = queryBlock + " action IN(" + action + ") AND";
            }
            else if (inventoryQuery || !actionExclude.isEmpty() || !includeBlock.isEmpty() || !includeEntity.isEmpty() || !excludeBlock.isEmpty() || !excludeEntity.isEmpty()) {
                queryBlock = queryBlock + " action NOT IN(-1) AND";
            }

            if (!includeBlock.isEmpty() || !includeEntity.isEmpty()) {
                queryBlock = queryBlock + " type IN(" + (!includeBlock.isEmpty() ? includeBlock : "0") + ") AND";
            }

            if (!excludeBlock.isEmpty() || !excludeEntity.isEmpty()) {
                queryBlock = queryBlock + " type NOT IN(" + (!excludeBlock.isEmpty() ? excludeBlock : "0") + ") AND";
            }

            if (!uuids.isEmpty()) {
                queryBlock = queryBlock + " uuid IN(" + uuids + ") AND";
            }

            if (!users.isEmpty()) {
                queryBlock = queryBlock + " user IN(" + users + ") AND";
            }

            if (!excludeUsers.isEmpty()) {
                queryBlock = queryBlock + " user NOT IN(" + excludeUsers + ") AND";
            }

            if (startTime > 0) {
                queryBlock = queryBlock + " time > '" + startTime + "' AND";
            }

            if (endTime > 0) {
                queryBlock = queryBlock + " time <= '" + endTime + "' AND";
            }

            if (actionList.contains(10)) {
                queryBlock = queryBlock + " action = '1' AND (LENGTH(line_1) > 0 OR LENGTH(line_2) > 0 OR LENGTH(line_3) > 0 OR LENGTH(line_4) > 0 OR LENGTH(line_5) > 0 OR LENGTH(line_6) > 0 OR LENGTH(line_7) > 0 OR LENGTH(line_8) > 0) AND";
            }

            if (!queryBlock.isEmpty()) {
                queryBlock = queryBlock.substring(0, queryBlock.length() - 4);
            }

            if (queryBlock.isEmpty()) {
                queryBlock = " 1";
            }

            queryEntity = queryBlock;
            if (!includeBlock.isEmpty() || !includeEntity.isEmpty()) {
                queryEntity = queryEntity.replace("type IN(" + (!includeBlock.isEmpty() ? includeBlock : "0") + ")", "type IN(" + (!includeEntity.isEmpty() ? includeEntity : "0") + ")");
            }
            if (!excludeBlock.isEmpty() || !excludeEntity.isEmpty()) {
                queryEntity = queryEntity.replace("type NOT IN(" + (!excludeBlock.isEmpty() ? excludeBlock : "0") + ")", "type NOT IN(" + (!excludeEntity.isEmpty() ? excludeEntity : "0") + ")");
            }

            String baseQuery = ((!includeEntity.isEmpty() || !excludeEntity.isEmpty()) ? queryEntity : queryBlock);
            if (limitOffset > -1 && limitCount > -1) {
                queryLimit = " LIMIT " + limitCount;
                unionLimit = countRows ? "" : (" ORDER BY time DESC LIMIT " + limitCount + " OFFSET " + limitOffset); // Do not add limits inside unions when rows need to be counted, otherwise the count breaks
                queryLimitOffset = queryLimit + (limitOffset > 0 ? (" OFFSET " + limitOffset) : "");
            }

            String rows = "rowid as id,time,user,wid,x,y,z,action,type,toString(data) as data,toString(meta) as meta,blockdata,rolled_back";
            String queryOrder = " ORDER BY rowid DESC";

            if (actionList.contains(4) || actionList.contains(5)) {
                queryTable = "container";
                rows = "rowid as id,time,user,wid,x,y,z,action,type,toString(data) as data,rolled_back,amount,toString(metadata) as metadata";
            }
            else if (actionList.contains(6) || actionList.contains(7)) {
                queryTable = "chat";
                rows = "rowid as id,time,user,message,wid,x,y,z";

                if (actionList.contains(7)) {
                    queryTable = "command";
                }
            }
            else if (actionList.contains(8)) {
                queryTable = "session";
                rows = "rowid as id,time,user,wid,x,y,z,action";
            }
            else if (actionList.contains(9)) {
                queryTable = "username_log";
                rows = "rowid as id,time,uuid,user";
            }
            else if (actionList.contains(10)) {
                queryTable = "sign";
                rows = "rowid as id,time,user,wid,x,y,z,face,line_1,line_2,line_3,line_4,line_5,line_6,line_7,line_8";
            }
            else if (actionList.contains(11)) {
                queryTable = "item";
                rows = "rowid as id,time,user,wid,x,y,z,type,toString(data) as metadata,'0' as data,amount,action,0 as rolled_back";
            }

            String unionSelect = "SELECT * FROM (";
            String countQuery = countRows ? "count(*) over () as count," : "";
            String baseSelect = countRows ? "SELECT count(*) over () as count, * FROM (" : unionSelect;

            boolean itemLookup = inventoryQuery;
            if ((lookup && actionList.isEmpty()) || (itemLookup && !actionList.contains(0))) {
                rows = "rowid as id,time,user,wid,x,y,z,type,toString(meta) as metadata,toString(data) as data,-1 as amount,action,rolled_back";

                if (inventoryQuery) {
                    if (validAction) {
                        baseQuery = baseQuery.replace("action IN(" + action + ")", "action IN(1)");
                    }
                    else {
                        baseQuery = baseQuery.replace("action NOT IN(-1)", "action IN(1)");
                    }

                    rows = "rowid as id,time,user,wid,x,y,z,type,toString(meta) as metadata,toString(data) as data,1 as amount,action,rolled_back";
                }

                if (!includeBlock.isEmpty() || !excludeBlock.isEmpty()) {
                    baseQuery = baseQuery.replace("action NOT IN(-1)", "action NOT IN(3)"); // if block specified for include/exclude, filter out entity data
                }

                query = baseSelect + "(SELECT " + "'0' as tbl," + rows + " FROM " + ConfigHandler.prefix + "block " + index + "WHERE" + baseQuery + unionLimit + ") UNION ALL ";
                itemLookup = true;
            }

            if (itemLookup) {
                rows = "rowid as id,time,user,wid,x,y,z,type,toString(metadata) as metadata,toString(data) as data,amount,action,rolled_back";
                query = query + unionSelect + "SELECT " + "'1' as tbl," + rows + " FROM " + ConfigHandler.prefix + "container WHERE" + queryBlock + unionLimit + ") UNION ALL ";

                rows = "rowid as id,time,user,wid,x,y,z,type,toString(data) as metadata,'0' as data,amount,action,rolled_back";
                queryOrder = " ORDER BY time DESC, tbl DESC, id DESC";

                if (!actionExclude.isEmpty()) {
                    queryBlock = queryBlock.replace("action NOT IN(-1)", "action NOT IN(" + actionExclude + ")");
                }

                query = query + unionSelect + "SELECT " + "'2' as tbl," + rows + " FROM " + ConfigHandler.prefix + "item WHERE" + queryBlock + unionLimit + ")";
            }

            if (query.isEmpty()) {
                if (!actionExclude.isEmpty()) {
                    baseQuery = baseQuery.replace("action NOT IN(-1)", "action NOT IN(" + actionExclude + ")");
                }

                query = "SELECT " + countQuery + "'0' as tbl," + rows + " FROM " + ConfigHandler.prefix + queryTable + " " + index + "WHERE" + baseQuery;
            }

            query = query.replace(" action NOT IN(-1) AND", ""); // Remove placeholders
            final boolean hasUnion = query.contains("UNION");
            if (hasUnion) {
                query += ")";
            }

            query += queryOrder + (hasUnion && !countRows ? queryLimit : queryLimitOffset) + " SETTINGS output_format_json_quote_64bit_integers=0";
            return statement.executeQuery(query);
        }
        catch (Exception e) {
            CoreProtect.getInstance().getSLF4JLogger().warn("An exception occurred while executing query '{}'", query, e);
            return null;
        }
    }
}
