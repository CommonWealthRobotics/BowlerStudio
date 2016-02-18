package com.neuronrobotics.bowlerstudio.threed;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.broadphase.BroadphaseNativeType;
import com.bulletphysics.collision.shapes.*;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;
import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.sdk.addons.kinematics.gui.TransformFactory;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Sphere;
import eu.mihosoft.vrl.v3d.Vertex;
import javafx.application.Platform;
import javafx.scene.transform.Affine;

public class CSGPhysicsManager {
	
	private RigidBody fallRigidBody;
	private Affine ballLocation;
	private CSG baseCSG;
	public CSGPhysicsManager(int sphereSize, Vector3f start){
		this.setBaseCSG(new Sphere(sphereSize).toCSG());
		CollisionShape fallShape = new SphereShape((float) (baseCSG.getMaxX()-baseCSG.getMinX())/2);
		setup(fallShape,start);
	}
	public CSGPhysicsManager(CSG baseCSG, Vector3f start){
		this.setBaseCSG(baseCSG);// force a hull of the shape to simplify physics
		
		ObjectArrayList<Vector3f> arg0= new ObjectArrayList<>();
		for( Polygon p:baseCSG.getPolygons()){
			for( Vertex v:p.vertices){
				arg0.add(new Vector3f((float)v.getX(), (float)v.getY(), (float)v.getZ()));
			}
		}
		CollisionShape fallShape =  new com.bulletphysics.collision.shapes.ConvexHullShape(arg0);
		setup(fallShape,start);
	}
	
	private void setup(CollisionShape fallShape,Vector3f start ){
		// setup the motion state for the ball
		DefaultMotionState fallMotionState = new DefaultMotionState(
				new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), start, 1.0f)));
		// This we're going to give mass so it responds to gravity
		int mass = 10;
		Vector3f fallInertia = new Vector3f(0, 0, 0);
		fallShape.calculateLocalInertia(mass, fallInertia);
		RigidBodyConstructionInfo fallRigidBodyCI = new RigidBodyConstructionInfo(mass, fallMotionState, fallShape,
				fallInertia);
		setFallRigidBody(new RigidBody(fallRigidBodyCI));
	}
	

	
	public void update(){
		Platform.runLater(new Runnable() {
			public void run() {
				Transform trans = new Transform();
				fallRigidBody.getMotionState().getWorldTransform(trans);
				Quat4f out= new Quat4f();
				trans.getRotation(out);
				TransformNR  tr = new TransformNR(trans.origin.x,
						trans.origin.y,
						trans.origin.z, out.w, out.x, out.y, out.z);
				TransformFactory.getTransform(tr, ballLocation);
			}
		});
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
		ballLocation = new Affine();
		baseCSG.setManipulator(ballLocation);
		BowlerStudioController.addCsg(baseCSG);
		this.baseCSG = baseCSG;
	}
}