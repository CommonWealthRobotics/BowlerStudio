package com.neuronrobotics.bowlerstudio;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.MeshContainer;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.MeshView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.Window;

import com.neuronrobotics.bowlerstudio.scripting.IScriptEventListener;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngineWidget;
import com.neuronrobotics.bowlerstudio.tabs.LocalFileScriptTab;
import com.neuronrobotics.bowlerstudio.tabs.ScriptingGistTab;
import com.neuronrobotics.jniloader.AbstractImageProvider;
import com.neuronrobotics.jniloader.HaarDetector;
import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.nrconsole.util.GroovyFilter;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.gui.Jfx3dManager;
import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.IConnectionEventListener;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.util.ThreadUtil;
import com.sun.javafx.scene.control.behavior.TabPaneBehavior;
import com.sun.javafx.scene.control.skin.TabPaneSkin;

public class BowlerStudioController extends TabPane implements IScriptEventListener{

	private static final String HOME_URL = "http://neuronrobotics.github.io/BowlerStudio/Welcome-To-BowlerStudio/";
	/**
	 * 
	 */
	private static final long serialVersionUID = -2686618188618431477L;
	private ConnectionManager connectionManager;
	private Jfx3dManager jfx3dmanager;
	private MainController mainController;
	

	public BowlerStudioController(Jfx3dManager jfx3dmanager, MainController mainController) {
		this.jfx3dmanager = jfx3dmanager;
		this.mainController = mainController;
		createScene();
	}
	
