package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.ArduinoLoader;
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
import javafx.scene.control.Alert;
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
	private boolean isArduino;
	private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
	private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

	public static String toSlug(String input) {
		String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
		String normalized = Normalizer.normalize(nowhitespace, Form.NFD);
		String slug = NONLATIN.matcher(normalized).replaceAll("");
		return slug.replaceAll("[^a-zA-Z0-9]", "");
	}
	// private GHGist gistID;

	public AddFileToGistController(String gitRepo, MenuRefreshEvent event) {
		this.setGitRepo(gitRepo);
		// this.gistID = id;
		this.refreshevent = event;

	}

	@SuppressWarnings("restriction")
	@Override
	public void start(Stage primaryStage) throws Exception {
		isArduino = false;
		FXMLLoader loader = AssetFactory.loadLayout("layout/addFileToGist.fxml", true);
		Parent root;
		loader.setController(this);
		// This is needed when loading on MAC
		loader.setClassLoader(getClass().getClassLoader());
		root = loader.load();
		extention.getItems().clear();
		if (getGitRepo() != null) {
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

				langaugeIcon.setImage(AssetFactory
						.loadAsset("Script-Tab-" + extention.getSelectionModel().getSelectedItem() + ".png"));
				String key = extention.getSelectionModel().getSelectedItem();
				IScriptingLanguage l = ScriptingEngine.getLangaugesMap().get(key);
				
				if (l != null) {
					extentionStr = l.getFileExtenetion().get(0);
				} else
					extentionStr = ".groovy";
				if(!extentionStr.startsWith(".")) {
					extentionStr="."+extentionStr;
				}
				isArduino = ArduinoLoader.class.isInstance(l);

				setGitRepo(gitRepo);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		});

		BowlerStudio.runLater(() -> {
			primaryStage.setTitle("Add File to Git Repo " + getGitRepo());

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
			BowlerStudio.runLater(() -> {
				Stage stage = (Stage) addFileButton.getScene().getWindow();
				stage.close();
			});
			String filename = filenameField.getText();

			if (!filename.endsWith(extentionStr)) {
				filename = filename + extentionStr;
			}
			String fileSlug = filename.replace(extentionStr, "");
			String message = description.getText();
			if (message == null || message.length() == 0) {
				message = filename;
			}

			if (getGitRepo() == null) {
				setGitRepo(GistHelper.createNewGist(filename, message, true));
			}
			System.out.println("Adding new file" + filename + " to " + getGitRepo());
			try {
				ScriptingEngine.pull(getGitRepo());
				String defaultContents = ScriptingEngine.getLangaugeByExtention(extentionStr).getDefaultContents(getGitRepo(), fileSlug );
				String fullBranch = ScriptingEngine.getFullBranch(getGitRepo());
				if (fullBranch == null)
					fullBranch = ScriptingEngine.newBranch(getGitRepo(), "main");
				
				ScriptingEngine.pushCodeToGit(getGitRepo(), fullBranch, filename, defaultContents, message);
				File nf = ScriptingEngine.fileFromGit(getGitRepo(), filename);

				BowlerStudio.createFileTab(nf);

				refreshevent.setToLoggedIn();
			} catch (Exception e) {
				new IssueReportingExceptionHandler().except(e);
			}

		}).start();
	}

	@FXML
	public void onCancel(ActionEvent event) {
		BowlerStudio.runLater(() -> {
			Stage stage = (Stage) cancelButton.getScene().getWindow();
			stage.close();
		});
	}

	public static void main(String[] args) {
		JavaFXInitializer.go();

		BowlerStudio.runLater(() -> {
			Stage s = new Stage();
			new Thread(() -> {
				String url = "https://github.com/madhephaestus/TestRepo.git";
				// url = null;
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
		BowlerStudio.runLater(() -> {
			newProject.setDisable(true);
		});
		new Thread(() -> {
			try {
				String text = description.getText();
				if (text == null || text.length() < 5) {
					text = "Project " + repoName.getText();
				}
				String txt = repoName.getText();
				String slugVer = toSlug(txt);
				if (!txt.contentEquals(slugVer)) {
					BowlerStudio.runLater(() -> {
						repoName.setText(slugVer);
						Alert alert = new Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
						alert.setContentText("Repository Name must Valid: " + slugVer);
						alert.showAndWait();
						BowlerStudio.runLater(() -> {
							newProject.setDisable(false);
						});
					});
					return;
				}
				GHRepository repository = ScriptingEngine.makeNewRepo(toSlug(repoName.getText()), text);
				setGitRepo(repository.getHttpTransportUrl());
				BowlerStudio.runLater(() -> {
					addFile.setDisable(false);
				});

			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}).start();

	}

	public String getGitRepo() {
		return gitRepo;
	}

	public void setGitRepo(String gitRepo) {
		this.gitRepo = gitRepo;
		if (gitRepo != null) {
			String dirName = ScriptingEngine.getRepositoryCloneDirectory(gitRepo).getName();
			if (filenameField != null)
				BowlerStudio.runLater(() -> {

					filenameField.setDisable(isArduino);
					filenameField.setText(dirName);
				});
		}
	}
}
