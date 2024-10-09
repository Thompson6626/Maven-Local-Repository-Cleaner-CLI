package org.example;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

import static org.example.Utils.getLastAccessedTime;
import static org.example.Utils.getLastModifiedDate;
public enum CleanMethod {
    HIGHER_VERSION((a, b) -> {
        String name1 = a.getName();
        String name2 = b.getName();

        String[] aa = name1.split("\\.");
        String[] bb = name2.split("\\.");
        int r;
        for (int i = 0, len = Math.min(aa.length, bb.length); i < len; i++) {
            r = Integer.compare(Integer.parseInt(bb[i]), Integer.parseInt(aa[i]));
            if (r != 0) return r;
        }
        return Integer.compare(bb.length, aa.length);
    }),
    LAST_ACCESSED((a,b) -> {
        try {
            return getLastAccessedTime(b).compareTo(getLastAccessedTime(a));
        } catch (IOException e) {
            System.err.println("Error getting last accessed time.");
            return 0;
        }
    }),
    LAST_MODIFIED((a,b)->{
        try {
            return getLastModifiedDate(b).compareTo(getLastModifiedDate(a));
        } catch (IOException e){
            System.err.println("Error getting last modified time.");
            return 0;
        }
    });

    private static final CleanMethod[] METHODS = CleanMethod.values();
    private final Comparator<File> comparator;

    CleanMethod(Comparator<File> comparator) {
        this.comparator = comparator;
    }

    public Comparator<File> getComparator(boolean reversed) {
        return reversed ? comparator.reversed() : comparator;
    }

    public static CleanMethod methodFrom(int n){
        if(n < 1 || n > 3){
            throw new IllegalStateException("Unexpected value: " + n);
        }
        return METHODS[n - 1];
    }



}
