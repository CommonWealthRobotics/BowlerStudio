// How to boxes in case more stuff goes bad http://stackoverflow.com/questions/26814069/how-to-set-region-of-interest-in-opencv-java
//				AbstractImageProvider.deepCopy(AbstractImageProvider.matToBufferedImage(Saliency), disp);

// ssdasd

//set microsoft life 3000 to approperiate settings or the low sat will cause havoc
//v4l2-ctl --set-ctrl=contrast=10,saturation=200,white_balance_temperature_auto=0,brightness=30 -d/dev/video1
package com.neuronrobotics.imageprovider;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.math.*;

import org.opencv.core.Core;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Size;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.python.modules.math;

public class SalientDetector implements IObjectDetector {

	int countPositiveHits = 0;
	int startReturning = 20;
	int timeBetweenPosHits = 0;
	
	Boolean avoidDeath = false;
	Boolean findNorth = false;
	Boolean findHome = false;
	
	Boolean stage1 = true;
	Boolean stage2 = false;
	Boolean stage3 = false;
	
	Boolean Object_Dropped = false;
	
	int stage1_count = 0;
	int stage2_count = 0;
	int stage3_count = 0;
	int dropped_it = 0;
	
	int s1_limit = 30;
	int s2_limit = 30;
	int s3_limit = 15;
	
	Scalar RedBox    = new Scalar(  0,   0, 255);
	Scalar YellowBox = new Scalar(255, 255,   0);
	
	Mat ComparTo = new Mat(); // STAGE 3 USE ONLY, check frames after this Mat is set to see how different it is.
	double CompareTo_Area = 0;
	Boolean prev = false;
	
	public SalientDetector(){}
	
	public SalientDetector(Boolean s1,Boolean s2,Boolean s3, Boolean s4, Boolean s5, Boolean s6){
		stage1=s1;
		stage2=s2;
		stage3=s3;
		avoidDeath=s4;
		findNorth=s5;
		findHome=s6;
	}
	
	
	public ArrayList<Rect> Stage2_Canny(Mat a, ArrayList<Rect> b){
		
		return b;	
	}
	public Mat FindSalient(Mat original, Mat Sal, Mat erodeElement, Mat dilateElement){
		
		int PyrSize = 5;
		
		Mat GaussBlur = new Mat();  // GaussBlured image to reduce noise
	
		Mat a_mat = new Mat(); // top level of pyramid
		Mat b_mat = new Mat(); // GaussBlured image to compare to
	
		Mat DS = new Mat();
		Mat UP = new Mat();
		
		GaussBlur = original.clone();
		
		Imgproc.GaussianBlur(GaussBlur, GaussBlur, new Size(5,5), 0);
		Imgproc.cvtColor(GaussBlur, GaussBlur, Imgproc.COLOR_BGR2GRAY);
		
		GaussBlur.convertTo(GaussBlur, CvType.CV_32F);
		
		DS = GaussBlur.clone();
		for (int i = 0; i < PyrSize; i++) {
			Mat a = new Mat();
			Imgproc.pyrDown(DS, a);
	
			float[] blah = new float[1];
			blah[0] = (float) 10;
			float[] bleh = new float[1];
	
			for (int j = 0; j < a.rows(); j++) {
				for (int k = 0; k < a.cols(); k++) {
					a.get(j, k, bleh);
					bleh[0] = bleh[0] + blah[0];
					a.put(j, k, bleh);
				}
			}
			DS = a.clone();
		}
	
		UP = DS.clone();
	
		for (int i = 0; i < PyrSize; i++) {
			Mat a = new Mat();
			Imgproc.pyrUp(UP, a);
			
			float[] blah = new float[1];
			blah[0] = (float)10;
			float[] bleh = new float[1];
			for (int j = 0; j < a.rows(); j++) {
				for (int k = 0; k < a.cols(); k++) {
					a.get(j, k, bleh);
					bleh[0] = bleh[0] + blah[0];
					a.put(j, k, bleh);
				}
			}
			UP = a.clone();
		}
	
		a_mat      = UP.clone();  
		b_mat      = GaussBlur.clone();
		Sal = GaussBlur.clone();
	 
		int cSize = a_mat.channels();
		
		float[] a_mat_temp = new float[cSize];
		float[] b_mat_temp = new float[cSize];
		float[] sal_temp = new float[cSize];
	
		for (int i = 0; i < a_mat.rows(); i++){
		    for (int j = 0; j < a_mat.cols(); j++){
		    	a_mat.get(i, j, a_mat_temp);
				b_mat.get(i, j, b_mat_temp);
				
				float a = a_mat_temp[0];
				float b = b_mat_temp[0];
				
		 
				if (a <= b) {sal_temp[0] = (float) (1 - a/b);}
				else        {sal_temp[0] = (float) (1 - b/a);}
				
				sal_temp[0] = (float) math.sqrt(sal_temp[0]);
				
				Sal.put(i,j, sal_temp);
		    }
		}
	
		Mat thr = new Mat(Sal.rows(), Sal.cols(), CvType.CV_8UC1);
		Sal.convertTo(thr, CvType.CV_8UC1, 255); // change float to 1-255
		Imgproc.threshold(thr, Sal, 150, 255,Imgproc.THRESH_BINARY_INV); // turn to black and white
		
		Imgproc.erode(Sal, Sal, erodeElement);
		Imgproc.erode(Sal, Sal, erodeElement);
		Imgproc.erode(Sal, Sal, erodeElement);
		
		Imgproc.dilate(Sal, Sal, dilateElement);
		Imgproc.dilate(Sal, Sal, dilateElement);
		Imgproc.dilate(Sal, Sal, dilateElement);
		Imgproc.dilate(Sal, Sal, dilateElement);
		
		return Sal;
	}
	
