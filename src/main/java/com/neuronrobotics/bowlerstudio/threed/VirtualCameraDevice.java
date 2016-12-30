package com.neuronrobotics.bowlerstudio.threed;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import com.neuronrobotics.bowlerstudio.physics.TransformFactory;
import com.neuronrobotics.imageprovider.AbstractImageProvider;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;

import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Translate;

public class VirtualCameraDevice extends AbstractImageProvider {

	private static final int DEFAULT_ZOOM_DEPTH = -1500;
	private PerspectiveCamera camera;
	private Group hand;
	private final Group cameraFrame = new Group();
	
	private double zoomDepth = getDefaultZoomDepth();
	private Affine zoomAffine = new Affine();
	private static final Affine offset = TransformFactory.nrToAffine(
			new TransformNR(0, 0, 0, new RotationNR(180,0,0))
			);
	private Group manipulationFrame;
	public VirtualCameraDevice(PerspectiveCamera camera, Group hand){
		this.hand = hand;
		this.setCamera(camera);
		setScriptingName("virtualCameraDevice");
	//System.out.println("Setting camera frame transform");
		
		manipulationFrame = new Group();
			camera.getTransforms().add(zoomAffine);

		cameraFrame.getTransforms().add(getOffset());
		manipulationFrame.getChildren().addAll(camera, hand);
		cameraFrame.getChildren().add(manipulationFrame);
		//new RuntimeException().printStackTrace();
		setZoomDepth(DEFAULT_ZOOM_DEPTH);
	}
	@Override
	public void setGlobalPositionListener(Affine affine) {
		super.setGlobalPositionListener(affine);
		manipulationFrame.getTransforms().clear();
		manipulationFrame.getTransforms().add(affine);
	}
	
	@Override
	protected boolean captureNewImage(BufferedImage imageData) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void disconnectDeviceImp() {	}

	@Override
	public boolean connectDeviceImp() {
		return true;
	}

	@Override
	public ArrayList<String> getNamespacesImp() {
		return new ArrayList<>();
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
		if(zoomDepth>-2)
			zoomDepth=-2;
		if(zoomDepth<-5000)
			zoomDepth=-5000;
		this.zoomDepth = zoomDepth;
		zoomAffine.setTz(getZoomDepth());
	}
	public BufferedImage captureNewImage() {
		// TODO Auto-generated method stub
		return null;
	}
	public static int getDefaultZoomDepth() {
		return DEFAULT_ZOOM_DEPTH;
	}
	public static Affine getOffset() {
		return offset;
	}

}
