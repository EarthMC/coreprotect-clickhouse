package net.coreprotect.command.lookup;

import java.sql.Connection;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.google.common.base.Strings;

import net.coreprotect.config.ConfigHandler;
import net.coreprotect.database.Database;
import net.coreprotect.database.Lookup;
import net.coreprotect.database.logger.ItemLogger;
import net.coreprotect.database.lookup.PlayerLookup;
import net.coreprotect.database.statement.UserStatement;
import net.coreprotect.language.Phrase;
import net.coreprotect.language.Selector;
import net.coreprotect.listener.channel.PluginChannelHandshakeListener;
import net.coreprotect.listener.channel.PluginChannelListener;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.ChatUtils;
import net.coreprotect.utility.Color;
import net.coreprotect.utility.EntityUtils;
import net.coreprotect.utility.ItemUtils;
import net.coreprotect.utility.MaterialUtils;
import net.coreprotect.utility.StringUtils;
import net.coreprotect.utility.WorldUtils;

public class StandardLookupThread implements Runnable {
    private final CommandSender player;
    private final Command command;
    private final List<String> rollbackUsers;
    private final List<Object> blockList;
    private final Map<Object, Boolean> excludedBlocks;
    private final List<String> excludedUsers;
    private final List<Integer> actions;
    private final Integer[] radius;
    private final Location location;
    private final int x;
    private final int y;
    private final int z;
    private final int worldId;
    private final int argWorldId;
    private final long timeStart;
    private final long timeEnd;
    private final int noisy;
    private final int excluded;
    private final int restricted;
    private final int page;
    private final int displayResults;
    private final int typeLookup;
    private final String rtime;
    private final boolean count;

    public StandardLookupThread(CommandSender player, Command command, List<String> rollbackUsers, List<Object> blockList, Map<Object, Boolean> excludedBlocks, List<String> excludedUsers, List<Integer> actions, Integer[] radius, Location location, int x, int y, int z, int worldId, int argWorldId, long timeStart, long timeEnd, int noisy, int excluded, int restricted, int page, int displayResults, int typeLookup, String rtime, boolean count) {
        this.player = player;
        this.command = command;
        this.rollbackUsers = rollbackUsers;
        this.blockList = blockList;
        this.excludedBlocks = excludedBlocks;
        this.excludedUsers = excludedUsers;
        this.actions = actions;
        this.radius = radius;
        this.location = location;
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldId = worldId;
        this.argWorldId = argWorldId;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.noisy = noisy;
        this.excluded = excluded;
        this.restricted = restricted;
        this.page = page;
        this.displayResults = displayResults;
        this.typeLookup = typeLookup;
        this.rtime = rtime;
        this.count = count;
    }

    @Override
    public void run() {
        try (Connection connection = Database.getConnection(true)) {
            ConfigHandler.lookupThrottle.put(player.getName(), new Object[] { true, System.currentTimeMillis() });

            List<String> uuidList = new ArrayList<>();
            Location finalLocation = location;
            boolean exists = false;
            String bc = x + "." + y + "." + z + "." + worldId + "." + timeStart + "." + timeEnd + "." + noisy + "." + excluded + "." + restricted + "." + argWorldId + "." + displayResults;
            ConfigHandler.lookupCommand.put(player.getName(), bc);
            ConfigHandler.lookupPage.put(player.getName(), page);
            ConfigHandler.lookupTime.put(player.getName(), rtime);
            ConfigHandler.lookupType.put(player.getName(), 5);
            ConfigHandler.lookupElist.put(player.getName(), excludedBlocks);
            ConfigHandler.lookupEUserlist.put(player.getName(), excludedUsers);
            ConfigHandler.lookupBlist.put(player.getName(), blockList);
            ConfigHandler.lookupUlist.put(player.getName(), rollbackUsers);
            ConfigHandler.lookupAlist.put(player.getName(), actions);
            ConfigHandler.lookupRadius.put(player.getName(), radius);

            if (connection == null) {
                Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.DATABASE_BUSY));
                return;
            }

