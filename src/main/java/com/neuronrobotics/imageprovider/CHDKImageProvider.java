package com.neuronrobotics.imageprovider;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import chdk.ptp.java.CameraFactory;
import chdk.ptp.java.ICamera;
import chdk.ptp.java.SupportedCamera;
import chdk.ptp.java.exception.CameraConnectionException;
import chdk.ptp.java.exception.CameraNotFoundException;
import chdk.ptp.java.exception.CameraShootException;
import chdk.ptp.java.exception.GenericCameraException;
import chdk.ptp.java.exception.PTPTimeoutException;
import chdk.ptp.java.model.CameraMode;

public class CHDKImageProvider extends AbstractImageProvider {
	ICamera cam;

	public CHDKImageProvider() throws PTPTimeoutException, GenericCameraException {
			cam = CameraFactory.getCamera(SupportedCamera.SX160IS);
			cam.connect();
			cam.setOperaionMode(CameraMode.RECORD);
	}

	@Override
	public boolean captureNewImage(BufferedImage imageData) {
		int failure = 0;
		while (failure < 5) {
			try {
				// Thread.sleep(3000);
				//bufferedImageToMat(cam.getPicture(), imageData);
				AbstractImageProvider.deepCopy(cam.getPicture(),imageData);
				return true;
			} catch (CameraConnectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			} catch (CameraShootException e) {
				e.printStackTrace();

				failure++;
			}
		}
		return false;
	}


	@Override
	public void disconnectDeviceImp() {
		try {
			cam.disconnect();
		} catch (CameraConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean connectDeviceImp() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ArrayList<String> getNamespacesImp() {
		// TODO Auto-generated method stub
		return null;
	}

}
