package com.neuronrobotics.nrconsole.util;

import java.util.ArrayList;
import java.util.Optional;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.assets.FontSizeManager;
import com.neuronrobotics.bowlerstudio.creature.IGistPromptCompletionListener;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

public class PromptForGit {
	private PromptForGit() {
	}

	public static void prompt(String purpose, String defaultID, IGistPromptCompletionListener listener){
		BowlerStudio.runLater(() -> {
			TextInputDialog alert = new TextInputDialog(defaultID);
			alert.setTitle(purpose);
			alert.setHeaderText("Enter the URL (Clone vie HTTPS)");
			alert.setContentText("Git Clone URL: ");
			alert.setResizable(true);
			alert.setWidth(800);
			Node rt = alert.getDialogPane();
			Stage st = (Stage) alert.getDialogPane().getScene().getWindow();
			st.setOnCloseRequest(ev -> alert.hide());
			FontSizeManager.addListener(fontNum -> {
				int tmp = fontNum - 10;
				if (tmp < 12)
					tmp = 12;
				rt.setStyle("-fx-font-size: " + tmp + "pt");
				alert.getDialogPane().applyCss();
				alert.getDialogPane().layout();
				st.sizeToScene();
			});
			// Traditional way to get the response value.
			Optional<String> result = alert.showAndWait();
			if (result.isPresent()){
			   
			    String gistcode=null;
			    if(result.get().endsWith(".git"))
			    	gistcode=result.get();
			    else
			    	gistcode= "https://gist.github.com/"+ScriptingEngine.urlToGist(result.get())+".git";
			    System.out.println("Creature Git " + gistcode);
			    ArrayList<String> choices;
			    String suggestedChoice="";
			    int numXml=0;
				try {
					choices = ScriptingEngine.filesInGit(gistcode);
				    for(int i=0;i<choices.size();i++){
				    	String s = choices.get(i);
			    		suggestedChoice=s;
			    		numXml++;
				    	
				    }
				    ChoiceDialog<String> d = new ChoiceDialog<>(suggestedChoice, choices);
				    d.setTitle("Choose a file in the git");
				    d.setHeaderText("Select from the files in the git to pick the Creature File");
				    d.setContentText("Choose A Creature:");
				    Node root = d.getDialogPane();
					Stage stage = (Stage) d.getDialogPane().getScene().getWindow();
					stage.setOnCloseRequest(ev -> d.hide());
					FontSizeManager.addListener(fontNum -> {
						int tmp = fontNum - 10;
						if (tmp < 12)
							tmp = 12;
						root.setStyle("-fx-font-size: " + tmp + "pt");
						d.getDialogPane().applyCss();
						d.getDialogPane().layout();
						stage.sizeToScene();
					});
				    // Traditional way to get the response value.
				    Optional<String> r = d.showAndWait();
				    if (r.isPresent()){
				        System.out.println("Your choice: " + r.get());
				        listener.done(gistcode,r.get());
				    }
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    if(numXml ==1){
			    	//System.out.println("Found just one file at  " + suggestedChoice);
			    	//loadMobilebaseFromGist(gistcode,suggestedChoice);
			    	//return;
			    	
			    }

			}
			
		});
	}
	
}
