package com.neuronrobotics.bowlerstudio;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.function.BooleanSupplier;

import com.neuronrobotics.bowlerstudio.assets.StudioBuildInfo;

import javafx.application.Platform;
import javafx.scene.image.Image;

public class SplashManager {
	private static Graphics2D splashGraphics;

	private static boolean loadFirst = true;
	private static BooleanSupplier closePreventer = () -> false;
	public static void closeSplash() {
		if (isVisableSplash())
			closeSplashLocal();

	}

	private static void closeSplashLocal() {
		if (BowlerStudio.splash != null) {
			BowlerStudio.splash.close();
			splashGraphics = null;
			return;
		}
		if(closePreventer.getAsBoolean())
			return;
		PsudoSplash.close();
	}

	public static boolean isVisableSplash() {
		if (BowlerStudio.splash != null)
			return BowlerStudio.splash.isVisible();
		if(!PsudoSplash.isInitialized())
			return false;
		return PsudoSplash.get().isVisableSplash();
	}

	private static void updateSplash() {
		PsudoSplash.get().updateSplash();
	}

	public static void renderSplashFrame(int frame, String message) {
		if (loadFirst) {
			
			initialize();
		}
		String string = frame + "% " + message;
		System.out.println(" Splash Rendering " + frame + " " + message);
		PsudoSplash.get().setMessage(string);
		updateSplash();

		if (Platform.isFxApplicationThread())
			throw new RuntimeException("Splash manager can not be opened from a javafx thread!");
		while(!SplashManager.isVisableSplash()) {
			System.out.println("Waiting for splash to open before moving on");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	private static void initialize() {
		com.neuronrobotics.sdk.common.Log.error("No splash screen availible!");

		loadFirst = false;
	}

	public BooleanSupplier getClosePreventer() {
		return closePreventer;
	}

	public void setClosePreventer(BooleanSupplier closePreventer) {
		this.closePreventer = closePreventer;
	}


}
