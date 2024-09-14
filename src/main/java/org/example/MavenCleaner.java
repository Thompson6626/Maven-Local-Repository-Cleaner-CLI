package org.example;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Callable;

@Command(name = "Maven Cleaner", mixinStandardHelpOptions = true, version = "1.0",
        description = "A CLI tool to clean the local repository.",
        subcommands = {MavenCleaner.SetDirCommand.class, MavenCleaner.CleanCommand.class})
public class MavenCleaner implements Callable<Integer> {

    private static final String CONFIG_DIR = System.getProperty("user.home") + "/asdasdqasd";
    private static final String CONFIG_FILE = CONFIG_DIR + "/cleaner.properties";
    private static final String DIR_KEY = "directory";
    private String directory;

    public static void main(String[] args) {
        var cli = new MavenCleaner();
        cli.loadConfig(); // Load config on startup
        int exitCode = new CommandLine(cli).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        // Default action: display the current directory or other default behavior
        System.out.println("Current directory: " + directory);
        return 0;
    }

    private void loadConfig() {
        createIfNotExist(CONFIG_DIR);

        try (var in = new FileInputStream(CONFIG_FILE)) {
            var prop = new Properties();
            prop.load(in);
            directory = prop.getProperty(DIR_KEY, null); // Null if not set
        } catch (IOException e) {
            System.err.println("Error loading config: " + e.getMessage());
            directory = null;
        }
    }

    private void saveConfig() {
        createIfNotExist(CONFIG_DIR);
        // Save the configuration properties
        try (var out = new FileOutputStream(CONFIG_FILE)) {
            var prop = new Properties();
            prop.setProperty(DIR_KEY, directory);
            prop.store(out, null);

        } catch (IOException e) {
            System.err.println("Error saving config: " + e.getMessage());
        }
    }
    private void createIfNotExist(String path){
        try{
            // Ensure the asdasdqasd directory exists
            var dir = new File(path);

            if (!dir.exists()) dir.mkdirs(); // Create the directory if it doesn't exist
        }catch (SecurityException e){
            System.err.println("Error creating directories: " + e.getMessage());
        }
    }


    @Command(name = "set-dir", description = "Set the local repository directory")
    class SetDirCommand implements Callable<Integer> {
        @Option(names = {"-d", "--directory"}, required = true, description = "The new local repository path")
        private String newDirectory;

        @Override
        public Integer call() {
            directory = newDirectory;
            saveConfig();  // Save the new directory to the config file
            System.out.println("Directory set to: " + directory);
            return 0;
        }

    }

    @Command(name = "clean", description = "Cleans the local repository.")
    class CleanCommand implements Callable<Integer> {

        @Option(names = {"-clm", "--cleanmethod"}, description = "The clean method", defaultValue = "1")
        private int cleanMethod;

        @Override
        public Integer call(){
            if (directory == null) {
                System.err.println("Error: Directory not set. Use 'set-dir' to set the directory.");
                return 1;
            }
            if(cleanMethod < 1 || cleanMethod > 3){
                System.err.println("Error: Clean method index not valid");
                return 1;
            }
            if(!new File(directory).isDirectory()){
                System.err.println("Error: Repository path is not a directory");
                return 1;
            }
            // Implement cleaning logic
            var cleaner = new Cleaner(directory, cleanMethod);

            return cleaner.clean();
        }
    }
}

