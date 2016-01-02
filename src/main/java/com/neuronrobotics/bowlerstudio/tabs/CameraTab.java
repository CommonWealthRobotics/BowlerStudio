package com.neuronrobotics.bowlerstudio.tabs;

import haar.HaarFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Timer;

import org.opencv.core.CvException;

import com.neuronrobotics.imageprovider.AbstractImageProvider;
import com.neuronrobotics.imageprovider.Detection;
import com.neuronrobotics.imageprovider.HaarDetector;
import com.neuronrobotics.imageprovider.IObjectDetector;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.Log;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;



public class CameraTab extends AbstractBowlerStudioTab  {
	private boolean open = true;
	private AbstractImageProvider provider;
	
	private IObjectDetector detector;
	private ImageView iconsProcessed = new ImageView();;
	private List<Detection> data;
	private Timer timer;
	private long session []=new long[4];
	private BufferedImage inputImage = AbstractImageProvider.newBufferImage(640,480);
	private BufferedImage outImage = AbstractImageProvider.newBufferImage(640,480);
	//set this variable to make this tab auto open when a device is connected

	public CameraTab(){}//default construtor
	public CameraTab(AbstractImageProvider pr, IObjectDetector dr) {
		this.provider = pr;
		this.setDetector(dr);
		initializeUI(pr);
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
		VBox box = new VBox();
		box.getChildren().add(iconsProcessed);
		ObservableList<String> options;
		try {
			List<String> l = HaarFactory.getAvailibHaar();
			System.err.println(l);
			options = FXCollections.observableArrayList(l);
			@SuppressWarnings("unchecked")
			ComboBox<String> comboBox = new ComboBox<String>(options);
			comboBox.setOnAction((event) -> {
				String item = comboBox.getSelectionModel().getSelectedItem();
			   Log.warning("Setting detector to "+item);
			   setDetector(new HaarDetector(item));
			});
			box.getChildren().add(comboBox);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setContent(box);
		// start the infinite loop
		System.out.println("Starting camera " + pm.getScriptingName());
		onTabReOpening();
	}

	@Override
	public void onTabReOpening() {
		open = true;
		for(int i=0;i<session.length;i++){
			session[i]=System.currentTimeMillis();
		}
		new Thread(){
			private boolean done;

			public void run(){
				try{
					done=true;// startup passes the frame update check
					while (open && provider.isAvailable()) {
	
						try {
							long spacing=System.currentTimeMillis()-session[3];
							double total = System.currentTimeMillis() - session[0];
							long capture=session[1]-session[0];
							long process=session[2]-session[1];
							long show=session[3]-session[2];
	
							
							if (isSelected()) {
								System.out.println("Total "+(int)(1/(total/1000.0))+"FPS "+
										"capture="+capture+"ms "+
										"process="+process+"ms "+
										"convert="+show+"ms "+
										"spacing="+spacing+"ms "
										);
								session[0] = System.currentTimeMillis();
								provider.getLatestImage(inputImage, outImage); // capture
								session[1] = System.currentTimeMillis();	   // image
								data = getDetector().getObjects(inputImage, outImage);
								session[2] = System.currentTimeMillis();
//								if (data.size() > 0)
//									System.out.println("Got: " + data.size());
							} else {
								//System.out.println("idle: ");
							}
							Image Img= AbstractImageProvider
									.getJfxImage(outImage);
							session[3] = System.currentTimeMillis();
							//make sure capture never gets ahead of showing
							while(!done){
								try {
									Thread.sleep(1);
								} catch (InterruptedException e) {
									done=true;//break the loop
								}
							}
							
							done = false;
							Platform.runLater(() -> {
								
								iconsProcessed.setImage(Img); // show processed image
								done=true;
							});
							
						
						} catch (CvException |NullPointerException |IllegalArgumentException e2) {
							// startup noise
							// e.printStackTrace();
							for(int i=0;i<session.length;i++){
								session[i]=System.currentTimeMillis();
							}
						}
	
					}
					System.out.print("\r\nFinished " + getText());
				}catch(Exception e){
					e.printStackTrace(System.out);
					System.out.println("Camera error, close and open the aplication (blame OpenCV)");
					throw e;
				}
			}
		}.start();
	}

	public IObjectDetector getDetector() {
		if(detector==null)
			setDetector(new HaarDetector());
		return detector;
	}

	public void setDetector(IObjectDetector detector) {
		this.detector = detector;
	}

}

// new CameraTabMine(camera0,"Camera Test", new HaarDetector());