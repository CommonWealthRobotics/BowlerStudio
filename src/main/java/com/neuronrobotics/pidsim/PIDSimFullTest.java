package com.neuronrobotics.pidsim;


public class PIDSimFullTest {
	public static void main(String[] args) throws InterruptedException {
//		PIDSim pid = new PIDSim( .01, // mass
//								 .5,  //link length
//								 5,  //torque from static friction
//								 4.5); //torque from dynamic friction
//    	pid.initialize();
//    	pid.setSetPoint(45);
//    	PIDConstantsDialog d = new PIDConstantsDialog(	2,//kp
//										    			.5,//ki
//										    			15);//kd
//    	
//    	
//    	double IntegralCircularBuffer[] = new double[100];
//    	double iTotal=0;
//    	int index=0;
//    	double previousState = 0;
//    	for(int i =0;i<IntegralCircularBuffer.length;i++) {
//    		IntegralCircularBuffer[i]=0;
//    	}
//    	while(true) {
//    		Thread.sleep(10); //Wait 10 ms to make a 100 hz control loop
//    		double set = pid.getSetPoint();
//    		double now = pid.getPosition();
//    		
//    		double torque = 0;
//    		
//    		double error = now - set;
//    		iTotal -= IntegralCircularBuffer[index];
//    		iTotal +=error;
//    		
//    		double derivitave=(error-previousState);
//    		index++;
//    		if (index == IntegralCircularBuffer.length){
//    			index=0;
//    		}
//    		IntegralCircularBuffer[index]=error;
//    		torque = (d.getKp())*(error) + (d.getKd())*derivitave +(d.getKi())*(iTotal/IntegralCircularBuffer.length);
//    		double tGravity = 0;
//    		double tFriction = 0;
//    		//tGravity = pid.getLength() * (pid.getMass() * Math.cos(Math.toRadians(now)) * -9.8);
//    		previousState=error;
//    		try{
//    			pid.setTorque((torque*-1)-tGravity+tFriction);
//    		}catch (Exception ex){
//    			System.out.println("Max Torque exceded");
//    		}
//    		
//    	}
	}

}
