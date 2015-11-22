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
		camera.getTransforms().add(TransformFactory.getTransform(
				new TransformNR(-0,
						0, 
						-1000, 
						new RotationNR(0,0,0)
						)));
		
		getCameraFrame().getTransforms().add(TransformFactory.getTransform(
				new TransformNR(0,
						0, 
						0, 
						new RotationNR(0,90,90)
						)));
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

}
