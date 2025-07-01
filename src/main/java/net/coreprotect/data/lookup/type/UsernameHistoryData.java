package net.coreprotect.data.lookup.type;

public record UsernameHistoryData(
        long rowId,
        long time,
        String uuid,
        String username
) {
}
