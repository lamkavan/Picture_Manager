package backend;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * A Manager to store and keep track of all Picture objects created
 */
public class PictureManager {

    /**
     * The current directory that the program is in
     */
    private String viewingDir;

    /**
     * The ArrayList of Picture objects in this directory and all sub-directories
     */
    private ArrayList<Picture> directoryPictures = new ArrayList<>();

    /**
     * The ArrayList of all Picture objects that have ever been opened by this program
     * and will be stored and saved to a serializable file
     */
    private ArrayList<Picture> allPictures = new ArrayList<>();

    /**
     * The file path to the .ser file for PictureManager
     */
    private String pathName;

    /**
     * The ArrayList that stores Picture objects hidden for the Controller.hidePicturesWithoutTag method
     */
    private ArrayList<Picture> hiddenPictures = new ArrayList<>();

    /**
     * Creates an instance of PictureManager
     *
     * @param currentDir       the current directory that we are in
     * @param pictureDataPaths the list of data paths of all pictures in this directory and all sub directories
     * @param pathName         the file path to the .ser file for this PictureManager
     * @throws ClassNotFoundException when a class is missing
     * @throws IOException            when the .ser file is not found
     */
    public PictureManager(String currentDir, ArrayList<String> pictureDataPaths, String pathName)
            throws ClassNotFoundException, IOException {
        this.viewingDir = currentDir;
        this.pathName = pathName;
        boolean initializeResult = SerializableOperator.initializeFile(this.pathName);
        if (initializeResult) {
            this.allPictures = (ArrayList<Picture>) SerializableOperator.readFile(pathName);
        } else {
            SerializableOperator.createFile(pathName, false);
        }
        updateAllPictures(pictureDataPaths);
        updateDirectoryPictures();
    }

    /**
     * Adds a tag to the specified Picture (if possible) and returns the new
     * data path of the Picture if successful and empty string otherwise
     *
     * @param dataPath the data path that we are looking for a picture at
     * @param tagToAdd the tag we will try to add to a Picture
     * @return new Picture data path if the tag was added to the Picture
     * otherwise return the empty string
     */
    public String addTagToPicture(String dataPath, String tagToAdd) throws IOException {
        Picture pictureOfInterest = this.getPictureAtDataPath(dataPath);

        // Do this if the picture already has the tag attached
        if (pictureOfInterest.getAttachedTags().contains(tagToAdd)) {
            return "";
        }

        //Do this if the picture does not currently already have the tag already attached
        // 1) Determine what the new data path will be like
        String oldDataPath = pictureOfInterest.getDataPath();
        String[] tempDataPath = (oldDataPath).split("\\.");
        String newDataPath = tempDataPath[0] + " " + tagToAdd + "." + tempDataPath[1];
        // The if statement makes sure no other picture has the same name
        if (this.getPictureAtDataPath(newDataPath) == null) {
            // 2) First update the prevFileNames list
            String tempPrevFile = pictureOfInterest.getName();
            pictureOfInterest.addNewPrevFileName(tempPrevFile);

            // 3) Now update the attachedTags list
            pictureOfInterest.addNewAttachedTag(tagToAdd);

            // 4) Now update the dataPath instance variable
            pictureOfInterest.setDataPath(newDataPath);

            // 5) Now update the name instance variable
            pictureOfInterest.setName(pictureOfInterest.getDataPath());

            // 6) Now actually physically rename the file
            this.changeActualFileName(oldDataPath, pictureOfInterest.getDataPath());
            SerializableOperator.saveFile(this.pathName, this.allPictures);
            return pictureOfInterest.getDataPath();
        }
        return "";
    }

