package com.neuronrobotics.bowlerstudio.creature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.bowlerstudio.threed.MobileBaseCadManager;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.DhInverseSolver;
import com.neuronrobotics.sdk.addons.kinematics.IDriveEngine;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.IDeviceConnectionEventListener;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.util.FileChangeWatcher;

public class CreatureLab extends AbstractBowlerStudioTab implements IOnEngineeringUnitsChange {

	
	private BowlerAbstractDevice pm;

	private FileChangeWatcher driveEngineWitcher;
	private HashMap<DHParameterKinematics, FileChangeWatcher> dhKinematicsFileWatchers = new HashMap<>();
	private IDriveEngine defaultDriveEngine;
	// private DhInverseSolver defaultDHSolver;
	private Menu localMenue;
	private ProgressIndicator pi;

	private MobileBaseCadManager baseManager;
	private CheckBox autoRegen = new CheckBox("Auto-Regnerate CAD");
	
	private AbstractGameController gameController = new AbstractGameController() {

		@Override
		public double getNavUp() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getNavRight() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getNavLeft() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getNavDown() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getControls3Plus() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getControls3Minus() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getControls2Plus() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getControls2Minus() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getControls1Plus() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getControls1Minus() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getControls0Plus() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getControls0Minus() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getActionRight() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getActionLeft() {
			// TODO Auto-generated method stub
			return 0;
		}
	};
	

	@Override
	public void onTabClosing() {
		baseManager.onTabClosing();
	}

	@Override
	public String[] getMyNameSpaces() {
		// TODO Auto-generated method stub
		return new String[0];
	}

	@Override
	public void initializeUI(BowlerAbstractDevice pm) {
		setGraphic(AssetFactory.loadIcon("CreatureLab-Tab.png"));
		this.pm = pm;
		autoRegen.setSelected(true);
		autoRegen.setOnAction(event -> {
			if(autoRegen.isSelected()){
				generateCad();
			}
		});
		// TODO Auto-generated method stub
		setText(pm.getScriptingName());

		GridPane dhlabTopLevel = new GridPane();
		if (DHParameterKinematics.class.isInstance(pm)) {
			DHParameterKinematics device = (DHParameterKinematics) pm;
			try {
				setDefaultDhParameterKinematics(device);

			} catch (Exception e) {
				BowlerStudioController.highlightException(null, e);
			}
			Log.debug("Loading xml: " + device.getXml());
			dhlabTopLevel.add(new DhChainWidget(device, null), 0, 0);
		} else if (MobileBase.class.isInstance(pm)) {
			try {
				ScriptingEngine.setAutoupdate(true);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			MobileBase device = (MobileBase) pm;

			// Button save = new Button("Save Configuration");

			setDefaultWalkingEngine(device);

			AnchorPane controls = new AnchorPane();
//			Accordion advancedPanel = new Accordion();
//			//if (device.getDriveType() == DrivingType.WALKING) {
//				TitledPane rp = new TitledPane("Walking Engine", );
//				advancedPanel.getPanes().add(rp);
//				advancedPanel.setExpandedPane(rp);
//			//}

			TreeItem<String> rootItem;
			try {
				rootItem = new TreeItem<String>( device.getScriptingName(),AssetFactory.loadIcon("creature.png"));
			} catch (Exception e) {
				rootItem = new TreeItem<String>( device.getScriptingName());
			}
			rootItem.setExpanded(true);
			HashMap<TreeItem<String>, Runnable> callbackMapForTreeitems = new HashMap<>();
			HashMap<TreeItem<String>, Group> widgetMapForTreeitems = new HashMap<>();

			TreeView<String> tree = new TreeView<>(rootItem);
			try {
				MobleBaseMenueFactory.load(device, tree, rootItem, callbackMapForTreeitems, widgetMapForTreeitems, this);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tree.setPrefSize(325, 1000);
			tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
			tree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Object>() {

				@Override
				public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
					@SuppressWarnings("unchecked")
					TreeItem<String> treeItem = (TreeItem<String>) newValue;
					new Thread() {
						public void run() {
							if (callbackMapForTreeitems.get(treeItem) != null) {
								callbackMapForTreeitems.get(treeItem).run();
							}
							if (widgetMapForTreeitems.get(treeItem) != null) {

								Platform.runLater(() -> {
									controls.getChildren().clear();
									controls.getChildren().add(widgetMapForTreeitems.get(treeItem));
								});
							} else {
								Platform.runLater(() -> {
									controls.getChildren().clear();
								});
							}
						}
					}.start();

				}
			});
			// addAppendagePanel(device.getLegs(),"Legs",advancedPanel);
			// addAppendagePanel(device.getAppendages(),"Appandges",advancedPanel);
			// addAppendagePanel(device.getSteerable(),"Steerable",advancedPanel);
			// addAppendagePanel(device.getDrivable(),"Drivable",advancedPanel);
			HBox progress = new HBox(10);
			pi = new ProgressIndicator(0);
			progress.getChildren().addAll(new Label("Cad Progress:"), pi,autoRegen);
			baseManager = new MobileBaseCadManager(device, pi,autoRegen);
			// dhlabTopLevel.add(advancedPanel, 0, 0);
			VBox inputs = new VBox(10);
			BowlerStudio.setOverlayLeft(new Group(tree));
			device.addConnectionEventListener(new IDeviceConnectionEventListener() {
				
				@Override
				public void onDisconnect(BowlerAbstractDevice arg0) {
					BowlerStudio.clearOverlayLeft();
					BowlerStudio.clearOverlayTop();
					BowlerStudio.clearOverlayTopRight();
					BowlerStudio.clearOverlayBottomRight();
				}
				
				@Override
				public void onConnect(BowlerAbstractDevice arg0) {}
			});
			progress.setStyle("-fx-background-color: #FFFFFF;");
			progress.setOpacity(.7);
			progress.setPrefSize(325, 50);
			BowlerStudio.setOverlayTop(new Group(progress));
			BowlerStudio.setOverlayTopRight(new Group(new JogWidget(device)));
			BowlerStudio.setOverlayBottomRight(new Group(controls));
			
			
			
//			//inputs.getChildren().addAll(progress);
//			dhlabTopLevel.add(inputs, 0, 0);
//			AnchorPane.setTopAnchor(controls, 0.0);
//			AnchorPane.setRightAnchor(controls, 0.0);
//			AnchorPane.setLeftAnchor(controls, 0.0);
//			AnchorPane.setBottomAnchor(controls, 0.0);
//			VBox controlGroup = new VBox(10);
//			controlGroup.getChildren().addAll(advancedPanel,controls);
//			dhlabTopLevel.add(controlGroup, 1, 0);
		} else if (AbstractKinematicsNR.class.isInstance(pm)) {
			AbstractKinematicsNR device = (AbstractKinematicsNR) pm;
			dhlabTopLevel.add(new DhChainWidget(device, null), 0, 0);
		}
		generateCad();

		setContent(new ScrollPane(dhlabTopLevel));

	}



