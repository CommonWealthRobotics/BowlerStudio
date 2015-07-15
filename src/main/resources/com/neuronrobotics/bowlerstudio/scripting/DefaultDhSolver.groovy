import java.util.ArrayList;

import com.neuronrobotics.sdk.addons.kinematics.DHChain;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DhInverseSolver;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.Log;

return new DhInverseSolver() {
	
	@Override
	public double[] inverseKinematics(TransformNR target,
			double[] jointSpaceVector, ArrayList<DHLink> links) {
		int linkNum = jointSpaceVector.length;
		double [] inv = new double[linkNum];
		// this is an ad-hock kinematic model for d-h parameters and only works for specific configurations

		double d = links.get(1).getD()-
				links.get(2).getD();
		double r = links.get(0).getR();
		
		double xSet = target.getX();
		double ySet = target.getY();
		
		double polarR = Math.sqrt(xSet*xSet+ySet*ySet);
		double polarTheta = Math.asin(ySet/polarR);
		
		
		double adjustedR = Math.sqrt((polarR*polarR)+(d*d))-r;
		double adjustedTheta =Math.asin(d/polarR);
		
		
		
		double orentation = polarTheta-adjustedTheta;
		xSet = adjustedR*Math.sin(adjustedTheta);
		ySet = adjustedR*Math.cos(adjustedTheta);
	
		
		double zSet = target.getZ()
				-links.get(0).getD();
		if(links.size()>4){
			zSet+=links.get(4).getD();
		}
		// Actual target for anylitical solution is above the target minus the z offset
		TransformNR overGripper = new TransformNR(
				xSet,
				ySet,
				zSet,
				target.getRotation());


		double l1 = links.get(1).getR();// First link length
		double l2 = links.get(2).getR();

		double vect = Math.sqrt(xSet*xSet+ySet*ySet+zSet*zSet);
//		println ( "TO: "+target);
//		println ( "Trangular TO: "+overGripper);
//		println ( "polarR: "+polarR);
//		println( "polarTheta: "+Math.toDegrees(polarTheta));
//		println( "adjustedTheta: "+Math.toDegrees(adjustedTheta));
//		println( "adjustedR: "+adjustedR);
//		
//		println( "x Correction: "+xSet);
//		println( "y Correction: "+ySet);
//		
//		println( "Orentation: "+Math.toDegrees(orentation));
//		println( "z: "+zSet);

		

		if (vect > l1+l2 ||  vect<0 ||adjustedR<0 ) {
			throw new RuntimeException("Hypotenus too long: "+vect+" longer then "+l1+l2);
		}
		//from https://www.mathsisfun.com/algebra/trig-solving-sss-triangles.html
		double a=l2;
		double b=l1;
		double c=vect;
		double A =Math.acos((Math.pow(b,2)+ Math.pow(c,2) - Math.pow(a,2)) / (2*b*c));
		double B =Math.acos((Math.pow(c,2)+ Math.pow(a,2) - Math.pow(b,2)) / (2*a*c));
		double C =Math.PI-A-B;//Rule of triangles
		double elevation = Math.asin(zSet/vect);


		Log.info( "vect: "+vect);
		Log.info( "A: "+Math.toDegrees(A));
		Log.info( "elevation: "+Math.toDegrees(elevation));
		Log.info( "l1 from x/y plane: "+Math.toDegrees(A+elevation));
		Log.info( "l2 from l1: "+Math.toDegrees(C));
		inv[0] = Math.toDegrees(orentation);
		inv[1] = -Math.toDegrees((A+elevation+links.get(1).getTheta()));
		inv[2] = (Math.toDegrees(C))+//interior angle of the triangle, map to external angle
				Math.toDegrees(links.get(2).getTheta());// offset for kinematics
		if(links.size()>3)
			inv[3] =(inv[1] -inv[2]);// keep it parallell
			// We know the wrist twist will always be 0 for this model
		if(links.size()>4)
			inv[4] = inv[0];//keep the camera orentation paralell from the base
		
		for(int i=0;i<inv.length;i++){
			Log.info( "Link#"+i+" is set to "+inv[i]);
		}
		int i=3;
		if(links.size()>3)
			i=5;
		//copy over remaining links so they do not move
		for(;i<inv.length && i<jointSpaceVector.length ;i++){
			inv[i]=jointSpaceVector[i];
		}

		return inv;
	}
};