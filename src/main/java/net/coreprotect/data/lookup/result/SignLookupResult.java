package net.coreprotect.data.lookup.result;

import net.coreprotect.data.lookup.LookupResult;
import net.coreprotect.data.lookup.type.SignLookupData;

import java.util.List;

public class SignLookupResult extends LookupResult<SignLookupData> {
    public SignLookupResult(long count, List<SignLookupData> data) {
        super(count, data);
    }
}
