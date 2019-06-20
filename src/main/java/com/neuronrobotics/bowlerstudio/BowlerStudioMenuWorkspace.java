package com.neuronrobotics.bowlerstudio;

import java.util.HashMap;

import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;

import javafx.scene.control.Menu;

@SuppressWarnings("restriction")
public class BowlerStudioMenuWorkspace {
	@SuppressWarnings("unused")
	private static Menu workspaceMenu;
	private static HashMap<String, Object> workspaceData =null;
	public static void init(Menu workspacemenu) {
		workspaceMenu=		workspacemenu;
		workspaceData = ConfigurationDatabase.getParamMap("workspace") ;
		
	}
	
	public static void add(String url, String message) {
		HashMap<String, String> data;
		if(workspaceData.get(url)==null) {
			data = new HashMap<String, String>();
			data.put("message",message);
			workspaceData.put(url,data);
		}
		data=(HashMap<String, String>) workspaceData.get(url);
		data.put("timestamp",new Long(System.currentTimeMillis()).toString());
		ConfigurationDatabase.save();
	}
	
	private void sort() {
		
	}

}
