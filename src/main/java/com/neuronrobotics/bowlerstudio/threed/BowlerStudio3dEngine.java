package com.neuronrobotics.bowlerstudio.threed;

/*
 * Copyright (c) 2011, 2013 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.VirtualCameraMobileBase;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.imageprovider.AbstractImageProvider;
import com.neuronrobotics.imageprovider.IVirtualCameraFactory;
import com.neuronrobotics.imageprovider.VirtualCameraFactory;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.ITaskSpaceUpdateListenerNR;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.IConnectionEventListener;
import com.neuronrobotics.sdk.common.IDeviceConnectionEventListener;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.dypid.DyPIDConfiguration;
import com.neuronrobotics.sdk.dyio.peripherals.DigitalInputChannel;
import com.neuronrobotics.sdk.dyio.peripherals.IDigitalInputListener;
import com.neuronrobotics.sdk.pid.PIDConfiguration;
import com.sun.javafx.geom.transform.Affine3D;
import com.sun.javafx.geom.transform.BaseTransform;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cylinder;
import javafx.application.Application;
import javafx.application.Platform;
import static javafx.application.Application.launch;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import javafx.scene.PerspectiveCamera;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;

import static javafx.scene.input.KeyCode.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import javafx.scene.Node;

// TODO: Auto-generated Javadoc
/**
 * MoleculeSampleApp.
 */
