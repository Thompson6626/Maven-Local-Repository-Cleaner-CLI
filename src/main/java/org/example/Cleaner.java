package org.example;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import static org.example.Utils.isNumericVersion;

public class Cleaner {

    private final String BASE_DIRECTORY;
    private final Comparator<File> COMPARATOR;

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

        assert inFiles != null;

        for(File file : inFiles){
            if(file.isDirectory()){
                if(traverse(file)) {
                    isVersionsFolder = true;
                    break;
                }
            }
            if(file.getName().endsWith(".jar")){
                return true;
            }
        }
        if (isVersionsFolder){
            Queue<File> pq = new PriorityQueue<>(COMPARATOR);

            Arrays.stream(inFiles)
                    .filter((e) -> isNumericVersion(e.getName()))
                    .forEach(pq::offer);

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
    private boolean deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        return directory.delete();
    }

}
