package backend;

import java.io.File;
import java.util.ArrayList;

/**
 * A file searcher that searches and finds picture files
 */
public class FileSearcher {
    /**
     * A list which contains file extensions that the program considers to be images
     */
    private ArrayList<String> imageFileTypes;

    /**
     * Initializes new FileSearcher object
     *
     * @param imageFileTypes List of file extensions that are considered to be pictures
     */
    public FileSearcher(ArrayList<String> imageFileTypes) {
        this.imageFileTypes = imageFileTypes;
    }

    /**
     * Returns an ArrayList of picture file names that are in and under the provided directory
     *
     * @param absoluteDir The absolute path for the directory
     * @return list of picture file names that are in and under the provided directory
     */
    public ArrayList<String> getFileNames(String absoluteDir) {
        File file = new File(absoluteDir);
        File[] files = file.listFiles();
        ArrayList<String> fileNames = new ArrayList<>();

        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    fileNames.addAll(getFileNames(f.getAbsolutePath()));
                } else if (this.isPicture(f)) {
                    fileNames.add(f.getAbsolutePath());
                }
            }
        }
        return fileNames;
    }

    /**
     * Returns true iff file is a picture
     *
     * @param file The file that is to be checked
     * @return Returns true iff file is a picture
     */
    private boolean isPicture(File file) {
        String fileType = getFileType(file);
        for (String type : imageFileTypes) {
            if (fileType.equals(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the file type of file
     * Inspired from: https://stackoverflow.com/questions/25298691/how-to-check-the-file-type-in-java
     *
     * @param file The file that is to be checked
     * @return Returns the file type of file
     */
    private String getFileType(File file) {
        String fileName = file.getName();
        int dot = fileName.lastIndexOf('.');
        return (dot == -1) ? "" : fileName.substring(dot + 1);
    }

}
