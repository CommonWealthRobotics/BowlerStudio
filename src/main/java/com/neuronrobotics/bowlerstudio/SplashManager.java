package com.neuronrobotics.bowlerstudio;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;

import com.neuronrobotics.bowlerstudio.assets.StudioBuildInfo;

import javafx.scene.image.Image;

public class SplashManager {
	private static Graphics2D splashGraphics;

	private static boolean loadFirst = true;

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
		System.err.println(" Splash Rendering " + frame + " " + message);
		PsudoSplash.get().setMessage(string);
		updateSplash();
	}

	private static void initialize() {
		System.err.println("No splash screen availible!");

		loadFirst = false;
	}


}
