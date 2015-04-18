// How to boxes in case more stuff goes bad http://stackoverflow.com/questions/26814069/how-to-set-region-of-interest-in-opencv-java

package com.neuronrobotics.jniloader;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Point;
import org.opencv.features2d.KeyPoint;
import org.opencv.imgproc.Imgproc;

public class SalientDetector implements IObjectDetector {

	Rect RegionOfInterest = new Rect();
	
	@Override
	public KeyPoint[] getObjects(Mat inputImage, Mat displayImage) {
			
		
		int Frame_Width = inputImage.cols();
		int Frame_Height = inputImage.rows() / 3; 
		
		// Following loop makes sure width / height is divisible by 2 twice.
		// So when you down/up sample you don't run the risk of a seg fault
		while(Frame_Width % 4 != 0 || Frame_Height % 4 != 0){ 
			if(Frame_Width % 4 != 0) {Frame_Width  = Frame_Width  - 1;}
			if(Frame_Height % 4 != 0){Frame_Height = Frame_Height - 1;}
		}
		 
		RegionOfInterest.x = 0; RegionOfInterest.y = Frame_Height;
		RegionOfInterest.width = Frame_Width; RegionOfInterest.height = Frame_Height * 2;

		inputImage = inputImage.submat(RegionOfInterest);

		Imgproc.pyrMeanShiftFiltering(inputImage, inputImage, 4, 20); 
		Imgproc.cvtColor(inputImage, inputImage, Imgproc.COLOR_BGR2Luv);
		
		
		return null;
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
