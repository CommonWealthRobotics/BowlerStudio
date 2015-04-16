package com.neuronrobotics.jniloader;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.KeyPoint;

public interface IObjectDetector {
	KeyPoint[] getObjects(Mat inputImage, Mat displayImage);
	
	public void setThreshhold(Scalar rgb_min, Scalar rgb_max);
	public void setThreshhold2(Scalar rgb_min2 ,Scalar rgb_max2);
	
}
