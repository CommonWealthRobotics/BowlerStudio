package com.neuronrobotics.nrconsole.util;

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import org.eclipse.jgit.api.Git;
import org.jfree.util.Log;

import java.io.File;
import java.util.Optional;

public class CommitWidget {
	public static void commit(File currentFile, String code){
		if(code.length()<1){
			Log.error("COmmit failed with no code to commit");
			return;
		}
		Platform.runLater(() ->{
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
			Platform.runLater(() -> username.requestFocus());
	
			// Convert the result to a username-password-pair when the login button is clicked.
			dialog.setResultConverter(dialogButton -> {
			    if (dialogButton == loginButtonType) {
			        return new Pair<>(username.getText(), password.getText());
			    }
			    return null;
			});

			
			Optional<Pair<String, String>> result = dialog.showAndWait();

			result.ifPresent(commitBody -> {
			    new Thread(){
			    	public void run(){
					    String message = commitBody.getKey()+"\n\n"+commitBody.getValue();
					    
					    Git git;
						try {
							git = ScriptingEngine.locateGit(currentFile);
							String remote= git.getRepository().getConfig().getString("remote", "origin", "url");
							String relativePath = ScriptingEngine.findLocalPath(currentFile,git);
						    ScriptingEngine.pushCodeToGit(remote,ScriptingEngine.getFullBranch(remote), relativePath, code, message);
						    git.close();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
			    	}
			    }.start();
			});
		});
	}

}
