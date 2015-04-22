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
import com.neuronrobotics.nrconsole.plugin.DyIO.DyIOConsole;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.dyio.DyIO;

public class PluginManager {
	
	private String name;
	private BowlerAbstractDevice dev;
	private BowlerStudioController bowlerStudioController;
	private TreeItem<String> item;
	
	private ArrayList<Class> deviceSupport = new ArrayList<Class>();

	public PluginManager(BowlerAbstractDevice dev, BowlerStudioController bowlerStudioController){
		this.dev = dev;
		this.setBowlerStudioController(bowlerStudioController);
		if(!dev.isAvailable())
			throw new RuntimeException();
		
		if(DyIO.class.isInstance(dev)){
			deviceSupport.add(DyIOConsole.class);
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
		AbstractBowlerStudioTab t =(AbstractBowlerStudioTab) Class.forName(
					c.getName()
				).cast(c.newInstance()
						)
				;
		t.setDevice(dev);
		return t;
	}

	public void setTree(TreeItem<String> item) {
		this.item =item;
		TreeItem<String> rpc = new TreeItem<String> ("Bowler RPC"); 
		rpc.setExpanded(false);
		item.getChildren().add(rpc);
		
		TreeItem<String> plugins = new TreeItem<String> ("Plugins"); 
		plugins.setExpanded(false);
		item.getChildren().add(plugins);
		
		for( Class<?> c:deviceSupport){

			CheckBoxTreeItem<String> p = new CheckBoxTreeItem<String> (c.getSimpleName());
//			p.
//			{
//				@Override
//			     public void updateItem(String item, boolean empty) {
//			        super.updateItem(item, empty);
//			        System.out.println("Loading tab "+c.getSimpleName());
//		        	try {
//						getBowlerStudioController().addTab(generateTab(c), true);
//					} catch (ClassNotFoundException | InstantiationException
//							| IllegalAccessException ex) {
//						// TODO Auto-generated catch block
//						ex.printStackTrace();
//					}
//			     }
//			
//			};
					

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
