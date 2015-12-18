package com.neuronrobotics.bowlerstudio;
import org.apache.commons.io.IOUtils;

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.addons.kinematics.DrivingType;
import com.neuronrobotics.sdk.addons.kinematics.IDriveEngine;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.TransformFactory;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.Log;

public class VirtualCameraMobileBase extends MobileBase {
	
	public  VirtualCameraMobileBase() throws Exception{
		//super (IOUtils.toInputStream(ScriptingEngine.codeFromGistID("bfa504cdfba41b132c5d","flyingCamera.xml")[0], "UTF-8"));
		super (BowlerStudio.class
				.getResourceAsStream("flyingCamera.xml"));
		setDriveType(DrivingType.WALKING);
		
		setWalkingDriveEngine(new IDriveEngine() {
			
			@Override
			public void DriveVelocityStraight(MobileBase source, double cmPerSecond) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void DriveVelocityArc(MobileBase source, double degreesPerSecond, double cmRadius) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void DriveArc(MobileBase source, TransformNR newPose, double seconds) {
				TransformNR pureTrans = newPose.copy();
				pureTrans.setRotation(new RotationNR());
				TransformNR global= source.getFiducialToGlobalTransform().times(pureTrans);
				
				double az = Math.toDegrees(newPose.getRotation().getRotationAzimuth()+global.getRotation().getRotationAzimuth());
				double el = Math.toDegrees(newPose.getRotation().getRotationElevation()+global.getRotation().getRotationElevation());
				double tl = Math.toDegrees(newPose.getRotation().getRotationTilt()+global.getRotation().getRotationTilt());
				//System.out.println("Azumuth = "+az+" elevation = "+el+" tilt = "+tl);
				global = new TransformNR(global.getX(),
						global.getY(),
						global.getZ(),
						new RotationNR(	tl,
										az, 
										0//el
										));
				// New target calculated appliaed to global offset
				source.setGlobalToFiducialTransform(global);
				int debug = Log.getMinimumPrintLevel();
				Log.enableWarningPrint();
				//System.out.println(this.getClass().getSimpleName()+"Setting camera to: "+global);
				Log.setMinimumPrintLevel(debug);
			}
		});
		
	}

}
