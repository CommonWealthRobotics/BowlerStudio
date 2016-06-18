package com.neuronrobotics.bowlerstudio.threed;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.util.ThreadUtil;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;
import jankovicsandras.imagetracer.ImageTracer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;

public class SVGFactory  extends Application {

	private static Pane snapshotGroup;


	@SuppressWarnings("static-access")
	public static File exportSVG(CSG currentCsg,File defaultDir) {
		currentCsg =currentCsg
					.toYMin()
					.toXMin();
					
		CSG slice = currentCsg.movez(-.1)
				.intersect(new Cube(currentCsg.getMaxX() - currentCsg.getMinX(),
						currentCsg.getMaxY() - currentCsg.getMinY(), 10).toCSG()
								.toXMin()
								.toYMin())
				.setColor(Color.BLACK);
		System.out.println("Object bounds  y=" + (currentCsg.getMaxY() - currentCsg.getMinY()));
		System.out.println("Object bounds  x=" + (currentCsg.getMaxX() - currentCsg.getMinX()));
		MeshView sliceMesh = slice.getMesh();
		//sliceMesh.getTransforms().add(Transform.translate(centerX, centerY));
		AnchorPane anchor = new AnchorPane(sliceMesh);
		AnchorPane.setBottomAnchor(sliceMesh, (double) 0);
		AnchorPane.setTopAnchor(sliceMesh, (double) 0);
		AnchorPane.setLeftAnchor(sliceMesh, (double) 0);
		AnchorPane.setRightAnchor(sliceMesh, (double) 0);
				
		snapshotGroup = new Pane(anchor);
		snapshotGroup.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
		
		File baseDirForFiles = FileSelectionFactory.GetFile(defaultDir, true);

		if (!baseDirForFiles.getAbsolutePath().toLowerCase().endsWith(".svg"))
			baseDirForFiles = new File(baseDirForFiles.getAbsolutePath() + ".svg");
		String imageName = baseDirForFiles.getAbsolutePath() + ".png";

		int snWidth = (int) 1024;
		int snHeight = (int) 1024;

		double realWidth = snapshotGroup.getBoundsInLocal().getWidth();
		double realHeight = snapshotGroup.getBoundsInLocal().getHeight();

		double scaleX = snWidth / realWidth;
		double scaleY = snHeight / realHeight;

		double scale = Math.min(scaleX, scaleY);

		SnapshotParameters snapshotParameters = new SnapshotParameters();
		snapshotParameters.setTransform(new Scale(scale, scale));
		snapshotParameters.setDepthBuffer(true);
		snapshotParameters.setFill(Color.TRANSPARENT);

		WritableImage snapshot = new WritableImage(snWidth, (int) (realHeight * scale));
		File finalDir = baseDirForFiles;
		Platform.runLater(() -> {
			snapshotGroup.snapshot(snapshotParameters, snapshot);
			new Thread(() -> {
				launch(new String[]{});
				
				
			}).start();
			new Thread(() -> {
				try {
					ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", new File(imageName));
				} catch (IOException ex) {
					ex.printStackTrace();
					Log.error(ex.getMessage());
				}
				try {
					// Options
					HashMap<String,Float> options = new HashMap<String,Float>();

					// Tracing
					options.put("ltres",1f);//Error treshold for straight lines.
					options.put("qtres",1f);//Error treshold for quadratic splines.
					options.put("pathomit",8f);//Edge node paths shorter than this will be discarded for noise reduction.

					// Color quantization
					options.put("colorsampling",1f); // 1f means true ; 0f means false: starting with generated palette
					options.put("numberofcolors",16f);//Number of colors to use on palette if pal object is not defined.
					options.put("mincolorratio",0.02f);//Color quantization will randomize a color if fewer pixels than (total pixels*mincolorratio) has it.
					options.put("colorquantcycles",3f);//Color quantization will be repeated this many times.
//
					// SVG rendering
					options.put("scale",1f);//Every coordinate will be multiplied with this, to scale the SVG.
					options.put("simplifytolerance",0f);//
					options.put("roundcoords",1f); // 1f means rounded to 1 decimal places, like 7.3 ; 3f means rounded to 3 places, like 7.356 ; etc.
					options.put("lcpr",0f);//Straight line control point radius, if this is greater than zero, small circles will be drawn in the SVG. Do not use this for big/complex images.
					options.put("qcpr",0f);//Quadratic spline control point radius, if this is greater than zero, small circles and lines will be drawn in the SVG. Do not use this for big/complex images.
					options.put("desc",1f); // 1f means true ; 0f means false: SVG descriptions deactivated
					options.put("viewbox",0f); // 1f means true ; 0f means false: fixed width and height

					// Selective Gauss Blur
					options.put("blurradius",0f); // 0f means deactivated; 1f .. 5f : blur with this radius
					options.put("blurdelta",20f); // smaller than this RGB difference will be blurred

					// Palette
					// This is an example of a grayscale palette
					// please note that signed byte values [ -128 .. 127 ] will be converted to [ 0 .. 255 ] in the getsvgstring function
					byte[][] palette = new byte[8][4];
					for(int colorcnt=0; colorcnt < 8; colorcnt++){
					    palette[colorcnt][0] = (byte)( -128 + colorcnt * 32); // R
					    palette[colorcnt][1] = (byte)( -128 + colorcnt * 32); // G
					    palette[colorcnt][2] = (byte)( -128 + colorcnt * 32); // B
					    palette[colorcnt][3] = (byte)255;             // A
					}

					ImageTracer.saveString(finalDir.getAbsolutePath(), ImageTracer.imageToSVG(imageName, options,palette));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
				}
				
			}).start();
		});
		
	
		return baseDirForFiles.getParentFile();
	}
	@Override
	public void start(Stage stage) throws Exception {
		Scene scene = new Scene(snapshotGroup); 
		
		System.out.println("Launching viewer");
        stage.setTitle("Viewed Image"); 
        stage.setScene(scene); 
        stage.sizeToScene(); 
        stage.show();
        stage.setOnCloseRequest(event -> {
        	System.exit(0);
		});
	}
	
	
	public static void main(String [] args){
		new JFXPanel();
		CSG main= new Cube(	400,// X dimention
				400,// Y dimention
				400//  Z dimention
				).toCSG();// this converts from the geometry to an object we can work with

		CSG cut= new Cube(	200,// X dimention
				200,// Y dimention
				200//  Z dimention
				).toCSG();// this converts from the geometry to an object we can work with	
				
	
		exportSVG( main.difference(cut).intersect(	new Cube(	400,400,2).toCSG()	),new File("export.svg"));

		//
	}
}
