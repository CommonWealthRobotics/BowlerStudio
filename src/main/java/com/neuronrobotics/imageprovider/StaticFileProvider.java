package com.neuronrobotics.imageprovider;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;

import com.neuronrobotics.sdk.common.BowlerDatagram;

public class StaticFileProvider extends AbstractImageProvider {
	
	private File file;

	public StaticFileProvider(File file){
		this.file = file;
	}

	@Override
	protected boolean captureNewImage(BufferedImage imageData) {
		// TODO Auto-generated method stub
		BufferedImage buffImg;

		/*In the constructor*/
		try { buffImg = ImageIO.read(file ); } catch (IOException e) { return false;}
		
		AbstractImageProvider.deepCopy(buffImg,imageData);
		return true;
	}

	@Override
	public void disconnect() {
		//ignore
	}

}
