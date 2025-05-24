package net.coreprotect.utility.serialize;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Bytes {
    public static String toBlobString(byte[] array) {
        if (array == null || array.length == 0) {
            return null;
        }

        final List<String> list = new ArrayList<>();

        for (final byte b : array) {
            list.add(String.valueOf(b));
        }

        return String.join(",", list);
    }

    public static byte @Nullable [] fromBlobString(String blob) {
        if (blob == null || blob.isEmpty()) {
            return null;
        }

        final int size = Math.toIntExact(blob.codePoints().filter(ch -> ch == ',').count() + 1);
        final byte[] array = new byte[size];

        final String[] parts = blob.split(",");

        for (int i = 0; i < parts.length; i++) {
            array[i] = Byte.parseByte(parts[i]);
        }

        return array;
    }
}
