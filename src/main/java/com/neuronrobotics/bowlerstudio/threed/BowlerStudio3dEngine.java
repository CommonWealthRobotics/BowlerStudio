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
import com.neuronrobotics.bowlerstudio.creature.IMobileBaseUI;
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
import eu.mihosoft.vrl.v3d.MissingManipulatorException;
import eu.mihosoft.vrl.v3d.parametrics.CSGDatabase;
import eu.mihosoft.vrl.v3d.parametrics.CSGDatabaseInstance;
import eu.mihosoft.vrl.v3d.parametrics.IParameterChanged;
import eu.mihosoft.vrl.v3d.parametrics.LengthParameter;
import eu.mihosoft.vrl.v3d.parametrics.Parameter;
import javafx.application.Platform;
import javafx.collections.ObservableList;
//import javafx.embed.swing.JFXPanel;
//import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.BlendMode;
import javafx.scene.AmbientLight;
import javafx.scene.DirectionalLight;
import javafx.scene.PointLight;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.transform.Translate;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.stage.Stage;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;

// Development, for objectDistance methode
//import com.sun.javafx.geom.PickRay;
//import com.sun.javafx.geom.Vec3d;
//import com.sun.javafx.scene.NodeHelper;
//import com.sun.javafx.scene.input.PickResultChooser;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.time.Duration;
import java.util.*;

/**
 * MoleculeSampleApp.
 */
public class BowlerStudio3dEngine implements ICameraChangeListener, IMobileBaseUI {
	private volatile boolean focusing = false;
	private volatile boolean abortFocus = false;
	private int NUMBER_OF_INTERPOLATION_STEPS = 30;

	/**
	 * 
	 */
	private static final long serialVersionUID = 6744581340628622682L;

	private static final TransformNR autoSpinSpeed = new TransformNR(0, 0, 0, new RotationNR(0, 0.25, 0));

	/** The root group. */
	private final Group rootGroup = new Group();

	/** The workplane group. */
	private Group workplaneGroup;

	/** The axis group. */
	final Group axisGroup = new Group();

	/** The grid group. */
	final Group gridGroup = new Group();

	/** The XYZ-ruler group. */
	private final Group rulerGroup = new Group();
	private Affine rulerOffset = new Affine();
	private Affine rulerInWorkplaneOffset = new Affine();

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
	private boolean aboveSplit = false;

	/** The mouse old y. */
	double mouseOldY;

	/** The mouse delta x. */
	double mouseDeltaX;

	/** The mouse delta y. */
	double mouseDeltaY;

	/** The manipulator group. */
	private final Group manipulatorGroup = new Group();

	/** The look group. */
	private final Group lookGroup = new Group();

	/** The camera group. */
	private final Group cameraGroup = new Group();

	/** The user group for the user defined objects and the navigation cube */
	private final Group userGroup = new Group();

	/** The scene. */
	private SubScene scene;

	/** The ground group. */
	private Group groundGroup;

	private Group group;

	private boolean captureMouse = false;
	private Button export;

	private VirtualCameraMobileBase flyingCamera;
	private Group handGroup;
	private double upDown = 0;
	private double leftRight = 0;
	private HashMap<CSG, MeshView> csgMap = new HashMap<>();
	private HashMap<CSG, File> csgSourceFile = new HashMap<>();
	private HashMap<MeshView, Axis> axisMap = new HashMap<>();
	private String lastFileSelected = "";
	private int lastFileLine = 0;
	private File defaultStlDir;
	private TransformNR defaultCameraView = new TransformNR(0, 0, 0, new RotationNR(90 - 127, 24, 0));
	// private static final TransformNR offsetForVisualization = new
	// TransformNR(0, 0, 0, new RotationNR(0,0, 0));

	private Button back;
	private Button fwd;
	private Button home;
	private int debuggerIndex = 0;
	private ArrayList<String> debuggerList = new ArrayList<>();
	private CSG selectedCsg = null;

	private long lastMosueMovementTime = System.currentTimeMillis();

	// private List<CSG> selectedSet = null;
	private TransformNR previousTarget = new TransformNR();

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
	private Group controlHandleGroup = new Group();
	private AmbientLight ambientLight = new AmbientLight(Color.color(1.0, 1.0, 1.0, 0));
	private volatile boolean waitingForCompletion;

	public BowlerStudio3dEngine addListener(ICameraChangeListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
		return this;
	}

	public BowlerStudio3dEngine removeListener(ICameraChangeListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
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

					com.neuronrobotics.sdk.common.Log.debug("Cancel event detected");
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

			return (me.isPrimaryButtonDown() && primaryButtonDown && !shiftDown);
		}

		public boolean isMove(MouseEvent me) {
			boolean shiftDown = me.isShiftDown();
			boolean primaryButtonDown = me.isPrimaryButtonDown();
			boolean secondaryButtonDown = me.isSecondaryButtonDown();
			return (secondaryButtonDown || (primaryButtonDown && shiftDown));
		}

