package net.coreprotect.data.lookup.result;

import net.coreprotect.data.lookup.LookupResult;
import net.coreprotect.data.lookup.type.ChatLookupData;

import java.util.List;

public class ChatLookupResult extends LookupResult<ChatLookupData> {
    public ChatLookupResult(long count, List<ChatLookupData> data) {
        super(count, data);
    }
}
