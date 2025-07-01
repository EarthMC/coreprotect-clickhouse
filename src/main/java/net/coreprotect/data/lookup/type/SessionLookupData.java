package net.coreprotect.data.lookup.type;

import net.coreprotect.database.lookup.PlayerLookup;

public record SessionLookupData(
        long rowId,
        long time,
        int userId,
        int worldId,
        int x,
        int y,
        int z,
        int action
) {
    public String playerName() {
        return PlayerLookup.playerName(this.userId);
    }
}
