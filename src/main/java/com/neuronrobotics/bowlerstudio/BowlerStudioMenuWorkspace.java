package com.neuronrobotics.bowlerstudio;

import java.io.IOException;
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
	private static final int maxMenueSize = 15;
	private static boolean sorting = false;

	public static void init(Menu workspacemenu) {
		if (workspacemenu == null)
			throw new RuntimeException();
		workspaceMenu = workspacemenu;
		loginEvent();

	}

	public static void loginEvent() {
		workspaceData = ConfigurationDatabase.getParamMap("workspace");
		for (String o : workspaceData.keySet()) {
			try {
				ScriptingEngine.pull(o);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		sort();
	}

	public static void add(String url) {
		add(url, BowlerStudioMenu.gitURLtoMessage(url));
	}

	@SuppressWarnings("unchecked")
	public static void add(String url, String menueMessage) {
		if (menueMessage == null || menueMessage.length() < 2)
			throw new RuntimeException();
		ArrayList<String> data;
		synchronized (workspaceData) {
			if (workspaceData.get(url) == null) {
				data = new ArrayList<String>();
				data.add(menueMessage);
				data.add(new Long(System.currentTimeMillis()).toString());
				workspaceData.put(url, data);
				System.out.println("Workspace add: "+url);
			}
		}
		data = (ArrayList<String>) workspaceData.get(url);
		data.set(1, new Long(System.currentTimeMillis()).toString());
		sort();
		//

	}

	@SuppressWarnings("unchecked")
	private static void sort() {
		if (sorting)
			return;
		sorting = true;
		System.out.println("Sorting workspace...");
		try {
			ArrayList<String> myOptions = new ArrayList<String>();
			synchronized (workspaceData) {
				for (String o : workspaceData.keySet()) {
					//System.out.println("Opt: "+o);
					myOptions.add(o);
				}
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
						// ScriptingEngine.pull(removedURL);
						menu.add(removedURL);
					} catch (Exception e) {
						// repo is broken or missing
						e.printStackTrace();
						System.out.println("Removing from workspace: " + removedURL);
						synchronized (workspaceData) {
							workspaceData.remove(removedURL);
						}
					}

				} else {
					System.out.println("Removing from workspace: " + removedURL);
					synchronized (workspaceData) {
						workspaceData.remove(removedURL);
					}
				}
			}
			Platform.runLater(() -> {
				if (workspaceMenu.getItems() != null)
					workspaceMenu.getItems().clear();
				
				new Thread(() -> {
					for (String url : menu) {
							BowlerStudioMenu.setUpRepoMenue(workspaceMenu, 
									url, 
									false, 
									false,
									((ArrayList<String>) workspaceData.get(url)).get(0));
						
					}
					sorting = false;
				}).start();
			});

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		ConfigurationDatabase.save();
	}

}
