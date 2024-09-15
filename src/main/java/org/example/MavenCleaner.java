package org.example;

import picocli.CommandLine;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;


@Command(name = "Maven Cleaner", mixinStandardHelpOptions = true, version = "1.0",
        description = "A CLI tool to clean the maven local repository.",
        subcommands = CommandLine.HelpCommand.class
)
public class MavenCleaner{
    private static final String PROPERTIES_FOLDER = "MavenCleaner";
    private static final String PROPERTIES_NAME = "cleaner.properties";
    private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + PROPERTIES_FOLDER;
    private static final String CONFIG_FILE = CONFIG_DIR + File.separator + PROPERTIES_NAME;
    private static final String DIR_KEY = "DIRECTORY";
    private static final String DEFAULT_CLEANING_MODE_KEY = "DEFAULT_CLEANING_MODE";
    private String directory;
    private Integer cleaningMode;

    public static void main(String[] args) {
        var cli = new MavenCleaner();
        // Save default cleaning mode to the properties file
        cli.saveConfig(DEFAULT_CLEANING_MODE_KEY, "1");
        cli.loadConfig(); // Load config on startup
        int exitCode = new CommandLine(cli).execute(args);
        System.exit(exitCode);
    }

    private void loadConfig() {
        createIfNotExist(CONFIG_DIR);

        try (var in = new FileInputStream(CONFIG_FILE)) {
            var prop = new Properties();
            prop.load(in);
            directory = prop.getProperty(DIR_KEY, null); // Nu

            String mode = prop.getProperty(DEFAULT_CLEANING_MODE_KEY,  null);
            cleaningMode = mode != null ? Integer.parseInt(mode) : null;
        } catch (IOException e) {
            System.err.println("Error loading config: " + e.getMessage());
            directory = null;
            cleaningMode = null;
        }
    }
    private void saveConfig(String key, String value) {
        createIfNotExist(CONFIG_DIR);

        // Load the existing configuration properties
        var prop = new Properties();
        if (new File(CONFIG_FILE).exists()) {
            try (var in = new FileInputStream(CONFIG_FILE)) {
                prop.load(in); // Load the existing properties
            } catch (IOException e) {
                System.err.println("Error loading config: " + e.getMessage());
            }
        }
        // Add or update the key-value pair
        prop.setProperty(key, value);  // Adds if key doesn't exist, updates if it does
        // Save the updated configuration properties
        try (var out = new FileOutputStream(CONFIG_FILE)) {
            prop.store(out, null); // Save the properties back to the file
        } catch (IOException e) {
            System.err.println("Error saving config: " + e.getMessage());
        }
    }

    private void createIfNotExist(String path){
        try{
            var dir = new File(path);

            if (!dir.exists()) dir.mkdirs(); // Create the directory if it doesn't exist

            var propertiesFile = new File(dir, PROPERTIES_NAME);
            propertiesFile.createNewFile();
        }catch (SecurityException e){
            System.err.println("Error creating directories: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Command(name = "set-dir", description = "Set the local repository directory")
    void setLocalDirectory(
            @Parameters(arity = "1", index = "0", description = "The new local repository path") String newDirectory
    ){
        directory = newDirectory;

        saveConfig(DIR_KEY, directory);  // Save the new directory to the config file
        System.out.println("Directory set to: " + directory);
    }

    @Command(name = "def-mode", description = "Sets the default cleaning mode")
    void setDefaultCleaningMode(
            @Parameters(arity = "1", index = "0", description = "The new default cleaning mode") int cleanMode
    ){
        cleaningMode = cleanMode;
        saveConfig(DEFAULT_CLEANING_MODE_KEY, String.valueOf(cleaningMode));
        System.out.println("Default cleaning mode set to: " + cleanMode);
    }

    @Command(name = "clean", description = "Starts cleaning the local repository.")
    void cleanRepository(
            @Option(
                    names = {"-clm", "--cleanmode"},
                    description = """
                            Selecting the cleaning mode:
                            1 -> Higher version will remain
                            2 -> The most recently version accessed will remain
                            3 -> The most recently modified version will remain
                            """
            ) Integer cleanMode,
            @Option(
                    names = {"-r", "--reversed"},
                    description = "Reverses the comparator of the cleaning mode",
                    defaultValue = "False"
            ) boolean reversed
            ){
        if(cleanMode == null){
            cleanMode = cleaningMode;
        }
        if (directory == null) {
            System.err.println("Error: Directory not set. Use 'set-dir' to set the directory.");
            return;
        }

        if(cleanMode < 1 || cleanMode > CleanMethod.values().length){
            System.err.println("Error: Clean method index not valid");
            return;
        }

        if(!new File(directory).isDirectory()){
            System.err.println("Error: Repository path is not a directory");
            return;
        }
        // Create cleaner
        var cleaner = new Cleaner(directory, cleanMode, reversed);

        Thread spinner = startSpinner();

        try {
            cleaner.clean();  // Perform the cleaning
        } finally {
            stopSpinner(spinner);  // Ensure the spinner stops even if an error occurs
        }

        System.out.println("\nCleaning completed successfully!");
    }

    private Thread startSpinner() {
        Thread spinner = new Thread(() -> {
            String[] spinnerChars = {"|", "/", "-", "\\"};  // Spinner animation frames
            int index = 0;
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    System.out.print("\rCleaning in progress... " + spinnerChars[index++ % spinnerChars.length]);
                    Thread.sleep(100);  // Adjust this for animation speed
                }
            } catch (InterruptedException e) {
                // Thread interrupted, stop the spinner
                Thread.currentThread().interrupt();
            }
        });

        spinner.start();  // Start the spinner thread
        return spinner;
    }

    private void stopSpinner(Thread spinner) {
        spinner.interrupt();  // Interrupt the spinner thread to stop the animation
        try {
            spinner.join();  // Wait for the spinner thread to finish
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.print("\rCleaning completed.");  // Clear the spinner line
    }
}

