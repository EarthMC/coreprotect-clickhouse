package net.coreprotect.listener.player;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.coreprotect.event.chat.PreLogChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import net.coreprotect.config.Config;
import net.coreprotect.consumer.Queue;

public final class PlayerChatListener extends Queue implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        final PreLogChatEvent preEvent = new PreLogChatEvent(event, message);

        if (message.startsWith("/") || !Config.getConfig(player.getWorld()).PLAYER_MESSAGES) {
            preEvent.setCancelled(true);
        }

        if (!preEvent.callEvent() || (preEvent.isChatCancelled() && !Config.getConfig(player.getWorld()).LOG_CANCELLED_CHAT)) {
            return;
        }

        long timestamp = System.currentTimeMillis() / 1000L;
        Queue.queuePlayerChat(player, preEvent.getMessageToLog(), timestamp, preEvent.isChatCancelled());
    }
}
