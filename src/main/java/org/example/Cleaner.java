package org.example;

import java.io.File;
import java.util.*;

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
            for(String inNames : FILES_INSIDE_VERSION_FOLDER){
                if(file.getName().endsWith(inNames)){
                    return true;
                }
            }
        }
        // If we are in a versions folder
        if (isVersionsFolder){

            Queue<File> pq = new PriorityQueue<>(COMPARATOR);

            Arrays.stream(inFiles)
                    .filter((e) -> isNumericVersion(e.getName()))
                    .forEach(pq::offer);

            // Only the one with the highest priority remains
            var survivor = pq.poll();

            while(!pq.isEmpty()){
                var delFile = pq.poll();
                boolean result = deleteDirectory(delFile);
                if (!result){
                    System.err.println("Failed to delete: " + delFile.getPath());
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
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if(file.isDirectory()) deleteDirectory(file);
                    else file.delete();
                }
            }
        }
        return directory.delete();
    }

}
