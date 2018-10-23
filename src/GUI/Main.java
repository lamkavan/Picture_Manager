package GUI;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

    public static boolean isCommandLine = false;

    @Override
    public void start(Stage primaryStage) throws Exception {
        GuiLayout.dir = new SimpleStringProperty(getDirFromChooser(primaryStage));
        Parent root = FXMLLoader.load(getClass().getResource("gui.fxml"));
        primaryStage.setScene(new Scene(root, 1450, 800));
        primaryStage.show();
    }


    public static void main(String[] args) {
        if (args.length > 0) {
            isCommandLine = true;
        }
        launch(args);
    }

    private String getDirFromChooser(Stage stage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDir = directoryChooser.showDialog(stage);

        // set the initial directory of the directory chooser
        directoryChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );

        // if user has selected a directory,
        if (selectedDir != null) {
            return selectedDir.getAbsolutePath();
        }

        return System.getProperty("user.home");
    }
}
