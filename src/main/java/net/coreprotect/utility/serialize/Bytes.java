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

    public static String toHexString(byte[] array) {
        if (array == null || array.length == 0) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(String.format("%02X", b));
        }

        return sb.toString();
    }

    public static byte @Nullable [] fromHexString(String blob) {
        if (blob == null || blob.isEmpty() || "0".equals(blob)) {
            return null;
        }

        final byte[] array = new byte[blob.length() / 2]; // one byte is represented by 2 chars

        for (int i = 0; i < blob.length(); i += 2) {
            array[i / 2] = (byte) ((Character.digit(blob.charAt(i), 16) << 4) + Character.digit(blob.charAt(i + 1), 16));
        }

        return array;
    }
}
