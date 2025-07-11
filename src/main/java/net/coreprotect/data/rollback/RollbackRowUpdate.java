package net.coreprotect.data.rollback;

import net.coreprotect.data.lookup.type.CommonLookupData;

import java.util.ArrayList;
import java.util.List;

/**
 * Passed to the consumer to indicate that this row needs it's rolled_back updated.
 * @param rowId The rowid value, the table is passed separately.
 * @param rolledBack The current rolled_back state.
 */
public record RollbackRowUpdate(long rowId, int rolledBack) {
    public static List<RollbackRowUpdate> fromResultData(List<CommonLookupData> commonData) {
        final List<RollbackRowUpdate> updatelist = new ArrayList<>();

        for (final CommonLookupData row : commonData) {
            updatelist.add(new RollbackRowUpdate(row.rowId(), row.rolledBack()));
        }

        return updatelist;
    }
}
