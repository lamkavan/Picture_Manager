package backend;

import java.io.*;
import java.util.ArrayList;

/**
 * A tool called SerializableOperator that writes, read and initializes .ser files
 */
public class SerializableOperator {

    /**
     * Checks if serializable file exists. If it does, it returns true. If it does not, return false
     *
     * @param pathName the relative directory to the .ser file
     * @return true if the file exist and false otherwise
     */
    public static boolean initializeFile(String pathName) {
        File file = new File(pathName);
        // returns true if the file we are interested in exist and false otherwise
        return file.exists();
    }

    /**
     * Creates a new file and deletes a old one if needed
     *
     * @param pathName  the relative directory to the .ser file
     * @param deleteOld a boolean to determine when to delete a old file
     */
    public static void createFile(String pathName, boolean deleteOld) {
        File file = new File(pathName);
        // when deleteOld is true this means we must delete the old file before making a new one
        try {
            if (deleteOld) {
                file.delete();
                file.createNewFile();
            } else {
                file.createNewFile();
            }
        } catch (IOException e1) {
            System.out.println("Unable to create a new file at specified directory");
        }
    }

    /**
     * Reads the serializable file and returns the deserialize data . If it
     * cannot read it, it deletes the serializable file and crates a new one.
     * Then returns an empty ArrayList.
     * <p>
     * This code was inspired from the serializable demo in our CSC207 lectures
     *
     * @param pathName the relative directory to the .ser file
     * @return an ArrayList of data from the serializable file
     */
    public static ArrayList<?> readFile(String pathName) {
        try {
            InputStream file = new FileInputStream(pathName);
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer);
            ArrayList<?> deserialize_data = (ArrayList<?>) input.readObject();
            input.close();
            // return the deserialize data
            return deserialize_data;
        } catch (ClassNotFoundException e2) {
            throw new RuntimeException("Could not find the Java Class required.");
        } catch (IOException e1) {
            System.out.println("Cannot read from input.");
            // if we cannot read the file, delete the file and create a new one.
            SerializableOperator.createFile(pathName, true);
            return new ArrayList();
        }
    }

    /**
     * Saves an ArrayList object, objectToSerialize, into a serializable file
     *
     * @param pathName          the relative directory to the .ser file
     * @param objectToSerialize the object to serialize
     */
    public static void saveFile(String pathName, ArrayList<?> objectToSerialize) {
        try {
            OutputStream file = new FileOutputStream(pathName);
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput output = new ObjectOutputStream(buffer);

            // serialize the object
            output.writeObject(objectToSerialize);
            output.close();
        } catch (FileNotFoundException e1) {
            System.out.println("Unable to save file since the file does not exist, check pathname");
        } catch (IOException e2) {
            System.out.println("Unable to save file, check pathname");
        }
    }
}
