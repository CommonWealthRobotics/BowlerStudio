package com.neuronrobotics.bowlerstudio;
/**
 * Sample Skeleton for 'newVitaminWizard.fxml' Controller Class
 */

import java.net.URL;
import java.util.ResourceBundle;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.sdk.addons.kinematics.JavaFXInitializer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class NewVitaminWizardController  extends Application {
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="typePane"
    private AnchorPane typePane; // Value injected by FXMLLoader

    @FXML // fx:id="x1"
    private Font x1; // Value injected by FXMLLoader

    @FXML // fx:id="x2"
    private Color x2; // Value injected by FXMLLoader

    @FXML // fx:id="existingTypeRadio"
    private RadioButton existingTypeRadio; // Value injected by FXMLLoader

    @FXML // fx:id="newTypeRadio"
    private RadioButton newTypeRadio; // Value injected by FXMLLoader

    @FXML // fx:id="typeComboBox"
    private ComboBox<String> typeComboBox; // Value injected by FXMLLoader

    @FXML // fx:id="sizePane"
    private AnchorPane sizePane; // Value injected by FXMLLoader

    @FXML // fx:id="sizeComboBox"
    private ComboBox<String> sizeComboBox; // Value injected by FXMLLoader

    @FXML // fx:id="measurmentPane"
    private AnchorPane measurmentPane; // Value injected by FXMLLoader

    @FXML // fx:id="measurmentsTable"
    private TableView<String> measurmentsTable; // Value injected by FXMLLoader
    
	private static INewVitaminCallback callback=null;

	private static Stage primaryStage;

	@FXML
    void onConfirmAndCreate(ActionEvent event) {
		try {
			this.primaryStage.close();
			primaryStage=null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new IssueReportingExceptionHandler().uncaughtException(Thread.currentThread(), e);
			
		}
    }

    @FXML
    void onConfirmSize(ActionEvent event) {
    	sizePane.setDisable(true);
        measurmentPane.setDisable(false);
        typePane.setDisable(true);
    }

    @FXML
    void onConfirmType(ActionEvent event) {
    	sizePane.setDisable(false);
        measurmentPane.setDisable(true);
        typePane.setDisable(true);
    }

    @FXML
    void onNewMeasurment(ActionEvent event) {

    }

    @FXML
    void onSelectExistingTypeMode(ActionEvent event) {

    }

    @FXML
    void onSelectNewTypeMode(ActionEvent event) {

    }

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert x1 != null : "fx:id=\"x1\" was not injected: check your FXML file 'newVitaminWizard.fxml'.";
        assert x2 != null : "fx:id=\"x2\" was not injected: check your FXML file 'newVitaminWizard.fxml'.";
        assert existingTypeRadio != null : "fx:id=\"existingTypeRadio\" was not injected: check your FXML file 'newVitaminWizard.fxml'.";
        assert newTypeRadio != null : "fx:id=\"newTypeRadio\" was not injected: check your FXML file 'newVitaminWizard.fxml'.";
        assert typeComboBox != null : "fx:id=\"typeComboBox\" was not injected: check your FXML file 'newVitaminWizard.fxml'.";
        assert sizeComboBox != null : "fx:id=\"sizeComboBox\" was not injected: check your FXML file 'newVitaminWizard.fxml'.";
        assert measurmentsTable != null : "fx:id=\"measurmentsTable\" was not injected: check your FXML file 'newVitaminWizard.fxml'.";
        
        sizePane.setDisable(true);
        measurmentPane.setDisable(true);
        
    }
    
    public static void launchWizard(INewVitaminCallback callback) throws Exception {
    	NewVitaminWizardController.callback=callback;
		Platform.runLater(() -> {
			Stage s = new Stage();
			primaryStage = s;
			new Thread(() -> {
				NewVitaminWizardController controller = new NewVitaminWizardController();
				
				try {
					controller.start(s);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}).start();
		});
    }

    public static void main(String [] args) throws Exception {
    	JavaFXInitializer.go();
    	NewVitaminWizardController.launchWizard(new INewVitaminCallback() {
			
			@Override
			public Menu getTypeMenu(String type) {
				System.out.println("Get Vitamin Menu");

				return new Menu(type);
			}
			
			@Override
			public void addVitaminType(String s) {
				// TODO Auto-generated method stub
				System.out.println("Add addVitaminType");
			}
			
			@Override
			public void addSizesToMenu(String size, String type) {
				System.out.println("Add addSizesToMenu");
			}
		});
    }
    
	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = AssetFactory.loadLayout("layout/newVitaminWizard.fxml");
		Parent root;
		//loader.setController(this);
		// This is needed when loading on MAC
		loader.setClassLoader(getClass().getClassLoader());
		root = loader.load();
		Platform.runLater(() -> {
			primaryStage.setTitle("New Vitamin Wizard");

			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.initModality(Modality.WINDOW_MODAL);
			primaryStage.setResizable(true);
			primaryStage.show();
		});
	}
}
