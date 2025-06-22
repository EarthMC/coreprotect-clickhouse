package net.coreprotect.database.convert.process;

public class CorruptResultRowException extends RuntimeException {
    private final long rowNumber;

    public CorruptResultRowException(long rowNumber) {
        super("Encountered a corrupt row in result set at row " + rowNumber);
        this.rowNumber = rowNumber;
    }

    public long getRowNumber() {
        return rowNumber;
    }
}
