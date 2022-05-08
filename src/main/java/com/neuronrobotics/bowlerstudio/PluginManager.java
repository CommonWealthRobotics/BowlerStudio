package com.neuronrobotics.bowlerstudio;


import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.creature.CreatureLab;
import com.neuronrobotics.bowlerstudio.tabs.*;
import com.neuronrobotics.nrconsole.plugin.BowlerCam.BowlerCamController;
import com.neuronrobotics.nrconsole.plugin.DyIO.Secheduler.AnamationSequencer;
import com.neuronrobotics.nrconsole.plugin.bootloader.BootloaderPanel;
import com.neuronrobotics.pidsim.LinearPhysicsEngine;
import com.neuronrobotics.pidsim.PidLab;
import com.neuronrobotics.sdk.addons.gamepad.BowlerJInputDevice;
import com.neuronrobotics.sdk.addons.kinematics.FirmataBowler;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.bootloader.NRBootLoader;
import com.neuronrobotics.sdk.bowlercam.device.BowlerCamDevice;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.common.RpcEncapsulation;
import com.neuronrobotics.sdk.dyio.DyIO;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import java.util.ArrayList;

//import com.neuronrobotics.nrconsole.plugin.DyIO.DyIOConsole;

public class PluginManager {
	
	private BowlerAbstractDevice dev;
	
	private static ArrayList<DeviceSupportPluginMap> deviceSupport = new ArrayList<>();
	private ArrayList<AbstractBowlerStudioTab> liveTabs = new ArrayList<>();
	
	// add tabs to the support list based on thier class
	// adding additional classes here will show up in the default 
	// tabs list for objects of that type
	static{
		//DyIO
		addPlugin(new DeviceSupportPluginMap(DyIO.class, AnamationSequencer.class));
		
		//Ipid
		// Image s
//		addPlugin(new DeviceSupportPluginMap(AbstractImageProvider.class, CameraTab.class));
//		addPlugin(new DeviceSupportPluginMap(AbstractImageProvider.class, SalientTab.class));
		// Bootloader
		addPlugin(new DeviceSupportPluginMap(NRBootLoader.class, BootloaderPanel.class));
		//BowlerBoard Specific
		//addPlugin(new DeviceSupportPlugginMap(BowlerBoardDevice.class, //none yet));
		//AbstractKinematicsNR
		//addPlugin(new DeviceSupportPluginMap(AbstractKinematicsNR.class, JogKinematicsDevice.class));
		//addPlugin(new DeviceSupportPluginMap(AbstractKinematicsNR.class, AdvancedKinematicsController.class));
		//addPlugin(new DeviceSupportPluginMap(AbstractKinematicsNR.class, DhLab.class));
		addPlugin(new DeviceSupportPluginMap(MobileBase.class, CreatureLab.class));
		//NRPrinter
		//Bowler Cam
		addPlugin(new DeviceSupportPluginMap(BowlerCamDevice.class, BowlerCamController.class));
		//LinearPhysicsEngine
		addPlugin(new DeviceSupportPluginMap(LinearPhysicsEngine.class, PidLab.class));
		//Firmata
		addPlugin(new DeviceSupportPluginMap(FirmataBowler.class, FirmataTab.class));
		//game controller
		addPlugin(new DeviceSupportPluginMap(BowlerJInputDevice.class, CalibrateGameControl.class));
	}
	
	public PluginManager(BowlerAbstractDevice dev){
		this.dev = dev;
		if(!dev.isAvailable())
			throw new RuntimeException("Device is not reporting availible "+dev.getClass().getSimpleName());
	}
	
	public static void addPlugin(DeviceSupportPluginMap newMap){

		for(int i=0;i<deviceSupport.size();i++){
			try{
				if(		deviceSupport.get(i).getDevice() == newMap.getDevice() && 
						deviceSupport.get(i).getPlugin() == newMap.getPlugin() ){
					System.out.println("Removing duplicate plugin: "+deviceSupport.remove(i));
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		Log.debug("Adding Plugin "+newMap);
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



	
	}

	
	public BowlerStudioController getBowlerStudioController() {
		return BowlerStudioController.getBowlerStudio();
	}
	
	public Node getBowlerBrowser(){

		CheckBoxTreeItem<String> rpc = new CheckBoxTreeItem<> ("Bowler RPC"); 
		TreeView<String> treeView =new  TreeView<>(rpc);
		treeView.setCellFactory(CheckBoxTreeCell.forTreeView());
		

		return treeView;
		
	}

	public ArrayList<TitledPane> getPlugins() {
		ArrayList<TitledPane> plugins = new ArrayList<>();
		
		VBox pluginLauncher = new VBox(20);
		
		for( DeviceSupportPluginMap c:deviceSupport){
			if(c.getDevice().isInstance(dev)){
				Button launcher;
				try {
					launcher= new Button(c.getPlugin().getSimpleName(), AssetFactory.loadIcon("Plugin-Icon.png"));
				}catch(RuntimeException e) {
					launcher= new Button(c.getPlugin().getSimpleName());
				}
				launcher.setTooltip(new javafx.scene.control.Tooltip("Launch plugin to "+c.getPlugin().getSimpleName()));
				try {// These tabs are the select few to autoload when a device of theis type is connected
					if( 	
							BootloaderPanel.class ==c.getPlugin()||
							CreatureLab.class ==c.getPlugin()
							){
						if(getBowlerStudioController()!=null){
							System.out.println("Auto loading "+c.getPlugin().getSimpleName());
							Log.warning("Attempting Autoloading "+c);
							//if(CreatureLab.class !=c.getPlugin())
								launchTab( c,launcher);	
							//else
							//	generateTab(c);// dont add the creature lab it uses the overlays
						}
					}else{
						Log.warning("Not autoloading "+c);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
				}
				launcher.setOnAction(b ->{
					launchTab( c,launcher);
		        });
					
				pluginLauncher.getChildren().add(launcher);
			}
		}
		TitledPane info = new TitledPane("Device Info", new Text(dev.getClass().getSimpleName()));
		TitledPane protocol = new TitledPane("Bowler Protocol",  getBowlerBrowser());
		TitledPane pluginsPane = new TitledPane("Plugins",  pluginLauncher);
		info.setGraphic(AssetFactory.loadIcon("Info.png"));
		protocol.setGraphic(AssetFactory.loadIcon("BowlerStudio.png"));
		pluginsPane.setGraphic(AssetFactory.loadIcon("Plugins.png"));
		plugins.add(info);
		if(dev.getConnection()!=null)
			plugins.add(protocol);
		plugins.add(pluginsPane);
		return plugins;
	}
	
	private void launchTab(DeviceSupportPluginMap c,Button launcher){
		new Thread(){
			public void run(){
				Thread.currentThread().setUncaughtExceptionHandler(new IssueReportingExceptionHandler());

				setName("Launching "+c.getPlugin().getSimpleName());
				try {
					AbstractBowlerStudioTab t = generateTab(c);

					// allow the threads to finish before adding
					//ThreadUtil.wait(50);
					getBowlerStudioController().addTab(t, true);
					
					t.setOnCloseRequest(new EventHandler<Event>() {
						@Override
						public void handle(Event arg0) {
							System.out.println("PM is Closing "+t.getText());
							t.onTabClosing();
							Platform.runLater(()->launcher.setDisable(false));
							
						}
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
	}

}
