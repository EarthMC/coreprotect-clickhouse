package net.coreprotect.event.chat;

import com.google.common.base.Preconditions;
import io.papermc.paper.event.player.AbstractChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Called after a chat event is received and before it is queued to be logged, allowing for the resulting logged message to be modified.
 * <p>
 * This event is not called if player chat logging is not enabled.
 */
public class PreLogChatEvent extends Event implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final AbstractChatEvent event;
    private boolean chatCancelled;
    private String messageToLog;
    private boolean cancelled;

    @ApiStatus.Internal
    public PreLogChatEvent(AbstractChatEvent event, String messageToLog) {
        super(true);
        this.event = event;
        this.messageToLog = messageToLog;
        this.chatCancelled = event.isCancelled();
    }

    @NotNull
    public String getMessageToLog() {
        return messageToLog;
    }

    public void setMessageToLog(@NotNull String messageToLog) {
        Preconditions.checkArgument(messageToLog != null, "messageToLog may not be null");
        this.messageToLog = messageToLog;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Whether this message will be logged as cancelled or not, defaults to the cancelled state of the event. Using this method does not affect the cancellation state of the chat event, nor is the state of the event re-checked after.
     *
     * @return Whether this event is logged as a cancelled event or not.
     */
    public boolean isChatCancelled() {
        return this.chatCancelled;
    }

    public void setChatCancelled(boolean chatCancelled) {
        this.chatCancelled = chatCancelled;
    }

    @NotNull
    public AbstractChatEvent getChatEvent() {
        return this.event;
    }

    @NotNull
    public Player getPlayer() {
        return this.event.getPlayer();
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
