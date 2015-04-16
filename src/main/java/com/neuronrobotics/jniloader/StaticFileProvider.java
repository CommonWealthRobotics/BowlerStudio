package com.neuronrobotics.jniloader;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;

import com.neuronrobotics.sdk.common.BowlerDatagram;

public class StaticFileProvider extends AbstractImageProvider {
	
	private File file;

	public StaticFileProvider(File file){
		this.file = file;
	}

	@Override
	protected boolean captureNewImage(Mat imageData) {
		// TODO Auto-generated method stub
		BufferedImage buffImg;

		/*In the constructor*/
		try { buffImg = ImageIO.read(file ); } catch (IOException e) { return false;}
		
		StaticFileProvider.bufferedImageToMat(buffImg,imageData);
		return true;
	}

}
