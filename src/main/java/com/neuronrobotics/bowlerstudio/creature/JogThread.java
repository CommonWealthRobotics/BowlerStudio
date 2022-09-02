package com.neuronrobotics.bowlerstudio.creature;

import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.common.TickToc;
import com.neuronrobotics.sdk.util.ThreadUtil;
import javafx.application.Platform;
public class JogThread {
	private static jogThread thread = null;
	private static Thread timer = null;
	private static HashMap<Duration, Long> startTimes = new HashMap<>();
	private static HashMap<Duration, Runnable> runnables = new HashMap<>();

	private static boolean controlThreadRunning = false;
	private static AbstractKinematicsNR source;
	private static IJogProvider provider = null;
	private static final double toSeconds = .032;
//	public static boolean setTarget(AbstractKinematicsNR source, TransformNR toSet, double toSeconds) {
//		JogThread.source = source;
//		if (thread == null) {
//			Log.enableSystemPrint(true);
//			thread = new jogThread();
//			thread.start();
//		}
//
//		return thread.setTarget(toSet, toSeconds);
//
//	}

	public static boolean isControlThreadRunning() {
		return controlThreadRunning;
	}

	private static void setControlThreadRunning(boolean controlThreadRunning) {
		JogThread.controlThreadRunning = controlThreadRunning;
	}

	public static IJogProvider getProvider() {
		return provider;
	}

	public static void setProvider(IJogProvider provider, AbstractKinematicsNR s) {
		JogThread.provider = provider;
		source = s;
    	//new Exception(s.getClass().getName()+"\n"+provider.getClass().getName()).printStackTrace();

		if (thread == null) {
			Log.enableSystemPrint(true);
			thread = new jogThread();
			thread.start();
		}
	}

	public static double getToseconds() {
		return toSeconds;
	}

	private static class jogThread extends Thread {

		private TransformNR toSet;

		// private long time = System.currentTimeMillis();

		public void run() {
			setName(source.getScriptingName() + " Jog Widget thread");
			long threadStart = System.currentTimeMillis();
			//long index = 0;
			while (source.isAvailable()) {
				threadStart = System.currentTimeMillis();
				TransformNR tr = provider.getJogIncrement();
				if (setTarget(tr)) {
					double bestTime = getToseconds();
					if (isControlThreadRunning()) {
						//TickToc.setEnabled(true);
						if (MobileBase.class.isInstance(source)) {
							try {
								((MobileBase) source).DriveArc(toSet, bestTime);
							} catch (Exception e) {
								// e.printStackTrace();
								BowlerStudioController.highlightException(null, e);
							}
						} else if (DHParameterKinematics.class.isInstance(source)) {
							DHParameterKinematics kin = (DHParameterKinematics) source;
							try {
								// Log.enableDebugPrint();
								TickToc.tic("Jogging ");
								bestTime = kin.getBestTime(toSet);
								if (bestTime < getToseconds())
									bestTime = getToseconds();
								else {
									System.out.println(
											"Jog paused for links to catch up " + bestTime + " vs " + getToseconds());
								}
								TickToc.tic("computed best time "+bestTime);
								kin.setDesiredTaskSpaceTransform(toSet, bestTime);
								//System.out.println("Joging to "+toSet);
							} catch (Exception e) {
								e.printStackTrace();
								// BowlerStudioController.highlightException(null, e);
							}
						}

						setControlThreadRunning(false);
					} else
						TickToc.setEnabled(false);

					double ms = bestTime * 1000.0;
					long gate = ((long)  ms) + threadStart-System.currentTimeMillis();
					TickToc.tic("Jog Thread set Done " + System.currentTimeMillis() + " waiting for " + gate);
					if(gate>0)
						ThreadUtil.wait((int)gate);
					TickToc.toc();
				}else {
					ThreadUtil.wait(1);
				}
			}
			thread = null;
		}

		private boolean setTarget(TransformNR toSet) {
			if (toSet == null)
				return false;
			this.toSet = toSet.copy();
			if (DHParameterKinematics.class.isInstance(source))
				if (!((DHParameterKinematics) source).checkTaskSpaceTransform(toSet)) {
					System.out.println("\n\nERROR Target unreachable " + toSet);
					int level = Log.getMinimumPrintLevel();
					Log.enableErrorPrint();
					((DHParameterKinematics) source).checkTaskSpaceTransform(toSet);
					Log.setMinimumPrintLevel(level);
					return false;
				}
			// this.toSeconds = toSeconds;
			setControlThreadRunning(true);

			return true;
		}

	}

}
