
import java.util.ArrayList;

import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.util.ThreadUtil;

return  new com.neuronrobotics.sdk.addons.kinematics.IDriveEngine (){
	double stepOverHeight=5;
	public void DriveArc(MobileBase source, TransformNR newPose, double seconds) {
		int numlegs = source.getLegs().size();
		TransformNR [] feetLocations = new TransformNR[numlegs];
		TransformNR [] home = new TransformNR[numlegs];
		ArrayList<DHParameterKinematics> legs = source.getLegs();
		// Load in the locations of the tips of each of the feet. 
		for(int i=0;i<numlegs;i++){
			feetLocations[i]=legs.get(i).getCurrentPoseTarget();
			home[i] = legs.get(i).calcHome();
		}
		//Apply transform to each dimention of current pose
		TransformNR global= source.getFiducialToGlobalTransform();
		global.translateX(newPose.getX());
		global.translateY(newPose.getY());
		global.translateZ(newPose.getZ());
		double rotz = newPose.getRotation().getRotationZ() +global.getRotation().getRotationZ() ;
		double roty = newPose.getRotation().getRotationY() ;
		double rotx = newPose.getRotation().getRotationX() ;
		global.setRotation(new RotationNR( rotx,roty, rotz) );
		// New target calculated appliaed to global offset
		source.setGlobalToFiducialTransform(global);
		for(int i=0;i<numlegs;i++){
			if(!legs.get(i).checkTaskSpaceTransform(feetLocations[i])){
				//new leg position is not reachable, reverse course and walk up the line to a better location
				loop {
					feetLocations[i].translateX(-newPose.getX());
					feetLocations[i].translateY(-newPose.getY());
				}until{legs.get(i).checkTaskSpaceTransform(feetLocations[i])};
				//step back one increment for new location
				feetLocations[i].translateX(newPose.getX());
				feetLocations[i].translateY(newPose.getY());
				//perform the step over
				home[i].translateZ(stepOverHeight);
				try {
					// lift leg above home
					legs.get(i).setDesiredTaskSpaceTransform(home[i], seconds/10);
					ThreadUtil.wait((int) (seconds*100));
					//step to new target 
					legs.get(i).setDesiredTaskSpaceTransform(feetLocations[i], seconds/10);
					ThreadUtil.wait((int) (seconds*100));
					//set new target for the coordinated motion step at the end
					feetLocations[i].translateX(newPose.getX());
					feetLocations[i].translateY(newPose.getY());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}

		}
		//all legs have a valid target set, perform coordinated motion
		for(int i=0;i<numlegs;i++){
			try {
				legs.get(i).setDesiredTaskSpaceTransform(feetLocations[i], seconds);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}

	
	public void DriveVelocityStraight(MobileBase source, double cmPerSecond) {
		// TODO Auto-generated method stub
		
	}

	
	public void DriveVelocityArc(MobileBase source, double degreesPerSecond,
			double cmRadius) {
		// TODO Auto-generated method stub
		
	}



}
