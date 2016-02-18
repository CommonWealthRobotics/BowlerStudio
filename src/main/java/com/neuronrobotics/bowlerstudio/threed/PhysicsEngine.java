package com.neuronrobotics.bowlerstudio.threed;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import javafx.application.Platform;
import javafx.scene.transform.Affine;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.collision.shapes.StaticPlaneShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.sdk.util.ThreadUtil;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Sphere;

public class PhysicsEngine {
	private static PhysicsEngine mainEngine;
	BroadphaseInterface broadphase = new DbvtBroadphase();
	DefaultCollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
	CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);

	SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();

	DiscreteDynamicsWorld dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, solver,
			collisionConfiguration);
	// setup our collision shapes
	CollisionShape groundShape = new StaticPlaneShape(new Vector3f(0, 1, 0), 1);


	// setup the motion state
	DefaultMotionState groundMotionState = new DefaultMotionState(
			new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), new Vector3f(0, -1, 0), 1.0f)));

	RigidBodyConstructionInfo groundRigidBodyCI = new RigidBodyConstructionInfo(0, groundMotionState, groundShape,
			new Vector3f(0, 0, 0));
	RigidBody groundRigidBody = new RigidBody(groundRigidBodyCI);
	
	public PhysicsEngine() {

		int sphereSize = 20;



		// set the gravity of our world
		dynamicsWorld.setGravity(new Vector3f(0, -98, 0));
		dynamicsWorld.addRigidBody(groundRigidBody); // add our ground to the
		// dynamic world..
		
		CSGPhysicsManager manager = new CSGPhysicsManager(
				new Sphere(sphereSize).toCSG()
				,new Vector3f(0, 100, 0));
		CSGPhysicsManager manager2 = new CSGPhysicsManager(
				new Sphere(sphereSize).toCSG()
				,new Vector3f(40, 150, 0));

		// now we add it to our physics simulation
		dynamicsWorld.addRigidBody(manager.getFallRigidBody());
		dynamicsWorld.addRigidBody(manager2.getFallRigidBody());
		int msLoopTime = 16;

		for (int i = 0; i < 300 && !Thread.interrupted(); i++) {
			long start = System.currentTimeMillis();
			dynamicsWorld.stepSimulation((float) ((float) msLoopTime / 1000.0), 10);

			manager.update();
			manager2.update();
			long took = (System.currentTimeMillis() - start);
			if (took < msLoopTime)
				ThreadUtil.wait((int) (msLoopTime - took));

			// System.out.println("sphere height: " + ballLocation.getTz()+"
			// took "+took);

		}
	}

	public static PhysicsEngine get() {
		if (mainEngine == null)
			mainEngine = new PhysicsEngine();
		return mainEngine;

	}

}
