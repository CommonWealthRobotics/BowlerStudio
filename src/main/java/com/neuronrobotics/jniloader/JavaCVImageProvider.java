package com.neuronrobotics.jniloader;

import java.awt.image.BufferedImage;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.VideoInputFrameGrabber;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

public class JavaCVImageProvider  extends AbstractImageProvider{
	private FrameGrabber grabber;
	private int camerIndex;
	private  Frame img;
	public JavaCVImageProvider(int camerIndex) throws Exception{
		this.camerIndex = camerIndex;
		 grabber = new VideoInputFrameGrabber(camerIndex); // 1 for next camera
		 grabber.start();
        
	}
	
	@Override
	public String toString(){
		String s="JavaCVImageProvider on camera "+camerIndex+" "+grabber.toString();
		return s;
	}
	
	@Override
	public boolean captureNewImage(BufferedImage imageData) {
		try {
			img = grabber.grab();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		//AbstractImageProvider.deepCopy(img.,imageData);
		return true;
	}

	@Override
	public void disconnect() {
		try {
			grabber.release();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
