package org.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static org.example.Utils.isNumericVersion;


public class Cleaner {

    private final String BASE_DIRECTORY;
    private final Comparator<File> COMPARATOR;

    private static final String[] FILES_INSIDE_VERSION_FOLDER = {
            "_remote.repositories",
            ".pom",
            ".pom.sha1",
            ".jar"
    };

    public Cleaner(String BASE_DIRECTORY, int comparatorIndex,boolean reversed) {
        this.BASE_DIRECTORY = BASE_DIRECTORY;

        this.COMPARATOR = CleanMethod.methodFrom(comparatorIndex)
                .getComparator(reversed);
    }


    public void clean(){
        var baseFile = new File(BASE_DIRECTORY);
        traverse(baseFile);
    }
    private boolean traverse(File dir){

        File[] inFiles = dir.listFiles();
        boolean isVersionsFolder = false;

        if (inFiles == null || inFiles.length == 0) {
            return false;
        }

        for(File file : inFiles){
            if(file.isDirectory()){
                // If it returns true it means we are in the versions folder
                if(traverse(file)) {
                    isVersionsFolder = true;
                    break;
                }
            }
            if (Arrays.stream(FILES_INSIDE_VERSION_FOLDER)
                    .anyMatch(file.getName()::endsWith)
            ){
                return true;
            }
        }
        // If we are in a versions folder
        if (isVersionsFolder){

            Queue<File> versionFiles = Arrays.stream(inFiles)
                    .filter(e -> isNumericVersion(e.getName()))
                    .collect(Collectors.toCollection(() -> new PriorityQueue<>(COMPARATOR)));

            // Only the one with the highest priority remains
            var survivor = versionFiles.poll();

            while(!versionFiles.isEmpty()){
                var fileToDelete = versionFiles.poll();
                if (!deleteDirectory(fileToDelete)){
                    System.err.println("Failed to delete: " + fileToDelete.getPath());
                }
            }
        }

        return false;
    }

    /**
     * Recursively all files inside the given directory and the directory
     * itself
     * @param directory
     */
    private boolean deleteDirectory(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("Error: Directory does not exist or is not a directory.");
            return false;
        }

        try (var paths = Files.walk(directory.toPath())) {
            // Sort paths in reverse order for safe deletion
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path); // Delete the file or directory
                        } catch (IOException e) {
                            System.err.println("Failed to delete " + path + ": " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error while deleting directory: " + e.getMessage());
            return false;
        }

        return true; // Directory deleted successfully
    }

}