    /**
     * Remove the tag, tagToBeRemoved, from the Picture (if possible) at the specified data path and
     * updates all the Picture's instance variables and save the the changes
     * Line 129 was adapted from a post by Jon Skeet on 20120426 to a stackoverflow forum here:
     * https://stackoverflow.com/questions/10336293/splitting-filenames-using-system-file-separator-symbol
     *
     * @param dataPath       The absolute directory that the picture of interest is stored in
     * @param tagToBeRemoved The tag that we are looking to remove from the Picture object.
     * @return new Picture data path if the tag was added to the Picture
     * otherwise return the empty string
     * @throws IOException when the .ser file for this PictureManager is not found
     */
    public String removeTagFromPicture(String dataPath, String tagToBeRemoved) throws IOException {
        Picture pictureOfInterest = this.getPictureAtDataPath(dataPath);

        // 1) First check to see if the tag being removed is actually on the picture
        if (!(pictureOfInterest.getAttachedTags().contains(tagToBeRemoved))) {
            return "";
        }
        // 2) Now construct the new data path of the picture
        String oldDataPath = pictureOfInterest.getDataPath();
        String regex = Pattern.quote(System.getProperty("file.separator"));
        String[] dataPathComponents = oldDataPath.split(regex);
        String newDataPath = "";
        // 2.1) Construct the new data path not including file name, tag, and extension
        for (int index = 0; index < dataPathComponents.length - 1; index++) {
            newDataPath += dataPathComponents[index] + File.separator;
        }
        // 2.2) Add the file name
        newDataPath += (pictureOfInterest.getOriginalName().split("\\."))[0];
        // 2.3) Add the tags attached to the picture minus the one we want to remove
        for (String tag : pictureOfInterest.getAttachedTags()) {
            if (!(tag.equals(tagToBeRemoved))) {
                newDataPath += " " + tag;
            }
        }
        // 2.4) Now add the extension
        newDataPath += "." + (pictureOfInterest.getOriginalName().split("\\."))[1];

        // 3) Now check to see if another picture in the same directory has the same name
        // after the removal of the tag and act accordingly
        if (this.getPictureAtDataPath(newDataPath) == null) {
            // Since no other picture will have the same name we continue
            // 4) Update the prevFileNames instance variable
            String tempPrevFile = pictureOfInterest.getName();
            pictureOfInterest.addNewPrevFileName(tempPrevFile);
            // 5) Update the attachedTags instance variable
            pictureOfInterest.removeAttachedTag(tagToBeRemoved);
            // 6) Update the data path instance variable
            pictureOfInterest.setDataPath(newDataPath);
            // 7) Update the name instance variable
            pictureOfInterest.setName(pictureOfInterest.getDataPath());
            // 8) change the actual file name and save the changes by serializing
            this.changeActualFileName(oldDataPath, newDataPath);
            SerializableOperator.saveFile(this.pathName, this.allPictures);
            return pictureOfInterest.getDataPath();
        }
        // we can't remove the tag since it will cause two pictures to have the same name
        return "";
    }

    /**
     * Changes the name of a picture to a old one and changes all the Picture's instance variables
     * to account for the name change.
     * <p>
     * Line 180 was adapted from a post by Jon Skeet on 20120426 to a stackoverflow forum here:
     * https://stackoverflow.com/questions/10336293/splitting-filenames-using-system-file-separator-symbol
     *
     * @param dataPath The absolute directory that the picture of interest is stored in
     * @param newName  The new name to change to
     * @return Whether or not the name was successfully changed
     */
    public boolean changeFileName(String dataPath, String newName) {
        Picture pictureOfInterest = this.getPictureAtDataPath(dataPath);

        // 1) First construct the new data path that the picture will have
        String oldDataPath = pictureOfInterest.getDataPath();
        String regex = Pattern.quote(System.getProperty("file.separator"));
        String[] dataPathComponents = oldDataPath.split(regex);
        String newDataPath = "";
        // 1.1) Construct the new data path not including file name, tag, and extension
        for (int index = 0; index < dataPathComponents.length - 1; index++) {
            newDataPath += dataPathComponents[index] + File.separator;
        }
        // 1.2) Now add the file name with tags and extension
        newDataPath += newName;

        // 2) With the new data path check to see if another picture has the same file name
        if (this.getPictureAtDataPath(newDataPath) == null) {
            // Since no other picture will have the same name we continue
            // 4) Update the prevFileNames instance variable
            String tempPrevFile = pictureOfInterest.getName();
            pictureOfInterest.addNewPrevFileName(tempPrevFile);
            // 5) Update the attachedTags instance variable
            pictureOfInterest.clearAttachedTags();
            String ogName = pictureOfInterest.getOriginalName();
            if (!(ogName.equals(newName))) {
                String[] tagsInNewName = newName.split("\\.");
                tagsInNewName = tagsInNewName[0].split((ogName.split("\\."))[0]);
                tagsInNewName = tagsInNewName[1].split(" @");
                for (String tag : tagsInNewName) {
                    if (!(tag.equals(""))) {
                        tag = "@" + tag;
                        pictureOfInterest.addNewAttachedTag(tag);
                    }
                }
            }
            // 6) Update the data path instance variable
            pictureOfInterest.setDataPath(newDataPath);
            // 7) Update the name instance variable
            pictureOfInterest.setName(pictureOfInterest.getDataPath());
            // 8) change the actual file name and save the changes by serializing
            this.changeActualFileName(oldDataPath, newDataPath);
            SerializableOperator.saveFile(this.pathName, this.allPictures);
            return true;
        }

        // we can't rename the picture since another picture already has the same file name
        return false;
    }

