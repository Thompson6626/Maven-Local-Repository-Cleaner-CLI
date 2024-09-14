package org.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.function.Function;

public enum CleanMethod {
    HIGHER_VERSION((a,b)->{
        String name1 = a.getName();
        String name2 = b.getName();

        String[] aa = name1.split("\\.");
        String[] bb = name2.split("\\.");
        int r;
        for (int i = 0, len = Math.min(aa.length,bb.length); i < len; i++) {
            r = Integer.compare(Integer.parseInt(aa[i]),Integer.parseInt(bb[i]));
            if (r != 0) return r;
        }
        return Integer.compare(aa.length, bb.length);
    }),
    LAST_ACCESSED((a,b) -> {
        try {
            return getLastAccessedTime(a).compareTo(getLastAccessedTime(b));
        } catch (IOException e) {
            System.err.println("Error getting last accessed time.");
            return 0;
        }
    }),
    LAST_MODIFIED((a,b)->{
        try {
            return getLastModifiedDate(a).compareTo(getLastModifiedDate(b));
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

    public Comparator<File> getComparator() {
        return comparator;
    }

    private static FileTime getLastAccessedTime(File file) throws IOException {
        return getProperty(file, BasicFileAttributes::lastAccessTime);
    }
    private static FileTime getLastModifiedDate(File file) throws IOException {
        return getProperty(file, BasicFileAttributes::lastModifiedTime);
    }
    private static <T> T getProperty(
            File file ,
            Function<BasicFileAttributes,T> attributesResolver
    ) throws IOException {
        Path filePath = file.toPath();
        BasicFileAttributes attributes = Files.readAttributes(filePath, BasicFileAttributes.class);

        return attributesResolver.apply(attributes);
    }

    public static CleanMethod toMethod(int n){
        if(n < 1 || n > 3){
            throw new IllegalStateException("Unexpected value: " + n);
        }
        return METHODS[n - 1];
    }



}
