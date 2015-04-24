package com.neuronrobotics.jniloader;

import java.awt.image.BufferedImage;
import java.util.List;

public interface IObjectDetector {
	List<Detection> getObjects(BufferedImage inputImage, BufferedImage displayImage);
	
}
