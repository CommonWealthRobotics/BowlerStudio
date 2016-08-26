package com.neuronrobotics.bowlerstudio.physics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.codehaus.groovy.transform.tailrec.TailRecursiveASTTransformation;
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
import javafx.application.Platform;

public class PhysicsEngine {
	private  static PhysicsCore mainEngine;

	public static void startPhysicsThread(int ms){
		get().startPhysicsThread(ms);
	}
	public static void stopPhysicsThread(){
		get().stopPhysicsThread();
	}
	public static void step(float timeStep){
		get().step(timeStep);
	}
	
	public static void stepMs(double timeStep){
		get().stepMs(timeStep);
	}
	
	public static void add(IPhysicsManager manager){
		get().add(manager);
	}
	
	public static void remove(IPhysicsManager manager){
		get().remove(manager);
	}
	public static void clear(){
		get().clear();
		mainEngine=null;
		
	}
	public static PhysicsCore get() {
		if (mainEngine == null)
			try {
				mainEngine = new PhysicsCore();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return mainEngine;

	}
	
	public static ArrayList<CSG> getCsgFromEngine(){

		return mainEngine.getCsgFromEngine();
	}


	


}
