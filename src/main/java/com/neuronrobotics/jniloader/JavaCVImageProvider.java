package com.neuronrobotics.jniloader;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

public class JavaCVImageProvider  extends AbstractImageProvider{
	private VideoCapture vc;
	private int camerIndex;
	
	public JavaCVImageProvider(int camerIndex){
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
			boolean wset = vc.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, 320);
			boolean hset = vc.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, 240);
			System.out.println("Camera OK at "+vc.get(5)+
					"fps width: "+vc.get(Highgui.CV_CAP_PROP_FRAME_WIDTH)+
					" height: "+vc.get(Highgui.CV_CAP_PROP_FRAME_HEIGHT) );
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
		Imgproc.resize(imageData, imageData, new Size(320,240));
		return true;
	}
}
