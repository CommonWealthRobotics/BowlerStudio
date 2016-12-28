package com.neuronrobotics.bowlerstudio.physics;

import java.util.ArrayList;

import com.bulletphysics.dynamics.constraintsolver.HingeConstraint;
import com.bulletphysics.dynamics.vehicle.RaycastVehicle;
import com.bulletphysics.linearmath.Transform;
import com.neuronrobotics.sdk.common.IClosedLoopController;

import eu.mihosoft.vrl.v3d.CSG;
import javafx.application.Platform;
import javafx.scene.paint.Color;

public class WheelCSGPhysicsManager extends CSGPhysicsManager{
	private HingeConstraint constraint=null;
	private IClosedLoopController controller=null;
	private double target=0;
	private static float muscleStrength=(float) 1000;
	boolean flagBroken=false;
	private double velocity;
	private RaycastVehicle vehicle;
	public WheelCSGPhysicsManager(ArrayList<CSG> baseCSG, Transform pose, double mass,PhysicsCore c, RaycastVehicle v) {
		super(baseCSG, pose, mass,false,c);
		this.vehicle = v;

	}

	@Override
	public void update(float timeStep){		
		super.update(timeStep);
		if(constraint!=null&&getController()!=null &&!flagBroken){
			velocity = getController().compute(constraint.getHingeAngle(), getTarget(),timeStep);
			constraint.enableAngularMotor(true, (float) velocity, getMotorStrength());
			if(constraint.getAppliedImpulse()>getMotorStrength()){
				for(CSG c1:baseCSG){
					c1.setColor(Color.WHITE);
				}
				flagBroken=true;
				getCore().remove(this);
				getCore().add (this);
				System.out.println("ERROR Link Broken, Strength: "+getMotorStrength()+" applied impluse "+constraint.getAppliedImpulse());
			}
		}else if (constraint!=null && flagBroken){
			constraint.enableAngularMotor(false, 0, 0);
		}
		//System.out.println("Constraint = "+constraint+" controller= "+getController()+" broken= "+flagBroken);
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

}
