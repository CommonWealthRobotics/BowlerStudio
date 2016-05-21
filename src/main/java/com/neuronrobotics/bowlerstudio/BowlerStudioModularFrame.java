package com.neuronrobotics.bowlerstudio;
/**
 * Sample Skeleton for "BowlerStudioModularFrame.fxml" Controller Class
 * You can copy and paste this code into your favorite IDE
 **/

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.UIManager;

import org.dockfx.DockNode;
import org.dockfx.DockPane;
import org.dockfx.DockPos;

import com.neuronrobotics.bowlerkernel.BowlerKernelBuildInfo;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.assets.BowlerStudioResourceFactory;
import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingFileWidget;
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

public class BowlerStudioModularFrame extends Application {

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

	private InvalidationListener connectionManagerRemover;

	private CreatureLab3dController creatureLab3dController;

	private DockNode WindowLoader3dDockNode;

	private InvalidationListener creatureManagerRemover;

	private BowlerStudio3dEngine jfx3dmanager;

	private BowlerStudioController controller;

	private static Stage primaryStage;

	private static BowlerStudioModularFrame bowlerStudioModularFrame;

	@FXML // This method is called by the FXMLLoader when initialization is
			// complete
	void initialize() throws Exception {
		try {
			assert editorContainer != null : "fx:id=\"editorContainer\" was not injected: check your FXML file 'BowlerStudioModularFrame.fxml'.";
			assert menurAnchor != null : "fx:id=\"menurAnchor\" was not injected: check your FXML file 'BowlerStudioModularFrame.fxml'.";
			dockPane = new DockPane();
			dockImage = AssetFactory.loadAsset("BowlerStudioModularFrameIcon.png");
			final Tab newtab = new Tab();
			newtab.setText("");
			newtab.setClosable(false);
			newtab.setGraphic(AssetFactory.loadIcon("New-Web-Tab.png"));
			String homeURL = Tutorial.getHomeUrl();
			Tab webtab = new Tab();
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
			DeviceManager.addDeviceAddedListener(new IDeviceAddedListener() {

				@Override
				public void onNewDeviceAdded(BowlerAbstractDevice arg0) {
					showConectionManager();
				}

				@Override
				public void onDeviceRemoved(BowlerAbstractDevice arg0) {}
			});

			// Add the dock pane to the window
			editorContainer.getChildren().add(dockPane);
			AnchorPane.setTopAnchor(dockPane, 0.0);
			AnchorPane.setRightAnchor(dockPane, 0.0);
			AnchorPane.setLeftAnchor(dockPane, 0.0);
			AnchorPane.setBottomAnchor(dockPane, 0.0);

			// test the look and feel with both Caspian and Modena
			Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
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
			creatureLab3dController = new CreatureLab3dController();
			BowlerStudio.setCreatureLab3d(creatureLab3dController);
			WindowLoader3d.setController(creatureLab3dController);
			WindowLoader3d.setClassLoader(CreatureLab3dController.class.getClassLoader());
			
			
			FXMLLoader menueBar;
			menueBar = AssetFactory.loadLayout("layout/BowlerStudioMenuBar.fxml");
			menueController = new BowlerStudioMenu();
			menueController.setTopLevel(this);
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
			BorderPane threed = (BorderPane) menueBar.getRoot();
			WindowLoader3dDockNode = new DockNode(threed,
						"Creature Lab",
						AssetFactory.loadIcon("CreatureLabDockWidget.png"));
			WindowLoader3dDockNode.setPrefSize(400, 400);
			
			// Add the dock pane to the window
			menurAnchor.getChildren().add(menue);
			AnchorPane.setTopAnchor(menue, 0.0);
			AnchorPane.setRightAnchor(menue, 0.0);
			AnchorPane.setLeftAnchor(menue, 0.0);
			AnchorPane.setBottomAnchor(menue, 0.0);
			
			if (ScriptingEngine.getCreds().exists()){
				if( (boolean) ConfigurationDatabase.getObject("BowlerStudioConfigs", "showCreatureLab", false))
					showCreatureLab();
			}
			jfx3dmanager =  new BowlerStudio3dEngine();
			controller = new BowlerStudioController(jfx3dmanager);
			
			
			
			// focus on the tutorial to start
			getTutorialDockNode().requestFocus();
		} catch (Exception | Error e) {
			e.printStackTrace();
		}

	}

	private void addTutorial() {
		Platform.runLater(() -> getTutorialDockNode().dock(dockPane, DockPos.LEFT));

	}

