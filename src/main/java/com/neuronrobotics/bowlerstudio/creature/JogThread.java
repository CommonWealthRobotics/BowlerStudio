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

public class JogThread {
	private static jogThread thread = null;
	private static Thread timer = null;
	private static HashMap<Duration, Long> startTimes = new HashMap<>();
	private static HashMap<Duration, Runnable> runnables = new HashMap<>();

	private static boolean controlThreadRunning = false;
	private static AbstractKinematicsNR source;

	public static boolean setTarget(AbstractKinematicsNR source, TransformNR toSet, double toSeconds) {
		JogThread.source = source;
		if (thread == null) {
			Log.enableSystemPrint(true);
			thread = new jogThread();
			thread.start();
		}

		return thread.setTarget(toSet, toSeconds);

	}

	public static boolean isControlThreadRunning() {
		return controlThreadRunning;
	}

	private static void setControlThreadRunning(boolean controlThreadRunning) {
		JogThread.controlThreadRunning = controlThreadRunning;
	}

	private static class jogThread extends Thread {

		private TransformNR toSet;
		private double toSeconds = .01;
		RuntimeException lastTarget = null;
		//private long time = System.currentTimeMillis();

		public void run() {
			setName(source.getScriptingName() + " Jog Widget thread");
			long threadStart = System.currentTimeMillis();
			long index = 0;
			while (source.isAvailable()) {
				// System.out.println("Jog loop");
				
				double bestTime=toSeconds;
				if (isControlThreadRunning()) {
					TickToc.setEnabled(true);
					// toSet.setZ(0);
					if (MobileBase.class.isInstance(source)) {
						try {
							((MobileBase) source).DriveArc(toSet, bestTime);
						} catch (Exception e) {
						//e.printStackTrace();
							 BowlerStudioController.highlightException(null, e);
						}
					} else if (DHParameterKinematics.class.isInstance(source)) {
						DHParameterKinematics kin = (DHParameterKinematics) source;
						try {
							// Log.enableDebugPrint();
							TickToc.tic("Jogging ");
							bestTime = kin.getBestTime(toSet);
							if(bestTime<toSeconds)
								bestTime=toSeconds;
							else {
								System.err.println("Jog paused for links to catch up "+bestTime+" vs "+toSeconds);
							}
							TickToc.tic("computed best time ");
							kin.setDesiredTaskSpaceTransform(toSet, bestTime);
						} catch (Exception e) {
							e.printStackTrace();
							// BowlerStudioController.highlightException(null, e);
						}
					}
					
					setControlThreadRunning(false);
				}else
					TickToc.setEnabled(false);
				
				double ms = toSeconds*1000.0;
				long gate =( (long)(index*ms))+threadStart;
				TickToc.tic("Jog Thread set Done "+System.currentTimeMillis()+" waiting for "+gate);
				while(System.currentTimeMillis()<gate) {
					//System.out.println(" "+(System.currentTimeMillis()-gate)+" waiting for "+gate+" "+ms+" index "+index+" thread start "+threadStart);
					ThreadUtil.wait(1);
				}
				TickToc.toc();
				index++;
			}
			//new RuntimeException("Jog thread finished").printStackTrace();
			thread = null;
		}

		public boolean setTarget(TransformNR toSet, double toSeconds) {

			String message = source.getScriptingName() + " secs " + toSeconds + " Jog Target set to " + toSet;
			RuntimeException runtimeException = new RuntimeException(message);
			
			if (isControlThreadRunning()) {
				return false;
			}
			//lastTarget.printStackTrace();
			//runtimeException.printStackTrace();
			lastTarget = runtimeException;
			this.toSet = toSet.copy();
			if (DHParameterKinematics.class.isInstance(source))
				if (!((DHParameterKinematics) source).checkTaskSpaceTransform(toSet)) {
					System.out.println("\n\nERROR Target unreachable "+toSet);
					int level = Log.getMinimumPrintLevel();
					Log.enableErrorPrint();
					((DHParameterKinematics) source).checkTaskSpaceTransform(toSet);
					Log.setMinimumPrintLevel(level);
					return false;
				}
			//this.toSeconds = toSeconds;
			setControlThreadRunning(true);
			return true;
		}

	}

}
