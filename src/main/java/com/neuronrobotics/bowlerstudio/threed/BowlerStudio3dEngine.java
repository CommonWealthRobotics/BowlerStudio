package com.neuronrobotics.bowlerstudio.threed;

import java.awt.image.BufferedImage;

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
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.python.modules.thread.thread;
import org.reactfx.util.FxTimer;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.BowlerStudioModularFrame;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.creature.EngineeringUnitsSliderWidget;
import com.neuronrobotics.bowlerstudio.creature.IOnEngineeringUnitsChange;
import com.neuronrobotics.bowlerstudio.physics.TransformFactory;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.imageprovider.AbstractImageProvider;
import com.neuronrobotics.imageprovider.IVirtualCameraFactory;
import com.neuronrobotics.imageprovider.VirtualCameraFactory;
import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.ITaskSpaceUpdateListenerNR;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
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
import com.neuronrobotics.sdk.util.ThreadUtil;
import com.sun.javafx.geom.transform.Affine3D;
import com.sun.javafx.geom.transform.BaseTransform;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;
import eu.mihosoft.vrl.v3d.Cylinder;
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.parametrics.CSGDatabase;
import eu.mihosoft.vrl.v3d.parametrics.IParameterChanged;
import eu.mihosoft.vrl.v3d.parametrics.IParametric;
import eu.mihosoft.vrl.v3d.parametrics.LengthParameter;
import eu.mihosoft.vrl.v3d.parametrics.Parameter;
import eu.mihosoft.vrl.v3d.parametrics.StringParameter;
import javafx.application.Application;
import javafx.application.Platform;
import static javafx.application.Application.launch;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.SubScene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.PerspectiveCamera;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;

