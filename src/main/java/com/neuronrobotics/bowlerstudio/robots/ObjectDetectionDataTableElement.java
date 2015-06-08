package com.neuronrobotics.bowlerstudio.robots;

import com.neuronrobotics.jniloader.IObjectDetector;

public class ObjectDetectionDataTableElement {

	private IObjectDetector detector;
	private String name;
	
	
	public ObjectDetectionDataTableElement(IObjectDetector detector, String Name){
		this.detector = detector;
		name = Name;
	}
}
