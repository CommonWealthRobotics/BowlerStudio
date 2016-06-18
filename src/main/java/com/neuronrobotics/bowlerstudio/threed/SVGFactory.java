package com.neuronrobotics.bowlerstudio.threed;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.sdk.common.Log;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;
import jankovicsandras.imagetracer.ImageTracer;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Scale;

public class SVGFactory {

	public static File exportSVG(CSG currentCsg,File defaultDir) {
		CSG slice = currentCsg.movez(-.1)
				.intersect(new Cube(currentCsg.getMaxX() - currentCsg.getMinX(),
						currentCsg.getMaxY() - currentCsg.getMinY(), 10).toCSG()
								.movex(currentCsg.getMaxX() / 2 - currentCsg.getMinX() / 2)
								.movey(currentCsg.getMaxY() / 2 - currentCsg.getMinY() / 2))
				.setColor(Color.BLACK);
		System.out.println("Object bounds  y=" + (currentCsg.getMaxY() - currentCsg.getMinY()));
		System.out.println("Object bounds  x=" + (currentCsg.getMaxX() - currentCsg.getMinX()));
		MeshView sliceMesh = slice.getMesh();
		Group snapshotGroup = new Group(sliceMesh);
		File baseDirForFiles = FileSelectionFactory.GetFile(defaultDir, true);

		if (!baseDirForFiles.getAbsolutePath().toLowerCase().endsWith(".svg"))
			baseDirForFiles = new File(baseDirForFiles.getAbsolutePath() + ".svg");
		String imageName = baseDirForFiles.getAbsolutePath() + ".png";

		int snWidth = (int) snapshotGroup.getBoundsInLocal().getWidth();
		int snHeight = (int) snapshotGroup.getBoundsInLocal().getHeight();

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
				try {
					ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", new File(imageName));
				} catch (IOException ex) {
					ex.printStackTrace();
					Log.error(ex.getMessage());
				}
				try {
					ImageTracer.saveString(finalDir.getAbsolutePath(), ImageTracer.imageToSVG(imageName, null, null));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}).start();
		});
		return baseDirForFiles.getParentFile();
	}
	
	public static void main(String [] args){
		CSG main= new Cube(	40,// X dimention
				40,// Y dimention
				40//  Z dimention
				).toCSG();// this converts from the geometry to an object we can work with

		CSG cut= new Cube(	20,// X dimention
				20,// Y dimention
				20//  Z dimention
				).toCSG();// this converts from the geometry to an object we can work with	
				
	
		exportSVG( main.difference(cut).intersect(	new Cube(	40,40,2).toCSG()	),new File("export.svg"));
	}
}
