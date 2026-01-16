package net.coreprotect.utility.serialize;

import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class ExtraNBTUtils {
    public static boolean isEmpty(final Tag tag) {
        return isEmpty(tag, 0);
    }

    private static boolean isEmpty(final Tag tag, int depth) {
        if (depth > 127) {
            return false;
        }

        return switch (tag) {
            case CollectionTag collectionTag -> {
                for (final Tag element : collectionTag) {
                    if (!isEmpty(element, depth + 1)) {
                        yield false;
                    }
                }

                yield true;
            }
            case CompoundTag compoundTag -> {
                for (final Tag element : compoundTag.values()) {
                    if (!isEmpty(element, depth + 1)) {
                        yield false;
                    }
                }

                yield true;
            }
            default -> false;
        };
    }
}
