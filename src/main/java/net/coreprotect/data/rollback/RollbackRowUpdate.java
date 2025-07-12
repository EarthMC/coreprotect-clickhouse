package net.coreprotect.data.rollback;

import net.coreprotect.data.lookup.type.CommonLookupData;

import java.util.ArrayList;
import java.util.List;

/**
 * Passed to the consumer to indicate that this row needs it's rolled_back updated.
 * @param rowId The rowid value, the table is passed separately.
 * @param time The time of the row, used to increase lookup speed.
 * @param user The user of the row, used to increase lookup speed.
 * @param worldId Used to increase lookup speed.
 * @param x Used to increase lookup speed.
 * @param z Used to increase lookup speed.
 * @param rolledBack The current rolled_back state.
 */
public record RollbackRowUpdate(long rowId, long time, int user, int worldId, int x, int z, int rolledBack) {
    public static List<RollbackRowUpdate> fromResultData(List<CommonLookupData> commonData) {
        final List<RollbackRowUpdate> updatelist = new ArrayList<>();

        for (final CommonLookupData row : commonData) {
            updatelist.add(readRow(row));
        }

        return updatelist;
    }

    public static RollbackRowUpdate readRow(CommonLookupData row) {
        return new RollbackRowUpdate(row.rowId(), row.time(), row.userId(), row.worldId(), row.x(), row.z(), row.rolledBack());
    }
}
