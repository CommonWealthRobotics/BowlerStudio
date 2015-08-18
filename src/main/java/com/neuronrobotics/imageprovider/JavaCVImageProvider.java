package com.neuronrobotics.imageprovider;

import java.awt.image.BufferedImage;

//import org.bytedeco.javacpp.opencv_core.IplImage;
//import org.bytedeco.javacv.Frame;
//import org.bytedeco.javacv.FrameGrabber;
//import org.bytedeco.javacv.FrameGrabber.Exception;
//import org.bytedeco.javacv.Java2DFrameConverter;
//import org.bytedeco.javacv.OpenCVFrameConverter;
//import org.bytedeco.javacv.OpenCVFrameGrabber;
//import org.bytedeco.javacv.VideoInputFrameGrabber;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

public class JavaCVImageProvider  extends AbstractImageProvider{
	//private OpenCVFrameGrabber grabber;
	private int camerIndex;
	private  BufferedImage  img;
	//private  OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
	//private  Java2DFrameConverter paintConverter = new Java2DFrameConverter();
	public JavaCVImageProvider(int camerIndex) throws Exception{
		start(camerIndex);
        
	}
	
	private void start(int num) throws Exception{
		this.camerIndex = num;
//		 grabber = new OpenCVFrameGrabber(camerIndex); // 1 for next camera
//		 grabber.start();
	}
	
	@Override
	public String toString(){
//		String s="JavaCVImageProvider on camera "+camerIndex+" "+grabber.toString();
		return "";
	}
	
	@Override
	public boolean captureNewImage(BufferedImage imageData) {
//		try {
//			img= paintConverter.getBufferedImage(grabber.grab(), 2.2/grabber.getGamma());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			//e.printStackTrace();
//			try {
//				System.err.println("Restarting...");
//				disconnect();
//				start(camerIndex);
//			} catch (Exception e1) {
//				throw new RuntimeException(e);
//			}
//			return false;
//		}
//		
//		AbstractImageProvider.deepCopy(img,imageData);
		return true;
	}

	@Override
	public void disconnect() {
//		try {
//			grabber.stop();
//			grabber.release();
//			grabber=null;
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			//e.printStackTrace();
//		}
	}
}
