package net.coreprotect.data.lookup.result;

import net.coreprotect.data.lookup.LookupResult;
import net.coreprotect.data.lookup.type.InventoryLookupData;

import java.util.List;

public class InventoryLookupResult extends LookupResult<InventoryLookupData> {
    public InventoryLookupResult(long count, List<InventoryLookupData> data) {
        super(count, data);
    }
}
