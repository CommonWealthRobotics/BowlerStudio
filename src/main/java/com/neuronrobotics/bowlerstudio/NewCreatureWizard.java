package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.bowlerstudio.scripting.IScriptingLanguage;
import com.neuronrobotics.bowlerstudio.scripting.RobotHelper;

import javafx.stage.Stage;

public class NewCreatureWizard {

	public static void run() {
		BowlerStudio.runLater(()->{
			Stage s = new Stage();
			new Thread(()->{
				Thread.setDefaultUncaughtExceptionHandler(new IssueReportingExceptionHandler());
				AddFileToGistController controller = new AddFileToGistController(null, BowlerStudioMenu.getSelfRef());
				try {
					controller.start(s,(IScriptingLanguage)new RobotHelper());
				} catch (Exception e) {
					e.printStackTrace();
				}

			}).start();			
		});

	}
	
}
