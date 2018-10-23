package JUnitTests;

import org.junit.Rule;
import org.junit.Test;
import backend.*;
import org.junit.rules.TemporaryFolder;

import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * JUnit test for the Picture class
 * NOTE: all the test will return "Cannot read from input." This is because of how the backend works and
 * has nothing to do with the test. It may be ignored.
 */
public class PictureJUnitTest {

    // Used http://junit.org/junit4/javadoc/4.12/org/junit/rules/TemporaryFolder.html to allow the unitTests
    // to use the serializable files without changing the information contained in it
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void creatingPicture() throws ClassNotFoundException, IOException {
        // to convert a File to a string used the code from:
        // https://stackoverflow.com/questions/12559917/java-how-to-convert-a-file-object-to-a-string-object-in-java
        File tempFile = folder.newFile("Picture.jpg");
        Picture picture = new Picture(tempFile.getPath());
        ArrayList<String> actualTags = new ArrayList<>();

        assertEquals("Picture.jpg", picture.getName());
        assertEquals(actualTags, picture.getAttachedTags());
        assertEquals("Picture.jpg", picture.getOriginalName());
    }

    @Test
    public void addingMultipleTagsToPicture() throws ClassNotFoundException, IOException {
        File tempFile = folder.newFile("Picture.jpg");
        Picture picture = new Picture(tempFile.getPath());
        picture.addNewAttachedTag("@Crayons");
        picture.addNewAttachedTag("@SomethingAboutPicture");

        ArrayList<String> actualTags = new ArrayList<>();
        actualTags.add("@Crayons");
        actualTags.add("@SomethingAboutPicture");

        assertEquals(actualTags, picture.getAttachedTags());
    }

    @Test
    public void addingSameTagToPicture() throws ClassNotFoundException, IOException {
        File tempFile = folder.newFile("Picture.jpg");
        File tempFile2 = folder.newFile("tag.ser");
        Picture picture = new Picture(tempFile.getPath());
        Tag tag = new Tag(tempFile2.getPath()); // creates a tag object to use
        tag.addTag("@Crayons");
        tag.addTag("@SomethingAboutPicture");
        tag.addTag("@Crayons");
        tag.addTag("@SomethingAboutPicture");

        for (String tagToAdd : tag.getTags()) {
            picture.addNewAttachedTag(tagToAdd);
        }

        ArrayList<String> actualTags = new ArrayList<>();
        actualTags.add("@Crayons");
        actualTags.add("@SomethingAboutPicture");

        assertEquals(actualTags, picture.getAttachedTags());
    }

    @Test
    public void addingInvalidTagToPicture() throws ClassNotFoundException, IOException {
        File tempFile = folder.newFile("Picture.jpg");
        File tempFile2 = folder.newFile("tag.ser");
        Picture picture = new Picture(tempFile.getPath());
        Tag tag = new Tag(tempFile2.getPath()); // creates a tag object to use
        tag.addTag("Crayons");
        tag.addTag("@SomethingAboutPicture");

        for (String tagToAdd : tag.getTags()) {
            picture.addNewAttachedTag(tagToAdd);
        }

        ArrayList<String> actualTags = new ArrayList<>();
        actualTags.add("@SomethingAboutPicture");

        assertEquals(actualTags, picture.getAttachedTags());
    }

    @Test
    public void deletingMultipleTagsFromPicture() throws ClassNotFoundException, IOException {
        File tempFile = folder.newFile("Picture.jpg");
        Picture picture = new Picture(tempFile.getPath());
        picture.addNewAttachedTag("@Crayons");
        picture.addNewAttachedTag("@SomethingAboutPicture");
        picture.removeAttachedTag("@Crayons");
        picture.removeAttachedTag("@SomethingAboutPicture");

        ArrayList<String> actualTags = new ArrayList<>();

        assertEquals(actualTags, picture.getAttachedTags());
    }

    @Test
    public void deletingNonExistentTag() throws ClassNotFoundException, IOException {
        File tempFile = folder.newFile("Picture.jpg");
        Picture picture = new Picture(tempFile.getPath());
        picture.removeAttachedTag("@Crayons");
        picture.removeAttachedTag("@SomethingAboutPicture");

        ArrayList<String> actualTags = new ArrayList<>();

        assertEquals(actualTags, picture.getAttachedTags());
    }

    @Test
    public void deletingInvalidTag() throws ClassNotFoundException, IOException {
        File tempFile = folder.newFile("Picture.jpg");
        Picture picture = new Picture(tempFile.getPath());
        picture.removeAttachedTag("Crayons");
        picture.removeAttachedTag("SomethingAboutPicture");
        picture.addNewAttachedTag("@Tag");

        ArrayList<String> actualTags = new ArrayList<>();
        actualTags.add("@Tag");
        assertEquals(actualTags, picture.getAttachedTags());
    }

    @Test
    public void clearingTags() throws ClassNotFoundException, IOException {
        File tempFile = folder.newFile("Picture.jpg");
        Picture picture = new Picture(tempFile.getPath());
        picture.addNewAttachedTag("@Crayons");
        picture.addNewAttachedTag("@Tag");
        picture.clearAttachedTags();

        ArrayList<String> actualTags = new ArrayList<>();
        assertEquals(actualTags, picture.getAttachedTags());
    }

    @Test
    public void changeName() throws ClassNotFoundException, IOException {
        File tempFile = folder.newFile("Picture.jpg");
        Picture picture = new Picture(tempFile.getPath());
        picture.addNewAttachedTag("@Crayons");
        picture.setName("@Crayons");

        assertEquals("@Crayons", picture.getName());
    }
}
