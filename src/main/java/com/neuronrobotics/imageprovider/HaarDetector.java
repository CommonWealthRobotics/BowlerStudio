package com.neuronrobotics.imageprovider;

import haar.HaarFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;


public class HaarDetector  implements IObjectDetector{
	private MatOfRect faceDetections = new MatOfRect();;
	private CascadeClassifier faceDetector ;
	private double scale=.3;
	
	public HaarDetector(String cascade){
		this(HaarFactory.jarResourceToFile(cascade));
	}
	public HaarDetector(File cascade){
		// Create a face detector from the cascade file in the resources
	    // directory.
		faceDetector = new CascadeClassifier(cascade.getAbsolutePath());
	}
	public HaarDetector(){

		// Create a face detector from the cascade file in the resources
	    // directory.
		this("haarcascade_frontalface_default.xml");
	}

	
	public List<Detection> getObjects(BufferedImage in, BufferedImage disp){
		Mat inputImage = new Mat();
		AbstractImageProvider.bufferedImageToMat(in,inputImage);
		try{
			Mat localImage = new Mat();
			Size s =new Size(in.getWidth(),in.getHeight());
			Imgproc.resize(inputImage, localImage, new Size(s.width*scale,s.height*scale));
			Imgproc.cvtColor(localImage, localImage, Imgproc.COLOR_BGR2GRAY);
		
			faceDetector.detectMultiScale(localImage, faceDetections);
			Rect [] smallArray = faceDetections.toArray();
			ArrayList<Detection> myArray = new ArrayList<Detection>();
			
			for(int i=0;i<smallArray.length;i++){
				Rect r = smallArray[i];
				myArray.add(new Detection((r.x/scale), (r.y/scale), (r.width/scale)));
			}
			Mat displayImage = new Mat();
			AbstractImageProvider.bufferedImageToMat(disp,displayImage);
			Point center=null;// 
			//System.out.println(String.format("Detected %s faces", myArray.length));
			// Draw a bounding box around each face.
		    for (Detection rect : myArray) {
		        //Core.rectangle(displayImage, rect.pt, new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
		    	center =  new Point(rect.getX()+(rect.getSize()/2), rect.getY()+(rect.getSize()/2));
		    	
				
				Size objectSize= new Size(	(rect.getSize()/2),
						(rect.getSize()/2));
				
				Core.ellipse(displayImage, center,objectSize, 0, 0, 360, new Scalar(255, 0,
						255), 4, 8, 0);
		    }
		    AbstractImageProvider.matToBufferedImage(displayImage).copyData(disp.getRaster());
		    
			return myArray;
		} catch (CvException |NullPointerException |IllegalArgumentException e2) {
			// startup noise
			// e.printStackTrace();
			return  new ArrayList<Detection>();
		}
	}

}