		public boolean isZoom(javafx.scene.input.ScrollEvent t) {
			return ScrollEvent.SCROLL == t.getEventType();
		}

	};
	private double mouseScale = 2.0;
	private MeshView handMesh;
	private ImageView homeIcon;
	private ImageView generateIcon;
	private ImageView clearIcon;
	private boolean move = true;
	private boolean disabeControl = false;
	private String name;

	/**
	 * Instantiates a new jfx3d manager.
	 * 
	 * @param string
	 */
	public BowlerStudio3dEngine(String name) {
		this.name = name;
		BowlerStudio.runLater(() -> {
			Thread.currentThread().setUncaughtExceptionHandler(new IssueReportingExceptionHandler());
		});
		com.neuronrobotics.sdk.common.Log.debug("Setting Scene " + name);
		setSubScene(new SubScene(getRoot(), 10, 10, true, SceneAntialiasing.BALANCED));

	// Show JavaFX diagnostics info
	ModuleLayer.boot().modules().stream().filter(m -> m.getName().startsWith("javafx"))
		.forEach(m -> System.out.println(m.getName() + " : " + m.getDescriptor().version()));
	}

	public void rebuild(boolean b) {
		rebuildingUIOnerror = true;

		com.neuronrobotics.sdk.common.Log.info("Rebuilding scene " + name);
		buildScene();

		com.neuronrobotics.sdk.common.Log.info("Rebuilding camera " + name);
		buildCamera(b);

		com.neuronrobotics.sdk.common.Log.info("Rebuilding axis " + name);
		buildAxes(b);

//		Stop[] stops = null;
//		com.neuronrobotics.sdk.common.Log.info("Rebuilding gradient " + name);
//		getSubScene().setFill(new LinearGradient(125, 0, 225, 0, false, CycleMethod.NO_CYCLE, stops));
		
		group = new Group(getSubScene());
		Scene s = new Scene(group);
		// handleKeyboard(s);
		handleMouse(getSubScene());

		BowlerStudio.runLater(() -> {
			getFlyingCamera().setGlobalToFiducialTransform(defaultCameraView);
			// setScene(s);
			rebuildingUIOnerror = false;
			getControlsBox(homeIcon, generateIcon, clearIcon);
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
							// Auto-generated catch block
							com.neuronrobotics.sdk.common.Log.error(e);
						}
					}
					com.neuronrobotics.sdk.common.Log.debug("Autospin Thread clean exit " + name);
				});
				autospingThread.setName("UI Autospin Thread " + name);
				autospingThread.start();
			}
		});

		showRuler.setOnAction((event) -> {
			boolean selected = showRuler.isSelected();
			// com.neuronrobotics.sdk.common.Log.error("CheckBox Action (selected: " +
			// selected +
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

		if (homeIcon != null)
			home.setGraphic(homeIcon);

		home.setOnAction(event -> {
			focusOrientation(new TransformNR(0, 0, 0, new RotationNR(0, 45, -45)), new TransformNR(),
					getFlyingCamera().getDefaultZoomDepth());
		});

		export = new Button("Export");

		if (generateIcon != null)
			export.setGraphic(generateIcon);

		export.setOnAction(event -> {
			if (!getCsgMap().isEmpty()) {
				exportAll(false);
				BowlerStudio.runLater(() -> {
					export.setDisable(true);
				});
			} else {
				com.neuronrobotics.sdk.common.Log.debug("Nothing to export!");
			}
		});

		final Tooltip tooltip = new Tooltip();
		tooltip.setText("\nExport all of the parts on the screen\n" + "to manufacturing. STL and SVG\n");
		export.setTooltip(tooltip);
		Button clear = new Button("Clear");
		if (clearIcon != null)
			clear.setGraphic(clearIcon);

		clear.setOnAction(event -> {
			clearUserNode();
			removeObjects();
		});

		javafx.scene.layout.VBox allControls = new javafx.scene.layout.VBox();
		controlsChecks = new HBox(10);

		BowlerStudio.runLater(() -> controls.getChildren().addAll(home, export, clear));

		BowlerStudio.runLater(() -> allControls.getChildren().addAll(controlsChecks, controls));

		return new Group(allControls);
	}

	private void exportAll(boolean makePrintBed) {
		new Thread() {
			public void run() {
				setName("Exporting the CAD objects");
				ArrayList<CSG> csgs = new ArrayList<CSG>(getCsgMap().keySet());
				if (makePrintBed) {

				}
				com.neuronrobotics.sdk.common.Log.debug("Exporting " + csgs.size() + " parts");
				File baseDirForFiles = FileSelectionFactory.GetDirectory(getDefaultStlDir());
				try {
					ArrayList<File> files = new CadFileExporter(BowlerStudioController.getMobileBaseUI())
							.generateManufacturingParts(csgs, baseDirForFiles);
					for (File f : files) {
						com.neuronrobotics.sdk.common.Log.debug("Exported " + f.getAbsolutePath());

					}
					com.neuronrobotics.sdk.common.Log.debug("Success! " + files.size() + " parts exported");

				} catch (Exception e) {
					// Auto-generated catch block
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
//		for (CSG previousCsg:getCsgMap().keySet())
//			for (Polygon poly:previousCsg.getPolygons())
//				sumVert-=(poly.vertices.size());
//		com.neuronrobotics.sdk.common.Log.error("Total Verts = "+sumVert);

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
//		for (Polygon poly:previousCsg.getPolygons())
//			sumVert-=(poly.vertices.size());
//		com.neuronrobotics.sdk.common.Log.error("Total Verts = "+sumVert);

		// com.neuronrobotics.sdk.common.Log.error(" Removing a CSG from file:
		// "+previousCsg+" from
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
					com.neuronrobotics.sdk.common.Log
							.debug("Testing for Regenerating " + i + " of " + currentObjectsToCheck.size());
					try {
						CSG tester = (CSG) array[i];
						for (String p : tester.getParameters(CSGDatabase.getInstance())) {
							if (p.contentEquals(key) && !toRemove.contains(tester)) {
								com.neuronrobotics.sdk.common.Log.debug("Regenerating " + i + " on key " + p);
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

				com.neuronrobotics.sdk.common.Log.debug("Saving CSG database");
				try {
					CSGDatabase.getInstance().saveDatabase();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
	}

	/**
	 * Adds the object.
	 *
	 * @param currentCsg the current
	 * @return the mesh view
	 */
	@Deprecated
	public MeshView addObject(CSG currentCsg, File source) {
		
		return addObject(currentCsg, source, currentCsg.getColor().getOpacity(), CSGDatabase.getInstance());
	}

	public MeshView addObject(CSG currentCsg, File source, double opacity, CSGDatabaseInstance instance) {
		if (currentCsg == null)
			return new MeshView();
//		for (Polygon poly:currentCsg.getPolygons())
//			sumVert+=(poly.vertices.size());
//		com.neuronrobotics.sdk.common.Log.error("Total Verts = "+sumVert);
		BowlerStudioModularFrame bowlerStudioModularFrame = BowlerStudioModularFrame.getBowlerStudioModularFrame();
		if (bowlerStudioModularFrame != null)
			bowlerStudioModularFrame.showCreatureLab();
		// com.neuronrobotics.sdk.common.Log.error(" Adding a CSG from file:
		// "+source.getName());
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
		if (opacity > 0) {
			PhongMaterial phongMaterial = (PhongMaterial) current.getMaterial();
			Color diffuseColor = phongMaterial.getDiffuseColor();
			diffuseColor = Color.color(diffuseColor.getRed(), diffuseColor.getGreen(), diffuseColor.getBlue(), opacity);
			phongMaterial.setDiffuseColor(diffuseColor);
		}
		// current.setCullFace(CullFace.BACK);// backs are tranparent
		current.setCullFace(CullFace.NONE);// backs are black
		((PhongMaterial) current.getMaterial()).setSpecularColor(javafx.scene.paint.Color.WHITE);
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

		Set<String> params = currentCsg.getParameters(instance);

		if (params != null) {
			Menu parameters = new Menu("Parameters...");
			parameters.setMnemonicParsing(false);
			for (String key : params) {
				Parameter param = instance.get(key);
				currentCsg.setParameterIfNull(instance, key);
				if (LengthParameter.class.isInstance(param)) {

					LengthParameter lp = (LengthParameter) param;

					String string = null;
					String string2 = null;
					if (lp.getOptions().size() > 1)
						try {
							string = lp.getOptions().get(1).toString();
							string2 = lp.getOptions().get(0).toString();
						} catch (Exception ex) {
							// some parameters from cadoodle do not work here...
							com.neuronrobotics.sdk.common.Log.error(ex);
							;
						}
					else {
						string = lp.getMM() + "";
						string2 = lp.getMM() + "";
					}
					try {
						EngineeringUnitsSliderWidget widget = new EngineeringUnitsSliderWidget(
								new IOnEngineeringUnitsChange() {

									@Override
									public void onSliderMoving(EngineeringUnitsSliderWidget s, double newAngleDegrees) {
										try {
											currentCsg.setParameterNewValue(instance, key, newAngleDegrees);

										} catch (Exception ex) {
											BowlerStudioController.highlightException(source, ex);
										}

									}

									@Override
									public void onSliderDoneMoving(EngineeringUnitsSliderWidget s,
											double newAngleDegrees) {
										// Get the set of objects to check for
										// regeneration after the initioal
										// regeneration cycle.
										Set<CSG> objects = getCsgMap().keySet();
										cm.hide();// hide this menu because the new
													// CSG talks to the new menu

										fireRegenerate(key, source, objects);
										resetMouseTime();

									}
								}, Double.parseDouble(string), Double.parseDouble(string2), lp.getMM(), 400, key);
						CustomMenuItem customMenuItem = new CustomMenuItem(widget);
						customMenuItem.setHideOnClick(false);
						parameters.getItems().add(customMenuItem);
					} catch (Exception ex) {
						com.neuronrobotics.sdk.common.Log.error(ex);
						;
					}
					// com.neuronrobotics.sdk.common.Log.error("Adding Length Paramater " +
					// lp.getName());
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
									com.neuronrobotics.sdk.common.Log
											.debug("Updating " + lp.getName() + " to " + myVal);
									lp.setStrValue(myVal);
									instance.get(lp.getName()).setStrValue(myVal);
									for (IParameterChanged l : instance.getParamListeners(lp.getName())) {
										l.parameterChanged(lp.getName(), lp);
									}

									// Get the set of objects to check for
									// regeneration after the initioal
									// regeneration cycle.
									Set<CSG> objects = getCsgMap().keySet();
									cm.hide();// hide this menu because the new
												// CSG talks to the new menu
									fireRegenerate(key, source, objects);
								});
								paramTypes.getItems().add(customMenuItem);
							}

							parameters.getItems().add(paramTypes);
							// com.neuronrobotics.sdk.common.Log.error("Adding String Paramater " +
							// lp.getName());
						}
					} catch (Exception ex) {
						com.neuronrobotics.sdk.common.Log.error(ex);
						;
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
					com.neuronrobotics.sdk.common.Log.error("consuming right release button in cm filter");
					event.consume();
				}
			}
		});

		class closeTheMenuHandler implements EventHandler<MouseEvent> {
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
		closeTheMenuHandler cmh = new closeTheMenuHandler();
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

			if (showRuler != null) {
				Axis axes = new Axis(showRuler.isSelected());
				if (currentCsg.hasManipulator())

					BowlerStudio.runLater(() -> {
						try {
							axes.getTransforms().add(currentCsg.getManipulator());
						} catch (MissingManipulatorException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					});

				axisMap.put(current, axes);
				BowlerStudio.runLater(() -> lookGroup.getChildren().add(axes));
			}
		}

		// Log.warning("Adding new axes");
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
			com.neuronrobotics.sdk.common.Log.error(ex);
		}
	}

	private static int webColorToArgb(Color color) {
		return (int) (color.getOpacity() * 255) << 24 |
			   (int) (color.getRed()	 * 255) << 16 |
			   (int) (color.getGreen()	 * 255) <<  8 |
			   (int) (color.getBlue()	 * 255);
	}

	private static Color argbToColor(int argb) {
	return Color.color(((argb >> 16) & 0xFF) / 255.0,
					   ((argb >>  8) & 0xFF) / 255.0,
					   ( argb		& 0xFF) / 255.0,
					   ((argb >> 24) & 0xFF) / 255.0);
	}

	// Create textured work-plane based on tiles of custom size
	public Group createTexturedWorkplane(double xSizeMM, double ySizeMM) {

		// Build square textured tile in MM
		final float TILE_SIZE_MM	   = 10.0f;
		final int TILE_BIG_GRID_PX   = 200;
		final int TILE_SMALL_GRID_PX =  20;

		// Build square textured tile in inches
		//final float TILE_SIZE_MM	   = 25.4f;
		//final int TILE_BIG_GRID_PX   = 200;
		//final int TILE_SMALL_GRID_PX =  20; // 1/10th inch

		// Build square textured tile in inches
		//final float TILE_SIZE_MM	   = 25.4f;
		//final int TILE_BIG_GRID_PX   = 256;
		//final int TILE_SMALL_GRID_PX =  16; // 1/16th inch
		
		// Build square textured tile in half inche
		//final float TILE_SIZE_MM	   = 12.7f;
		//final int TILE_BIG_GRID_PX   = 254;
		//final int TILE_SMALL_GRID_PX = 127;

		// Upscale work plane texture
		final int wpUpscale = 4;
		
		// Work plane noise in percentage [0-100%]
		int wpNoise = 25;

		// Work plane texture colors
		int wpColor	 = webColorToArgb(Color.web("#3838A8")); // Higher is lighter color
		int grid1Color  = webColorToArgb(Color.web("#202060"));
		int grid10Color = webColorToArgb(Color.web("#0000FF"));

		final float TILE_HALF_PIXEL_SIZE = TILE_SIZE_MM / (TILE_BIG_GRID_PX * 2);
		float workPlaneX = (float)xSizeMM;
		float workPlaneY = (float)ySizeMM;

		// Calculate texture offsets. Note X and Y are swapped in the 3D view
		float xTextureOffset = (float)((int)(ySizeMM / (TILE_SIZE_MM * 2)) - ySizeMM / (TILE_SIZE_MM * 2));
		float yTextureOffset = (float)((int)(xSizeMM / (TILE_SIZE_MM * 2)) - xSizeMM / (TILE_SIZE_MM * 2));

		int[] src = new int[TILE_BIG_GRID_PX * TILE_BIG_GRID_PX];

		// Set work plane background (done when adding noise)
		//Arrays.fill(src, wpColor);

		// Add some noise to make the work plane look real
		Random rnd = new Random();
		int r = (wpColor >> 16) & 0xFF;
		int g = (wpColor >>  8) & 0xFF;
		int b =  wpColor		   & 0xFF;
		for (int i = 0; i < src.length; i++) {		
			int n = 100 + rnd.nextInt(wpNoise + 1) - (wpNoise / 2);
			src[i] = 0xFF000000 | 
					 (Math.min(255, (r * n) / 100) << 16) |
					 (Math.min(255, (g * n) / 100) <<  8) |
					 (Math.min(255, (b * n) / 100));
		}

		// Draw small grid, 1 line
		for (int x1 = 0; x1 < TILE_BIG_GRID_PX; x1 += TILE_SMALL_GRID_PX) {
			for (int y = 0; y < TILE_BIG_GRID_PX; y++) {
			src[y * TILE_BIG_GRID_PX + x1] = grid1Color;
			src[x1 * TILE_BIG_GRID_PX + y] = grid1Color;
			}
		}

		// Draw big grid, 3 lines
		int last = TILE_BIG_GRID_PX - 1;
		for (int i = 0; i < TILE_BIG_GRID_PX; i++) {
			src[i + TILE_BIG_GRID_PX	] = grid10Color;
			src[i * TILE_BIG_GRID_PX + 1] = grid10Color;

			src[i					] = grid10Color;
			src[i * TILE_BIG_GRID_PX] = grid10Color;

			src[i * TILE_BIG_GRID_PX + last] = grid10Color;
			src[last * TILE_BIG_GRID_PX + i] = grid10Color;
		}

		// Scale up with nearest neighbor algorithm
		int upscaledX = TILE_BIG_GRID_PX * wpUpscale;
		int upscaledY = TILE_BIG_GRID_PX * wpUpscale;
		WritableImage tile = new WritableImage(upscaledX, upscaledY);
		PixelWriter pw = tile.getPixelWriter();

		for (int y = 0; y < upscaledY; y++) {
			int sy = y / wpUpscale;
			for (int x = 0; x < upscaledX; x++) {
				int sx = x / wpUpscale;
				pw.setArgb(x, y, src[sy * TILE_BIG_GRID_PX + sx]);
			}
		}

		// Create the work plane material
		PhongMaterial material = new PhongMaterial();
// Sharp edges, edges with aliasing
//		material.setDiffuseMap(tile);
//		material.setDiffuseColor(new Color(1, 1, 0, 0.33));
//		material.setSpecularColor(Color.BLACK);
//		material.setSelfIlluminationMap(tile);

		// Set work plane texture
		material.setDiffuseMap(tile);
		
		// Control work plane transparency
		Color transWhite = new Color(1, 1, 1, 0.35);
		material.setDiffuseColor(transWhite); // Work plane color
		material.setSpecularColor(Color.BLACK); // No shiny spots

//		WritableImage selfIlluminationImage = new WritableImage(1, 1);
//		selfIlluminationImage.getPixelWriter().setColor(0, 0, Color.color(0.1, 0.1, 0.1, 1.0)); // RGBA
//		material.setSelfIlluminationMap(selfIlluminationImage);

		// Create the work plane outline material
		PhongMaterial material2 = new PhongMaterial();
		WritableImage outlineImage = new WritableImage(1, 1);
		outlineImage.getPixelWriter().setColor(0, 0, argbToColor(grid10Color));
		material2.setDiffuseMap(outlineImage);
		material2.setDiffuseColor(transWhite); // Work plane color
		material2.setSpecularColor(Color.BLACK); // No shiny spots
//		material2.setSelfIlluminationMap(selfIlluminationImage);

		// Create the work plane mesh
		TriangleMesh topMesh = new TriangleMesh();
		topMesh.getPoints().setAll(
		  	0f,	0f, 0f,
  			workPlaneX, 0f, 0f,
  			workPlaneX, workPlaneY, 0f,
		  	0f, workPlaneY, 0f);

		// Map texture to mesh
		topMesh.getTexCoords().setAll(
			xTextureOffset						  , yTextureOffset,						   // bottom-left
			xTextureOffset						  , yTextureOffset + workPlaneX/TILE_SIZE_MM, // top-left
			xTextureOffset + workPlaneY/TILE_SIZE_MM, yTextureOffset + workPlaneX/TILE_SIZE_MM, // top-right
			xTextureOffset + workPlaneY/TILE_SIZE_MM, yTextureOffset);						  // bottom-right

		topMesh.getFaces().setAll(0,0, 1,1, 2,2, 0,0, 2,2, 3,3);

		MeshView topView = new MeshView(topMesh);
		topView.setMaterial(material);
		topView.setBlendMode(BlendMode.SRC_OVER);
		topView.setCullFace(CullFace.NONE);
		//topView.setCache(false); // keeps JavaFX from scaling the image

		// Create the work plane outline mesh
		final float OUT = 2.0f; // outwards mm
		final float IN  = 0.0f; // inwards mm
		float[] vert = {
						 IN,			   IN,  0f, // 0 Inside
	 		workPlaneX - IN,			   IN,  0f, // 1
	 		workPlaneX - IN,  workPlaneY - IN,  0f, // 2
						 IN,  workPlaneY - IN,  0f, // 3
					  - OUT,			 - OUT, 0f, // 4 Outside
			workPlaneX + OUT,			 - OUT, 0f, // 5
			workPlaneX + OUT, workPlaneY + OUT, 0f, // 6
					   - OUT, workPlaneY + OUT, 0f  // 7
		};

		TriangleMesh outline = new TriangleMesh();
		outline.getPoints().setAll(vert);

		outline.getTexCoords().setAll(
			0,0,  1,0,  1,1,  0,1,   // inside
			0,0,  1,0,  1,1,  0,1);  // outide

		// 8 triangles (4 quads)
		outline.getFaces().setAll(
			0,0, 4,4, 5,5,	0,0, 5,5, 1,1,   // bottom
			1,1, 5,5, 6,6,	1,1, 6,6, 2,2,   // right
			2,2, 6,6, 7,7,	2,2, 7,7, 3,3,   // top
			3,3, 7,7, 4,4,	3,3, 4,4, 0,0 ); // left

		MeshView outlineView = new MeshView(outline);
		outlineView.setMaterial(material2);
		outlineView.setBlendMode(BlendMode.SRC_OVER);
		outlineView.setCullFace(CullFace.NONE);

		// Create illumination for the work plane
		//AmbientLight ambientLight = new AmbientLight(Color.color(1.0, 1.0, 1.0, 0));
		//ambientLight.setLightOn(true);

		Group wp = new Group(topView, outlineView);

		// Center workplane, add a half pixel offset to align with bitmap
		wp.getTransforms().add(new Translate(-workPlaneX / 2 - TILE_HALF_PIXEL_SIZE, -workPlaneY / 2 - TILE_HALF_PIXEL_SIZE, 0));
		wp.setMouseTransparent(true);

		return wp;
	}

	/**
	 * Builds the scene.
	 */
	private void buildScene() {
		world.ry.setAngle(-90); // point z upwards
		world.ry.setAngle(180); // arm out towards user
		BowlerStudio.runLater(() -> getRoot().getChildren().add(world));
	}

	public void hideHand() {
		handGroup.getChildren().remove(handMesh);
	}

	public void showHand() {
		handGroup.getChildren().add(handMesh);
	}

	/**
	 * Builds the camera.
	 */
	private void buildCamera(boolean addHand) {

		// Setup scene illumination
		cameraGroup.getChildren().setAll(camera);

		/*
		// Fixed directional light from the top
		DirectionalLight sunLight1 = new DirectionalLight();
		sunLight1.setColor(Color.color(0.3, 0.3, 0.3));
		sunLight1.setDirection(new Point3D(0, 0, -1));
		sunLight1.setLightOn(false);
		cameraGroup.getChildren().add(sunLight1);

		// Point light sun high above the work plane
		PointLight sunLight2 = new PointLight(Color.color(0.2, 0.2, 0.2));
		sunLight2.setConstantAttenuation(1);
		sunLight2.setLinearAttenuation(0);
		sunLight2.setQuadraticAttenuation(0);
		sunLight2.getTransforms().add(new Translate(0, 0, 10000));
		sunLight2.setLightOn(false);
		cameraGroup.getChildren().add(sunLight2);

		// Ambient lighting
		AmbientLight ambientLight = new AmbientLight(Color.color(0.1, 0.1, 0.1));
		ambientLight.setLightOn(false);
		cameraGroup.getChildren().add(ambientLight);

		// Directional light follows the camera view angle
		DirectionalLight directionalCameraLight = new DirectionalLight(Color.color(1.0, 1.0, 1.0));
		camera.localToSceneTransformProperty().addListener((obs, oldT, newT) -> {
			Point3D d = camera.localToScene(0, 0, -1).subtract(camera.localToScene(0, 0, 0)).normalize();
			directionalCameraLight.setDirection(new Point3D(d.getX(), -d.getY(), d.getZ()));   // Y inverted
		});
		directionalCameraLight.setLightOn(false);
		cameraGroup.getChildren().add(directionalCameraLight);
		*/

		// Point light behind camera, similar to default JavaFX light
		PointLight cameraLight = new PointLight(Color.color(1.0, 1.0, 1.0));
		cameraLight.setConstantAttenuation(1);
		cameraLight.setLinearAttenuation(0);
		cameraLight.setQuadraticAttenuation(0);
		cameraLight.setLightOn(true);
		cameraGroup.getChildren().add(cameraLight);
		// listener keeps the light at the camera
		camera.localToSceneTransformProperty().addListener((obs, oldT, newT) -> {
			final float distanceBehindCamera = 10000;
			Point3D p = camera.localToScene(1000, 1000, -distanceBehindCamera);
			cameraLight.setTranslateX(-p.getX());
			cameraLight.setTranslateY( p.getY());
			cameraLight.setTranslateZ(-p.getZ());
		});

		// Enable point light illumination for selected groups
		cameraLight.getScope().addAll(userGroup, controlHandleGroup);

		CSG cylinder = new Cylinder(0, 2.5, 10, 20) // Top radius, bottom radius, height, nr. segments
			.toCSG().roty(90).setColor(Color.BLACK);

		handMesh = cylinder.getMesh();

		handGroup = new Group();
		if (addHand)
			showHand();

		camera.setNearClip(0.1);
		// camera.setFarClip(1000.0); // this is set in VirtualCameraMobileBase
		getSubScene().setCamera(camera);

		// Flip the camera upside down
		camera.setRotationAxis(Rotate.Z_AXIS);
		camera.setRotate(180);
		
		camera.setDepthTest(DepthTest.ENABLE);
		setVirtualcam(new VirtualCameraMobileBase(camera, handGroup, this, name));
		VirtualCameraFactory.setFactory(new IVirtualCameraFactory() {
			@Override
			public AbstractImageProvider getVirtualCamera() {
				throw new RuntimeException("No virtual camera available!");
			}
		});

		// TODO reorient the start camera
		BowlerStudio.runLater(() -> {
			getFlyingCamera().setGlobalToFiducialTransform(defaultCameraView);
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
	 * 
	 * @param showAxes
	 */
	private void buildAxes(boolean showAxes) {

		// int gridSize=1000;
		// int gridDensity=gridSize/10;
		//
		// PhongMaterial phongMaterial = new PhongMaterial();
		// phongMaterial.setDiffuseColor(Color.BLACK);
		// for (int i=-gridSize;i<gridSize;i++){
		// for (int j=-gridSize;j<gridSize;j++){
		// if (i%gridDensity==0 &&j%gridDensity==0){
		// Sphere s = new Sphere(1);
		// s.setMaterial(phongMaterial);
		// Affine sp=new Affine();
		// sp.setTy(i);
		// sp.setTx(j);
		// //com.neuronrobotics.sdk.common.Log.error("Placing sphere at "+i+" , "+j);
		// s.getTransforms().add(sp);
		// ground.getChildren().add(s);
		// }
		// }
		// }

		new Thread() {

			public void run() {
				try {
					// Image ruler = AssetFactory.loadAsset("ruler.png");
					// Image ruler = new Image(BowlerStudio.class.getResourceAsStream("ruler.png"));
					// Image groundLocal = AssetFactory.loadAsset("ground.png");

					// Create the rulers
					double scale = 1;
					Affine xRuler = new Affine();
					xRuler.appendScale(scale, scale, scale);
					xRuler.appendRotation(180, 0, 0, 0, 1, 0, 0);
					Affine xRulerZoffset = new Affine();
					xRulerZoffset.setTz(-0.01); // Raise xRuler up a bit

					Affine yRuler = new Affine();
					yRuler.appendScale(scale, scale, scale);
					yRuler.appendRotation(90, 0, 0, 0, 0, 0, 1);
					Affine yRulerZoffset = new Affine();					
					yRulerZoffset.setTz(0.01); // Raise yRuler up a bit

					Affine zRuler = new Affine();
                    zRuler.appendScale(scale, scale, scale);
					//zRuler.appendRotation(-180, 0, 0, 0, 1, 0, 0);
					//zRuler.appendRotation( -90, 0, 0, 0, 0, 0, 1);
					//zRuler.appendRotation(  90, 0, 0, 0, 0, 1, 0);
					//zRuler.appendRotation(-180, 0, 0, 0, 1, 0, 0);
					zRuler.appendRotation(120, 0, 0, 0, 1, -1, 1);

					// Create the workplane
					//workplaneGroup = createGridMesh(1000, 1000, 20);
					workplaneGroup = createTexturedWorkplane(196, 208);

					boolean selected = (showRuler != null) ? showRuler.isSelected() : true;

					Axis axes = new Axis(showAxes ? selected : false);

					// Lower XY-axes a bit
					Affine axisOffset = new Affine();
					axisOffset.setTz(-0.01);
					axes.getTransforms().add(axisOffset);

					BowlerStudio.runLater(() -> {

						Node xrulerImage = MakeRuler.createRuler(true);
						Node yrulerImage = MakeRuler.createRuler(false);
						Node zrulerImage = MakeRuler.createRuler(true);

						xrulerImage.getTransforms().addAll(getRulerInWorkplaneOffset(), getRulerOffset(), xRuler, xRulerZoffset);
						yrulerImage.getTransforms().addAll(getRulerInWorkplaneOffset(), getRulerOffset(), yRuler, yRulerZoffset);
						zrulerImage.getTransforms().addAll(getRulerInWorkplaneOffset(), getRulerOffset(), zRuler);

						rulerGroup.getChildren().addAll(xrulerImage, yrulerImage, zrulerImage);
						gridGroup.getChildren().addAll(rulerGroup);

						Affine groundPlacement = new Affine();
						groundPlacement.setTz(-1);
						// groundGroup.setOpacity(.5);
						groundGroup = new Group();
						groundGroup.getTransforms().add(groundPlacement);

						cameraGroup.getChildren().add(getVirtualcam().getCameraFrame());
				
						if (showAxes) {
							gridGroup.getChildren().addAll(axes, groundGroup);
							showAxis();
						}

						gridGroup.getChildren().add(workplaneGroup);
						world.getChildren().addAll(lookGroup, cameraGroup, userGroup, axisGroup, controlHandleGroup, ambientLight);
						// Use ambient illumination for workplane and axes, ruler is black so no need to illuminate

						ambientLight.getScope().addAll(workplaneGroup, axisGroup);
					});

				} catch (Exception e) {
					com.neuronrobotics.sdk.common.Log.error(e);
				}
			}

		}.start();

	}

	public Group getWorkplaneGroup() {
		return workplaneGroup;
	}

	public Group createGridMesh(int width, int height, int cellSize) {
		return createGridMesh(width, height, cellSize, 0.05);
	}

	public Group createGridMesh(int width, int height, int cellSize, double lineThickness) {
		Affine groundMove = new Affine();
		groundMove.setTx(-width / 2.0);
		groundMove.setTy(-height / 2.0);

		Group gridMeshGroup = new Group();

		// Create material for lines
		PhongMaterial material = new PhongMaterial();
		material.setDiffuseColor(Color.LIGHTBLUE);

		int numXLines = (width / cellSize) + 1;
		int numYLines = (height / cellSize) + 1;

		// Create horizontal lines
		for (int y = 0; y < numYLines; y++) {
			double yPos = y * cellSize;
			Box horizontalLine = new Box(width, lineThickness, lineThickness);
			horizontalLine.setMaterial(material);
			horizontalLine.setTranslateX(width / 2.0);
			horizontalLine.setTranslateY(yPos);
			horizontalLine.setTranslateZ(-lineThickness);
			horizontalLine.setMouseTransparent(true);
			horizontalLine.setCullFace(CullFace.NONE);// backs are black
			gridMeshGroup.getChildren().add(horizontalLine);
		}

		// Create vertical lines
		for (int x = 0; x < numXLines; x++) {
			double xPos = x * cellSize;
			Box verticalLine = new Box(lineThickness, height, lineThickness);
			verticalLine.setMaterial(material);
			verticalLine.setTranslateX(xPos);
			verticalLine.setTranslateY(height / 2.0);
			verticalLine.setTranslateZ(-lineThickness);
			verticalLine.setMouseTransparent(true);
			verticalLine.setCullFace(CullFace.NONE);// backs are black
			gridMeshGroup.getChildren().add(verticalLine);
		}

		gridMeshGroup.getTransforms().addAll(gridPlacementAffine, groundMove);

		return gridMeshGroup;
	}

	// Add the control nodes (handles/edit boxes) at the end so they are always visible
	public void addControlNode(Node n) {
		BowlerStudioModularFrame bowlerStudioModularFrame = BowlerStudioModularFrame.getBowlerStudioModularFrame();
		if (bowlerStudioModularFrame != null)
			bowlerStudioModularFrame.showCreatureLab();

		if (Platform.isFxApplicationThread())
			controlHandleGroup.getChildren().add(n);
		else
			BowlerStudio.runLater(() -> controlHandleGroup.getChildren().add(n));
	}

	public void removeControlNode(Node n) {
		BowlerStudio.runLater(() -> controlHandleGroup.getChildren().remove(n));
	}

	// Check if the userGroup contains a node
	public boolean contains(Node n) {
		return userGroup.getChildren().contains(n);
	}

	// Add nodes to the userGroup
	public void addUserNode(Node n) {
		BowlerStudioModularFrame bowlerStudioModularFrame = BowlerStudioModularFrame.getBowlerStudioModularFrame();
		if (bowlerStudioModularFrame != null)
			bowlerStudioModularFrame.showCreatureLab();

		if (Platform.isFxApplicationThread())
			userGroup.getChildren().add(n);
		else
			BowlerStudio.runLater(() -> userGroup.getChildren().add(n));
	}

	// Remove nodes from the userGroup
	public void removeUserNode(Node n) {
		BowlerStudio.runLater(() -> userGroup.getChildren().remove(n));
	}

	// Clear all object from the userGroup
	public void clearUserNode() {
		// new RuntimeException("Clearing all user nodes!");
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
			// com.neuronrobotics.sdk.common.Log.error(e);
		}

	}

	/**
	 * Handle mouse.
	 *
	 * @param scene the scene
	 */

	private void handleMouse(SubScene scene) {
		if (disabeControl) {
			com.neuronrobotics.sdk.common.Log.error("No mouse control added " + name);
			scene.setPickOnBounds(false);
			return;
		}

		com.neuronrobotics.sdk.common.Log.debug("Setting up Mouse Handelers " + name);
		scene.setOnMouseClicked(event -> {
			resetMouseTime();
			if (getControlsMap().timeToCancel(event))
				cancelSelection();
		});

		scene.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				// com.neuronrobotics.sdk.common.Log.error("Bowler 3d start "+name);
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
				
				Node node = (Node) me.getSource();
				double mouseY = me.getY(); // Y position relative to the node itself
				double nodeHeight = node.getBoundsInLocal().getHeight();
				aboveSplit = mouseY < (nodeHeight / 2);

				mouseOldX = mousePosX;
				mouseOldY = mousePosY;
				mousePosX = me.getSceneX();
				mousePosY = me.getSceneY();
				mouseDeltaX = (mousePosX - mouseOldX);
				mouseDeltaY = (mousePosY - mouseOldY);
				double modifier = 1.0;
				double modifierFactor = 0.1;

				if (getControlsMap().isSlowMove(me))
					modifier = 0.1;

				if (getControlsMap().isRotate(me)) {
					double el = getVirtualcam().getTiltAngle();
					boolean above = (el > 0);
//					if (aboveSplit) {
//						above=!above;
//					}
					//System.out.println("Above = "+el);
					double i = above ? -1 : 1;
					TransformNR trans = new TransformNR(0, 0, 0,
							new RotationNR(mouseDeltaY * modifierFactor * modifier * mouseScale,
									i * mouseDeltaX * modifierFactor * modifier * mouseScale, 0));
					moveCamera(trans);
				}

				if (getControlsMap().isMove(me) && move) {
					double depth = -100 / getVirtualcam().getZoomDepth();

					// Limit smallest movement amount
					depth = Math.min(100, depth);
					TransformNR newPose = new TransformNR(
							mouseDeltaX * modifierFactor * modifier * (mouseScale / 2) / depth,
							mouseDeltaY * modifierFactor * modifier * (mouseScale / 2) / depth, 0, new RotationNR());
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

/* DEVELOPMENT
    --add-exports javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED
    --add-exports javafx.graphics/com.sun.javafx.geom=ALL-UNNAMED
    --add-exports javafx.graphics/com.sun.javafx.scene.input=ALL-UNNAMED
    --add-exports javafx.graphics/com.sun.javafx.geom.transform=ALL-UNNAMED
    public double objectDistance() {

        Point3D p = camera.localToScene(0, 0, 0);
        Vec3d camPos = new Vec3d(-p.getX(), p.getY(), -p.getZ());

        Point3D dir = camera.localToScene(0, 0, -1).subtract(camera.localToScene(0, 0, 0)).normalize();
        Vec3d camDir = new Vec3d(dir.getX(), -dir.getY(), dir.getZ());

        //System.out.println("\nCamera position : " + camPos);
        //System.out.println(  "Camera direction: " + camDir);

        PickRay ray = new PickRay(camPos, camDir, 0.1, 499);

        PickResultChooser chooser = new PickResultChooser();   
        NodeHelper.pickNode(userGroup, ray, chooser);
        
        PickResult pr = chooser.toPickResult();

        if ((pr != null) && (pr.getIntersectedNode() != null)) {
            double dist = pr.getIntersectedDistance();

            //System.out.println(">>> HIT POINT: " + pr.getIntersectedPoint() + " Distance: " + (int)dist);

            return dist;
        }

        return Double.POSITIVE_INFINITY;
    }
*/

	public void zoomIncrement(double deltaY) {
		double zoomFactor = -deltaY * getVirtualcam().getZoomDepth() / 500;
		//
		// double z = camera.getTranslateY();
		// double newZ = z + zoomFactor;
		// camera.setTranslateY(newZ);
		// com.neuronrobotics.sdk.common.Log.error("Z = "+zoomFactor);

		getVirtualcam().setZoomDepth(getVirtualcam().getZoomDepth() + zoomFactor);

        // In addition to the zoom also move a bit closer, gives unlimited zoom
		double moveCloser = (deltaY > 0) ? 0.1 : -0.1;
		TransformNR zoomMove = new TransformNR();
		zoomMove.translateZ(moveCloser);

		moveCamera(zoomMove);
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
		com.neuronrobotics.sdk.common.Log.debug("Setting UI scene");
		this.scene = scene;
	}

	/**
	 * Gets the root.
	 *
	 * @return the root
	 */
	public Group getRoot() {
		return rootGroup;
	}

	/**
	 * Removes the arm.
	 */
	public void removeArm() {
		world.getChildren().remove(manipulatorGroup);
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
		// com.neuronrobotics.sdk.common.Log.error("Selecting group");
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
			if ((selectedCsg.getMaxX() < 1) || (selectedCsg.getMinX() > -1)) {
				finalCSG = finalCSG.movex(-xcenter);
				poseToMove.translateX(xcenter);
			}

			if ((selectedCsg.getMaxY() < 1) || (selectedCsg.getMinY() > -1)) {
				finalCSG = finalCSG.movey(-ycenter);
				poseToMove.translateY(ycenter);
			}

			if ((selectedCsg.getMaxZ() < 1) || (selectedCsg.getMinZ() > -1)) {
				finalCSG = finalCSG.movez(-zcenter);
				poseToMove.translateZ(zcenter);
			}

			Affine manipulator2;
			try {
				manipulator2 = selectedCsg.hasManipulator() ? selectedCsg.getManipulator() : new Affine();
				focusToAffine(poseToMove, manipulator2);
			} catch (MissingManipulatorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		resetMouseTime();
	}

	public void focusTo(TransformNR poseToMove) {
		focusToAffine(poseToMove, new Affine());
	}

	public void focusToAffine(TransformNR poseToMove, Affine manipulator2) {
		if (focusing)
			return;

		if (manipulator2 == null) {
			com.neuronrobotics.sdk.common.Log.error(new RuntimeException("Can not focus on null affine"));
			return;
		}

		focusing = true;
		BowlerStudio.runLater(() -> {
			Affine centering = TransformFactory.nrToAffine(poseToMove);
			// this section keeps the camera oriented the same way to avoid whipping
			// around

			TransformNR rotationOnlyCOmponentOfManipulator = TransformFactory.affineToNr(manipulator2);
			rotationOnlyCOmponentOfManipulator.setX(0);
			rotationOnlyCOmponentOfManipulator.setY(0);
			rotationOnlyCOmponentOfManipulator.setZ(0);
			TransformNR reverseRotation = rotationOnlyCOmponentOfManipulator.inverse();
			TransformNR startSelectNr = previousTarget.copy();
			TransformNR targetNR;// =
									// TransformFactory.affineToNr(selectedCsg.getManipulator());
			if ((Math.abs(manipulator2.getTx()) > 0.1) || (Math.abs(manipulator2.getTy()) > 0.1)
					|| (Math.abs(manipulator2.getTz()) > 0.1)) {
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
			cameraGroup.getTransforms().add(interpolator);
			try {
				if ((Math.abs(manipulator2.getTx()) > 0.1) || (Math.abs(manipulator2.getTy()) > 0.1)
				 || (Math.abs(manipulator2.getTz()) > 0.1)) {
					// BowlerStudio.runLater(() -> {
					cameraGroup.getTransforms().add(manipulator2);
					cameraGroup.getTransforms().add(correction);
					// });

				} else
					// BowlerStudio.runLater(() -> {
					cameraGroup.getTransforms().add(centering);
				// });
			} catch (Exception ex) {

				com.neuronrobotics.sdk.common.Log.error(ex);
			}

			focusInterpolate(startSelectNr, targetNR, NUMBER_OF_INTERPOLATION_STEPS, interpolator);
		});
	}

	public void targetAndFollow(TransformNR poseToMove, Affine manipulator2) {
		this.poseToMove = poseToMove;

		if (focusing)
			return;

		if (manipulator2 == null) {
			com.neuronrobotics.sdk.common.Log.error(new RuntimeException("Can not focus on null affine"));
			return;
		}

		focusing = true;
		BowlerStudio.runLater(() -> {
			Affine referenceFrame = TransformFactory.nrToAffine(poseToMove);
			// this section keeps the camera oriented the same way to avoid whipping around

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

			TransformNR startSelectNr = previousTarget.copy();
			// =
			// TransformFactory.affineToNr(selectedCsg.getManipulat/or());

			targetNR = poseToMove.times(TransformFactory.affineToNr(manipulator2));

			Affine interpolator = new Affine();
			interpolator.setTx(startSelectNr.getX() - targetNR.getX());
			interpolator.setTy(startSelectNr.getY() - targetNR.getY());
			interpolator.setTz(startSelectNr.getZ() - targetNR.getZ());
			removeAllFocusTransforms();
			cameraGroup.getTransforms().add(interpolator);
			cameraGroup.getTransforms().add(referenceFrame);
			try {
				cameraGroup.getTransforms().add(manipulator2);
				cameraGroup.getTransforms().add(correction);
				cameraGroup.getTransforms().add(correction2);
			} catch (Exception ex) {
				com.neuronrobotics.sdk.common.Log.error(ex);
			}
			focusInterpolate(startSelectNr, targetNR, NUMBER_OF_INTERPOLATION_STEPS, interpolator);
		});
	}

	private void resetMouseTime() {
		// com.neuronrobotics.sdk.common.Log.error("Resetting mouse");
		this.lastMosueMovementTime = System.currentTimeMillis();
	}

	double bound180(double in) {
		while (in > 180)
			in -= 360;
		while (in < -180)
			in += 360;
		return in;
	}

	public void focusOrientation(TransformNR orient) {
		focusOrientation(orient, null, getFlyingCamera().getDefaultZoomDepth());
	}

	public void focusOrientation(TransformNR orient, TransformNR trans, double zoom) {

		abortFocus = true;

		if ((orient != null) || (trans != null)) {

			// Wait until possible previous focus aborts
			new Thread(() -> {
				while (focusing) {
				   try {
						Thread.sleep(8);
					} catch (InterruptedException e) {
					   focusing = false;
					}
				}

				focusing = true;
				abortFocus = false;
				runSyncFocus(orient, trans, zoom);
			}).start();
		}
	}

	private void runSyncFocus(TransformNR orient, TransformNR trans, double zoom) {

		double az = (orient == null) ? 0
				: bound180(getFlyingCamera().getPanAngle() - 90
						+ Math.toDegrees(orient.getRotation().getRotationAzimuth()));

		double el = (orient == null) ? 0
				: bound180(getFlyingCamera().getTiltAngle() + 90
						+ Math.toDegrees(orient.getRotation().getRotationElevation()));
		// com.neuronrobotics.sdk.common.Log.error("Focus from\n\taz:" + az + " \n\tel:"
		// + el);
		double x = 0;
		double y = 0;
		double z = 0;
		double zoomDelta = zoom - getFlyingCamera().getZoomDepth();

		if (trans != null) {
			x = trans.getX() - getFlyingCamera().getGlobalX();
			y = trans.getY() - getFlyingCamera().getGlobalY();
			z = trans.getZ() - getFlyingCamera().getGlobalZ();
		}

		int interpolationSteps = Math.max((int)(Math.abs(x) / 6), (int)(Math.abs(y) / 6));
		interpolationSteps = Math.max((int)(Math.abs(z) / 6), interpolationSteps);
		interpolationSteps = Math.max((int)(Math.abs(el) / 5), interpolationSteps);
		interpolationSteps = Math.max((int)(Math.abs(az) / 5), interpolationSteps);
		if (!getFlyingCamera().isZoomLocked())
			interpolationSteps = Math.max((int)(Math.abs(zoomDelta) / 10), interpolationSteps);

		interpolationSteps = Math.min(interpolationSteps, NUMBER_OF_INTERPOLATION_STEPS);
		final int steps = interpolationSteps;
		
		try {
			for (int i = 0; (i < steps) && !abortFocus; i++) {
				// com.neuronrobotics.sdk.common.Log.error("\tFocus to \n\t\taz:" + aztmp + "
				// \n\t\tel:" + eltmp);
				double mx = x / steps;
				double my = y / steps;
				double mz = z / steps;

				waitingForCompletion = true;
				long startTime = System.currentTimeMillis();

				BowlerStudio.runLater(() -> {
					moveCamera(new TransformNR(0, 0, 0,
							new RotationNR(-el / steps, -az / steps, 0)));

					getFlyingCamera().DrivePositionAbsolute(mx, my, mz);

					if (!getFlyingCamera().isZoomLocked())
						getFlyingCamera().setZoomDepth(
								getFlyingCamera().getZoomDepth() + (zoomDelta / steps));
					waitingForCompletion = false;
				});

				// Wait for 36ms including the processing of the update
                while (waitingForCompletion || ((System.currentTimeMillis() - startTime) < 36) && !abortFocus) {
					try {
						Thread.sleep(6);
					} catch (InterruptedException e) {
						abortFocus = true;
					}
			  }

			}

			if (!abortFocus) {
				 BowlerStudio.runLater(() -> {
					 getFlyingCamera().SetOrientation(orient);
					 getFlyingCamera().SetPosition(trans);
				 });
			 }

		} catch (Throwable t) {
			com.neuronrobotics.sdk.common.Log.error(t);
		}

		focusing = false;
		abortFocus = false;
	}

	private void focusInterpolate(TransformNR start, TransformNR target, int interpolationSteps, Affine interpolator) {

		new Thread(() -> {
			int depth = 0;
			while (focusing) {
				try {
					Thread.sleep(16);
				} catch (InterruptedException e) {
					// Auto-generated catch block
					com.neuronrobotics.sdk.common.Log.error(e);
					focusing = false;
				}
				double depthScale = 1 - (double) depth / (double) interpolationSteps;
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
				// com.neuronrobotics.sdk.common.Log.error("Interpolation step " + depth + " x "
				// + xIncrement
				// + " y " + yIncrement + " z " + zIncrement);
				if (depth >= interpolationSteps) {
					// com.neuronrobotics.sdk.common.Log.error("Camera intrpolation done");
					BowlerStudio.runLater(() -> {
						cameraGroup.getTransforms().remove(interpolator);
					});
					previousTarget = target.copy();
					previousTarget.setRotation(new RotationNR());
					focusing = false;
				}

				depth++;
			}
		}).start();

	}

	private void removeAllFocusTransforms() {
		cameraGroup.getTransforms().clear();
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
					// com.neuronrobotics.sdk.common.Log.error("Script matches");
					try {
						int num = Integer.parseInt(traceParts[1].trim());

						if (num == lineNumber) {
							// com.neuronrobotics.sdk.common.Log.error("MATCH");
							objsFromScriptLine.add(checker);
						}
					} catch (Exception e) {
						com.neuronrobotics.sdk.common.Log.error(e);
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
		// Auto-generated method stub
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

	public void setMouseScale(double mouseScale) {
		this.mouseScale = mouseScale;
	}

	public void lockZoom() {
		getFlyingCamera().lockZoom();
	}

	@Override
	public void onChange(VirtualCameraMobileBase camera) {
		for (ICameraChangeListener c : listeners) {
			try {
				c.onChange(camera);
			} catch (Throwable t) {
				com.neuronrobotics.sdk.common.Log.error(t);
			}
		}
	}

	public void lockMove() {
		move = false;
		getFlyingCamera().lockMove();
	}

	public void disableControls() {
		// Auto-generated method stub
		disabeControl = true;
	}

	public void placeGrid(TransformNR workplane) {
		BowlerKernel.runLater(() -> {
			TransformFactory.nrToAffine(workplane, gridPlacementAffine);
		});
	}

	@Override
	public void setAllCSG(Collection<CSG> toAdd, File source) {
		clearUserNode();
		addCSG(toAdd, source);
	}

	@Override
	public void addCSG(Collection<CSG> toAdd, File source) {
		for (CSG c : toAdd)
			addObject(c, source);
	}

	@Override
	public void highlightException(File fileEngineRunByName, Throwable ex) {

	}

	@Override
	public Set<CSG> getVisibleCSGs() {
		return getCsgMap().keySet();
	}

	@Override
	public void setSelectedCsg(Collection<CSG> selectedCsg) {
		for (CSG c : selectedCsg)
			selectObjectsSourceFile(c);
	}

	public Affine getRulerOffset() {
		return rulerOffset;
	}

	public void setRulerOffset(Affine rulerOffset) {
		this.rulerOffset = rulerOffset;
	}

	public Affine getRulerInWorkplaneOffset() {
		return rulerInWorkplaneOffset;
	}

	public void setRulerInWorkplaneOffset(Affine rulerInWorkplaneOffset) {
		this.rulerInWorkplaneOffset = rulerInWorkplaneOffset;
	}

	public Group getRulerGroup() {
		return rulerGroup;
	}
}
