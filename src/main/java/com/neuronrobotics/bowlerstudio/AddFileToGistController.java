package com.neuronrobotics.bowlerstudio;

import java.io.File;
import java.util.List;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Created by Ryan Benasutti on 2/6/2016.
 */

public class AddFileToGistController extends Application {
	@FXML
	public TextField filenameField;

	@FXML
	public Button addFileButton, cancelButton;
	@FXML
    private ComboBox<String> extention;
	@FXML // fx:id="langaugeIcon"
	private ImageView langaugeIcon; // Value injected by FXMLLoader
	private String extentionStr = ".groovy";
	private String gitRepo;

	// private GHGist gistID;

	public AddFileToGistController(String gitRepo) {
		this.gitRepo = gitRepo;
		// this.gistID = id;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = AssetFactory.loadLayout("layout/addFileToGist.fxml", true);
		Parent root;
		loader.setController(this);
		loader.setClassLoader(getClass().getClassLoader());
		root = loader.load();
		extention.getItems().clear();
		List<String> langs = ScriptingEngine.getAllLangauges();
		ObservableList<String> options = FXCollections.observableArrayList(langs);
		//
		for(String s:options){
			extention.getItems().add(s);
		}
		extention.getSelectionModel().select("Groovy");
		Image icon;
		try {
			icon = AssetFactory.loadAsset("Script-Tab-" + extention.getSelectionModel().getSelectedItem() + ".png");
			langaugeIcon.setImage(icon);
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		extention.setOnAction(event -> {
			try {

				langaugeIcon.setImage(AssetFactory
						.loadAsset("Script-Tab-" + extention.getSelectionModel().getSelectedItem() + ".png"));
				switch(extention.getSelectionModel().getSelectedItem()){
				case "Groovy":
					extentionStr=".groovy";
				case "Clojure":
					extentionStr=".clj";
				case "Jython":
					extentionStr=".py";
				case "Arduino":
					extentionStr=".ino";
				case "JSON":
					extentionStr=".json";
					break;
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		});

		Platform.runLater(() -> {
			primaryStage.setTitle("Add File to Gist");

			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.initModality(Modality.WINDOW_MODAL);
			primaryStage.setResizable(true);
			primaryStage.show();
		});
	}

	@FXML
	public void onAddFile(ActionEvent event) {
		
		String text = filenameField.getText();
		if(!text.endsWith(extentionStr)){
			text=text+extentionStr;
		}
		System.out.println("Adding new file"+text+" to "+gitRepo);
		try {
			ScriptingEngine.pushCodeToGit(gitRepo, "master", text, "//Your code here",
					"Adding new file from BowlerStudio");
			File nf = ScriptingEngine.fileFromGit(gitRepo, text);
			BowlerStudio.createFileTab(nf);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Platform.runLater(() -> {
			Stage stage = (Stage) addFileButton.getScene().getWindow();
			stage.close();
		});
	}

	@FXML
	public void onCancel(ActionEvent event) {
		Platform.runLater(() -> {
			Stage stage = (Stage) cancelButton.getScene().getWindow();
			stage.close();
		});
	}
}
