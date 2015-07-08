package com.neuronrobotics.bowlerstudio.tabs;

import java.util.ArrayList;

import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.IConnectionEventListener;
import com.neuronrobotics.sdk.common.IDeviceConnectionEventListener;
import com.sun.javafx.scene.control.behavior.TabPaneBehavior;
import com.sun.javafx.scene.control.skin.TabPaneSkin;

import javafx.application.Platform;
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
		pm.addConnectionEventListener(new IDeviceConnectionEventListener() {
			
			@Override
			public void onDisconnect(BowlerAbstractDevice source) {
				//if the device disconnects, close the tab
				if(source ==pm && source !=null )
					requestClose();
			}
			
			@Override
			public void onConnect(BowlerAbstractDevice source) {
				// TODO Auto-generated method stub
				
			}
		});
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
    	Platform.runLater(()->{
    		try{
		        TabPaneBehavior behavior = getBehavior();
		        if(behavior.canCloseTab(this)) {
		            behavior.closeTab(this);
		        }
    		}catch (NullPointerException e){
    			//e.printStackTrace();
    		}
    	});
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
