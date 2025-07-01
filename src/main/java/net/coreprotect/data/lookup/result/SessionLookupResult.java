package net.coreprotect.data.lookup.result;

import net.coreprotect.data.lookup.LookupResult;
import net.coreprotect.data.lookup.type.SessionLookupData;

import java.util.List;

public class SessionLookupResult extends LookupResult<SessionLookupData> {
    public SessionLookupResult(long count, List<SessionLookupData> data) {
        super(count, data);
    }
}
