package com.neuronrobotics.jniloader;

import haar.HaarFactory;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;
import org.opencv.objdetect.CascadeClassifier;


public class HaarDetector  implements IObjectDetector{
	private MatOfRect faceDetections = new MatOfRect();;
	private CascadeClassifier faceDetector ;
	private double scale=.6;
	
	public HaarDetector(String cascade){
		File f = HaarFactory.jarResourceToFile(cascade);
		// Create a face detector from the cascade file in the resources
	    // directory.
		faceDetector = new CascadeClassifier(f.getAbsolutePath());
	}
	
	public HaarDetector(){

		// Create a face detector from the cascade file in the resources
	    // directory.
		this("haarcascade_frontalface_default.xml");
	}

	
	public KeyPoint[] getObjects(Mat inputImage, Mat displayImage){

		BufferedImage tmp= AbstractImageProvider.toGrayScale(AbstractImageProvider.matToBufferedImage(inputImage),scale);
		
		MatOfByte mb;

		try {
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(tmp, "jpg", baos);
			baos.flush();
			byte[] tmpByteArray = baos.toByteArray();
			baos.close();
			mb = new MatOfByte(tmpByteArray);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	
		Mat matImageLocal =Highgui.imdecode(mb, 0);
		
		if(matImageLocal.empty()){
			return null;
		}
		
		faceDetector.detectMultiScale(matImageLocal, faceDetections);
		Rect [] smallArray = faceDetections.toArray();
		KeyPoint [] myArray = new KeyPoint [smallArray.length];
		
		for(int i=0;i<smallArray.length;i++){
			Rect r = smallArray[i];
			myArray[i] = new KeyPoint(	(int)	(r.x/scale),
									(int)(r.y/scale),
									(int)(r.width/scale), 
									(int)(r.height/scale));
		}
		
		Point center=null;// 
		//System.out.println(String.format("Detected %s faces", myArray.length));
		// Draw a bounding box around each face.
	    for (KeyPoint rect : myArray) {
	        //Core.rectangle(displayImage, rect.pt, new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
	    	center = new Point(rect.pt.x+(rect.size/2), rect.pt.y+(rect.size/2)) ;
	    	
			
			Size objectSize= new Size(	rect.size/2,
					rect.size/2);
			
			Core.ellipse(displayImage, center,objectSize, 0, 0, 360, new Scalar(255, 0,
					255), 4, 8, 0);
	    }
		return myArray;
	}



	@Override
	public void setThreshhold(Scalar rgb_min, Scalar rgb_max) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setThreshhold2(Scalar rgb_min2, Scalar rgb_max2) {
		// TODO Auto-generated method stub
		
	}
}
