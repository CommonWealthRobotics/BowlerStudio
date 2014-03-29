package eu.mihosoft.vrl.fxscad;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent main = loadFromFXML();

        Scene scene = new Scene(main, 1024, 768,true);

        scene.getStylesheets().add(Main.class.getResource("java-keywords.css").
                toExternalForm());
        
        PerspectiveCamera camera = new PerspectiveCamera();
        
        
        scene.setCamera(camera);

        primaryStage.setTitle("JavaFXScad");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(768);
        primaryStage.show();
    }

    private Parent loadFromFXML() {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("Main.fxml"));
        try {
            fxmlLoader.load();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).
                    log(Level.SEVERE, null, ex);
        }

        Parent root = fxmlLoader.getRoot();

        MainController controller = fxmlLoader.getController();

        return root;
    }
}
