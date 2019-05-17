package com.neuronrobotics.bowlerstudio;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;

import com.neuronrobotics.bowlerstudio.assets.StudioBuildInfo;

public class SplashManager {
	private static Graphics2D splashGraphics;

	private static boolean loadFirst = true;

	public static void closeSplash() {
		if (splashGraphics != null && isVisableSplash()) {
			closeSplashLocal();
			splashGraphics = null;
		}
	}

	private static void closeSplashLocal() {
		BowlerStudio.splash.close();
	}

	private static boolean isVisableSplash() {
		return BowlerStudio.splash.isVisible();
	}
	private static void updateSplash() {
		BowlerStudio.splash.update();
	}
	
	public static void renderSplashFrame(int frame, String message) {
		if(loadFirst) {
			loadFirst=false;
			initialize();
		}

		System.err.println(" Splash Rendering " + frame + " " + message);
		if (splashGraphics != null && isVisableSplash()) {
			splashGraphics.setComposite(AlphaComposite.Clear);
			splashGraphics.fillRect(65, 270, 200, 40);
			splashGraphics.setPaintMode();
			splashGraphics.setColor(Color.WHITE);
			splashGraphics.drawString(frame + "% " + message, 65, 280);
			// Platform.runLater(() -> {
			updateSplash();
			// });
		}
	}



	private static void initialize() {
		if (BowlerStudio.splash != null) {
			try {
				splashGraphics = BowlerStudio.splash.createGraphics();
			} catch (IllegalStateException e) {
			}
		}
		if (splashGraphics != null && isVisableSplash()) {
			splashGraphics.setComposite(AlphaComposite.Clear);
			splashGraphics.fillRect(65, 270, 200, 40);
			splashGraphics.setPaintMode();
			splashGraphics.setColor(Color.WHITE);
			splashGraphics.drawString(StudioBuildInfo.getVersion(), 65, 45);
			updateSplash();
		}
	}
}
