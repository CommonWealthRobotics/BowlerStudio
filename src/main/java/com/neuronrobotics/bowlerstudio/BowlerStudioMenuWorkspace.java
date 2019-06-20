package com.neuronrobotics.bowlerstudio;

import java.util.ArrayList;
import java.util.HashMap;
import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;

import javafx.application.Platform;
import javafx.scene.control.Menu;

@SuppressWarnings("restriction")
public class BowlerStudioMenuWorkspace {
	private static Menu workspaceMenu;
	private static HashMap<String, Object> workspaceData = null;
	private static final int maxMenueSize = 30;
	
	public static void init(Menu workspacemenu) {
		if(workspacemenu==null)
			throw new RuntimeException();
		workspaceMenu = workspacemenu;
		workspaceData = ConfigurationDatabase.getParamMap("workspace");
		sort();
	}

	@SuppressWarnings("unchecked")
	public static void add(String url, String message) {
		ArrayList<String> data;
		if (workspaceData.get(url) == null) {
			data = new ArrayList<String>();
			data.add(message);
			data.add(new Long(System.currentTimeMillis()).toString());
			workspaceData.put(url, data);

		}
		data = (ArrayList<String>) workspaceData.get(url);
		data.set(1, new Long(System.currentTimeMillis()).toString());
		sort();
		ConfigurationDatabase.save();

	}

	@SuppressWarnings("unchecked")
	private static void sort() {
		try {
			ArrayList<String> myOptions = new ArrayList<String>();
			for(String o:workspaceData.keySet()) {
				myOptions.add(o);
			}
			ArrayList<String> menu = new ArrayList<>();
			while (myOptions.size() > 0) {
				int bestIndex = 0;
				String besturl = (String) myOptions.get(bestIndex);
				long newestTime = Long.parseLong(((ArrayList<String>) workspaceData.get(besturl)).get(1));
				for (int i = 0; i < myOptions.size(); i++) {
					String nowurl = (String) myOptions.get(i);
					long myTime = Long.parseLong(((ArrayList<String>) workspaceData.get(nowurl)).get(1));
					if (myTime >= newestTime) {
						newestTime = myTime;
						besturl = nowurl;
						bestIndex = i;
					}
				}
				String removedURL = (String) myOptions.remove(bestIndex);
				if (menu.size() < maxMenueSize) {
					
					// clone all repos from git
					try {
						ScriptingEngine.pull(removedURL);
						menu.add(removedURL);
					}catch(Exception e) {
						// repo is broken or missing
						workspaceData.remove(removedURL);
					}
					
				}else {
					workspaceData.remove(removedURL);
				}
			}
			Platform.runLater(() -> {
				if(workspaceMenu.getItems()!=null)
					workspaceMenu.getItems().clear();
				for (String url : menu) {
					ArrayList<String> data = (ArrayList<String>) workspaceData.get(url);
					
					String message = data.get(0);
					BowlerStudioMenu.setUpRepoMenue(workspaceMenu,message,url, false);
			
				}
			});
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
