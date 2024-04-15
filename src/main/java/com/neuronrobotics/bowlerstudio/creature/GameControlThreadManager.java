package com.neuronrobotics.bowlerstudio.creature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class GameControlThreadManager {
	private static Thread scriptRunner=null;
	private static IAmControlled currentController=null;
	private static boolean running = false;
	public static void stop() {
		if(!running)
			return;
		reset();
		Thread tmp = scriptRunner;
		if (tmp != null)
			while (tmp.isAlive()) {

				System.out.println("Interrupting "+currentController.getName());
				ThreadUtil.wait(10);
				try {
					tmp.interrupt();
					tmp.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}

		scriptRunner = null;
	}
	/**
	 * @return the currentController
	 */
	public static IAmControlled getCurrentController() {
		return currentController;
	}
	/**
	 * @param currentController the currentController to set
	 */
	public static void setCurrentController(IAmControlled c) {
		if(currentController!=null)
			if(c!=currentController)
				stop();
		currentController = c;
		
	}
	public static void startStopAction() {
		currentController.getRunStopButton().setDisable(true);
		if (running)
			stop();
		else
			try {
				start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		currentController.getRunStopButton().setDisable(false);
	}
	public static void start() throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		File currentFile = currentController.getScriptFile();
		

		running = true;
		BowlerStudio.runLater(() -> {
			BowlerStudio.setToStopButton(currentController.getRunStopButton());
		});
		scriptRunner = new Thread() {

			public void run() {
				try {
					ScriptingEngine.inlineFileScriptRun(currentFile, currentController.getArguments());
					reset();

				} catch (Throwable ex) {
					ex.printStackTrace();
					reset();
				}

			}
		};

		try {

			scriptRunner.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void reset() {
		running = false;
		BowlerStudio.runLater(() -> {
			currentController.getRunStopButton().setText(currentController.getButtonRunText());
			// game.setGraphic(AssetFactory.loadIcon("Run.png"));
			for(String classes : currentController.getRunStopButton().getStyleClass()) {
				System.out.println("Clearing "+classes);
			}
			BowlerStudio.setToRunButton(currentController.getRunStopButton());
			currentController.getRunStopButton().setGraphic(currentController.getRunAsset());
		});

	}
}
