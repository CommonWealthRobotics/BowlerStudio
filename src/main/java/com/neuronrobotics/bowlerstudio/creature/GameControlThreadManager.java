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
		if(!isRunning())
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
		boolean was=isRunning();
		if(currentController!=null)
			if(c!=currentController)
				stop();
		currentController = c;
		if(was) {
			start();
		}
	}
	public static void startStopAction() {
		currentController.getRunStopButton().setDisable(true);
		if (isRunning())
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
	public static void start()  {
		File currentFile = currentController.getScriptFile();
		

		setRunning(true);
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
		setRunning(false);
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
	/**
	 * @return the running
	 */
	public static boolean isRunning() {
		return running;
	}
	/**
	 * @param running the running to set
	 */
	private static void setRunning(boolean running) {
		GameControlThreadManager.running = running;
	}
}
