package com.neuronrobotics.jniloader;

public class Detection {

	private final double X;
	private final double Y;
	private final double Size;
	private double confidence;
	
	public Detection(double x, double y, double size){
		X=x;
		Y=y;
		Size=size;
		this.setConfidence(1.0);
	}
	/**
	 * 
	 * @param x detection location in image
	 * @param y detection location in image
	 * @param size detection location in image
	 * @param confidence confidence value from 0-1 of confidence in detection
	 */
	public Detection(double x, double y, double size, double confidence){
		X=x;
		Y=y;
		Size=size;
		this.setConfidence(confidence);
	}

	public double getSize() {
		return Size;
	}

	public double getY() {
		return Y;
	}

	public double getX() {
		return X;
	}
	public String toString(){
		return "X pos ="+X+" Y pos="+Y+" size="+Size; 
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}
}
