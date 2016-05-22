package com.neuronrobotics.bowlerstudio;
/**
 * Sample Skeleton for "BowlerStudioModularFrame.fxml" Controller Class
 * You can copy and paste this code into your favorite IDE
 **/

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.UIManager;

import org.dockfx.DockNode;
import org.dockfx.DockPane;
import org.dockfx.DockPos;
import org.eclipse.jgit.api.errors.GitAPIException;

import com.neuronrobotics.bowlerkernel.BowlerKernelBuildInfo;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.assets.BowlerStudioResourceFactory;
import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingFileWidget;
import com.neuronrobotics.bowlerstudio.tabs.LocalFileScriptTab;
import com.neuronrobotics.bowlerstudio.tabs.WebTab;
import com.neuronrobotics.bowlerstudio.threed.BowlerStudio3dEngine;
import com.neuronrobotics.bowlerstudio.twod.TwoDCad;
import com.neuronrobotics.imageprovider.NativeResource;
import com.neuronrobotics.imageprovider.OpenCVJNILoader;
import com.neuronrobotics.javacad.JavaCadBuildInfo;
import com.neuronrobotics.replicator.driver.Slic3r;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.DeviceManager;
import com.neuronrobotics.sdk.common.IDeviceAddedListener;
import com.neuronrobotics.sdk.config.SDKBuildInfo;
import com.neuronrobotics.sdk.util.ThreadUtil;

import eu.mihosoft.vrl.v3d.parametrics.CSGDatabase;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.MenuBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jmapps.export.PanelMediaTargetFormat;

public class BowlerStudioModularFrame {

	@FXML // ResourceBundle that was given to the FXMLLoader
	private ResourceBundle resources;

	@FXML // URL location of the FXML file that was given to the FXMLLoader
	private URL location;

	@FXML // fx:id="editorContainer"
	private AnchorPane editorContainer; // Value injected by FXMLLoader

	@FXML // fx:id="menurAnchor"
	private AnchorPane menurAnchor; // Value injected by FXMLLoader

	private Image dockImage;

	private DockNode tutorialDockNode;

	private DockNode connectionManagerDockNode;

	private DockPane dockPane;

	private BowlerStudioMenu menueController;

	//private InvalidationListener connectionManagerRemover;

	private CreatureLab3dController creatureLab3dController;

	private DockNode WindowLoader3dDockNode;

	//private InvalidationListener creatureManagerRemover;

	private BowlerStudio3dEngine jfx3dmanager;

	private BowlerStudioController controller;

	private static Stage primaryStage;

	private static BowlerStudioModularFrame bowlerStudioModularFrame;

	private HashMap<Tab, DockNode> webTabs = new HashMap<>();

