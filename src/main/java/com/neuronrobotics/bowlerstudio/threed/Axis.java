package com.neuronrobotics.bowlerstudio.threed;

import java.util.Arrays;

import com.neuronrobotics.bowlerstudio.BowlerStudio;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;
import eu.mihosoft.vrl.v3d.TextExtrude;
import javafx.application.Platform;
import javafx.scene.text.Font;
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
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;

// TODO: Auto-generated Javadoc
/**
 * The Class Axis.
 */
public class Axis extends Group {

	private CSG xAxis;
	private CSG yAxis;
	private CSG zAxis;
	private CSG xText;
	private CSG yText;
	private CSG zText;

	/**
	 * Instantiates a new axis.
	 */
	public Axis(boolean isVis) {
		this(50,isVis);
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
	public Axis(int i,boolean isVis) {
		double strokWidth = 0.5;

		Affine xp = new Affine();
		xp.setTx(i / 2);
		Font font = new Font("Arial",  5);

		

		xText = CSG.unionAll(TextExtrude.text((double)strokWidth,"X",font)).movex(i).moveToCenterY();
		//xText.getTransforms().add(xp);

		Affine yp = new Affine();
		yp.setTy(i / 2);
		yText = CSG.unionAll(TextExtrude.text((double)strokWidth,"Y",font)).rotz(90).toYMin().movey(i).moveToCenterX();
		//yText.getTransforms().add(yp);

		// zp.setTz(i/2);
		Affine zTextAffine = new Affine();
		zTextAffine.setTz(i / 2);
		zTextAffine.setTx(i / 2);
		zTextAffine.appendRotation(-90, 0, 0, 0, 1, 0, 0);
		zTextAffine.appendRotation(180, 0, 0, 0, 0, 0, 1);
		zText = CSG.unionAll(TextExtrude.text((double)strokWidth,"Z",font)).rotx(90).rotz(90).movez(i).moveToCenterY();
		//zText.getTransforms().add(zTextAffine);
		// zText.smoothProperty().set(false);
		xAxis = new Cube(i, strokWidth, strokWidth).toCSG().toXMin();
		yAxis = new Cube( strokWidth,i, strokWidth).toCSG().toYMin();
		zAxis = new Cube( strokWidth, strokWidth,i).toCSG().toZMin();
		xText.setColor(Color.RED);

		yText.setColor(Color.GREEN);

		zText.setColor(Color.BLUE);
		xAxis.setColor(Color.RED);

		yAxis.setColor(Color.GREEN);

		zAxis.setColor(Color.BLUE);
		if(isVis)
			show();
		else
			hide();
	}

	public void show() {
		BowlerStudio.runLater(() -> showAll());
	}

	private void showAll() {
		try {
			for (Node n : Arrays.asList(xAxis.getMesh(), yAxis.getMesh(), zAxis.getMesh(), xText.getMesh(), yText.getMesh(), zText.getMesh())) {
				try {
					n.setPickOnBounds(false);
					n.setMouseTransparent(true);
					getChildren().add(n);
				} catch (Exception e) {
				}
			}
		}catch(Exception ex) {
			// no exception on exit
		}
	}

	public void hide() {
		BowlerStudio.runLater(() -> getChildren().clear());
	}

} // end of class Axis
