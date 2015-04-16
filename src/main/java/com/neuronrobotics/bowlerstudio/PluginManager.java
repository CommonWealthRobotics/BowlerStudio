package com.neuronrobotics.bowlerstudio;

import java.awt.Component;

import javax.swing.JFrame;

import com.neuronrobotics.sdk.common.BowlerAbstractDevice;

public class PluginManager {
	
	public PluginManager(BowlerAbstractDevice dev){
		if(!dev.isAvailable())
			throw new RuntimeException();
		
		
	}

}
