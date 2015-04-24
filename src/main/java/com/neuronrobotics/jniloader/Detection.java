package com.neuronrobotics.jniloader;

public class Detection {

	private final double X;
	private final double Y;
	private final double Size;
	
	public Detection(double x, double y, double size){
		X=x;
		Y=y;
		Size=size;
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
}
