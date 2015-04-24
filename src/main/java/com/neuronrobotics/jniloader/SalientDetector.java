// How to boxes in case more stuff goes bad http://stackoverflow.com/questions/26814069/how-to-set-region-of-interest-in-opencv-java

package com.neuronrobotics.jniloader;


import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Size;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.KeyPoint;
import org.opencv.imgproc.Imgproc;

public class SalientDetector implements IObjectDetector {

	Rect RegionOfInterest = new Rect();
	
	int [] L_array = {100, 90, 80};
	int [] U_array = {120, 110, 100};
	
	
	List<Mat> chs1;
	List<Mat> chs2;
	
	Mat MeanShift, HSV, Luv, Hist, Gradient, Pyramid = new Mat();
	
	Mat level_1 = new Mat();
	Mat level_2 = new Mat();
	@Override
	public List<Detection> getObjects(BufferedImage in, BufferedImage disp){
		Mat inputImage = new Mat();
		AbstractImageProvider.bufferedImageToMat(in,inputImage);
		
		int Frame_Width = inputImage.cols();
		int Frame_Height = inputImage.rows() / 3; 
		
		// Following loop makes sure width / height is divisible by 2 twice
		// So when you down/up sample you don't run the risk of a segmentation fault
		while(Frame_Width % 4 != 0 || Frame_Height % 4 != 0){ 
			if(Frame_Width % 4 != 0) {Frame_Width  = Frame_Width  - 1;}
			if(Frame_Height % 4 != 0){Frame_Height = Frame_Height - 1;}
		}
		 
		RegionOfInterest.x = 0; RegionOfInterest.y = Frame_Height;
		RegionOfInterest.width = Frame_Width; RegionOfInterest.height = Frame_Height * 2;

		MeanShift = inputImage.submat(RegionOfInterest);

		Imgproc.pyrMeanShiftFiltering(inputImage, MeanShift, 4, 20);
		Imgproc.cvtColor(MeanShift, Luv, Imgproc.COLOR_BGR2Luv);
		Imgproc.cvtColor(MeanShift, HSV, Imgproc.COLOR_BGR2Luv);

		// Perform Pyramid ********************************************************************************
		level_1 = Luv.clone(); level_2 = Luv.clone();
		
		Imgproc.pyrDown(level_1, level_1, new Size(level_1.cols() / 2, level_1.rows() / 2)); // Down sample 
		Imgproc.pyrDown(level_2, level_2, new Size(level_2.cols() / 2, level_2.rows() / 2));
		Imgproc.pyrDown(level_2, level_2, new Size(level_2.cols() / 2, level_2.rows() / 2));

		Core.split(level_1, chs1); Core.split(level_1, chs1); // Split into an array each channel
		Core.split(level_2, chs2); Core.split(level_2, chs2); 

		Core.normalize(chs1.get(0), chs1.get(0), 0, L_array[0], Core.NORM_MINMAX, -1, new Mat()); // normalize 1st and 2nd channel
		Core.normalize(chs1.get(0), chs1.get(0), 0, U_array[0], Core.NORM_MINMAX, -1, new Mat()); // NOTE : remember to try out normalize on V

		Core.normalize(chs2.get(0), chs2.get(0), 0, L_array[0], Core.NORM_MINMAX, -1, new Mat());
		Core.normalize(chs2.get(0), chs2.get(0), 0, U_array[0], Core.NORM_MINMAX, -1, new Mat());
		
		Core.merge(chs1, level_1);
		Core.merge(chs2, level_2);
		
		Imgproc.pyrUp(level_1, level_1, new Size(level_1.cols() / 2, level_1.rows() / 2));        // up sample them back to original size
		Imgproc.pyrUp(level_2, level_2, new Size(level_2.cols() / 2, level_2.rows() / 2));
		Imgproc.pyrUp(level_2, level_2, new Size(level_2.cols() / 2, level_2.rows() / 2));

		Core.add(level_1, level_2, Luv); // combine them
		// ************************************************************************************************
		
		// Perform Hist
		
		return new ArrayList<Detection>();
	}

}
