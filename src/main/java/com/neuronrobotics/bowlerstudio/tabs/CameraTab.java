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
	private IObjectDetector detector;
	private ImageView iconsProcessed;
	KeyPoint[] data;
	public CameraTab(AbstractImageProvider provider, String name,IObjectDetector detector ){
		this.provider = provider;
		this.detector = detector;
		setText(name);
		HBox box = new HBox();
		// perform the first capture
		provider.getLatestImage(inputImage,null); 
		iconsProcessed = new ImageView();
		box.getChildren().add(iconsProcessed);
		setContent(box);
		setOnCloseRequest(this);
		//start the infinite loop
		System.out.println("Starting camera "+name);
		doUpdate();
	}
	
	private void doUpdate(){
		//ThreadUtil.wait(10);
		if(open){	
				try{
					if(isSelected()){
						provider.getLatestImage(inputImage,null);                        // capture image
						data = detector.getObjects(inputImage, inputImage);
						System.out.println("Got: "+data.length);
					}else{
						System.out.println("idle: ");
					}
				}catch(IllegalArgumentException e){
					//startup noise
					//e.printStackTrace();
				}
				Platform.runLater(()->{
					try{
						iconsProcessed.setImage(AbstractImageProvider.matToJfxImage(inputImage));	// show processed image
					}catch(IllegalArgumentException e){
						//startup noise
						//e.printStackTrace();
					}
					doUpdate();
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