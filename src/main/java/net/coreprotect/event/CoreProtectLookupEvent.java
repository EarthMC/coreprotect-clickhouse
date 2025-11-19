package net.coreprotect.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CoreProtectLookupEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private final CommandSender commandSender;
    private List<String> users;
    private String cancelMessage = "This lookup was cancelled by another plugin.";

    public CoreProtectLookupEvent(CommandSender commandSender, List<String> users) {
        this.commandSender = commandSender;
        this.users = users;
    }

    public CommandSender getSender() {
        return commandSender;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        if (users.isEmpty()) {
            setCancelled(true);
            setCancelMessage("Users list was modified by another plugin. No users remaining.");
            return;
        }
        this.users = users;
    }

    public void setCancelMessage(String cancelMessage) {
        this.cancelMessage = cancelMessage;
    }

    public String getCancelMessage() {
        return cancelMessage;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
