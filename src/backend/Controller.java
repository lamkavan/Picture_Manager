package backend;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Controller for the GUI
 */
public class Controller {

    /**
     * A file searcher
     */
    private FileSearcher mainFileSearcher;

    /**
     * The current directory the program is in
     */
    private String currentDir;

    /**
     * A Manager that stores and keeps track of Pictures
     */
    private PictureManager mainPictureManager;

    /**
     * A list of all file extensions that the user wants the program to search for
     */
    private ArrayList<String> extensionPreferences;

    /**
     * Log of all changes
     */
    private Log log;

    /**
     * An ArrayList to store all currently existing tags the user can select and attach to Pictures
     */
    private Tag availableTags;

    /**
     * Object which performs GET requests to the Clarifai image recognition API
     */
    private PictureRecognizer pictureRecognizer;

    /**
     * Initializes new Controller object
     *
     * @param currentDir                 the current directory of the program
     * @param extensionPreferences       an ArrayList of picture extensions that program will recognize
     * @param logFileLocation            the location of the log.ser file
     * @param tagFileLocation            the location of the Tag.ser file
     * @param pictureManagerFileLocation the location of the PictureManager.ser file
     */
    public Controller(String currentDir, ArrayList<String> extensionPreferences, String logFileLocation,
                      String tagFileLocation, String pictureManagerFileLocation)
            throws IOException, ClassNotFoundException {
        this.currentDir = currentDir;
        this.extensionPreferences = new ArrayList<>();
        this.mainFileSearcher = new FileSearcher(extensionPreferences);
        this.mainPictureManager = new PictureManager(currentDir, mainFileSearcher.getFileNames(currentDir),
                pictureManagerFileLocation);
        this.log = new Log(logFileLocation);
        this.availableTags = new Tag(tagFileLocation);
        this.pictureRecognizer = new PictureRecognizer("b4324e8aa37c48d28a8199bf87448e8f");
    }

