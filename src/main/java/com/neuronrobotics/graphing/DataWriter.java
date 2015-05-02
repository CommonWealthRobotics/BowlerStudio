package com.neuronrobotics.graphing;

import java.io.File;

public interface DataWriter {
	public void setFile(File f);
	public void addData(DataChannel c);
	public void cleanup();
}
