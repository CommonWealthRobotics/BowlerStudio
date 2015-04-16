package com.neuronrobotics.bowlerstudio.tabs;

import java.util.ArrayList;

import com.neuronrobotics.sdk.common.BowlerAbstractDevice;

import javafx.scene.control.Tab;

public abstract class AbstractBowlerStudioTab extends Tab {

	private boolean active = false;
	ArrayList<String> myNames = new ArrayList<String> ();
	
	
	
	public AbstractBowlerStudioTab(String myNamespaces[],BowlerAbstractDevice pm){
		for(int i=0;i<myNamespaces.length;i++){
			myNames.add(myNamespaces[i]);
		}
		
		if(!isMyNamespace(pm.getNamespaces())){
			throw new RuntimeException("Device and namespaces are incompatible ");
		}
	}
	
	
	public boolean isMyNamespace(ArrayList<String> names) {
		if (names == null)
			return false;
		for(String s:names){
			for(String m:myNames){
				if(s.contains(m)){
					setActive(true);
				}
			}
		}
		
		return isAcvive();
	}

	public void setActive(boolean a){
		active=a;
	}
	
	public boolean isAcvive() {
		return active;
	}
}