    /**
     * Returns an ArrayList of all the directories (including file name and extension)
     * for all pictures in the current directory and all of it's subdirectories
     *
     * @return a list of Strings that represents the directories of all pictures
     * in the current directory and all the subdirectories
     */
    public ArrayList<String> getPictureDataPaths() {
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < this.mainPictureManager.getDirectoryPictures().size(); i++) {
            result.add(this.mainPictureManager.getDirectoryPictures().get(i).getDataPath());
        }
        return result;
    }

    /**
     * A getter for the currentDir instance variable
     *
     * @return a String that represents the current directory of the program
     */
    public String getDirectory() {
        return this.currentDir;
    }

    /**
     * Returns an ArrayList containing all the previous file names of a Picture
     *
     * @param pictureDir the Pictures directory (with name of file and extension)
     * @return an ArrayList containing the previous file names of a Picture
     */
    public ArrayList getPrevFileNames(String pictureDir) {
        Picture pictureOfInterest = this.mainPictureManager.getPictureAtDataPath(pictureDir);

        // This this if the Picture can not be found
        if (pictureOfInterest == null) {
            return new ArrayList();
        }
        // Do this if the Picture can be found
        return pictureOfInterest.getPrevFileNames();
    }

    /**
     * Adds a given list of tags to a Picture specified by its data path in the order that it appears in
     * the list. If a tag in the list can not be added it ignores it and moves on to the next tag. Also,
     * upon completion a status message is returned
     *
     * @param dataPath  the directory of the Picture file including name and extension
     * @param tagsToAdd the ArrayList of tags to add to a Picture
     * @return a status message letting the user know what happen
     */
    public String addTagsToPicture(String dataPath, ArrayList<String> tagsToAdd) throws IOException {
        String newestDataPath = dataPath; // this keeps track of the data path of the picture after each tag added
        int numOfChanges = 0; // keeps track how many tags we have successfully added to a picture
        for (String tag : tagsToAdd) {
            // Try to add a tag to the Picture and save the result in result
            String[] result = this.addTagToPicture(newestDataPath, tag);
            // read and store what the new data path will be for the picture
            // this is done so the next call to addTagToPicture will have the
            // correct data path. Also update numOfChanges
            if (!(result[1].equals(newestDataPath))) {
                numOfChanges += 1;
            }
            newestDataPath = result[1];
        }
        // Adjust the prevFileName for the picture we changed to only include the first change
        Picture pictureOfInterest = this.mainPictureManager.getPictureAtDataPath(newestDataPath);
        for (int i = numOfChanges; i > 1; i--) {
            pictureOfInterest.deletePrevFileName();
        }
        // Record the changes in the Log
        log.logAddedTag(dataPath, pictureOfInterest.getName());
        return "Tags that could be added to the picture were added";
    }

    /**
     * Adds a new tag to a Picture (if possible) and returns a status message with
     * the data path of the Picture we worked with
     *
     * @param dataPath the directory of the Picture file including name and extension
     * @param tagToAdd the tag we will try to add to a Picture
     * @return a String[] that contains a status message for the user and the data path of the Picture
     */
    public String[] addTagToPicture(String dataPath, String tagToAdd) throws IOException {
        // Get a reference to the picture we are trying to change so we can use it later
        Picture pictureOfInterest = this.mainPictureManager.getPictureAtDataPath(dataPath);
        String result = this.mainPictureManager.addTagToPicture(dataPath, tagToAdd);
        if (!(result.equals(""))) {
            String[] returnValue = {"Successfully added tag to the picture", result};
            return returnValue;
        } else {
            String[] returnValue = {"Unable to add tag to the picture. The tag may have " +
                    "already been applied to this picture", dataPath};
            return returnValue;
        }
    }

    /**
     * Removes a given list of tags from a Picture specified by its data path in the order that it appears in
     * the list. If a tag in the list can not be removed it ignores it and moves on to the next tag. Also,
     * upon completion a status message is returned
     *
     * @param dataPath     the directory of the Picture file including name and extension
     * @param tagsToRemove the ArrayList of tags to remove from a Picture
     * @return a status message letting the user know what happen
     */
    public String removeTagsFromPicture(String dataPath, ArrayList<String> tagsToRemove) throws IOException {
        String newestDataPath = dataPath; // this keeps track of the data path of the picture after each tag removed
        int numOfChanges = 0; // keeps track how many tags we have successfully added to a picture
        for (String tag : tagsToRemove) {
            // Try to remove a tag from the Picture and save the result in result
            String[] result = this.removeTagFromPicture(newestDataPath, tag);
            // read and store what the new data path will be for the picture
            // this is done so the next call to removeTagFromPicture will have the
            // correct data path. Also update numOfChanges
            if (!(result[1].equals(newestDataPath))) {
                numOfChanges += 1;
            }
            newestDataPath = result[1];
        }
        // Adjust the prevFileName for the picture we changed to only include the first change
        Picture pictureOfInterest = this.mainPictureManager.getPictureAtDataPath(newestDataPath);
        for (int i = numOfChanges; i > 1; i--) {
            pictureOfInterest.deletePrevFileName();
        }
        // Record the changes in the Log
        log.logRemovedTag(dataPath, pictureOfInterest.getName());
        return "Tags that could be removed from the picture were removed";
    }

    /**
     * Removes a tag from a Picture (if possible) and returns a status message with
     * the data path of the Picture we worked with
     *
     * @param dataPath       the directory of the Picture file including name and extension
     * @param tagToBeRemoved the tag which will be removed from the Picture
     * @return a string[] that states whether or not the tag was successfully removed
     * and the data path of the Picture
     */
    public String[] removeTagFromPicture(String dataPath, String tagToBeRemoved) throws IOException {
        // Get a reference to the picture we are trying to change so we can use it later
        Picture pictureOfInterest = this.mainPictureManager.getPictureAtDataPath(dataPath);
        String result = this.mainPictureManager.removeTagFromPicture(dataPath, tagToBeRemoved);
        if (!(result.equals(""))) {
            String[] returnValue = {"The tag was successfully removed from the picture", result};
            return returnValue;
        } else {
            String[] returnValue = {"The tag was not able to be removed from the picture.", dataPath};
            return returnValue;
        }
    }

    /**
     * Returns the log of all changes made to Pictures
     *
     * @return the contents of Log
     */
    public ArrayList<String> getLog() {
        return log.getLogList();
    }

    /**
     * Changes a picture file directory to a new specified one and returns a status message
     *
     * @param dataPath the directory of the Picture file including name and extension
     * @param newDir   the new directory that the program is moving the picture to
     * @return a status message telling the user what happen
     */
    public String changeFileDirectory(String dataPath, String newDir) {
        Picture pictureToMove = this.mainPictureManager.getPictureAtDataPath(dataPath);
        String result = this.mainPictureManager.changeDirectoryOfPicture(pictureToMove, newDir);

        // Update the directory pictures as moving a picture could affect this
        this.mainPictureManager.updateDirectoryPictures();

        // Return the status message
        return result;
    }

    /**
     * Changes the current directory of the program to newDir
     *
     * @param newDir the new directory that the program is moving to
     */
    public void changeCurrentDirectory(String newDir) throws IOException {
        this.currentDir = newDir;
        this.mainPictureManager.setViewingDir(newDir);
        this.mainPictureManager.updateAllPictures(this.mainFileSearcher.getFileNames(newDir));
        this.mainPictureManager.updateDirectoryPictures();

        // Now we need to check all the pictures that the user can see in the current directory
        // and see if any of the pictures has tags that are not in the existing tag set.
        // We add these tags to the existing tag set
        ArrayList<Picture> picturesOnScreen = this.mainPictureManager.getDirectoryPictures();
        ArrayList<String> tagsToAdd = new ArrayList<>();
        ArrayList<String> tagsToCheck;
        // search for the tags we might have to add
        for (Picture pictureOfInterest : picturesOnScreen) {
            tagsToCheck = pictureOfInterest.getAttachedTags();
            for (String tag : tagsToCheck) {
                if (!(this.availableTags.containsTag(tag))) {
                    tagsToAdd.add(tag);
                }
            }
        }
        // finally add the tags
        this.addToTagList(tagsToAdd);

    }

    /**
     * Adds a list of tag(s) to list of all tags and returns a status message
     *
     * @param newTags Tag(s) to be added to list of all tags
     * @return a message saying if the tag(s) were added or not
     */
    public String addToTagList(ArrayList<String> newTags) throws IOException {
        for (String newTag : newTags) {
            availableTags.addTag(newTag);
        }
        return "Tag(s) that could be added were added to the set of all tags";
    }

    /**
     * Removes a list of tag(s) from the list of all tags and returns a status message
     *
     * @param deletedTags Tag to be removed
     * @return a message saying that the tag(s) were deleted
     */
    public String removeFromTagList(ArrayList<String> deletedTags) throws IOException {
        for (String deletedTag : deletedTags) {
            availableTags.removeTag(deletedTag);
        }
        return "Tag(s) were deleted from the existing set of tags";
    }

    /**
     * Returns the currently available tag
     *
     * @return Returns the currently available tags
     */
    public ArrayList<String> getAvailableTags() {
        return availableTags.getTags();
    }

    /**
     * Returns a list of strings which represent the tags of the picture specified in dataPath
     *
     * @param dataPath Data path for the picture to get the current Tags from
     * @return Returns a list of strings which represent the tags of the picture specified in dataPath
     */
    public ArrayList<String> getPictureCurrentTags(String dataPath) {
        Picture picture = this.mainPictureManager.getPictureAtDataPath(dataPath);
        return picture.getAttachedTags();
    }

    /**
     * Changes a Picture name to a previous file name selected by the user
     * and returns a status message letting the user now what happen
     *
     * @param dataPath Data path for the picture we want to change the file name(includes extension)
     * @param newName  the new name that we want the Picture file to have with tags and extension
     * @return a status message
     */
    public String revertToPrevFileName(String dataPath, String newName) throws IOException {
        if (this.mainPictureManager.changeFileName(dataPath, newName)) {
            log.logChangedName(dataPath, newName);
            return "The file name was successfully reverted";
        } else {
            return "Unable to change the file name. Another picture may already have this file name";
        }
    }

    /**
     * Returns a list of suggested tag names for the picture located at dataPath.
     * Returns an empty list if the API cannot be accessed.
     *
     * @param dataPath Data path for the picture we want suggested tags for
     * @return an ArrayList of suggested tags
     */
    public ArrayList<String> getSuggestedTags(String dataPath) {
        return pictureRecognizer.getSuggestions(dataPath);
    }

    /**
     * Sorts the list of usable tags with respect to a keyword.
     * Words that contain the keyword will be position closer to the front
     * of the list
     *
     * @param keyword the keyword we are searching for in tags to sort them
     */
    public void sortTags(String keyword) {
        this.availableTags.sortTagsByKeyword(keyword);
    }

    /**
     * Modify the list of pictures in the current directory so that only the
     * Picture objects with tag tagOfInterest are not hidden.
     *
     * @param tagsOfInterest The tag that we are searching all Picture objects for
     */
    public void hidePicturesWithoutTag(ArrayList<String> tagsOfInterest) {
        this.mainPictureManager.hidePicturesWithoutTags(tagsOfInterest);
    }

    /**
     * Modify the list of pictures in the current directory so that Picture objects
     * in subdirectories are no longer displayed. For example, if the current viewing
     * directory is "C:/Users/Somebody/Pictures", then a Picture with the data path
     * "C:/Users/Somebody/Pictures/Chicken.jpg" would remain in the list while a
     * picture at data path "C:/Users/Somebody/Pictures/sub/Cow.jpg" would not.
     */
    public void hideSubdirectoryPictures() {
        this.mainPictureManager.hideSubdirectoryPictures();
    }

    /**
     * Modify the list of pictures in the current directory so that all of the
     * Picture objects which were previously removed from the list via either the
     * hidePicturesWithoutTag or hideSubdirectoryPictures methods are added back
     * into the list. This function call has no effect on the list if it is called
     * when no pictures were previously removed from the list via the aforementioned
     * methods.
     */
    public void showHiddenPictures() {
        this.mainPictureManager.showHiddenPictures();
    }
}

