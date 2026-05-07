package net.coreprotect.api;

import java.io.Serializable;

/**
 * Wrapper class for storing custom block data from BlockDataProviders.
 */
public class BlockDataProviderData implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String MARKER = "__COREPROTECT_PROVIDER_DATA__";

    private final byte[] data;

    public BlockDataProviderData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
}
