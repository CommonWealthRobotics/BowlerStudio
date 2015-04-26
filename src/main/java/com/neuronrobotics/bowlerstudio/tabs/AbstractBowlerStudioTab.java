package com.neuronrobotics.bowlerstudio.tabs;

import java.util.ArrayList;

import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.sun.javafx.scene.control.behavior.TabPaneBehavior;
import com.sun.javafx.scene.control.skin.TabPaneSkin;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;

public abstract class AbstractBowlerStudioTab extends Tab implements EventHandler<Event> {

	private boolean active = false;
	ArrayList<String> myNames =null;
	
	public abstract void onTabClosing();
	public abstract String[] getMyNameSpaces();
	public abstract void initializeUI(BowlerAbstractDevice pm);
	public abstract void onTabReOpening();
	public static boolean isAutoLoad; 
	
	
	public void setDevice(BowlerAbstractDevice pm){
		 myNames = new ArrayList<String> ();
		 if(getMyNameSpaces().length>0){
			for(int i=0;i<getMyNameSpaces().length;i++){
				myNames.add(getMyNameSpaces()[i]);
			}
			if(!isMyNamespace(pm.getNamespaces())){
				throw new RuntimeException("Device and namespaces are incompatible ");
			}
		 }
		setOnCloseRequest(this);
		initializeUI(pm);
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
	
	@Override
	public void handle(Event event){
		System.out.println("Closing "+getText());
		onTabClosing();
	}
}
