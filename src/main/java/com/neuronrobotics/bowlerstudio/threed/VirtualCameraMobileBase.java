package com.neuronrobotics.bowlerstudio.threed;

import com.neuronrobotics.bowlerstudio.physics.TransformFactory;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Affine;

public class VirtualCameraMobileBase {
	private TransformNR myGlobal = new TransformNR();
	double azOffset = 0;
	double elOffset = 0;
	double tlOffset = 0;
	TransformNR pureTrans = new TransformNR();
	private static final int DEFAULT_ZOOM_DEPTH = -1500;
	private PerspectiveCamera camera;
	private Group hand;
	private final Group cameraFrame = new Group();

	private double zoomDepth = getDefaultZoomDepth();
	private Affine zoomAffine = new Affine();
	private static final Affine offset = new Affine();
	private Group manipulationFrame;
	long timeSinceLastUpdate=System.currentTimeMillis();
	boolean error=false;
	

	private Affine camerUserPerspective=new Affine();

	public VirtualCameraMobileBase(PerspectiveCamera camera, Group hand) {
		this.hand = hand;
		this.setCamera(camera);
		// System.out.println("Setting camera frame transform");

		manipulationFrame = new Group();
		camera.getTransforms().add(zoomAffine);
		Platform.runLater(
				() -> TransformFactory.nrToAffine(new TransformNR(0, 0, 0, new RotationNR(180, 0, 0)), offset));
		cameraFrame.getTransforms().add(getOffset());
		manipulationFrame.getChildren().addAll(camera, hand);
		manipulationFrame.getTransforms().add(camerUserPerspective);
		cameraFrame.getChildren().add(manipulationFrame);
		// new RuntimeException().printStackTrace();
		setZoomDepth(DEFAULT_ZOOM_DEPTH);
		updatePositions();
	}

	public void setGlobalToFiducialTransform(TransformNR defautcameraView) {
		myGlobal = defautcameraView;
	}

	public void updatePositions() {
		if(System.currentTimeMillis()-timeSinceLastUpdate>16) {
			timeSinceLastUpdate=System.currentTimeMillis();
			error=false;
			TransformFactory.nrToAffine(myGlobal, camerUserPerspective);
		}else {
			// too soon
			error=true;
		}
	}

	public TransformNR getFiducialToGlobalTransform() {
		return myGlobal;
	}

	public void DriveArc(TransformNR newPose, double seconds) {
		// TODO Auto-generated method stub
		pureTrans.setX(newPose.getX());
		pureTrans.setY(newPose.getY());
		pureTrans.setZ(newPose.getZ());

		TransformNR global = getFiducialToGlobalTransform().times(pureTrans);
		global.setRotation(new RotationNR(
				tlOffset + (Math.toDegrees(
						newPose.getRotation().getRotationTilt() + global.getRotation().getRotationTilt()) % 360),
				azOffset + (Math.toDegrees(
						newPose.getRotation().getRotationAzimuth() + global.getRotation().getRotationAzimuth()) % 360),
				elOffset + Math.toDegrees(
						newPose.getRotation().getRotationElevation() + global.getRotation().getRotationElevation())));
//		 global.getRotation().setStorage(nr);
		//System.err.println("Camera tilt="+global);
		// New target calculated appliaed to global offset
		setGlobalToFiducialTransform(global);
		updatePositions();
	}

	public PerspectiveCamera getCamera() {
		return camera;
	}

	public Group getCameraGroup() {
		return getCameraFrame();
	}

	private void setCamera(PerspectiveCamera camera) {
		this.camera = camera;
	}

	public Group getCameraFrame() {
		return cameraFrame;
	}

	public double getZoomDepth() {
		return zoomDepth;
	}

	public void setZoomDepth(double zoomDepth) {
		if (zoomDepth > -2)
			zoomDepth = -2;
		if (zoomDepth < -5000)
			zoomDepth = -5000;
		this.zoomDepth = zoomDepth;
		zoomAffine.setTz(getZoomDepth());
	}

	public static int getDefaultZoomDepth() {
		return DEFAULT_ZOOM_DEPTH;
	}

	public static Affine getOffset() {
		return offset;
	}

}