    /**
     * Physically renames a file name for a picture with respect to
     * its full directory including the file name and extension
     * Inspired from: https://www.tutorialspoint.com/javaexamples/file_rename.htm
     *
     * @param oldPath the data path of the picture right now
     * @param newPath the data path of the picture we want it to have
     */
    public void changeActualFileName(String oldPath, String newPath) {
        File oldName = new File(oldPath);
        File newName = new File(newPath);
        oldName.renameTo(newName);
    }

    /**
     * Returns a Picture at specified dataPath
     *
     * @param dataPath the data path that we are looking for a picture at
     * @return Picture object with the specified data path, if it exists. Otherwise, return null.
     */
    public Picture getPictureAtDataPath(String dataPath) {
        for (int i = 0; i < this.getAllPictures().size(); i++) {
            if (this.getAllPictures().get(i).getDataPath().equals(dataPath)) {
                return this.getAllPictures().get(i);
            }
        }
        return null;
    }

    /**
     * Updates the allPictures instance variable by creating new Picture objects
     * if the Picture doesn't already exist in the allPictures ArrayList.
     *
     * @param dataPaths ArrayList<String> containing dataPaths of all Picture files in this directory (including
     *                  sub-directories)
     */
    public void updateAllPictures(ArrayList<String> dataPaths) throws IOException {
        HashMap<String, Picture> dataPathMap = new HashMap<String, Picture>();
        for (int i = 0; i < this.allPictures.size(); i++) {
            dataPathMap.put(this.allPictures.get(i).getDataPath(), this.allPictures.get(i));
        }
        for (int i = 0; i < dataPaths.size(); i++) {
            if (dataPathMap.get(dataPaths.get(i)) == null) {
                Picture newPicture = new Picture(dataPaths.get(i));
                this.allPictures.add(newPicture);
            }
        }
        SerializableOperator.saveFile(this.pathName, this.allPictures);
    }

    /**
     * Clears the directoryPictures instance variable and
     * creates a new ArrayList of Picture objects from pictures
     * that are in this directory and subsequent sub-directories
     */
    public void updateDirectoryPictures() {
        this.directoryPictures.clear();
        this.hiddenPictures.clear();
        String pathSplitRegex = Pattern.quote(File.separator);
        String[] viewingDirPieces = this.viewingDir.split(pathSplitRegex);
        String[] pictureDirPieces;
        boolean okToAdd;
        for (int i = 0; i < this.allPictures.size(); i++) {
            pictureDirPieces = this.allPictures.get(i).getDataPath().split(pathSplitRegex);
            okToAdd = true;
            for (int index=0; index < viewingDirPieces.length; index++){
                if (!(pictureDirPieces[index].equals(viewingDirPieces[index]))){
                    okToAdd = false;
                }
            }
            if (okToAdd){
                this.directoryPictures.add(this.allPictures.get(i));
            }
        }
    }

    /**
     * Getter for allPictures instance variable
     *
     * @return ArrayList of all Picture objects that the program has created thus far
     */
    public ArrayList<Picture> getAllPictures() {
        return this.allPictures;
    }

    /**
     * Getter for directoryPictures instance variable
     *
     * @return list of all Picture objects that are in this directory (and sub-directories)
     */
    public ArrayList<Picture> getDirectoryPictures() {
        return this.directoryPictures;
    }

    /**
     * Getter for viewingDir instance variable
     *
     * @return The current value of this PictureManager's viewingDir
     */
    public String getViewingDir() {
        return this.viewingDir;
    }

    /**
     * Setter for viewingDir instance variable
     *
     * @param newDir the new directory that viewingDir will be set to
     */
    public void setViewingDir(String newDir) {
        this.viewingDir = newDir;
    }

