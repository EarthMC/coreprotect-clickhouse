package net.coreprotect.data.lookup;

import java.util.List;

public abstract class LookupResult<T> {
    private final long count;
    private final List<T> data;

    protected LookupResult(long count, List<T> data) {
        this.count = count;
        this.data = data;
    }

    /**
     * @return The total amount of results for the query, or -1 if rows weren't counted for the query.
     */
    public long totalResultSize() {
        return this.count;
    }

    public List<T> data() {
        return this.data;
    }
}
