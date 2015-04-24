package com.neuronrobotics.jniloader;

import java.awt.image.BufferedImage;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

public class OpenCVImageProvider extends AbstractImageProvider{
	private VideoCapture vc;
	private int camerIndex;
	Mat m = new Mat();
	
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
	public boolean captureNewImage(BufferedImage imageData) {
		if (!vc.isOpened())
			return false;
		
		vc.read(m);
		AbstractImageProvider.matToBufferedImage(m).copyData(imageData.getRaster());
		return true;
	}

}
