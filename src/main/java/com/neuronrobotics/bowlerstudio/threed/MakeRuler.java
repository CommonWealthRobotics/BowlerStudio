package com.neuronrobotics.bowlerstudio.threed;

import java.util.HashMap;

import com.neuronrobotics.bowlerstudio.BowlerKernel;

import eu.mihosoft.vrl.v3d.CSG;
import javafx.scene.Group;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.shape.SVGPath;

public class MakeRuler {
	// SVG paths for numbers 0-9
	private static HashMap<Integer, CSG> numbers = new HashMap<>();

	public static Group createRuler(boolean flipNumber) {
		return createRuler(flipNumber, 30); // 30 cm default length
	}

	public static Group createRuler(boolean flipNumber, int rulerLengthCM) {
		double baseWidth = 0.15; // The width of the ruler base in mm
		double tickLength = 8; // The length of a tick in mm
		double tickWidth = 0.25; // The width of a tick in mm
		Group ruler = new Group();

		new Thread(() -> {
			// Create base mesh for the ruler line
			TriangleMesh baseMesh = createRectangleMesh(rulerLengthCM * 10 + tickWidth, baseWidth);
			MeshView baseView = new MeshView(baseMesh);
			baseView.setMouseTransparent(true);
			baseView.setCullFace(CullFace.NONE);
			PhongMaterial phongMaterial = new PhongMaterial(Color.BLACK);

			baseView.setMaterial(phongMaterial);

			// Position the base line in the middle
			Affine baseTransform = new Affine();
			baseTransform.setTx(-tickWidth / 2.0);
			baseTransform.setTy(-baseWidth / 2.0);
			baseView.getTransforms().add(baseTransform);
			BowlerKernel.runLater(() -> ruler.getChildren().add(baseView));

			// Draw tick marks and labels
			for (int i = 0; i <= rulerLengthCM * 10; i++) {
				TriangleMesh tickMesh;

				// Determine tick type based on position
				if (i % 10 == 0) {
					// Centimeter tick marks
					tickMesh = createRectangleMesh(tickWidth, tickLength);
					// Always draw a number at the end of the ruler
					if ((i % 20 == 0) || (i == (int) (rulerLengthCM * 10))) {
						// Add centimeter number using SVGPath
						int number = i / 10;
						// Prevent double "0" at origin "(flipNumber || (i != 0))"
						if ((numbers.get(number) == null) && (flipNumber || (i != 0))) {
							numbers.put(number, CSG.textToSize("" + i, 4, 6, 0.1).movey(tickLength + 0.5)
									.moveToCenterX().setColor(Color.BLACK));
						}
						CSG movey = flipNumber ? numbers.get(number).roty(180) : numbers.get(number);
						int index = i;
						if (movey != null)
							BowlerKernel.runLater(() -> {
								MeshView numberGroup = movey.newMesh();
								numberGroup.setMouseTransparent(true);
								// Scale and position the number
								Affine numberTransform = new Affine();
								numberTransform.appendTranslation(index, 0);
								numberGroup.getTransforms().add(numberTransform);
								ruler.getChildren().add(numberGroup);
							});
					}
				} else if (i % 5 == 0) {
					// 5mm tick marks
					tickMesh = createRectangleMesh(tickWidth, tickLength / 2);
				} else {
					// 1mm tick marks
					tickMesh = createRectangleMesh(tickWidth, tickLength / 4);
				}

				// Create and position tick mark
				MeshView tickView = new MeshView(tickMesh);
				tickView.setMouseTransparent(true);
				tickView.setMaterial(phongMaterial);

				// Use Affine transform for tick positioning
				// com.neuronrobotics.sdk.common.Log.error("Tick for " + i);
				Affine tickTransform = new Affine();
				tickTransform.setTx(i - tickWidth / 2);
				tickView.getTransforms().add(tickTransform);
				tickView.setCullFace(CullFace.NONE);
				BowlerKernel.runLater(() -> ruler.getChildren().add(tickView));
			}
		}).start();
		return ruler;
	}

	private static TriangleMesh createRectangleMesh(double width, double tickLength) {
		float[] points = { 0, 0, 0, // point 0
				(float) width, 0, 0, // point 1
				(float) width, (float) tickLength, 0, // point 2
				0, (float) tickLength, 0 // point 3
		};

		float[] texCoords = { 0, 0, 1, 0, 1, 1, 0, 1 };

		int[] faces = { 0, 0, 1, 1, 2, 2, // First triangle
				0, 0, 2, 2, 3, 3 // Second triangle
		};

		TriangleMesh mesh = new TriangleMesh();
		mesh.getPoints().addAll(points);
		mesh.getTexCoords().addAll(texCoords);
		mesh.getFaces().addAll(faces);

		return mesh;
	}
}
