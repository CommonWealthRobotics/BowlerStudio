package com.neuronrobotics.bowlerstudio;

import eu.mihosoft.vrl.v3d.CSG;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

//import org.bytedeco.javacpp.DoublePointer;












import org.reactfx.util.FxTimer;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Affine;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

import com.neuronrobotics.bowlerstudio.scripting.IScriptEventListener;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngineWidget;
import com.neuronrobotics.bowlerstudio.tabs.LocalFileScriptTab;
import com.neuronrobotics.bowlerstudio.tabs.ScriptingGistTab;
import com.neuronrobotics.imageprovider.AbstractImageProvider;
import com.neuronrobotics.imageprovider.Detection;
import com.neuronrobotics.imageprovider.HaarDetector;
import com.neuronrobotics.imageprovider.IObjectDetector;
import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.nrconsole.util.GroovyFilter;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.gui.Jfx3dManager;
import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.IConnectionEventListener;
import com.neuronrobotics.sdk.common.IDeviceConnectionEventListener;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.util.RollingAverageFilter;
import com.neuronrobotics.sdk.util.ThreadUtil;
import com.sun.javafx.scene.control.behavior.TabPaneBehavior;
import com.sun.javafx.scene.control.skin.TabPaneSkin;

public class BowlerStudioController extends TabPane implements
		IScriptEventListener {

	private static final String HOME_URL = "http://neuronrobotics.com/BowlerStudio/Welcome-To-BowlerStudio/";
	/**
	 * 
	 */
	private static final long serialVersionUID = -2686618188618431477L;
	private ConnectionManager connectionManager;
	private Jfx3dManager jfx3dmanager;
	private MainController mainController;
	private AbstractImageProvider vrCamera;
	private static BowlerStudioController bowlerStudio=null;
	private Stage dialog = new Stage();
	public BowlerStudioController(Jfx3dManager jfx3dmanager,
			MainController mainController) {
		if(getBowlerStudio()!=null)
			throw new RuntimeException("There can be only one Bowler Studio controller");
		setBowlerStudio(this);
		this.jfx3dmanager = jfx3dmanager;
		this.mainController = mainController;
		createScene();
	}

	// Custom function for creation of New Tabs.
	public ScriptingEngineWidget createFileTab(File file) {
		Tab fileTab =new Tab(file.getName());
		
		try {
			Log.warning("Loading local file from: "+file.getAbsolutePath());
			LocalFileScriptTab t  =new LocalFileScriptTab( file);
			fileTab.setContent(t);
			addTab(fileTab, true);
			return t.getScripting();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	// Custom function for creation of New Tabs.
	private void createAndSelectNewTab(final BowlerStudioController tabPane,
			final String title) {


			Platform.runLater(() -> {
				try {
					if(ScriptingEngine.getLoginID() != null)
						
						addTab(new ScriptingGistTab(title,getHomeUrl(), true), false);
				} catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});


	}
	
	public void openUrlInNewTab(URL url){
		Platform.runLater(() -> {
			try {
				if(ScriptingEngine.getLoginID() != null)
					
					addTab(new ScriptingGistTab("Web",url.toExternalForm(), false), true);
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	private Tab createTab() throws IOException, InterruptedException {
		final ScriptingGistTab tab = new ScriptingGistTab(null,
				 null);

		return tab;
	}

	public void addTab(Tab tab, boolean closable) {

		//new RuntimeException().printStackTrace();

		Platform.runLater(() -> {
			final ObservableList<Tab> tabs = getTabs();
			tab.setClosable(closable);
			int index = tabs.size() - 1;
			//new RuntimeException().printStackTrace();
			tabs.add(index, tab);
			setSelectedTab(tab);
		});
		
	}

	public void createScene() {

		// BorderPane borderPane = new BorderPane();

		// Placement of TabPane.
		setSide(Side.TOP);

		/*
		 * To disable closing of tabs.
		 * tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
		 */

		final Tab newtab = new Tab();
		newtab.setText("+");
		newtab.setClosable(false);

		// Addition of New Tab to the tabpane.
		getTabs().addAll(newtab);

		Tab t=new Tab();
		try {
			t = new ScriptingGistTab("Tutorial",getHomeUrl(), true);
		} catch (IOException | InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Tab tab =t;
		Platform.runLater(() -> {
			final ObservableList<Tab> tabs = getTabs();
			ConnectionManager.getConnectionManager().setClosable(false);
			int index = tabs.size() - 1;
			//new RuntimeException().printStackTrace();
			tabs.add(index, ConnectionManager.getConnectionManager());
			setSelectedTab(ConnectionManager.getConnectionManager());

			tab.setClosable(false);
			index = tabs.size() - 1;
			//new RuntimeException().printStackTrace();
			tabs.add(index, tab);
			setSelectedTab(tab);

		});

		// Function to add and display new tabs with default URL display.
		getSelectionModel().selectedItemProperty().addListener(
				new ChangeListener<Tab>() {
					@Override
					public void changed(
							ObservableValue<? extends Tab> observable,
							Tab oldSelectedTab, Tab newSelectedTab) {
						if (newSelectedTab == newtab) {

							try {
								addTab(createTab(), true);

							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}
				});
	}

	public static String getHomeUrl() {

		return HOME_URL;
	}

	public void open() {
		File last = FileSelectionFactory.GetFile(
				ScriptingEngineWidget.getWorkspace(), new ExtensionFilter("Groovy Scripts","*.groovy","*.java","*.txt"));
		if (last != null) {
			createFileTab(last);
		}
	}

	private void removeObject(Object p) {
		if (CSG.class.isInstance(p)) {
			Platform.runLater(() -> {
				jfx3dmanager.removeObjects();
			});
		} else if (Tab.class.isInstance(p)) {
			Platform.runLater(() -> {
				// new RuntimeException().printStackTrace();
				getTabs().remove(p);
				Tab t = (Tab) p;
				TabPaneBehavior behavior = ((TabPaneSkin) getSkin())
						.getBehavior();
				if (behavior.canCloseTab(t)) {
					behavior.closeTab(t);
				}
			});
		}
	}
	
	public static void setCsg(List<CSG> toadd){
		Platform.runLater(() -> {
			getBowlerStudio().jfx3dmanager.removeObjects();
			if(toadd!=null)
			for(CSG c:toadd){
				MeshView current = c.getMesh();
				getBowlerStudio().jfx3dmanager.addObject(current);
			}
		});
	}

	private void addObject(Object o) {
		if (CSG.class.isInstance(o)) {
			CSG csg = (CSG) o;
			MeshView current = csg.getMesh();
			Platform.runLater(() -> {
				// new RuntimeException().printStackTrace();
				jfx3dmanager.addObject(current);
			});
		} else if (Tab.class.isInstance(o)) {

			addTab((Tab) o, true);

		} else if (MeshView.class.isInstance(o)) {
			Platform.runLater(() -> {
				// new RuntimeException().printStackTrace();
				jfx3dmanager.addObject((MeshView) o);
			});
		} else if (BowlerAbstractDevice.class.isInstance(o)) {
			BowlerAbstractDevice bad = (BowlerAbstractDevice) o;
			ConnectionManager.addConnection((BowlerAbstractDevice) o,
					bad.getScriptingName());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onGroovyScriptFinished(Object result, Object Previous) {
		Log.warning("Loading script results " + result + " previous "
				+ Previous);
		// this is added in the script engine when the connection manager is
		// loaded
		if (ArrayList.class.isInstance(Previous)) {
			ArrayList<Object> c = (ArrayList<Object>) Previous;
			for (int i = 0; i < c.size(); i++) {
				removeObject(c.get(i));
			}
		} else {
			removeObject(Previous);
		}
		if (ArrayList.class.isInstance(result)) {
			ArrayList<Object> c = (ArrayList<Object>) result;
			for (int i = 0; i < c.size(); i++) {
				Log.warning("Loading array Lists with removals " + c.get(i));
				addObject(c.get(i));
			}
		} else {
			addObject(result);
		}
	}

	@Override
	public void onGroovyScriptChanged(String previous, String current) {

	}

	@Override
	public void onGroovyScriptError(Exception except) {
		// TODO Auto-generated method stub

	}
//
//	public void onAddDefaultRightArm(ActionEvent event) {
//		if (mainController.getAddDefaultRightArm().isSelected()) {
//			// TODO Auto-generated method stub
//			BowlerAbstractDevice dev = ConnectionManager
//					.pickConnectedDevice(DHParameterKinematics.class);
//			IDeviceConnectionEventListener l = new IDeviceConnectionEventListener() {
//				@Override
//				public void onDisconnect(BowlerAbstractDevice source) {
//					jfx3dmanager.removeArm();
//					mainController.getAddDefaultRightArm().selectedProperty()
//							.set(false);
//				}
//
//				@Override
//				public void onConnect(BowlerAbstractDevice source) {
//				}
//			};
//			if (dev == null) {
//				DyIO tmp = (DyIO) ConnectionManager.pickConnectedDevice(
//						DyIO.class);
//				if (tmp != null) {
//					tmp.addConnectionEventListener(l);
//					dev = new DHParameterKinematics(tmp, "TrobotMaster.xml");
//					ConnectionManager.addConnection(dev, "DHArm");
//				}
//			}
//			if (dev != null) {
//				jfx3dmanager.attachArm((DHParameterKinematics) dev);
//				dev.addConnectionEventListener(l);
//			} else {
//				mainController.getAddDefaultRightArm().selectedProperty()
//						.set(false);
//			}
//		} else {
//			jfx3dmanager.removeArm();
//		}
//	}

//	public void onAddVRCamera(ActionEvent event) {
//		// TODO Auto-generated method stub
//
//		setVrCamera((AbstractImageProvider) ConnectionManager
//				.pickConnectedDevice(AbstractImageProvider.class));
//		if (getVrCamera() == null)
//			setVrCamera(ConnectionManager.onConnectCVCamera());
//		if (getVrCamera() != null) {
//			getVrCamera().addConnectionEventListener(
//					new IDeviceConnectionEventListener() {
//						@Override
//						public void onDisconnect(BowlerAbstractDevice source) {
//							mainController.getAddVRCamera().selectedProperty()
//									.set(false);
//						}
//
//						@Override
//						public void onConnect(BowlerAbstractDevice source) {
//						}
//					});
//			new Thread() {
//				public void run() {
//					setName("Load Camera Thread");
//					IObjectDetector detector = new HaarDetector(
//							"lbpcascade_frontalface.xml");
//					double xSize = 320;
//					double ySize = 240;
//					// Create the input and display images. The display is where
//					// the detector writes its detections overlay on the input
//					// image
//					BufferedImage inputImage = AbstractImageProvider
//							.newBufferImage((int) xSize, (int) ySize);
//					BufferedImage displayImage = AbstractImageProvider
//							.newBufferImage((int) xSize, (int) ySize);
//					System.out.println("Camera VR Started");
//					DoubleProperty view = jfx3dmanager
//							.getCameraFieldOfViewProperty();
//					Affine carmermanipulation = jfx3dmanager.getCameraVR();
//					RollingAverageFilter rollingSize = new RollingAverageFilter(
//							10, 0);
//					RollingAverageFilter rollingX = new RollingAverageFilter(
//							10, 0);
//					RollingAverageFilter rollingY = new RollingAverageFilter(
//							10, 0);
//					while (mainController.getAddVRCamera().isSelected()) {
//						getVrCamera().getLatestImage(inputImage, displayImage); // capture
//																				// image
//						List<Detection> data = detector.getObjects(inputImage,
//								displayImage);
//						if (data.size() > 0) {
//
//							double xWarp = (((xSize / 2) - data.get(0).getX()) / (xSize / 2)) - .5;
//							double yWarp = (((ySize / 2) - data.get(0).getY()) / (ySize / 2)) - .5;
//							double sizeWarp = (((ySize / 2) - data.get(0)
//									.getSize()) / (ySize / 2));
//
//							rollingX.add(xWarp);
//							rollingY.add(yWarp);
//							rollingSize.add(sizeWarp);
//
//							Platform.runLater(() -> {
//								carmermanipulation.setTx(100 * rollingX
//										.getValue());
//								carmermanipulation.setTy(-100
//										* rollingY.getValue());
//								carmermanipulation.setTz(-500
//										* rollingSize.getValue());
//								view.set(30.0 + (-5.0 * rollingSize.getValue()));
//							});
//
//						}
//					}
//					// bail out when the checkbox is unchecked
//					System.out.println("Camera VR disabled");
//				}
//			}.start();
//		}
//	}

	public void disconnect() {
		ConnectionManager.disconnectAll();
	}

	public Stage getPrimaryStage() {
		// TODO Auto-generated method stub
		return BowlerStudio.getPrimaryStage();
	}

	public void setSelectedTab(Tab tab) {
		Platform.runLater(() -> {
			getSelectionModel().select(tab);
		});
	}

	public AbstractImageProvider getVrCamera() {
		return vrCamera;
	}

	public void setVrCamera(AbstractImageProvider vrCamera) {
		this.vrCamera = vrCamera;
	}

	public static BowlerStudioController getBowlerStudio() {
		return bowlerStudio;
	}

	private static void setBowlerStudio(BowlerStudioController bowlerStudio) {
		BowlerStudioController.bowlerStudio = bowlerStudio;
	}

	public static void setup() {
		// TODO Auto-generated method stub
		
	}

}