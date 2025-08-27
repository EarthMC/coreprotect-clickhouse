package net.coreprotect.command;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import net.coreprotect.utility.GitProperties;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import net.coreprotect.CoreProtect;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.Consumer;
import net.coreprotect.consumer.process.Process;
import net.coreprotect.language.Phrase;
import net.coreprotect.language.Selector;
import net.coreprotect.patch.Patch;
import net.coreprotect.thread.NetworkHandler;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.Color;
import net.coreprotect.utility.VersionUtils;

public class StatusCommand {
    private static ConcurrentHashMap<String, Boolean> alert = new ConcurrentHashMap<>();

    protected static void runCommand(CommandSender player, boolean permission, String[] args) {
        if (!permission) {
            Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.NO_PERMISSION));
            return;
        }

        class BasicThread implements Runnable {
            @Override
            public void run() {
                try {
                    CoreProtect instance = CoreProtect.getInstance();
                    PluginDescriptionFile pdfFile = instance.getDescription();

                    String versionCheck = "";
                    if (Config.getGlobal().CHECK_UPDATES) {
                        String latestVersion = NetworkHandler.latestVersion();
                        if (latestVersion != null) {
                            versionCheck = " (" + Phrase.build(Phrase.LATEST_VERSION, "v" + latestVersion) + ")";
                        }
                    }

                    Chat.sendMessage(player, Color.WHITE + "----- " + Color.DARK_AQUA + "CoreProtect" + (VersionUtils.isCommunityEdition() ? " " + ConfigHandler.COMMUNITY_EDITION : "") + Color.WHITE + " -----");
                    Chat.sendMessage(player, Color.DARK_AQUA + Phrase.build(Phrase.STATUS_VERSION, Color.WHITE, ConfigHandler.EDITION_NAME + " v" + pdfFile.getVersion() + ".") + versionCheck);

                    String donationKey = NetworkHandler.donationKey();
                    if (donationKey != null) {
                        Chat.sendMessage(player, Color.DARK_AQUA + Phrase.build(Phrase.STATUS_LICENSE, Color.WHITE, Phrase.build(Phrase.VALID_DONATION_KEY)) + " (" + donationKey + ")");
                    }
                    else {
                        Chat.sendMessage(player, Color.DARK_AQUA + Phrase.build(Phrase.STATUS_LICENSE, Color.WHITE, Phrase.build(Phrase.INVALID_DONATION_KEY)) + Color.GREY + Color.ITALIC + " (" + Phrase.build(Phrase.CHECK_CONFIG) + ")");
                    }

                    /*
                        Items processed (since server start)
                        Items processed (last 60 minutes)
                     */

                    // Using MySQL/SQLite (Database Size: 587MB)

                    String firstVersion = Patch.getFirstVersion();
                    if (firstVersion.length() > 0) {
                        firstVersion = " (" + Phrase.build(Phrase.FIRST_VERSION, firstVersion) + ")";
                    }
                    if (Config.getGlobal().MYSQL) {
                        Chat.sendMessage(player, Color.DARK_AQUA + Phrase.build(Phrase.STATUS_DATABASE, Color.WHITE, "MySQL") + firstVersion);
                    }
                    else {
                        Chat.sendMessage(player, Color.DARK_AQUA + Phrase.build(Phrase.STATUS_DATABASE, Color.WHITE, "SQLite") + firstVersion);
                    }

                    if (ConfigHandler.worldeditEnabled) {
                        Chat.sendMessage(player, Color.DARK_AQUA + Phrase.build(Phrase.STATUS_INTEGRATION, Color.WHITE, "WorldEdit", Selector.FIRST));
                    }
                    else if (instance.getServer().getPluginManager().getPlugin("WorldEdit") != null) {
                        Chat.sendMessage(player, Color.DARK_AQUA + Phrase.build(Phrase.STATUS_INTEGRATION, Color.WHITE, "WorldEdit", Selector.SECOND));
                    }

                    try {
                        int consumerCount = 0;
                        int currentConsumerSize = Process.getCurrentConsumerSize();
                        if (currentConsumerSize == 0) {
                            consumerCount = Consumer.getConsumerSize(0) + Consumer.getConsumerSize(1);
                        }
                        else {
                            int consumerId = (Consumer.currentConsumer == 1) ? 1 : 0;
                            consumerCount = Consumer.getConsumerSize(consumerId) + currentConsumerSize;
                        }

                        if (consumerCount >= 1 && (player instanceof Player)) {
                            if (Config.getConfig(((Player) player).getWorld()).PLAYER_COMMANDS) {
                                consumerCount = consumerCount - 1;
                            }
                        }

                        Chat.sendMessage(player, Color.DARK_AQUA + Phrase.build(Phrase.STATUS_CONSUMER, Color.WHITE, String.format("%,d", consumerCount), (consumerCount == 1 ? Selector.FIRST : Selector.SECOND)));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Functions.sendMessage(player, Color.DARK_AQUA + "Website: " + Color.WHITE + "www.coreprotect.net/updates/");

                    // Functions.sendMessage(player, Color.DARK_AQUA + Phrase.build(Phrase.LINK_DISCORD, Color.WHITE + "www.coreprotect.net/discord/").replaceFirst(":", ":" + Color.WHITE));
                    //Chat.sendMessage(player, Color.DARK_AQUA + Phrase.build(Phrase.LINK_DISCORD, Color.WHITE, "www.coreprotect.net/discord/"));
                    //Chat.sendMessage(player, Color.DARK_AQUA + Phrase.build(Phrase.LINK_PATREON, Color.WHITE, "www.patreon.com/coreprotect/"));

                    try {
                        final GitProperties gitProperties = GitProperties.retrieve(CoreProtect.getInstance());

                        String repositoryUrl = gitProperties.repositoryUrl();
                        if (repositoryUrl.endsWith(".git")) {
                            repositoryUrl = repositoryUrl.substring(0, repositoryUrl.length() - ".git".length());
                        }

                        final String viewCommitUrl = repositoryUrl + "/commit/" + gitProperties.commit();

                        player.sendMessage(Component.text("Build Information: ", TextColor.color(0x31b0e8))
                                .append(Component.text(gitProperties.commitShort(), NamedTextColor.WHITE))
                                .append(Component.text("/"))
                                .append(Component.text(gitProperties.branch(), NamedTextColor.WHITE))
                                .clickEvent(viewCommitUrl.startsWith("http") ? ClickEvent.openUrl(viewCommitUrl) : null)
                                .hoverEvent(HoverEvent.showText(Component.text(gitProperties.message(), NamedTextColor.WHITE)))
                        );
                    } catch (IOException ignored) {}
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Runnable runnable = new BasicThread();
        Thread thread = new Thread(runnable);
        thread.start();
    }
}
