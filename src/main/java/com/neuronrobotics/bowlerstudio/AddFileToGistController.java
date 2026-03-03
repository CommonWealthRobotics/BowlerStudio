package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.assets.FontSizeManager;
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
import javafx.scene.Node;
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
	private ComboBox<String> extension;
	@FXML // fx:id="langaugeIcon"
	private ImageView langaugeIcon; // Value injected by FXMLLoader
	private String extensionStr = ".groovy";
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
	private String forcedType=null;
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
		FontSizeManager.addListener(fontNum->{
			int tmp = fontNum-10;
			if(tmp<12)
				tmp=12;
			root.setStyle("-fx-font-size: "+tmp+"pt");
		});
		extension.getItems().clear();
		if (getGitRepo() != null) {
			newProject.getChildren().clear();
		} else {
			addFile.setDisable(true);
		}
		List<String> langs = ScriptingEngine.getAllLangauges();
		ObservableList<String> options = FXCollections.observableArrayList(langs);
		//
		for (String s : options) {
			extension.getItems().add(s);
		}
		extension.getSelectionModel().select("Groovy");
		Image icon;
		String asset = "Script-Tab-" + extension.getSelectionModel().getSelectedItem() + ".png";

		try {

			icon = AssetFactory.loadAsset(asset);
			langaugeIcon.setImage(icon);
			FontSizeManager.addListener(fontNum->{
		    	  langaugeIcon.setScaleX(FontSizeManager.getImageScale());
		    	  langaugeIcon.setScaleY(FontSizeManager.getImageScale());
		      });
		} catch (Exception e2) {
			// Auto-generated catch block
			e2.printStackTrace();
		}

		extension.setOnAction(event -> {
			try {

				String selectedItem = extension.getSelectionModel().getSelectedItem();
				setSelected(selectedItem);
			} catch (Exception e1) {
				// Auto-generated catch block
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

	private void setSelected(String selectedItem) throws Exception {
		String file = "Script-Tab-" + selectedItem + ".png";
		Image loadAsset = AssetFactory.loadAsset(file);
		try {
		langaugeIcon.setImage(loadAsset);
		
		}catch(Throwable t) {
			com.neuronrobotics.sdk.common.Log.error(t);
		}
		String key = selectedItem;
		IScriptingLanguage l = ScriptingEngine.getLangaugesMap().get(key);
		
		if (l != null) {
			extensionStr = l.getFileExtension().get(0);
		} else
			extensionStr = ".groovy";
		if(!extensionStr.startsWith(".")) {
			extensionStr="."+extensionStr;
		}
		isArduino = ArduinoLoader.class.isInstance(l);

		setGitRepo(gitRepo);
	}

	@FXML
	public void onAddFile(ActionEvent event) {
		new Thread(() -> {
			BowlerStudio.runLater(() -> {
				Stage stage = (Stage) addFileButton.getScene().getWindow();
				stage.close();
			});
			String filename = filenameField.getText();

			if (!filename.endsWith(extensionStr)) {
				filename = filename + extensionStr;
			}
			String fileSlug = filename.replace(extensionStr, "");
			String message = description.getText();
			if (message == null || message.length() == 0) {
				message = filename;
			}

			if (getGitRepo() == null) {
				setGitRepo(GistHelper.createNewGist(filename, message, true));
			}
			com.neuronrobotics.sdk.common.Log.error("Adding new file" + filename + " to " + getGitRepo());
			try {
				ScriptingEngine.pull(getGitRepo());
				//String defaultContents = 
				String fullBranch = ScriptingEngine.getFullBranch(getGitRepo());
				if (fullBranch == null)
					fullBranch = ScriptingEngine.newBranch(getGitRepo(), "main");
				ScriptingEngine.getLangaugeByExtension(extensionStr).getDefaultContents(getGitRepo(), filename );
				ScriptingEngine.pushCodeToGit(getGitRepo(), fullBranch, filename, null, message);
				File nf = ScriptingEngine.fileFromGit(getGitRepo(), filename);
				try {
					BowlerStudio.createFileTab(nf);
				}catch(Exception ex) {
					ex.printStackTrace();
				}
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
						// Auto-generated method stub

					}

				});

				try {
					controller.start(s);
					// setToLoggedIn("");
				} catch (Exception e) {
					com.neuronrobotics.sdk.common.Log.error(e);
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
						Node root = alert.getDialogPane();
						Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
						stage.setOnCloseRequest(ev -> alert.hide());
						FontSizeManager.addListener(fontNum -> {
							int tmp = fontNum - 10;
							if (tmp < 12)
								tmp = 12;
							root.setStyle("-fx-font-size: " + tmp + "pt");
							alert.getDialogPane().applyCss();
							alert.getDialogPane().layout();
							stage.sizeToScene();
						});
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
				// Auto-generated catch block
				com.neuronrobotics.sdk.common.Log.error(e);
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

	public void setFileExtensionType(IScriptingLanguage lang) {
		String string = lang.getShellType();
		try {
			setSelected(string);
			BowlerStudio.runLater(() -> {
				extension.setValue(string);
				extension.setDisable(true);
			});
		} catch (Exception e) {
			// Auto-generated catch block
			com.neuronrobotics.sdk.common.Log.error(e);
		}

	}

	public void start(Stage s, IScriptingLanguage iScriptingLanguage) throws Exception {
		start(s);
		BowlerStudio.runLater(()->{
			setFileExtensionType(iScriptingLanguage);
		});
	}
}
