
import java.util.ArrayList;

import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.util.ThreadUtil;
import com.neuronrobotics.sdk.addons.kinematics.IDriveEngine;

IDriveEngine engine =  new IDriveEngine (){
	double stepOverHeight=5;
	boolean takingStep = false;
	@Override
	public void DriveArc(MobileBase source, TransformNR newPose, double seconds) {
		if(takingStep)
			return;
		takingStep = true;
		int numlegs = source.getLegs().size();
		TransformNR [] feetLocations = new TransformNR[numlegs];
		TransformNR [] home = new TransformNR[numlegs];
		ArrayList<DHParameterKinematics> legs = source.getLegs();
		// Load in the locations of the tips of each of the feet. 
		for(int i=0;i<numlegs;i++){
			feetLocations[i]=legs.get(i).getCurrentTaskSpaceTransform();
			home[i] = legs.get(i).calcHome();
			feetLocations[i].setZ(home[i].getZ());
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
			double footx,footy;
			TransformNR startLocation = legs.get(i).getCurrentTaskSpaceTransform();
			// start by storing where the feet are
			footx = startLocation.getX();
			footy = startLocation.getY();
			if(!legs.get(i).checkTaskSpaceTransform(feetLocations[i])){
				println "Leg "+i+" cant reach x="+feetLocations[i].getX()+" y="+feetLocations[i].getY()
				feetLocations[i].setX(home[i].getX()-newPose.getX());
				feetLocations[i].setY(home[i].getY()-newPose.getY());
				println " Trying from  to x="+feetLocations[i].getX()+" y="+feetLocations[i].getY()
				int j=0;
				while(legs.get(i).checkTaskSpaceTransform(feetLocations[i])){
					feetLocations[i].translateX(newPose.getX());
					feetLocations[i].translateY(newPose.getY());
					j++;
				}
				//step back one to ensure a valid reset location
				feetLocations[i].translateX(-newPose.getX());
				feetLocations[i].translateY(-newPose.getY());
				//perform the step over
				home[i].translateZ(stepOverHeight);
				println j+" iterations Leg "+i+" setep over to x="+feetLocations[i].getX()+" y="+feetLocations[i].getY()
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
		takingStep = false;
		
	}

	@Override
	public void DriveVelocityStraight(MobileBase source, double cmPerSecond) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void DriveVelocityArc(MobileBase source, double degreesPerSecond,
			double cmRadius) {
		// TODO Auto-generated method stub
		
	}



}

return engine;