	public void showConectionManager() {
		Platform.runLater(() -> {
			connectionManagerDockNode.dock(dockPane, DockPos.CENTER, getTutorialDockNode());
			connectionManagerDockNode.requestFocus();
			
			if (ScriptingEngine.getCreds().exists()){
				ConfigurationDatabase.setObject("BowlerStudioConfigs", "showDevices", true);
			}
			connectionManagerRemover = new InvalidationListener() {
				@Override
				public void invalidated(Observable event) {
					if (ScriptingEngine.getCreds().exists()){
						ConfigurationDatabase.setObject("BowlerStudioConfigs", "showDevices", false);
					}
					connectionManagerDockNode.closedProperty().removeListener(connectionManagerRemover);	
				}
			};
			connectionManagerDockNode.closedProperty().addListener(connectionManagerRemover);	
			
			
		});
	}
	public void showCreatureLab() {
		Platform.runLater(() -> {
			WindowLoader3dDockNode.dock(dockPane, DockPos.RIGHT);
			WindowLoader3dDockNode.requestFocus();
			
			if (ScriptingEngine.getCreds().exists()){
				ConfigurationDatabase.setObject("BowlerStudioConfigs", "showCreatureLab", true);
			}
			creatureManagerRemover = new InvalidationListener() {
				@Override
				public void invalidated(Observable event) {
					if (ScriptingEngine.getCreds().exists()){
						ConfigurationDatabase.setObject("BowlerStudioConfigs", "showCreatureLab", false);
					}
					connectionManagerDockNode.closedProperty().removeListener(creatureManagerRemover);	
				}
			};
			WindowLoader3dDockNode.closedProperty().addListener(creatureManagerRemover);	
			
			
		});
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		BowlerStudioModularFrame.setBowlerStudioModularFrame(this);
		BowlerStudioModularFrame.setPrimaryStage(primaryStage);
		// Initialize your logic here: all @FXML variables will have been
		// injected
		FXMLLoader mainControllerPanel;

		try {
			mainControllerPanel = AssetFactory.loadLayout("layout/BowlerStudioModularFrame.fxml");
			mainControllerPanel.setController(this);
			mainControllerPanel.setClassLoader(getClass().getClassLoader());
			try {
				mainControllerPanel.load();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Scene scene = new Scene(mainControllerPanel.getRoot(), 1024, 768, true);
			File f = AssetFactory.loadFile("layout/default.css");
			scene.getStylesheets().clear();
			scene.getStylesheets().add("file:///" + f.getAbsolutePath().replace("\\", "/"));

			primaryStage.setTitle("Bowler Studio");
			primaryStage.setScene(scene);
			primaryStage.show();
			primaryStage.setOnCloseRequest(arg0 -> {
				// ThreadUtil.wait(100);
				new Thread(){
					public void run(){
						ConnectionManager.disconnectAll();
						if (ScriptingEngine.getCreds().exists()) 
							ConfigurationDatabase.save();
						System.exit(0);
					}
				}.start();
				
			});

			primaryStage.setResizable(true);

			String firstVer = "";
			if (ScriptingEngine.getCreds().exists())
				firstVer = (String) ConfigurationDatabase.getObject("BowlerStudioConfigs", "firstVersion",
						StudioBuildInfo.getVersion());

			System.out.println("BowlerStudio First Version: " + firstVer);
			System.out.println("Java-Bowler Version: " + SDKBuildInfo.getVersion());
			System.out.println("Bowler-Scripting-Kernel Version: " + BowlerKernelBuildInfo.getVersion());
			System.out.println("JavaCad Version: " + JavaCadBuildInfo.getVersion());
			System.out.println("Welcome to BowlerStudio!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	

	public static void main(String[] args) throws Exception {

		if (args.length == 0) {

			// ScriptingEngine.logout();
			ScriptingEngine.setLoginManager(new GitHubLoginManager());

			if (ScriptingEngine.getCreds().exists()) {
				ScriptingEngine.runLogin();
				if (BowlerStudio.hasNetwork())
					ScriptingEngine.setAutoupdate(true);
			} else
				ScriptingEngine.setupAnyonmous();
			// Download and Load all of the assets
			AssetFactory.loadAsset("BowlerStudio.png");
			BowlerStudioResourceFactory.load();
			// load tutorials repo
			ScriptingEngine.fileFromGit("https://github.com/NeuronRobotics/NeuronRobotics.github.io.git", "index.html");
			ScriptingEngine.fileFromGit("https://github.com/madhephaestus/BowlerStudioExampleRobots.git", // git
																											// repo,
																											// change
																											// this
																											// if
																											// you
																											// fork
																											// this
																											// demo
					"exampleRobots.json"// File from within the Git repo
			);
			CSGDatabase.setDbFile(new File(ScriptingEngine.getWorkspace().getAbsoluteFile() + "/csgDatabase.json"));
			// if (!ScriptingEngine.getCreds().exists()) {
			// ScriptingEngine.logout();
			// }

			// System.out.println("Loading assets ");

			// System.out.println("Loading Main.fxml");

			try {
				OpenCVJNILoader.load(); // Loads the JNI (java native interface)
			} catch (Exception | Error e) {
				// e.printStackTrace();
				// opencvOk=false;
				Platform.runLater(() -> {
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("OpenCV missing");
					alert.setHeaderText("Opencv library is missing");
					alert.setContentText(e.getMessage());
					alert.initModality(Modality.APPLICATION_MODAL);
					alert.show();
					e.printStackTrace(System.out);
				});

			}
			if (NativeResource.isLinux()) {

				Slic3r.setExecutableLocation("/usr/bin/slic3r");

			} else if (NativeResource.isWindows()) {
				String basedir = System.getenv("OPENCV_DIR");
				if (basedir == null)
					throw new RuntimeException(
							"OPENCV_DIR was not found, environment variable OPENCV_DIR needs to be set");
				System.err.println("OPENCV_DIR found at " + basedir);
				basedir += "\\..\\..\\..\\Slic3r_X64\\Slic3r\\slic3r.exe";
				Slic3r.setExecutableLocation(basedir);

			}
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
				// This is a workaround for #8 and is only relavent on osx
				// it causes the SwingNodes not to load if not called way ahead
				// of time
				javafx.scene.text.Font.getFamilies();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			launch(args);
		} else {
			BowlerKernel.main(args);
		}
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
		return null;
	}

	public void openUrlInNewTab(URL url) {
		// TODO Auto-generated method stub
		
	}

	public void addTab(TwoDCad twoDCad, boolean b) {
		// TODO Auto-generated method stub
		
	}

	public static BowlerStudioModularFrame getBowlerStudioModularFrame() {
		return bowlerStudioModularFrame;
	}

	public static void setBowlerStudioModularFrame(BowlerStudioModularFrame bowlerStudioModularFrame) {
		BowlerStudioModularFrame.bowlerStudioModularFrame = bowlerStudioModularFrame;
	}

}