	public ArrayList<Rect> SortFindContours(ArrayList<MatOfPoint> a, ArrayList<Rect> b, Mat c, int Horizon, int minArea){
		
		ArrayList<MatOfPoint> contourFinal = new ArrayList<MatOfPoint>();
		
		if (a.size() != 0 && a.size() < 100){
			
			while (true) {        // Sort a by size and what not
				int sortCount = 0;
				for (int i = 0; i < a.size() - 1; i++) {

					MatOfPoint contourHold1, contourHold2;
					double area1 = Imgproc.contourArea(a.get(i));
					double area2 = Imgproc.contourArea(a.get(i + 1));

					if (area1 < area2) {
						contourHold1 = a.get(i);
						contourHold2 = a.get(i + 1);
						a.set(i, contourHold2);
						a.set(i + 1, contourHold1);
						sortCount++;
					}
				}
				if (sortCount == 0) {break;}
			}

			for (int i = 0; i < a.size(); i++) {
				Rect rectHold = new Rect();
				rectHold = Imgproc.boundingRect(new MatOfPoint(a.get(i)));

				if (i == 0)                         {contourFinal.add(a.get(i));} 
				else if (rectHold.tl().y > Horizon) {contourFinal.add(a.get(i));}
			}

			int FinalSize = contourFinal.size();
			
			if (FinalSize > 15) {FinalSize = 15;} // if there's 16 objects, look for 15, otherwise look for 1-15

			for (int i = -1; i < FinalSize; i++) {

				Rect test = new Rect();
				double area;
				int newX = 100;
				int newY = 100;
		        
				if (i == -1) {
					newY = 250;
					newX = 250;
					
					test = Imgproc.boundingRect(new MatOfPoint(contourFinal.get(0))); // test = rect around the contour's most outer extremes
					area = Imgproc.contourArea(contourFinal.get(0));
				} 
				else {
					test = Imgproc.boundingRect(new MatOfPoint(contourFinal.get(i))); // test = rect around the contour's most outer extremes
					area = Imgproc.contourArea(contourFinal.get(i));
				}

				if (area >= minArea){ // size check

					Boolean addThis = true;

					int oldX = (int) (test.br().x - (test.width / 2));
					int oldY = (int) (test.br().y - (test.height / 2));

					if (test.width < newX && test.height < newY) { //test.br().y > Horizon && test

						test.width = newX;
						test.height = newY;

						int nX = (int) (test.br().x - (test.width / 2));
						int nY = (int) (test.br().y - (test.height / 2));

						int shiftX = nX - oldX;
						int shiftY = nY - oldY;

						test.x -= shiftX;
						test.y -= shiftY;

						if (test.x <= 0 || test.y <= 0 || test.br().x >= c.cols() || test.br().y >= c.rows()) {addThis = false;}
						if (addThis == true) {b.add(test);}
					}  
				}
			}
		}
		return b;
	}
	
