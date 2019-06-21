package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.IScriptingLanguage;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

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
    @FXML
    private TextArea description;

	private MenuRefreshEvent refreshevent;

	// private GHGist gistID;

	public AddFileToGistController(String gitRepo,MenuRefreshEvent event) {
		this.gitRepo = gitRepo;
		// this.gistID = id;
		this.refreshevent = event;
	}

	@SuppressWarnings("restriction")
	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = AssetFactory.loadLayout("layout/addFileToGist.fxml", true);
		Parent root;
		loader.setController(this);
		// This is needed when loading on MAC
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
		String asset = "Script-Tab-" + extention.getSelectionModel().getSelectedItem() + ".png";
		
		try {
			
			icon = AssetFactory.loadAsset(asset);
			langaugeIcon.setImage(icon);
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		extention.setOnAction(event -> {
			try {

				langaugeIcon.setImage(AssetFactory
						.loadAsset(asset));
				String key = extention.getSelectionModel().getSelectedItem();
				IScriptingLanguage l  = ScriptingEngine
						.getLangaugesMap()
						.get(key);
				if(l!=null){
					extentionStr= "."+l.getFileExtenetion()
							.get(0);
				}
				else
					extentionStr=".groovy";
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
		new Thread(()->{
			Platform.runLater(() -> {
				Stage stage = (Stage) addFileButton.getScene().getWindow();
				stage.close();
			});
			String text = filenameField.getText();
			if(!text.endsWith(extentionStr)){
				text=text+extentionStr;
			}
			
			String message = description.getText();
			if(message == null || message.length()==0){
				message = text;
			}
			
			if(gitRepo==null){
				gitRepo=GistHelper.createNewGist(text, message, true);
			}
			System.out.println("Adding new file"+text+" to "+gitRepo);
			try {
				ScriptingEngine.pushCodeToGit(gitRepo, ScriptingEngine.getFullBranch(gitRepo), text, "//Your code here",
						message);
				File nf = ScriptingEngine.fileFromGit(gitRepo, text);
				BowlerStudio.createFileTab(nf);
				refreshevent.setToLoggedIn();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	

		}).start();
	}

	@FXML
	public void onCancel(ActionEvent event) {
		Platform.runLater(() -> {
			Stage stage = (Stage) cancelButton.getScene().getWindow();
			stage.close();
		});
	}
}
