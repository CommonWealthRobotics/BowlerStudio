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
	private static final String key = "workspace";
	private static Menu workspaceMenu;
	private static final int maxMenueSize = 20;
	private static boolean sorting = false;
	private static HashMap<String, Integer> rank = new HashMap<String, Integer>();
	private static boolean running = false;

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
			if (ScriptingEngine.hasNetwork())
				
				for (int i = 0; i < ConfigurationDatabase.keySet(key).size(); i++) {
					try {
						String o = (String) ConfigurationDatabase.keySet(key).toArray()[i];
						if (o.endsWith(".git")) {
							boolean wasState = ScriptingEngine.isPrintProgress();
							ScriptingEngine.setPrintProgress(false);
							System.err.println("Pulling workspace " + o);
							try {
								if (!ScriptingEngine.isUrlAlreadyOpen(o))
									ScriptingEngine.pull(o);
							} catch(WrongRepositoryStateException ex) {
								// ignore, unsaved work
							}catch (Exception e) {
								BowlerStudioMenu.checkandDelete(o);
							}catch (Throwable ex) {
								ex.printStackTrace();
								ConfigurationDatabase.removeObject(key, o);
								// ScriptingEngine.deleteRepo(o);
								// i--;
							}
							ScriptingEngine.setPrintProgress(wasState);


						} else {
							ConfigurationDatabase.remove(key,o);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			running = false;

		}).start();
		sort();
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
		if(!BowlerStudio.checkValidURL(url)) {
			BowlerStudio.runLater(()->BowlerStudio.showExceptionAlert(new RuntimeException(),"URL does not exist: "+url));
			return;
		}
		}catch(Exception ex) {
			ex.printStackTrace();
			return;
		}
		ArrayList<String> data;
		
			
				if (ConfigurationDatabase.getObject(key,url,null) == null) {
					data = new ArrayList<String>();
					data.add(menueMessage);
					data.add(new Long(System.currentTimeMillis()).toString());
					ConfigurationDatabase.put(key,url, data);
					//System.out.println("Workspace add: " + url);
				}
			
		// data = (ArrayList<String>) workspaceData.get(url);
		// data.set(1, new Long(System.currentTimeMillis()).toString());
		sort();
		//

	}

	

	@SuppressWarnings("unchecked")
	public static void sort() {
		if (sorting)
			return;
		sorting = true;

		boolean rankChanged = false;
		try {
			ArrayList<String> myOptions = new ArrayList<String>();
			
				for (String o : ConfigurationDatabase.keySet(key)) {
					// System.out.println("Opt: "+o);
					myOptions.add(o);
				}
			
			ArrayList<String> menu = new ArrayList<>();
			while (myOptions.size() > 0) {
				int bestIndex = 0;
				String besturl = (String) myOptions.get(bestIndex);
				ArrayList<String> arrayList = (ArrayList<String>) ConfigurationDatabase.get(key,besturl);
				long newestTime=0;
				if(arrayList!=null)
				if(arrayList.size()>1) {
					newestTime= Long.parseLong(arrayList.get(1));
					for (int i = 0; i < myOptions.size(); i++) {
						String nowurl = (String) myOptions.get(i);
						long myTime = Long.parseLong(arrayList.get(1));
						if (myTime >= newestTime) {
							newestTime = myTime;
							besturl = nowurl;
							bestIndex = i;
						}
					}
				}else
					continue;
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
						remove(removedURL);
					}

				} else {
					System.out.println("Removing from workspace: " + removedURL);
					remove(removedURL);
				}
			}

			for (int i = 0; i < menu.size(); i++) {
				String url = menu.get(i);
				if (rank.get(url) == null) {
					rankChanged = true;
					rank.put(url, i);
					// System.out.println("Rank firstNoted : "+url+" "+i);
				}
				if (rank.get(url).intValue() != i) {
					rankChanged = true;

				}
				rank.put(url, i);
			}
			if (rankChanged) {
				BowlerStudio.runLater(() -> {
					if (workspaceMenu.getItems() != null)
						workspaceMenu.getItems().clear();

					new Thread(() -> {
						for (String url : menu) {
							//System.out.println("Workspace : " + url);
							ArrayList<String> arrayList = (ArrayList<String>) ConfigurationDatabase.getObject(key,url,new ArrayList<>());
							if (arrayList != null)
								if (arrayList.size() >= 0)
									try {
										BowlerStudioMenu.setUpRepoMenue(workspaceMenu, url, false, false,
												arrayList.get(0));
									} catch (Throwable t) {
										System.out.println("Error with "+url+" "+arrayList.toArray());
										t.printStackTrace();
									}

						}
						sorting = false;
					}).start();
				});
			} else {
				sorting = false;
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (rankChanged) {
			//System.out.println("Sorting workspace...");
			new Thread(() -> {
				ConfigurationDatabase.save();
			}).start();
		}
	}

//	public static HashMap<String, Object> getWorkspaceData() {
//		return ConfigurationDatabase.getParamMap("workspace");
//	}

	public static void remove(String url) {
		ConfigurationDatabase.removeObject(key, url);
	}

}
