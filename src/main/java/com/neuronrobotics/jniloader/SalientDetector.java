package com.neuronrobotics.jniloader;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import com.neuronrobotics.bowlerstudio.tabs.CameraTab;

// it broke the whole thing last time you fixed the issue of changing the file name to
// MySalientDetector instead of just SalientDetector.

public class SalientDetector implements IObjectDetector {
	@Override
	public List<Detection> getObjects(java.awt.image.BufferedImage inImg,java.awt.image.BufferedImage disp) {
		// public KeyPoint[] getObjects(Mat inputImage, Mat displayImage) {

		Scalar RedBox = new Scalar(0, 0, 255);

		int horizon = 100;
		int minArea = 100;
		
		int threshMin = 50;
		int threshMax = 255;

		int MeanShift_spatialRad = 10;
		int MeanShift_colorRad = 50;

		int Erode_Max = 1;
		int Erode_Min = 2;

		int Dilate_Max = 5;
		int Dilate_Min = 5;

		int PyrSize = 3; // how many times to downsample

		ArrayList<Detection> InterestingArea = new ArrayList<Detection>(); 
		ArrayList<Mat> RegionsOfInterest = new ArrayList<Mat>();

		Mat inputImage = new Mat();
		Mat MeanShift = new Mat(); // MeanShifted image to reduce noise
		Mat ObjFound = new Mat(); // Where stuff is found and red boxes drawn
		Mat Saliency = new Mat(); // Saliency of image

		Mat top = new Mat(); // top level of pyramid
		Mat bot = new Mat(); // meanshifted image to compare to

		Mat DS = new Mat();
		Mat UP = new Mat();

		AbstractImageProvider.bufferedImageToMat(inImg, inputImage);

		ObjFound = inputImage.clone();
		MeanShift = inputImage.clone();

		// TermCriteria crit = new TermCriteria(TermCriteria.COUNT, 0,0);
		Imgproc.pyrMeanShiftFiltering(MeanShift, MeanShift, MeanShift_spatialRad, MeanShift_colorRad);
		// Imgproc.pyrMeanShiftFiltering(MeanShift, MeanShift, 10, 60);
		// Imgproc.pyrMeanShiftFiltering(MeanShift, MeanShift, 5, 30);
		// AbstractImageProvider.deepCopy(AbstractImageProvider.matToBufferedImage(MeanShift),disp);
		// // BLOBS
		Imgproc.cvtColor(MeanShift, MeanShift, Imgproc.COLOR_BGR2GRAY);

		// Perform Pyramid Function **********************************
		MeanShift.convertTo(MeanShift, CvType.CV_32F); // 32F between 0 - 1.
														// **************
														// IMPORTANT LINE
		DS = MeanShift.clone();

		for (int i = 0; i < PyrSize; i++) {
			// Mat a = new Mat(DS.rows()/2, DS.cols()/2, DS.type());
			Mat a = new Mat();
			Imgproc.pyrDown(DS, a);
			// Imgproc.pyrDown(DS, a, new Size(DS.cols()/2, DS.rows()/2));
			DS = a.clone();
		}

		UP = DS.clone();

		for (int i = 0; i < PyrSize; i++) {
			// Mat a = new Mat(UP.rows()*2, UP.cols()*2, UP.type());
			Mat a = new Mat();
			Imgproc.pyrUp(UP, a);
			// Imgproc.pyrUp(UP, a, new Size(UP.cols()*2, UP.rows()*2));
			UP = a.clone();
		}

		top = UP.clone(); // most downsampled then upsampled image
		System.out.println(top.channels());
		bot = MeanShift.clone();
		Saliency = MeanShift.clone();
		
		// AbstractImageProvider.deepCopy(AbstractImageProvider.matToBufferedImage(top),disp);
		// http://answers.opencv.org/question/5/how-to-get-and-modify-the-pixel-of-mat-in-java/
		// http://www.tutorialspoint.com/java_dip/applying_weighted_average_filter.htm

		int channelNumb = top.channels();

		float[] top_temp = new float[channelNumb]; // since it's grayscale it'll be a float[] of size 1
		float[] bot_temp = new float[channelNumb];
		float[] sal_temp = new float[channelNumb];

		float top_min = (float) 1.0;
		float bot_min = (float) 1.0;

		for (int i = 0; i < top.rows(); i++) { // find the smallest value in both top and meanShift
			for (int j = 0; j < top.cols(); j++) { // this is so you can sub out the 0 with the min value
				top.get(i, j, top_temp);
				bot.get(i, j, bot_temp);
				float a = top_temp[0];
				float b = bot_temp[0];
				if (a < top_min && a > 0) {top_min = a;} 
				if (b < bot_min && b > 0) {bot_min = b;}
			}
		}

		for (int i = 0; i < top.rows(); i++) {
			for (int j = 0; j < top.cols(); j++) {
				top.get(i, j, top_temp);
				bot.get(i, j, bot_temp);
				
				float a = top_temp[0];
				float b = bot_temp[0];

				if (a == 0) {a = top_min;}
				if (b == 0) {b = bot_min;}

				if (a <= b) {sal_temp[0] = (float) (1.0 - a / b);} 
				else        {sal_temp[0] = (float) (1.0 - b / a);}
			
				Saliency.put(i, j, sal_temp);
			}
		}

		// Saliency.convertTo(Saliency, CvType.CV_8U);

		Mat thr = new Mat(Saliency.rows(), Saliency.cols(), CvType.CV_8UC1);
		Saliency.convertTo(thr, CvType.CV_8UC1, 255); 
		Imgproc.threshold(thr, Saliency, threshMin, threshMax, Imgproc.THRESH_BINARY); // turn to black and white
																			
		int width = Saliency.cols();
		int height = Saliency.rows();


		Mat Saliency2 = new Mat();
		Mat erodeElement = new Mat();
		Mat erodeElement1 = new Mat();
		Mat dilateElement = new Mat();

		erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(Erode_Min, Erode_Max));
		dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(Dilate_Min, Dilate_Max));

		Saliency2 = Saliency.clone();

		Imgproc.erode(Saliency, Saliency, erodeElement);
		Imgproc.erode(Saliency, Saliency, erodeElement); 
		Imgproc.dilate(Saliency, Saliency, dilateElement);
		Imgproc.dilate(Saliency, Saliency, dilateElement); 

		erodeElement1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(Erode_Min, Erode_Max + 1));

		Imgproc.erode(Saliency2, Saliency2, erodeElement1); 
		Imgproc.erode(Saliency2, Saliency2, erodeElement1); 
		Imgproc.dilate(Saliency2, Saliency2, dilateElement); 
		Imgproc.dilate(Saliency2, Saliency2, dilateElement); 
		
		Core.add(Saliency, Saliency2, Saliency);
		Mat copySal = new Mat();
		copySal = Saliency.clone();
		copySal.convertTo(copySal, CvType.CV_8U);
		// AbstractImageProvider.deepCopy(AbstractImageProvider.matToBufferedImage(copySal),disp); // Show Blobs

		// findContours accepts MatOfPoint in java ONLY. this is why conversion is needed.

		ArrayList<MatOfPoint> contours        = new ArrayList<MatOfPoint>();          // all the shapes aka contours in image stored here
		ArrayList<Rect> boundRect             = new ArrayList<Rect>();
		ArrayList<MatOfPoint> contourFinal    = new ArrayList<MatOfPoint>();
		
		Imgproc.findContours(Saliency, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		if(contours.size() < 100){

		    while(true){            // sort by size
				int sortCount = 0;
				for(int i = 0; i < contours.size()-1; i++){
				    MatOfPoint contourHold1, contourHold2;
				    double area1 = Imgproc.contourArea(contours.get(i), false);
				    double area2 = Imgproc.contourArea(contours.get(i+1), false);
				    if (area1 < area2){
						contourHold1   =  contours.get(i);
						contourHold2   = contours.get(i+1);
						contours.set(i, contourHold2);
						contours.set(i+1, contourHold1);
						sortCount++;
				    }
				}
				if(sortCount == 0){break;}
		    }

		    for (int i = 0; i < contours.size(); i++){
				Rect rectHold = new Rect();
				rectHold = Imgproc.boundingRect(new MatOfPoint(contours.get(i)));
	
				if      (i == 0)                    {contourFinal.add(contours.get(i));}
				else if (rectHold.tl().y > horizon) {contourFinal.add(contours.get(i));}
		    }

		    int FinalSize = contourFinal.size();

		    if (FinalSize > 15) {FinalSize = 15;} // if there's 16 objects, look for 15, otherwise look for 1-15

		    for(int i = -1; i < FinalSize; i++){
			
				Rect test = new Rect();
				double area;
				int newX = 100;
				int newY = 100;
	
				if (i == -1){
				    newY = 250;
				    newX = 250;
				    area = Imgproc.contourArea(contourFinal.get(0), false);             // found contour area
				    test = Imgproc.boundingRect(new MatOfPoint(contourFinal.get(0)));
				}		
				else{
				    area = Imgproc.contourArea(contourFinal.get(i), false);             // found contour area
				    test = Imgproc.boundingRect(new MatOfPoint(contourFinal.get(i)));
				}
	
				if (area >= minArea){ // size check
				    
				    Boolean a = true;
				    
				    int oldX = (int) (test.br().x - (test.width/2));
				    int oldY = (int) (test.br().y - (test.height/2));
				    
				    if (test.br().y > horizon && test.width < newX && test.height < newY){
					
						test.width = newX;
						test.height = newY;
						
						int nX = (int) (test.br().x - (test.width/2));
						int nY = (int) (test.br().y - (test.height/2));
						
						int shiftX = nX - oldX;
						int shiftY = nY - oldY;
		
						test.x -= shiftX;
						test.y -= shiftY;
		
						if (test.x <= 0 || test.y <= 0 || test.br().x >= width || test.br().y >= height){a = false;}
						if (a == true) {boundRect.add(test);}			
					}		  
				}
		    }

			for (int i = 0; i < boundRect.size(); i++) {
				Mat holder = new Mat(); // hold the cropped 100x100
				Core.rectangle(ObjFound, boundRect.get(i).tl(), boundRect.get(i).br(), RedBox, 1, 8, 0); // make box in ObjFound
				holder = inputImage.submat(boundRect.get(i)); // put cropped 100x100 in holder
				RegionsOfInterest.add(holder);                 // put holder in array
			}
	
			AbstractImageProvider.deepCopy(AbstractImageProvider.matToBufferedImage(ObjFound), disp);
		}
		return InterestingArea;
	}
	
}


