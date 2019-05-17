package com.neuronrobotics.bowlerstudio;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;

import com.neuronrobotics.bowlerstudio.assets.StudioBuildInfo;

public class SplashManager {
	private static Graphics2D splashGraphics;

	private static boolean loadFirst = true;
	private static PsudoSplash psudo = null;

	public static void closeSplash() {
		if(isVisableSplash())
			closeSplashLocal();
		
	}

	private static void closeSplashLocal() {
		if (BowlerStudio.splash != null) {
			BowlerStudio.splash.close();
			splashGraphics = null;
			return;
		}
		psudo.closeSplashLocal();
	}

	private static boolean isVisableSplash() {
		if (BowlerStudio.splash != null)
			return BowlerStudio.splash.isVisible();
		return psudo.isVisableSplash();
	}

	private static void updateSplash() {
		if (BowlerStudio.splash != null) {
			BowlerStudio.splash.update();
			return;
		}
		psudo.updateSplash();
	}

	public static void renderSplashFrame(int frame, String message) {
		if (loadFirst) {
			loadFirst = false;
			initialize();
		}
		String string = frame + "% " + message;
		System.err.println(" Splash Rendering " + frame + " " + message);
		if (psudo != null) {
			psudo.setMessage(string);
			updateSplash();
		}else if (splashGraphics != null && isVisableSplash()) {
			splashGraphics.setComposite(AlphaComposite.Clear);
			splashGraphics.fillRect(65, 270, 200, 40);
			splashGraphics.setPaintMode();
			splashGraphics.setColor(Color.WHITE);

			splashGraphics.drawString(string, 65, 280);
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
		} else {
			System.err.println("No splash screen availible!");
			psudo = new PsudoSplash();
		}
		if (psudo == null)
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
