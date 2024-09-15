package org.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.function.Function;
import java.util.regex.Pattern;

public class Utils {

    public static FileTime getLastAccessedTime(File file) throws IOException {
        return getProperty(file, BasicFileAttributes::lastAccessTime);
    }
    public static FileTime getLastModifiedDate(File file) throws IOException {
        return getProperty(file, BasicFileAttributes::lastModifiedTime);
    }
    public static <T> T getProperty(
            File file ,
            Function<BasicFileAttributes,T> attributesResolver
    ) throws IOException {
        Path filePath = file.toPath();
        BasicFileAttributes attributes = Files.readAttributes(filePath, BasicFileAttributes.class);

        return attributesResolver.apply(attributes);
    }

    private static final Pattern NUMERIC_VERSION_PATTERN = Pattern.compile("^\\d+(\\.\\d+)*$");

    public static boolean isNumericVersion(String version) {
        return NUMERIC_VERSION_PATTERN.matcher(version).matches();
    }


}
