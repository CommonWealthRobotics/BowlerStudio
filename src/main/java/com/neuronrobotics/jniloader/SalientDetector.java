package com.neuronrobotics.jniloader;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.features2d.KeyPoint;

public class SalientDetector implements IObjectDetector {

	@Override
	public KeyPoint[] getObjects(Mat inputImage, Mat displayImage) {
		// TODO Auto-generated method stub
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
