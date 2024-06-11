package com.neuronrobotics.bowlerstudio;
/**
 * Sample Skeleton for "BowlerStudioModularFrame.fxml" Controller Class
 * You can copy and paste this code into your favorite IDE
 **/

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.PasswordManager;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingFileWidget;
import com.neuronrobotics.bowlerstudio.tabs.WebTab;
import com.neuronrobotics.bowlerstudio.threed.BowlerStudio3dEngine;
import com.neuronrobotics.sdk.util.ThreadUtil;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.dockfx.DockNode;
import org.dockfx.DockPane;
import org.dockfx.DockPos;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
@SuppressWarnings("restriction")
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

	public BowlerStudioMenu menueController;

	// private InvalidationListener connectionManagerRemover;

	private CreatureLab3dController creatureLab3dController;

	private DockNode creatureLab3dDockNode=null;

	// private InvalidationListener creatureManagerRemover;


	private BowlerStudioController controller;

	private static Stage primaryStage;

	private static BowlerStudioModularFrame bowlerStudioModularFrame;

	private HashMap<Tab, DockNode> webTabs = new HashMap<>();
	private HashMap<String, Boolean> isOpen = new HashMap<>();

	private Terminal terminal;

	private DockNode terminalDockNode;
	private boolean startup = false;

	private WebTab webtab;

	@FXML // This method is called by the FXMLLoader when initialization is
			// complete
	void initialize() throws Exception {
		bowlerStudioModularFrame=this;
		assert editorContainer != null : "fx:id=\"editorContainer\" was not injected: check your FXML file 'BowlerStudioModularFrame.fxml'.";
		assert menurAnchor != null : "fx:id=\"menurAnchor\" was not injected: check your FXML file 'BowlerStudioModularFrame.fxml'.";
		dockPane = new DockPane();
		dockImage = AssetFactory.loadAsset("BowlerStudioModularFrameIcon.png");
		// final Tab newtab = new Tab();
		// newtab.setText("");
		// newtab.setClosable(false);
		// newtab.setGraphic(AssetFactory.loadIcon("New-Web-Tab.png"));
		String homeURL = Tutorial.getHomeUrl();
		

		controller = new BowlerStudioController();
		WebTab.setBSController(controller);

		 webtab = null;
		try {

			BowlerStudio.runLater(()->{
				try {
					webtab = new WebTab("Documentation", homeURL, true);
					setTutorialDockNode(new DockNode(webtab.getContent(), webtab.getText(), webtab.getGraphic()));
					getTutorialDockNode().setPrefSize(1024, 730);
				} catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			});;
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		

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

		FXMLLoader WindowLoader3d;
		WindowLoader3d = AssetFactory.loadLayout("layout/CreatureLab.fxml");
		creatureLab3dController = new CreatureLab3dController();
		BowlerStudio.setCreatureLab3d(creatureLab3dController);
		WindowLoader3d.setController(creatureLab3dController);
		WindowLoader3d.setClassLoader(CreatureLab3dController.class.getClassLoader());
		FXMLLoader commandLine;
		commandLine = AssetFactory.loadLayout("layout/Terminal.fxml");
		terminal = new Terminal();
		commandLine.setController(terminal);
		commandLine.setClassLoader(Terminal.class.getClassLoader());
		FXMLLoader menueBar;
		menueBar = AssetFactory.loadLayout("layout/BowlerStudioMenuBar.fxml");
		menueController = new BowlerStudioMenu(this,creatureLab3dController);
		menueBar.setController(menueController);
		menueBar.setClassLoader(BowlerStudioMenu.class.getClassLoader());
		
		

		try {
			menueBar.load();
			WindowLoader3d.load();
			commandLine.load();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		BorderPane menue = (BorderPane) menueBar.getRoot();
		BorderPane threed = (BorderPane) WindowLoader3d.getRoot();
		VBox cmd = (VBox) commandLine.getRoot();
		do {
			try {
				creatureLab3dDockNode = new DockNode(threed, "Creature Lab", AssetFactory.loadIcon("CreatureLab-Tab.png"));
			}catch(Exception  e) {
				e.printStackTrace();
				ThreadUtil.wait(100);
			}
		} while (creatureLab3dDockNode==null);
		creatureLab3dDockNode.setPrefSize(500, 500);
		


		terminalDockNode = new DockNode(cmd, "Terminal", AssetFactory.loadIcon("Command-Line.png"));
		terminalDockNode.setPrefSize(400, 400);

		// Add the dock pane to the window
		menurAnchor.getChildren().add(menue);
		AnchorPane.setTopAnchor(menue, 0.0);
		AnchorPane.setRightAnchor(menue, 0.0);
		AnchorPane.setLeftAnchor(menue, 0.0);
		AnchorPane.setBottomAnchor(menue, 0.0);
		isOpen.put("showCreatureLab", false);
		isOpen.put("showTerminal", false);
		isOpen.put("showDevices", false);

		// focus on the tutorial to start
		BowlerStudio.runLater(() -> getTutorialDockNode().requestFocus());
		connectionManagerDockNode.onMouseClickedProperty().addListener((a, b, c) -> {
			System.err.println("Cloick");
		});

	}

	public void loadMobilebaseFromGit(String id, String file) {
		menueController.loadMobilebaseFromGit(id, file);
	}

	private void addTutorial() {
		BowlerStudio.runLater(() -> getTutorialDockNode().dock(dockPane, DockPos.LEFT));

	}

	public void showConectionManager() {
		String key = "showDevices";
		if (isOpen.get(key) == null) {
			isOpen.put(key, false);
		}
		if (isOpen.get("showTerminal") == null) {
			isOpen.put("showTerminal", false);
		}
		BowlerStudio.runLater(() -> {
			if (!isOpen.get(key)) {
				isOpen.put(key, true);
				if (isOpen.get("showTerminal"))
					connectionManagerDockNode.dock(dockPane, DockPos.RIGHT, terminalDockNode);
				else
					connectionManagerDockNode.dock(dockPane, DockPos.BOTTOM, getTutorialDockNode());
				connectionManagerDockNode.requestFocus();

				connectionManagerDockNode.closedProperty().addListener(new InvalidationListener() {
					@Override
					public void invalidated(Observable event) {
						connectionManagerDockNode.closedProperty().removeListener(this);
						isOpen.put(key, false);
					}
				});

			}
			BowlerStudio.runLater(() -> connectionManagerDockNode.requestFocus());
		});

	}

	public void showTerminal() {

		String key = "showTerminal";
		if (isOpen.get(key) == null || getTutorialDockNode()==null ||terminalDockNode==null) {
			isOpen.put(key, false);
			return;
		}
		if (isOpen.get("showDevices") == null) {
			isOpen.put("showDevices", false);
		}
		if (!isOpen.get(key)) {
			isOpen.put(key, true);
			BowlerStudio.runLater(() -> {

				if (isOpen.get("showDevices"))
					terminalDockNode.dock(dockPane, DockPos.LEFT, connectionManagerDockNode);
				else
					terminalDockNode.dock(dockPane, DockPos.BOTTOM, getTutorialDockNode());
				terminalDockNode.requestFocus();

				if (ScriptingEngine.isLoginSuccess()) {

				}

				terminalDockNode.closedProperty().addListener(new InvalidationListener() {
					@Override
					public void invalidated(Observable event) {
						terminalDockNode.closedProperty().removeListener(this);
						isOpen.put(key, false);
					}
				});

				BowlerStudio.runLater(() -> terminalDockNode.requestFocus());

			});

		}
	}

	public void showCreatureLab() {
		showCreatureLab(0);
	}

	public void showCreatureLab(int depth) {
		String key = "showCreatureLab";
		Boolean boolean1 = isOpen.get(key);
		if(boolean1!=null)
		if (!boolean1) {
			isOpen.put(key, true);
			new Thread(() -> {
				//ThreadUtil.wait(100);

				BowlerStudio.runLater(() -> {
					try {
						creatureLab3dDockNode.dock(dockPane, DockPos.RIGHT);
						isOpen.put(key, true);
						return;
					} catch (NullPointerException e) {
						// keep trying to open
						// e.printStackTrace();
						isOpen.put(key, false);
						if (depth < 2) {
							showCreatureLab(depth + 1);
						} else
							BowlerStudio.printStackTrace(e);// fail and show
															// user
					} catch (Exception e) {
						isOpen.put(key, false);
						BowlerStudio.printStackTrace(e);// fail and show user
					}

				});

				BowlerStudio.runLater(() -> creatureLab3dDockNode.closedProperty().addListener(new InvalidationListener() {
					@Override
					public void invalidated(Observable event) {
						creatureLab3dDockNode.closedProperty().removeListener(this);
						isOpen.put(key, false);
					}
				}));

			}).start();
		}
		// else
		// BowlerStudio.runLater(() -> creatureLab3dDockNode.requestFocus());

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
		BowlerStudio.runLater(() -> {
			try {
				if (PasswordManager.getUsername()  != null) {
					WebTab newTab = new WebTab("Web", url.toExternalForm(), false);

					addTab(newTab, true);
				}
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}
	
	public void closeTab(Tab newTab) {
		if (webTabs.get(newTab) != null) {
			System.err.println("Closing tab: " + newTab.getText());
			DockNode dn=webTabs.get(newTab);
			webTabs.remove(newTab);
			if(dn!=null)
				BowlerStudio.runLater(() -> {
					try {
						dn.undock();
					}catch(java.lang.NullPointerException ex) {}
					BowlerStudio.runLater(() -> dn.close());
					if(newTab!=null)
						if (newTab.getOnCloseRequest() != null) {
							newTab.getOnCloseRequest().handle(null);
						}
				});
		} 
	}

	public void addTab(Tab newTab, boolean b) {
		System.err.println("Loading a new tab: " + newTab.getText());
		if (webTabs.get(newTab) != null) {
			BowlerStudio.runLater(() -> webTabs.get(newTab).requestFocus());
		} else {
			//BowlerStudio.runLater(() -> {
				if(newTab.getGraphic()==null) {
					newTab.setGraphic(AssetFactory.loadIcon("Hardware-Config.png"));
				}
				if(newTab.getContent()==null) {
					throw new RuntimeException("Tabs must have content before loading");
				}
				DockNode dn = new DockNode(newTab.getContent(), newTab.getText(), newTab.getGraphic());
				dn.closedProperty().addListener(event -> {
					BowlerStudio.runLater(() ->closeTab(newTab));
				});
	
				webTabs.put(newTab, dn);
				dn.dock(dockPane, DockPos.CENTER, getTutorialDockNode());
				
			//});
		}
	}

	public static BowlerStudioModularFrame getBowlerStudioModularFrame() {
		return bowlerStudioModularFrame;
	}


	public void setSelectedTab(Tab tab) {
		DockNode dockNode = webTabs.get(tab);
		if (dockNode != null)
			BowlerStudio.runLater(() -> {
				dockNode.requestFocus();
			});
	}



}
