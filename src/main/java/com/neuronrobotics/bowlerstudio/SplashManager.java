package com.neuronrobotics.bowlerstudio;

import java.awt.Graphics2D;
import java.util.function.BooleanSupplier;

import org.jfree.util.Log;

public class SplashManager {
	private static Graphics2D splashGraphics;

	private static boolean loadFirst = true;
	private static BooleanSupplier closePreventer = () -> false;
	private static BooleanSupplier openPreventer = () -> false;
	public static void closeSplash() {
		if (isVisibleSplash())
			closeSplashLocal();

	}

	private static void closeSplashLocal() {
		if (BowlerStudio.splash != null) {
			BowlerStudio.splash.close();
			splashGraphics = null;
			return;
		}
		if (closePreventer.getAsBoolean()) {
			Log.debug("Close prevented by " + closePreventer);
			return;
		}
		PsudoSplash.close();
	}

	public static boolean isVisibleSplash() {
		if (BowlerStudio.splash != null)
			return BowlerStudio.splash.isVisible();
		if (!PsudoSplash.isInitialized())
			return false;
		return PsudoSplash.isVisibleSplash();
	}

	private static void updateSplash() {
		PsudoSplash.get().updateSplash();
	}

	public static void renderSplashFrame(int percent, String message) {
		if(openPreventer.getAsBoolean())
			return;
		if (loadFirst) {

			initialize();
		}
		String string = percent + "% " + message;
		// com.neuronrobotics.sdk.common.Log.debug(" Splash Rendering " + percent + " "
		// + message);
		PsudoSplash.get().setMessage(string);
		waitForUpdate();
	}

	public static void onLogUpdate(String message) {
		if(openPreventer.getAsBoolean())
			return;
		if (loadFirst) {

			initialize();
		}
		PsudoSplash.get().onLogUpdate(message, null);
		waitForUpdate();
	}

	private static void waitForUpdate() {
		updateSplash();

		// if (Platform.isFxApplicationThread())
		// throw new RuntimeException("Splash manager can not be opened from a javafx
		// thread!");
		int index = 0;
		while (!SplashManager.isVisibleSplash()) {
			com.neuronrobotics.sdk.common.Log.debug("Waiting for splash to open before moving on");
			try {
				Thread.sleep(100);
				index++;
			} catch (InterruptedException e) {
				return;
			}
			if (index > 10)
				return;
		}
	}

	private static void initialize() {
		com.neuronrobotics.sdk.common.Log.error("No splash screen available!");

		loadFirst = false;
	}

	public static BooleanSupplier getClosePreventer() {
		return closePreventer;
	}

	public static void setClosePreventer(BooleanSupplier cp) {
		closePreventer = cp;
	}

	public static BooleanSupplier getOpenPreventer() {
		return openPreventer;
	}

	public static void setOpenPreventer(BooleanSupplier openPreventer) {
		SplashManager.openPreventer = openPreventer;
	}

}
