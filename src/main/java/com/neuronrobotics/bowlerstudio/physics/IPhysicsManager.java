package com.neuronrobotics.bowlerstudio.physics;

import java.util.ArrayList;

import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;

import eu.mihosoft.vrl.v3d.CSG;
import javafx.scene.transform.Affine;

public interface IPhysicsManager {
	/**
	 * Run the update for this ridgid body. Run any controllers for links
	 * @param timeStep
	 */
	public void update(float timeStep);
	/**
	 * Return a RigidBody for the physics engine
	 * @return
	 */
	public RigidBody getFallRigidBody() ;
	/**
	 * Return the CSG that tis being modelsed
	 * @return
	 */
	public ArrayList<CSG> getBaseCSG() ;
	/**
	 * Return the current spatial location fo the rigid body
	 * @return
	 */
	public Affine getRigidBodyLocation();
	/**
	 * The Bullet version of the location
	 * @return
	 */
	public Transform getUpdateTransform();
	
}