	//Custom function for creation of New Tabs.
	public void createFileTab(File file) {

		try {
			addTab(new LocalFileScriptTab( getConnectionManager(),  file),true);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//Custom function for creation of New Tabs.
	private void createAndSelectNewTab(final BowlerStudioController tabPane, final String title) {


		try {
			addTab(new ScriptingGistTab(title,getConnectionManager() , getHomeUrl(),tabPane), false);
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

	private Tab createTab() throws IOException, InterruptedException{
		final ScriptingGistTab tab = new ScriptingGistTab(null,getConnectionManager(),   null,null);

		return tab;
	}
	
	public void addTab(Tab tab, boolean closable){
		Platform.runLater(()->{
			//new RuntimeException().printStackTrace();
			final ObservableList<Tab> tabs = getTabs();
			tab.setClosable(closable);
			tabs.add(tabs.size() - 1, tab);
			getSelectionModel().select(tab);
		});
	}


	public void createScene() {

		//BorderPane borderPane = new BorderPane();


		//Placement of TabPane.
		setSide(Side.TOP);

		/* To disable closing of tabs.
		 * tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);*/

		final Tab newtab = new Tab();
		newtab.setText("+");
		newtab.setClosable(false);

		//Addition of New Tab to the tabpane.
		getTabs().addAll(newtab);
		
		setConnectionManager(new ConnectionManager(this));
		addTab(getConnectionManager(),false);
		
		createAndSelectNewTab(this, "Tutorial");


		//Function to add and display new tabs with default URL display.
		getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
			@Override
			public void changed(ObservableValue<? extends Tab> observable,
					Tab oldSelectedTab, Tab newSelectedTab) {
				if (newSelectedTab == newtab) {

					try {
						addTab(createTab(),true);

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
		 File last=FileSelectionFactory.GetFile(ScriptingEngineWidget.getWorkspace(), new GroovyFilter());
		 if(last != null){
			 createFileTab(last);
		 }
	}


	
	private void removeObject(Object p){
		if(CSG.class.isInstance(p)){          
            Platform.runLater(() -> {
            	 jfx3dmanager.removeObjects();
			});
		}else if(Tab.class.isInstance(p)){
			Platform.runLater(() -> {
				//new RuntimeException().printStackTrace();
				getTabs().remove(p);
				Tab t =(Tab)p;
		        TabPaneBehavior behavior = ((TabPaneSkin) getSkin()).getBehavior();
		        if(behavior.canCloseTab(t)) {
		            behavior.closeTab(t);
		        }
			});
		}
	}
	private void addObject(Object o){
		if(CSG.class.isInstance(o)){
            Platform.runLater(() -> {
            	//new RuntimeException().printStackTrace();
            	 CSG csg = (CSG) o;
                 MeshView current = csg.getMesh();
            	 jfx3dmanager.addObject(current);
			});
		}else if(Tab.class.isInstance(o)){
			Platform.runLater(() -> {
				//new RuntimeException().printStackTrace();
				addTab((Tab) o,true);
			});
		}else if(MeshView.class.isInstance(o)){
			Platform.runLater(() -> {
				//new RuntimeException().printStackTrace();
				jfx3dmanager.addObject((MeshView)o);
			});
		}else if(BowlerAbstractDevice.class.isInstance(o)){
			BowlerAbstractDevice bad = (BowlerAbstractDevice) o;
			getConnectionManager().addConnection((BowlerAbstractDevice) o, bad.getScriptingName());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onGroovyScriptFinished(Object result, Object Previous) {
		Log.warning("Loading script results "+result+ " previous "+ Previous);
		// this is added in the script engine when the connection manager is loaded
		if(ArrayList.class.isInstance(Previous)){
			ArrayList<Object>c = (ArrayList<Object>) Previous;
			for(int i=0;i<c.size();i++){
				removeObject(c.get(i));
			}
		}else{
			removeObject(Previous);
		}
		if(ArrayList.class.isInstance(result)){
			ArrayList<Object>c = (ArrayList<Object>) result;
			for(int i=0;i<c.size();i++){
				Log.warning("Loading array Lists with removals "+c.get(i));
				addObject(c.get(i));
			}
		}else{
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

	public void onAddDefaultRightArm(ActionEvent event) {
		if(mainController.getAddDefaultRightArm().isSelected()){
			// TODO Auto-generated method stub
			BowlerAbstractDevice dev = getConnectionManager().pickConnectedDevice(DHParameterKinematics.class);
			IConnectionEventListener l = new IConnectionEventListener() {
				@Override public void onDisconnect(BowlerAbstractConnection source) {
					jfx3dmanager.removeArm();
					mainController.getAddDefaultRightArm().selectedProperty().set(false);
				}
				@Override public void onConnect(BowlerAbstractConnection source) {}
			} ;
			if(dev == null){
				DyIO tmp =  (DyIO) getConnectionManager().pickConnectedDevice(DyIO.class);
				if(tmp!=null){
					tmp.addConnectionEventListener(l);
					dev = new DHParameterKinematics(tmp, "TrobotMaster.xml");
					getConnectionManager().addConnection(dev, "DHArm");
				}
			}
			if(dev!=null){
				jfx3dmanager.attachArm((DHParameterKinematics)dev);
				dev.addConnectionEventListener(l);
			}else{
				mainController.getAddDefaultRightArm().selectedProperty().set(false);
			}
		}else{
			jfx3dmanager.removeArm();
		}
	}

	public void onAddVRCamera(ActionEvent event) {
		// TODO Auto-generated method stub
		
		BowlerAbstractDevice dev = getConnectionManager().pickConnectedDevice(AbstractImageProvider.class);
		if(dev==null)
			dev= getConnectionManager().onConnectCVCamera();
		if(dev!=null){
			dev.addConnectionEventListener(new IConnectionEventListener() {
				@Override public void onDisconnect(BowlerAbstractConnection source) {
					mainController.getAddVRCamera().selectedProperty().set(false);
				}
				@Override public void onConnect(BowlerAbstractConnection source) {}
			});
			new Thread(){
				public void run(){
					HaarDetector faces = new HaarDetector();
					System.out.println("Camera VR Started");
					while(mainController.getAddVRCamera().isSelected()){
						ThreadUtil.wait(100);
					}
					//bail out when the checkbox is unchecked
					System.out.println("Camera VR disabled");
				}
			}.start();
		}
	}

	public void disconnect() {
		getConnectionManager().disconnectAll();
	}

	public Stage getPrimaryStage() {
		// TODO Auto-generated method stub
		return BowlerStudio.getPrimaryStage();
	}

	public void setSelectedTab(Tab tab) {
		getSelectionModel().select(tab);
	}

	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}

	public void setConnectionManager(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

}