public class BowlerStudio3dEngine extends JFXPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6744581340628622682L;

	/** The root. */
	private final Group root = new Group();
	
	/** The axis group. */
	final Group axisGroup = new Group();
	
	/** The world. */
	final Xform world = new Xform();
	
	/** The camera. */
	final PerspectiveCamera camera = new PerspectiveCamera(true);

	
	/** The camera distance. */
	final double cameraDistance = 3000;
	
	/** The molecule group. */
	final Xform moleculeGroup = new Xform();
	
	/** The one frame. */
	double ONE_FRAME = 1.0 / 24.0;
	
	/** The delta multiplier. */
	double DELTA_MULTIPLIER = 200.0;
	
	/** The control multiplier. */
	double CONTROL_MULTIPLIER = 0.1;
	
	/** The shift multiplier. */
	double SHIFT_MULTIPLIER = 0.1;
	
	/** The alt multiplier. */
	double ALT_MULTIPLIER = 0.5;
	
	/** The mouse pos x. */
	double mousePosX;
	
	/** The mouse pos y. */
	double mousePosY;
	
	/** The mouse old x. */
	double mouseOldX;
	
	/** The mouse old y. */
	double mouseOldY;
	
	/** The mouse delta x. */
	double mouseDeltaX;
	
	/** The mouse delta y. */
	double mouseDeltaY;

	/** The manipulator. */
	private final Group manipulator = new Group();
	
	/** The look group. */
	private final Group lookGroup = new Group();
	
	/** The scene. */
	private SubScene scene;
	
	
	/** The ground. */
	private Group ground;
	
	private boolean captureMouse = false;

	private VirtualCameraDevice virtualcam;

	private VirtualCameraMobileBase flyingCamera;
	private Group hand;
	private double upDown=0;
	private double leftRight=0;
	private HashMap<CSG,MeshView> csgMap = new HashMap<>();
	private String lastFileSelected="";
	private int lastFileLine=0;

	private TransformNR defautcameraView;
	private static final TransformNR offsetForVisualization = new TransformNR(0,0,0,
			new RotationNR(0, 90, 90));

	private Button back;

	private Button fwd;

	private Button home;
	private int debuggerIndex=0;
	private ArrayList<String> debuggerList=new ArrayList<>();
	private CSG selectedCsg=null;
	/**
	 * Instantiates a new jfx3d manager.
	 */
	public BowlerStudio3dEngine() {
		setSubScene(new SubScene(getRoot(), 1024, 1024, true, null));
		buildScene();
		buildCamera();
		buildAxes();

		Stop[] stops = null;
		getSubScene().setFill(new LinearGradient(125, 0, 225, 0, false, CycleMethod.NO_CYCLE, stops));
		Scene s = new Scene(new Group(getSubScene()));
		handleKeyboard(s);
		handleMouse(getSubScene());

		setScene(s);
		
	}
	
	private void highlightDebugIndex(int index, java.awt.Color c){
		String trace = debuggerList.get(index);
		BowlerStudioController
		.getBowlerStudio()
		.setHighlight(
				ScriptingEngine
					.getFileEngineRunByName(getFilenameFromTrace(trace)),
					getLineNumbereFromTrace(trace),
				c
						);
	}
	
	private String getFilenameFromTrace(String trace){
		String[] parts =trace.split(":");
		return parts[0];
	}
	private int getLineNumbereFromTrace(String trace){
		String[] parts =trace.split(":");
		return Integer.parseInt(parts[1]);
	}
	public Group getControlsBox(){
		HBox controls = new HBox(10);
		
		back = new Button("Back");
		fwd = new Button("Forward");
		
		fwd.setOnAction(event ->{
			BowlerStudioController.getBowlerStudio().clearHighlits();
			highlightDebugIndex(debuggerIndex, java.awt.Color.PINK);
			debuggerIndex--;
			if(debuggerIndex==0){
				fwd.disableProperty().set(true);
			}
			back.disableProperty().set(false);
			highlightDebugIndex(debuggerIndex, java.awt.Color.GREEN);
		});
		
		back.setOnAction(event ->{
			BowlerStudioController.getBowlerStudio().clearHighlits();
			highlightDebugIndex(debuggerIndex, java.awt.Color.PINK);
			debuggerIndex++;
			if(debuggerIndex>=debuggerList.size()){
				back.disableProperty().set(true);
				debuggerIndex--;
			}
			if(debuggerIndex>0)
				fwd.disableProperty().set(false);
			highlightDebugIndex(debuggerIndex, java.awt.Color.GREEN);
		});
		
		
		fwd.disableProperty().set(true);
		back.disableProperty().set(true);
		
		home = new Button("Home Camera");
		
		home.setOnAction(event ->{
			getFlyingCamera().setGlobalToFiducialTransform(defautcameraView);
			getFlyingCamera().updatePositions();
		});
		
		controls.getChildren().addAll(back,fwd,home);
		return new Group(controls);
	}
	
	/**
	 * Removes the objects.
	 */
	public void removeObjects(){
		lookGroup.getChildren().clear();
		csgMap.clear();
	}

	/**
	 * Removes the object.
	 *
	 * @param previous the previous
	 */
	public void removeObject(CSG previousCsg) {
		
		MeshView previous  = csgMap.get(previousCsg);
		if (previous != null) {
			lookGroup.getChildren().remove(previous);
		}
		csgMap.remove(previousCsg);

	}

	/**
	 * Adds the object.
	 *
	 * @param current the current
	 * @return the mesh view
	 */
	public MeshView addObject(CSG currentCsg) {
		
		csgMap.put(currentCsg, currentCsg.getMesh());
		
		MeshView current = csgMap.get(currentCsg);

		current.setOnMouseClicked(event -> {
			if(selectedCsg == currentCsg)
				return;
			selectedCsg= currentCsg;
			new Thread(){
				public void run(){
			        selectObjectsSourceFile(currentCsg);
				}
			}.start();
		});
		
		Group og = new Group();
		og.getChildren().add(current);
		Axis a = new Axis();
		a.getTransforms().addAll(current.getTransforms());
		og.getChildren().add(a);
		lookGroup.getChildren().add(og);
		//Log.warning("Adding new axis");
		return current;
	}

	/**
	 * Save to png.
	 *
	 * @param f the f
	 */
	public void saveToPng(File f) {
		String fName = f.getAbsolutePath();

		if (!fName.toLowerCase().endsWith(".png")) {
			fName += ".png";
		}

		int snWidth = 1024;
		int snHeight = 1024;

		double realWidth = getRoot().getBoundsInLocal().getWidth();
		double realHeight = getRoot().getBoundsInLocal().getHeight();

		double scaleX = snWidth / realWidth;
		double scaleY = snHeight / realHeight;

		double scale = Math.min(scaleX, scaleY);

		PerspectiveCamera snCam = new PerspectiveCamera(false);
		snCam.setTranslateZ(-200);

		SnapshotParameters snapshotParameters = new SnapshotParameters();
		snapshotParameters.setTransform(new Scale(scale, scale));
		snapshotParameters.setCamera(snCam);
		snapshotParameters.setDepthBuffer(true);
		snapshotParameters.setFill(Color.TRANSPARENT);

		WritableImage snapshot = new WritableImage(snWidth,
				(int) (realHeight * scale));

		getRoot().snapshot(snapshotParameters, snapshot);

		try {
			ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png",
					new File(fName));
		} catch (IOException ex) {
			ex.printStackTrace();
			Log.error(ex.getMessage());
		}
	}


	/**
	 * Builds the scene.
	 */
	private void buildScene() {
		world.rx.setAngle(-90);// point z upwards
		world.ry.setAngle(180);// arm out towards user
		getRoot().getChildren().add(world);
	}

	/**
	 * Builds the camera.
	 */
	private void buildCamera() {

		CSG cylinder = new Cylinder(	0, // Radius at the top
  				5, // Radius at the bottom
  				20, // Height
  			         (int)20 //resolution
  			         ).toCSG()
				.roty(90)
				.setColor(Color.BLACK);
	
		
		hand = new Group(cylinder.getMesh());

		camera.setNearClip(.1);
		camera.setFarClip(100000.0);
		getSubScene().setCamera(camera);
		camera.setRotationAxis(Rotate.Z_AXIS);
		camera.setRotate(180);

	
		setVirtualcam(new VirtualCameraDevice(camera,hand));
		VirtualCameraFactory.setFactory(new IVirtualCameraFactory() {
			@Override
			public AbstractImageProvider getVirtualCamera() {
				// TODO Auto-generated method stub
				return getVirtualcam();
			}
		});
		
		try {
			setFlyingCamera(new VirtualCameraMobileBase());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		moveCamera(
				new TransformNR(0,
						0, 
						0, 
						new RotationNR(24,-127,0)
						),0);
		defautcameraView = getFlyingCamera().getFiducialToGlobalTransform();
	}
	
	/**
	 * Gets the camera field of view property.
	 *
	 * @return the camera field of view property
	 */
	public DoubleProperty getCameraFieldOfViewProperty(){
		return camera.fieldOfViewProperty();
	}

	/**
	 * Builds the axes.
	 */
	private void buildAxes() {
		
		int gridSize=1000;
		int gridDensity=gridSize/10;
		ground = new Group();
		Affine groundPlacment=new Affine();
		for(int i=-gridSize;i<gridSize;i++){
			for(int j=-gridSize;j<gridSize;j++){
				if(i%gridDensity==0 &&j%gridDensity==0){
					Sphere s = new Sphere(1);
					Affine sp=new Affine();
					sp.setTy(i);
					sp.setTx(j);
					//System.err.println("Placing sphere at "+i+" , "+j);
					s.getTransforms().add(sp);
					ground.getChildren().add(s);
				}
			}
		}

		groundPlacment.setTz(-1);
		//ground.setOpacity(.5);
		ground.getTransforms().add(groundPlacment);
		axisGroup.getChildren().addAll(new Axis(),ground, getVirtualcam().getCameraFrame());
		world.getChildren().addAll(axisGroup, lookGroup);
		
	}
	
	

	/**
	 * Handle mouse.
	 *
	 * @param scene the scene
	 * @param root the root
	 */
	private void handleMouse(SubScene scene) {
		scene.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				mousePosX = me.getSceneX();
				mousePosY = me.getSceneY();
				mouseOldX = me.getSceneX();
				mouseOldY = me.getSceneY();
				if(me.isPrimaryButtonDown())
					captureMouse=true;
				else
					captureMouse=false;
			}
		});
		scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				mouseOldX = mousePosX;
				mouseOldY = mousePosY;
				mousePosX = me.getSceneX();
				mousePosY = me.getSceneY();
				mouseDeltaX = (mousePosX - mouseOldX);
				mouseDeltaY = (mousePosY - mouseOldY);

				double modifier = 1.0;
				double modifierFactor = 0.1;

				if (me.isControlDown()) {
					modifier = 0.1;
				}
				if (me.isShiftDown()) {
					modifier = 10.0;
				}
				if (me.isPrimaryButtonDown()) {
//					cameraXform.ry.setAngle(cameraXform.ry.getAngle()
//							- mouseDeltaX * modifierFactor * modifier * 2.0); // +
//					cameraXform.rx.setAngle(cameraXform.rx.getAngle()
//							+ mouseDeltaY * modifierFactor * modifier * 2.0); // -
					if (me.isPrimaryButtonDown()) {
						moveCamera(new TransformNR(0,0,0,
								new RotationNR(
										mouseDeltaY * modifierFactor * modifier * 2.,
										-mouseDeltaX * modifierFactor * modifier * 2.0,
										0//
										))
								, 0);
					} 
				} 
				else if (me.isMiddleButtonDown()) {

				} 
				else if (me.isSecondaryButtonDown()) {
					moveCamera(new TransformNR(mouseDeltaX
							* modifierFactor * modifier * 10,
							mouseDeltaY
							* modifierFactor * modifier * 10,
							0,
							new RotationNR())
							, 0);
				}
			}
		});
		scene.addEventHandler(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {

			@Override
			public void handle(ScrollEvent t) {
				if (ScrollEvent.SCROLL == (t).getEventType()) {

//					double zoomFactor = (t.getDeltaY());
//
//					double z = camera.getTranslateY();
//					double newZ = z + zoomFactor;
//					camera.setTranslateY(newZ);
					// System.out.println("Z = "+newZ);
					getVirtualcam().setZoomDepth(getVirtualcam().getZoomDepth()+t.getDeltaY());
				}
				t.consume();
			}
		});

	}

	/**
	 * Handle keyboard.
	 *
	 * @param scene the scene
	 * @param root the root
	 */
	public void handleKeyboard(Scene scene) {
		//final boolean moveCamera = true;
		//System.out.println("Adding keyboard listeners");
		
		double modifier = 5.0;		
		
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			
			@Override
			public void handle(KeyEvent event) {
				//System.err.println(event);
				//Duration currentTime;
				
				switch (event.getCode()) {
				case W:
				case UP:
					upDown=modifier;
					break;
				case S:
				case DOWN:
					upDown=-modifier;
					break;
				case D:
				case RIGHT:
					leftRight=-modifier;
					break;
				case A:
				case LEFT:
					leftRight=modifier;
					break;
				default:// do not consume events associated with the navigation
					return;
				}
				moveCamera(new TransformNR(leftRight,upDown,0,
						new RotationNR())
						, 0);
				
				event.consume();
			}
			
		});
		scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
			
			@Override
			public void handle(KeyEvent event) {
				//System.err.println(event);
				//Duration currentTime;
				
				switch (event.getCode()) {
				case W:
				case UP:
				case S:
				case DOWN:
					upDown=0;
					break;
				case D:
				case RIGHT:
				case A:
				case LEFT:
					leftRight=0;
					break;
				default:// do not consume events associated with the navigation
					return;
				}
				moveCamera(new TransformNR(leftRight,upDown,0,
						new RotationNR())
						, 0);
				
				event.consume();
			}
			
		});
	}
	
	private void moveCamera( TransformNR newPose, double seconds){
		getFlyingCamera()
		.DriveArc(newPose, seconds);
		// Selection of a part fro the cameras focal point
//		TransformNR t = offsetForVisualization// re-orent the frame of reference for the gobal camera. 
//				.times(getFlyingCamera().getFiducialToGlobalTransform());
//		for ( Entry<CSG, MeshView> bits:csgMap.entrySet()){
//			Bounds locBounds = bits.getValue().getBoundsInParent();
//			if(locBounds.contains(	t.getX(),
//									t.getY(), 
//									t.getZ())){
//				//System.err.println("Object in screen bounded by: "+locBounds);
//				//System.err.println("Look Center: "+getFlyingCamera().getFiducialToGlobalTransform());
//				//bits.getKey().getCreationEventStackTrace().printStackTrace();
//		        selectObjectsSourceFile(bits.getKey());
//			}
//		}
	}
	
	private void selectObjectsSourceFile(CSG source ){
		BowlerStudioController.getBowlerStudio().clearHighlits();
		debuggerList.clear();
		debuggerIndex=0;

		for(String ex: source.getCreationEventStackTraceList()){
			
			String fileName = getFilenameFromTrace(ex);
			int linNum = getLineNumbereFromTrace(ex);

			boolean duplicate=false;
			for(String have:debuggerList){
				if(		   getFilenameFromTrace(have).contentEquals(fileName)
						&& getLineNumbereFromTrace(have) == linNum
						)
					duplicate=true;
			}
			if(!duplicate)
				debuggerList.add(0,ex);

			lastFileSelected=fileName;
			lastFileLine=linNum;
			
			BowlerStudioController.getBowlerStudio().setHighlight(ScriptingEngine.getFileEngineRunByName(fileName),linNum,java.awt.Color.PINK);

		        		
		    	
		}
		debuggerIndex = debuggerList.size()-1;
		Platform.runLater(()->{
			fwd.disableProperty().set(false);
			back.disableProperty().set(true);
		});
	    
	}

	// @Override
	// public void start(Stage primaryStage) {
	//
	//
	// }

	/**
	 * The main() method is ignored in correctly deployed JavaFX application.
	 * main() serves only as fallback in case the application can not be
	 * launched through deployment artifacts, e.g., in IDEs with limited FX
	 * support. NetBeans ignores main().
	 *
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		System.setProperty("prism.dirtyopts", "false");

		JFrame frame = new JFrame();
		frame.setContentPane(new BowlerStudio3dEngine());

		frame.setSize(1024, 1024);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	/**
	 * Gets the sub scene.
	 *
	 * @return the sub scene
	 */
	public SubScene getSubScene() {
		return scene;
	}

	/**
	 * Sets the sub scene.
	 *
	 * @param scene the new sub scene
	 */
	public void setSubScene(SubScene scene) {
		this.scene = scene;
	}

	/**
	 * Gets the root.
	 *
	 * @return the root
	 */
	public Group getRoot() {
		return root;
	}
	
	/**
	 * Removes the arm.
	 */
	public void removeArm() {
		world.getChildren().remove(manipulator);
	}

	public VirtualCameraDevice getVirtualcam() {
		return virtualcam;
	}

	public void setVirtualcam(VirtualCameraDevice virtualcam) {
		this.virtualcam = virtualcam;
	}

	public VirtualCameraMobileBase getFlyingCamera() {
		return flyingCamera;
	}

	public void setFlyingCamera(VirtualCameraMobileBase flyingCamera) {
		this.flyingCamera = flyingCamera;
	}

	public static TransformNR getOffsetforvisualization() {
		return offsetForVisualization;
	}
}