    /**
     * Changes the directory of the photo with the current location of dataPath
     * For example, suppose we have a Picture object with data path:
     * C:/Users/Calvin/Documents/Something.jpeg
     * if we were to change the directory to "C:/Users/SomeoneElse/Downloads", then the new data path would be:
     * C:/Users/SomeoneElse/Downloads/Something.jpeg
     *
     * @param picture Picture object that is going to be moved
     * @param newDir  new directory to move photo into
     * @return message describing the result of this method
     */
    public String changeDirectoryOfPicture(Picture picture, String newDir) {
        try {
            Path currentDirectory = Paths.get(picture.getDataPath());
            //Creates a Path object with absolute directory of picture.getDataPath.
            String newDataPath = newDir + File.separator + picture.getName();
            Path newDirectory = Paths.get(newDataPath);
            Files.move(currentDirectory, newDirectory);  //moves file at currentDirectory to newDirectory
            //Files.move(...) will throw a FileAlreadyExistsException if the method fails due to a file with the
            // same name already existing at newDirectory
            picture.setDataPath(newDataPath);
            SerializableOperator.saveFile(this.pathName, this.allPictures);
            return ("File movement was successful");
        } catch (FileAlreadyExistsException e1) {
            return ("File already exists at target directory!");
        } catch (IOException e2) {
            return ("File movement was unsuccessful");
        }
    }

    /**
     * Modifies directoryPictures attribute so that only Pictures that contain
     * all of the tags in tagsOfInterest will remain in directoryPictures. ALl
     * filtered pictures will be stored in the hiddenPictures list attribute.
     *
     * @param tagsOfInterest List containing tags that the pictures should contain
     */
    void hidePicturesWithoutTags(ArrayList<String> tagsOfInterest) {
        // 1) Check if there are any tags that we want to filter pictures by
        if (tagsOfInterest.size() == 0) {
            return;
        }

        // 2) For every tag in the tagsOfInterest, check every Picture in the
        //    directoryPictures attribute to see if it contains the tag. If it
        //    doesn't, add that Picture object to the hiddenPictures attribute
        //    and remove it from directoryPictures.
        for (String currentTag : tagsOfInterest) {
            ArrayList<Picture> updatedDirectoryPictures = new ArrayList<>();

            for (int i = 0; i < this.directoryPictures.size(); i++) {
                if (this.directoryPictures.get(i).getAttachedTags().contains(currentTag)) {
                    updatedDirectoryPictures.add(this.directoryPictures.get(i));
                } else {
                    this.hiddenPictures.add(this.directoryPictures.get(i));
                }
            }
            this.directoryPictures = updatedDirectoryPictures;
        }
    }

    /**
     * Modifies the directoryPictures attribute so that it only contains Pictures directly
     * in the current viewing directory, i.e., no pictures from subdirectories.
     * <p>
     * Lines 410-411 was adapted from a post by Jon Skeet on 20120426 to a stackoverflow forum here:
     * https://stackoverflow.com/questions/10336293/splitting-filenames-using-system-file-separator-symbol
     */
    void hideSubdirectoryPictures() {
        ArrayList<Picture> updatedDirectoryPictures = new ArrayList<>();

        for (int i = 0; i < this.directoryPictures.size(); i++) {
            // Get the exact directory that the Picture object is stored in and store it in
            // pictureDirectory. For example, if the data path to a picture is:
            // C:/Users/Somebody/Pictures/subdirectory/chicken.png
            // then, pictureDirectory = "C:/Users/Somebody/Pictures/subdirectory".
            String regex = Pattern.quote(System.getProperty("file.separator"));
            String[] dataPathComponents = this.directoryPictures.get(i).getDataPath().split(regex);
            String pictureDirectory = "";
            for (int index = 0; index < dataPathComponents.length - 1; index++) {
                if (index < dataPathComponents.length - 2) {
                    pictureDirectory += dataPathComponents[index] + File.separator;
                } else {
                    pictureDirectory += dataPathComponents[index];
                }
            }

            // Checks to see if the pictureDirectory matches the viewingDir attribute completely via
            // String.equals() comparison. If pictureDirectory is a subdirectory of viewingDir, then
            // the if statement on the following line will be false.
            if (pictureDirectory.equals(this.viewingDir)) {
                updatedDirectoryPictures.add(this.directoryPictures.get(i));
            } else {
                this.hiddenPictures.add(this.directoryPictures.get(i));
            }
        }
        this.directoryPictures = updatedDirectoryPictures;
    }

    /**
     * Modifies directoryPictures attribute so that all Pictures that are in
     * the hiddenPictures attribute are added to the directoryPictures list
     * attribute.
     */
    void showHiddenPictures() {
        for (int i = 0; i < this.hiddenPictures.size(); i++) {
            this.directoryPictures.add(this.hiddenPictures.get(i));
        }
        this.hiddenPictures.clear();
    }
}

