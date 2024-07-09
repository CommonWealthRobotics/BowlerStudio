package com.neuronrobotics.nrconsole.util;

import java.io.File;
import java.util.Optional;

import org.eclipse.jgit.api.Git;
import org.jfree.util.Log;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.IssueReportingExceptionHandler;
import com.neuronrobotics.bowlerstudio.assets.FontSizeManager;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.video.OSUtil;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

public class CommitWidget {
	public static void commit(File currentFile, String code){
		if(code!=null)
			if(code.length()<1){
				Log.error("COmmit failed with no code to commit");
				return;
			}
		BowlerStudio.runLater(() ->{
			// Create the custom dialog.
			Dialog<Pair<String, String>> dialog = new Dialog<>();
			dialog.setTitle("Commit message Dialog");
			dialog.setHeaderText("Enter a commit message to publish changes");
	
	
			// Set the button types.
			ButtonType loginButtonType = new ButtonType("Publish", ButtonData.OK_DONE);
			dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
	
			// Create the username and password labels and fields.
			GridPane grid = new GridPane();
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(20, 150, 10, 10));
	
			TextField username = new TextField();
			username.setPromptText("60 character description");
			TextArea password = new TextArea();
			password.setPrefRowCount(5);
			password.setPrefColumnCount(40);
			password.setPromptText("Full Sentences describing explanation");
	
			grid.add(new Label("What did you change?"), 0, 0);
			grid.add(username, 1, 0);
			grid.add(new Label("Why did you change it?"), 0, 1);
			grid.add(password, 1, 1);
	
			// Enable/Disable login button depending on whether a username was entered.
			Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
			loginButton.setDisable(true);
	
			// Do some validation (using the Java 8 lambda syntax).
			username.textProperty().addListener((observable, oldValue, newValue) -> {
			    loginButton.setDisable(newValue.trim().length()<5);
			});
	
			dialog.getDialogPane().setContent(grid);
	
			// Request focus on the username field by default.
			BowlerStudio.runLater(() -> username.requestFocus());
	
			// Convert the result to a username-password-pair when the login button is clicked.
			dialog.setResultConverter(dialogButton -> {
			    if (dialogButton == loginButtonType) {
			        return new Pair<>(username.getText(), password.getText());
			    }
			    return null;
			});
			if (OSUtil.isOSX()) {
				Modality mode = Modality.NONE;
				dialog.initModality(mode);
			}
			System.err.println("Show commit Dialog");
			Node root = dialog.getDialogPane();
			Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();

			FontSizeManager.addListener(fontNum -> {
				int tmp = fontNum - 10;
				if (tmp < 12)
					tmp = 12;
				root.setStyle("-fx-font-size: " + tmp + "pt");
				dialog.getDialogPane().applyCss();
				dialog.getDialogPane().layout();
				stage.sizeToScene();
			});
			Optional<Pair<String, String>> result = dialog.showAndWait();
			System.err.println("Commit Dialog finished");
			dialog.close();
			System.err.println("Result: "+result);
			result.ifPresent(commitBody -> {
			    new Thread(){
			    	public void run(){
						Thread.currentThread().setUncaughtExceptionHandler(new IssueReportingExceptionHandler());

					    String message = commitBody.getKey()+"\n\n"+commitBody.getValue();
					    
					    Git git=null;
						try {
							git = ScriptingEngine.locateGit(currentFile);
							String remote= git.getRepository().getConfig().getString("remote", "origin", "url");
							String relativePath = ScriptingEngine.findLocalPath(currentFile,git);
							ScriptingEngine.closeGit(git);
							ScriptingEngine.pull(remote);
						    ScriptingEngine.pushCodeToGit(remote,ScriptingEngine.getFullBranch(remote), relativePath, code, message,true);
						    
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							if(git!=null)
								ScriptingEngine.closeGit(git);

						}
			    	}
			    }.start();
			});
		});
	}

}
