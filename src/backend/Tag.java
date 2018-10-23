package backend;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Tag class which keeps track of all currently available tags.
 */
public class Tag {

    /**
     * An ArrayList of tags the user can select and apply to Pictures
     */
    private ArrayList<String> tags = new ArrayList<>();

    /**
     * The directory to the .ser file for Tag including file name and extension
     */
    private String pathName;

    /**
     * Constructs a new Tag object
     *
     * @param pathName the relative directory to the .ser file for this Tag
     * @throws ClassNotFoundException Thrown when the class is not found
     * @throws IOException            Thrown when the file is not found
     */
    public Tag(String pathName) throws ClassNotFoundException, IOException {
        this.pathName = pathName;
        boolean initializeResult = SerializableOperator.initializeFile(this.pathName);
        if (initializeResult) {
            this.tags = (ArrayList<String>) SerializableOperator.readFile(pathName);
        } else {
            SerializableOperator.createFile(pathName, false);
        }
        SerializableOperator.saveFile(this.pathName, this.tags);
    }

    /**
     * Getter for the tags instance variable of this Tag object
     *
     * @return the ArrayList of available tags
     */
    public ArrayList<String> getTags() {
        return this.tags;
    }

    /**
     * Adds a tag to the current tags list and returns a status message.
     * Tags without a leading @ and tags that contains at least one
     * of "\", "/" or "." will not be added. Also tags with more than
     * one @ symbol will not be added.
     *
     * @param newTag The tag to be added to the current ArrayList of tags.
     * @return a message saying if the tag was added or not
     */
    public String addTag(String newTag) throws IOException {
        // Checks to see if newTag contains any illegal characters
        if (newTag.contains(".")) {
            return "Tag was not added as it contains \".\"";
        } else if (newTag.contains("/")) {
            return "Tag was not added as it contains \"/\"";
        } else if (newTag.contains("\\")) {
            return "Tag was not added as it contains \"\\\"";
        }

        // Checks to see if the tag starts with @ and has only one
        int count = 0;
        for (int index = 0; index < newTag.length(); index++) {
            if (newTag.charAt(index) == '@') {
                count += 1;
            }
        }
        if (newTag.charAt(0) != '@') {
            return "Tag was not added as it must begin with @";
        } else if (count != 1) {
            return "Tag was not added as it must only have one @ sign";
        }

        //Checks if the the tag is empty
        if (!(newTag.length() > 1)) {
            return "The tag can not be empty";
        }

        //Checks if the tag is just an @ with spaces
        if (newTag.trim().equals("@")) {
            return "The tag can not be empty";
        }

        //Checks to see if the tag already exists (Case sensitive)
        if (this.tags.indexOf(newTag) == -1) {
            this.tags.add(newTag);
            // We write to the .ser file to save the current state of tags (the ArrayList)
            SerializableOperator.saveFile(this.pathName, this.tags);
            return "The tag was added successfully";
        } else {
            return "The Tag already exist and was not added";
        }
    }

    /**
     * Deletes a tag from the current tags list and returns a status message
     *
     * @param deletedTag The tag to be deleted from the current ArrayList of tags.
     * @return a message saying if the tag was deleted or not
     */
    public String removeTag(String deletedTag) throws IOException {
        this.tags.remove(deletedTag); //Removes first occurrence of deletedTag
        // We write to the .ser file to save the current state of tags (the ArrayList)
        SerializableOperator.saveFile(this.pathName, this.tags);
        // Note that it will always be successfully since we are forcing the user to select
        // a tag from a list of existing tags
        return "Successfully removed the tag";
    }

    /**
     * Checks to see if a given tag is already in this set of tags
     *
     * @param tagToCheck the tag we want to know if it is in this set of tags
     * @return true if the tagToCheck is in this set of Tags, false otherwise
     */
    public boolean containsTag(String tagToCheck) {
        return this.tags.contains(tagToCheck);
    }

    /**
     * Sorts this list of usable tags with respect to a keyword.
     * Words that contain the keyword will be position closer to the front
     * of the list
     *
     * @param keyword the keyword we are searching for in tags to sort them
     */
    public void sortTagsByKeyword(String keyword) {
        ArrayList<String> sortedVersion = new ArrayList<>();
        for (int index = 0; index < this.tags.size(); index++) {
            if (this.tags.get(index).contains(keyword)) {
                // add tag to front since it matched
                sortedVersion.add(0, this.tags.get(index));
            } else {
                // add tag to back of list since not match
                sortedVersion.add(this.tags.get(index));
            }
        }
        this.tags = sortedVersion;
    }
}
