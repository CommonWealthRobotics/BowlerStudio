package com.neuronrobotics.imageprovider;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.Tab;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.KeyPoint;

// any java file starting with i is the interface

public class OpenCVTest  {
	private ArrayList<IObjectDetector> detectors;
	private RGBColorDetector mainFilter;

	public void run() { 
		HaarDetector faceDetectorObject = new HaarDetector();

		BufferedImage inputImage = AbstractImageProvider.newBufferImage(640,480);
		BufferedImage displayImage =  AbstractImageProvider.newBufferImage(640,480);
		
		JFrame frame = new JFrame();

		JTabbedPane  tabs = new JTabbedPane();
		frame .setContentPane(tabs);
		frame.setSize(640, 580);
		frame.setVisible(true);
		frame .setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		detectors = new ArrayList<IObjectDetector>();
		ArrayList<AbstractImageProvider> imageProviders = new ArrayList<AbstractImageProvider>();
		ArrayList<ImageIcon> iconsCaptured = new ArrayList<ImageIcon>();
		ArrayList<ImageIcon> iconsProcessed = new ArrayList<ImageIcon>();
		
		Scalar upper =new Scalar(30, 150, 0, 0);
		Scalar lower =new Scalar(240, 166, 0, 0);
		
		Scalar upper1 =new Scalar(360, 255, 255, 0);
		Scalar lower1 =new Scalar(240, 0, 0, 0);
		
		
		imageProviders.add(new OpenCVImageProvider(0));               // Image provider
		imageProviders.get(0).getLatestImage(inputImage,displayImage);
		// Provides the static file to the processors
		imageProviders.add(new StaticFileProvider(new File("image.png")));
		
		
//		mainFilter = new RGBColorDetector(inputImage, 
//				lower,
//				upper, 
//				lower1, 
//				upper1);

		//add human detector later
		//detectors.add(faceDetectorObject);
		detectors.add(new SalientDetector());
		//detectors.add(new WhiteBlobDetect((int) upper.val[0],(int) upper.val[1], lower));
		Tab t = new Tab();
		
		int x=0;
		for (AbstractImageProvider img:imageProviders){
			img.getLatestImage(inputImage,displayImage);
			
			ImageIcon tmp = new ImageIcon(img.getLatestImage());
			iconsCaptured.add(tmp);
			
			tabs.addTab("Camera "+x, new JLabel(tmp));
			
			for (int i=0;i<detectors.size();i++){
				detectors.get(i).getObjects(inputImage, displayImage);
				ImageIcon ptmp = new ImageIcon(img.getLatestImage());
				iconsProcessed.add(ptmp);
				tabs.addTab("Processed "+x+"."+i, new JLabel(ptmp));
			}
			x++;
		}
		
		while (true){
			try{
				for (int i=0;i< imageProviders.size();i++){ //list of image provid
					imageProviders.get(i).getLatestImage(inputImage,displayImage);                        // capture image
					iconsCaptured.get(i).setImage(inputImage);  // show raw image
				
					for (int j=0;j<detectors.size();j++){   // list of object detectors
						List<Detection> data = detectors.get(j).getObjects(inputImage, displayImage);
						iconsProcessed.get(i*j).setImage(displayImage);	// show processed image
						
						//System.out.println("Got: "+data.length);
						
					}
					frame.repaint();
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}   
	  }

	/**
	 * @param args
	 */
	public static void main(String[] args) { // Main entry for object detection 
		
        
        new OpenCVTest().run();              // starts
	}


}
