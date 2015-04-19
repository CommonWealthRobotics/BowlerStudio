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
	private AbstractImageProvider provider;
	private Mat inputImage = new Mat();
	private Mat displayImage = new Mat();
	private IObjectDetector detector;
	private ImageView iconsProcessed;
	public CameraTab(AbstractImageProvider provider, String name,IObjectDetector detector ){
		this.provider = provider;
		this.detector = detector;
		setText(name);
		HBox box = new HBox();
		provider.getLatestImage(inputImage,displayImage);
		detector.getObjects(inputImage, displayImage);
		iconsProcessed = new ImageView(provider.getLatestJfxImage());
		box.getChildren().add(iconsProcessed);
		setContent(box);
		setOnCloseRequest(this);
		//start the infinite loop
		doUpdate();
	}
	
	private void doUpdate(){
		if(open){
			Platform.runLater(()->{
				if(isSelected()){
					provider.getLatestImage(inputImage,displayImage);                        // capture image
					KeyPoint[] data = detector.getObjects(inputImage, displayImage);
					iconsProcessed.setImage(AbstractImageProvider.matToJfxImage(displayImage));	// show processed image
					System.out.println("Got: "+data.length);
					doUpdate();
				}
			});
		}else{
			System.out.print("\r\nFinished "+getText());
		}
	}
	
	
	@Override
	public void handle(Event event) {
		System.out.print("\r\nCalling stop for "+getText());
		open=false;
	}
}
