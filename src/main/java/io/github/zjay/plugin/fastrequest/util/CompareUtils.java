package io.github.zjay.plugin.fastrequest.util;

public class CompareUtils {

    public static <T extends Comparable<? super T>> int compare(T c1, T c2, boolean isNullGreater) {
        if (c1 == c2) {
            return 0;
        } else if (c1 == null) {
            return isNullGreater ? 1 : -1;
        } else if (c2 == null) {
            return isNullGreater ? -1 : 1;
        }
        return c1.compareTo(c2);
    }
}
