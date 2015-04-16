package com.neuronrobotics.bowlerstudio.tabs;

import java.util.ArrayList;

import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.sun.javafx.scene.control.behavior.TabPaneBehavior;
import com.sun.javafx.scene.control.skin.TabPaneSkin;

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
	
    public void requestClose() {
        TabPaneBehavior behavior = getBehavior();
        if(behavior.canCloseTab(this)) {
            behavior.closeTab(this);
        }
    }

    private TabPaneBehavior getBehavior() {
        return ((TabPaneSkin) getTabPane().getSkin()).getBehavior();
    }

	public void setActive(boolean a){
		active=a;
	}
	
	public boolean isAcvive() {
		return active;
	}
}