	private File setDefaultDhParameterKinematics(DHParameterKinematics device) {
		File code = null;
		try {
			code = ScriptingEngine.fileFromGit(device.getGitDhEngine()[0], device.getGitDhEngine()[1]);
			DhInverseSolver defaultDHSolver = (DhInverseSolver) ScriptingEngine.inlineFileScriptRun(code, null);

			if (dhKinematicsFileWatchers.get(device) != null) {
				dhKinematicsFileWatchers.get(device).close();
			}
			FileChangeWatcher w;
			try {
				w = new FileChangeWatcher(code);
				dhKinematicsFileWatchers.put(device, w);
				File c = code;
				w.addIFileChangeListener((fileThatChanged, event) -> {
					try {
						System.out.println("D-H Solver changed, updating "+device.getScriptingName());
						DhInverseSolver d = (DhInverseSolver) ScriptingEngine.inlineFileScriptRun(c, null);
						device.setInverseSolver(d);
					} catch (Exception ex) {
						BowlerStudioController.highlightException(c, ex);
					}
				});
				w.start();
				
			} catch (IOException e) {
				BowlerStudioController.highlightException(code, e);
			}

			device.setInverseSolver(defaultDHSolver);
			return code;
		} catch (Exception e1) {
			BowlerStudioController.highlightException(code, e1);
		}
		return null;

	}
	
	

	private void setDefaultWalkingEngine(MobileBase device) {
		if (defaultDriveEngine == null) {
			setGitWalkingEngine(device.getGitWalkingEngine()[0], device.getGitWalkingEngine()[1], device);
		}
		for (DHParameterKinematics dh : device.getAllDHChains()) {
			setDefaultDhParameterKinematics(dh);
		}
	}

	public void setGitWalkingEngine(String git, String file, MobileBase device) {

		device.setGitWalkingEngine(new String[] { git, file });
		File code = null;
		try {
			code = ScriptingEngine.fileFromGit(git, file);
		} catch (GitAPIException | IOException e) {
			BowlerStudioController.highlightException(code, e);
		}

		if (driveEngineWitcher != null)
			driveEngineWitcher.close();

		try {
			driveEngineWitcher = new FileChangeWatcher(code);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File c = code;
		driveEngineWitcher.addIFileChangeListener((fileThatChanged, event) -> {

			try {

				defaultDriveEngine = (IDriveEngine) ScriptingEngine.inlineFileScriptRun(c, null);
				device.setWalkingDriveEngine(defaultDriveEngine);
			} catch (Exception ex) {
				BowlerStudioController.highlightException(c, ex);
			}

		});
		driveEngineWitcher.start();
		try {
			defaultDriveEngine = (IDriveEngine) ScriptingEngine.inlineFileScriptRun(c, null);
			device.setWalkingDriveEngine(defaultDriveEngine);
		} catch (Exception ex) {
			BowlerStudioController.highlightException(c, ex);
		}
	}



	public void generateCad() {
		//new Exception().printStackTrace();
		baseManager.generateCad();
	}

	
	@Override
	public void onTabReOpening() {
		baseManager.setCadScript(baseManager.getCadScript());
		try {
			if(autoRegen.isSelected())
				generateCad();
		} catch (Exception ex) {

		}
	}

	public static String getFormatted(double value) {
		return String.format("%4.3f%n", (double) value);
	}

	
	@Override
	public void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSliderDoneMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
		if(autoRegen.isSelected())
			generateCad();
	}

	public AbstractGameController getController() {

		return gameController;
	}

	public void setGitDhEngine(String gitsId, String file, DHParameterKinematics dh) {
		dh.setGitDhEngine(new String[] { gitsId, file });

		setDefaultDhParameterKinematics(dh);

	}

	public void setGitCadEngine(String gitsId, String file, MobileBase device) throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		baseManager.setGitCadEngine(gitsId, file, device);
	}

	public void setGitCadEngine(String gitsId, String file, DHParameterKinematics dh) throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		baseManager.setGitCadEngine(gitsId, file, dh);
	}



}
