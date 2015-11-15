package com.neuronrobotics.bowlerstudio;
import org.apache.commons.io.IOUtils;

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.addons.kinematics.DrivingType;
import com.neuronrobotics.sdk.addons.kinematics.IDriveEngine;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.Log;

public class VirtualCameraMobileBase extends MobileBase {
	
	public  VirtualCameraMobileBase() throws Exception{
		super (IOUtils.toInputStream(ScriptingEngine.codeFromGistID("bfa504cdfba41b132c5d","flyingCamera.xml")[0], "UTF-8"));
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
				TransformNR global= source.getFiducialToGlobalTransform().times(newPose);
				// New target calculated appliaed to global offset
				source.setGlobalToFiducialTransform(global);
				int debug = Log.getMinimumPrintLevel();
				Log.enableWarningPrint();
				Log.warning(this.getClass().getSimpleName()+"Setting camera to: "+newPose);
				Log.setMinimumPrintLevel(debug);
			}
		});
		
	}

}
