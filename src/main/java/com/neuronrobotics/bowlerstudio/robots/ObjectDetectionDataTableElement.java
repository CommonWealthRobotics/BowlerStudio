package com.neuronrobotics.bowlerstudio.robots;

import java.util.ArrayList;

import com.neuronrobotics.imageprovider.Detection;
import com.neuronrobotics.imageprovider.IObjectDetector;

public class ObjectDetectionDataTableElement {

	private IObjectDetector detector;
	private String name;
	private ArrayList<Detection> detections = new ArrayList<Detection>();
	private int storeTime=10;
	
	public ObjectDetectionDataTableElement(IObjectDetector detector, String Name){
		this.setDetector(detector);
		setName(Name);
	}
	
	public void addDetection(Detection d){
		if(d!=null){
			detections.add(d);
		}
		//pop out old elements
		if(detections.size()>storeTime)
			detections.remove(detections.size()-1);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public IObjectDetector getDetector() {
		return detector;
	}

	public void setDetector(IObjectDetector detector) {
		this.detector = detector;
	}
	
}
