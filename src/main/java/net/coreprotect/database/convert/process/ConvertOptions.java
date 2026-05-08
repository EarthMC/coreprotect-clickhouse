package net.coreprotect.database.convert.process;

public record ConvertOptions(boolean truncate, long offset, long chunkSize, long chunkStart, int workers) {
    public static final long DEFAULT_CHUNK_SIZE = 1_000_000L;
    public static final int DEFAULT_WORKERS = 1;
    public static final long UNBOUNDED_CHUNK_SIZE = 0L;
    public static final long NO_CHUNK_START = -1L;

    public ConvertOptions(boolean truncate, long offset) {
        this(truncate, offset, UNBOUNDED_CHUNK_SIZE, NO_CHUNK_START, DEFAULT_WORKERS);
    }

    public ConvertOptions {
        if (offset < 0) {
            throw new IllegalArgumentException("offset cannot be negative");
        }

        if (chunkSize < 0) {
            throw new IllegalArgumentException("chunk size cannot be negative");
        }

        if (chunkStart < NO_CHUNK_START) {
            throw new IllegalArgumentException("chunk start cannot be less than -1");
        }

        if (workers < 1) {
            throw new IllegalArgumentException("workers cannot be less than 1");
        }
    }

    public boolean chunked() {
        return chunkSize > 0;
    }

    public boolean singleChunk() {
        return chunkStart >= 0;
    }

    public long sourceOffset() {
        return offset + Math.max(chunkStart, 0L);
    }

    public long chunkEndExclusive() {
        if (!singleChunk() || !chunked()) {
            return -1L;
        }

        return chunkStart + chunkSize;
    }

    public ConvertOptions withChunk(long chunkStart) {
        return new ConvertOptions(false, offset, chunkSize, chunkStart, workers);
    }

    public ConvertOptions withChunkSize(long chunkSize) {
        return new ConvertOptions(truncate, offset, chunkSize, chunkStart, workers);
    }

    public ConvertOptions withWorkers(int workers) {
        return new ConvertOptions(truncate, offset, chunkSize, chunkStart, workers);
    }

    public ConvertOptions withoutTruncate() {
        return new ConvertOptions(false, offset, chunkSize, chunkStart, workers);
    }
}
