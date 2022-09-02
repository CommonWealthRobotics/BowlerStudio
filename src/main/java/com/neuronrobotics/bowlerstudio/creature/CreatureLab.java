package com.neuronrobotics.bowlerstudio.creature;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.BowlerStudioModularFrame;
import com.neuronrobotics.bowlerstudio.IssueReportingExceptionHandler;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.bowlerstudio.util.FileWatchDeviceWrapper;
import com.neuronrobotics.sdk.addons.gamepad.BowlerJInputDevice;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.DhInverseSolver;
import com.neuronrobotics.sdk.addons.kinematics.IDriveEngine;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.IDeviceConnectionEventListener;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class CreatureLab extends AbstractBowlerStudioTab implements IOnEngineeringUnitsChange {

	private BowlerAbstractDevice pm;

	private IDriveEngine defaultDriveEngine;
	// private DhInverseSolver defaultDHSolver;
	private Menu localMenue;
	private ProgressIndicator pi;

	private MobileBaseCadManager baseManager;
	private CheckBox autoRegen = new CheckBox("Auto-Regen CAD");
	Parent root;
	private BowlerJInputDevice gameController = null;
	CreatureLabControlsTab tab = new CreatureLabControlsTab();;

	@Override
	public void onTabClosing() {
		baseManager.onTabClosing();
	}

	@Override
	public String[] getMyNameSpaces() {
		// TODO Auto-generated method stub
		return new String[0];
	}

	@SuppressWarnings({ "restriction", "restriction" })
	@Override
	public void initializeUI(BowlerAbstractDevice pm) {
		setGraphic(AssetFactory.loadIcon("CreatureLab-Tab.png"));
		this.pm = pm;
		autoRegen.setSelected(true);
		autoRegen.setOnAction(event -> {
			baseManager.setAutoRegen(autoRegen.isSelected());
			if (autoRegen.isSelected()) {
				generateCad();
			}
		});
		// TODO Auto-generated method stub
		setText(pm.getScriptingName());

		try {
			ScriptingEngine.setAutoupdate(true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		MobileBase device = (MobileBase) pm;
		
		// Button save = new Button("Save Configuration");

		FXMLLoader loader;
		try {
			loader = AssetFactory.loadLayout("layout/CreatureLabControlsTab.fxml", true);
			Platform.runLater(() -> {
				loader.setController(tab);
				// This is needed when loading on MAC
				loader.setClassLoader(getClass().getClassLoader());
				try {
					root = loader.load();
					finishLoading(device);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			});

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	private void finishLoading(MobileBase device) {

		TreeView<String> tree = null;
		TreeItem<String> rootItem = null;
		TreeItem<String> mainBase = null;
		int count =1;
		for(DHParameterKinematics kin:device.getAllDHChains()) {
			for(int i=0;i<kin.getNumberOfLinks();i++) {
				DHLink dhLink = kin.getDhLink(i);
				if(dhLink.getSlaveMobileBase()!=null) {
					count++;
				}
			}
		}
		try {
			rootItem = new TreeItem<String>("Mobile Bases", AssetFactory.loadIcon("creature.png"));
			mainBase= new TreeItem<String>(device.getScriptingName(), AssetFactory.loadIcon("creature.png"));
		} catch (Exception e) {
			rootItem = new TreeItem<String>(device.getScriptingName());
		}
		if(count==1) {
			rootItem=mainBase;
		}else {
			rootItem.getChildren().add(mainBase);
		}
		tree = new TreeView<>(rootItem);
		AnchorPane treebox1 = tab.getTreeBox();
		treebox1.getChildren().clear();
		treebox1.getChildren().add(tree);
		AnchorPane.setTopAnchor(tree, 0.0);
		AnchorPane.setLeftAnchor(tree, 0.0);
		AnchorPane.setRightAnchor(tree, 0.0);
		AnchorPane.setBottomAnchor(tree, 0.0);

		
		HashMap<TreeItem<String>, Runnable> callbackMapForTreeitems = new HashMap<>();
		HashMap<TreeItem<String>, Group> widgetMapForTreeitems = new HashMap<>();
		File source;
		boolean creatureIsOwnedByUser=false;
		try {
			source = ScriptingEngine.fileFromGit(device.getGitSelfSource()[0], device.getGitSelfSource()[1]);
			creatureIsOwnedByUser = ScriptingEngine.checkOwner(source);
		} catch (GitAPIException | IOException e) {
			// TODO Auto-generated catch block
			new IssueReportingExceptionHandler().uncaughtException(Thread.currentThread(), e);
			
		}
		
		rootItem.setExpanded(true);
		MobileBaseCadManager.get(device,BowlerStudioController.getMobileBaseUI());
		MobleBaseMenueFactory.load(device, tree, mainBase, callbackMapForTreeitems, widgetMapForTreeitems, this,true,creatureIsOwnedByUser);
		tree.setPrefWidth(325);
		tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		JogMobileBase walkWidget = new JogMobileBase(device);
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
						AnchorPane box=hasWalking(device)?tab.getControlsBox():tab.getWalkingBox();
						if (widgetMapForTreeitems.get(treeItem) != null) {

							Platform.runLater(() -> {
								box.getChildren().clear();
								Group g = widgetMapForTreeitems.get(treeItem);
								box.getChildren().add(g);
								AnchorPane.setTopAnchor(g, 0.0);
								AnchorPane.setLeftAnchor(g, 0.0);
								AnchorPane.setRightAnchor(g, 0.0);
								AnchorPane.setBottomAnchor(g, 0.0);
							});
						} else {
							Platform.runLater(() -> {
								box.getChildren().clear();
							});
						}
					}


				}.start();

			}
		});
		VBox progress = new VBox(10);

		final ToggleGroup group = new ToggleGroup();

		RadioButton rb1 = new RadioButton();
		rb1.setToggleGroup(group);
		rb1.setSelected(true);
		rb1.setOnAction(event -> {
			setCadMode(false);
		});

		RadioButton rb2 = new RadioButton();
		rb2.setToggleGroup(group);
		rb2.fire();
		rb2.setOnAction(event -> {
			setCadMode(true);
		});
		
		HBox radioOptions = new HBox(10);
		radioOptions.getChildren().addAll(new Label("Cad"), rb1, rb2, new Label("Config"));

		pi = new ProgressIndicator(0);
		baseManager = MobileBaseCadManager.get(device, BowlerStudioController.getMobileBaseUI());
		pi.progressProperty().bindBidirectional(baseManager.getProcesIndictor());
		HBox progressIndicatorPanel = new HBox(10);
		progressIndicatorPanel.getChildren().addAll(new Label("Cad Progress:"), pi);
		progress.getChildren().addAll(progressIndicatorPanel, autoRegen, radioOptions);

		progress.setStyle("-fx-background-color: #FFFFFF;");
		progress.setOpacity(.7);
		progress.setMinHeight(100);
		progress.setPrefSize(325, 150);
		tab.setOverlayTop(progress);
		if(	hasWalking(device)) {
			tab.setOverlayTopRight(walkWidget);
		}
		BowlerStudioModularFrame.getBowlerStudioModularFrame().showCreatureLab();
		setCadMode(true);// start the UI in config mode
		generateCad();

		setContent(root);

	}
	private boolean hasWalking(MobileBase device) {
		return device.getLegs().size()>0||
				device.getSteerable().size()>0||
				device.getDrivable().size()>0;
	}
	private void setCadMode(boolean mode) {
		new Thread(() -> {
			baseManager.setConfigurationViewerMode(mode);
			baseManager.setAutoRegen(autoRegen.isSelected());
			if (autoRegen.isSelected()) {
				generateCad();
			}
		}).start();
		
	}

	public void generateCad() {
		// new Exception().printStackTrace();
		baseManager.generateCad();
	}

	@Override
	public void onTabReOpening() {
		baseManager.run();
		try {
			if (autoRegen.isSelected())
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
		if (autoRegen.isSelected())
			generateCad();
	}

	public BowlerJInputDevice getController() {

		return getGameController();
	}

	public BowlerJInputDevice getGameController() {
		return gameController;
	}

	public void setGameController(BowlerJInputDevice bowlerJInputDevice) {
		this.gameController = bowlerJInputDevice;
	}

	public void setGitDhEngine(String gitsId, String file, DHParameterKinematics dh) {
		MobileBaseLoader.get(baseManager.getMobileBase()).setDefaultDhParameterKinematics(dh);

	}

	public void setGitCadEngine(String gitsId, String file, MobileBase device)
			throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		baseManager.setGitCadEngine(gitsId, file, device);
	}

	public void setGitCadEngine(String gitsId, String file, DHParameterKinematics dh)
			throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		baseManager.setGitCadEngine(gitsId, file, dh);
	}

	public void setGitWalkingEngine(String git, String file, MobileBase device) {

		MobileBaseLoader.get(baseManager.getMobileBase()).setGitWalkingEngine(git, file, device);
	}

}
