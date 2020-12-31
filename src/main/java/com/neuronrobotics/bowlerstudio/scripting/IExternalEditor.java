package com.neuronrobotics.bowlerstudio.scripting;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public interface IExternalEditor {
	
	boolean isSupportedByExtention(File file);
	
	void launch(File file);
	
	String nameOfEditor();
	
	default void run(String finalCommand) {
		System.out.println("Running:\n\n"+finalCommand+"\n\n");
		new Thread(() -> {
			try {
				Process process = Runtime.getRuntime().exec(finalCommand);
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				String line;
				while ((line = reader.readLine()) != null && process.isAlive()) {
					System.out.println(line);
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}).start();
	}

}
