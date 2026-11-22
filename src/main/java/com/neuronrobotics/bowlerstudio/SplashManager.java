package com.neuronrobotics.bowlerstudio;

import java.awt.Graphics2D;
import java.util.Objects;
import java.util.function.BooleanSupplier;

import org.jfree.util.Log;

public class SplashManager {
	private static Graphics2D splashGraphics;

	private static boolean loadFirst = true;
	private static volatile BooleanSupplier closePreventer = () -> false;
	private static BooleanSupplier openPreventer = () -> false;
	public static void closeSplash() {
		if (isVisibleSplash())
			closeSplashLocal();

	}

	private static void closeSplashLocal() {
		Log.debug("Closing Splash Here");
		Log.error(new Exception());
		if (closePreventer.getAsBoolean()) {
			Log.debug("Close prevented by " + closePreventer);
			return;
		}
		if (BowlerStudio.splash != null) {
			BowlerStudio.splash.close();
			splashGraphics = null;
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
		if (openPreventer.getAsBoolean())
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
		if (openPreventer.getAsBoolean())
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

	public static synchronized void setClosePreventer(BooleanSupplier cp) {
		closePreventer = activePreventer(Objects.requireNonNull(cp, "closePreventer"));
	}

	/** Holds the close-preventer policy until the owning operation completes. */
	public static synchronized CloseHold preventClosing() {
		CloseHold hold = new CloseHold(closePreventer);
		closePreventer = hold;
		return hold;
	}

	private static BooleanSupplier activePreventer(BooleanSupplier preventer) {
		while (preventer instanceof CloseHold && !((CloseHold) preventer).active)
			preventer = ((CloseHold) preventer).previous;
		return preventer;
	}

	public static final class CloseHold implements BooleanSupplier, AutoCloseable {
		private final BooleanSupplier previous;
		private volatile boolean active = true;

		private CloseHold(BooleanSupplier previous) {
			this.previous = previous;
		}

		@Override
		public boolean getAsBoolean() {
			return active || previous.getAsBoolean();
		}

		@Override
		public void close() {
			synchronized (SplashManager.class) {
				if (!active)
					return;
				active = false;
				// An approval dialog may temporarily own the policy; preserve its override.
				if (closePreventer == this)
					closePreventer = activePreventer(previous);
			}
		}
	}

	public static BooleanSupplier getOpenPreventer() {
		return openPreventer;
	}

	public static void setOpenPreventer(BooleanSupplier openPreventer) {
		SplashManager.openPreventer = openPreventer;
	}

}
