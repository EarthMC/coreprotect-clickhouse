package net.coreprotect.utility;

import java.util.logging.Level;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public final class Chat {

    private Chat() {
        throw new IllegalStateException("Utility class");
    }

    public static void sendComponent(CommandSender sender, String string, String bypass) {
        final Component message;
        if (bypass != null) {
            message = MiniMessage.miniMessage().deserialize(string + (!string.contains("<extra>") ? "<extra>" : ""), Placeholder.unparsed("extra", bypass));
        } else {
            message = MiniMessage.miniMessage().deserialize(string);
        }

        sender.sendMessage(message);
    }

    public static void sendComponent(CommandSender sender, String string) {
        sendComponent(sender, string, null);
    }

    public static void sendMessage(CommandSender sender, String message) {
        sendComponent(sender, message);
    }

    public static void sendConsoleMessage(String string) {
        Bukkit.getServer().getConsoleSender().sendMessage(string);
    }

    public static void console(String string) {
        if (string.startsWith("-") || string.startsWith("[")) {
            Bukkit.getLogger().log(Level.INFO, string);
        }
        else {
            Bukkit.getLogger().log(Level.INFO, "[CoreProtect] " + string);
        }
    }

    public static void sendGlobalMessage(CommandSender user, String string) {
        if (user instanceof ConsoleCommandSender) {
            sendMessage(user, Color.DARK_AQUA + "[CoreProtect] " + Color.WHITE + string);
            return;
        }

        Server server = Bukkit.getServer();
        server.getConsoleSender().sendMessage("[CoreProtect] " + string);
        for (Player player : server.getOnlinePlayers()) {
            if (player.isOp() && !player.getName().equals(user.getName())) {
                sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + string);
            }
        }
        if (user instanceof Player) {
            if (((Player) user).isOnline()) {
                sendMessage(user, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + string);
            }
        }
    }

}
