package com.neuronrobotics.bowlerstudio.threed;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import com.neuronrobotics.imageprovider.AbstractImageProvider;

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
		
		cameraFrame.getTransforms().add(new Translate(0, -200, -1000));
		cameraFrame.getTransforms().add(affine);
		hand.getTransforms().add(affine);
		hand.getTransforms().add(new Translate(0, 200, 1000));
		
		
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
		return cameraFrame;
	}

	private void setCamera(PerspectiveCamera camera) {
		this.camera = camera;
	}

}
