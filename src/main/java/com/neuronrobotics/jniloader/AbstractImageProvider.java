package com.neuronrobotics.jniloader;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;

import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;

import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.BowlerDatagram;
import com.neuronrobotics.sdk.common.InvalidConnectionException;

public abstract class AbstractImageProvider extends BowlerAbstractDevice {
	private BufferedImage image = null;
	private MatOfByte mb = new MatOfByte();
	/**
	 * This method should capture a new image and load it into the Mat datatype
	 * @param imageData
	 * @return
	 */
	protected abstract boolean captureNewImage(Mat imageData);
	
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
	
	/**
	 * This method tells the connection object to disconnect its pipes and close out the connection. Once this is called, it is safe to remove your device.
	 */
	@Override
	public void disconnect() {

	}
	
	
	public BufferedImage getLatestImage(Mat inputImage, Mat displayImage){
		captureNewImage(inputImage);
//		try {
//		    // retrieve image
//		    BufferedImage bi = matToBufferedImage(inputImage);
//		    File outputfile = new File("Robot_Log_Image"+System.currentTimeMillis()+".png");
//		    ImageIO.write(bi, "png", outputfile);
//		    
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		inputImage.copyTo(displayImage);
		
		
		try {
			Highgui.imencode(".jpg", inputImage, mb);
			image = ImageIO.read(new ByteArrayInputStream(mb.toArray()));
		} catch (IOException|CvException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return image;
	}
	
	public BufferedImage getLatestImage(){
		return image;
	}
	
	/**
	 * Converts/writes a Mat into a BufferedImage.
	 * 
	 * @param matrix
	 *            Mat of type CV_8UC3 or CV_8UC1
	 * @return BufferedImage of type TYPE_3BYTE_BGR or TYPE_BYTE_GRAY
	 */
	public static BufferedImage matToBufferedImage(Mat matrix) {
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
	public static Image matToJfxImage(Mat matrix) {
		
		return getJfxImage(matToBufferedImage( matrix) ) ;
	}
	
	public static void bufferedImageToMat(BufferedImage input, Mat output){
		Mat mb;

		byte[] tmpByteArray = ((DataBufferByte) input.getRaster().getDataBuffer()).getData();
		mb = new Mat(input.getHeight(),input.getWidth(),16); //8uc3
		System.out.println("Image was ("+input.getWidth()+"x"+input.getHeight()+") array should be: ("+input.getWidth()*input.getHeight()*3+") and is: ("+tmpByteArray.length+")\n Mat is: ("+mb+")");
	    mb.put(0, 0, tmpByteArray);
	    mb.copyTo(output);
	    System.out.println(output);
		//Mat matImageLocal =Highgui.imdecode(mb, 0);
		//matImageLocal.copyTo(output);
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
		 WritableImage wr = null;
       if (bf != null) {
           wr = new WritableImage(bf.getWidth(), bf.getHeight());
           PixelWriter pw = wr.getPixelWriter();
           for (int x = 0; x < bf.getWidth(); x++) {
               for (int y = 0; y < bf.getHeight(); y++) {
                   pw.setArgb(x, y, bf.getRGB(x, y));
               }
           }
       }
       return wr;
	}
	public Image getLatestJfxImage() {
		return getJfxImage(getLatestImage());
	}
}
