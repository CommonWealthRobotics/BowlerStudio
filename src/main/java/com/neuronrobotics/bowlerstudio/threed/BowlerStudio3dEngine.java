package com.neuronrobotics.bowlerstudio.threed;

import com.neuronrobotics.bowlerstudio.BowlerKernel;
import com.neuronrobotics.bowlerstudio.BowlerStudio;

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

import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.BowlerStudioModularFrame;
import com.neuronrobotics.bowlerstudio.IssueReportingExceptionHandler;
//import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.creature.CadFileExporter;
import com.neuronrobotics.bowlerstudio.creature.EngineeringUnitsSliderWidget;
import com.neuronrobotics.bowlerstudio.creature.IOnEngineeringUnitsChange;
import com.neuronrobotics.bowlerstudio.physics.TransformFactory;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.imageprovider.AbstractImageProvider;
import com.neuronrobotics.imageprovider.IVirtualCameraFactory;
import com.neuronrobotics.imageprovider.VirtualCameraFactory;
import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
//import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.Log;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cylinder;
import eu.mihosoft.vrl.v3d.JavaFXInitializer;
import eu.mihosoft.vrl.v3d.parametrics.CSGDatabase;
import eu.mihosoft.vrl.v3d.parametrics.IParameterChanged;
import eu.mihosoft.vrl.v3d.parametrics.LengthParameter;
import eu.mihosoft.vrl.v3d.parametrics.Parameter;
import javafx.application.Platform;
import javafx.collections.ObservableList;
//import javafx.embed.swing.JFXPanel;
//import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.*;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
//import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.time.Duration;
import java.util.*;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.paint.Color;

/**
 * MoleculeSampleApp.
 */
public class BowlerStudio3dEngine implements ICameraChangeListener {
	private boolean focusing = false;
	private double numberOfInterpolationSteps = 30;
	private MeshView grid;

	/**
	 * 
	 */
	private static final long serialVersionUID = 6744581340628622682L;

	private static final TransformNR autoSpinSpeed = new TransformNR(0, 0, 0, new RotationNR(0, 0.25, 0));

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
	private Group group;
	private boolean captureMouse = false;
	private Button export;;

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
	private TransformNR defautcameraView = new TransformNR(0, 0, 0, new RotationNR(90 - 127, 24, 0));
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

	// private List<CSG> selectedSet = null;
	private TransformNR perviousTarget = new TransformNR();

	private long lastSelectedTime = System.currentTimeMillis();

	private long timeForAutospin = 5000;

	// private CheckBox spin;

	// private CheckBox autoHighilight;

	private boolean rebuildingUIOnerror = false;
	private static int sumVert = 0;
	private CheckMenuItem autoHighilight;
	private CheckMenuItem spin;
	private HBox controlsChecks;
	private Thread autospingThread = null;
	private CheckMenuItem showRuler;
	private TransformNR targetNR;
	private TransformNR poseToMove = new TransformNR();
	private ArrayList<ICameraChangeListener> listeners = new ArrayList<>();
	private Affine gridPlacementAffine = new Affine();

	public BowlerStudio3dEngine addListener(ICameraChangeListener l) {
		if(!listeners.contains(l))
			listeners.add(l);
		return this;
	}
	public BowlerStudio3dEngine removeListener(ICameraChangeListener l) {
		if(listeners.contains(l))
			listeners.remove(l);
		return this;
	}
	private IControlsMap map = new IControlsMap() {
		long lastClickedTimeLocal = 0;
		long offset = 500;

		public boolean timeToCancel(MouseEvent event) {
			long lastClickedDifference = (System.currentTimeMillis() - lastClickedTimeLocal);
			long differenceIntime = System.currentTimeMillis() - lastSelectedTime;
			boolean ret = false;
			if (differenceIntime > 2000) {
				// reset only if an object is not being selected
				if (lastClickedDifference < offset) {

					System.err.println("Cancel event detected");
					ret = true;
				}
			}
			lastClickedTimeLocal = System.currentTimeMillis();
			return ret;
		}
		
		public boolean isSlowMove(MouseEvent event) {
			return event.isControlDown();
		}
		public boolean isRotate(MouseEvent me) {
			boolean shiftDown = me.isShiftDown();
			boolean primaryButtonDown = me.isPrimaryButtonDown();

			return (me.isPrimaryButtonDown() && primaryButtonDown && !shiftDown) ;
		}

		public boolean isMove(MouseEvent me) {
			boolean shiftDown = me.isShiftDown();
			boolean primaryButtonDown = me.isPrimaryButtonDown();
			boolean secondaryButtonDown = me.isSecondaryButtonDown();
			return (secondaryButtonDown || (primaryButtonDown && (shiftDown))) ;
		}
		public boolean isZoom(javafx.scene.input.ScrollEvent t) {
			return ScrollEvent.SCROLL == t.getEventType();
		}

	};
	private double mouseScale=2.0;
	private MeshView handMesh;
	private ImageView homeIcon;
	private ImageView generateIcon;
	private ImageView clearIcon;
	private boolean move=true;
	private boolean disabeControl=false;
	private String name;;

	/**
	 * Instantiates a new jfx3d manager.
	 * @param string 
	 */
	public BowlerStudio3dEngine(String name) {
		this.name = name;
		BowlerStudio.runLater(() -> {
			Thread.currentThread().setUncaughtExceptionHandler(new IssueReportingExceptionHandler());
		});
		System.err.println("Setting Scene ");
		setSubScene(new SubScene(getRoot(), 10, 10, true, SceneAntialiasing.BALANCED));

		// Set up the Ui THread explosion handler

	}

