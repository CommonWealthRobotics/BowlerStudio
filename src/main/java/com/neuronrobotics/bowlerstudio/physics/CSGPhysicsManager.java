package com.neuronrobotics.bowlerstudio.physics;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.*;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.HingeConstraint;
import com.bulletphysics.dynamics.constraintsolver.TypedConstraint;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;
//import com.neuronrobotics.sdk.addons.kinematics.gui.TransformFactory;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Sphere;
import eu.mihosoft.vrl.v3d.Vertex;
import javafx.application.Platform;
import javafx.scene.transform.Affine;

public class CSGPhysicsManager  implements IPhysicsManager{
	
	private RigidBody fallRigidBody;
	private Affine ballLocation = new Affine();
	protected CSG baseCSG;
	private Transform updateTransform = new Transform();
	private IPhysicsUpdate updateManager = null;
	public CSGPhysicsManager(int sphereSize, Vector3f start, double mass,PhysicsCore core){
		this.setBaseCSG(new Sphere(sphereSize).toCSG());
		CollisionShape fallShape = new SphereShape((float) (baseCSG.getMaxX()-baseCSG.getMinX())/2);
		setup(fallShape,new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), start, 1.0f)),mass,core);
	}
	public CSGPhysicsManager(CSG baseCSG, Vector3f start, double mass,PhysicsCore core){
		this(baseCSG,new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), start, 1.0f)),mass,true, core);
	}
	
	protected void loadCSGToPoints(CSG baseCSG,ObjectArrayList<Vector3f> arg0){
		
	}
	
	public CSGPhysicsManager(CSG baseCSG, Transform pose,  double mass, boolean adjustCenter, PhysicsCore core){
		this.setBaseCSG(baseCSG);// force a hull of the shape to simplify physics
		CSG finalCSG = baseCSG;
		if(adjustCenter){
			double xcenter = baseCSG.getMaxX()/2+baseCSG.getMinX()/2;
			double ycenter = baseCSG.getMaxY()/2+baseCSG.getMinY()/2;
			double zcenter = baseCSG.getMaxZ()/2+baseCSG.getMinZ()/2;
			
			TransformNR poseToMove = TransformFactory.bulletToNr(pose);
			if(baseCSG.getMaxX()<1 ||baseCSG.getMinX()>-1 ){
				finalCSG=finalCSG.movex(-xcenter);
				poseToMove.translateX(xcenter);
			}
			if(baseCSG.getMaxY()<1 ||baseCSG.getMinY()>-1 ){
				finalCSG=finalCSG.movey(-ycenter);
				poseToMove.translateY(ycenter);
			}
			if(baseCSG.getMaxZ()<1 ||baseCSG.getMinZ()>-1 ){
				finalCSG=finalCSG.movez(-zcenter);
				poseToMove.translateZ(zcenter);
			}
			TransformFactory.nrToBullet(poseToMove, pose);
		}
		
		
		this.setBaseCSG(finalCSG);// force a hull of the shape to simplify physics
		ObjectArrayList<Vector3f> arg0= new ObjectArrayList<>();
		for( Polygon p:finalCSG.getPolygons()){
			for( Vertex v:p.vertices){
				arg0.add(new Vector3f((float)v.getX(), (float)v.getY(), (float)v.getZ()));
			}
		}
		CollisionShape fallShape =  new com.bulletphysics.collision.shapes.ConvexHullShape(arg0);
		setup(fallShape,pose,mass,core);
	}
	public void setup(CollisionShape fallShape,Transform pose, double mass, PhysicsCore core ){
		// setup the motion state for the ball
		DefaultMotionState fallMotionState = new DefaultMotionState(
				pose);
		// This we're going to give mass so it responds to gravity
		Vector3f fallInertia = new Vector3f(0, 0, 0);
		fallShape.calculateLocalInertia((float) mass, fallInertia);
		RigidBodyConstructionInfo fallRigidBodyCI = new RigidBodyConstructionInfo((float) mass, fallMotionState, fallShape,
				fallInertia);
		fallRigidBodyCI.additionalDamping = true;
		setFallRigidBody(new RigidBody(fallRigidBodyCI));
		update(40);
	}
	

	public void update(float timeStep){		
		fallRigidBody.getMotionState().getWorldTransform(getUpdateTransform());
		if(getUpdateManager()!=null){
			getUpdateManager().update(timeStep);
		}
	}
	
	


	public RigidBody getFallRigidBody() {
		return fallRigidBody;
	}

	public void setFallRigidBody(RigidBody fallRigidBody) {
		
		this.fallRigidBody = fallRigidBody;
	}

	public CSG getBaseCSG() {
		return baseCSG;
	}

	public void setBaseCSG(CSG baseCSG) {
		
		baseCSG.setManipulator(getRigidBodyLocation());
		this.baseCSG = baseCSG;
	}
	public Transform getUpdateTransform() {
		return updateTransform;
	}
	public void setUpdateTransform(Transform updateTransform) {
		this.updateTransform = updateTransform;
	}
	public Affine getRigidBodyLocation() {
		return ballLocation;
	}
	public void setBallLocation(Affine ballLocation) {
		this.ballLocation = ballLocation;
	}
	public IPhysicsUpdate getUpdateManager() {
		return updateManager;
	}
	public void setUpdateManager(IPhysicsUpdate updateManager) {
		this.updateManager = updateManager;
	}


}
