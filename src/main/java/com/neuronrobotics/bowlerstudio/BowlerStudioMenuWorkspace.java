package com.neuronrobotics.bowlerstudio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jgit.api.errors.InvalidRemoteException;

import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.util.ThreadUtil;

import javafx.application.Platform;
import javafx.scene.control.Menu;

@SuppressWarnings("restriction")
public class BowlerStudioMenuWorkspace {
	private static Menu workspaceMenu;
	private static final int maxMenueSize = 20;
	private static boolean sorting = false;
	private static HashMap<String,Integer> rank = new HashMap<String, Integer>();
	private static boolean running = false;
	public static void init(Menu workspacemenu) {
		if (workspacemenu == null)
			throw new RuntimeException();
		workspaceMenu = workspacemenu;
	}

	public static void loginEvent() {
		if(running)
			return;
		running = true;
		rank.clear();
		new Thread(()-> {
			if(ScriptingEngine.hasNetwork())
			for (int i=0;i<getWorkspaceData().keySet().size();i++) {
				try {
					String o = (String) getWorkspaceData().keySet().toArray()[i];
					System.err.println("Pulling workspace "+o);
					try {
						if(!ScriptingEngine.isUrlAlreadyOpen(o))
							ScriptingEngine.pull(o);
					} catch (Throwable e) {
						try {
							ScriptingEngine.pull(o);
						} catch (InvalidRemoteException ex) {
							System.err.println("Deleting repo from workspace "+ex.getMessage());
							getWorkspaceData().remove(o);
							ScriptingEngine.deleteRepo(o);
						}catch (Throwable ex) {
							e.printStackTrace();
							getWorkspaceData().remove(o);
							//ScriptingEngine.deleteRepo(o);
							//i--;
						}
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
		if (menueMessage == null )
			throw new RuntimeException("Menu Message can not be "+menueMessage);
		if (menueMessage.length()<2) {
			menueMessage= new Date().toString();
		}
		ArrayList<String> data;
		if(getWorkspaceData()!=null)
		synchronized (getWorkspaceData()) {
			if (getWorkspaceData().get(url) == null) {
				data = new ArrayList<String>();
				data.add(menueMessage);
				data.add(new Long(System.currentTimeMillis()).toString());
				getWorkspaceData().put(url, data);
				System.out.println("Workspace add: "+url);
			}
		}
		//data = (ArrayList<String>) workspaceData.get(url);
		//data.set(1, new Long(System.currentTimeMillis()).toString());
		sort();
		//

	}

	@SuppressWarnings("unchecked")
	public static void sort() {
		if (sorting)
			return;
		sorting = true;
		
		boolean rankChanged=false;
		try {
			ArrayList<String> myOptions = new ArrayList<String>();
			synchronized (getWorkspaceData()) {
				for (String o : getWorkspaceData().keySet()) {
					//System.out.println("Opt: "+o);
					myOptions.add(o);
				}
			}
			ArrayList<String> menu = new ArrayList<>();
			while (myOptions.size() > 0) {
				int bestIndex = 0;
				String besturl = (String) myOptions.get(bestIndex);
				long newestTime = Long.parseLong(((ArrayList<String>) getWorkspaceData().get(besturl)).get(1));
				for (int i = 0; i < myOptions.size(); i++) {
					String nowurl = (String) myOptions.get(i);
					long myTime = Long.parseLong(((ArrayList<String>) getWorkspaceData().get(nowurl)).get(1));
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
						remove(removedURL);
					}

				} else {
					System.out.println("Removing from workspace: " + removedURL);
					remove(removedURL);
				}
			}
			
			for(int i=0;i<menu.size();i++) {
				String url = menu.get(i);
				if(rank.get(url)==null) {
					rankChanged=true;
					rank.put(url,i);
					//System.out.println("Rank firstNoted : "+url+" "+i);
				}
				if(rank.get(url).intValue()!=i) {
					rankChanged=true;
					
				}
				rank.put(url,i);
			}
			if(rankChanged) {
				BowlerStudio.runLater(() -> {
					if (workspaceMenu.getItems() != null)
						workspaceMenu.getItems().clear();
					
					new Thread(() -> {
						for (String url : menu) {
							System.out.println("Workspace : "+url);
								ArrayList<String> arrayList = (ArrayList<String>) getWorkspaceData().get(url);
								if(arrayList!=null)
									BowlerStudioMenu.setUpRepoMenue(workspaceMenu, 
											url, 
											false, 
											false,
											arrayList.get(0));
							
						}
						sorting = false;
					}).start();
				});
			}else {
				sorting = false;
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if(rankChanged) {
			System.out.println("Sorting workspace...");
			new Thread(()->{
				ConfigurationDatabase.save();
			}).start();
		}
	}

	public static HashMap<String, Object> getWorkspaceData() {
		return ConfigurationDatabase.getParamMap("workspace");
	}

	public static void remove(String url) {
		synchronized (getWorkspaceData()) {
			getWorkspaceData().remove(url);
		}
	}


}