	public void rebuild(boolean b) {
		rebuildingUIOnerror = true;
		System.err.println("Building scene "+name);
		buildScene();
		System.err.println("Building camera "+name);

		buildCamera(b);
		System.err.println("Building axis "+name);
		buildAxes(b);
		
		Stop[] stops = null;
		System.err.println("Building gradiant "+name );

		getSubScene().setFill(new LinearGradient(125, 0, 225, 0, false, CycleMethod.NO_CYCLE, stops));
		group = new Group(getSubScene());
		Scene s = new Scene(group);
		// handleKeyboard(s);
		handleMouse(getSubScene());

		BowlerStudio.runLater(() -> {
			getFlyingCamera().setGlobalToFiducialTransform(defautcameraView);
			// setScene(s);
			rebuildingUIOnerror = false;
			getControlsBox(homeIcon,generateIcon,clearIcon);
		});
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

	public void setControls(CheckMenuItem showRuler, CheckMenuItem idlespin, CheckMenuItem autohighlight) {
		this.showRuler = showRuler;
		rebuild(true);
		this.spin = idlespin;
		this.autoHighilight = autohighlight;
		idlespin.setOnAction((event) -> {
			resetMouseTime();
			if (spin.isSelected()) {
				autospingThread = new Thread(() -> {
					while (spin.isSelected()) {
						BowlerStudio.runLater(new Runnable() {
							@Override
							public void run() {
								autoSpin();
							}
						});
						try {
							Thread.sleep(30);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					System.err.println("Autospin Thread clean exit "+name);
				});
				autospingThread.setName("UI Autospin Thread "+name);
				autospingThread.start();
			}
		});

		showRuler.setOnAction((event) -> {
			boolean selected = showRuler.isSelected();
			// System.out.println("CheckBox Action (selected: " + selected +
			// ")");
			if (selected)
				showAxis();
			else
				hideAxis();
		});

	}

	public Group getControlsBox(ImageView homeIcon, ImageView generateIcon, ImageView clearIcon) {
		this.homeIcon = homeIcon;
		this.generateIcon = generateIcon;
		this.clearIcon = clearIcon;
		HBox controls = new HBox(10);
		home = new Button("Home");
		home.setTooltip(new javafx.scene.control.Tooltip("Home the camera"));
		if(homeIcon!=null)
		home.setGraphic(homeIcon);
		home.setOnAction(event -> {
			focusOrentation(
					new TransformNR(0,0,0,new RotationNR(0,45,-45)),
					new TransformNR(),
					getFlyingCamera().getDefaultZoomDepth());
		});
		export = new Button("Export");
		if(generateIcon!=null)
		export.setGraphic(generateIcon);
		export.setOnAction(event -> {
			if (!getCsgMap().isEmpty()) {
				exportAll(false);
				BowlerStudio.runLater(() -> {
					export.setDisable(true);
				});
			} else {
				System.out.println("Nothing to export!");
			}
		});
		final Tooltip tooltip = new Tooltip();
		tooltip.setText("\nExport all of the parts on the screen\n" + "to manufacturing. STL and SVG\n");
		export.setTooltip(tooltip);
		Button clear = new Button("Clear");
		if(clearIcon!=null)
		clear.setGraphic(clearIcon);
		clear.setOnAction(event -> {
			clearUserNode();
			removeObjects();
		});

		javafx.scene.layout.VBox allCOntrols = new javafx.scene.layout.VBox();
		controlsChecks = new HBox(10);

		BowlerStudio.runLater(() -> controls.getChildren().addAll(home, export, clear));

		BowlerStudio.runLater(() -> allCOntrols.getChildren().addAll(controlsChecks, controls));

		return new Group(allCOntrols);
	}

	private void exportAll(boolean makePrintBed) {
		new Thread() {
			public void run() {
				setName("Exporting the CAD objects");
				ArrayList<CSG> csgs = new ArrayList<CSG>(getCsgMap().keySet());
				if (makePrintBed) {

				}
				System.out.println("Exporting " + csgs.size() + " parts");
				File baseDirForFiles = FileSelectionFactory.GetDirectory(getDefaultStlDir());
				try {
					ArrayList<File> files = new CadFileExporter(BowlerStudioController.getMobileBaseUI())
							.generateManufacturingParts(csgs, baseDirForFiles);
					for (File f : files) {
						System.out.println("Exported " + f.getAbsolutePath());

					}
					System.out.println("Success! " + files.size() + " parts exported");

				} catch (Exception e) {
					// TODO Auto-generated catch block
					BowlerStudio.printStackTrace(e);
				}

				BowlerStudio.runLater(() -> {
					export.setDisable(false);
				});
			}
		}.start();
	}

	public boolean isAutoHightlight() {
		if (autoHighilight != null)
			return autoHighilight.isSelected();
		return false;
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

		BowlerStudio.runLater(() -> controls.getChildren().addAll(new Label("Cad Debugger"), back, fwd));
		return new Group(controls);
	}

	/**
	 * Removes the objects.
	 */
	public void removeObjects() {
//		for(CSG previousCsg:getCsgMap().keySet())
//			for(Polygon poly:previousCsg.getPolygons())
//				sumVert-=(poly.vertices.size());
//		System.err.println("Total Verts = "+sumVert);

		lookGroup.getChildren().clear();
		getCsgMap().clear();
		csgSourceFile.clear();
		axisMap.clear();

	}

	/**
	 * Removes the object.
	 *
	 * @param previousCsg the previous
	 */
	public void removeObject(CSG previousCsg) {
//		for(Polygon poly:previousCsg.getPolygons())
//			sumVert-=(poly.vertices.size());
//		System.err.println("Total Verts = "+sumVert);

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
				// synchronized (currentObjectsToCheck) {
				array = (Object[]) currentObjectsToCheck.toArray();
				// }
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

				BowlerStudio.runLater(() -> {
					for (CSG add : toRemove)
						removeObject(add);
					BowlerStudio.runLater(() -> {
						for (CSG ret : toAdd)
							addObject(ret, source);
					});
				});

				System.out.println("Saving CSG database");
				CSGDatabase.saveDatabase();
			}
		}.start();
	}

	/**
	 * Adds the object.
	 *
	 * @param currentCsg the current
	 * @return the mesh view
	 */
	public MeshView addObject(CSG currentCsg, File source) {
		if (currentCsg == null)
			return new MeshView();
//		for(Polygon poly:currentCsg.getPolygons())
//			sumVert+=(poly.vertices.size());
//		System.err.println("Total Verts = "+sumVert);
		BowlerStudioModularFrame bowlerStudioModularFrame = BowlerStudioModularFrame.getBowlerStudioModularFrame();
		if (bowlerStudioModularFrame != null)
			bowlerStudioModularFrame.showCreatureLab();
		// System.out.println(" Adding a CSG from file: "+source.getName());
		if (getCsgMap().get(currentCsg) != null)
			return currentCsg.getMesh();
		getCsgMap().put(currentCsg, currentCsg.getMesh());
		BowlerStudio.runLater(() -> controlsChecks.getChildren().clear());
		Slider slider = AssemblySlider.getSlider(getCsgMap().keySet());
		BowlerStudio.runLater(() -> {
			controlsChecks.getChildren().addAll(slider);
		});
		csgSourceFile.put(currentCsg, source);
		Optional<Object> m = currentCsg.getStorage().getValue("manipulator");
		HashMap<javafx.event.EventType<MouseEvent>, EventHandler<MouseEvent>> eventForManipulation = null;
		try {
			if (HashMap.class.isInstance(m.get())) {
				eventForManipulation = (HashMap<javafx.event.EventType<MouseEvent>, EventHandler<MouseEvent>>) m.get();
			}
		} catch (java.util.NoSuchElementException ex) {
			eventForManipulation = null;
		}

		MeshView current = getCsgMap().get(currentCsg);
		current.setCullFace(CullFace.BACK);
		((PhongMaterial)current.getMaterial()).setSpecularColor(javafx.scene.paint.Color.WHITE);
		// TriangleMesh mesh =(TriangleMesh) current.getMesh();
		// mesh.vertexFormatProperty()
		ContextMenu cm = new ContextMenu();
		Menu infomenu = new Menu("Info...");
		infomenu.getItems().add(new MenuItem("Name= " + currentCsg.getName()));
		infomenu.getItems().add(new MenuItem("Mass = " + (currentCsg.getMassKG(0.001) * 1000) + " grams "));
		infomenu.getItems().add(new MenuItem("Total X= " + currentCsg.getTotalX()));
		infomenu.getItems().add(new MenuItem("Total Y= " + currentCsg.getTotalY()));
		infomenu.getItems().add(new MenuItem("Total Z= " + currentCsg.getTotalZ()));

		infomenu.getItems().add(new MenuItem("Maximums: "));
		infomenu.getItems().add(new MenuItem("Max X= " + currentCsg.getMaxX()));
		infomenu.getItems().add(new MenuItem("Max Y= " + currentCsg.getMaxY()));
		infomenu.getItems().add(new MenuItem("Max Z= " + currentCsg.getMaxZ()));

		infomenu.getItems().add(new MenuItem("Minums: "));
		infomenu.getItems().add(new MenuItem("Min X= " + currentCsg.getMinX()));
		infomenu.getItems().add(new MenuItem("Min Y= " + currentCsg.getMinY()));
		infomenu.getItems().add(new MenuItem("Min Z= " + currentCsg.getMinZ()));

		cm.getItems().add(infomenu);

		Set<String> params = currentCsg.getParameters();

		if (params != null) {
			Menu parameters = new Menu("Parameters...");
			parameters.setMnemonicParsing(false);
			for (String key : params) {
				Parameter param = CSGDatabase.get(key);
				currentCsg.setParameterIfNull(key);
				if (LengthParameter.class.isInstance(param)) {
					LengthParameter lp = (LengthParameter) param;

					EngineeringUnitsSliderWidget widget = new EngineeringUnitsSliderWidget(
							new IOnEngineeringUnitsChange() {

								@Override
								public void onSliderMoving(EngineeringUnitsSliderWidget s, double newAngleDegrees) {
									try {
										currentCsg.setParameterNewValue(key, newAngleDegrees);

									} catch (Exception ex) {
										BowlerStudioController.highlightException(source, ex);
									}

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
					// System.err.println("Adding Length Paramater " + lp.getName());
				} else {
					try {
						Parameter lp = (Parameter) param;
						if (lp != null) {
							Menu paramTypes = new Menu(lp.getName() + " " + lp.getStrValue());
							paramTypes.setMnemonicParsing(false);
							for (String opt : lp.getOptions()) {
								String myVal = opt;
								MenuItem customMenuItem = new MenuItem(myVal);
								customMenuItem.setMnemonicParsing(false);
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
							// System.err.println("Adding String Paramater " +
							// lp.getName());
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
			cm.getItems().add(parameters);
		}

		MenuItem exportObj = new MenuItem("Export OBJ...");
		exportObj.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				currentCsg.addExportFormat("obj");
				exportManufacturingPart(currentCsg, source);
			}

		});

		MenuItem exportDXF = new MenuItem("Export SVG...");
		exportDXF.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				currentCsg.addExportFormat("svg");
				exportManufacturingPart(currentCsg, source);
			}

		});
		cm.getItems().add(exportDXF);
		MenuItem blend = new MenuItem("Export Blender File...");
		blend.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				currentCsg.addExportFormat("blend");
				exportManufacturingPart(currentCsg, source);
			}
		});
		cm.getItems().add(blend);

		MenuItem export = new MenuItem("Export STL...");
		export.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				currentCsg.addExportFormat("stl");
				exportManufacturingPart(currentCsg, source);
			}
		});
		cm.getItems().add(export);
		cm.getItems().add(exportObj);

		MenuItem toWireframe = new MenuItem("To Wire Frame");
		toWireframe.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				resetMouseTime();
				if (current.getDrawMode() == DrawMode.FILL) {
					toWireframe.setText("To Solid Fill");
					current.setDrawMode(DrawMode.LINE);
				} else {
					current.setDrawMode(DrawMode.FILL);
					toWireframe.setText("To Wire Frame");
				}
			}
		});
		cm.getItems().add(toWireframe);

		MenuItem hide = new MenuItem("Hide Object");
		hide.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				resetMouseTime();
				removeObject(currentCsg);

			}
		});
		cm.getItems().add(hide);

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
				if (event.isSecondaryButtonDown())
					cm.show(current, event.getScreenX() - 10, event.getScreenY() - 10);
				else if (event.isPrimaryButtonDown()) {
					if (System.currentTimeMillis() - lastClickedTime < 500) {
						BowlerStudio.runLater(java.time.Duration.ofMillis(200), new Runnable() {
							@Override
							public void run() {
								setSelectedCsg(currentCsg);
							}
						});

					}
					lastClickedTime = System.currentTimeMillis();
				}

			}
		}
		closeTheMenueHandler cmh = new closeTheMenueHandler();
		if (!currentCsg.isWireFrame()) {
			BowlerStudio.runLater(() -> current.addEventHandler(MouseEvent.MOUSE_PRESSED, cmh));
			if (eventForManipulation != null) {
				HashMap<javafx.event.EventType<MouseEvent>, EventHandler<MouseEvent>> manip = eventForManipulation;
				for (javafx.event.EventType<MouseEvent> e : manip.keySet())
					BowlerStudio.runLater(() -> current.addEventHandler(e, manip.get(e)));
			}
		} else {
			current.setDrawMode(DrawMode.LINE);
			current.setPickOnBounds(false);
			current.setMouseTransparent(true);
		}
		// cm.getScene().addEventHandler(MouseEvent.MOUSE_EXITED, cmh);
		if (current == null)
			return new MeshView();
		if (!lookGroup.getChildren().contains(current)) {
			BowlerStudio.runLater(() -> {
				try {
					lookGroup.getChildren().add(current);
				} catch (Throwable e) {
					// duplicate
				}
			});
			if(showRuler!=null) {
				Axis axis = new Axis(showRuler.isSelected());
				BowlerStudio.runLater(() -> axis.getTransforms().add(currentCsg.getManipulator()));
				axisMap.put(current, axis);
				BowlerStudio.runLater(() -> lookGroup.getChildren().add(axis));
			}
		}

		// Log.warning("Adding new axis");
		return current;
	}

	public void exportManufacturingPart(CSG currentCsg, File source) {
		resetMouseTime();

		new Thread() {

			public void run() {
				try {

					setDefaultStlDir(new CadFileExporter(BowlerStudioController.getMobileBaseUI())
							.generateManufacturingParts(Arrays.asList(currentCsg),
									FileSelectionFactory.GetDirectory(getDefaultStlDir()))
							.get(0));
				} catch (Exception e1) {
					BowlerStudioController.highlightException(source, e1);
				}

			}
		}.start();
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

		WritableImage snapshot = new WritableImage(snWidth, (int) (realHeight * scale));

		getRoot().snapshot(snapshotParameters, snapshot);

		try {
			ImageIO.write(javafx.embed.swing.SwingFXUtils.fromFXImage(snapshot, null), "png", new File(fName));
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
		BowlerStudio.runLater(() -> getRoot().getChildren().add(world));
	}
	
	public void hideHand() {
		hand.getChildren().remove(handMesh);
	}
	public void showand() {
		hand.getChildren().add(handMesh);
	}
	/**
	 * Builds the camera.
	 */
	private void buildCamera(boolean addHand) {

		CSG cylinder = new Cylinder(0, // Radius at the top
				2.5, // Radius at the bottom
				10, // Height
				(int) 20 // resolution
		).toCSG().roty(90).setColor(Color.BLACK);

		handMesh = cylinder.getMesh();
		
		hand = new Group();
		if(addHand)
			showand();
		camera.setNearClip(.1);
		// camera.setFarClip(1000.0);//this is set in VirtualCameraMobileBase
		getSubScene().setCamera(camera);

		camera.setRotationAxis(Rotate.Z_AXIS);
		camera.setRotate(180);
		camera.setDepthTest(DepthTest.ENABLE);
		setVirtualcam(new VirtualCameraMobileBase(camera, hand,this, name));
		VirtualCameraFactory.setFactory(new IVirtualCameraFactory() {
			@Override
			public AbstractImageProvider getVirtualCamera() {

				throw new RuntimeException("No virtual camera availible!");
			}
		});

		// TODO reorent the start camera
		BowlerStudio.runLater(() -> {
			getFlyingCamera().setGlobalToFiducialTransform(defautcameraView);
		});

	}

	/**
	 * Gets the camera field of view property.
	 *
	 * @return the camera field of view property
	 */
	// public DoubleProperty getCameraFieldOfViewProperty() {
	// return camera.fieldOfViewProperty();
	// }

	/**
	 * Builds the axes.
	 * @param b 
	 */
	private void buildAxes(boolean b) {

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
					//Image ruler = AssetFactory.loadAsset("ruler.png");
					Image ruler = new Image(BowlerStudio.class.getResourceAsStream("ruler.png"));
					//Image groundLocal = AssetFactory.loadAsset("ground.png");


					Affine zRuler = new Affine();
					double scale = 0.2522;
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
					Affine downset = new Affine();
					downset.setTz(0.1);
					xp.setTx(-20 * scale);
					xp.appendScale(scale, scale, scale);
					xp.appendRotation(180, 0, 0, 0, 1, 0, 0);
					grid = createGridMesh(1000,1000,20);
					
					BowlerStudio.runLater(() -> {
						ImageView rulerImage = new ImageView(ruler);
						ImageView yrulerImage = new ImageView(ruler);
						ImageView zrulerImage = new ImageView(ruler);
						//ImageView groundView = new ImageView(groundLocal);
						//groundView.getTransforms().addAll(groundMove, downset);
						//groundView.setOpacity(0.3);
						zrulerImage.getTransforms().addAll(zRuler, downset);
						rulerImage.getTransforms().addAll(xp, downset);
						yrulerImage.getTransforms().addAll(yRuler, downset);
						ObservableList<Node> children = gridGroup.getChildren();
						children.addAll(zrulerImage, rulerImage, yrulerImage,getGrid());
						//children.addAll(grid);
						
						//children.addAll(groundView);

						Affine groundPlacment = new Affine();
						groundPlacment.setTz(-1);
						// ground.setOpacity(.5);
						ground = new Group();
						ground.getTransforms().add(groundPlacment);
						focusGroup.getChildren().add(getVirtualcam().getCameraFrame());

						boolean selected = showRuler != null ? showRuler.isSelected() : true;
						if(!b)
							selected=false;
						if(b) {
							children.addAll(new Axis(selected), ground);
							showAxis();
						}
						axisGroup.getChildren().addAll(focusGroup, userGroup);
						world.getChildren().addAll(lookGroup, axisGroup);
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}.start();

	}


	public MeshView getGrid() {
		return grid;
	}
	public MeshView createGridMesh(int width, int height, int cellSize) {
		Affine groundMove = new Affine();
		// groundMove.setTz(-3);
		groundMove.setTx(-width / 2);
		groundMove.setTy(-height/ 2);
		
		TriangleMesh mesh = new TriangleMesh();

		// Create points
		for (int y = 0; y <= height; y += cellSize) {
			for (int x = 0; x <= width; x += cellSize) {
				mesh.getPoints().addAll(x, y, 0);
			}
		}

		// Create lines (faces in TriangleMesh terms)
		int numXLines = (width / cellSize) + 1;
		int numYLines = (height / cellSize) + 1;

		// Horizontal lines
		for (int y = 0; y < numYLines; y++) {
			for (int x = 0; x < numXLines - 1; x++) {
				int p1 = y * numXLines + x;
				int p2 = y * numXLines + x + 1;
				mesh.getFaces().addAll(p1, 0, p2, 0, p1, 0);
			}
		}

		// Vertical lines
		for (int x = 0; x < numXLines; x++) {
			for (int y = 0; y < numYLines - 1; y++) {
				int p1 = y * numXLines + x;
				int p2 = (y + 1) * numXLines + x;
				mesh.getFaces().addAll(p1, 0, p2, 0, p1, 0);
			}
		}

		// Dummy texture coordinates (required by TriangleMesh)
		mesh.getTexCoords().addAll(0, 0);

		MeshView meshView = new MeshView(mesh);

		// Set material properties for thin lines
		PhongMaterial material = new PhongMaterial();
		material.setDiffuseColor(Color.LIGHTBLUE);
		meshView.setMaterial(material);

		// Make lines appear thin
		meshView.setDrawMode(DrawMode.LINE);

		// Ensure the mesh is visible
		meshView.setCullFace(CullFace.NONE);
		meshView.getTransforms().addAll(gridPlacementAffine,groundMove);
		return meshView;
	}
	public void addUserNode(Node n) {
		BowlerStudioModularFrame bowlerStudioModularFrame = BowlerStudioModularFrame.getBowlerStudioModularFrame();
		if (bowlerStudioModularFrame != null)
			bowlerStudioModularFrame.showCreatureLab();
		if (Platform.isFxApplicationThread())
			userGroup.getChildren().add(n);
		else
			BowlerStudio.runLater(() -> userGroup.getChildren().add(n));
	}

	public void removeUserNode(Node n) {
		BowlerStudio.runLater(() -> userGroup.getChildren().remove(n));
	}

	public void clearUserNode() {
		//new RuntimeException("Clearing all user nodes!");
		BowlerStudio.runLater(() -> userGroup.getChildren().clear());
	}

	public void showAxis() {
		BowlerStudio.runLater(() -> axisGroup.getChildren().add(gridGroup));
		for (MeshView a : axisMap.keySet()) {
			axisMap.get(a).show();
		}
	}

	public void hideAxis() {
		BowlerStudio.runLater(() -> axisGroup.getChildren().remove(gridGroup));
		for (MeshView a : axisMap.keySet()) {
			axisMap.get(a).hide();
		}
	}

	private void autoSpin() {
		try {
			long diff = System.currentTimeMillis() - getLastMosueMovementTime();
			if (spin != null)
				if (diff > timeForAutospin && spin.isSelected()) {
					// TODO start spinning
					double scale = 0.5;
					long finaSpeedScale = timeForAutospin + (timeForAutospin / 2);
					if (diff < finaSpeedScale) {
						double finaSpeedDiff = ((double) (finaSpeedScale - diff));
						double sineScale = (finaSpeedDiff / ((double) (timeForAutospin / 2)));
						scale = 1 - Math.sin(sineScale * (Math.PI / 2));
						moveCamera(new TransformNR(0, 0, 0, new RotationNR(0, 0.5 * scale, 0)));
					} else {
						moveCamera(autoSpinSpeed);
					}

				}
		} catch (Exception | Error e) {
			// e.printStackTrace();
		}

	}

	/**
	 * Handle mouse.
	 *
	 * @param scene the scene
	 */

	private void handleMouse(SubScene scene) {
		if(disabeControl) {
			System.out.println("No mouse control added "+name);
			scene.setPickOnBounds(false);
			return;
		}
		System.out.println("Settinng up Mouse Handelers "+name);
		scene.setOnMouseClicked(event -> {
			resetMouseTime();
			if (getControlsMap().timeToCancel(event))
				cancelSelection();
		});
		scene.addEventFilter(MouseEvent.MOUSE_PRESSED,new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				System.out.println("Bowler 3d start "+name);
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

				if (getControlsMap().isSlowMove(me)) {
					modifier = 0.1;
				}
				if (getControlsMap().isRotate(me)) {
					TransformNR trans = new TransformNR(0, 0, 0,
							new RotationNR(mouseDeltaY * modifierFactor * modifier * mouseScale,
									mouseDeltaX * modifierFactor * modifier *mouseScale, 0

							));
					moveCamera(trans);
				}

				if (getControlsMap().isMove(me) && move) {
					double depth = -100 / getVirtualcam().getZoomDepth();

					TransformNR newPose = new TransformNR(mouseDeltaX * modifierFactor * modifier *  (mouseScale/2) / depth,
							mouseDeltaY * modifierFactor * modifier * (mouseScale/2) / depth, 0, new RotationNR());
					moveCamera(newPose);
				}

			}
		});
		scene.addEventHandler(ScrollEvent.ANY, t -> {
			if (getControlsMap().isZoom(t)) {
				double deltaY = t.getDeltaY();
				zoomIncrement(deltaY);
			}
			t.consume();
		});

	}

	public void zoomIncrement(double deltaY) {
		double zoomFactor = -deltaY * getVirtualcam().getZoomDepth() / 500;
		//
		// double z = camera.getTranslateY();
		// double newZ = z + zoomFactor;
		// camera.setTranslateY(newZ);
		// System.out.println("Z = "+zoomFactor);

		getVirtualcam().setZoomDepth(getVirtualcam().getZoomDepth() + zoomFactor);
	}

	public void moveCamera(TransformNR newPose) {
		getFlyingCamera().DriveArc(newPose);
	}

	private void selectObjectsSourceFile(CSG source) {
		new Thread(() -> {
			// this code is thread safed
			BowlerStudioController.getBowlerStudio().clearHighlits();
			debuggerList.clear();
			debuggerIndex = 0;

			for (String ex : source.getCreationEventStackTraceList()) {
				// Thread safed
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
				// this code is thread safed
				BowlerStudioController.getBowlerStudio().setHighlight(locateFile(fileName, source), linNum,
						java.awt.Color.PINK);

			}
			debuggerIndex = debuggerList.size() - 1;
		}).start();
		// BowlerStudio.runLater(()->{
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
		System.out.println("Setting UI scene");
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

	public VirtualCameraMobileBase getVirtualcam() {
		return flyingCamera;
	}

	public void setVirtualcam(VirtualCameraMobileBase virtualcam) {
		this.flyingCamera = virtualcam;
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

			BowlerStudio.runLater(() -> getCsgMap().get(key).setMaterial(new PhongMaterial(key.getColor())));
		}

		this.selectedCsg = null;
		// new Exception().printStackTrace();
		focusToAffine(new TransformNR(), new Affine());
		resetMouseTime();
	}

	/**
	 * Select a provided affine that is in a given global pose
	 * 
	 * @param startingLocation the starting pose
	 * @param rootListener     what affine to attach to
	 */
	public void setSelected(TransformNR startingLocation, Affine rootListener) {
		focusToAffine(startingLocation, rootListener);
	}

	/**
	 * Select a provided affine that is in a given global pose
	 * 
	 * @param startingLocation the starting pose
	 * @param rootListener     what affine to attach to
	 */
	public void setSelected(Affine rootListener) {
		focusToAffine(new TransformNR(), rootListener);
	}

	public void setSelectedCsg(List<CSG> selectedCsg) {
		// System.err.println("Selecting group");
		setSelectedCsg(selectedCsg.get(selectedCsg.size() - 1));
		try {

			for (int in = 0; in < selectedCsg.size() - 1; in++) {
				int i = in;
				MeshView mesh = getCsgMap().get(selectedCsg.get(i));
				if (mesh != null)
					BowlerStudio.runLater(() -> {
						try {
							mesh.setMaterial(new PhongMaterial(Color.GOLD));
						} catch (Exception ex) {
						}
					});
			}
		} catch (java.lang.NullPointerException ex0) {
		} // if a selection is called before the limb is loaded
		resetMouseTime();
	}

	public void setSelectedCsg(CSG scg) {
		setSelectedCsg(scg, false);
	}

	public void setSelectedCsg(CSG scg, boolean justHighlight) {
		if (scg == this.selectedCsg)
			return;
		if (focusing)
			return;
		if (scg == null)
			return;
		this.selectedCsg = scg;

		for (CSG key : getCsgMap().keySet()) {

			BowlerStudio.runLater(() -> {
				try {
					getCsgMap().get(key).setMaterial(new PhongMaterial(key.getColor()));
				} catch (Throwable ex) {
				}
			});
		}
		lastSelectedTime = System.currentTimeMillis();

		BowlerStudio.runLater(() -> {
			try {
				getCsgMap().get(selectedCsg).setMaterial(new PhongMaterial(Color.GOLD));
			} catch (Exception e) {
			}
		});
		if (!justHighlight) {
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

			Affine manipulator2 = selectedCsg.getManipulator();

			focusToAffine(poseToMove, manipulator2);
		}
		resetMouseTime();
	}
	public void focusTo(TransformNR poseToMove) {
		focusToAffine(poseToMove,new Affine());
	}
	public void focusToAffine(TransformNR poseToMove, Affine manipulator2) {
		if (focusing)
			return;
		if (manipulator2 == null) {
			new RuntimeException("Can not focus on null affine").printStackTrace();
			return;
		}
		focusing = true;
		BowlerStudio.runLater(() -> {
			Affine centering = TransformFactory.nrToAffine(poseToMove);
			// this section keeps the camera orented the same way to avoid whipping
			// around

			TransformNR rotationOnlyCOmponentOfManipulator = TransformFactory.affineToNr(manipulator2);
			rotationOnlyCOmponentOfManipulator.setX(0);
			rotationOnlyCOmponentOfManipulator.setY(0);
			rotationOnlyCOmponentOfManipulator.setZ(0);
			TransformNR reverseRotation = rotationOnlyCOmponentOfManipulator.inverse();
			TransformNR startSelectNr = perviousTarget.copy();
			TransformNR targetNR;// =
									// TransformFactory.affineToNr(selectedCsg.getManipulator());
			if (Math.abs(manipulator2.getTx()) > 0.1 || Math.abs(manipulator2.getTy()) > 0.1
					|| Math.abs(manipulator2.getTz()) > 0.1) {
				targetNR = TransformFactory.affineToNr(manipulator2);
			} else {
				targetNR = TransformFactory.affineToNr(centering);
			}
			this.poseToMove = targetNR;
			Affine interpolator = new Affine();
			Affine correction = TransformFactory.nrToAffine(reverseRotation);
			interpolator.setTx(startSelectNr.getX() - targetNR.getX());
			interpolator.setTy(startSelectNr.getY() - targetNR.getY());
			interpolator.setTz(startSelectNr.getZ() - targetNR.getZ());
			removeAllFocusTransforms();
			focusGroup.getTransforms().add(interpolator);
			try {
				if (Math.abs(manipulator2.getTx()) > 0.1 || Math.abs(manipulator2.getTy()) > 0.1
						|| Math.abs(manipulator2.getTz()) > 0.1) {
					// BowlerStudio.runLater(() -> {
					focusGroup.getTransforms().add(manipulator2);
					focusGroup.getTransforms().add(correction);
					// });

				} else
					// BowlerStudio.runLater(() -> {
					focusGroup.getTransforms().add(centering);
				// });
			} catch (Exception ex) {

				ex.printStackTrace();
			}
			focusInterpolate(startSelectNr, targetNR,(int) numberOfInterpolationSteps, interpolator);
		});
	}

	public void targetAndFollow(TransformNR poseToMove, Affine manipulator2) {
		this.poseToMove = poseToMove;
		if (focusing)
			return;
		if (manipulator2 == null) {
			new RuntimeException("Can not focus on null affine").printStackTrace();
			return;
		}
		focusing = true;
		BowlerStudio.runLater(() -> {
			Affine referenceFrame = TransformFactory.nrToAffine(poseToMove);
			// this section keeps the camera orented the same way to avoid whipping
			// around

			TransformNR rotationOnlyCOmponentOfManipulator2 = poseToMove.copy();
			rotationOnlyCOmponentOfManipulator2.setX(0);
			rotationOnlyCOmponentOfManipulator2.setY(0);
			rotationOnlyCOmponentOfManipulator2.setZ(0);
			TransformNR reverseRotation2 = rotationOnlyCOmponentOfManipulator2.inverse();
			Affine correction2 = TransformFactory.nrToAffine(reverseRotation2);

			TransformNR rotationOnlyCOmponentOfManipulator = TransformFactory.affineToNr(manipulator2);
			rotationOnlyCOmponentOfManipulator.setX(0);
			rotationOnlyCOmponentOfManipulator.setY(0);
			rotationOnlyCOmponentOfManipulator.setZ(0);
			TransformNR reverseRotation = rotationOnlyCOmponentOfManipulator.inverse();
			Affine correction = TransformFactory.nrToAffine(reverseRotation);

			TransformNR startSelectNr = perviousTarget.copy();
			// =
			// TransformFactory.affineToNr(selectedCsg.getManipulat/or());

			targetNR = poseToMove.times(TransformFactory.affineToNr(manipulator2));

			Affine interpolator = new Affine();
			interpolator.setTx(startSelectNr.getX() - targetNR.getX());
			interpolator.setTy(startSelectNr.getY() - targetNR.getY());
			interpolator.setTz(startSelectNr.getZ() - targetNR.getZ());
			removeAllFocusTransforms();
			focusGroup.getTransforms().add(interpolator);
			focusGroup.getTransforms().add(referenceFrame);
			try {
				focusGroup.getTransforms().add(manipulator2);
				focusGroup.getTransforms().add(correction);
				focusGroup.getTransforms().add(correction2);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			focusInterpolate(startSelectNr, targetNR, (int)numberOfInterpolationSteps, interpolator);
		});
	}

	private void resetMouseTime() {
		// System.err.println("Resetting mouse");
		this.lastMosueMovementTime = System.currentTimeMillis();

	}
	public void focusOrentation(TransformNR orent) {
		focusOrentation(orent,null,getFlyingCamera().getDefaultZoomDepth());
	}
	public void focusOrentation(TransformNR orent, TransformNR trans, double zoom) {
		if (focusing)
			return;
		focusing = true;
		new Thread(() -> {
			runSyncFocus(orent,trans,zoom);
		}).start();
	}

	double bound(double in) {
		while (in > 180)
			in -= 360;
		while (in < -180)
			in += 360;
		return in;
	}

	private void runSyncFocus(TransformNR orent,TransformNR trans, double zoom) {
		double az = orent==null?0:bound(
				getFlyingCamera().getPanAngle() - 90 + Math.toDegrees(orent.getRotation().getRotationAzimuth()));
		double el = orent==null?0:bound(
				getFlyingCamera().getTiltAngle() + 90 + Math.toDegrees(orent.getRotation().getRotationElevation()));
		//System.out.println("Focus from\n\taz:" + az + " \n\tel:" + el);
		double x=0;
		double y=0;
		double z=0;
		double zoomDelta = zoom -getFlyingCamera().getZoomDepth();
		
		if(trans!=null) {
			x=trans.getX()-getFlyingCamera().getGlobalX();
			y=trans.getY()-getFlyingCamera().getGlobalY();
			z=trans.getZ()-getFlyingCamera().getGlobalZ();
		}
		try {
			double d = 1.0 / numberOfInterpolationSteps;
			for (double i = 0; i < 1; i += d) {
				if(i>1)
					i=1;
//				double aztmp = getFlyingCamera().getPanAngle();
//				double eltmp = getFlyingCamera().getTiltAngle();
				//System.out.println("\tFocus to \n\t\taz:" + aztmp + " \n\t\tel:" + eltmp);
				double mx=x/ numberOfInterpolationSteps;
				double my=y/ numberOfInterpolationSteps;
				double mz=z/ numberOfInterpolationSteps;
				BowlerStudio.runLater(() -> {
					moveCamera(new TransformNR(0, 0, 0, new RotationNR(-el / numberOfInterpolationSteps, -az / numberOfInterpolationSteps, 0)));
					getFlyingCamera().DrivePositionAbsolute(mx, my, mz);
					if(!getFlyingCamera().isZoomLocked())
						getFlyingCamera().setZoomDepth(getFlyingCamera().getZoomDepth()+(zoomDelta/numberOfInterpolationSteps));
				});
				try {
					Thread.sleep(36);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					focusing = false;
				}
			}
			BowlerStudio.runLater(() ->{
				getFlyingCamera().SetOrentation(orent);
				getFlyingCamera().SetPosition(trans);
			});
			
		} catch (Throwable t) {
			t.printStackTrace();
		}
		focusing = false;
		
			//focusTo(trans);
	}

	private void focusInterpolate(TransformNR start, TransformNR target, int targetDepth, Affine interpolator) {

		new Thread(() -> {
			int depth = 0;
			while (focusing) {
				try {
					Thread.sleep(16);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					focusing = false;
				}
				double depthScale = 1 - (double) depth / (double) targetDepth;
				double sinunsoidalScale = Math.sin(depthScale * (Math.PI / 2));

				// double xIncrement =target.getX()- ((start.getX() - target.getX()) *
				// depthScale) + start.getX();
				double difference = start.getX() - target.getX();
				double scaledDifference = (difference * sinunsoidalScale);

				double xIncrement = scaledDifference;
				double yIncrement = ((start.getY() - target.getY()) * sinunsoidalScale);
				double zIncrement = ((start.getZ() - target.getZ()) * sinunsoidalScale);

				BowlerStudio.runLater(() -> {
					interpolator.setTx(xIncrement);
					interpolator.setTy(yIncrement);
					interpolator.setTz(zIncrement);
				});
				// System.err.println("Interpolation step " + depth + " x " + xIncrement
				// + " y " + yIncrement + " z " + zIncrement);
				if (depth >= targetDepth) {
					// System.err.println("Camera intrpolation done");
					BowlerStudio.runLater(() -> {
						focusGroup.getTransforms().remove(interpolator);
					});
					perviousTarget = target.copy();
					perviousTarget.setRotation(new RotationNR());
					focusing = false;
				}

				depth++;
			}
		}).start();

	}

	private void removeAllFocusTransforms() {
		focusGroup.getTransforms().clear();
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

			setSelectedCsg(objsFromScriptLine);
		}
	}

	public long getLastMosueMovementTime() {
		return lastMosueMovementTime;
	}

	/**
	 * @return the defaultStlDir
	 */
	public File getDefaultStlDir() {
		if (defaultStlDir == null)
			defaultStlDir = new File(System.getProperty("user.home") + "/bowler-workspace/STL/");
		if (!defaultStlDir.exists()) {
			defaultStlDir.mkdirs();
		}

		return defaultStlDir;
	}

	/**
	 * @param defaultStlDir the defaultStlDir to set
	 */
	public void setDefaultStlDir(File defaultStlDir) {
		this.defaultStlDir = defaultStlDir;
	}

	public void focusToAffine(Affine af) {
		focusToAffine(new TransformNR(), af);
	}

	public TransformNR getTargetNR() {
		// TODO Auto-generated method stub
		return poseToMove;
	}

	/**
	 * The main() method is ignored in correctly deployed JavaFX application. main()
	 * serves only as fallback in case the application can not be launched through
	 * deployment artifacts, e.g., in IDEs with limited FX support. NetBeans ignores
	 * main().
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		JavaFXInitializer.go();
		System.setProperty("prism.dirtyopts", "false");

		AnchorPane view3d = new AnchorPane();
		BowlerStudio3dEngine engine = new BowlerStudio3dEngine("Test");
		engine.rebuild(true);
		SubScene subScene = engine.getSubScene();
		view3d.getChildren().add(subScene);

		subScene.setFocusTraversable(false);
		subScene.widthProperty().bind(view3d.widthProperty());
		subScene.heightProperty().bind(view3d.heightProperty());

		AnchorPane.setTopAnchor(subScene, 0.0);
		AnchorPane.setRightAnchor(subScene, 0.0);
		AnchorPane.setLeftAnchor(subScene, 0.0);
		AnchorPane.setBottomAnchor(subScene, 0.0);
		BowlerKernel.runLater(() -> {
			Stage newStage = new Stage();
			Scene scene = new Scene(view3d, 1024, 960, true);
			newStage.setScene(scene);
			scene.getRoot().setStyle("-fx-font-family: 'Arial';");
			scene.getRoot().applyCss();
			scene.getRoot().layout();
			// Add a close request handler
			newStage.setOnCloseRequest(event -> {
				// Exit the JVM when the window is closed
				System.exit(0);
			});
			newStage.show();
		});
	}

	public IControlsMap getControlsMap() {
		return map;
	}

	public void setControlsMap(IControlsMap map) {
		this.map = map;
	}

	public void setZoom(int i) {
		flyingCamera.setZoomDepth(i);
	}

	public void setMouseScale(double  mouseScale) {
		this.mouseScale = mouseScale;
	}

	public void lockZoom() {
		getFlyingCamera().lockZoom();
	}

	@Override
	public void onChange(VirtualCameraMobileBase camera) {
		for(ICameraChangeListener c:listeners) {
			try {
				c.onChange(camera);
			}catch(Throwable t) {
				t.printStackTrace();
			}
		}
	}
	public void lockMove() {
		move = false;
		getFlyingCamera().lockMove();
	}
	public void disableControls() {
		// TODO Auto-generated method stub
		disabeControl=true;
	}
	public void placeGrid(TransformNR workplane) {
		BowlerKernel.runLater(()->{
			TransformFactory.nrToAffine(workplane, gridPlacementAffine);
		});
	}
}