package org.example;

import java.io.File;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class Cleaner {

    private final String BASE_DIRECTORY;
    private final Comparator<File> COMPARATOR;


    public Cleaner(String BASE_DIRECTORY, int comparatorIndex) {
        this.BASE_DIRECTORY = BASE_DIRECTORY;
        this.COMPARATOR = CleanMethod.toMethod(comparatorIndex).getComparator();
    }


    public int clean(){
        // Check for inner files it they have a jar then they are it is the folder version
        var baseFile = new File(BASE_DIRECTORY);

        // Folder -> Some other folders... ->
        traverse(baseFile);

        return 0;
    }
    private void traverse(File dir){

        File[] inFiles = dir.listFiles();

        for(File file : inFiles){
            if(dir.isDirectory()){
                traverse(dir);
            }
            if(file.getName().contains(".jar")){

            }
        }

    }






}
