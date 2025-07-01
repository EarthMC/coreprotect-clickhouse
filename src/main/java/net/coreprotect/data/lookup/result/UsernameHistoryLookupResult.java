package net.coreprotect.data.lookup.result;

import net.coreprotect.data.lookup.LookupResult;
import net.coreprotect.data.lookup.type.UsernameHistoryData;

import java.util.List;

public class UsernameHistoryLookupResult extends LookupResult<UsernameHistoryData> {
    public UsernameHistoryLookupResult(long count, List<UsernameHistoryData> data) {
        super(count, data);
    }
}
