package com.neuronrobotics.bowlerstudio;


import java.util.ArrayList;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.bowlerstudio.tabs.CameraTab;
import com.neuronrobotics.bowlerstudio.tabs.SalientTab;
import com.neuronrobotics.jniloader.AbstractImageProvider;
import com.neuronrobotics.jniloader.SalientDetector;
import com.neuronrobotics.nrconsole.plugin.BowlerCam.BowlerCamController;
import com.neuronrobotics.nrconsole.plugin.DeviceConfig.PrinterConiguration;
import com.neuronrobotics.nrconsole.plugin.DyIO.DyIOConsole;
import com.neuronrobotics.nrconsole.plugin.DyIO.Secheduler.AnamationSequencer;
import com.neuronrobotics.nrconsole.plugin.DyIO.Secheduler.SchedulerGui;
import com.neuronrobotics.nrconsole.plugin.DyIO.hexapod.HexapodController;
import com.neuronrobotics.nrconsole.plugin.PID.PIDControl;
import com.neuronrobotics.nrconsole.plugin.bootloader.BootloaderPanel;
import com.neuronrobotics.nrconsole.plugin.cartesian.AdvancedKinematicsController;
import com.neuronrobotics.nrconsole.plugin.cartesian.DHKinematicsLab;
import com.neuronrobotics.nrconsole.plugin.cartesian.JogKinematicsDevice;
import com.neuronrobotics.pidsim.LinearPhysicsEngine;
import com.neuronrobotics.pidsim.PidLab;
import com.neuronrobotics.replicator.driver.BowlerBoardDevice;
import com.neuronrobotics.replicator.driver.NRPrinter;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.bootloader.NRBootLoader;
import com.neuronrobotics.sdk.bowlercam.device.BowlerCamDevice;
import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.IConnectionEventListener;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.common.RpcEncapsulation;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.namespace.bcs.pid.IExtendedPIDControl;
import com.neuronrobotics.sdk.namespace.bcs.pid.IPidControlNamespace;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class PluginManager {
	
	private BowlerAbstractDevice dev;
	
	private static ArrayList<DeviceSupportPluginMap> deviceSupport = new ArrayList<DeviceSupportPluginMap>();
	private ArrayList<AbstractBowlerStudioTab> liveTabs = new ArrayList<>();
	
	// add tabs to the support list based on thier class
	// adding additional classes here will show up in the default 
	// tabs list for objects of that type
	static{
		//DyIO
		deviceSupport.add(new DeviceSupportPluginMap(DyIO.class, DyIOConsole.class));
		deviceSupport.add(new DeviceSupportPluginMap(DyIO.class, AnamationSequencer.class));
		deviceSupport.add(new DeviceSupportPluginMap(DyIO.class, HexapodController.class));
		//Ipid
		deviceSupport.add(new DeviceSupportPluginMap(IPidControlNamespace.class, PIDControl.class));
		// Image s
		deviceSupport.add(new DeviceSupportPluginMap(AbstractImageProvider.class, CameraTab.class));
		deviceSupport.add(new DeviceSupportPluginMap(AbstractImageProvider.class, SalientTab.class));
		// Bootloader
		deviceSupport.add(new DeviceSupportPluginMap(NRBootLoader.class, BootloaderPanel.class));
		//BowlerBoard Specific
		//deviceSupport.add(new DeviceSupportPlugginMap(BowlerBoardDevice.class, //none yet));
		//AbstractKinematicsNR
		deviceSupport.add(new DeviceSupportPluginMap(AbstractKinematicsNR.class, JogKinematicsDevice.class));
		deviceSupport.add(new DeviceSupportPluginMap(AbstractKinematicsNR.class, AdvancedKinematicsController.class));
		//NRPrinter
		deviceSupport.add(new DeviceSupportPluginMap(NRPrinter.class, PrinterConiguration.class));
		//Bowler Cam
		deviceSupport.add(new DeviceSupportPluginMap(BowlerCamDevice.class, BowlerCamController.class));
		//LinearPhysicsEngine
		deviceSupport.add(new DeviceSupportPluginMap(LinearPhysicsEngine.class, PidLab.class));
		//LinearPhysicsEngine
		deviceSupport.add(new DeviceSupportPluginMap(DHParameterKinematics.class, DHKinematicsLab.class));
	}
	
	public PluginManager(BowlerAbstractDevice dev){
		this.dev = dev;
		if(!dev.isAvailable())
			throw new RuntimeException("Device is not reporting availible "+dev.getClass().getSimpleName());
	}
	
	public static void addPlugin(DeviceSupportPluginMap newMap){
		if(!newMap.isFactoryProvided()){
			throw new RuntimeException("To add a plugin at runtime, the plugin factory must be provided: ");
		}
		deviceSupport.add(newMap);
	}
	
	

	public void setName(String name) {
		dev.setScriptingName(name);
	}
	
	public String getName(){
		return dev.getScriptingName();
	}



	public BowlerAbstractDevice getDevice() {
		return dev;
	}

	private AbstractBowlerStudioTab generateTab(DeviceSupportPluginMap c) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		for(AbstractBowlerStudioTab t: liveTabs){
			if(c.getPlugin().isInstance(t)){
				// tab already exists, wake it up and return it
				t.onTabReOpening();
				return t;
			}
		}
		AbstractBowlerStudioTab t=c.generateNewPlugin();

		t.setDevice(dev);
		liveTabs.add(t);

		return t;
	}

	public void setTree(TreeItem<String> item) {

		if(dev.getConnection()!=null){
			TreeItem<String> rpc = new TreeItem<String> ("Bowler RPC"); 
			rpc.setExpanded(false);
			item.getChildren().add(rpc);
			ArrayList<String> nameSpaceList = dev.getNamespaces();
			for(String namespace:nameSpaceList){
				CheckBoxTreeItem<String> ns = new CheckBoxTreeItem<String> (namespace); 
				ns.setExpanded(false);
				rpc.getChildren().add(ns);
				ArrayList<RpcEncapsulation> rpcList = dev.getRpcList(namespace);
				CheckBoxTreeItem<String> get = new CheckBoxTreeItem<String> ("GET"); 
				CheckBoxTreeItem<String> post = new CheckBoxTreeItem<String> ("POST"); 
				CheckBoxTreeItem<String> async = new CheckBoxTreeItem<String> ("ASYNC"); 
				CheckBoxTreeItem<String> crit = new CheckBoxTreeItem<String> ("CRITICAL");
				get.setExpanded(false);
				ns.getChildren().add(get);
				post.setExpanded(false);
				ns.getChildren().add(post);
				async.setExpanded(false);
				ns.getChildren().add(async);
				crit.setExpanded(false);
				ns.getChildren().add(crit);
				for(RpcEncapsulation rpcEnc:rpcList){
					CheckBoxTreeItem<String> rc = new CheckBoxTreeItem<String> (rpcEnc.getRpc()); 
					rc.setExpanded(false);
					switch(rpcEnc.getDownstreamMethod()){
					case ASYNCHRONOUS:
						async.getChildren().add(rc);
						break;
					case CRITICAL:
						crit.getChildren().add(rc);
						break;
					case GET:
						get.getChildren().add(rc);
						break;
					case POST:
						post.getChildren().add(rc);
						break;
					default:
						break;
					
					}
					RpcCommandPanel panel =new RpcCommandPanel(rpcEnc, dev,rc);

					Platform.runLater(()->{
						SwingNode sn = new SwingNode();
						Stage dialog = new Stage();
						dialog.setHeight(panel.getHeight());
						dialog.setWidth(panel.getWidth());
						dialog.initStyle(StageStyle.UTILITY);
					    sn.setContent(panel);
						Scene scene = new Scene(new Group(sn));
						dialog.setScene(scene);
						dialog.setOnCloseRequest(event -> {
							rc.setSelected(false);
						});
						rc.selectedProperty().addListener(b ->{
							 if(rc.isSelected()){
								 dialog.show();
							 }else{
								 dialog.hide();
							 }
				        });
					});
					
				}
			}
		}

	
	}

	
	public BowlerStudioController getBowlerStudioController() {
		return BowlerStudioController.getBowlerStudio();
	}
	
	public Node getBowlerBwowser(){
		return null;
		
	}

	public ArrayList<TitledPane> getPlugins() {
		ArrayList<TitledPane> plugins = new ArrayList<TitledPane>();
		
		VBox pluginLauncher = new VBox(20);
		
		for( DeviceSupportPluginMap c:deviceSupport){
			if(c.getDevice().isInstance(dev)){
				Button launcher = new Button("Launch "+c.getPlugin().getSimpleName());
				try {// These tabs are the select few to autoload when a device of theis type is connected
					if( 	DyIOConsole.class ==c.getPlugin() ||
							BootloaderPanel.class ==c.getPlugin()
							){
						if(getBowlerStudioController()!=null){
							System.out.println("Auto loading "+c.getPlugin().getSimpleName());
							//launcher.setDisable(true);
							getBowlerStudioController().addTab(generateTab(c), true);
						}
					}else{
						Log.warning("Not autoloading "+c);
					}
				} catch (IllegalArgumentException | IllegalAccessException
						 | SecurityException
						| ClassNotFoundException | InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				launcher.setOnAction(b ->{
					
					new Thread(){
						public void run(){
							try {
								AbstractBowlerStudioTab t = generateTab(c);

								// allow the threads to finish before adding
								//ThreadUtil.wait(50);
								getBowlerStudioController().addTab(t, true);
								
								t.setOnCloseRequest(arg0 -> {
									System.out.println("PM is Closing "+t.getText());
									t.onTabClosing();
									Platform.runLater(()->launcher.setDisable(false));
								});
								Platform.runLater(()->{
									launcher.setDisable(true);
								});
								
								System.out.println("Launching "+c.getPlugin().getSimpleName());
					        	
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}.start();
	
		        	
		        });
					
				pluginLauncher.getChildren().add(launcher);
			}
		}
		
		plugins.add(new TitledPane("Device Type", new Text(dev.getClass().getSimpleName())));
		if(dev.getConnection()!=null)
			plugins.add(new TitledPane("Bowler Protocol",  getBowlerBwowser()));
		plugins.add(new TitledPane("Plugins",  pluginLauncher));
		return plugins;
	}

}
