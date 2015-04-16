package com.neuronrobotics.jniloader;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.features2d.KeyPoint;

public class ProcessingPipeline {
	private ArrayList<AbstractImageProvider> imageProviders = new ArrayList<AbstractImageProvider>();
	private ArrayList<IObjectDetector> detectors= new ArrayList<IObjectDetector>();
	
	public void addAbstractImageProvider(AbstractImageProvider newIp){
		imageProviders.add(newIp);
	}
	
	public BufferedImage getLatestImage(int index,Mat inputImage, Mat displayImage){
		return imageProviders.get(index).getLatestImage(inputImage,displayImage);
	}
	
	public KeyPoint[] getObjects(int index,Mat inputImage, Mat displayImage){
		return detectors.get(index).getObjects(inputImage, displayImage);
	}
	
	public void addDetector(IObjectDetector newDetect){
		detectors.add(newDetect);
	}
	
	public int getProviderSize(){
		return imageProviders.size();
	}
	
	public int getDetectorSize(){
		return detectors.size();
	}
	
}
