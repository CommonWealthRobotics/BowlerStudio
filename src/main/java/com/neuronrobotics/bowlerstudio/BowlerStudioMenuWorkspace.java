package com.neuronrobotics.bowlerstudio;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;

import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.util.ThreadUtil;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;

@SuppressWarnings("restriction")
public class BowlerStudioMenuWorkspace {
	private static final String key = "workspaceList";
	private static String catagory = "list";
	private static Menu workspaceMenu;
	private static final int maxMenueSize = 20;
	private static boolean sorting = false;
	private static HashMap<String, Integer> rank = new HashMap<String, Integer>();
	private static boolean running = false;
	private static ArrayList<ArrayList<String>> wp = null;

	public static void init(Menu workspacemenu) {
		if (workspacemenu == null)
			throw new RuntimeException();
		workspaceMenu = workspacemenu;
	}

	public static void loginEvent() {
		if (running)
			return;
		running = true;
		rank.clear();
		new Thread(() -> {
			if (ScriptingEngine.hasNetwork()) {
				wp = (ArrayList<ArrayList<String>>) ConfigurationDatabase.getObject(key, catagory,
						new ArrayList<ArrayList<String>>());

				for (ArrayList<String> entry : wp) {
					String o = entry.get(0);
					String message = entry.get(1);
					try {
						ScriptingEngine.pull(o);
					} catch (WrongRepositoryStateException ex) {
						// ignore, unsaved work
					} catch (Exception e) {
						BowlerStudioMenu.checkandDelete(o);
					} catch (Throwable ex) {
						ex.printStackTrace();
					}
				}
			}
			updateMenu();
//				
//				for (int i = 0; i < ConfigurationDatabase.keySet(key).size(); i++) {
//					try {
//						String o = (String) ConfigurationDatabase.keySet(key).toArray()[i];
//						if (o.endsWith(".git")) {
//							boolean wasState = ScriptingEngine.isPrintProgress();
//							ScriptingEngine.setPrintProgress(false);
//							com.neuronrobotics.sdk.common.Log.error("Pulling workspace " + o);
//							try {
//								ScriptingEngine.pull(o);
//							} catch (WrongRepositoryStateException ex) {
//								// ignore, unsaved work
//							} catch (Exception e) {
//								BowlerStudioMenu.checkandDelete(o);
//							} catch (Throwable ex) {
//								ex.printStackTrace();
//								ConfigurationDatabase.removeObject(key, o);
//								// ScriptingEngine.deleteRepo(o);
//								// i--;
//							}
//							ScriptingEngine.setPrintProgress(wasState);
//
//						} else {
//							ConfigurationDatabase.remove(key, o);
//						}
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			running = false;

		}).start();

	}

	public static void add(String url) {
		add(url, BowlerStudioMenu.gitURLtoMessage(url));
	}

