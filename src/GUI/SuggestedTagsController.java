package GUI;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class SuggestedTagsController implements Initializable {

    public VBox vbox;
    public Label topText;

    private GuiLayout guiLayoutController;
    private StringProperty topTextString = new SimpleStringProperty("");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        topText.textProperty().bind(topTextString);
    }

    /**
     * Adds the selected tags into existing tags.
     */
    public void addSelectedTags() {
        // find which tags are selected
        ArrayList<String> selectedTags = new ArrayList<>();
        for (Node c : vbox.getChildren()) {
            if (((CheckBox) c).isSelected()) {
                selectedTags.add("@" + ((CheckBox) c).getText());
                ((CheckBox) c).setSelected(false);
            }
        }
        guiLayoutController.addTagsToList(selectedTags);
        topTextString.set("Successfully added to existing tags.");

    }

    void setSuggestedTags(ArrayList<String> suggestedTags) {
        for (String tagName : suggestedTags) {
            CheckBox c = new CheckBox(tagName);
            vbox.getChildren().add(c);
        }
    }

    void setGuiLayoutController(GuiLayout g) {
        guiLayoutController = g;
    }
}
