package com.neuronrobotics.bowlerstudio;


import com.neuronrobotics.sdk.common.BowlerAbstractDevice;

public class PluginManager {
	
	private String name;

	public PluginManager(BowlerAbstractDevice dev){
		if(!dev.isAvailable())
			throw new RuntimeException();
		
		
	}
	
	

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName(){
		return name;
	}

}
