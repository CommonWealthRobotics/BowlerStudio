package com.neuronrobotics.bowlerstudio;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import org.kohsuke.github.GHRepository;
//import org.kohsuke.github.GHWorkflow;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.PasswordManager;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.util.ThreadUtil;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextFormatter.Change;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import javafx.scene.control.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.collections.*;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
public class MakeReleaseController extends Application {

	private String gitRepo;

	@FXML // ResourceBundle that was given to the FXMLLoader
	private ResourceBundle resources;

	@FXML // URL location of the FXML file that was given to the FXMLLoader
	private URL location;

	@FXML // fx:id="bugfix"
	private TextField bugfix; // Value injected by FXMLLoader

	@FXML // fx:id="listOfTags"
	private ListView<String> listOfTags; // Value injected by FXMLLoader

	@FXML // fx:id="major"
	private TextField major; // Value injected by FXMLLoader

	@FXML // fx:id="minor"
	private TextField minor; // Value injected by FXMLLoader

	@FXML // fx:id="releaseButton"
	private Button releaseButton; // Value injected by FXMLLoader

	@FXML // fx:id="tagName"
	private Label tagName; // Value injected by FXMLLoader
	private List<String> tags;
	Stage primaryStage;

	@FXML
	void makeRelease(ActionEvent event) {
		String newTag = getNewTag();
		new Thread(() -> {

			File dir = ScriptingEngine.getRepositoryCloneDirectory(gitRepo);
			File workflows = new File(dir.getAbsolutePath() + delim() + ".github" + delim() + "workflows");
			boolean hasWorkflow = false;

			if(workflows.exists())
				hasWorkflow = true;

			Object st[];
			try {
				st = ScriptingEngine.filesInGit(gitRepo).toArray();
				if (!hasWorkflow) {
					CreatenewWorkflow(event, newTag, st);
				} else {
					ScriptingEngine.tagRepo(gitRepo, newTag);
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}).start();

		BowlerStudio.runLater(() -> {
			primaryStage.close();
		});
	}

	private void CreatenewWorkflow(ActionEvent event, String newTag, Object[] st) {
		BowlerStudio.runLater(() -> {
			ChoiceDialog d = new ChoiceDialog(st[0], st);
			d.setTitle("Choose File From this Repo to release");
			d.setHeaderText("Select file to compile in CI");
			d.setContentText("File:");
			// show the dialog
			d.showAndWait();
			String selectedItem = (String) d.getSelectedItem();

			new Thread(() -> {
				String filename = selectedItem.split("\\.")[0];
				System.out.println(selectedItem + " selected");
				String fileContents;
				try {
					fileContents = ScriptingEngine.codeFromGit("https://github.com/CommonWealthRobotics/Bowler-Script-Release-CI.git", "TEMPLATE.job")[0];
					fileContents=fileContents.replaceAll("FILENAME_REPLACE", selectedItem);
					fileContents=fileContents.replaceAll("JOBNAME_REPLACE", filename);
					try {
						createWorkflow(event, fileContents);
						ThreadUtil.wait(1000);
						ScriptingEngine.tagRepo(gitRepo, newTag);
						return;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}).start();
		});
	}

	private void createWorkflow(ActionEvent event, String fileContents) throws Exception, IOException {

		ScriptingEngine.pushCodeToGit(gitRepo, null, ".github/workflows/bowler.yml", fileContents, "Creating workflow");
		//GHWorkflow wf = getWorkflow(gitRepo);
		makeRelease(event);
	}

//	private GHWorkflow getWorkflow(String repoURL) throws IOException {
//		File repoDir = ScriptingEngine.getRepositoryCloneDirectory(repoURL);
//		String Project = repoDir.getParentFile().getName();
//		String Repo = repoDir.getName();
//		GHRepository repo = PasswordManager.getGithub().getRepository(Project + "/" + Repo);
//		GHWorkflow workflow = repo.getWorkflow("bowler.yml");
//		if (!workflow.getState().equals("active")) {
//			workflow.enable();
//		}
//		return workflow;
//	}

	private String delim() {
		return System.getProperty("file.separator");
	}

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		assert bugfix != null : "fx:id=\"bugfix\" was not injected: check your FXML file 'release.fxml'.";
		assert listOfTags != null : "fx:id=\"listOfTags\" was not injected: check your FXML file 'release.fxml'.";
		assert major != null : "fx:id=\"major\" was not injected: check your FXML file 'release.fxml'.";
		assert minor != null : "fx:id=\"minor\" was not injected: check your FXML file 'release.fxml'.";
		assert releaseButton != null : "fx:id=\"releaseButton\" was not injected: check your FXML file 'release.fxml'.";
		assert tagName != null : "fx:id=\"tagName\" was not injected: check your FXML file 'release.fxml'.";
		UnaryOperator<Change> filter = change -> {
			String text = change.getText();
			if (text.matches("[0-9]*")) {
				return change;
			}
			return null;
		};
		major.setTextFormatter(new TextFormatter<String>(filter));
		minor.setTextFormatter(new TextFormatter<String>(filter));
		bugfix.setTextFormatter(new TextFormatter<String>(filter));
		major.textProperty().addListener((obs, old, niu) -> {
			check();
		});
		minor.textProperty().addListener((obs, old, niu) -> {
			check();
		});
		bugfix.textProperty().addListener((obs, old, niu) -> {
			check();
		});
		releaseButton.setDisable(true);

	}

	private void check() {
		if (major.getText().length() == 0 || minor.getText().length() == 0 || bugfix.getText().length() == 0) {
			return;
		}
		String newTag = getNewTag();
		for (String s : tags) {
			if (s.contains(newTag)) {
				tagName.setText("error: tag exists");
				for (String Item : listOfTags.getItems()) {
					if (Item.contentEquals(s)) {
						listOfTags.getSelectionModel().select(s);
					}
				}
				return;
			}
		}

		tagName.setText(newTag);
		releaseButton.setDisable(false);
	}

	private String getNewTag() {
		return major.getText() + "." + minor.getText() + "." + bugfix.getText();
	}

	public MakeReleaseController(String gitRepo) {
		this.gitRepo = gitRepo;

	}

	@SuppressWarnings("restriction")
	@Override
	public void start(Stage st) throws Exception {
		primaryStage = st;
		FXMLLoader loader = AssetFactory.loadLayout("layout/release.fxml", true);
		Parent root;
		loader.setController(this);
		// This is needed when loading on MAC
		loader.setClassLoader(getClass().getClassLoader());
		root = loader.load();
		tags = ScriptingEngine.getAllTags(gitRepo);
		for (String s : tags) {
			listOfTags.getItems().add(s);
		}
		if (tags.size() > 0) {
			String topValue = tags.get(0);
			String[] top = topValue.split("\\.");
			String majorStart = top[0];
			String minorStart = top[1];
			String bugStart = "" + (Integer.parseInt(top[2].split("-")[0]) + 1);
			major.setText(majorStart);
			minor.setText(minorStart);
			bugfix.setText(bugStart);
		}

		BowlerStudio.runLater(() -> {
			primaryStage.setTitle("Release for " + gitRepo);

			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.initModality(Modality.WINDOW_MODAL);
			primaryStage.setResizable(true);
			check();
			primaryStage.show();
		});

	}

}
