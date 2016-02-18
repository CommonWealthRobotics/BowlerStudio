package com.neuronrobotics.bowlerstudio.threed;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.StaticPlaneShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
import com.neuronrobotics.sdk.util.ThreadUtil;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Sphere;

public class PhysicsEngine {
	private static PhysicsEngine mainEngine;
	private BroadphaseInterface broadphase = new DbvtBroadphase();
	private DefaultCollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
	private CollisionDispatcher dispatcher = new CollisionDispatcher(getCollisionConfiguration());

	private SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();

	private DiscreteDynamicsWorld dynamicsWorld = new DiscreteDynamicsWorld(getDispatcher(), getBroadphase(), getSolver(),
			getCollisionConfiguration());
	// setup our collision shapes
	private CollisionShape groundShape =null;
	
	private ArrayList<CSGPhysicsManager> objects =new ArrayList<>();
	private RigidBody groundRigidBody;
	
	public PhysicsEngine() throws Exception {
		// set the gravity of our world
		getDynamicsWorld().setGravity(new Vector3f(0, 0, (float) -98));
		
		setGroundShape(new StaticPlaneShape(new Vector3f(0, 0, 1), 1));

	}

	public static void main(String[] args) throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		File servoFile = ScriptingEngine.fileFromGit("https://github.com/NeuronRobotics/BowlerStudioVitamins.git",
				"BowlerStudioVitamins/stl/servo/smallservo.stl");
		// Load the .CSG from the disk and cache it in memory
		CSG servo = Vitamins.get(servoFile);

		PhysicsEngine.add(new CSGPhysicsManager(
				servo, 
				new Vector3f(6, 2, 180),
				10));
		PhysicsEngine.add(new CSGPhysicsManager(
				new Sphere(20 * 2).toCSG(), 
				new Vector3f(0, 0, 100),
				20));
		
		int msLoopTime = 16;

		for (int i = 0; i < 300 && !Thread.interrupted(); i++) {
			long start = System.currentTimeMillis();
			PhysicsEngine.stepMs(msLoopTime);
			long took = (System.currentTimeMillis() - start);
			if (took < msLoopTime)
				ThreadUtil.wait((int) (msLoopTime - took));
		 }
	}
	
	public static void step(float timeStep){
		get().getDynamicsWorld().stepSimulation(timeStep , 10);
		for(CSGPhysicsManager o:get().getPhysicsObjects()){
			o.update();
		}
	}
	
	public static void stepMs(double timeStep){
		step((float) ((float) timeStep / 1000.0));
	}
	
	public static void add(CSGPhysicsManager manager){
		if(!get().getPhysicsObjects().contains(manager)){
			get().getPhysicsObjects().add(manager);
			get().getDynamicsWorld().addRigidBody(manager.getFallRigidBody());
		}
	}
	
	public static void remove(CSGPhysicsManager manager){
		if(get().getPhysicsObjects().contains(manager)){
			get().getPhysicsObjects().remove(manager);
			get().getDynamicsWorld().removeRigidBody(manager.getFallRigidBody());
		}
	}
	public static void clear(){
		for(CSGPhysicsManager o:get().getPhysicsObjects()){
			get().getDynamicsWorld().removeRigidBody(o.getFallRigidBody());
		}
		get().getPhysicsObjects().clear();
		
	}
	public static PhysicsEngine get() {
		if (mainEngine == null)
			try {
				mainEngine = new PhysicsEngine();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return mainEngine;

	}
	
	public static ArrayList<CSG> getCsgFromEngine(){
		ArrayList<CSG> csg = new ArrayList<>();
		for(CSGPhysicsManager o:get().getPhysicsObjects()){
			csg.add(o.getBaseCSG());
		}
		return csg;
	}


	public BroadphaseInterface getBroadphase() {
		return broadphase;
	}



	public void setBroadphase(BroadphaseInterface broadphase) {
		this.broadphase = broadphase;
	}

	public DefaultCollisionConfiguration getCollisionConfiguration() {
		return collisionConfiguration;
	}

	public void setCollisionConfiguration(DefaultCollisionConfiguration collisionConfiguration) {
		this.collisionConfiguration = collisionConfiguration;
	}

	public CollisionDispatcher getDispatcher() {
		return dispatcher;
	}

	public void setDispatcher(CollisionDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	public SequentialImpulseConstraintSolver getSolver() {
		return solver;
	}

	public void setSolver(SequentialImpulseConstraintSolver solver) {
		this.solver = solver;
	}

	public DiscreteDynamicsWorld getDynamicsWorld() {
		return dynamicsWorld;
	}

	public void setDynamicsWorld(DiscreteDynamicsWorld dynamicsWorld) {
		this.dynamicsWorld = dynamicsWorld;
	}

	public CollisionShape getGroundShape() {
		return groundShape;
	}

	public void setGroundShape(CollisionShape groundShape) {
		if(groundRigidBody!=null){
			getDynamicsWorld().removeRigidBody(groundRigidBody); // add our ground to the
		}
		this.groundShape = groundShape;
		// setup the motion state
		DefaultMotionState groundMotionState = new DefaultMotionState(
				new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), new Vector3f(0, -1, 0), 1.0f)));

		RigidBodyConstructionInfo groundRigidBodyCI = new RigidBodyConstructionInfo(0, groundMotionState, getGroundShape(),
				new Vector3f(0, 0, 0));
		groundRigidBody = new RigidBody(groundRigidBodyCI);
		getDynamicsWorld().addRigidBody(groundRigidBody); // add our ground to the
	}

	public ArrayList<CSGPhysicsManager> getPhysicsObjects() {
		return objects;
	}

	public void setObjects(ArrayList<CSGPhysicsManager> objects) {
		this.objects = objects;
	}

}
