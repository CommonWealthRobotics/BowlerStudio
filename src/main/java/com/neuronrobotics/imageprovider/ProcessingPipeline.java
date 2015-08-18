package com.neuronrobotics.imageprovider;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.features2d.KeyPoint;

public class ProcessingPipeline {
	private ArrayList<AbstractImageProvider> imageProviders = new ArrayList<AbstractImageProvider>();
	private ArrayList<IObjectDetector> detectors= new ArrayList<IObjectDetector>();
	
	public void addAbstractImageProvider(AbstractImageProvider newIp){
		imageProviders.add(newIp);
	}
	
	public BufferedImage getLatestImage(int index,BufferedImage inputImage, BufferedImage displayImage){
		return imageProviders.get(index).getLatestImage(inputImage,displayImage);
	}
	
	public List<Detection> getObjects(int index,BufferedImage inputImage, BufferedImage displayImage){
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
