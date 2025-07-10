package net.coreprotect.data.lookup.type;

import net.coreprotect.database.lookup.PlayerLookup;

public record ChatLookupData(
        long rowId,
        long time,
        int userId,
        String message,
        boolean cancelled,
        int worldId,
        int x,
        int y,
        int z
) {
    public String playerName() {
        return PlayerLookup.playerName(this.userId);
    }
}
