package com.neuronrobotics.bowlerstudio.physics;

import java.util.ArrayList;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.BvhTriangleMeshShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.CylinderShapeX;
import com.bulletphysics.collision.shapes.TriangleIndexVertexArray;

import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.dynamics.vehicle.DefaultVehicleRaycaster;
import com.bulletphysics.dynamics.vehicle.RaycastVehicle;
import com.bulletphysics.dynamics.vehicle.VehicleRaycaster;
import com.bulletphysics.dynamics.vehicle.VehicleTuning;
import com.bulletphysics.dynamics.vehicle.WheelInfo;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;
import javax.vecmath.Vector3f;

import eu.mihosoft.vrl.v3d.CSG;
import javafx.scene.paint.Color;

public class VehicleCSGPhysicsManager extends CSGPhysicsManager{
	private static final int rightIndex = 0;
	private static final int upIndex = 1;
	private static final int forwardIndex = 2;
	private static final Vector3f wheelDirectionCS0 = new Vector3f(0,-1,0);
	private static final Vector3f wheelAxleCS = new Vector3f(-1,0,0);
	//#endif
	
	private static final int maxProxies = 32766;
	private static final int maxOverlap = 65535;

	// RaycastVehicle is the interface for the constraint that implements the raycast vehicle
	// notice that for higher-quality slow-moving vehicles, another approach might be better
	// implementing explicit hinged-wheel constraints with cylinder collision, rather then raycasts
	private static float gEngineForce = 0.f;
	private static float gBreakingForce = 0.f;

	private static float maxEngineForce = 1000.f;//this should be engine/velocity dependent
	private static float maxBreakingForce = 100.f;

	private static float gVehicleSteering = 0.f;
	private static float steeringIncrement = 0.04f;
	private static float steeringClamp = 0.3f;
	private static float wheelRadius = 0.5f;
	private static float wheelWidth = 0.4f;
	private static float wheelFriction = 1000;//1e30f;
	private static float suspensionStiffness = 20.f;
	private static float suspensionDamping = 2.3f;
	private static float suspensionCompression = 4.4f;
	private static float rollInfluence = 0.1f;//1.0f;

	private static final float suspensionRestLength = 0.6f;

	private static final int CUBE_HALF_EXTENTS = 1;
	
	////////////////////////////////////////////////////////////////////////////
	

	public ObjectArrayList<CollisionShape> collisionShapes = new ObjectArrayList<CollisionShape>();

	public VehicleTuning tuning = new VehicleTuning();
	public VehicleRaycaster vehicleRayCaster;
	private RaycastVehicle vehicle;


	public VehicleCSGPhysicsManager(ArrayList<CSG> baseCSG, Transform pose, double mass, boolean adjustCenter,
			PhysicsCore core) {
		super(baseCSG, pose, mass, adjustCenter, core);
		
		vehicleRayCaster = new DefaultVehicleRaycaster(core.getDynamicsWorld());
		setVehicle(new RaycastVehicle(tuning, getFallRigidBody(), vehicleRayCaster));
	}
	@Override
	public void update(float timeStep){
		super.update(timeStep);
		for (int i = 0; i < vehicle.getNumWheels(); i++) {
			// synchronize the wheels with the (interpolated) chassis worldtransform
			vehicle.updateWheelTransform(i, true);
		}

	}


	public RaycastVehicle getVehicle() {
		return vehicle;
	}


	public void setVehicle(RaycastVehicle vehicle) {
		this.vehicle = vehicle;
	}

}