	@SuppressWarnings("unchecked")
	public static void add(String url, String menueMessage) {
		if (menueMessage == null)
			throw new RuntimeException("Menu Message can not be " + menueMessage);
		if (menueMessage.length() < 2) {
			menueMessage = new Date().toString();
		}
		try {
			if (!BowlerStudio.checkValidURL(url)) {
				BowlerStudio.runLater(
						() -> BowlerStudio.showExceptionAlert(new RuntimeException(), "URL does not exist: " + url));
				return;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
		for (ArrayList<String> entry : wp) {
			String o = entry.get(0);
			if (o.contentEquals(url)) {
				System.out.println("Already in menu " + url);
				return;
			}
		}
		ArrayList<String> data = new ArrayList<String>();
		data.add(url);
		data.add(menueMessage);
		wp.add(0, data);
		ConfigurationDatabase.save();
//
//		Object object = ConfigurationDatabase.getObject(key, url, null);
//		if (object == null) {
//			data = new ArrayList<String>();
//			data.add(menueMessage);
//			data.add(new Long(System.currentTimeMillis()).toString());
//			System.err.println("Adding URL to workspace " + url);
//			ConfigurationDatabase.put(key, url, data);
//			ConfigurationDatabase.save();
//			// com.neuronrobotics.sdk.common.Log.error("Workspace add: " + url);
//		}

		// data = (ArrayList<String>) workspaceData.get(url);
		// data.set(1, new Long(System.currentTimeMillis()).toString());
		updateMenu();
		//

	}

//	@SuppressWarnings("unchecked")
//	public static void sort() {
//		if (sorting)
//			return;
//		sorting = true;
//
//		boolean rankChanged = false;
//		try {
//			ArrayList<String> myOptions = new ArrayList<String>();
//
//			for (String o : ConfigurationDatabase.keySet(key)) {
//				// com.neuronrobotics.sdk.common.Log.error("Opt: "+o);
//				myOptions.add(o);
//			}
//
//			ArrayList<String> menu = new ArrayList<>();
//			while (myOptions.size() > 0) {
//				int bestIndex = 0;
//				String besturl = (String) myOptions.get(bestIndex);
//				ArrayList<String> arrayList = (ArrayList<String>) ConfigurationDatabase.get(key, besturl);
//				long newestTime = 0;
//				if (arrayList != null)
//					if (arrayList.size() > 1) {
//						newestTime = Long.parseLong(arrayList.get(1));
//						for (int i = 0; i < myOptions.size(); i++) {
//							String nowurl = (String) myOptions.get(i);
//							long myTime = Long.parseLong(arrayList.get(1));
//							if (myTime >= newestTime) {
//								newestTime = myTime;
//								besturl = nowurl;
//								bestIndex = i;
//							}
//						}
//					} else
//						continue;
//				String removedURL = (String) myOptions.remove(bestIndex);
//
//				// clone all repos from git
//				try {
//					// ScriptingEngine.pull(removedURL);
//					menu.add(removedURL);
//				} catch (Exception e) {
//					// repo is broken or missing
//					e.printStackTrace();
//					System.err.println("Removing from workspace: " + removedURL);
//					remove(removedURL);
//				}
//
//			}
//
//			for (int i = 0; i < menu.size(); i++) {
//				String url = menu.get(i);
//				if (rank.get(url) == null) {
//					rankChanged = true;
//					rank.put(url, i);
//					// com.neuronrobotics.sdk.common.Log.error("Rank firstNoted : "+url+" "+i);
//				}
//				if (rank.get(url).intValue() != i) {
//					rankChanged = true;
//
//				}
//				rank.put(url, i);
//			}
//			if (rankChanged) {
//				BowlerStudio.runLater(() -> {
//					if (workspaceMenu.getItems() != null)
//						workspaceMenu.getItems().clear();
//
//					new Thread(() -> {
//						int numAdded = 0;
//						for (String url : menu) {
//							if(numAdded>=maxMenueSize) {
//								System.err.println("Pruning "+url+" because too big!");
//								remove(url);
//								continue;
//								
//							}
//							// com.neuronrobotics.sdk.common.Log.error("Workspace : " + url);
//							ArrayList<String> arrayList = (ArrayList<String>) ConfigurationDatabase.getObject(key, url,
//									new ArrayList<>());
//							if (arrayList != null)
//								if (arrayList.size() >= 0)
//									try {
//										BowlerStudioMenu.setUpRepoMenue(workspaceMenu, url, false, false,
//												arrayList.get(0));
//										numAdded++;
//									} catch (Throwable t) {
//										com.neuronrobotics.sdk.common.Log
//												.error("Error with " + url + " " + arrayList.toArray());
//										t.printStackTrace();
//									}
//
//						}
//						sorting = false;
//					}).start();
//				});
//			} else {
//				sorting = false;
//			}
//
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		if (rankChanged) {
//			// com.neuronrobotics.sdk.common.Log.error("Sorting workspace...");
//			new Thread(() -> {
//				ConfigurationDatabase.save();
//			}).start();
//		}
//	}

//	public static HashMap<String, Object> getWorkspaceData() {
//		return ConfigurationDatabase.getParamMap("workspace");
//	}
	private static void updateMenu() {
		BowlerStudio.runLater(() -> {
			if (workspaceMenu.getItems() != null)
				workspaceMenu.getItems().clear();
			for (int i = 0; i < wp.size(); i++) {
				ArrayList<String> entry = wp.get(i);
				String url = entry.get(0);
				String message = entry.get(1);
				BowlerStudioMenu.setUpRepoMenue(workspaceMenu, url, false, false, message);
			}
		});
	}

	public static void remove(String url) {
		new Exception("BowlerStudiotMenuWorkspace removing URL " + url).printStackTrace();
		for (int i = 0; i < wp.size(); i++) {
			ArrayList<String> entry = wp.get(i);
			if (entry.get(0).contentEquals(url)) {
				wp.remove(entry);
				ConfigurationDatabase.save();
				return;
			}
		}
		updateMenu();
	}

}
