package com.neuronrobotics.imageprovider;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;

import com.neuronrobotics.sdk.common.BowlerDatagram;

public class URLImageProvider extends AbstractImageProvider {


	private URL url;

	public URLImageProvider(URL url) {
		this.url = url;
	}

	@Override
	protected boolean captureNewImage(BufferedImage imageData) {
		// TODO Auto-generated method stub
		BufferedImage buffImg;

		/*In the constructor*/
		try { buffImg = ImageIO.read(url ); } catch (IOException e) { return false;}
		
		AbstractImageProvider.deepCopy(buffImg,imageData);
		return true;
	}

	@Override
	public void disconnectDeviceImp() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean connectDeviceImp() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ArrayList<String> getNamespacesImp() {
		// TODO Auto-generated method stub
		return null;
	}


}
