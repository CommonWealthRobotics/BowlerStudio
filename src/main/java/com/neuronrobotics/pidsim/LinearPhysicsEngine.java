package com.neuronrobotics.pidsim;

import java.util.ArrayList;

import com.neuronrobotics.sdk.common.NonBowlerDevice;

public class LinearPhysicsEngine extends NonBowlerDevice {
	/**
	 *
	 */
	private double torque=0;// Newton meters
	private double w = 0;// Radians/second
	private double angle = 0;//radians
	private double mass;// in kg
	private double linkLen;// inmeters
	private double muStatic;// Newton meters
	private double muDynamic;// Newton meters
	private PIDSim pid;
	private double step = .005;//in seconds
	private double maxTorque = 20;// Newton meters
	private long time = 0l;// Seconds
	double acceleration;
	private boolean run = true;
	
	public void setEnabled(boolean isEnabled) {
		run = isEnabled;
	}
	
	public void setTorque(double torque) throws Exception{
		if(Math.abs(torque)<=Math.abs(getMaxTorque())){
			this.torque = torque;
			return;
		}
		if(torque>0)
			this.torque = getMaxTorque();
		else
			this.torque = getMaxTorque()*-1;
		throw new Exception("Max Torque exceded, actuator saturates at: "+maxTorque+" N*M, tried to set to value: "+ torque);
	}
	
	public void setMass(double mass) {
		this.mass = mass;
	}
	
	public double getMass() {
		return mass;
	}
	
	public void setLinkLen(double linkLen) {
		this.linkLen = linkLen;
	}
	
	public double getLinkLen() {
		return linkLen;
	}
	
	public void setMuStatic(double muStatic) {
		this.muStatic = muStatic;
	}
	
	public double getMuStatic() {
		return muStatic;
	}
	
	public void setMuDynamic(double muDynamic) {
		this.muDynamic = muDynamic;
	}
	
	public double getMuDynamic() {
		return muDynamic;
	}

	public void setMaxTorque(double maxTorque) {
		this.maxTorque = maxTorque;
	}

	public double getMaxTorque() {
		return maxTorque;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getTime() {
		return time;
	}

	@Override
	public void disconnectDeviceImp() {
		// TODO Auto-generated method stub
		run=false;
	}

	@Override
	public boolean connectDeviceImp() {
		run=true;
		getPid();
		new Thread(){
			public void run() {
				System.out.println("Starting physics engine.");
				while (run) {
					
					long localStep = (long) (step*1000);
					
					setTime(getTime() + localStep);
					getPid().setTime(getTime());
					double I = getMass() * getLinkLen() * getLinkLen() ;
					double tGravity = (getLinkLen()*Math.cos(angle)) * ((getMass()  * -9.8)); // the torque due to gravity
					double tTotal = torque + tGravity;
					
					if( w==0 && (getMuStatic()>Math.abs(tTotal))){
						//System.out.println("Static friction not overcome");
						tTotal=0;
					}
					if(w!=0){
						double t = tTotal;
						if(tTotal>0){
							tTotal =t-getMuDynamic()*w;
						}else{
							tTotal =t+getMuDynamic()*w*-1;
						}
//						if(tTotal!=0)
//							System.out.println("Torque: \n\tgravity="+ tGravity+" \n\tgravity plus set="+t+" \n\tafter friction="+tTotal);
					}

					acceleration = tTotal/I;
					
					w+=acceleration*step*step;
					
					if(w != 0) {
						angle+=w*step;
					}
					
					if(Math.toDegrees(angle) >181){
						angle = Math.PI;
						w=0;
					}
					
					if(Math.toDegrees(angle) < -1){
						angle = 0;
						w=0;
					}
					
					//System.out.println("Controls: \n\ttorque: "+torque+" \n\tTorque Total: "+tTotal+" \n\tTg: "+tGravity+" \n\tAceleration: "+acceleration+" \n\tAngular velocity: "+w+" \n\tAngle: "+Math.toDegrees(angle));
					getPid().setPosition(Math.toDegrees(angle));
					
					try {Thread.sleep(localStep);} catch (InterruptedException e) {}
				}
			}
		}.start();
		return false;
	}

	@Override
	public ArrayList<String> getNamespacesImp() {
		// TODO Auto-generated method stub
		return null;
	}

	public PIDSim getPid() {
		if(pid==null)
			setPid(new PIDSim(this));
		return pid;
	}

	public void setPid(PIDSim pid) {
		this.pid = pid;
		setMass(getPid().getMass());
		setLinkLen(getPid().getLength());
		setMuStatic(getPid().getStaticFriction());
		setMuDynamic(getPid().getDynamicFriction());
		setTime(System.currentTimeMillis());
		this.maxTorque=pid.getMaxTorque();
		
	}
	
	@Override
	public String toString(){
		return "Torque set= "+torque+" max torque"+maxTorque;
	}
	
}
