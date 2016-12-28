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
import com.neuronrobotics.sdk.common.IClosedLoopController;

import javax.vecmath.Vector3f;

import eu.mihosoft.vrl.v3d.CSG;
import javafx.scene.paint.Color;

public class VehicleCSGPhysicsManager extends CSGPhysicsManager{

	
	
	
	////////////////////////////////////////////////////////////////////////////
	


	private VehicleTuning tuning = new VehicleTuning();
	public VehicleRaycaster vehicleRayCaster;
	private RaycastVehicle vehicle;

	public VehicleCSGPhysicsManager(ArrayList<CSG> baseCSG, Transform pose, double mass, boolean adjustCenter,
			PhysicsCore core) {
		super(baseCSG, pose, mass, adjustCenter, core);
		
		vehicleRayCaster = new DefaultVehicleRaycaster(core.getDynamicsWorld());
		setVehicle(new RaycastVehicle(getTuning(), getFallRigidBody(), vehicleRayCaster));
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
	public VehicleTuning getTuning() {
		return tuning;
	}
	public void setTuning(VehicleTuning tuning) {
		this.tuning = tuning;
	}


}
