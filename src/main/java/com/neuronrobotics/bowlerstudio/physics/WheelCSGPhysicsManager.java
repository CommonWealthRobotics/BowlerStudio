package com.neuronrobotics.bowlerstudio.physics;

import java.util.ArrayList;

import com.bulletphysics.dynamics.vehicle.RaycastVehicle;
import com.bulletphysics.dynamics.vehicle.WheelInfo;
import com.bulletphysics.linearmath.Transform;
import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.IClosedLoopController;
import com.neuronrobotics.sdk.util.ThreadUtil;

import eu.mihosoft.vrl.v3d.CSG;

public class WheelCSGPhysicsManager extends CSGPhysicsManager{
	private IClosedLoopController controller=null;
	private double target=0;
	private static float muscleStrength=(float) 1000;
	boolean flagBroken=false;
	private double velocity;
	private RaycastVehicle vehicle;
	private final int wheelIndex;
	public WheelCSGPhysicsManager(ArrayList<CSG> baseCSG, Transform pose, double mass,PhysicsCore c, RaycastVehicle v, int wheelIndex) {
		super(baseCSG, pose, mass,false,c);
		this.vehicle = v;
		this.wheelIndex = wheelIndex;	
	}

	@Override
	public void update(float timeStep){	
		//cut out the falling body update
		if(getUpdateManager()!=null){
			try{
				getUpdateManager().update(timeStep);
			}catch(Exception e){
				BowlerStudio.printStackTrace(e);
				throw e;
			}
		}
		if(getController()!=null){
			velocity = getController().compute(getWheelInfo().deltaRotation, getTarget(),timeStep);
			
		}
		vehicle.updateWheelTransform(getWheelIndex(), true);
		TransformNR trans = TransformFactory.bulletToNr(vehicle.getWheelInfo(getWheelIndex()).worldTransform);
		//copy in the current wheel location
		TransformFactory.nrToBullet(trans, getUpdateTransform());
		ThreadUtil.wait(16);
	}

	public double getTarget() {
		return target;
	}
	public void setTarget(double target) {
		this.target = target;
	}
	public static float getMotorStrength() {
		return muscleStrength;
	}
	public static void setMuscleStrength(float ms) {
		muscleStrength = ms;
	}
	public void setMuscleStrength(double muscleStrength) {
		setMuscleStrength((float)muscleStrength);
	}
	public IClosedLoopController getController() {
		return controller;
	}
	public void setController(IClosedLoopController controller) {
		this.controller = controller;
	}

	public WheelInfo getWheelInfo() {
		return vehicle.getWheelInfo(getWheelIndex());
	}

	public int getWheelIndex() {
		return wheelIndex;
	}

	

}
