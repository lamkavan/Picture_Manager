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
 * JUnit test for the Tag class
 * NOTE: all the test will return "Cannot read from input." This is because of how the backend works and
 * has nothing to do with the test. It may be ignored.
 */
public class TagJUnitTest {

    // Used http://junit.org/junit4/javadoc/4.12/org/junit/rules/TemporaryFolder.html to allow the unitTests
    // to use the serializable files without changing the information contained in it
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void createAFileWithNoTags() throws ClassNotFoundException, IOException {
        // to convert a File to a string used the code from:
        // https://stackoverflow.com/questions/12559917/java-how-to-convert-a-file-object-to-a-string-object-in-java
        File tempFile = folder.newFile("tag.ser");
        Tag tag = new Tag(tempFile.getPath()); // creates a tag object to use
        ArrayList<String> actualTagList = new ArrayList<>();
        assertEquals(actualTagList, tag.getTags());
    }

    @Test
    public void addMultipleValidTags() throws ClassNotFoundException, IOException {
        File tempFile = folder.newFile("tag.ser");
        Tag tag = new Tag(tempFile.getPath()); // dummy files
        tag.addTag("@Tiger_a");
        tag.addTag("@Deer hi");
        tag.addTag("@Trees");

        ArrayList<String> actualTagList = new ArrayList<>();
        actualTagList.add("@Tiger_a");
        actualTagList.add("@Deer hi");
        actualTagList.add("@Trees");

        assertEquals(actualTagList, tag.getTags());
    }

    @Test
    public void addMultipleInvalidTag() throws ClassNotFoundException, IOException {
        File tempFile = folder.newFile("tag.ser");
        Tag tag = new Tag(tempFile.getPath()); // dummy files
        tag.addTag("Tiger");
        tag.addTag("@@Deer");
        tag.addTag("T/rees");
        tag.addTag("T\\rees");
        tag.addTag("T@rees@");
        tag.addTag("@    ");

        ArrayList<String> actualTagList = new ArrayList<>();

        assertEquals(actualTagList, tag.getTags());
    }

    @Test
    public void addTagThatExists() throws ClassNotFoundException, IOException {
        File tempFile = folder.newFile("tag.ser");
        Tag tag = new Tag(tempFile.getPath()); // dummy files
        tag.addTag("@Tiger");
        tag.addTag("@Tiger");
        tag.addTag("@Deer");
        tag.addTag("@Tiger");
        tag.addTag("@Deer");

        ArrayList<String> actualTagList = new ArrayList<>();
        actualTagList.add("@Tiger");
        actualTagList.add("@Deer");

        assertEquals(actualTagList, tag.getTags());
    }

    @Test
    public void removeMultipleTags() throws ClassNotFoundException, IOException {
        File tempFile = folder.newFile("tag.ser");
        Tag tag = new Tag(tempFile.getPath()); // dummy files
        tag.addTag("@Tiger");
        tag.addTag("@Deer");
        tag.removeTag("@Tiger");
        tag.removeTag("@Deer");

        ArrayList<String> actualTagList = new ArrayList<>();

        assertEquals(actualTagList, tag.getTags());
    }

    @Test
    public void removeNonExistentTag() throws ClassNotFoundException, IOException {
        File tempFile = folder.newFile("tag.ser");
        Tag tag = new Tag(tempFile.getPath()); // dummy files
        tag.removeTag("@Tiger");

        ArrayList<String> actualTagList = new ArrayList<>();

        assertEquals(actualTagList, tag.getTags());
    }

    @Test
    public void checkIfTagExistsInNewFile() throws ClassNotFoundException, IOException {
        File tempFile = folder.newFile("tag.ser");
        Tag tag = new Tag(tempFile.getPath()); // dummy files

        assertFalse(tag.containsTag("@Tiger"));
    }

    @Test
    public void checkIfExistingTagExists() throws ClassNotFoundException, IOException {
        File tempFile = folder.newFile("tag.ser");
        Tag tag = new Tag(tempFile.getPath()); // dummy files
        tag.addTag("@Tiger");
        tag.addTag("@Deer");
        tag.addTag("@Seal");

        assertTrue(tag.containsTag("@Tiger"));
        assertTrue(tag.containsTag("@Deer"));
        assertTrue(tag.containsTag("@Seal"));
    }

    @Test
    public void checkIfNonExistentTagExists() throws ClassNotFoundException, IOException {
        File tempFile = folder.newFile("tag.ser");
        Tag tag = new Tag(tempFile.getPath()); // dummy files
        tag.addTag("@Tiger");
        tag.addTag("@Deer");
        tag.addTag("@Seal");

        assertFalse(tag.containsTag("@Water"));
    }

    @Test
    public void checkIfGetTagWorks() throws ClassNotFoundException, IOException {
        File tempFile = folder.newFile("tag.ser");
        Tag tag = new Tag(tempFile.getPath()); // dummy files
        tag.addTag("@Tiger");

        ArrayList<String> actualTagList = new ArrayList<>();
        actualTagList.add("@Tiger");

        assertEquals(tag.getTags(), actualTagList);
    }

    @Test
    public void checkIfContainsTagWorks() throws ClassNotFoundException, IOException {
        File tempFile = folder.newFile("tag.ser");
        Tag tag = new Tag(tempFile.getPath()); // dummy files
        tag.addTag("@Tiger");
        tag.addTag("@hi hh");

        assertTrue(tag.containsTag("@hi hh"));
        assertFalse(tag.containsTag("@hi"));
    }

    @Test
    public void sortTagsByKeywordWithMatches() throws ClassNotFoundException, IOException {
        File tempFile = folder.newFile("tag.ser");
        Tag tag = new Tag(tempFile.getPath()); // dummy files
        tag.addTag("@Tiger");
        tag.addTag("@hi hh");
        tag.addTag("@Deer");
        tag.addTag("@Seal");

        tag.sortTagsByKeyword("e");

        ArrayList<String> actualTagList = new ArrayList<>();
        actualTagList.add("@Seal");
        actualTagList.add("@Deer");
        actualTagList.add("@Tiger");
        actualTagList.add("@hi hh");

        assertEquals(tag.getTags(), actualTagList);
    }

    @Test
    public void sortTagsByKeywordNoMatches() throws ClassNotFoundException, IOException {
        File tempFile = folder.newFile("tag.ser");
        Tag tag = new Tag(tempFile.getPath()); // dummy files
        tag.addTag("@Tiger");
        tag.addTag("@hi hh");
        tag.addTag("@Deer");
        tag.addTag("@Seal");
        tag.sortTagsByKeyword("UofT");

        ArrayList<String> actualTagList = new ArrayList<>();
        actualTagList.add("@Tiger");
        actualTagList.add("@hi hh");
        actualTagList.add("@Deer");
        actualTagList.add("@Seal");

        assertEquals(tag.getTags(), actualTagList);
    }
}