            try (Statement statement = connection.createStatement()) {
                String baduser = "";
                for (String check : rollbackUsers) {
                    if ((!check.equals("#global") && !check.equals("#container")) || actions.contains(9)) {
                        exists = PlayerLookup.playerExists(connection, check);
                        if (!exists) {
                            baduser = check;
                            break;
                        } else if (actions.contains(9)) {
                            if (ConfigHandler.uuidCache.get(check.toLowerCase(Locale.ROOT)) != null) {
                                String uuid = ConfigHandler.uuidCache.get(check.toLowerCase(Locale.ROOT));
                                uuidList.add(uuid);
                            }
                        }
                    } else {
                        exists = true;
                    }
                }

                if (exists) {
                    for (String check : excludedUsers) {
                        if (!check.equals("#global") && !check.equals("#hopper")) {
                            exists = PlayerLookup.playerExists(connection, check);
                            if (!exists) {
                                baduser = check;
                                break;
                            }
                        } else if (check.equals("#global")) {
                            baduser = "#global";
                            exists = false;
                        }
                    }
                }

                if (!exists) {
                    Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.USER_NOT_FOUND, baduser));
                    return;
                }

                List<String> userList = new ArrayList<>();
                if (!actions.contains(9)) {
                    userList = rollbackUsers;
                }

                int currentUnixSeconds = (int) (System.currentTimeMillis() / 1000L);
                boolean restrict_world = radius != null;

                if (finalLocation == null) {
                    restrict_world = false;
                }

                if (argWorldId > 0) {
                    restrict_world = true;
                    finalLocation = new Location(Bukkit.getServer().getWorld(WorldUtils.getWorldName(argWorldId)), x, y, z);
                } else if (finalLocation != null) {
                    finalLocation = new Location(Bukkit.getServer().getWorld(WorldUtils.getWorldName(worldId)), x, y, z);
                }

                Long[] rowData = new Long[] { 0L, 0L, 0L, 0L };
                long rowMax = (long) page * displayResults;
                long pageStart = rowMax - displayResults;
                boolean checkRows = true;

                if (typeLookup == 5 && page > 1) {
                    rowData = ConfigHandler.lookupRows.get(player.getName());
                    long cachedRows = rowData[3];

                    if (pageStart < cachedRows) {
                        checkRows = false;
                    }
                }

                final LookupResult<?> lookupResult = Lookup.performLookup(statement, player, uuidList, userList, blockList, excludedBlocks, excludedUsers, actions, finalLocation, radius, rowData, timeStart, timeEnd, (int) pageStart, displayResults, restrict_world, true, checkRows);
                if (lookupResult == null) {
                    Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- An error occurred while processing this lookup.");
                    return;
                }

                long rows = lookupResult.totalResultSize();

                if (checkRows) {
                    rowData[3] = rows;
                    ConfigHandler.lookupRows.put(player.getName(), rowData);
                } else {
                    // retrieve cached rows
                    rows = rowData[3];
                }

                if (count) {
                    String row_format = NumberFormat.getInstance().format(rows);
                    Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.LOOKUP_ROWS_FOUND, row_format, (rows == 1 ? Selector.FIRST : Selector.SECOND)));
                    return;
                } else if (pageStart >= rows) {
                    if (rows > 0) {
                        Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.NO_RESULTS_PAGE, Selector.FIRST));
                    } else {
                        Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.NO_RESULTS));
                    }

                    return;
                }

                Chat.sendMessage(player, Color.WHITE + "----- " + Color.DARK_AQUA + Phrase.build(Phrase.LOOKUP_HEADER, "CoreProtect" + Color.WHITE + " | " + Color.DARK_AQUA) + Color.WHITE + " -----");
                switch (lookupResult) {
                    case ChatLookupResult chatResult -> {
                        for (ChatLookupData data : chatResult.data()) {
                            String timeAgo = ChatUtils.getTimeSince(data.time(), currentUnixSeconds, true);
                            final String dashColor = data.cancelled() ? Color.RED : Color.WHITE;

                            Chat.sendComponent(player, timeAgo + " " + dashColor + "- " + Color.DARK_AQUA + data.playerName() + ": " + Color.WHITE, data.message());
                            if (PluginChannelHandshakeListener.getInstance().isPluginChannelPlayer(player)) {
                                PluginChannelListener.getInstance().sendMessageData(player, data.time(), data.playerName(), data.message(), false, data.x(), data.y(), data.z(), data.worldId());
                            }
                        }
                    }
                    case SessionLookupResult sessionResult -> {
                        for (SessionLookupData data : sessionResult.data()) {
                            String timeAgo = ChatUtils.getTimeSince(data.time(), currentUnixSeconds, true);
                            int timeLength = 50 + (ChatUtils.getTimeSince(data.time(), currentUnixSeconds, false).replaceAll("[^0-9]", "").length() * 6);
                            String leftPadding = Color.BOLD + Strings.padStart("", 10, ' ');

                            if (timeLength % 4 == 0) {
                                leftPadding = Strings.padStart("", timeLength / 4, ' ');
                            } else {
                                leftPadding = leftPadding + Color.WHITE + Strings.padStart("", (timeLength - 50) / 4, ' ');
                            }

                            final boolean isLogin = data.action() == 1;
                            String tag = (isLogin ? Color.GREEN + "+" : Color.RED + "-");
                            Chat.sendComponent(player, timeAgo + " " + tag + " " + Color.DARK_AQUA + Phrase.build(Phrase.LOOKUP_LOGIN, Color.DARK_AQUA + data.playerName() + Color.WHITE, (isLogin ? Selector.FIRST : Selector.SECOND)));
                            Chat.sendComponent(player, Color.WHITE + leftPadding + Color.GREY + "^ " + ChatUtils.getCoordinates(command.getName(), data.worldId(), data.x(), data.y(), data.z(), true, true));

                            PluginChannelListener.getInstance().sendInfoData(player, data.time(), Phrase.LOOKUP_LOGIN, (isLogin ? Selector.FIRST : Selector.SECOND), data.playerName(), -1, data.x(), data.y(), data.z(), data.worldId());
                        }
                    }
                    case UsernameHistoryLookupResult usernameHistoryResult -> {
                        for (UsernameHistoryData data : usernameHistoryResult.data()) {
                            String user = ConfigHandler.uuidCacheReversed.get(data.uuid());
                            String timeAgo = ChatUtils.getTimeSince(data.time(), currentUnixSeconds, true);

                            Chat.sendComponent(player, timeAgo + " " + Color.WHITE + "- " + Phrase.build(Phrase.LOOKUP_USERNAME, Color.DARK_AQUA + user + Color.WHITE, Color.DARK_AQUA + data.username() + Color.WHITE));
                            PluginChannelListener.getInstance().sendUsernameData(player, data.time(), user, data.username());
                        }
                    }
                    case SignLookupResult signResult -> {
                        for (SignLookupData data : signResult.data()) {
                            String timeAgo = ChatUtils.getTimeSince(data.time(), currentUnixSeconds, true);
                            int timeLength = 50 + (ChatUtils.getTimeSince(data.time(), currentUnixSeconds, false).replaceAll("[^0-9]", "").length() * 6);
                            String leftPadding = Color.BOLD + Strings.padStart("", 10, ' ');
                            if (timeLength % 4 == 0) {
                                leftPadding = Strings.padStart("", timeLength / 4, ' ');
                            } else {
                                leftPadding = leftPadding + Color.WHITE + Strings.padStart("", (timeLength - 50) / 4, ' ');
                            }

                            Chat.sendComponent(player, timeAgo + " " + Color.WHITE + "- " + Color.DARK_AQUA + data.playerName() + ": " + Color.WHITE, data.text());
                            Chat.sendComponent(player, Color.WHITE + leftPadding + Color.GREY + "^ " + ChatUtils.getCoordinates(command.getName(), data.worldId(), data.x(), data.y(), data.z(), true, true));
                            PluginChannelListener.getInstance().sendMessageData(player, data.time(), data.playerName(), data.text(), true, data.x(), data.y(), data.z(), data.worldId());
                        }
                    }
                    case CommonLookupResult commonResult -> {
                        if (actions.contains(4) && actions.contains(11)) { // inventory transactions
                            for (CommonLookupData data : commonResult.data()) {
                                String dplayer = data.playerName();
                                int dtype = data.type();
                                int ddata = data.data();
                                int daction = data.action();
                                int amount = data.amount();
                                int wid = data.worldId();
                                int dataX = data.x();
                                int dataY = data.y();
                                int dataZ = data.z();
                                String rollbackDecoration = ((data.rolledBack() == 2 || data.rolledBack() == 3) ? Color.STRIKETHROUGH : "");
                                String timeAgo = ChatUtils.getTimeSince(data.time(), currentUnixSeconds, true);
                                Material blockType = ItemUtils.itemFilter(MaterialUtils.getType(dtype), data.table() == 0);
                                String dname = StringUtils.nameFilter(blockType.name().toLowerCase(Locale.ROOT), ddata);
                                String itemData = data.metadata();
                                String hover = ItemUtils.getItemHover(itemData, dtype, amount);

                                String selector = Selector.FIRST;
                                String tag = Color.WHITE + "-";
                                if (daction == 2 || daction == 3) { // LOOKUP_ITEM
                                    selector = (daction != 2 ? Selector.FIRST : Selector.SECOND);
                                    tag = (daction != 2 ? Color.GREEN + "+" : Color.RED + "-");
                                } else if (daction == 4 || daction == 5) { // LOOKUP_STORAGE
                                    selector = (daction == 4 ? Selector.FIRST : Selector.SECOND);
                                    tag = (daction == 4 ? Color.GREEN + "+" : Color.RED + "-");
                                } else if (daction == 6 || daction == 7) { // LOOKUP_PROJECTILE
                                    selector = Selector.SECOND;
                                    tag = Color.RED + "-";
                                } else if (daction == ItemLogger.ITEM_BREAK || daction == ItemLogger.ITEM_DESTROY || daction == ItemLogger.ITEM_CREATE) {
                                    selector = (daction == ItemLogger.ITEM_CREATE ? Selector.FIRST : Selector.SECOND);
                                    tag = (daction == ItemLogger.ITEM_CREATE ? Color.GREEN + "+" : Color.RED + "-");
                                } else if (daction == ItemLogger.ITEM_SELL || daction == ItemLogger.ITEM_BUY) { // LOOKUP_TRADE
                                    selector = (daction == ItemLogger.ITEM_BUY ? Selector.FIRST : Selector.SECOND);
                                    tag = (daction == ItemLogger.ITEM_BUY ? Color.GREEN + "+" : Color.RED + "-");
                                } else { // LOOKUP_CONTAINER
                                    selector = (daction == 0 ? Selector.FIRST : Selector.SECOND);
                                    tag = (daction == 0 ? Color.GREEN + "+" : Color.RED + "-");
                                }

                                Chat.sendComponent(player, timeAgo + " " + tag + " " + Phrase.build(Phrase.LOOKUP_CONTAINER, Color.DARK_AQUA + rollbackDecoration + dplayer + Color.WHITE + rollbackDecoration, "x" + amount, ItemUtils.createItemTooltip(Color.DARK_AQUA + rollbackDecoration + dname, hover) + Color.WHITE, selector));
                                PluginChannelListener.getInstance().sendData(player, data.time(), Phrase.LOOKUP_CONTAINER, selector, dplayer, dname, amount, dataX, dataY, dataZ, wid, rollbackDecoration, true, tag.contains("+"));
                            }
                        } else {
                            for (CommonLookupData data : commonResult.data()) { // everything else
                                int rolledBack = data.rolledBack();
                                String rollbackDecoration = "";
                                if (rolledBack == 1 || rolledBack == 3) {
                                    rollbackDecoration = Color.STRIKETHROUGH;
                                }

                                String dplayer = data.playerName();
                                int dataX = data.x();
                                int dataY = data.y();
                                int dataZ = data.z();
                                int dtype = data.type();
                                int ddata = data.data();
                                int daction = data.action();
                                int worldId = data.worldId();
                                int amount = data.amount();
                                String tag = Color.WHITE + "-";

                                String timeago = ChatUtils.getTimeSince(data.time(), currentUnixSeconds, true);
                                int timeLength = 50 + (ChatUtils.getTimeSince(data.time(), currentUnixSeconds, false).replaceAll("[^0-9]", "").length() * 6);
                                String leftPadding = Color.BOLD + Strings.padStart("", 10, ' ');
                                if (timeLength % 4 == 0) {
                                    leftPadding = Strings.padStart("", timeLength / 4, ' ');
                                } else {
                                    leftPadding = leftPadding + Color.WHITE + Strings.padStart("", (timeLength - 50) / 4, ' ');
                                }

                                String dname = "";
                                boolean isPlayer = false;
                                if (daction == 3 && !actions.contains(11) && amount == -1) {
                                    if (dtype == 0) {
                                        String playerName = ConfigHandler.playerIdCacheReversed.get(ddata);
                                        if (playerName == null) {
                                            playerName = UserStatement.loadName(connection, ddata);
                                        }

                                        dname = playerName;
                                        isPlayer = true;
                                    } else {
                                        dname = EntityUtils.getEntityType(dtype).name();
                                    }
                                } else {
                                    dname = MaterialUtils.getType(dtype).name().toLowerCase(Locale.ROOT);
                                    dname = StringUtils.nameFilter(dname, ddata);
                                }

                                /* CH - don't add minecraft: just to remove it
                                if (!dname.isEmpty() && !isPlayer) {
                                    dname = "minecraft:" + dname.toLowerCase(Locale.ROOT);
                                }
                                */

                                // Hide "minecraft:" for now.
                                if (dname.contains("minecraft:")) {
                                    String[] blockNameSplit = dname.split(":", 2);
                                    dname = blockNameSplit[1];
                                }

                                Phrase phrase = Phrase.LOOKUP_BLOCK;
                                String selector = Selector.FIRST;
                                String action = "a:block";
                                if (actions.contains(4) || actions.contains(5) || actions.contains(11) || amount > -1) {
                                    String itemData = data.metadata();
                                    String hover = ItemUtils.getItemHover(itemData, dtype, amount);

                                    if (daction == 2 || daction == 3) {
                                        phrase = Phrase.LOOKUP_ITEM; // {picked up|dropped}
                                        selector = (daction != 2 ? Selector.FIRST : Selector.SECOND);
                                        tag = (daction != 2 ? Color.GREEN + "+" : Color.RED + "-");
                                        action = "a:item";
                                    } else if (daction == 4 || daction == 5) {
                                        phrase = Phrase.LOOKUP_STORAGE; // {deposited|withdrew}
                                        selector = (daction != 4 ? Selector.FIRST : Selector.SECOND);
                                        tag = (daction != 4 ? Color.RED + "-" : Color.GREEN + "+");
                                        action = "a:item";
                                    } else if (daction == 6 || daction == 7) {
                                        phrase = Phrase.LOOKUP_PROJECTILE; // {threw|shot}
                                        selector = (daction != 7 ? Selector.FIRST : Selector.SECOND);
                                        tag = Color.RED + "-";
                                        action = "a:item";
                                    } else {
                                        phrase = Phrase.LOOKUP_CONTAINER; // {added|removed}
                                        selector = (daction != 0 ? Selector.FIRST : Selector.SECOND);
                                        tag = (daction != 0 ? Color.GREEN + "+" : Color.RED + "-");
                                        action = "a:container";
                                    }

                                    Chat.sendComponent(player, timeago + " " + tag + " " + Phrase.build(phrase, Color.DARK_AQUA + rollbackDecoration + dplayer + Color.WHITE + rollbackDecoration, "x" + amount, ItemUtils.createItemTooltip(Color.DARK_AQUA + rollbackDecoration + dname, hover) + Color.WHITE, selector));
                                    PluginChannelListener.getInstance().sendData(player, data.time(), phrase, selector, dplayer, dname, (tag.contains("+") ? 1 : -1), dataX, dataY, dataZ, worldId, rollbackDecoration, action.contains("container"), tag.contains("+"));
                                } else {
                                    if (daction == 2 || daction == 3) {
                                        phrase = Phrase.LOOKUP_INTERACTION; // {clicked|killed}
                                        selector = (daction != 3 ? Selector.FIRST : Selector.SECOND);
                                        tag = (daction != 3 ? Color.WHITE + "-" : Color.RED + "-");
                                        action = (daction == 2 ? "a:click" : "a:kill");
                                    } else {
                                        phrase = Phrase.LOOKUP_BLOCK; // {placed|broke}
                                        selector = (daction != 0 ? Selector.FIRST : Selector.SECOND);
                                        tag = (daction != 0 ? Color.GREEN + "+" : Color.RED + "-");
                                    }

                                    Chat.sendComponent(player, timeago + " " + tag + " " + Phrase.build(phrase, Color.DARK_AQUA + rollbackDecoration + dplayer + Color.WHITE + rollbackDecoration, Color.DARK_AQUA + rollbackDecoration + dname + Color.WHITE, selector));
                                    PluginChannelListener.getInstance().sendData(player, data.time(), phrase, selector, dplayer, dname, (tag.contains("+") ? 1 : -1), dataX, dataY, dataZ, worldId, rollbackDecoration, false, tag.contains("+"));
                                }

                                action = (actions.isEmpty() ? " (" + action + ")" : "");
                                Chat.sendComponent(player, Color.WHITE + leftPadding + Color.GREY + "^ " + ChatUtils.getCoordinates(command.getName(), worldId, dataX, dataY, dataZ, true, true) + Color.GREY + Color.ITALIC + action);
                            }
                        }
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + lookupResult);
                }

                if (rows > displayResults) {
                    int total_pages = (int) Math.ceil(rows / (displayResults + 0.0));
                    if (actions.contains(6) || actions.contains(7) || actions.contains(9) || (actions.contains(4) && actions.contains(11))) {
                        Chat.sendMessage(player, "-----");
                    }
                    Chat.sendComponent(player, ChatUtils.getPageNavigation(command.getName(), page, total_pages));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            ConfigHandler.lookupThrottle.put(player.getName(), new Object[] { false, System.currentTimeMillis() });
        }
    }
}
