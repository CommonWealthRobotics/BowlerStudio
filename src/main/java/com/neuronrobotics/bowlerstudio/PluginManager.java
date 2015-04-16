package com.neuronrobotics.bowlerstudio;


import com.neuronrobotics.sdk.common.BowlerAbstractDevice;

public class PluginManager {
	
	private String name;
	private BowlerAbstractDevice dev;

	public PluginManager(BowlerAbstractDevice dev){
		this.dev = dev;
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
		// TODO Auto-generated method stub
		return dev;
	}

}