import static javafx.scene.input.KeyCode.*;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
//import javafx.util.Duration;
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

	/** The grid group. */
	final Group gridGroup = new Group();
	/** The world. */
	final Xform world = new Xform();

	/** The camera. */
	final PerspectiveCamera camera = new PerspectiveCamera(true);

	/** The camera distance. */
	final double cameraDistance = 4000;

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
	/** The look group. */
	private final Group focusGroup = new Group();
	/** The user group. */
	private final Group userGroup = new Group();
	/** The scene. */
	private SubScene scene;

	/** The ground. */
	private Group ground;

	private boolean captureMouse = false;

	private VirtualCameraDevice virtualcam;

	private VirtualCameraMobileBase flyingCamera;
	private Group hand;
	private double upDown = 0;
	private double leftRight = 0;
	private HashMap<CSG, MeshView> csgMap = new HashMap<>();
	private HashMap<CSG, File> csgSourceFile = new HashMap<>();
	private HashMap<MeshView, Axis> axisMap = new HashMap<>();
	private String lastFileSelected = "";
	private int lastFileLine = 0;
	private File defaultStlDir;
	private TransformNR defautcameraView;
	// private static final TransformNR offsetForVisualization = new
	// TransformNR(0, 0, 0, new RotationNR(0,0, 0));

	private Button back;

	private Button fwd;

	private Button home;
	private int debuggerIndex = 0;
	private ArrayList<String> debuggerList = new ArrayList<>();
	private CSG selectedCsg = null;
	double color = 0;
	private long lastMosueMovementTime = System.currentTimeMillis();

	private List<CSG> selectedSet = null;
	private TransformNR perviousTarget = new TransformNR();

	private boolean spinSelected=true;

	private long lastSelectedTime=System.currentTimeMillis();

	private long timeForAutospin = 5000;;

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
		// handleKeyboard(s);
		handleMouse(getSubScene());

		setScene(s);

		// new Thread() {
		//
		// public void run() {
		// setName("3d Highlighter");
		//
		// while (true) {
		// ThreadUtil.wait(100);
		// if (getSelectedCsg() != null) {
		// Color newColor =
		// getSelectedCsg().getColor().interpolate(Color.YELLOW,
		// Math.sin(color));
		// color += .05;
		// if (color > Math.PI)
		// color = 0;
		// PhongMaterial m = new PhongMaterial(newColor);
		// // current.setMaterial(m);
		// Platform.runLater(() -> csgMap.get(getSelectedCsg()).setMaterial(m));
		// }
		// }
		// }
		// }.start();
		//
		autoSpin();
	}

	private void highlightDebugIndex(int index, java.awt.Color c) {
		String trace = debuggerList.get(index);
		BowlerStudioController.getBowlerStudio().setHighlight(locateFile(getFilenameFromTrace(trace), getSelectedCsg()),
				getLineNumbereFromTrace(trace), c);
	}

	private String getFilenameFromTrace(String trace) {
		String[] parts = trace.split(":");
		return parts[0];
	}

	private int getLineNumbereFromTrace(String trace) {
		String[] parts = trace.split(":");
		return Integer.parseInt(parts[1]);
	}

	public Group getControlsBox() {
		HBox controls = new HBox(10);
		home = new Button("Home Camera");
		home.setGraphic(AssetFactory.loadIcon("Home-Camera.png"));
		home.setOnAction(event -> {
			getFlyingCamera().setGlobalToFiducialTransform(defautcameraView);
			getVirtualcam().setZoomDepth(VirtualCameraDevice.getDefaultZoomDepth());
			getFlyingCamera().updatePositions();
		});

		Button clear = new Button("Clear");
		clear.setGraphic(AssetFactory.loadIcon("Clear-Screen.png"));
		clear.setOnAction(event -> {
			clearUserNode();
			removeObjects();
		});
		CheckBox spin = new CheckBox("Idle Spin");
		spin.setSelected(true);
		spin.setOnAction((event) -> {
			spinSelected = spin.isSelected();
			resetMouseTime();
		});
		CheckBox ruler = new CheckBox("Show Ruler");
		ruler.setSelected(true);
		ruler.setOnAction((event) -> {
			boolean selected = ruler.isSelected();
			// System.out.println("CheckBox Action (selected: " + selected +
			// ")");
			if (selected)
				showAxis();
			else
				hideAxis();
		});

		controls.getChildren().addAll(home, ruler,spin, clear);
		return new Group(controls);
	}

	public Group getDebuggerBox() {
		HBox controls = new HBox(10);

		back = new Button("Back");
		fwd = new Button("Forward");

		fwd.setOnAction(event -> {
			BowlerStudioController.getBowlerStudio().clearHighlits();
			highlightDebugIndex(debuggerIndex, java.awt.Color.PINK);
			debuggerIndex--;
			if (debuggerIndex == 0) {
				fwd.disableProperty().set(true);
			}
			back.disableProperty().set(false);
			highlightDebugIndex(debuggerIndex, java.awt.Color.GREEN);
		});

		back.setOnAction(event -> {
			BowlerStudioController.getBowlerStudio().clearHighlits();
			highlightDebugIndex(debuggerIndex, java.awt.Color.PINK);
			debuggerIndex++;
			if (debuggerIndex >= debuggerList.size()) {
				back.disableProperty().set(true);
				debuggerIndex--;
			}
			if (debuggerIndex > 0)
				fwd.disableProperty().set(false);
			highlightDebugIndex(debuggerIndex, java.awt.Color.GREEN);
		});

		fwd.disableProperty().set(true);
		back.disableProperty().set(true);

		controls.getChildren().addAll(new Label("Cad Debugger"), back, fwd);
		return new Group(controls);
	}

	/**
	 * Removes the objects.
	 */
	public void removeObjects() {
		lookGroup.getChildren().clear();
		getCsgMap().clear();
		csgSourceFile.clear();
		axisMap.clear();
	}

	/**
	 * Removes the object.
	 *
	 * @param previous
	 *            the previous
	 */
	public void removeObject(CSG previousCsg) {
		// System.out.println(" Removing a CSG from file: "+previousCsg+" from
		// file "+csgSourceFile.get(previousCsg));
		MeshView previous = getCsgMap().get(previousCsg);
		if (previous != null) {
			lookGroup.getChildren().remove(previous);
			lookGroup.getChildren().remove(axisMap.get(previous));
			axisMap.remove(previous);
		}
		getCsgMap().remove(previousCsg);
		csgSourceFile.remove(previousCsg);
	}

	private void fireRegenerate(String key, File source, Set<CSG> currentObjectsToCheck) {
		new Thread() {
			public void run() {
				ArrayList<CSG> toAdd = new ArrayList<>();
				ArrayList<CSG> toRemove = new ArrayList<>();

				Object[] array = null;
				synchronized (currentObjectsToCheck) {
					array = (Object[]) currentObjectsToCheck.toArray();
				}
				for (int i = 0; i < currentObjectsToCheck.size(); i++) {
					System.out.println("Testing for Regenerating " + i + " of " + currentObjectsToCheck.size());
					try {
						CSG tester = (CSG) array[i];
						for (String p : tester.getParameters()) {
							if (p.contentEquals(key) && !toRemove.contains(tester)) {
								System.out.println("Regenerating " + i + " on key " + p);
								try {
									CSG ret = tester.regenerate();
									toRemove.add(tester);
									toAdd.add(ret);
								} catch (Exception ex) {
									ex.printStackTrace(System.out);
								}
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace(System.out);
					}
				}
				for (CSG add : toRemove)
					Platform.runLater(() -> {
						removeObject(add);
					});
				for (CSG ret : toAdd)
					Platform.runLater(() -> {
						addObject(ret, source);
					});
			}
		}.start();
	}

	/**
	 * Adds the object.
	 *
	 * @param current
	 *            the current
	 * @return the mesh view
	 */
	public MeshView addObject(CSG currentCsg, File source) {
		BowlerStudioModularFrame.getBowlerStudioModularFrame().showCreatureLab();
		// System.out.println(" Adding a CSG from file: "+source.getName());
		if (getCsgMap().get(currentCsg) != null)
			return currentCsg.getMesh();
		getCsgMap().put(currentCsg, currentCsg.getMesh());
		csgSourceFile.put(currentCsg, source);

		MeshView current = getCsgMap().get(currentCsg);

		// TriangleMesh mesh =(TriangleMesh) current.getMesh();
		// mesh.vertexFormatProperty()
		ContextMenu cm = new ContextMenu();

		Set<String> params = currentCsg.getParameters();
		if (params != null) {
			Menu parameters = new Menu("Parameters...");

			for (String key : params) {
				Parameter param = CSGDatabase.get(key);
				currentCsg.setParameterIfNull(key);
				if (LengthParameter.class.isInstance(param)) {
					LengthParameter lp = (LengthParameter) param;

					EngineeringUnitsSliderWidget widget = new EngineeringUnitsSliderWidget(
							new IOnEngineeringUnitsChange() {

								@Override
								public void onSliderMoving(EngineeringUnitsSliderWidget s, double newAngleDegrees) {
									new Thread() {
										public void run() {
											try {
												currentCsg.setParameterNewValue(key, newAngleDegrees);

											} catch (Exception ex) {
												BowlerStudioController.highlightException(source, ex);
											}
										}
									}.start();
								}

								@Override
								public void onSliderDoneMoving(EngineeringUnitsSliderWidget s, double newAngleDegrees) {
									// Get the set of objects to check for
									// regeneration after the initioal
									// regeneration cycle.
									Set<CSG> objects = getCsgMap().keySet();
									cm.hide();// hide this menue because the new
												// CSG talks to the new menue

									fireRegenerate(key, source, objects);
									resetMouseTime();

								}
							}, Double.parseDouble(lp.getOptions().get(1).toString()),
							Double.parseDouble(lp.getOptions().get(0).toString()), lp.getMM(), 400, key);
					CustomMenuItem customMenuItem = new CustomMenuItem(widget);
					customMenuItem.setHideOnClick(false);
					parameters.getItems().add(customMenuItem);
					System.err.println("Adding Length Paramater " + lp.getName());
				} else {
					try {
						Parameter lp = (Parameter) param;
						if (lp != null) {
							Menu paramTypes = new Menu(lp.getName() + " " + lp.getStrValue());

							for (String opt : lp.getOptions()) {
								String myVal = opt;
								MenuItem customMenuItem = new MenuItem(myVal);
								customMenuItem.setOnAction(event -> {
									resetMouseTime();
									System.out.println("Updating " + lp.getName() + " to " + myVal);
									lp.setStrValue(myVal);
									CSGDatabase.get(lp.getName()).setStrValue(myVal);
									for (IParameterChanged l : CSGDatabase.getParamListeners(lp.getName())) {
										l.parameterChanged(lp.getName(), lp);
									}

									// Get the set of objects to check for
									// regeneration after the initioal
									// regeneration cycle.
									Set<CSG> objects = getCsgMap().keySet();
									cm.hide();// hide this menue because the new
												// CSG talks to the new menue
									fireRegenerate(key, source, objects);
								});
								paramTypes.getItems().add(customMenuItem);
							}

							parameters.getItems().add(paramTypes);
							System.err.println("Adding String Paramater " + lp.getName());
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
			cm.getItems().add(parameters);
		}

		MenuItem exportDXF = new MenuItem("Export SVG...");
		exportDXF.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				resetMouseTime();
				if (defaultStlDir == null)
					defaultStlDir = new File(System.getProperty("user.home") + "/bowler-workspace/STL/");
				if (!defaultStlDir.exists()) {
					defaultStlDir.mkdirs();
				}

				new Thread() {

					public void run() {
						try {
							defaultStlDir = SVGFactory.exportSVG(currentCsg.prepForManufacturing(), defaultStlDir);
						} catch (Exception e1) {
							BowlerStudioController.highlightException(source, e1);
						}

					}
				}.start();
			}
		});
		cm.getItems().add(exportDXF);

		MenuItem export = new MenuItem("Export STL...");
		export.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				resetMouseTime();
				if (defaultStlDir == null)
					defaultStlDir = new File(System.getProperty("user.home") + "/bowler-workspace/STL/");
				if (!defaultStlDir.exists()) {
					defaultStlDir.mkdirs();
				}

				new Thread() {

					public void run() {
						File baseDirForFiles = FileSelectionFactory.GetFile(defaultStlDir, true);
						defaultStlDir = baseDirForFiles.getParentFile();
						if (!baseDirForFiles.getAbsolutePath().toLowerCase().endsWith(".stl"))
							baseDirForFiles = new File(baseDirForFiles.getAbsolutePath() + ".stl");
						if (!baseDirForFiles.exists())
							try {
								baseDirForFiles.createNewFile();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						try {
							FileUtil.write(Paths.get(baseDirForFiles.getAbsolutePath()),
									currentCsg.prepForManufacturing().toStlString());
							System.out.println("Exported STL to" + baseDirForFiles.getAbsolutePath());
						} catch (Exception e) {
							BowlerStudioController.highlightException(source, e);
						}
					}
				}.start();
			}
		});
		cm.getItems().add(export);

		MenuItem cut = new MenuItem("Read Source");
		cut.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				resetMouseTime();
				setSelectedCsg(currentCsg);
				new Thread() {
					public void run() {
						selectObjectsSourceFile(selectedCsg);
					}
				}.start();
			}
		});
		cm.getItems().add(cut);
		cm.addEventFilter(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.getButton() == MouseButton.SECONDARY) {
					System.out.println("consuming right release button in cm filter");
					event.consume();
				}
			}
		});

		class closeTheMenueHandler implements EventHandler<MouseEvent> {
			long lastClickedTime = 0;

			@Override
			public void handle(MouseEvent event) {
				if (event.isSecondaryButtonDown() || event.isShiftDown())
					cm.show(current, event.getScreenX() - 10, event.getScreenY() - 10);
				else if (event.isPrimaryButtonDown()) {
					if (System.currentTimeMillis() - lastClickedTime < 500) {
						FxTimer.runLater(java.time.Duration.ofMillis(200), () -> setSelectedCsg(currentCsg));

					}
					lastClickedTime = System.currentTimeMillis();
				}

			}
		}
		closeTheMenueHandler cmh = new closeTheMenueHandler();
		current.addEventHandler(MouseEvent.MOUSE_PRESSED, cmh);

		// cm.getScene().addEventHandler(MouseEvent.MOUSE_EXITED, cmh);

		lookGroup.getChildren().add(current);
		Axis axis = new Axis();
		axis.getTransforms().add(currentCsg.getManipulator());
		axisMap.put(current, axis);
		lookGroup.getChildren().add(axis);
		// Log.warning("Adding new axis");
		return current;
	}

	private void prepAllItems(ObservableList<MenuItem> items, EventHandler<MouseEvent> exited,
			EventHandler<MouseEvent> entered) {
		for (MenuItem item : items) {
			if (Menu.class.isInstance(item)) {
				Menu m = (Menu) item;
				prepAllItems(m.getItems(), exited, entered);
			} else {
				item.addEventHandler(MouseEvent.MOUSE_EXITED, entered);
				item.addEventHandler(MouseEvent.MOUSE_ENTERED, entered);
			}

		}
	}

	/**
	 * Save to png.
	 *
	 * @param f
	 *            the f
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

		WritableImage snapshot = new WritableImage(snWidth, (int) (realHeight * scale));

		getRoot().snapshot(snapshotParameters, snapshot);

		try {
			ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", new File(fName));
		} catch (IOException ex) {
			ex.printStackTrace();
			Log.error(ex.getMessage());
		}
	}

	/**
	 * Builds the scene.
	 */
	private void buildScene() {
		world.ry.setAngle(-90);// point z upwards
		world.ry.setAngle(180);// arm out towards user
		getRoot().getChildren().add(world);
	}

	/**
	 * Builds the camera.
	 */
	private void buildCamera() {

		CSG cylinder = new Cylinder(0, // Radius at the top
				5, // Radius at the bottom
				20, // Height
				(int) 20 // resolution
		).toCSG().roty(90).setColor(Color.BLACK);

		hand = new Group(cylinder.getMesh());

		camera.setNearClip(.1);
		camera.setFarClip(100000.0);
		getSubScene().setCamera(camera);

		camera.setRotationAxis(Rotate.Z_AXIS);
		camera.setRotate(180);

		setVirtualcam(new VirtualCameraDevice(camera, hand));
		VirtualCameraFactory.setFactory(new IVirtualCameraFactory() {
			@Override
			public AbstractImageProvider getVirtualCamera() {

				return virtualcam;
			}
		});

		try {
			setFlyingCamera(new VirtualCameraMobileBase());
		} catch (Exception e) {
			e.printStackTrace();
		}
		// TODO reorent the start camera
		moveCamera(new TransformNR(0, 0, 0, new RotationNR(90-127, 24, 0)), 0);
		defautcameraView = getFlyingCamera().getFiducialToGlobalTransform();
	}

	/**
	 * Gets the camera field of view property.
	 *
	 * @return the camera field of view property
	 */
	public DoubleProperty getCameraFieldOfViewProperty() {
		return camera.fieldOfViewProperty();
	}

	/**
	 * Builds the axes.
	 */
	private void buildAxes() {

		// int gridSize=1000;
		// int gridDensity=gridSize/10;
		//
		// PhongMaterial phongMaterial = new PhongMaterial();
		// phongMaterial.setDiffuseColor(Color.BLACK);
		// for(int i=-gridSize;i<gridSize;i++){
		// for(int j=-gridSize;j<gridSize;j++){
		// if(i%gridDensity==0 &&j%gridDensity==0){
		// Sphere s = new Sphere(1);
		// s.setMaterial(phongMaterial);
		// Affine sp=new Affine();
		// sp.setTy(i);
		// sp.setTx(j);
		// //System.err.println("Placing sphere at "+i+" , "+j);
		// s.getTransforms().add(sp);
		// ground.getChildren().add(s);
		// }
		// }
		// }

		new Thread() {
			public void run() {
				try {
					Image ruler = AssetFactory.loadAsset("ruler.png");
					Image ground = AssetFactory.loadAsset("ground.png");
					Affine groundMove = new Affine();
					// groundMove.setTz(-3);
					groundMove.setTx(-ground.getHeight() / 2);
					groundMove.setTy(-ground.getWidth() / 2);

					Affine zRuler = new Affine();
					double scale = 0.25;
					// zRuler.setTx(-130*scale);
					zRuler.setTz(-20 * scale);
					zRuler.appendScale(scale, scale, scale);
					zRuler.appendRotation(-180, 0, 0, 0, 1, 0, 0);
					zRuler.appendRotation(-90, 0, 0, 0, 0, 0, 1);
					zRuler.appendRotation(90, 0, 0, 0, 0, 1, 0);
					zRuler.appendRotation(-180, 0, 0, 0, 1, 0, 0);

					Affine yRuler = new Affine();
					yRuler.setTx(-130 * scale);
					yRuler.setTy(-20 * scale);
					yRuler.appendScale(scale, scale, scale);
					yRuler.appendRotation(180, 0, 0, 0, 1, 0, 0);
					yRuler.appendRotation(-90, 0, 0, 0, 0, 0, 1);

					Affine xp = new Affine();
					xp.setTx(-20 * scale);
					xp.appendScale(scale, scale, scale);
					xp.appendRotation(180, 0, 0, 0, 1, 0, 0);
					Platform.runLater(() -> {
						ImageView rulerImage = new ImageView(ruler);
						ImageView yrulerImage = new ImageView(ruler);
						ImageView zrulerImage = new ImageView(ruler);
						ImageView groundView = new ImageView(ground);
						groundView.getTransforms().add(groundMove);
						groundView.setOpacity(0.3);
						zrulerImage.getTransforms().add(zRuler);
						rulerImage.getTransforms().add(xp);
						yrulerImage.getTransforms().add(yRuler);
						gridGroup.getChildren().addAll(zrulerImage, rulerImage, yrulerImage, groundView);
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
		Affine xp = new Affine();
		xp.setTx(25);
		Label xText = new Label("+X");
		xText.getTransforms().add(xp);

		Affine yp = new Affine();
		yp.setTy(25);
		Label yText = new Label("+Y");
		yText.getTransforms().add(yp);

		Affine zp = new Affine();
		zp.setTz(25);
		zp.setTx(25);
		zp.appendRotation(-90, 0, 0, 0, 1, 0, 0);
		zp.appendRotation(180, 0, 0, 0, 0, 0, 1);

		Label zText = new Label("+Z");
		zText.getTransforms().add(zp);
		Affine groundPlacment = new Affine();
		groundPlacment.setTz(-1);
		// ground.setOpacity(.5);
		ground = new Group();
		ground.getTransforms().add(groundPlacment);
		focusGroup.getChildren().add(getVirtualcam().getCameraFrame());

		gridGroup.getChildren().addAll(yText, zText, xText, ground);
		showAxis();
		axisGroup.getChildren().addAll(focusGroup, userGroup);
		world.getChildren().addAll(lookGroup, axisGroup);

	}

	public void addUserNode(Node n) {
		BowlerStudioModularFrame.getBowlerStudioModularFrame().showCreatureLab();

		Platform.runLater(() -> userGroup.getChildren().add(n));
	}

	public void removeUserNode(Node n) {
		Platform.runLater(() -> userGroup.getChildren().remove(n));
	}

	public void clearUserNode() {
		Platform.runLater(() -> userGroup.getChildren().clear());
	}

	public void showAxis() {
		Platform.runLater(() -> axisGroup.getChildren().add(gridGroup));
		for (MeshView a : axisMap.keySet()) {
			axisMap.get(a).show();
		}
	}

	public void hideAxis() {
		Platform.runLater(() -> axisGroup.getChildren().remove(gridGroup));
		for (MeshView a : axisMap.keySet()) {
			axisMap.get(a).hide();
		}
	}
	
	private void autoSpin(){
		
		long diff = System.currentTimeMillis() - getLastMosueMovementTime();
		
		if(diff>timeForAutospin && spinSelected){
			//TODO start spinning
			double scale = 1;
			long finaSpeedScale =  timeForAutospin+(timeForAutospin/2);
			if(diff<finaSpeedScale){
				double finaSpeedDiff = ((double)(finaSpeedScale- diff));
				double sineScale = (finaSpeedDiff/((double)(timeForAutospin/2)));
				scale = 1-Math.sin(sineScale * (Math.PI/2));
				
			}
			moveCamera(new TransformNR(0, 0, 0, new RotationNR(0, 0.5*scale, 0)), 0);
			
		}
		FxTimer.runLater(Duration.ofMillis(60), () -> {
			autoSpin();
		});
	}

	/**
	 * Handle mouse.
	 *
	 * @param scene
	 *            the scene
	 * @param root
	 *            the root
	 */
	private void handleMouse(SubScene scene) {

		scene.setOnMouseClicked(new EventHandler<MouseEvent>() {
			long lastClickedTimeLocal = 0;
			long offset = 500;
			@Override
			public void handle(MouseEvent event) {
				resetMouseTime();
				long lastClickedDifference= (System.currentTimeMillis() - lastClickedTimeLocal);
				FxTimer.runLater(Duration.ofMillis(100), () -> {
					long differenceIntime = System.currentTimeMillis() - lastSelectedTime;
					if(differenceIntime>2000){
						//reset only if an object is not being selected
						if (lastClickedDifference < offset) {
							cancelSelection();
							//System.err.println("Cancel event detected");
						}		
					}else{
						//System.err.println("too soon after a select "+differenceIntime+" from "+lastSelectedTime);
					}
				});
				lastClickedTimeLocal= System.currentTimeMillis();
			}

		});
		scene.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				mousePosX = me.getSceneX();
				mousePosY = me.getSceneY();
				mouseOldX = me.getSceneX();
				mouseOldY = me.getSceneY();
				if (me.isPrimaryButtonDown())
					captureMouse = true;
				else
					captureMouse = false;
				resetMouseTime();
				
			}
		});
		scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				resetMouseTime();
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
					// cameraXform.ry.setAngle(cameraXform.ry.getAngle()
					// - mouseDeltaX * modifierFactor * modifier * 2.0); // +
					// cameraXform.rx.setAngle(cameraXform.rx.getAngle()
					// + mouseDeltaY * modifierFactor * modifier * 2.0); // -
					// RotationNR roz = RotationNR.getRotationZ(-mouseDeltaX *
					// modifierFactor * modifier * 2.0);
					// RotationNR roy = RotationNR.getRotationY(mouseDeltaY *
					// modifierFactor * modifier * 2.);
					TransformNR trans = new TransformNR(0, 0, 0,
							new RotationNR(mouseDeltaY * modifierFactor * modifier * 2.0,
									mouseDeltaX * modifierFactor * modifier * 2.0, 0

							));

					if (me.isPrimaryButtonDown()) {
						moveCamera(trans, 0);
					}
				} else if (me.isMiddleButtonDown()) {

				} else if (me.isSecondaryButtonDown()) {
					double depth = -100 / getVirtualcam().getZoomDepth();
					moveCamera(new TransformNR(mouseDeltaX * modifierFactor * modifier * 1 / depth,
							mouseDeltaY * modifierFactor * modifier * 1 / depth, 0, new RotationNR()), 0);
				}
			}
		});
		scene.addEventHandler(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {

			@Override
			public void handle(ScrollEvent t) {
				if (ScrollEvent.SCROLL == t.getEventType()) {

					double zoomFactor = -(t.getDeltaY()) * getVirtualcam().getZoomDepth() / 500;
					//
					// double z = camera.getTranslateY();
					// double newZ = z + zoomFactor;
					// camera.setTranslateY(newZ);
					// System.out.println("Z = "+zoomFactor);

					getVirtualcam().setZoomDepth(getVirtualcam().getZoomDepth() + zoomFactor);
				}
				t.consume();
			}
		});

	}

	private void moveCamera(TransformNR newPose, double seconds) {
		getFlyingCamera().DriveArc(newPose, seconds);

	}

	private void selectObjectsSourceFile(CSG source) {
		new Thread(() -> {
			BowlerStudioController.getBowlerStudio().clearHighlits();
			debuggerList.clear();
			debuggerIndex = 0;

			for (String ex : source.getCreationEventStackTraceList()) {

				String fileName = getFilenameFromTrace(ex);
				int linNum = getLineNumbereFromTrace(ex);

				boolean duplicate = false;
				for (String have : debuggerList) {
					if (getFilenameFromTrace(have).contentEquals(fileName) && getLineNumbereFromTrace(have) == linNum)
						duplicate = true;
				}
				if (!duplicate)
					debuggerList.add(0, ex);

				lastFileSelected = fileName;
				lastFileLine = linNum;

				BowlerStudioController.getBowlerStudio().setHighlight(locateFile(fileName, source), linNum,
						java.awt.Color.PINK);

			}
			debuggerIndex = debuggerList.size() - 1;
		}).start();
		// Platform.runLater(()->{
		// fwd.disableProperty().set(false);
		// back.disableProperty().set(true);
		// });

	}

	private File locateFile(String fileName, CSG source) {
		File f = csgSourceFile.get(source);
		if (f != null && f.getName().contains(fileName))
			return f;
		return ScriptingEngine.getFileEngineRunByName(fileName);
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
	 * @param scene
	 *            the new sub scene
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

	// public static TransformNR getOffsetforvisualization() {
	// return offsetForVisualization;
	// }

	public CSG getSelectedCsg() {
		return selectedCsg;
	}

	public void cancelSelection() {
		for (CSG key : getCsgMap().keySet()) {

			Platform.runLater(() -> getCsgMap().get(key).setMaterial(new PhongMaterial(key.getColor())));
		}

		selectedSet = null;
		this.selectedCsg = null;
		//new Exception().printStackTrace();
		TransformNR startSelectNr = perviousTarget.copy();
		TransformNR targetNR = new TransformNR();
		Affine interpolator = new Affine();
		TransformFactory.nrToAffine(startSelectNr, interpolator);
		
		Platform.runLater(() -> {
			
			removeAllFocusTransforms();
			
			focusGroup.getTransforms().add(interpolator);

			focusInterpolate(startSelectNr, targetNR, 0, 15, interpolator);

		});
		resetMouseTime();
	}
	
	

	public void setSelectedCsg(List<CSG> selectedCsg) {
		// System.err.println("Selecting group");
		selectedSet = selectedCsg;
		for (int in = 1; in < selectedCsg.size(); in++) {
			int i = in;
			MeshView mesh = getCsgMap().get(selectedCsg.get(i));
			if (mesh != null)
				FxTimer.runLater(java.time.Duration.ofMillis(20),

						() -> {
							// mesh.setMaterial(new PhongMaterial(new Color(
							// 1,
							// (selectedCsg.get(i).getColor().getGreen())*0.6,
							// (selectedCsg.get(i).getColor().getBlue())*0.6,
							// selectedCsg.get(i).getColor().getOpacity())));
							mesh.setMaterial(new PhongMaterial(Color.GOLD));
						});

		}
		resetMouseTime();
	}

	public void setSelectedCsg(CSG scg) {
		if (scg == this.selectedCsg)
			return;

		for (CSG key : getCsgMap().keySet()) {

			Platform.runLater(() -> getCsgMap().get(key).setMaterial(new PhongMaterial(key.getColor())));
		}
		lastSelectedTime = System.currentTimeMillis();
		//System.err.println("Selecting a CSG");
		
		selectedSet = null;
		// System.err.println("Selecting one");
		this.selectedCsg = scg;

		FxTimer.runLater(java.time.Duration.ofMillis(20),

				() -> {
					try {
						getCsgMap().get(selectedCsg).setMaterial(new PhongMaterial(Color.GOLD));
					} catch (Exception e) {
					}
				});
		// System.out.println("Selecting "+selectedCsg);
		double xcenter = selectedCsg.getMaxX() / 2 + selectedCsg.getMinX() / 2;
		double ycenter = selectedCsg.getMaxY() / 2 + selectedCsg.getMinY() / 2;
		double zcenter = selectedCsg.getMaxZ() / 2 + selectedCsg.getMinZ() / 2;

		TransformNR poseToMove = new TransformNR();
		CSG finalCSG = selectedCsg;
		if (selectedCsg.getMaxX() < 1 || selectedCsg.getMinX() > -1) {
			finalCSG = finalCSG.movex(-xcenter);
			poseToMove.translateX(xcenter);
		}
		if (selectedCsg.getMaxY() < 1 || selectedCsg.getMinY() > -1) {
			finalCSG = finalCSG.movey(-ycenter);
			poseToMove.translateY(ycenter);
		}
		if (selectedCsg.getMaxZ() < 1 || selectedCsg.getMinZ() > -1) {
			finalCSG = finalCSG.movez(-zcenter);
			poseToMove.translateZ(zcenter);
		}
		Affine centering = TransformFactory.nrToAffine(poseToMove);
		// this section keeps the camera orented the same way to avoid whipping
		// around
		TransformNR rotationOnlyCOmponentOfManipulator = TransformFactory.affineToNr(selectedCsg.getManipulator());
		rotationOnlyCOmponentOfManipulator.setX(0);
		rotationOnlyCOmponentOfManipulator.setY(0);
		rotationOnlyCOmponentOfManipulator.setZ(0);
		TransformNR reverseRotation = rotationOnlyCOmponentOfManipulator.inverse();

		// Platform.runLater(() -> {
		// focusGroup.getTransforms().clear();
		// if (Math.abs(selectedCsg.getManipulator().getTx()) > 0.1
		// || Math.abs(selectedCsg.getManipulator().getTy()) > 0.1
		// || Math.abs(selectedCsg.getManipulator().getTz()) > 0.1) {
		// Platform.runLater(() -> {
		// focusGroup.getTransforms().add(selectedCsg.getManipulator());
		// focusGroup.getTransforms().add(TransformFactory.nrToAffine(reverseRotation));
		// });
		//
		// } else
		// focusGroup.getTransforms().add(centering);
		//
		// });
		//System.err.println("Camera intrpolation start");
		TransformNR startSelectNr = perviousTarget.copy();
		TransformNR targetNR;// =
								// TransformFactory.affineToNr(selectedCsg.getManipulator());
		if (Math.abs(selectedCsg.getManipulator().getTx()) > 0.1 || Math.abs(selectedCsg.getManipulator().getTy()) > 0.1
				|| Math.abs(selectedCsg.getManipulator().getTz()) > 0.1) {
			targetNR = TransformFactory.affineToNr(selectedCsg.getManipulator());
		} else {
			targetNR = TransformFactory.affineToNr(centering);
		}
		Affine interpolator = new Affine();
		Affine correction=TransformFactory.nrToAffine(reverseRotation);
		
		Platform.runLater(() -> {
			interpolator.setTx(startSelectNr.getX()- targetNR.getX());
			interpolator.setTy(startSelectNr.getY()- targetNR.getY());
			interpolator.setTz(startSelectNr.getZ()- targetNR.getZ());
			removeAllFocusTransforms();
			focusGroup.getTransforms().add(interpolator);
			try{
				if (Math.abs(selectedCsg.getManipulator().getTx()) > 0.1
						|| Math.abs(selectedCsg.getManipulator().getTy()) > 0.1
						|| Math.abs(selectedCsg.getManipulator().getTz()) > 0.1) {
					//Platform.runLater(() -> {
						focusGroup.getTransforms().add(selectedCsg.getManipulator());
						focusGroup.getTransforms().add(correction);
					//});

				} else
					//Platform.runLater(() -> {
						focusGroup.getTransforms().add(centering);
					//});
			}catch (Exception ex){
				
			}
			focusInterpolate(startSelectNr, targetNR, 0, 30, interpolator);
		});
		resetMouseTime();
	}

	private void resetMouseTime() {
		//System.err.println("Resetting mouse");
		this.lastMosueMovementTime = System.currentTimeMillis();
		
	}

	private void focusInterpolate(TransformNR start, TransformNR target, int depth, int targetDepth,
			Affine interpolator) {
		double depthScale = 1-(double) depth / (double) targetDepth;
		double sinunsoidalScale = Math.sin(depthScale*(Math.PI/2));
		
		//double xIncrement =target.getX()- ((start.getX() - target.getX()) * depthScale) + start.getX();
		double difference = start.getX() - target.getX();
		double scaledDifference = (difference * sinunsoidalScale); 
		
		double xIncrement = scaledDifference ;
		double yIncrement =((start.getY() - target.getY()) * sinunsoidalScale);
		double zIncrement = ((start.getZ() - target.getZ()) * sinunsoidalScale);

		Platform.runLater(() -> {
			interpolator.setTx(xIncrement);
			interpolator.setTy(yIncrement);
			interpolator.setTz(zIncrement);
		});
		//System.err.println("Interpolation step " + depth + " x " + xIncrement + " y " + yIncrement + " z " + zIncrement);
		if (depth < targetDepth) {
			FxTimer.runLater(Duration.ofMillis(16), () -> {
				focusInterpolate(start, target, depth + 1, targetDepth, interpolator);
			});
		} else {
			//System.err.println("Camera intrpolation done");
			Platform.runLater(() -> {
				focusGroup.getTransforms().remove(interpolator);
			});
			perviousTarget = target.copy();
			perviousTarget.setRotation(new RotationNR());
		}

	}

	private void removeAllFocusTransforms() {
		ObservableList<Transform> allTrans = focusGroup.getTransforms();
		 List<Object> toRemove = Arrays.asList(allTrans.toArray());
		for(Object t:toRemove){
			allTrans.remove(t);
		}
	}

	public HashMap<CSG, MeshView> getCsgMap() {
		return csgMap;
	}

	public void setCsgMap(HashMap<CSG, MeshView> csgMap) {
		this.csgMap = csgMap;
	}

	public void setSelectedCsg(File script, int lineNumber) {

		ArrayList<CSG> objsFromScriptLine = new ArrayList<>();
		// check all visable CSGs
		for (CSG checker : getCsgMap().keySet()) {
			for (String trace : checker.getCreationEventStackTraceList()) {
				String[] traceParts = trace.split(":");
				// System.err.println("Seeking: "+script.getName()+" line=
				// "+lineNumber+" checking from line: "+trace);
				// System.err.println("TraceParts "+traceParts[0]+" and
				// "+traceParts[1]);
				if (traceParts[0].trim().toLowerCase().contains(script.getName().toLowerCase().trim())) {
					// System.out.println("Script matches");
					try {
						int num = Integer.parseInt(traceParts[1].trim());

						if (num == lineNumber) {
							// System.out.println("MATCH");
							objsFromScriptLine.add(checker);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		if (objsFromScriptLine.size() > 0) {
			setSelectedCsg(objsFromScriptLine.get(0));
			setSelectedCsg(objsFromScriptLine);
		}
	}

	public long getLastMosueMovementTime() {
		return lastMosueMovementTime;
	}


}