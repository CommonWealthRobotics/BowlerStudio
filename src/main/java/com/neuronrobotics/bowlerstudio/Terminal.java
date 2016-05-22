package com.neuronrobotics.bowlerstudio;
/**
 * Sample Skeleton for "Terminal.fxml" Controller Class
 * You can copy and paste this code into your favorite IDE
 **/

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;


public class Terminal {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="executionBox"
    private TextField executionBox; // Value injected by FXMLLoader

    @FXML // fx:id="langaugeIcon"
    private ImageView langaugeIcon; // Value injected by FXMLLoader

    @FXML // fx:id="langauges"
    private ComboBox<?> langauges; // Value injected by FXMLLoader

    @FXML // fx:id="outputBox"
    private TextArea outputBox; // Value injected by FXMLLoader


    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert executionBox != null : "fx:id=\"executionBox\" was not injected: check your FXML file 'Terminal.fxml'.";
        assert langaugeIcon != null : "fx:id=\"langaugeIcon\" was not injected: check your FXML file 'Terminal.fxml'.";
        assert langauges != null : "fx:id=\"langauges\" was not injected: check your FXML file 'Terminal.fxml'.";
        assert outputBox != null : "fx:id=\"outputBox\" was not injected: check your FXML file 'Terminal.fxml'.";

        // Initialize your logic here: all @FXML variables will have been injected

    }

}

