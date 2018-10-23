package backend;

import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;
import java.io.Serializable;

/**
 * A picture
 */
public class Picture implements Serializable {

    /**
     * The directory that Picture is in with file name and extension
     * ie/ C:/Users/Calvin/Documents/SomePicture @Person @Person2.jpeg (for windows)
     */
    private String dataPath;

    /**
     * ArrayList of all previous file names (tags and extension included) for this Picture.
     * ie/ [Calvin Lu.png, Calvin Lu @canProgram.png]
     */
    private ArrayList<String> prevFileNames = new ArrayList<>();

    /**
     * ArrayList of all tags currently attached to this Picture (all tags are unique)
     */
    private ArrayList<String> attachedTags = new ArrayList<>();

    /**
     * File name of the Picture object with tags and extension
     * ie/ bob @gg @ww @qq.jpeg
     */
    private String name;

    /**
     * The original file name without the tags (includes extension)
     */
    private String originalName;

    /**
     * Constructs a new Picture object.
     *
     * @param dataPath The data path that leads to the Picture file with file name and extension
     */
    public Picture(String dataPath) {
        this.dataPath = dataPath;
        this.name = extractName(dataPath);
        this.originalName = this.name;
    }

    /**
     * Getter for prevFileNames instance variable
     *
     * @return ArrayList of all previous file names this picture had (includes tags and extension)
     */
    public ArrayList<String> getPrevFileNames() {
        return this.prevFileNames;
    }

    /**
     * Getter for attachedTags instance variable
     *
     * @return ArrayList of tags currently attached to this Picture
     */
    public ArrayList<String> getAttachedTags() {
        return this.attachedTags;
    }

    /**
     * Getter for the directory instance variable
     *
     * @return Data path for the current directory
     */
    public String getDataPath() {
        return this.dataPath;
    }

    /**
     * Getter for the name instance variable
     *
     * @return name of picture file
     */
    public String getName() {
        return this.name;
    }

    /**
     * Getter for the originalName instance variable
     *
     * @return the contents of originalName
     */
    public String getOriginalName() {
        return this.originalName;
    }

    /**
     * Setter for the name instance variable
     *
     * @param newDataPath the new Picture file name along with the directory path
     */
    public void setName(String newDataPath) {
        this.name = this.extractName(newDataPath);
    }

    /**
     * Setter for the dataPath instance variable
     *
     * @param newDataPath the absolute directory to set to
     */
    public void setDataPath(String newDataPath) {
        this.dataPath = newDataPath;
    }

    /**
     * Adds a new previous file name to the prevFileNames instance variable
     *
     * @param newPrevFileName a new previous file name to add
     */
    public void addNewPrevFileName(String newPrevFileName) {
        this.prevFileNames.add(newPrevFileName);
    }

    /**
     * Deletes the most recent element in the prevFileNames instance variable
     */
    public void deletePrevFileName() {
        this.prevFileNames.remove(this.prevFileNames.size() - 1);
    }

    /**
     * Adds a new attached tag to the attachedTags instance variable
     *
     * @param newAttachedTag a new attached tag to add
     */
    public void addNewAttachedTag(String newAttachedTag) {
        this.attachedTags.add(newAttachedTag);
    }

    /**
     * Clears all the entries in the attachedTags instance variable (clean start)
     */
    public void clearAttachedTags() {
        this.attachedTags.clear();
    }

    /**
     * Removes a tag from the attachedTags instance variable
     *
     * @param tagToDelete a tag to delete
     */
    public void removeAttachedTag(String tagToDelete) {
        this.attachedTags.remove(tagToDelete);
    }

    /**
     * Returns the file name from a data path
     *
     * @param dataPath The data path that we want to extract the name from
     * @return Returns the file name from a directory
     */
    public String extractName(String dataPath) {
        String pathSplitRegex = Pattern.quote(File.separator); //escapes any special character in File.separator
        //This was adapted from a post by Jon Skeet on 20120426 to a stackoverflow forum here:
        //https://stackoverflow.com/questions/10336293/splitting-filenames-using-system-file-separator-symbol
        String[] dataPathComponents = dataPath.split(pathSplitRegex);
        return dataPathComponents[dataPathComponents.length - 1];
    }
}
