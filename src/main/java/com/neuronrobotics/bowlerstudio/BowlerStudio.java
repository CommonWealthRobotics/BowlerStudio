package com.neuronrobotics.bowlerstudio;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.neuronrobotics.jniloader.HaarDetector;
import com.neuronrobotics.jniloader.IObjectDetector;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.util.ThreadUtil;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class BowlerStudio extends Application {
    
    private static TextArea log;
    private static MainController controller;

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

        scene.getStylesheets().add(BowlerStudio.class.getResource("java-keywords.css").
                toExternalForm());
        
        PerspectiveCamera camera = new PerspectiveCamera();
        
        scene.setCamera(camera);

        primaryStage.setTitle("Bowler Studio");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(768);
        primaryStage.show();
        primaryStage.setOnCloseRequest(arg0 -> {
        	
        	controller.disconnect();
        	ThreadUtil.wait(500);
        	System.exit(0);
		});
        Log.enableDebugPrint();
        IObjectDetector detector = new HaarDetector("haarcascade_frontalface_default.xml");
    }

    public static Parent loadFromFXML() {
        
        if (controller!=null) {
            throw new IllegalStateException("UI already loaded");
        }
        
        FXMLLoader fxmlLoader = new FXMLLoader(
                BowlerStudio.class.getResource("Main.fxml"));
        try {
            fxmlLoader.load();
        } catch (IOException ex) {
            Logger.getLogger(BowlerStudio.class.getName()).
                    log(Level.SEVERE, null, ex);
        }

        Parent root = fxmlLoader.getRoot();
        
        root.getStylesheets().add(BowlerStudio.class.getResource("java-keywords.css").
                toExternalForm());

        controller = fxmlLoader.getController();
        log = controller.getLogView();
        

        return root;
    }
    
    public static TextArea getLogView() {
        
        if (log==null) {
            throw new IllegalStateException("Load the UI first.");
        }
        
        return log;
    }
}
