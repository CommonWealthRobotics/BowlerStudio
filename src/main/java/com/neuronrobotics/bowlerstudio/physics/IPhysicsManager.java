package com.neuronrobotics.bowlerstudio.physics;

import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;
import eu.mihosoft.vrl.v3d.CSG;
import javafx.scene.transform.Affine;

import java.util.ArrayList;

public interface IPhysicsManager {
	/**
	 * Run the update for this ridgid body. Run any controllers for links
	 * @param timeStep
	 */
	void update(float timeStep);
	/**
	 * Return a RigidBody for the physics engine
	 * @return
	 */
	RigidBody getFallRigidBody() ;
	/**
	 * Return the CSG that tis being modelsed
	 * @return
	 */
	ArrayList<CSG> getBaseCSG() ;
	/**
	 * Return the current spatial location fo the rigid body
	 * @return
	 */
	Affine getRigidBodyLocation();
	/**
	 * The Bullet version of the location
	 * @return
	 */
	Transform getUpdateTransform();
	
}
