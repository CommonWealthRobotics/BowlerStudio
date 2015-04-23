package com.neuronrobotics.bowlerstudio.tabs;

import com.neuronrobotics.bowlerstudio.tabs.*;

import java.util.Timer;
import java.util.TimerTask;

import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.features2d.KeyPoint;

import com.neuronrobotics.jniloader.AbstractImageProvider;
import com.neuronrobotics.jniloader.HaarDetector;
import com.neuronrobotics.jniloader.IObjectDetector;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class CameraTab extends AbstractBowlerStudioTab  {
	private boolean open = true;
	private AbstractImageProvider provider;
	private Mat inputImage = new Mat();
	private Mat outImage = new Mat();
	private IObjectDetector detector;
	private ImageView iconsProcessed = new ImageView();;
	KeyPoint[] data;
	private Timer timer;
	long session []=new long[4];
	
	public CameraTab(){
		detector = new HaarDetector();
	}

	public CameraTab(AbstractImageProvider pr, IObjectDetector dr) {
		this.provider = pr;
		this.detector = dr;

	}

	private void update() {

		Platform.runLater(() -> {
			//new RuntimeException().printStackTrace();
			if (open) {
				try {
					long spacing=System.currentTimeMillis()-session[3];
					long capture=session[1]-session[0];
					long process=session[2]-session[1];
					long show=session[3]-session[2];
					System.out.println("Frame Spacing "+spacing+"ms "+
							"capture "+capture+"ms "+
							"process "+process+"ms "+
							"show "+show+"ms "
							);
					
					if (isSelected()) {
						session[0] = System.currentTimeMillis();
						provider.getLatestImage(inputImage, outImage); // capture
						session[1] = System.currentTimeMillis();	   // image
						data = detector.getObjects(inputImage, outImage);
						session[2] = System.currentTimeMillis();
						if (data.length > 0)
							System.out.println("Got: " + data.length);
					} else {
						System.out.println("idle: ");
					}
					Image Img= AbstractImageProvider
							.matToJfxImage(outImage);
					Platform.runLater(() -> {
						iconsProcessed.setImage(Img); // show processed image
					});
					session[3] = System.currentTimeMillis();
					
					
				} catch (CvException |NullPointerException |IllegalArgumentException e2) {
					// startup noise
					// e.printStackTrace();
				}
				update();

			} else {
				System.out.print("\r\nFinished " + getText());
			}

		});
	}


	@Override
	public void onTabClosing() {
		System.out.print("\r\nCalling stop for " + getText());
		open = false;
	}

	@Override
	public String[] getMyNameSpaces() {
		return new String[0];
	}

	@Override
	public void initializeUI(BowlerAbstractDevice pm) {
		provider = (AbstractImageProvider)pm;
		setText(pm.getScriptingName());
		HBox box = new HBox();
		box.getChildren().add(iconsProcessed);
		setContent(box);
		// start the infinite loop
		System.out.println("Starting camera " + pm.getScriptingName());
		onTabReOpening();
	}

	@Override
	public void onTabReOpening() {
		open = true;
		update();
		for(int i=0;i<session.length;i++){
			session[i]=System.currentTimeMillis();
		}
	}
}

// new CameraTabMine(camera0,"Camera Test", new HaarDetector());