package com.neuronrobotics.bowlerstudio.threed;

import javafx.application.Platform;

/*
 *      Axis.java 1.0 98/11/25
 *
 * Copyright (c) 1998 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

/*
 * Getting Started with the Java 3D API
 * written in Java 3D
 *
 * This program demonstrates:
 *   1. writing a visual object class
 *      In this program, Axis class defines a visual object
 *      This particular class extends Shape3D
 *      See the text for a discussion.
 *   2. Using LineArray to draw 3D lines.
 */

import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Affine;

// TODO: Auto-generated Javadoc
/**
 * The Class Axis.
 */
public class Axis extends Group {
	
	private Box xAxis;
	private Box yAxis;
	private Box zAxis;
	private Label xText;
	private Label yText;
	private Label zText;
	/**
	 * Instantiates a new axis.
	 */
	public Axis() {
		this(50);
	}
	// //////////////////////////////////////////
	//
	// create axis visual object
	/**
	 * Instantiates a new axis.
	 *
	 * @param i the i
	 */
	//
	public Axis(int i) {
		final PhongMaterial redMaterial = new PhongMaterial();
		redMaterial.setDiffuseColor(Color.DARKRED);
		redMaterial.setSpecularColor(Color.RED);

		final PhongMaterial greenMaterial = new PhongMaterial();
		greenMaterial.setDiffuseColor(Color.DARKGREEN);
		greenMaterial.setSpecularColor(Color.GREEN);

		final PhongMaterial blueMaterial = new PhongMaterial();
		blueMaterial.setDiffuseColor(Color.DARKBLUE);
		blueMaterial.setSpecularColor(Color.BLUE);

		xAxis = new Box(i, 2, 2);
		yAxis = new Box(2, i, 2);
		zAxis = new Box(2, 2, i);
		
		
		Affine xp = new Affine();
		xp.setTx(i/2);
		xAxis.getTransforms().add(xp);
		xText = new Label("+X");
		xText.getTransforms().add(xp);
		
		Affine yp = new Affine();
		yp.setTy(i/2);
		yAxis.getTransforms().add(yp);
		yText = new Label("+Y");
		yText.getTransforms().add(yp);
		
		Affine zp = new Affine();
		zp.setTz(i/2);
		Affine zTextAffine = new Affine();
		zTextAffine.setTz(i/2);
		zTextAffine.setTx(i/2);
		zTextAffine.appendRotation(-90, 0, 0, 0, 1, 0, 0);
		zTextAffine.appendRotation(180, 0, 0, 0, 0, 0, 1);
		zAxis.getTransforms().add(zp);
		zText = new Label("+Z");
		zText.getTransforms().add(zTextAffine);
		
		
		xAxis.setMaterial(redMaterial);
		yAxis.setMaterial(greenMaterial);
		zAxis.setMaterial(blueMaterial);
		show();
	}
	
	public void show(){
		Platform.runLater(()->getChildren().addAll(xAxis,yAxis,zAxis,xText,yText,zText));
	}
	public void hide(){
		Platform.runLater(()->getChildren().removeAll(xAxis,yAxis,zAxis,xText,yText,zText));
	}
	

} // end of class Axis
