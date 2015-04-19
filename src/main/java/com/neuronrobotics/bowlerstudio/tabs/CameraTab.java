package com.neuronrobotics.bowlerstudio.tabs;

import com.neuronrobotics.bowlerstudio.tabs.*;

import java.util.Timer;
import java.util.TimerTask;

import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.features2d.KeyPoint;

import com.neuronrobotics.jniloader.AbstractImageProvider;
import com.neuronrobotics.jniloader.IObjectDetector;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class CameraTab extends Tab implements EventHandler<Event> {
	private boolean open = true;
	private AbstractImageProvider provider;
	private Mat inputImage = new Mat();
	private Mat outImage = new Mat();
	private IObjectDetector detector;
	private ImageView iconsProcessed = new ImageView();;
	KeyPoint[] data;
	private Timer timer;

	public CameraTab(AbstractImageProvider pr, String name, IObjectDetector dr) {
		this.provider = pr;
		this.detector = dr;
		setText(name);
		HBox box = new HBox();
		box.getChildren().add(iconsProcessed);
		setContent(box);
		setOnCloseRequest(this);
		// start the infinite loop
		System.out.println("Starting camera " + name);
		update();

	}

	private void update() {

		Platform.runLater(() -> {
			if (open) {
				try {
					if (isSelected()) {

						provider.getLatestImage(inputImage, outImage); // capture
																		// image
						data = detector.getObjects(inputImage, outImage);

						if (data.length > 0)
							System.out.println("Got: " + data.length);
					} else {
						System.out.println("idle: ");
					}
				} catch (CvException | IllegalArgumentException e1) {
					// startup noise
					// e.printStackTrace();
				}

			} else {
				System.out.print("\r\nFinished " + getText());
			}
			try {

				iconsProcessed.setImage(AbstractImageProvider
						.matToJfxImage(outImage)); // show processed image

			} catch (IllegalArgumentException e2) {
				// startup noise
				// e.printStackTrace();
			}
			update();
		});
	}

	@Override
	public void handle(Event event) {
		System.out.print("\r\nCalling stop for " + getText());
		open = false;
	}
}

// new CameraTabMine(camera0,"Camera Test", new HaarDetector());