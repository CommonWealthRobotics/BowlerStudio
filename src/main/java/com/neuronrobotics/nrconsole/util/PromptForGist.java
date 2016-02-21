package com.neuronrobotics.nrconsole.util;

import java.util.ArrayList;
import java.util.Optional;

import com.neuronrobotics.bowlerstudio.creature.IGistPromptCompletionListener;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;

import javafx.application.Platform;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;

public class PromptForGist {
	public static void prompt(String purpose,String defaultID, IGistPromptCompletionListener listener){
		Platform.runLater(() -> {
			TextInputDialog dialog = new TextInputDialog(defaultID);
			dialog.setTitle(purpose);
			dialog.setHeaderText("Enter the URL (Clone vie HTTPS)");
			dialog.setContentText("Link to Gist: ");
			dialog.setResizable(true);
			dialog.setWidth(800);
			// Traditional way to get the response value.
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent()){
			   
			    String gistcode=null;
			    if(result.get().endsWith(".git"))
			    	gistcode=result.get();
			    else
			    	gistcode= "https://gist.github.com/"+ScriptingEngine.urlToGist(result.get())+".git";
			    System.out.println("Creature Gist " + gistcode);
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
				    d.setTitle("Choose a file in the gist");
				    d.setHeaderText("Select from the files in the gist to pick the Creature File");
				    d.setContentText("Choose A Creature:");

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
