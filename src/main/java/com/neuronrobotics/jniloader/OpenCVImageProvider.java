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
		setVc(new VideoCapture(camerIndex));

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!getVc().isOpened()) {
			System.out.println("Camera Error");
		} else {
//			boolean wset = getVc().set(Highgui.CV_CAP_PROP_FRAME_WIDTH, 320);
//			boolean hset = getVc().set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, 240);
			System.out.println("Camera OK at "+getVc().get(5)+
					"fps width: "+getVc().get(Highgui.CV_CAP_PROP_FRAME_WIDTH)+
					" height: "+getVc().get(Highgui.CV_CAP_PROP_FRAME_HEIGHT) );
		}
	}
	
	@Override
	public String toString(){
		String s="OpenCVImageProvider on camera "+camerIndex+" "+getVc().toString();
		return s;
	}
	
	@Override
	public boolean captureNewImage(BufferedImage imageData) {
		if (!getVc().isOpened())
			return false;
		
		getVc().read(m);
		try{
			AbstractImageProvider.deepCopy(AbstractImageProvider.matToBufferedImage(m),imageData);
		}catch(Exception ex){
			if(InterruptedException.class.isInstance(ex))throw new RuntimeException(ex);
		}
		return true;
	}

	@Override
	public void disconnect() {
		
		getVc().release();
		setVc(null);
	}

	private VideoCapture getVc() {
		return vc;
	}

	private void setVc(VideoCapture vc) {
		this.vc = vc;
	}

}
