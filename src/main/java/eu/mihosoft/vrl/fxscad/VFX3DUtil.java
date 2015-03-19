/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package eu.mihosoft.vrl.fxscad;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * Utility class that allows to visualize meshes created with null
 * {@link MathUtil#evaluateFunction(eu.mihosoft.vrl.javaone2013.math.Function2D, int, int, float, float, float, double, double, double, double)
 * }
 * .
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class VFX3DUtil {
	//final DragContext dragContext = new DragContext();
	private VFX3DUtil() {
		throw new AssertionError("don't instanciate me!");
	}

	/**
	 * Adds rotation behavior to the specified node.
	 *
	 * @param n
	 *            node
	 * @param eventReceiver
	 *            receiver of the event
	 * @param btn
	 *            mouse button that shall be used for this behavior
	 */
	public static void addMouseBehavior(Node n, Scene eventReceiver) {
		eventReceiver.addEventHandler(MouseEvent.ANY, new MouseBehaviorImpl1(n));
		eventReceiver.addEventHandler(ScrollEvent.ANY, new MouseBehaviorImpl2(n));
		
	}

	/**
	 * Adds rotation behavior to the specified node.
	 *
	 * @param n
	 *            node
	 * @param eventReceiver
	 *            receiver of the event
	 * @param btn
	 *            mouse button that shall be used for this behavior
	 */
	public static void addMouseBehavior(Node n, Node eventReceiver) {
		eventReceiver.addEventHandler(MouseEvent.ANY, new MouseBehaviorImpl1(n));
		eventReceiver.addEventHandler(ScrollEvent.ANY, new MouseBehaviorImpl2(n));
	
	}
}

// rotation behavior implementation
class MouseBehaviorImpl2 implements EventHandler<ScrollEvent> {

	private Node n;
	double xscale;

	public MouseBehaviorImpl2(Node n) {
		n.setScaleX(1);
		n.setScaleY(1);
		n.setScaleZ(1);
		this.n = n;
		xscale = n.getScaleX();
	}

	@Override
	public void handle(ScrollEvent t) {
		if (ScrollEvent.SCROLL==(t).getEventType()) {

			double zoomFactor = (t.getDeltaY())/300.0;
			xscale+=zoomFactor;
			if(xscale<.1){
				xscale=.1;
			}
			System.out.println("Zoom "+xscale);
			n.setScaleX( xscale);
            n.setScaleY( xscale);
            n.setScaleZ( xscale);
            
		}else{
			System.out.println("No event "+t);
		}
		
		t.consume();

	}
}

// rotation behavior implementation
class MouseBehaviorImpl1 implements EventHandler<MouseEvent> {

	private double anchorAngleX;
	private double anchorAngleY;
	private double anchorX;
	private double anchorY;
	private final Rotate rotateX = new Rotate(0, 0, 0, 0, Rotate.X_AXIS);
	private final Rotate rotateZ = new Rotate(0, 0, 0, 0, Rotate.Z_AXIS);
	private Node n;
	private double mouseAnchorX;
	//private MouseButton btn;
	private double mouseAnchorY;
	private double initialTranslateX;
	private double initialTranslateY;

	public MouseBehaviorImpl1(Node n) {
		this.n = n;
		n.getTransforms().addAll(rotateX, rotateZ);
		rotateZ.setAngle(-15);
		rotateX.setAngle(-50);
		Platform.runLater(() -> {
            n.setTranslateX(-302.99);
            n.setTranslateY(-156.00);
        });
	}

	@Override
	public void handle(MouseEvent t) {

		t.consume();
		if(t.getButton() ==  MouseButton.PRIMARY){
			if (MouseEvent.MOUSE_PRESSED.equals(t.getEventType())) {
				anchorX = t.getSceneX();
				anchorY = t.getSceneY();
				anchorAngleX = rotateX.getAngle();
				anchorAngleY = rotateZ.getAngle();
				t.consume();
			} else if (MouseEvent.MOUSE_DRAGGED.equals(t.getEventType())) {
				System.out.println(" Setting from "+anchorAngleX+" "+anchorAngleY);
				double rotZ= anchorAngleY + (anchorX - t.getSceneX()) * 0.7;
				double rotX= anchorAngleX - (anchorY - t.getSceneY()) * 0.7;
				rotateZ.setAngle(rotZ);
				rotateX.setAngle(rotX);
//				System.out.println("Rotation set to X="+rotX+" Z="+rotZ);
			}
		}else if(t.getButton() ==  MouseButton.SECONDARY){
			if (MouseEvent.MOUSE_PRESSED.equals(t.getEventType())) {
		           // and node position
                mouseAnchorX = t.getX();
                mouseAnchorY = t.getY();
                initialTranslateX =n.getTranslateX();
                initialTranslateY =n.getTranslateY();
			} else if (MouseEvent.MOUSE_DRAGGED.equals(t.getEventType())) {
				// shift node from its initial position by delta
                // calculated from mouse cursor movement
				double tranX =initialTranslateX
                        +( t.getX()
                        - mouseAnchorX);
				double tranY =initialTranslateY
                        +( t.getY()
                        - mouseAnchorY);
                n.setTranslateX(tranX);
                n.setTranslateY(tranY);
//                System.out.println("Translate set to X="+tranX+" Y="+tranY);
			}
		}

	}
}
