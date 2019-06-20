package com.neuronrobotics.bowlerstudio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.google.gson.internal.LinkedTreeMap;
import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;

import javafx.application.Platform;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

@SuppressWarnings("restriction")
public class BowlerStudioMenuWorkspace {
	@SuppressWarnings("unused")
	private static Menu workspaceMenu;
	private static HashMap<String, Object> workspaceData = null;
	private static final int maxMenueSize = 2;

	public static void init(Menu workspacemenu) {
		workspaceMenu = workspacemenu;
		workspaceData = ConfigurationDatabase.getParamMap("workspace");
		sort();
	}

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
				if (menu.size() < maxMenueSize)
					menu.add(removedURL);
				else
					workspaceData.remove(removedURL);
			}
			Platform.runLater(() -> {
				workspaceMenu.getItems().clear();
				for (String s : menu) {
					ArrayList<String> data = (ArrayList<String>) workspaceData.get(s);
					MenuItem e = new MenuItem(data.get(0));
					workspaceMenu.getItems().add(e);
				}
			});
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
