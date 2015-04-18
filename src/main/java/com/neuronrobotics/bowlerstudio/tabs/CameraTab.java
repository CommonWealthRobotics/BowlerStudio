package com.neuronrobotics.bowlerstudio.tabs;

import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.opencv.core.Mat;
import org.opencv.features2d.KeyPoint;

import com.neuronrobotics.jniloader.AbstractImageProvider;
import com.neuronrobotics.jniloader.HaarDetector;
import com.neuronrobotics.jniloader.IObjectDetector;
import com.neuronrobotics.sdk.util.ThreadUtil;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class CameraTab extends Tab implements EventHandler<Event> {
	private boolean open=true;
	private ArrayList<AbstractImageProvider> imageProviders = new ArrayList<AbstractImageProvider>();
	public CameraTab(AbstractImageProvider p, String name,IObjectDetector detector ){
		setText(name);
		imageProviders.add(p);
		Mat inputImage = new Mat();
		Mat displayImage = new Mat();
		int x=0;
		ArrayList<ImageView> iconsCaptured = new ArrayList<ImageView>();
		ArrayList<ImageView> iconsProcessed = new ArrayList<ImageView>();
		HBox box = new HBox();
		for (AbstractImageProvider img:imageProviders){
			img.getLatestImage(inputImage,displayImage);
			
			ImageView tmp = new ImageView();
			tmp.setImage(img.getLatestJfxImage());
			iconsCaptured.add(tmp);

			detector.getObjects(inputImage, displayImage);
			ImageView ptmp = new ImageView(img.getLatestJfxImage());
			iconsProcessed.add(ptmp);
			box.getChildren().add(ptmp);

		}

		setContent(box);
		
		setOnCloseRequest(this);
		new Thread(){
			public void run(){
				while(open){
					//ThreadUtil.wait(100);
					//System.out.print("\r\nRunning "+getText());
					if(isSelected()){
						for (int i=0;i< imageProviders.size();i++){ //list of image provid
							imageProviders.get(i).getLatestImage(inputImage,displayImage);                        // capture image
							iconsCaptured.get(i).setImage(AbstractImageProvider.matToJfxImage(inputImage));  // show raw image

							KeyPoint[] data = detector.getObjects(inputImage, displayImage);
							iconsProcessed.get(0).setImage(AbstractImageProvider.matToJfxImage(displayImage));	// show processed image
							System.out.println("Got: "+data.length);
							
						}
					}
				}
			}
		}.start();
	}
	@Override
	public void handle(Event event) {
		open=false;
	}

}
