package com.neuronrobotics.bowlerstudio;


import java.util.ArrayList;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.MouseEvent;

import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.bowlerstudio.tabs.CameraTab;
import com.neuronrobotics.jniloader.AbstractImageProvider;
import com.neuronrobotics.nrconsole.plugin.DyIO.DyIOConsole;
import com.neuronrobotics.nrconsole.plugin.bootloader.BootloaderPanel;
import com.neuronrobotics.nrconsole.plugin.bootloader.core.NRBootLoader;
import com.neuronrobotics.replicator.driver.BowlerBoardDevice;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.dyio.DyIO;

public class PluginManager {
	
	private String name;
	private BowlerAbstractDevice dev;
	private BowlerStudioController bowlerStudioController;
	private TreeItem<String> item;
	
	private ArrayList<Class> deviceSupport = new ArrayList<Class>();
	ArrayList<AbstractBowlerStudioTab> liveTabs = new ArrayList<>();
	public PluginManager(BowlerAbstractDevice dev, BowlerStudioController bowlerStudioController){
		this.dev = dev;
		this.setBowlerStudioController(bowlerStudioController);
		if(!dev.isAvailable())
			throw new RuntimeException();
		
		if(DyIO.class.isInstance(dev)){
			deviceSupport.add(DyIOConsole.class);
			
		}
		
		if(AbstractImageProvider.class.isInstance(dev)){
			deviceSupport.add(CameraTab.class);
		}
		
		if(NRBootLoader.class.isInstance(dev)){
			deviceSupport.add(BootloaderPanel.class);
		}
		
		if(BowlerBoardDevice.class.isInstance(dev)){
			
		}
		
		
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

	private AbstractBowlerStudioTab generateTab(Class<?> c) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		for(AbstractBowlerStudioTab t: liveTabs){
			if(c.isInstance(t)){
				// tab already exists, wake it up and return it
				t.onTabReOpening();
				return t;
			}
		}
		AbstractBowlerStudioTab t =(AbstractBowlerStudioTab) Class.forName(
					c.getName()
				).cast(c.newInstance()// This is where the new tab allocation is called
						)
				;
		t.setDevice(dev);
		liveTabs.add(t);
		return t;
	}

	public void setTree(TreeItem<String> item) {
		this.item =item;
		if(dev.getConnection()!=null){
			TreeItem<String> rpc = new TreeItem<String> ("Bowler RPC"); 
			rpc.setExpanded(false);
			item.getChildren().add(rpc);
		}
		TreeItem<String> plugins = new TreeItem<String> ("Plugins"); 
		plugins.setExpanded(false);
		item.getChildren().add(plugins);
		
		for( Class<?> c:deviceSupport){
			CheckBoxTreeItem<String> p = new CheckBoxTreeItem<String> (c.getSimpleName());
			p.setSelected(false);
			p.selectedProperty().addListener(b ->{
				try {
					AbstractBowlerStudioTab t = generateTab(c);
					if(p.isSelected()){
						getBowlerStudioController().addTab(t, true);
						System.out.println("Launching "+c.getSimpleName());
		        	}else{
		        		try{
		        			t.requestClose();
		        		}catch (NullPointerException ex){};// tab is already cloed
		        	}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	        	
	        });
				
			plugins.getChildren().add(p);
		}
	
	}



	public TreeItem<String> getTreeItem() {
		return item;
	}



	public BowlerStudioController getBowlerStudioController() {
		return bowlerStudioController;
	}



	public void setBowlerStudioController(BowlerStudioController bowlerStudioController) {
		this.bowlerStudioController = bowlerStudioController;
	}

}
