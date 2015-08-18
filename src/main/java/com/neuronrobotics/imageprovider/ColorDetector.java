package com.neuronrobotics.imageprovider;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
//import org.opencv.features2d.KeyPoint;
import org.opencv.imgproc.Imgproc;

//http://cell0907.blogspot.com/2013/07/tracking-ball-with-javaopencv.html

public class ColorDetector implements IObjectDetector{
	private Mat hsv_image = new Mat();
	private Mat thresholded = new Mat();
	private Mat thresholded2 = new Mat();

	private Mat circles = new Mat(); // No need (and don't know how) to initialize it.
	// The function later will do it... (to a 1*N*CV_32FC3)
//	Scalar hsv_min = new Scalar(200, 200, 200, 0);
//	Scalar hsv_max = new Scalar(255, 255, 255, 0);
//	Scalar hsv_min2 = new Scalar(50, 50, 50, 0);
//	Scalar hsv_max2 = new Scalar(255, 255, 255, 0);
	private double[] data = new double[3];
	private List<Mat> lhsv;
	private Mat array255 ;
	private Mat distance;
	private Scalar hsv_min;
	private Scalar hsv_max;
	private Scalar hsv_min2;
	private Scalar hsv_max2;
	
	public ColorDetector(Mat matImage,Scalar hsv_min, Scalar hsv_max,Scalar hsv_min2 ,Scalar hsv_max2) {
		this.hsv_min = hsv_min;
		this.hsv_max = hsv_max;
		this.hsv_min2 = hsv_min2;
		this.hsv_max2 = hsv_max2;
		lhsv = new ArrayList<Mat>(3);
		array255 = new Mat(matImage.height(), matImage.width(),
				CvType.CV_8UC1);
		array255.setTo(new Scalar(255));
		distance = new Mat(matImage.height(), matImage.width(),
				CvType.CV_8UC1);
		
	}

	public void setThreshhold(Scalar hsv_min, Scalar hsv_max){
		this.hsv_min = hsv_min;
		this.hsv_max = hsv_max;
	}
	public void setThreshhold2(Scalar hsv_min2 ,Scalar hsv_max2){
		this.hsv_min2 = hsv_min2;
		this.hsv_max2 = hsv_max2;
	}

	public List<Detection> getObjects(BufferedImage in, BufferedImage disp){
		Mat inputImage = new Mat();
		AbstractImageProvider.bufferedImageToMat(in,inputImage);
		Mat displayImage = new Mat();
		// One way to select a range of colors by Hue
		Imgproc.cvtColor(inputImage, hsv_image, Imgproc.COLOR_BGR2HSV);
		Core.inRange(hsv_image, hsv_min, hsv_max, thresholded);
		Core.inRange(hsv_image, hsv_min2, hsv_max2, thresholded2);
		Core.bitwise_or(thresholded, thresholded2, thresholded);
		// Notice that the thresholds don't really work as a "distance"
		// Ideally we would like to cut the image by hue and then pick just
		// the area where S combined V are largest.
		// Strictly speaking, this would be something like
		// sqrt((255-S)^2+(255-V)^2)>Range
		// But if we want to be "faster" we can do just (255-S)+(255-V)>Range
		// Or otherwise 510-S-V>Range
		// Anyhow, we do the following... Will see how fast it goes...
		Core.split(hsv_image, lhsv); // We get 3 2D one channel Mats
		Mat S = lhsv.get(1);
		Mat V = lhsv.get(2);
		Core.subtract(array255, S, S);
		Core.subtract(array255, V, V);
		S.convertTo(S, CvType.CV_32F);
		V.convertTo(V, CvType.CV_32F);
		Core.magnitude(S, V, distance);
		Core.inRange(distance, new Scalar(0.0), new Scalar(200.0), thresholded2);
		Core.bitwise_and(thresholded, thresholded2, thresholded);
		// Apply the Hough Transform to find the circles
		Imgproc.GaussianBlur(thresholded, thresholded, new Size(9, 9), 0, 0);
		Imgproc.HoughCircles(thresholded, circles, Imgproc.CV_HOUGH_GRADIENT,
				2, thresholded.height() / 4, 500, 50, 0, 0);
		

		//Make the display image the thresholded image
		thresholded.copyTo(displayImage);
		
		// Imgproc.Canny(thresholded, thresholded, 500, 250);
		// -- 4. Add some info to the image

		data = inputImage.get(210, 210);
	
		// int cols = circles.cols();
		int rows = circles.rows();
		int elemSize = (int) circles.elemSize(); // Returns 12 (3 * 4bytes in a
													// float)
		float[] data2 = new float[rows * elemSize / 4];
		ArrayList<Detection> myArray = new ArrayList<Detection>();
		Point center=null;// 
		
		if (data2.length > 0) {
			circles.get(0, 0, data2); // Points to the first element and reads
										// the whole thing
			// into data2
			for (int i = 0; i < data2.length; i = i + 3) {
				center = new Point(data2[i], data2[i + 1]);
				Size objectSize= new Size((double) data2[i + 2],
						(double) data2[i + 2]);
				// Core.ellipse( this, center, new Size( rect.width*0.5,
				// rect.height*0.5), 0, 0, 360, new Scalar( 255, 0, 255 ), 4, 8,
				// 0 );
				Core.ellipse(displayImage, center,objectSize, 0, 0, 360, new Scalar(255, 0,
						255), 4, 8, 0);
				myArray.add(new Detection(center.x, center.y, objectSize.height));
			}
			if(center != null){
				Core.putText(
						displayImage,
						String.format("Circles (" + String.valueOf(data[0]) + ","
								+ String.valueOf(data[1]) + ","
								+ String.valueOf(data[2]) + ")"), new Point(30, 30), 2 // FONT_HERSHEY_SCRIPT_SIMPLEX
						, .5, new Scalar(100, 10, 10, 255), 3);
				for(int i=0;i<myArray.size();i++){

						Point centerTmp =  new Point(myArray.get(i).getX(), myArray.get(i).getY());
						Core.line(displayImage, new Point(150, 50), centerTmp,
								new Scalar(100, 10, 10)/* CV_BGR(100,10,10) */, 3);
						Core.circle(displayImage, centerTmp, 10, new Scalar(100, 10, 10),
								3);
					
				}
			}
		}
		
		
		AbstractImageProvider.deepCopy(AbstractImageProvider.matToBufferedImage(displayImage),disp);
		return myArray;
	}
}


