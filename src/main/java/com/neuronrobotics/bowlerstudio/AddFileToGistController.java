package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.IScriptingLanguage;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;

import eu.mihosoft.vrl.v3d.JavaFXInitializer;
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
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.eclipse.jgit.lib.Repository;
import org.kohsuke.github.GHRepository;

/**
 * Created by Ryan Benasutti on 2/6/2016.
 */
@SuppressWarnings("restriction")
public class AddFileToGistController extends Application {
	@FXML
	public TextField filenameField;
	@FXML
	public TextField repoName;
	@FXML
	private ComboBox<String> projects;
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
	@FXML
	private AnchorPane newProject;

	@FXML
	private AnchorPane addFile;

	private MenuRefreshEvent refreshevent;
	private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
	private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

	public static String toSlug(String input) {
		String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
		String normalized = Normalizer.normalize(nowhitespace, Form.NFD);
		String slug = NONLATIN.matcher(normalized).replaceAll("");
		return slug.toLowerCase(Locale.ENGLISH);
	}
	// private GHGist gistID;

	public AddFileToGistController(String gitRepo, MenuRefreshEvent event) {
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
		if (gitRepo != null) {
			newProject.getChildren().clear();
		} else {
			addFile.setDisable(true);
		}
		List<String> langs = ScriptingEngine.getAllLangauges();
		ObservableList<String> options = FXCollections.observableArrayList(langs);
		//
		for (String s : options) {
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

				langaugeIcon.setImage(AssetFactory.loadAsset("Script-Tab-" + extention.getSelectionModel().getSelectedItem() + ".png"));
				String key = extention.getSelectionModel().getSelectedItem();
				IScriptingLanguage l = ScriptingEngine.getLangaugesMap().get(key);
				if (l != null) {
					extentionStr = "." + l.getFileExtenetion().get(0);
				} else
					extentionStr = ".groovy";
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		});

		Platform.runLater(() -> {
			primaryStage.setTitle("Add File to Git Repo " + gitRepo);

			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.initModality(Modality.WINDOW_MODAL);
			primaryStage.setResizable(true);
			primaryStage.show();
		});
	}

	@FXML
	public void onAddFile(ActionEvent event) {
		new Thread(() -> {
			Platform.runLater(() -> {
				Stage stage = (Stage) addFileButton.getScene().getWindow();
				stage.close();
			});
			String text = filenameField.getText();
			if (!text.endsWith(extentionStr)) {
				text = text + extentionStr;
			}

			String message = description.getText();
			if (message == null || message.length() == 0) {
				message = text;
			}

			if (gitRepo == null) {
				gitRepo = GistHelper.createNewGist(text, message, true);
			}
			System.out.println("Adding new file" + text + " to " + gitRepo);
			try {

				String defaultContents = ScriptingEngine.getLangaugeByExtention(extentionStr).getDefaultContents();
				String fullBranch = ScriptingEngine.getFullBranch(gitRepo);
				if(fullBranch==null)
					fullBranch=ScriptingEngine.newBranch(gitRepo, "main");
				ScriptingEngine.pushCodeToGit(gitRepo, fullBranch, text, defaultContents,
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

	public static void main(String[] args) {
		JavaFXInitializer.go();

		Platform.runLater(() -> {
			Stage s = new Stage();
			new Thread(() -> {
				String url = "https://github.com/madhephaestus/TestRepo.git";
				url=null;
				AddFileToGistController controller = new AddFileToGistController(url, new MenuRefreshEvent() {
					@Override
					public void setToLoggedIn() {
						// TODO Auto-generated method stub

					}

				});

				try {
					controller.start(s);
					// setToLoggedIn("");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}).start();
		});
	}

	@FXML
	void createProject(ActionEvent event) {
		try {
			String text = description.getText();
			if(text==null || text.length()<5) {
				text="Project "+repoName.getText();
			}
			GHRepository repository = ScriptingEngine.makeNewRepo(toSlug(repoName.getText()), text);
			gitRepo= repository.getHttpTransportUrl();
			newProject.setDisable(true);
			addFile.setDisable(false);
		}catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