	@FXML // This method is called by the FXMLLoader when initialization is
			// complete
	void initialize() throws Exception {
		try {
			assert editorContainer != null : "fx:id=\"editorContainer\" was not injected: check your FXML file 'BowlerStudioModularFrame.fxml'.";
			assert menurAnchor != null : "fx:id=\"menurAnchor\" was not injected: check your FXML file 'BowlerStudioModularFrame.fxml'.";
			dockPane = new DockPane();
			dockImage = AssetFactory.loadAsset("BowlerStudioModularFrameIcon.png");
//			final Tab newtab = new Tab();
//			newtab.setText("");
//			newtab.setClosable(false);
//			newtab.setGraphic(AssetFactory.loadIcon("New-Web-Tab.png"));
			String homeURL = Tutorial.getHomeUrl();
			jfx3dmanager = new BowlerStudio3dEngine();
			controller = new BowlerStudioController(jfx3dmanager);
			WebTab.setController(controller);

			WebTab webtab = null;
			try {

				webtab = new WebTab("Tutorial", homeURL, true);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			setTutorialDockNode(new DockNode(webtab.getContent(), webtab.getText(), webtab.getGraphic()));
			getTutorialDockNode().setPrefSize(1024, 730);

			connectionManagerDockNode = new DockNode(ConnectionManager.getConnectionManager().getContent(),
					ConnectionManager.getConnectionManager().getText(),
					ConnectionManager.getConnectionManager().getGraphic());
			connectionManagerDockNode.setPrefSize(200, 700);

			// Initial docked setup
			addTutorial();

			// Add the dock pane to the window
			editorContainer.getChildren().add(dockPane);
			AnchorPane.setTopAnchor(dockPane, 0.0);
			AnchorPane.setRightAnchor(dockPane, 0.0);
			AnchorPane.setLeftAnchor(dockPane, 0.0);
			AnchorPane.setBottomAnchor(dockPane, 0.0);

			// test the look and feel with both Caspian and Modena
			Application.setUserAgentStylesheet(Application.STYLESHEET_CASPIAN);
			// initialize the default styles for the dock pane and undocked
			// nodes using the DockFX
			// library's internal Default.css stylesheet
			// unlike other custom control libraries this allows the user to
			// override them globally
			// using the style manager just as they can with internal JavaFX
			// controls
			// this must be called after the primary stage is shown
			// https://bugs.openjdk.java.net/browse/JDK-8132900
			DockPane.initializeDefaultUserAgentStylesheet();
			FXMLLoader WindowLoader3d;
			WindowLoader3d = AssetFactory.loadLayout("layout/CreatureLab.fxml");
			creatureLab3dController = new CreatureLab3dController(jfx3dmanager);
			BowlerStudio.setCreatureLab3d(creatureLab3dController);
			WindowLoader3d.setController(creatureLab3dController);
			WindowLoader3d.setClassLoader(CreatureLab3dController.class.getClassLoader());

			FXMLLoader menueBar;
			menueBar = AssetFactory.loadLayout("layout/BowlerStudioMenuBar.fxml");
			menueController = new BowlerStudioMenu(this);
			menueBar.setController(menueController);
			menueBar.setClassLoader(BowlerStudioMenu.class.getClassLoader());

			try {
				menueBar.load();
				WindowLoader3d.load();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			BorderPane menue = (BorderPane) menueBar.getRoot();
			BorderPane threed = (BorderPane) WindowLoader3d.getRoot();
			WindowLoader3dDockNode = new DockNode(threed, "Creature Lab",
					AssetFactory.loadIcon("CreatureLabDockWidget.png"));
			WindowLoader3dDockNode.setPrefSize(400, 400);

			// Add the dock pane to the window
			menurAnchor.getChildren().add(menue);
			AnchorPane.setTopAnchor(menue, 0.0);
			AnchorPane.setRightAnchor(menue, 0.0);
			AnchorPane.setLeftAnchor(menue, 0.0);
			AnchorPane.setBottomAnchor(menue, 0.0);

			if (ScriptingEngine.getCreds().exists()) {
				if ((boolean) ConfigurationDatabase.getObject("BowlerStudioConfigs", "showCreatureLab", false)){
					ConfigurationDatabase.setObject("BowlerStudioConfigs", "showCreatureLab", false);//bypass the already open check for startup
					showCreatureLab();
				}
				if ((boolean) ConfigurationDatabase.getObject("BowlerStudioConfigs", "showDevices", false)){
					ConfigurationDatabase.setObject("BowlerStudioConfigs", "showDevices", false);//bypass the already open check for startup
					showConectionManager();
				}
			}

			// focus on the tutorial to start
			Platform.runLater(() -> getTutorialDockNode().requestFocus());

		} catch (Exception | Error e) {
			e.printStackTrace();
		}

	}

	private void addTutorial() {
		Platform.runLater(() -> getTutorialDockNode().dock(dockPane, DockPos.LEFT));

	}

	public void showConectionManager() {
		if (!(boolean)ConfigurationDatabase.getParamMap("BowlerStudioConfigs").get("showDevices"))
			Platform.runLater(() -> {
				connectionManagerDockNode.dock(dockPane, DockPos.BOTTOM, getTutorialDockNode());
				connectionManagerDockNode.requestFocus();

				if (ScriptingEngine.getCreds().exists()) {
					ConfigurationDatabase.setObject("BowlerStudioConfigs", "showDevices", true);
				}
		
				connectionManagerDockNode.closedProperty().addListener(new InvalidationListener() {
					@Override
					public void invalidated(Observable event) {
						if (ScriptingEngine.getCreds().exists()) {
							//System.err.println("Closing devices");
							ConfigurationDatabase.setObject("BowlerStudioConfigs", "showDevices", false);
						}
						connectionManagerDockNode.closedProperty().removeListener(this);
					}
				});

			});
		Platform.runLater(() ->connectionManagerDockNode.requestFocus());
	}

	public void showCreatureLab() {
		if (!(boolean) ConfigurationDatabase.getParamMap("BowlerStudioConfigs").get("showCreatureLab"))
			Platform.runLater(() -> {
				WindowLoader3dDockNode.dock(dockPane, DockPos.RIGHT);
				WindowLoader3dDockNode.requestFocus();

				if (ScriptingEngine.getCreds().exists()) {
					ConfigurationDatabase.setObject("BowlerStudioConfigs", "showCreatureLab", true);
				}
				
				WindowLoader3dDockNode.closedProperty().addListener(new InvalidationListener() {
					@Override
					public void invalidated(Observable event) {
						if (ScriptingEngine.getCreds().exists()) {

							ConfigurationDatabase.setObject("BowlerStudioConfigs", "showCreatureLab", false);
						}
						connectionManagerDockNode.closedProperty().removeListener(this);
					}
				});

			});
		Platform.runLater(() -> WindowLoader3dDockNode.requestFocus());
			
	}

	public DockNode getTutorialDockNode() {

		return tutorialDockNode;
	}

	public void setTutorialDockNode(DockNode tutorialDockNode) {
		this.tutorialDockNode = tutorialDockNode;
		tutorialDockNode.closableProperty().set(false);
	}

	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	public static void setPrimaryStage(Stage primaryStage) {
		BowlerStudioModularFrame.primaryStage = primaryStage;
	}

	public ScriptingFileWidget createFileTab(File file) {
		// TODO Auto-generated method stub
		return controller.createFileTab(file);
	}

	public void openUrlInNewTab(URL url) {
		Platform.runLater(() -> {
			try {
				if (ScriptingEngine.getLoginID() != null) {
					WebTab newTab = new WebTab("Web", url.toExternalForm(), false);

					addTab(newTab, true);
				}
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	public void addTab(Tab newTab, boolean b) {
		System.err.println("Loading a new tab: " + newTab.getText());
		if (webTabs.get(newTab) != null) {
			Platform.runLater(() -> webTabs.get(newTab).requestFocus());
		} else {
			DockNode dn = new DockNode(newTab.getContent(), newTab.getText(), newTab.getGraphic());
			dn.closedProperty().addListener(event -> {
				System.err.println("Closing tab: " + newTab.getText());
				webTabs.remove(newTab);
				if (newTab.getOnCloseRequest() != null) {

					newTab.getOnCloseRequest().handle(null);
				}
			});
			webTabs.put(newTab, dn);
			Platform.runLater(() -> {
				dn.dock(dockPane, DockPos.CENTER, getTutorialDockNode());
				// dn.setFloating(true);
			});
		}
	}

	public static BowlerStudioModularFrame getBowlerStudioModularFrame() {
		return bowlerStudioModularFrame;
	}

	public static void setBowlerStudioModularFrame(BowlerStudioModularFrame bowlerStudioModularFrame) {
		BowlerStudioModularFrame.bowlerStudioModularFrame = bowlerStudioModularFrame;
	}

	public void setSelectedTab(Tab tab) {
		// TODO Auto-generated method stub
		Platform.runLater(() -> webTabs.get(tab).requestFocus());
	}

}
