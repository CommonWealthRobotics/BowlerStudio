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
			TextInputDialog dialog = new TextInputDialog("https://gist.github.com/"+defaultID);
			dialog.setTitle(purpose);
			dialog.setHeaderText("Enter the URL (Link from the browser)");
			dialog.setContentText("Link to Gist: ");

			// Traditional way to get the response value.
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent()){
			   
			    String gistcode = ScriptingEngine.urlToGist(result.get());
			    System.out.println("Creature Gist " + gistcode);
			    ArrayList<String> choices =ScriptingEngine.filesInGist(gistcode,null);
			    String suggestedChoice="";
			    int numXml=0;
			    for(int i=0;i<choices.size();i++){
			    	String s = choices.get(i);
		    		suggestedChoice=s;
		    		numXml++;
			    	
			    }
			    
			    if(numXml ==1){
			    	//System.out.println("Found just one file at  " + suggestedChoice);
			    	//loadMobilebaseFromGist(gistcode,suggestedChoice);
			    	//return;
			    	
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
			}
			
		});
	}
	
}
