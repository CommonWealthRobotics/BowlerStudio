package com.neuronrobotics.imageprovider;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;

import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.BowlerDatagram;
import com.neuronrobotics.sdk.common.InvalidConnectionException;
import com.neuronrobotics.sdk.common.NonBowlerDevice;

public abstract class AbstractImageProvider extends NonBowlerDevice {
	private BufferedImage image = null;
	/**
	 * This method should capture a new image and load it into the Mat datatype
	 * @param imageData
	 * @return
	 */
	protected abstract boolean captureNewImage(BufferedImage imageData);
	
	@Override
	public void onAsyncResponse(BowlerDatagram data) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean connect(){
		return true;
	}
	
	/**
	 * Determines if the device is available.
	 *
	 * @return true if the device is avaiable, false if it is not
	 * @throws InvalidConnectionException the invalid connection exception
	 */
	@Override
	public boolean isAvailable() throws InvalidConnectionException{
		return true;
	}
	

	public static void deepCopy(org.opencv.core.Mat src, BufferedImage dest) {
		Graphics g = dest.createGraphics();
		g.drawImage(matToBufferedImage(src), 0, 0, null);
	}
	
	public static void deepCopy(BufferedImage src, BufferedImage dest) {
		Graphics g = dest.createGraphics();
		g.drawImage(src, 0, 0, null);
	}
	
	public BufferedImage getLatestImage(BufferedImage inputImage, BufferedImage displayImage){
		captureNewImage(inputImage);
		if(displayImage!=null){
			AbstractImageProvider.deepCopy(inputImage,displayImage);
		}
		image = inputImage;
		
		return image;
	}
	
	public BufferedImage getLatestImage(){
		return image;
	}
	
	public static BufferedImage newBufferImage(int w, int h) {
		return new BufferedImage(w, h,  BufferedImage.TYPE_3BYTE_BGR);
	
	}
	/**
	 * Converts/writes a Mat into a BufferedImage.
	 * 
	 * @param matrix
	 *            Mat of type CV_8UC3 or CV_8UC1
	 * @return BufferedImage of type TYPE_3BYTE_BGR or TYPE_BYTE_GRAY
	 */
	public static BufferedImage matToBufferedImage(org.opencv.core.Mat matrix) {
		int cols = matrix.cols();
		int rows = matrix.rows();
		int elemSize = (int) matrix.elemSize();
		byte[] data = new byte[cols * rows * elemSize];
		int type;
		matrix.get(0, 0, data);
		switch (matrix.channels()) {
		case 1:
			type = BufferedImage.TYPE_BYTE_GRAY;
			break;
		case 3:
			type = BufferedImage.TYPE_3BYTE_BGR;
			// bgr to rgb
			byte b;
			for (int i = 0; i < data.length; i = i + 3) {
				b = data[i];
				data[i] = data[i + 2];
				data[i + 2] = b;
			}
			break;
		default:
			return null;
		}
		BufferedImage image2 = new BufferedImage(cols, rows, type);
		image2.getRaster().setDataElements(0, 0, cols, rows, data);
		return image2;
	}
	/**
	 * Converts/writes a Mat into a BufferedImage.
	 * 
	 * @param matrix
	 *            Mat of type CV_8UC3 or CV_8UC1
	 * @return BufferedImage of type TYPE_3BYTE_BGR or TYPE_BYTE_GRAY
	 */
	public static Image matToJfxImage(org.opencv.core.Mat matrix) {

		return getJfxImage(matToBufferedImage( matrix) ) ;
	}
	
	public static void bufferedImageToMat(BufferedImage input, org.opencv.core.Mat output){
		org.opencv.core.Mat mb;

		byte[] tmpByteArray = ((DataBufferByte) input.getRaster().getDataBuffer()).getData();
		mb = new org.opencv.core.Mat(input.getHeight(),input.getWidth(),16); //8uc3
	    mb.put(0, 0, tmpByteArray);
	    mb.copyTo(output);
	}

	public static  BufferedImage toGrayScale(BufferedImage in, int w, int h) {
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
		Graphics g = bi.createGraphics();
		g.drawImage(in, 0, 0, w, h, null);
		return bi;
	}

	public  static BufferedImage toGrayScale(BufferedImage in, double scale) {
		int w = (int) (in.getWidth() * scale);
		int h = (int) (in.getHeight() * scale);
		return toGrayScale(in, w, h);
	}
	public static Image getJfxImage(BufferedImage bf) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
	    try {
	        ImageIO.write( bf, "png", out);
	        out.flush();
	        } catch (IOException ex) {
	           
	        }
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
	    return new javafx.scene.image.Image(in);
	}
	public Image getLatestJfxImage() {
		return getJfxImage(getLatestImage());
	}
}
