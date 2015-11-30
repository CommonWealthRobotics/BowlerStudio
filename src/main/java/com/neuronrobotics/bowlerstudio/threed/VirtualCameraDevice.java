package com.neuronrobotics.bowlerstudio.threed;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import com.neuronrobotics.imageprovider.AbstractImageProvider;
import com.neuronrobotics.sdk.addons.kinematics.TransformFactory;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;

import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Translate;

public class VirtualCameraDevice extends AbstractImageProvider {

	private PerspectiveCamera camera;
	private Group hand;
	private Group cameraFrame = new Group();
	
	private double zoomDepth = -1000;
	private Affine zoomAffine = new Affine();

	public VirtualCameraDevice(PerspectiveCamera camera, Group hand){
		this.hand = hand;
		this.setCamera(camera);
		setScriptingName("virtualCameraDevice");
		
	}
	@Override
	public void setGlobalPositionListener(Affine affine) {
		super.setGlobalPositionListener(affine);
		//System.out.println("Setting camera frame transform");
		Group manipulationFrame = new Group();
		camera.getTransforms().add(zoomAffine);
		zoomAffine.setTz(getZoomDepth());
		
		getCameraFrame().getTransforms().add(TransformFactory.getTransform(
				BowlerStudio3dEngine.getOffsetforvisualization()
						));
		manipulationFrame.getTransforms().add(affine);

		
		manipulationFrame.getChildren().addAll(camera, hand);
		getCameraFrame().getChildren().add(manipulationFrame);
		
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
		return new ArrayList<String>();
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
	public void setCameraFrame(Group cameraFrame) {
		this.cameraFrame = cameraFrame;
	}
	public double getZoomDepth() {
		return zoomDepth;
	}
	public void setZoomDepth(double zoomDepth) {
		if(zoomDepth>-2)
			zoomDepth=-2;
		if(zoomDepth<-3000)
			zoomDepth=-3000;
		this.zoomDepth = zoomDepth;
		zoomAffine.setTz(getZoomDepth());
	}

}
