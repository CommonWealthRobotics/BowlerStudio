package com.neuronrobotics.bowlerstudio;


import javafx.scene.control.TreeItem;

import com.neuronrobotics.sdk.common.BowlerAbstractDevice;

public class PluginManager {
	
	private String name;
	private BowlerAbstractDevice dev;
	private BowlerStudioController bowlerStudioController;
	private TreeItem<String> item;

	public PluginManager(BowlerAbstractDevice dev, BowlerStudioController bowlerStudioController){
		this.dev = dev;
		this.bowlerStudioController = bowlerStudioController;
		if(!dev.isAvailable())
			throw new RuntimeException();
		
		
	}
	
	

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName(){
		return name;
	}



	public BowlerAbstractDevice getDevice() {
		return dev;
	}



	public void setTree(TreeItem<String> item) {
		this.item =item;
		
	}



	public TreeItem<String> getTreeItem() {
		return item;
	}

}
