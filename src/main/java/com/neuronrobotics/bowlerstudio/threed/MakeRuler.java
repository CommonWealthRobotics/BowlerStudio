package com.neuronrobotics.bowlerstudio.threed;

import java.util.HashMap;

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
	private static HashMap<Integer,CSG> numbers = new HashMap<>();


	public static Group createRuler(boolean flipNumber) {
		int numCentimeters = 30;
		double width = 0.15;
		double height = 10;
		Group ruler = new Group();

		// Create base mesh for the ruler line
		TriangleMesh baseMesh = createRectangleMesh(width, 1.0f);
		MeshView baseView = new MeshView(baseMesh);
		PhongMaterial phongMaterial = new PhongMaterial(Color.BLACK);

		baseView.setMaterial(phongMaterial);

		// Position the base line in the middle
		Affine baseTransform = new Affine();
		baseTransform.setTy(height / 2 - 1.0);
		baseView.getTransforms().add(baseTransform);
		ruler.getChildren().add(baseView);

		// Constants for spacing
		double mmSpacing = width / (numCentimeters * 10.0);
		double tickWidth=0.25;
		// Draw tick marks and labels
		for (int i = 0; i <= numCentimeters * 10; i++) {
			double x = i * mmSpacing;
			TriangleMesh tickMesh;

			// Determine tick type based on position
			if (i % 10 == 0) {
				// Centimeter marks
				tickMesh = createRectangleMesh(tickWidth, height / 1.5);
				if (i % 20 == 0) {
					// Add centimeter number using SVGPath
					int number = i / 10;
					if(numbers.get(number)==null) {
						numbers.put(number,CSG.textToSize(""+i, 4, 6, 0.1).movey(height).moveToCenterX().setColor(Color.BLACK));
					}
					CSG movey = numbers.get(number);
					if(flipNumber) {
						movey=movey.roty(180);
					}
					MeshView numberGroup = movey.newMesh();
	
					// Scale and position the number
					Affine numberTransform = new Affine();
					numberTransform.appendTranslation(i, 0);
					numberGroup.getTransforms().add(numberTransform);
					ruler.getChildren().add(numberGroup);
				}
			} else if (i % 5 == 0) {
				// 5mm marks
				tickMesh = createRectangleMesh(tickWidth, height / 2.5);
			} else {
				// 1mm marks
				tickMesh = createRectangleMesh(tickWidth, height / 4);
			}

			// Create and position tick mark
			MeshView tickView = new MeshView(tickMesh);
			tickView.setMaterial(phongMaterial);

			// Use Affine transform for tick positioning
			System.out.println("Tick for " + i);
			Affine tickTransform = new Affine();
			tickTransform.setTx(i-tickWidth/2);
			// tickTransform.setTy((height - tickView.getBoundsInLocal().getHeight()) / 2);
			tickView.getTransforms().add(tickTransform);
			tickView.setCullFace(CullFace.NONE);
			ruler.getChildren().add(tickView);
		}

		return ruler;
	}

	private static TriangleMesh createRectangleMesh(double width, double height) {
		float[] points = { 0, 0, 0, // point 0
				(float) width, 0, 0, // point 1
				(float) width, (float) height, 0, // point 2
				0, (float) height, 0 // point 3
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
