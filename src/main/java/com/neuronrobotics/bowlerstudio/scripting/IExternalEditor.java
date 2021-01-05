package com.neuronrobotics.bowlerstudio.scripting;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import com.neuronrobotics.bowlerstudio.BowlerStudio;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;

public interface IExternalEditor {
	
	boolean isSupportedByExtention(File file);
	
	void launch(File file, Button advanced);
	
	String nameOfEditor();
	
	URL getInstallURL() throws MalformedURLException;
	
	void onProcessExit(int ev);
	
	
	default void run(File dir,String... finalCommand) {
		List<String> asList = Arrays.asList(finalCommand);
		String command ="";
		System.out.println("Running:\n\n");
		for(String s:asList)
			command+=(s+" ");
		String cmd = command;
		System.out.println(command);
		System.out.println("\nIn "+dir.getAbsolutePath());
		System.out.println("\n\n");
		
		new Thread(() -> {
			try {
				// creating the process
				
				ProcessBuilder pb = new ProcessBuilder(asList);
				// setting the directory
				pb.directory(dir);
				// startinf the process
				Process process = pb.start();
				
				// for reading the ouput from stream
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
				BufferedReader errInput = new BufferedReader(new InputStreamReader(process.getErrorStream()));

				String s = null;
				String e = null;
				Thread.sleep(100);
				while ((s = stdInput.readLine()) != null || (e = errInput.readLine()) != null) {
					if (s != null)
						System.err.println(s);
					if (e != null)
						System.err.println(e);
					//
				}
				process.waitFor();
				int ev = process.exitValue();
				// System.out.println("Running "+commands);
				if (ev != 0) {
					System.out.println("ERROR PROCESS Process exited with " + ev);
				}
				while (process.isAlive()) {
					Thread.sleep(100);
				}
				System.out.println("");
				onProcessExit(ev);
					
			} catch (Throwable e) {
				
				Platform.runLater(()->{
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle(nameOfEditor()+" is missing");
					alert.setHeaderText("failed to run "+cmd);
					alert.setContentText("Close to bring me to the install website");
					alert.showAndWait();
					new Thread(() -> {
						try {
							BowlerStudio.openExternalWebpage(getInstallURL());
						} catch (MalformedURLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}).start();
				});
			

			}
		}).start();
	}

}
