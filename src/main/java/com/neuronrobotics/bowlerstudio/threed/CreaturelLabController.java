package com.neuronrobotics.bowlerstudio.threed;

/**
 * Sample Skeleton for "CreatureLab.fxml" Controller Class
 * You can copy and paste this code into your favorite IDE
 **/

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;


public class CreaturelLabController {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="CadControlsAnchor"
    private AnchorPane CadControlsAnchor; // Value injected by FXMLLoader

    @FXML // fx:id="DriveControlsAnchor"
    private AnchorPane DriveControlsAnchor; // Value injected by FXMLLoader

    @FXML // fx:id="TempControlsAnchor"
    private AnchorPane TempControlsAnchor; // Value injected by FXMLLoader

    @FXML // fx:id="jfx3dControls"
    private AnchorPane jfx3dControls; // Value injected by FXMLLoader

    @FXML // fx:id="overlayScrollPanel"
    private ScrollPane overlayScrollPanel; // Value injected by FXMLLoader

    @FXML // fx:id="viewContainer"
    private AnchorPane viewContainer; // Value injected by FXMLLoader


    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert CadControlsAnchor != null : "fx:id=\"CadControlsAnchor\" was not injected: check your FXML file 'CreatureLab.fxml'.";
        assert DriveControlsAnchor != null : "fx:id=\"DriveControlsAnchor\" was not injected: check your FXML file 'CreatureLab.fxml'.";
        assert TempControlsAnchor != null : "fx:id=\"TempControlsAnchor\" was not injected: check your FXML file 'CreatureLab.fxml'.";
        assert jfx3dControls != null : "fx:id=\"jfx3dControls\" was not injected: check your FXML file 'CreatureLab.fxml'.";
        assert overlayScrollPanel != null : "fx:id=\"overlayScrollPanel\" was not injected: check your FXML file 'CreatureLab.fxml'.";
        assert viewContainer != null : "fx:id=\"viewContainer\" was not injected: check your FXML file 'CreatureLab.fxml'.";

        // Initialize your logic here: all @FXML variables will have been injected
        

    }

}