	@Override
	public List<Detection> getObjects(BufferedImage inImg, BufferedImage disp) {

		ArrayList<Detection> ReturnedArea = new ArrayList<Detection>(); // areas
		
		Mat original = new Mat(); // original webcam image
		
		AbstractImageProvider.bufferedImageToMat(inImg, original);// ACCESS
		
		if (original.empty() == false){ // Prevent runtime exception incase bowler derps and doesn't give a frame
			
			ArrayList<Rect> STAGE1_BOXES = new ArrayList<Rect>();
			ArrayList<Rect> STAGE2_BOXES = new ArrayList<Rect>(); 	

			Mat ObjFound = new Mat();   // Where stuff is found and red boxes drawn
			ObjFound = original.clone();
			
			 if (stage2 == true){ // lowering
				
				int Horizon = 100;
				int minArea = 100;
				Mat Canny = new Mat();

				Canny = original.clone();
				Imgproc.cvtColor(Canny, Canny, Imgproc.COLOR_BGR2GRAY);
				
				Imgproc.blur(Canny, Canny, new Size(3,3));
				Imgproc.Canny(Canny, Canny, 100, 300, 3, false);
				
				ArrayList<MatOfPoint> contourCanny = new ArrayList<MatOfPoint>();
				Imgproc.findContours(Canny, contourCanny, new Mat(),Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
					
				STAGE2_BOXES = SortFindContours(contourCanny, STAGE2_BOXES, Canny,  Horizon,  minArea);
				
				for (int i = 0; i < STAGE2_BOXES.size(); i++){
					Core.rectangle(ObjFound, STAGE2_BOXES.get(i).tl(), STAGE2_BOXES.get(i).br(), RedBox, 1, 8, 0);
				}						
			 }
			
			 else if (stage1 == true || stage3 == true){ // captured, now retrieving
				 
				int Horizon = 100;
				int minArea = 100;
		
				int Erode_Max = 2;
				int Erode_Min = 2;
		
				int Dilate_Max = 5;
				int Dilate_Min = 5;
		
				Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(Erode_Min, Erode_Max));
				Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(Dilate_Min, Dilate_Max));
						
				Mat Saliency = new Mat();   // Saliency of image
				
				Saliency = FindSalient(original, Saliency, erodeElement, dilateElement);
				
				if (stage1 == false && stage3 == true){ // This is stage 3, it is all solo
					if (prev == false){
						ComparTo = Saliency.clone();

						ArrayList<MatOfPoint> compCont = new ArrayList<MatOfPoint>(); 
						Imgproc.findContours(ComparTo, compCont, new Mat(),Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

						for (int i = 0; i < compCont.size(); i++){
							double area = Imgproc.contourArea(compCont.get(i));
							CompareTo_Area = CompareTo_Area + area;
						}
						
						prev = true;
					}
					else{
						double total = 0;
						ArrayList<MatOfPoint> currCont = new ArrayList<MatOfPoint>();
						Imgproc.findContours(Saliency, currCont, new Mat(),Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
						for (int i = 0; i < currCont.size(); i++){
							double area = Imgproc.contourArea(currCont.get(i));
							total = total + area;
						}
						
						if (total < CompareTo_Area / 2){ // if total is greater, fine whatever weird lighting happened. 
							Object_Dropped = true;
						}
						
					}
				}
				
				else if (stage1 == true && stage3 == false){
	
					ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>(); 
					
					Imgproc.findContours(Saliency, contours, new Mat(),Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
					
					STAGE1_BOXES = SortFindContours(contours, STAGE1_BOXES, Saliency, Horizon, minArea);		
				    
					for (int i = 0; i < STAGE1_BOXES.size(); i++) {
						Core.rectangle(ObjFound, STAGE1_BOXES.get(i).tl(), STAGE1_BOXES.get(i).br(), RedBox, 1, 8, 0);
					}
		
					// PROCESS SMALL AREAS ******************************************************************************************************
					// public ArrayList<Detection> ProcessInterestingAreas(Mat a, ArrayList<Rect> b)
					
					int bigBox_X = 0, bigBox_Y = 0;
		
					for (int a1 = 0; a1 < STAGE1_BOXES.size(); a1++) { // process those small
														
						int returnArea_X, returnArea_Y;
			
						Rect aRect = new Rect(); // copy of the rect
						Mat ObjectTemp = new Mat(); // 100x100 or 250x250 cutout
						Mat colorResult = new Mat(); // hsv version of ObjectTemp
			
						int contMinArea = 100;
						int numbOfObj = 10;
						int ObjCent = 50;
						int centoffset = 20;
						int edge = 5;
						
						aRect = STAGE1_BOXES.get(a1);  // copy of rect
						ObjectTemp = original.submat(aRect).clone();                  // cutout of 100x100 or 250x250
						
						returnArea_X = (int) (aRect.br().x - (aRect.width / 2));
						returnArea_Y = (int) (aRect.br().y - (aRect.height / 2));
									
						int ObjWidth = ObjectTemp.cols(); // the first time it runs 250x250
						int ObjHeight = ObjectTemp.rows();
			
						if (ObjWidth == 250 && ObjHeight == 250) { // 100x100 squares
							contMinArea = 300;
							numbOfObj = 20;
							centoffset = 80;
							ObjCent = 125;
							edge = 5;
						}
			
						Scalar white_min1 = new Scalar(0, 0, 100);
						Scalar white_max1 = new Scalar(180, 100, 200);
						
						Scalar white_min2 = new Scalar(0,0,0);
						Scalar white_max2 = new Scalar(180,50, 256);
			
						Scalar white_min3 = new Scalar(0,0,0);
						Scalar white_max3 = new Scalar(180,30, 256);
						
						Scalar pink_min1 = new Scalar(120, 0, 0); // very very very distinct
						Scalar pink_max1 = new Scalar(180, 0, 256);
			
						Scalar red_min1 = new Scalar(0, 0, 0);
						Scalar red_max1 = new Scalar(10, 256, 256);
			
						Scalar red_min2 = new Scalar(0, 0, 0);
						Scalar red_max2 = new Scalar(10, 256, 256);
			
						Scalar yellow_min1 = new Scalar(0, 0, 0);
						Scalar yellow_max1 = new Scalar(30, 256, 256);
			
						Scalar grey_min1 = new Scalar(0, 0, 200);
						Scalar grey_max1 = new Scalar(0, 0, 256);
			
						ArrayList<MatOfPoint> resultCont = new ArrayList<MatOfPoint>();
						Imgproc.cvtColor(ObjectTemp, ObjectTemp, Imgproc.COLOR_BGR2HSV);

						// ArrayList<MatOfPoint> FinalContours = new ArrayList<MatOfPoint>();
						// ONLY FINDS WHITE FOR NOW
			
						Boolean bigbox = false;
						
						for (int cc = 0; cc < 3; cc++){    // ITERATE THROUGH COLOR RANGES AND STUFF
	
							Boolean smallbox = false;
							
							colorResult = ObjectTemp.clone();
							
							if      (cc == 0) {Core.inRange(colorResult, white_min1, white_max1, colorResult);}
							else if (cc == 1) {Core.inRange(colorResult, white_min2, white_max2, colorResult);}
							else if (cc == 2) {Core.inRange(colorResult, white_min3, white_max3, colorResult);}
							
							Imgproc.erode(colorResult, colorResult, erodeElement);
							Imgproc.erode(colorResult, colorResult, erodeElement);

							Imgproc.dilate(colorResult, colorResult, dilateElement);
							Imgproc.dilate(colorResult, colorResult, dilateElement);
			
							Imgproc.findContours(colorResult, resultCont, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			
							if (resultCont.size() >= 1 && resultCont.size() <= numbOfObj){
								for (int z = 0; z < resultCont.size(); z++) {
									double area = Imgproc.contourArea(resultCont.get(z));
									double confidence = 0;

									if (area > contMinArea) {
										//if (area < 200) {centoffset = 40;}
		
										Rect test = Imgproc.boundingRect(new MatOfPoint(resultCont.get(z)));
			
										int tl_x = (int) test.tl().x; // top left x
										int tl_y = (int) test.tl().y; // top left y
			
										int br_x = (int) test.br().x; // bottom right x
										int br_y = (int) test.br().y; // bottom right y
			
										int centX = (int) br_x - (test.width / 2); // set center X
										int centY = (int) br_y - (test.width / 2); // set center Y
			
										if (tl_x - edge > 0 && tl_y - edge > 0 && br_x + edge < ObjWidth && br_y + edge < ObjHeight) {
											int X1 = ObjCent - centoffset; int X2 = ObjCent + centoffset;
											int Y1 = ObjCent - centoffset; int Y2 = ObjCent + centoffset;
			
											if (centX >= X1 && centX <= X2 && centY >= Y1 && centY <= Y2) {	 // VALID INTERESTING AREA
												
												stage1_count++;
												returnArea_X = (int) (aRect.br().x - (aRect.width/2)); 
												returnArea_Y = (int) (aRect.br().y - (aRect.height/2));
												
												if(ObjWidth == 250 && ObjHeight == 250){
													bigBox_X = returnArea_X;
													bigBox_Y = returnArea_Y;
													bigbox = true;
												}
												else {
													smallbox = true;
												}
												
												Core.rectangle(ObjFound, STAGE1_BOXES.get(a1).tl(), STAGE1_BOXES.get(a1).br(), YellowBox, 2,8,0);
												AbstractImageProvider.deepCopy(AbstractImageProvider.matToBufferedImage(ObjFound), disp);

												double m = (double)returnArea_X;
												double n = (double)returnArea_Y;
												
												if (smallbox == true && bigbox == true){  // if small box is inside big box
													if (bigBox_X == returnArea_X && bigBox_Y == returnArea_Y){
														confidence = 1;
														
													}
												}
												else if (smallbox == true){
													if (area < 110)     {confidence = 0.1;}
													else if (area < 120){confidence = 0.2;}
													else if (area < 150){confidence = 0.3;}
													else if (area < 200){confidence = 0.4;}
													else if (area < 250){confidence = 0.5;}
													else if (area < 260){confidence = 0.6;}
													else if (area < 280){confidence = 0.7;}
													else if (area < 300){confidence = 0.8;}
													else if (area > 300){confidence = 0.9;}
												}
												
												Detection INTERESTING = new Detection(m, n, area, confidence);
												ReturnedArea.add(INTERESTING);
												countPositiveHits++;
											    System.out.println("STUFF FOUND" + m + "," + n + " AREA : " + area + " CONFIDENCE : " + confidence);
												
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return ReturnedArea;
	}
}

