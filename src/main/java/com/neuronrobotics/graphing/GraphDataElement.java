package com.neuronrobotics.graphing;

public class GraphDataElement {
	private long ms;
	private double [] data;
	public GraphDataElement(long currentTimeMillis, double [] data) {
		setTimestamp(currentTimeMillis);
		this.setData(data);
	}
	public void setData(double [] data) {
		this.data = data;
	}
	public double [] getData() {
		return data;
	}
	public void setTimestamp(long ms) {
		this.ms = ms;
	}
	public long getTimestamp() {
		return ms;
	}

}
