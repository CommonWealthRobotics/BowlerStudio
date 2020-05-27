package com.neuronrobotics.bowlerstudio.threed;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.physics.TransformFactory;
import com.neuronrobotics.sdk.addons.kinematics.IDriveEngine;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Affine;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.util.ArrayList;

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
	static {
		Platform.runLater(
				() -> TransformFactory.nrToAffine(new TransformNR(0, 0, 0, new RotationNR(180, 0, 0)), offset));
	}

	private Affine affine=new Affine();

	public VirtualCameraMobileBase(PerspectiveCamera camera, Group hand) {
		this.hand = hand;
		this.setCamera(camera);
		// System.out.println("Setting camera frame transform");

		manipulationFrame = new Group();
		camera.getTransforms().add(zoomAffine);

		cameraFrame.getTransforms().add(getOffset());
		manipulationFrame.getChildren().addAll(camera, hand);
		cameraFrame.getChildren().add(manipulationFrame);
		// new RuntimeException().printStackTrace();
		setZoomDepth(DEFAULT_ZOOM_DEPTH);
	}

	public void setGlobalToFiducialTransform(TransformNR defautcameraView) {
		myGlobal = defautcameraView;
	}

	public void updatePositions() {
		if(System.currentTimeMillis()-timeSinceLastUpdate>16) {
			timeSinceLastUpdate=System.currentTimeMillis();
			error=false;
			Platform.runLater(()->TransformFactory.nrToAffine(myGlobal, affine));
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
		// RotationNR finalRot =
		// TransformNR(0,0,0,globalRot).times(newPose).getRotation();
		// System.out.println("Azumuth = "+az+" elevation = "+el+" tilt = "+tl);
//		Rotation n = newPose.getRotation().getStorage();
//		Rotation g = global.getRotation().getStorage();
//		Rotation nr =n.compose(g, RotationNR.getConvention());

		global.setRotation(new RotationNR(
				tlOffset + (Math.toDegrees(
						newPose.getRotation().getRotationTilt() + global.getRotation().getRotationTilt()) % 360),
				azOffset + (Math.toDegrees(
						newPose.getRotation().getRotationAzimuth() + global.getRotation().getRotationAzimuth()) % 360),
				elOffset + Math.toDegrees(
						newPose.getRotation().getRotationElevation() + global.getRotation().getRotationElevation())));
//		 global.getRotation().setStorage(nr);
		// System.err.println("Camera tilt="+global);
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