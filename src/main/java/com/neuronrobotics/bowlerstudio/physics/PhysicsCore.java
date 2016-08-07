package com.neuronrobotics.bowlerstudio.physics;

import java.util.ArrayList;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

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
import com.neuronrobotics.sdk.util.ThreadUtil;

import eu.mihosoft.vrl.v3d.CSG;
import javafx.application.Platform;

public class PhysicsCore {
	
	private BroadphaseInterface broadphase = new DbvtBroadphase();
	private DefaultCollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
	private CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);

	private SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();

	private DiscreteDynamicsWorld dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, solver,
			collisionConfiguration);
	// setup our collision shapes
	private CollisionShape groundShape =null;
	
	private ArrayList<IPhysicsManager> objects =new ArrayList<>();
	private RigidBody groundRigidBody;
	
	private  boolean runEngine = false;
	private  int msTime=16;
	
	private  Thread physicsThread = null;
	public PhysicsCore() throws Exception {
		// set the gravity of our world
		getDynamicsWorld().setGravity(new Vector3f(0, 0, (float) -98*6));
		
		setGroundShape(new StaticPlaneShape(new Vector3f(0, 0, 10), 1));
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

	public void setGroundShape(CollisionShape cs ){
		if(groundRigidBody!=null){
			getDynamicsWorld().removeRigidBody(groundRigidBody); // add our ground to the
		}
		this.groundShape = cs;
		// setup the motion state
		DefaultMotionState groundMotionState = new DefaultMotionState(
				new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), new Vector3f(0, 0, 0), 1.0f)));

		RigidBodyConstructionInfo groundRigidBodyCI = new RigidBodyConstructionInfo(0, groundMotionState, groundShape,
				new Vector3f(0, 0, 0));
		groundRigidBody = new RigidBody(groundRigidBodyCI);
		dynamicsWorld.addRigidBody(groundRigidBody); // add our ground to the
	}

	public ArrayList<IPhysicsManager> getPhysicsObjects() {
		return objects;
	}

	public void setObjects(ArrayList<IPhysicsManager> objects) {
		this.objects = objects;
	}
	public void startPhysicsThread(int ms){
		msTime=ms;
		if(physicsThread==null){
			runEngine=true;
			physicsThread=new Thread(()->{
				while(runEngine){
					try{
						long start = System.currentTimeMillis();
						stepMs(msTime);
						long took = (System.currentTimeMillis() - start);
						if (took < msTime)
							ThreadUtil.wait((int) (msTime - took));
						else
							System.out.println("Real time physics broken: "+took);
					}catch(Exception E){
						E.printStackTrace();
					}
				}
			});
			physicsThread.start();
		}
	}
	public  ArrayList<CSG> getCsgFromEngine(){
		ArrayList<CSG> csg = new ArrayList<>();
		for(IPhysicsManager o:getPhysicsObjects()){
			csg.add(o.getBaseCSG());
		}
		return csg;
	}
	public void stopPhysicsThread(){
		physicsThread=null;
		runEngine=false;
	}
	public  void step(float timeStep){
		long startTime = System.currentTimeMillis();
		 getDynamicsWorld().stepSimulation(timeStep , 5);
		if( (((float)(System.currentTimeMillis()- startTime))/1000.0f)>timeStep){
			//System.out.println(" Compute took too long "+timeStep);
		}
		for(IPhysicsManager o: getPhysicsObjects()){
			o.update( timeStep);
		}
		Platform.runLater(()->{
			for(IPhysicsManager o: getPhysicsObjects())
				TransformFactory.bulletToAffine(o.getRigidBodyLocation(), o.getUpdateTransform());
		});
	}
	
	public  void stepMs(double timeStep){
		step((float) ((float) timeStep / 1000.0));
	}
	
	public  void add(IPhysicsManager manager){
		if(! getPhysicsObjects().contains(manager)){
			 getPhysicsObjects().add(manager);
			 getDynamicsWorld().addRigidBody(manager.getFallRigidBody());
			if(HingeCSGPhysicsManager.class.isInstance(manager)){
				if(((HingeCSGPhysicsManager) manager).getConstraint()!=null)
					 getDynamicsWorld().addConstraint(((HingeCSGPhysicsManager) manager).getConstraint(), true);
			}
		}
	}
	
	public  void remove(IPhysicsManager manager){
		if( getPhysicsObjects().contains(manager)){
			 getPhysicsObjects().remove(manager);
			 getDynamicsWorld().removeRigidBody(manager.getFallRigidBody());
			if(HingeCSGPhysicsManager.class.isInstance(manager)){
				if(((HingeCSGPhysicsManager) manager).getConstraint()!=null)
					 getDynamicsWorld().removeConstraint(((HingeCSGPhysicsManager) manager).getConstraint());
			}
		}
	}
	public  void clear(){
		stopPhysicsThread();
		ThreadUtil.wait((int) (msTime*2));
		for(IPhysicsManager o:getPhysicsObjects()){
			getDynamicsWorld().removeRigidBody(o.getFallRigidBody());
			if(HingeCSGPhysicsManager.class.isInstance(o)){
				if(((HingeCSGPhysicsManager) o).getConstraint()!=null)
					getDynamicsWorld().removeConstraint(((HingeCSGPhysicsManager) o).getConstraint());
			}
		}
		getPhysicsObjects().clear();
		
	}
}
