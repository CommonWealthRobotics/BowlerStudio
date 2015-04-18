package com.neuronrobotics.jniloader;

import org.opencv.core.Mat;

import chdk.ptp.java.CameraFactory;
import chdk.ptp.java.ICamera;
import chdk.ptp.java.SupportedCamera;
import chdk.ptp.java.exception.CameraConnectionException;
import chdk.ptp.java.exception.CameraShootException;
import chdk.ptp.java.model.CameraMode;


public class CHDKImageProvider extends AbstractImageProvider {
	ICamera cam;

	public CHDKImageProvider() {
		try {
			cam = CameraFactory.getCamera(SupportedCamera.SX160IS);
			cam.connect();
			cam.setOperaionMode(CameraMode.RECORD);

		} catch (Exception e) {

		}
	}

	@Override
	public boolean captureNewImage(Mat imageData) {
		int failure=0;
		while(failure<5){
		try {
			Thread.sleep(3000);
			bufferedImageToMat(cam.getPicture(), imageData);
			return true;
		} catch (CameraConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (CameraShootException e){
			e.printStackTrace();
			
			failure++;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
		return false;
	}

}
