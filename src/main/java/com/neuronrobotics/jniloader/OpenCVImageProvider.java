package com.neuronrobotics.jniloader;

import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

public class OpenCVImageProvider extends AbstractImageProvider{
	private VideoCapture vc;
	private int camerIndex;
	
	public OpenCVImageProvider(int camerIndex){
		this.camerIndex = camerIndex;
		vc = new VideoCapture(camerIndex);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!vc.isOpened()) {
			System.out.println("Camera Error");
		} else {
			System.out.println("Camera OK");
		}
	}
	
	@Override
	public String toString(){
		String s="OpenCVImageProvider on camera "+camerIndex+" "+vc.toString();
		return s;
	}
	
	@Override
	public boolean captureNewImage(Mat imageData) {
		if (!vc.isOpened())
			return false;

		vc.read(imageData);
		return true;
	}

}
