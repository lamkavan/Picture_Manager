package GUI;

import backend.Controller;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class GuiLayout implements Initializable {

    public ScrollPane scrollPane;
    public HBox hbox, bottomButtonHBox;
    public Text directoryText, selectedPictureText;
    public Label bottomLabel;
    public VBox existingTagBox, currentTagBox, prevNameBox, logBox;
    public Button movePicToDirButton, changeDirButton, addNewTagToListButton, addToPicButton,
            deleteButton, revertToPrevButton, sortButton;
    public TextField newTagToList, keyWordEntry;

    private GridPane gridPane = new GridPane();
    private int cols = 4;

    private ArrayList<String> pictureLocations;

    static StringProperty dir = new SimpleStringProperty();
    static StringProperty selectedPicturePath = new SimpleStringProperty("");
    private StringProperty bottomLabelText = new SimpleStringProperty();
    private ArrayList<String> extPrefs = new ArrayList<String>();
    private String logFileLocation = "./src/serial_files/log.ser";
    private String tagFileLocation = "./src/serial_files/tag.ser";
    private String pictureManagerFileLocation = "./src/serial_files/picMan.ser";

    private Controller controller;

    private ToggleGroup group = new ToggleGroup();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        if (Main.isCommandLine) {
            this.logFileLocation = "./serial_files/log.ser";
            this.tagFileLocation = "./serial_files/tag.ser";
            this.pictureManagerFileLocation = "./serial_files/picMan.ser";
        }

        extPrefs.add("jpg");
        extPrefs.add("jpeg");
        extPrefs.add("png");
        extPrefs.add("bmp");
        extPrefs.add("gif");
        extPrefs.add("JPG");
        extPrefs.add("JPEG");
        extPrefs.add("PNG");
        extPrefs.add("BMP");
        extPrefs.add("GIF");

        directoryText.textProperty().bind(dir);
        selectedPictureText.textProperty().bind(selectedPicturePath);
        bottomLabel.textProperty().bind(bottomLabelText);

        try {
            controller = new Controller(dir.get(), extPrefs, logFileLocation, tagFileLocation, pictureManagerFileLocation);

        } catch (Exception e) {
            System.out.println("Error");
        }

        updateAll();
    }


    /**
     * This method displays all photos in the list with picture datapaths
     */
    private void createAndSetImageViews() {

        ArrayList<ImageView> imageViews = new ArrayList<>();

        try {
            for (String location : pictureLocations) {

                imageViews.add(createImageView(location));

            }
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL exception!");
        }

        setImagePositionOnGrid(imageViews, cols);
        addImageViewsOnGrid(imageViews);

    }

    /**
     * Creates and returns and ImageView containing the image at imagePath
     *
     * @param imagePath path in which the image is located
     * @return Returns ImageView containing the image at imagePath
     * @throws MalformedURLException Throws when a Malformed URL is formed
     */
    private ImageView createImageView(String imagePath) throws MalformedURLException {
        File file = new File(imagePath);
        Image image = new Image(file.toURI().toURL().toExternalForm());

        ImageView imageView = new ImageView();
        imageView.setImage(image);
        imageView.setId(imagePath);
        imageView.setFitWidth(200);
        imageView.setPreserveRatio(true);
        // we set this to false, so that we use faster, but lesser quality filtering.
        imageView.setSmooth(false);

        // store the imageView as cache, which may increase performance (but uses more memory)
        imageView.setCache(true);

        // add click event handler
        imageView.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                    selectedPicturePath.setValue(imageView.getId());
                    imageViewClicked();
                    updateCurrentTagView();
                    updatePrevNameView();
                }
        );

        return imageView;
    }

    /**
     * Creates the "view" button dynamically.
     * This method is called when an image view has been clicked.
     */
    private void imageViewClicked() {

        if (bottomButtonHBox.getChildren().get(0) instanceof Button) {
            return;
        }

        // generate the view larger, open in file browser and view suggested tags button
        Button viewButton = new Button("View Larger");
        Button openInFBrowseButton = new Button("Open In OS File Browser");
        Button suggestedTagsButton = new Button("View Suggested Tags");

        viewButton.setStyle("-fx-font: 11 system;");
        openInFBrowseButton.setStyle("-fx-font: 11 system;");
        suggestedTagsButton.setStyle("-fx-font: 11 system;");

        viewButton.setOnAction(e -> {
            replaceGridPaneWithLargeImage();
            generateReturnToGridPaneButton();
        });

        openInFBrowseButton.setOnAction(e -> {
            openInFileBrowser();
        });

        suggestedTagsButton.setOnAction(e -> {
            openSuggestedTagsWindow();
        });

        bottomButtonHBox.getChildren().add(0, suggestedTagsButton);
        bottomButtonHBox.getChildren().add(0, openInFBrowseButton);
        bottomButtonHBox.getChildren().add(0, viewButton);
    }

    private void openSuggestedTagsWindow() {
        if (selectedPicturePath.get().equals("")) {
            return;
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("suggestedTagsWindow.fxml"));
            Parent root = (Parent) fxmlLoader.load();
            SuggestedTagsController controller = fxmlLoader.<SuggestedTagsController>getController();
            controller.setSuggestedTags(this.controller.getSuggestedTags(selectedPicturePath.get()));
            controller.setGuiLayoutController(this);
            Stage stage = new Stage();
            stage.setResizable(false);
            stage.setScene(new Scene(root, 250, 350));
            stage.show();
        } catch (Exception e) {
            bottomLabelText.set("Could not connect to image recognition API.");
        }
    }

    /**
     * Line 204 was adapted from a post by Buhake Sindi on a stackoverflow forum here:
     * https://stackoverflow.com/questions/15875295/open-a-folder-in-explorer-using-java
     */
    private void openInFileBrowser() {

        if (selectedPicturePath.get().equals("")) {
            return;
        }

        // get the directory for the picture
        String datapath = selectedPicturePath.get();
        // get the parent directory
        File dir = new File(datapath).getParentFile();
        try {
            Desktop.getDesktop().open(dir);
        } catch (IOException e) {
            System.out.println("Unable to open file.");
        }
    }

    /**
     * Generates the button that returns the grid pane to display image in a grid.
     * This method should be called when the user wants to view a larger version of
     * the selected picture.
     */
    private void generateReturnToGridPaneButton() {
        bottomButtonHBox.getChildren().remove(0);
        Button returnButton = new Button("Back");
        returnButton.setStyle("-fx-font: 11 system;");
        returnButton.setOnAction(e -> {
            scrollPane.setContent(null);
            bottomButtonHBox.getChildren().remove(0);
            bottomButtonHBox.getChildren().remove(0);
            bottomButtonHBox.getChildren().remove(0);
            updateAll();
        });

        bottomButtonHBox.getChildren().add(0, returnButton);

    }

    /**
     * Replaces the grid pane (the grid of images) with a larger view of the selected
     * picture.
     */
    private void replaceGridPaneWithLargeImage() {
        scrollPane.setContent(null);
        ImageView imageView = new ImageView();
        File file = new File(GuiLayout.selectedPicturePath.get());
        Image image = null;
        try {
            image = new Image(file.toURI().toURL().toExternalForm());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        imageView.setImage(image);
//            imageView.setFitHeight(800);
        imageView.setFitWidth(800);
        imageView.setFitHeight(700);
        scrollPane.setContent(imageView);
    }


    /**
     * Adds the ImageViews inside imageViews into the GridPane
     *
     * @param imageViews The ArrayList in which the desired imageViews are located in
     */
    private void addImageViewsOnGrid(ArrayList<ImageView> imageViews) {
        for (ImageView image : imageViews) {
            gridPane.getChildren().add(image);
        }
    }

    /**
     * Sets the positioning of the imageView on the grid
     *
     * @param imageViews The list of ImageView files that we will add
     * @param maxCols    Max number of columns
     */
    private void setImagePositionOnGrid(ArrayList<ImageView> imageViews, int maxCols) {
        int i = 0;
        int currentRow = 0;
        int currentCol = 0;
        while (i < imageViews.size()) {

            if (currentCol == maxCols) {
                currentRow++;
                currentCol = 0;
            }
            GridPane.setConstraints(imageViews.get(i), currentCol, currentRow);
            currentCol++;
            i++;
        }
    }

    /**
     * Updates the GridPane by clearing the current GridPane and adds pictures that should be viewed onto
     * the GridPane again.
     * <p>
     * This method is useful when the user changes the directory of a picture into a directory that is
     * not under the current directory. This would mean that the picture that was moved, should not be
     * visible in the current program. Therefore, the GridPane must be updated.
     */
    private void updateGridPane() {
        gridPane.getChildren().clear();
        // we need pictureLocations to know which pictures to display
        pictureLocations = controller.getPictureDataPaths();

        createAndSetImageViews();

        for (Node imageView : gridPane.getChildren()) {
            GridPane.setValignment(imageView, VPos.CENTER);
            GridPane.setHalignment(imageView, HPos.CENTER);
        }

        gridPane.setVgap(20);
        gridPane.setHgap(20);
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setOpaqueInsets(new Insets(10, 10, 10, 10));
        scrollPane.setContent(gridPane);
    }

    /**
     * Filters Pictures so that only Pictures containing ALL of the tags selected in the tag box
     * are displayed in the grid pane.
     */
    public void hidePicturesWithoutSpecifiedTags() {
        ArrayList<String> tagsToFilter = new ArrayList<>();
        // if the user selected something in the existingTagBox,
        if (existingTagBox.getChildren().size() > 0) {
            ObservableList<Node> tags = existingTagBox.getChildren();
            for (Node node : tags) {
                CheckBox r = (CheckBox) node;
                if (r.isSelected()) {
                    tagsToFilter.add(r.getText());
                }
            }
        }
        controller.hidePicturesWithoutTag(tagsToFilter);
        updateAll();
    }

    /**
     * Filters Pictures that the user is currently viewing so that pictures in subdirectories are
     * hidden from view.
     */
    public void hideSubdirectoryPictures() {
        controller.hideSubdirectoryPictures();
        updateAll();
    }

    /**
     * Shows Pictures that were previously filtered via the hidePicturesWithoutSpecifiedTags and
     * hideSubdirectoryPictures methods.
     */
    public void showHiddenPictures() {
        controller.showHiddenPictures();
        updateAll();
    }

    /**
     * Moves the picture located in selectedPicturePath.get() to newPictureDir.getText()
     */
    public void changeFileDirectory(ActionEvent event) {
        if (selectedPicturePath.get().equals("")) {
            return;
        }
        String newDir = getDirFromFileChooser(event);
        if (!newDir.equals("")) {
            String result = controller.changeFileDirectory(selectedPicturePath.get(), newDir);
            updateAll();
            bottomLabelText.setValue(result);
            selectedPicturePath.setValue("");
        }
    }

    /**
     * Changes the current viewing directory to newDir.getText();
     */
    public void changeCurrentDirectory(ActionEvent event) {
        String oldDir = dir.get();
        String newDir = getDirFromFileChooser(event);
        try {
            if (!newDir.equals("")) {
                controller.changeCurrentDirectory(newDir);
                updateAll();
                dir.setValue(newDir);
                bottomLabelText.setValue("Viewing directory has been changed from " + oldDir + " to " + newDir);
            }
        } catch (Exception e) {
            bottomLabelText.setValue("Could not change directory!");
        }
    }

    /**
     * Updates the existing tag view in the GUI, so that relevant information is displayed.
     */
    private void updateExistingTagView() {
        existingTagBox.getChildren().clear();

        // get the currently existing tags
        ArrayList<String> existingTags = controller.getAvailableTags();

        // create and add checkBox button for each tag
        for (String tag : existingTags) {
            CheckBox box = new CheckBox(tag);
            existingTagBox.getChildren().add(box);
        }
    }


    /**
     * Updates the current tag view in the GUI, so that relevant information is displayed.
     */
    private void updateCurrentTagView() {
        currentTagBox.getChildren().clear();

        // If there is no selected picture path, then return, because current tag view should be empty
        if (selectedPicturePath.get().equals("")) {
            return;
        }

        // Get current tags for the selected picture, create and add checkBox buttons for the current tag box
        ArrayList<String> currentTags = controller.getPictureCurrentTags(selectedPicturePath.get());
        for (String tag : currentTags) {
            CheckBox box = new CheckBox(tag);
            currentTagBox.getChildren().add(box);
        }
    }


    /**
     * Updates the previous names box in the GUI, so that relevant information is displayed.
     */
    private void updatePrevNameView() {
        prevNameBox.getChildren().clear();
        if (selectedPicturePath.get().equals("")) {
            return;
        }
        // get previous names and create radio buttons and add to the prev name box
        ArrayList<String> prevNames = controller.getPrevFileNames(selectedPicturePath.get());
        for (String prevName : prevNames) {
            RadioButton prevRadio = new RadioButton(prevName);
            prevRadio.setToggleGroup(group);
            prevNameBox.getChildren().add(prevRadio);
        }
    }


    /**
     * Adds the tag in the textbox, into the existing tag list.
     * This method is called when the "Add Tag to Picture" button is clicked on the GUI.
     */
    public void addNewTagButtonClicked() {
        ArrayList<String> tagsToAdd = new ArrayList<>();
        tagsToAdd.add("@" + newTagToList.getText());
        addTagsToList(tagsToAdd);
        newTagToList.clear(); //removes the text from the text field
    }

    /**
     * Connects to the controller to add a tag to the set existing tags
     */
    public void addTagsToList(ArrayList<String> tags) {
        try {
            controller.addToTagList(tags);
        } catch (Exception e) {
            bottomLabelText.setValue("Unable to access previous list of tags! Something Went Wrong!");
        }
        updateExistingTagView();
    }

    /**
     * Adds the selected tag in the existing tag list, onto the selected picture
     * This method is called when the "Add Tag to Picture" button is clicked on the GUI.
     */
    public void addToPicClicked() {
        if (selectedPicturePath.get().equals("")) {
            return;
        }

        ArrayList<String> tagsToAdd = new ArrayList<>();
        String feedback = "";

        // if the user selected something in the existingTagBox,
        if (existingTagBox.getChildren().size() > 0) {
            ObservableList<Node> tags = existingTagBox.getChildren();
            for (Node node : tags) {
                CheckBox r = (CheckBox) node;
                if (r.isSelected()) {
                    tagsToAdd.add(r.getText());
                }
            }
        }

        try {
            // only call addTags to picture when there are tags to add
            if (tagsToAdd.size() > 0) {
                feedback = controller.addTagsToPicture(selectedPicturePath.get(), tagsToAdd);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        bottomLabelText.set(feedback);

        updateAll();
    }

    /**
     * Returns true if the Vbox box contains a radio button that is selected.
     *
     * @param box The Vbox to be checked for a clicked radio button
     * @return Returns true if the Vbox box contains a radio button that is selected.
     */
    private boolean existsCheckedItem(VBox box) {
        ObservableList<Node> items = box.getChildren();
        for (Node node : items) {
            CheckBox r = (CheckBox) node;
            if (r.isSelected()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the selected radio button from the existing tag box
     */
    public void removeTagsFromExistingTags() {

        if (!existsCheckedItem(existingTagBox)) {
            return;
        }

        ObservableList<Node> tags = existingTagBox.getChildren();
        String feedback = "";
        ArrayList<String> tagsToRemove = new ArrayList<>();

        for (Node node : tags) {
            CheckBox r = (CheckBox) node;
            if (r.isSelected()) {
                tagsToRemove.add(r.getText());
            }
        }
        if (tagsToRemove.size() > 0) {
            try {
                feedback = controller.removeFromTagList(tagsToRemove);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        bottomLabelText.set(feedback);
        updateAll();
    }

    /**
     * Removes the selected radio button from the current tag box
     */
    public void removeTagsFromPicture() {
        ObservableList<Node> tags = currentTagBox.getChildren();

        String feedBack = "";

        if (selectedPicturePath.get().equals("") || !(existsCheckedItem(currentTagBox))) {
            return;
        }
        ArrayList<String> tagsToRemove = new ArrayList<>();


        for (Node node : tags) {
            CheckBox r = (CheckBox) node;

            if (r.isSelected()) {
                tagsToRemove.add(r.getText());
            }
        }
        try {
            if (tagsToRemove.size() > 0) {
                feedBack = controller.removeTagsFromPicture(selectedPicturePath.get(), tagsToRemove);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        bottomLabelText.setValue(feedBack);
        updateAll();
    }


    /**
     * Reverts the selected file name to the selected previous name in the GUI
     * This method is called when the Revert to Previous Tag button is clicked in the GUI.
     */
    public void revertToPrev() {

        ObservableList<Node> prevNames = prevNameBox.getChildren();

        if (selectedPicturePath.get().equals("")) {
            return;
        }

        // This string is the string that will appear on the label at the bottom of the GUI
        String feedBack = "";

        for (Node node : prevNames) {
            RadioButton r = (RadioButton) node;

            try {
                if (r.isSelected()) {
                    feedBack = controller.revertToPrevFileName(selectedPicturePath.get(), r.getText());
                }

            } catch (IOException e) {
                feedBack = "Something went wrong! Cannot read file!";
            }
        }
        bottomLabelText.setValue(feedBack);
        updateAll();
    }

    /**
     * Updates the log view on the GUI with relevant information.
     */
    private void updateLog() {
        logBox.getChildren().clear();
        ArrayList<String> logs = controller.getLog();
        for (String l : logs) {
            Text text = new Text(l);
            logBox.getChildren().add(text);
        }
    }

    /**
     * Updates the GridPane (the picture display), the previous name view, current tag view, existing tag view
     * and the log view.
     */
    private void updateAll() {
        selectedPicturePath.setValue("");
        updateGridPane();
        updatePrevNameView();
        updateCurrentTagView();
        updateExistingTagView();
        updateLog();
    }

    /**
     * Opens file chooser where the user can choose a directory.
     * Returns a string that represents the absolute path of the selected directory.
     *
     * @param event ActionEvent from the button click that invokes this method.
     * @return Returns a string that represents the absolute path of the selected directory.
     */
    public String getDirFromFileChooser(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        File selectedDir = directoryChooser.showDialog(stage);
        if (selectedDir != null) {
            return selectedDir.getAbsolutePath();
        }
        return "";
    }

    /**
     * Connects to the controller to sort the list of existing tags
     */
    public void sortExistingTags() {
        String keyword = keyWordEntry.getText();
        try {
            controller.sortTags(keyword);
        } catch (Exception e) {
            bottomLabelText.setValue("Unable to access previous list of tags! Something Went Wrong!");
        }
        updateExistingTagView();
        keyWordEntry.clear();
    }
}
