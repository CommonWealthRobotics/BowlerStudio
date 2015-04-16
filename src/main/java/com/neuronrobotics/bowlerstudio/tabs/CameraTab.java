package com.neuronrobotics.bowlerstudio.tabs;

import com.neuronrobotics.jniloader.AbstractImageProvider;

import javafx.scene.control.Tab;

public class CameraTab extends Tab {
	
	public CameraTab(AbstractImageProvider p, String name){
		setText(name);
		
	}

}
