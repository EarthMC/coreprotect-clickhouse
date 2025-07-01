package net.coreprotect.data.lookup.result;

import net.coreprotect.data.lookup.LookupResult;
import net.coreprotect.data.lookup.type.CommonLookupData;

import java.util.List;

public class CommonLookupResult extends LookupResult<CommonLookupData> {
    public CommonLookupResult(long count, List<CommonLookupData> data) {
        super(count, data);
    }
}
