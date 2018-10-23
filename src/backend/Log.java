package backend;

import java.io.*;
import java.util.ArrayList;
import java.sql.Timestamp;
import java.util.regex.Pattern;

/**
 * A Log to keep track of changes made to Pictures
 */
public class Log {
    /**
     * An ArrayList to store all the changes made to Pictures
     */
    private ArrayList<String> logs = new ArrayList<>();

    /**
     * The directory to the .ser file for Log including file name and extension
     */
    private String pathName;

    /**
     * Constructs a new Log object
     *
     * @param pathName the relative directory to the .ser file for this Log
     * @throws ClassNotFoundException Thrown when the class is not found
     * @throws IOException            Thrown when the file is not found
     */
    public Log(String pathName) throws ClassNotFoundException, IOException {
        this.pathName = pathName;
        boolean initializeResult = SerializableOperator.initializeFile(this.pathName);
        if (initializeResult) {
            this.logs = (ArrayList<String>) SerializableOperator.readFile(pathName);
        } else {
            SerializableOperator.createFile(pathName, false);
        }
        SerializableOperator.saveFile(this.pathName, this.logs);
    }

    /**
     * Adds a new change into the log in the following format:
     * prevName -> newName timestamp
     * and saves the new change into the serializable file.
     *
     * @param prevName The previous name of the file
     * @param newName  The current name of the file
     */
    public void addChange(String prevName, String newName) throws IOException {
        logs.add(prevName + " -> " + newName + " " + new Timestamp(System.currentTimeMillis()));
        SerializableOperator.saveFile(this.pathName, this.logs);
    }

    /**
     * Adds a new log entry in the following format:
     * Added tag(s): (old name) ---> (new name) (timestamp)
     *
     * @param dataPath Data path of the picture that has changed
     * @param newName  The new file name of the picture
     */
    public void logAddedTag(String dataPath, String newName) {
        String oldName = extractFileName(dataPath);
        logs.add("Added tag(s): " + oldName + " ---> " + newName + " " +
                new Timestamp(System.currentTimeMillis()));
        SerializableOperator.saveFile(this.pathName, this.logs);
    }

    /**
     * Adds a new log entry in the following format:
     * Removed tag(s): (old name) ---> (new name) (timestamp)
     *
     * @param dataPath Data path of the picture that has changed
     * @param newName  The new file name of the picture
     */
    public void logRemovedTag(String dataPath, String newName) {
        String oldName = extractFileName(dataPath);
        logs.add("Removed tag(s): " + oldName + " ---> " + newName + " " +
                new Timestamp(System.currentTimeMillis()));
        SerializableOperator.saveFile(this.pathName, this.logs);
    }

    /**
     * Adds a new log entry in the following format:
     * Name of picture reverted: (old name) ---> (new name) (timestamp)
     *
     * @param dataPath Data path of the picture that has changed
     * @param newName  The file name changed to
     */
    public void logChangedName(String dataPath, String newName) {
        String oldName = extractFileName(dataPath);
        logs.add("Name of picture reverted: " + oldName + " ---> " + newName + " " +
                new Timestamp(System.currentTimeMillis()));
        SerializableOperator.saveFile(this.pathName, this.logs);
    }

    /**
     * A getter for logs
     *
     * @return the contents of log in an ArrayList
     */
    public ArrayList<String> getLogList() {
        return this.logs;
    }

    /**
     * Returns the file name of a file give it's full directory
     *
     * @param dataPath The full directory of a file to extract file name from
     * @return Returns the file name
     */
    public String extractFileName(String dataPath) {
        String pathSplitRegex = Pattern.quote(File.separator); //escapes any special character in File.separator
        //This was adapted from a post by Jon Skeet on 20120426 to a stackoverflow forum here:
        //https://stackoverflow.com/questions/10336293/splitting-filenames-using-system-file-separator-symbol
        String[] dataPathComponents = dataPath.split(pathSplitRegex);
        return dataPathComponents[dataPathComponents.length - 1];
    }
}
