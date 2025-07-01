package net.coreprotect.data.lookup.type;

import net.coreprotect.database.lookup.PlayerLookup;

public record SignLookupData(
        long rowId,
        long time,
        int userId,
        int worldId,
        int x,
        int y,
        int z,
        String text
) {
    public String playerName() {
        return PlayerLookup.playerName(this.userId);
    }
}
