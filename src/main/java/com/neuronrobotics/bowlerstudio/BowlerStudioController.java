package com.neuronrobotics.bowlerstudio;

import eu.mihosoft.vrl.v3d.CSG;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.text.BadLocationException;



//import org.bytedeco.javacpp.DoublePointer;












import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.stage.Stage;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;
import com.neuronrobotics.bowlerstudio.scripting.IScriptEventListener;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingFileWidget;
import com.neuronrobotics.bowlerstudio.tabs.LocalFileScriptTab;
import com.neuronrobotics.bowlerstudio.threed.BowlerStudio3dEngine;
import com.neuronrobotics.bowlerstudio.threed.MobileBaseCadManager;
import com.neuronrobotics.imageprovider.AbstractImageProvider;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.util.ThreadUtil;

@SuppressWarnings("restriction")
public class BowlerStudioController  implements
		IScriptEventListener {


	/**
	 * 
	 */
	private ConnectionManager connectionManager;
	private BowlerStudio3dEngine jfx3dmanager;
	private AbstractImageProvider vrCamera;
	private static BowlerStudioController bowlerStudioControllerStaticReference=null;
	private boolean doneLoadingTutorials = false;
	public BowlerStudioController(BowlerStudio3dEngine jfx3dmanager) {
		if(getBowlerStudio()!=null)
			throw new RuntimeException("There can be only one Bowler Studio controller");
		bowlerStudioControllerStaticReference=this;
		this.setJfx3dmanager(jfx3dmanager);

		
	}
	private HashMap<String,Tab> openFiles = new HashMap<>();
	private HashMap<String,LocalFileScriptTab> widgets = new HashMap<>();
	// Custom function for creation of New Tabs.
	public ScriptingFileWidget createFileTab(File file) {
		if(openFiles.get(file.getAbsolutePath())!=null && widgets.get(file.getAbsolutePath())!=null){
			BowlerStudioModularFrame.getBowlerStudioModularFrame().setSelectedTab(openFiles.get(file.getAbsolutePath()));
			return widgets.get(file.getAbsolutePath()).getScripting();
		}

		Tab fileTab =new Tab(file.getName());
		openFiles.put(file.getAbsolutePath(), fileTab);
		
		try {
			Log.warning("Loading local file from: "+file.getAbsolutePath());
			LocalFileScriptTab t  =new LocalFileScriptTab( file);
			String key =t.getScripting().getGitRepo()+":"+t.getScripting().getGitFile();
			ArrayList<String> files = new ArrayList<>();
			files.add(t.getScripting().getGitRepo());
			files.add(t.getScripting().getGitFile());
			ConfigurationDatabase.setObject(
					"studio-open-git", 
					key, 
					files);
			
			fileTab.setContent(t);
			fileTab.setGraphic(AssetFactory.loadIcon("Script-Tab-"+ScriptingEngine.getShellType(file.getName())+".png"));
			addTab(fileTab, true);
			widgets.put(file.getAbsolutePath(),  t);
			fileTab.setOnCloseRequest(event->{
				widgets.remove(file.getAbsolutePath());
				openFiles.remove(file.getAbsolutePath());
				ConfigurationDatabase.removeObject(
						"studio-open-git", 
						key);
			});
			return t.getScripting();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	public void clearHighlits(){
		for(Entry<String, LocalFileScriptTab> set: widgets.entrySet()){
			set.getValue().clearHighlits();
		}
	}
	
	public void setHighlight(File fileEngineRunByName, int lineNumber, Color color) {
		System.out.println("Highlighting line "+lineNumber+" in "+fileEngineRunByName);
		if(openFiles.get(fileEngineRunByName.getAbsolutePath())==null){
			createFileTab(fileEngineRunByName);
			ThreadUtil.wait(100);
		}
		
		//BowlerStudioModularFrame.getBowlerStudioModularFrame().setSelectedTab(openFiles.get(fileEngineRunByName.getAbsolutePath()));
		//System.out.println("Highlighting "+fileEngineRunByName+" at line "+lineNumber+" to color "+color);
		try {
			widgets.get(fileEngineRunByName.getAbsolutePath()).setHighlight(lineNumber,color);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	public static void highlightException(File fileEngineRunByName, Exception ex){
		bowlerStudioControllerStaticReference.highlightExceptionLocal(fileEngineRunByName, ex);
	}
	public static void clearHighlight(){
		bowlerStudioControllerStaticReference.clearHighlits();
	}
	private void highlightExceptionLocal(File fileEngineRunByName, Exception ex) {
		new Thread(){
			public void run(){
				setName("Highlighter thread");
				if(fileEngineRunByName!=null){
					if(openFiles.get(fileEngineRunByName.getAbsolutePath())==null){
						createFileTab(fileEngineRunByName);
					}
					BowlerStudioModularFrame.getBowlerStudioModularFrame().setSelectedTab(openFiles.get(fileEngineRunByName.getAbsolutePath()));
					widgets.get(fileEngineRunByName.getAbsolutePath()).clearHighlits();
					//System.out.println("Highlighting "+fileEngineRunByName+" at line "+lineNumber+" to color "+color);
					for(StackTraceElement el:ex.getStackTrace()){
						try {
							//System.out.println("Compairing "+fileEngineRunByName.getName()+" to "+el.getFileName());
							if(el.getFileName().contentEquals(fileEngineRunByName.getName())){
								widgets.get(fileEngineRunByName.getAbsolutePath()).setHighlight(el.getLineNumber(),Color. CYAN);
							}
						} catch (Exception e) {
//							StringWriter sw = new StringWriter();
//							PrintWriter pw = new PrintWriter(sw);
//							e.printStackTrace(pw);
//							System.out.println(sw.toString());
						}
					}
					
				}
				try{
					if(widgets.get(fileEngineRunByName.getAbsolutePath())!=null){
						String message = ex.getMessage();
						//System.out.println(message);
						if(message!=null && message.contains(fileEngineRunByName.getName()))
							try {
								int indexOfFile = message.lastIndexOf(fileEngineRunByName.getName());
								String fileSub=message.substring(indexOfFile);
								String [] fileAndNum =fileSub .split(":");
								String FileNum = fileAndNum[1];
								int linNum =  Integer.parseInt(FileNum.trim());
								widgets.get(fileEngineRunByName.getAbsolutePath()).setHighlight(linNum,Color.CYAN);
							} catch (Exception e) {
								StringWriter sw = new StringWriter();
								PrintWriter pw = new PrintWriter(sw);
								e.printStackTrace(pw);
								System.out.println(sw.toString());
							}
					}
				}catch(Exception ex){
					
				}
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				ex.printStackTrace(pw);
				System.out.println(sw.toString());
				ex.printStackTrace();
			}
		}.start();

		
	}
	
	


	public void addTab(Tab tab, boolean closable) {

		//new RuntimeException().printStackTrace();

		Platform.runLater(() -> {
			BowlerStudioModularFrame.getBowlerStudioModularFrame().addTab(tab, closable);
		});
		
	}





	private boolean removeObject(Object p) {
		if (CSG.class.isInstance(p)) {
			Platform.runLater(() -> {
				getJfx3dmanager().removeObjects();
			});
			return true;
		} 
		if (Node.class.isInstance(p)) {
			getJfx3dmanager().clearUserNode();
			
			return true;
		} 
		ThreadUtil.wait(20);
		return false;
	}
	
	public static void setCsg(List<CSG> toadd, File source){
		Platform.runLater(() -> {
			getBowlerStudio().getJfx3dmanager().removeObjects();
			if(toadd!=null)
			for(CSG c:toadd){
				Platform.runLater(() ->getBowlerStudio().getJfx3dmanager().addObject(c,source));
			}
		});
	}
	public static void setCsg(List<CSG> toadd){
		setCsg(toadd,null);
	}
	public static void addCsg(CSG toadd){
		addCsg(toadd,null);
	}
	
	public static void setUserNode(List<Node> toadd){
		Platform.runLater(() -> {
			getBowlerStudio().getJfx3dmanager().clearUserNode();
			if(toadd!=null)
			for(Node c:toadd){
				getBowlerStudio().getJfx3dmanager().addUserNode(c);
			}
		});
	}
	public static void addUserNode(Node toadd){
		Platform.runLater(() -> {
			if(toadd!=null)
				getBowlerStudio().getJfx3dmanager().addUserNode(toadd);
			
		});
	}
	public static void addCsg(CSG toadd, File source){
		Platform.runLater(() -> {
			if(toadd!=null)
				getBowlerStudio().getJfx3dmanager().addObject(toadd,source);
			
		});
	}
	private void addObject(Object o, File source) {
		
		if (List.class.isInstance(o)) {
			List<Object> c = (List<Object>) o;
			for (int i = 0; i < c.size(); i++) {
				//Log.warning("Loading array Lists with removals " + c.get(i));
				addObject(c.get(i),  source);
			}
			return;
		} 
		
		if (CSG.class.isInstance(o)) {
			CSG csg = (CSG) o;
			Platform.runLater(() -> {
				// new RuntimeException().printStackTrace();
				getJfx3dmanager().addObject(csg,source);
			});
			
		} else if (Tab.class.isInstance(o)) {

			addTab((Tab) o, true);

		}
		else if (Node.class.isInstance(o)) {

			addNode((Node) o);

		}
		
		if (BowlerAbstractDevice.class.isInstance(o)) {
			BowlerAbstractDevice bad = (BowlerAbstractDevice) o;
			ConnectionManager.addConnection((BowlerAbstractDevice) o,
					bad.getScriptingName());
		}
	}

	public void addNode(Node o) {
		getJfx3dmanager().addUserNode(o);
	}


	@SuppressWarnings({ "unchecked" })
	@Override
	public void onScriptFinished(Object result, Object Previous,File source) {
		Log.warning("Loading script results " + result + " previous "
				+ Previous);
		// this is added in the script engine when the connection manager is
		// loaded

		ThreadUtil.wait(20);
		
		clearObjects(Previous);
		
		//Check if a CSG is coming in and clear the screen first
		clearObjects(result);
		
		if (List.class.isInstance(result)) {
			List<Object> c = (List<Object>) result;
			for (int i = 0; i < c.size(); i++) {
				//Log.warning("Loading array Lists with removals " + c.get(i));
				addObject(c.get(i),  source);
			}
		} else {
			addObject(result,  source);
		}
	}
	
	private void clearObjects(Object o){
		if (List.class.isInstance(o)) {
			List<Object> c = (List<Object>) o;
			for (int i = 0; i < c.size(); i++) {
				removeObject(c.get(i));
			}
		} else {
			removeObject(o);
		}
	}

	@Override
	public void onScriptChanged(String previous, String current,File source) {

	}

	@Override
	public void onScriptError(Exception except,File source) {
		// TODO Auto-generated method stub

	}

	public void disconnect() {
		ConnectionManager.disconnectAll();
	}

	public Stage getPrimaryStage() {
		// TODO Auto-generated method stub
		return BowlerStudioModularFrame.getPrimaryStage();
	}

	public AbstractImageProvider getVrCamera() {
		return vrCamera;
	}

	public void setVrCamera(AbstractImageProvider vrCamera) {
		this.vrCamera = vrCamera;
	}

	public static BowlerStudioController getBowlerStudio() {
		return bowlerStudioControllerStaticReference;
	}


	public static void setup() {
		// TODO Auto-generated method stub
		
	}
	
	public static void clearCSG() {
		Platform.runLater(() -> {
			getBowlerStudio().getJfx3dmanager().removeObjects();
		});
	}

	public static void setCsg(CSG legAssembly, File cadScript) {
		Platform.runLater(() -> {
			getBowlerStudio().getJfx3dmanager().removeObjects();
			if(legAssembly!=null)
	
				Platform.runLater(() ->getBowlerStudio().getJfx3dmanager().addObject(legAssembly,cadScript));
			
		});
	}


	public static void setCsg(MobileBaseCadManager thread, File cadScript) {
		setCsg(thread.getAllCad(), cadScript);
	}


	public BowlerStudio3dEngine getJfx3dmanager() {
		return jfx3dmanager;
	}


	private void setJfx3dmanager(BowlerStudio3dEngine jfx3dmanager) {
		this.jfx3dmanager = jfx3dmanager;
	}


	public boolean isDoneLoadingTutorials() {
		return doneLoadingTutorials;
	}


	public void setDoneLoadingTutorials(boolean doneLoadingTutorials) {
		this.doneLoadingTutorials = doneLoadingTutorials;
	}


	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}


	public void setConnectionManager(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	

}