package com.neuronrobotics.bowlerstudio.physics;

import java.util.ArrayList;

import com.bulletphysics.dynamics.constraintsolver.HingeConstraint;
import com.bulletphysics.linearmath.Transform;
import com.neuronrobotics.sdk.common.IClosedLoopController;

import eu.mihosoft.vrl.v3d.CSG;
import javafx.application.Platform;
import javafx.scene.paint.Color;

public class HingeCSGPhysicsManager extends CSGPhysicsManager{
	private HingeConstraint constraint=null;
	private IClosedLoopController controller=null;
	private double target=0;
	private static float muscleStrength=(float) 1000;
	boolean flagBroken=false;
	private double velocity;
	private PhysicsCore core;
	public HingeCSGPhysicsManager(ArrayList<CSG> baseCSG, Transform pose, double mass,PhysicsCore core) {
		super(baseCSG, pose, mass,false,core);
		this.core = core;
		//baseCSG.setColor(Color.YELLOW);
	}
	@Override
	public void update(float timeStep){
		super.update(timeStep);
		if(constraint!=null&&getController()!=null &&!flagBroken){
			velocity = getController().compute(constraint.getHingeAngle(), getTarget(),timeStep);
			constraint.enableAngularMotor(true, (float) velocity, getMuscleStrength());
			if(constraint.getAppliedImpulse()>getMuscleStrength()){
				for(CSG c:baseCSG){
					c.setColor(Color.WHITE);
				}
				flagBroken=true;
				core.remove(this);
				setConstraint(null);
				core.add (this);
				System.out.println("ERROR Link Broken, Strength: "+getMuscleStrength()+" applied impluse "+constraint.getAppliedImpulse());
			}else
				System.out.println("Impulse = "+constraint.getAppliedImpulse()+" strength = "+getMuscleStrength() );
		}else if (constraint!=null && flagBroken){
			constraint.enableAngularMotor(false, 0, 0);
		}
	}

	
	public HingeConstraint getConstraint() {
		return constraint;
	}
	public void setConstraint(HingeConstraint constraint) {
		this.constraint = constraint;
	}
	public double getTarget() {
		return target;
	}
	public void setTarget(double target) {
		this.target = target;
	}
	public static float getMuscleStrength() {
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
