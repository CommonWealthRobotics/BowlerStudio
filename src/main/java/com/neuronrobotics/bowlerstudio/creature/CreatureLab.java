package com.neuronrobotics.bowlerstudio.creature;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.BowlerStudioModularFrame;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.sdk.addons.gamepad.BowlerJInputDevice;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.IDriveEngine;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.util.ThreadUtil;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos; 
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class CreatureLab extends AbstractBowlerStudioTab implements IOnEngineeringUnitsChange {

	private BowlerAbstractDevice pm;
	private boolean enabled=true;

	private IDriveEngine defaultDriveEngine;
	// private DhInverseSolver defaultDHSolver;
	private Menu localMenue;
	private ProgressIndicator pi;

	private MobileBaseCadManager baseManager;
	private CheckBox autoRegen = new CheckBox("Auto");
	private Button regen=new Button("Generate Vitamins Now");
	Parent root;
	private BowlerJInputDevice gameController = null;
	CreatureLabControlsTab tab = new CreatureLabControlsTab();

	private long timeSinceLastUpdate = 0;

	private GridPane radioOptions;
	private long timeOfLastDisable=0;

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

		disable();
		autoRegen.setOnAction(event -> {
			baseManager.setAutoRegen(autoRegen.isSelected());
			//BowlerStudio.runLater(() -> {
				regenFromUiEvent();
			//});
		});
		regen.setOnAction(event -> {
			autoRegen.setSelected(true);
			baseManager.setAutoRegen(true);
			//BowlerStudio.runLater(()->{
				regenFromUiEvent();
			//});
		});
		regen.setGraphic(AssetFactory.loadIcon("Generate-Cad.png"));
		// TODO Auto-generated method stub
		setText(pm.getScriptingName());


		MobileBase device = (MobileBase) pm;

		// Button save = new Button("Save Configuration");

		FXMLLoader loader;
		try {
			loader = AssetFactory.loadLayout("layout/CreatureLabControlsTab.fxml", true);
			BowlerStudio.runLater(() -> {
				loader.setController(tab);
				// This is needed when loading on MAC
				loader.setClassLoader(getClass().getClassLoader());
				try {
					root = loader.load();
					setContent(root);
					new Thread(() -> {
						finishLoading(device);
					}).start();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			ThreadUtil.wait(16);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while (getContent() == null)
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	private void regenFromUiEvent() {
		if (System.currentTimeMillis() - timeSinceLastUpdate < 500) {
			return;
		}
		System.out.println("Regenerating robot "+System.currentTimeMillis());
		timeSinceLastUpdate = System.currentTimeMillis();
		BowlerStudio.runLater(() -> {
			if (autoRegen.isSelected()) {
				disable();
				generateCad();
			}
		});
	}



	private void finishLoading(MobileBase device) {

		TreeItem<String> rootItem = null;
		TreeItem<String> mainBase = null;
		int count = 1;
		for (DHParameterKinematics kin : device.getAllDHChains()) {
			for (int i = 0; i < kin.getNumberOfLinks(); i++) {
				DHLink dhLink = kin.getDhLink(i);
				if (dhLink.getSlaveMobileBase() != null) {
					count++;
				}
			}
		}
		try {
			rootItem = new TreeItem<String>("Mobile Bases", AssetFactory.loadIcon("creature.png"));
			mainBase = new TreeItem<String>(device.getScriptingName(), AssetFactory.loadIcon("creature.png"));
		} catch (Exception e) {
			rootItem = new TreeItem<String>(device.getScriptingName());
		}
		if (count == 1) {
			rootItem = mainBase;
		} else {
			rootItem.getChildren().add(mainBase);
		}
		TreeItem<String> rootItemFinal = rootItem;
		TreeItem<String> mainBaseFinal = mainBase;
		AnchorPane treebox1 = tab.getTreeBox();
		// @JansenSmith - placed contents in llambda runnable - 20220915
		BowlerStudio.runLater(() -> {
			TreeView<String> tree = new TreeView<>(rootItemFinal);
			treebox1.getChildren().clear();
			treebox1.getChildren().add(tree);
			AnchorPane.setTopAnchor(tree, 0.0);
			AnchorPane.setLeftAnchor(tree, 0.0);
			AnchorPane.setRightAnchor(tree, 0.0);
			AnchorPane.setBottomAnchor(tree, 0.0);

			HashMap<TreeItem<String>, Runnable> callbackMapForTreeitems = new HashMap<>();
			HashMap<TreeItem<String>, Group> widgetMapForTreeitems = new HashMap<>();
			File source;
			boolean creatureIsOwnedByUser = false;
			try {
				source = ScriptingEngine.fileFromGit(device.getGitSelfSource()[0], device.getGitSelfSource()[1]);
				creatureIsOwnedByUser = ScriptingEngine.checkOwner(source);
			} catch (Exception e) {
				e.printStackTrace();

			}

			rootItemFinal.setExpanded(true);
			MobileBaseCadManager.get(device, BowlerStudioController.getMobileBaseUI());
			MobleBaseMenueFactory.load(device, tree, mainBaseFinal, callbackMapForTreeitems, widgetMapForTreeitems,
					this, true, creatureIsOwnedByUser);
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
							AnchorPane box = hasWalking(device) ? tab.getControlsBox() : tab.getWalkingBox();
							if (widgetMapForTreeitems.get(treeItem) != null) {

								BowlerStudio.runLater(() -> {
									box.getChildren().clear();
									Group g = widgetMapForTreeitems.get(treeItem);
									box.getChildren().add(g);
									AnchorPane.setTopAnchor(g, 0.0);
									AnchorPane.setLeftAnchor(g, 0.0);
									AnchorPane.setRightAnchor(g, 0.0);
									AnchorPane.setBottomAnchor(g, 0.0);
								});
							} else {
								BowlerStudio.runLater(() -> {
									box.getChildren().clear();
									if (hasWalking(device)) {
										tab.setOverlayTopRight(walkWidget);
									}
								});
							}
						}

					}.start();

				}
			});
			if (hasWalking(device)) {
				tab.setOverlayTopRight(walkWidget);
			}
		});
		//VBox progress = new VBox(10);

		final ToggleGroup group = new ToggleGroup();

		RadioButton rb1 = new RadioButton();
		rb1.setToggleGroup(group);
		rb1.setSelected(true);
		rb1.setOnAction(event -> {
				disable();
			//autoRegen.setText("Auto-Generate CAD");
			regen.setText("Generate CAD Now");
			
			BowlerStudio.runLater(() ->setCadMode(false));
		});
		regen.setMinWidth(120);

		RadioButton rb2 = new RadioButton();
		rb2.setToggleGroup(group);
		rb2.fire();
		rb2.setOnAction(event -> {
				disable();
		
			//autoRegen.setText("Auto-Generate Vitamins");
			regen.setText("Generate Vitamins Now");
			BowlerStudio.runLater(() ->setCadMode(true));
		});

		radioOptions = new GridPane();
		radioOptions.setPadding(new Insets(10, 10, 10, 10));
		radioOptions.setVgap(5);
		radioOptions.setHgap(5);

		// Setting the Grid alignment
		radioOptions.setAlignment(Pos.CENTER);
		radioOptions.add(new Label("Select Display Mode:"), 0, 0);
		radioOptions.add(new Label("Cad Generation"), 0, 1);
		radioOptions.add(rb1, 1, 1);
		
		radioOptions.add(new Label("Vitamins View"), 0, 2);
		radioOptions.add(rb2, 1, 2);

		pi = new ProgressIndicator(0);
		baseManager = MobileBaseCadManager.get(device, BowlerStudioController.getMobileBaseUI());
		//pi.progressProperty().bindBidirectional(baseManager.getProcesIndictor());
		new Thread(()->{
			while(device.isAvailable()) {
				double d = baseManager.getProcesIndictor().get();
				pi.setProgress(d);
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {
					return;
				}
				if(!enabled)
					if(d>0.999) {
						enabled=true;
							enable();
						
					}
			}
		}).start();
		//HBox progressIndicatorPanel = new HBox(10);
		radioOptions.add(pi, 1, 0);
		radioOptions.add(autoRegen, 1, 3);
		radioOptions.add(regen,0,3);
		
		//progress.getChildren().addAll( regen,autoRegen, radioOptions);
//		progressIndicatorPanel.getChildren().addAll( radioOptions,pi);
//
//		progressIndicatorPanel.setStyle("-fx-background-color: #FFFFFF;");
//		progressIndicatorPanel.setOpacity(.7);
//		progressIndicatorPanel.setMinHeight(100);
//		progressIndicatorPanel.setPrefSize(325, 150);
		tab.setOverlayTop(radioOptions);

		BowlerStudioModularFrame.getBowlerStudioModularFrame().showCreatureLab();
		setCadMode(true);// start the UI in config mode
		generateCad();

//		pi.progressProperty().addListener((observable,  oldValue,  newValue)-> {
//				//System.out.println("Progress listener " + newValue);
//				if (newValue.doubleValue() > 0.99) {
//					BowlerStudio.runLater(() -> {
//						enable();
//					});
//				}else {
//					BowlerStudio.runLater(() -> {
//						disable();
//					});
//				}
//			
//		});
	}
	private void disable() {
		
		enabled=false;
		if(baseManager!=null)
			baseManager.getProcesIndictor().set(0);
		BowlerStudio.runLater(() -> {
			autoRegen.setDisable(true);
			if (radioOptions != null)
				radioOptions.setDisable(true);
			regen.setDisable(true);
		});
	}
	private void enable() {
		enabled=true;
		BowlerStudio.runLater(() -> {
			autoRegen.setDisable(false);
			if (radioOptions != null)
				radioOptions.setDisable(false);
			regen.setDisable(false);
		});
	}
	private boolean hasWalking(MobileBase device) {
		return device.getLegs().size() > 0 || device.getSteerable().size() > 0 || device.getDrivable().size() > 0;
	}

	private void setCadMode(boolean mode) {
		new Thread(() -> {
			baseManager.setConfigurationViewerMode(mode);
			regenFromUiEvent();
		}).start();

	}

	public void generateCad() {
		disable();
		
		baseManager.generateCadWithEnd(()->{
			
				enable();
			
		});
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
