package com.neuronrobotics.bowlerstudio.physics;

import java.util.ArrayList;

import com.bulletphysics.dynamics.constraintsolver.HingeConstraint;
import com.bulletphysics.dynamics.vehicle.RaycastVehicle;
import com.bulletphysics.dynamics.vehicle.WheelInfo;
import com.bulletphysics.linearmath.Transform;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.IClosedLoopController;

import eu.mihosoft.vrl.v3d.CSG;
import javafx.application.Platform;
import javafx.scene.paint.Color;

public class WheelCSGPhysicsManager extends CSGPhysicsManager{
	private IClosedLoopController controller=null;
	private double target=0;
	private static float muscleStrength=(float) 1000;
	boolean flagBroken=false;
	private double velocity;
	private RaycastVehicle vehicle;
	private int wheelIndex;
	private WheelInfo wheel;
	public WheelCSGPhysicsManager(ArrayList<CSG> baseCSG, Transform pose, double mass,PhysicsCore c, RaycastVehicle v, int wheelIndex) {
		super(baseCSG, pose, mass,false,c);
		this.vehicle = v;
		this.wheelIndex = wheelIndex;
		setWheelInfo(vehicle.getWheelInfo(wheelIndex));
		
	}

	@Override
	public void update(float timeStep){		
		super.update(timeStep);
		if(getController()!=null){
			velocity = getController().compute(getWheelInfo().deltaRotation, getTarget(),timeStep);
			if(Math.abs(velocity)>0.0001){
				vehicle.applyEngineForce((float) velocity, wheelIndex);
				vehicle.setBrake(0.f, wheelIndex);
			}else{
				vehicle.applyEngineForce(0.f, wheelIndex);
				vehicle.setBrake(1000.f, wheelIndex);
			}
		}
		vehicle.updateWheelTransform(wheelIndex, true);
		TransformNR trans = TransformFactory.bulletToNr(vehicle.getWheelInfo(wheelIndex).worldTransform);
		//copy in the current wheel location
		TransformFactory.nrToBullet(trans, getUpdateTransform());
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
		return wheel;
	}

	public void setWheelInfo(WheelInfo wheel) {
		this.wheel = wheel;
	}

}
