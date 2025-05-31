package net.coreprotect.utility.serialize;

import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;

public class Bytes {
    public static String toBlobString(byte[] array) {
        if (array == null || array.length == 0) {
            return null;
        }

        return new String(array, StandardCharsets.ISO_8859_1);
    }

    public static byte @Nullable [] fromBlobString(String blob) {
        if (blob == null || blob.isEmpty() || "0".equals(blob)) {
            return null;
        }

        return blob.getBytes(StandardCharsets.ISO_8859_1);
    }
}
