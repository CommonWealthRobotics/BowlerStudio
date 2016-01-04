package com.neuronrobotics.bowlerstudio;

import eu.mihosoft.vrl.v3d.CSG;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.text.BadLocationException;

//import org.bytedeco.javacpp.DoublePointer;












import org.reactfx.util.FxTimer;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Affine;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

import com.neuronrobotics.bowlerstudio.scripting.IScriptEventListener;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingFileWidget;
import com.neuronrobotics.bowlerstudio.tabs.LocalFileScriptTab;
import com.neuronrobotics.bowlerstudio.tabs.ScriptingGistTab;
import com.neuronrobotics.bowlerstudio.threed.BowlerStudio3dEngine;
import com.neuronrobotics.imageprovider.AbstractImageProvider;
import com.neuronrobotics.imageprovider.Detection;
import com.neuronrobotics.imageprovider.HaarDetector;
import com.neuronrobotics.imageprovider.IObjectDetector;
import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.nrconsole.util.GroovyFilter;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.IConnectionEventListener;
import com.neuronrobotics.sdk.common.IDeviceConnectionEventListener;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.util.RollingAverageFilter;
import com.neuronrobotics.sdk.util.ThreadUtil;
import com.sun.javafx.scene.control.behavior.TabPaneBehavior;
import com.sun.javafx.scene.control.skin.TabPaneSkin;

public class BowlerStudioController extends TabPane implements
		IScriptEventListener {

	private static final String HOME_URL = "http://neuronrobotics.com/BowlerStudio/Welcome-To-BowlerStudio/";
	/**
	 * 
	 */
	private static final long serialVersionUID = -2686618188618431477L;
	private ConnectionManager connectionManager;
	private BowlerStudio3dEngine jfx3dmanager;
	private MainController mainController;
	private AbstractImageProvider vrCamera;
	private static BowlerStudioController bowlerStudio=null;
	private Stage dialog = new Stage();
	public BowlerStudioController(BowlerStudio3dEngine jfx3dmanager,
			MainController mainController) {
		if(getBowlerStudio()!=null)
			throw new RuntimeException("There can be only one Bowler Studio controller");
		bowlerStudio=this;
		this.jfx3dmanager = jfx3dmanager;
		this.mainController = mainController;
		createScene();
	}
	private HashMap<File,Tab> openFiles = new HashMap<>();
	private HashMap<File,LocalFileScriptTab> widgets = new HashMap<>();
	private HashMap<String,ScriptingGistTab> webTabs = new HashMap<>();
	// Custom function for creation of New Tabs.
	public ScriptingFileWidget createFileTab(File file) {
		if(openFiles.get(file)!=null){
			setSelectedTab(openFiles.get(file));
			return widgets.get(file).getScripting();
		}

		Tab fileTab =new Tab(file.getName());
		openFiles.put(file, fileTab);
		try {
			Log.warning("Loading local file from: "+file.getAbsolutePath());
			LocalFileScriptTab t  =new LocalFileScriptTab( file);
			
			fileTab.setContent(t);
			addTab(fileTab, true);
			widgets.put(file,  t);
			fileTab.setOnCloseRequest(event->{
				widgets.remove(file);
				openFiles.remove(file);
			});
			return t.getScripting();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void clearHighlits(){
		for(Entry<File, LocalFileScriptTab> set: widgets.entrySet()){
			set.getValue().clearHighlits();
		}
	}
	
	public void setHighlight(File fileEngineRunByName, int lineNumber, Color color) {
		if(openFiles.get(fileEngineRunByName)==null){
			createFileTab(fileEngineRunByName);
		}
		setSelectedTab(openFiles.get(fileEngineRunByName));
		//System.out.println("Highlighting "+fileEngineRunByName+" at line "+lineNumber+" to color "+color);
		try {
			widgets.get(fileEngineRunByName).setHighlight(lineNumber,color);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	public static void highlightException(File fileEngineRunByName, Exception ex){
		bowlerStudio.highlightExceptionLocal(fileEngineRunByName, ex);
	}
	private void highlightExceptionLocal(File fileEngineRunByName, Exception ex) {
		
		if(fileEngineRunByName!=null){
			if(openFiles.get(fileEngineRunByName)==null){
				createFileTab(fileEngineRunByName);
			}
			setSelectedTab(openFiles.get(fileEngineRunByName));
			widgets.get(fileEngineRunByName).clearHighlits();
			//System.out.println("Highlighting "+fileEngineRunByName+" at line "+lineNumber+" to color "+color);
			for(StackTraceElement el:ex.getStackTrace()){
				if(el.getFileName().contentEquals(fileEngineRunByName.getName())){
					try {
						widgets.get(fileEngineRunByName).setHighlight(el.getLineNumber(),Color.RED);
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
			}
			
			
		}
		if(org.codehaus.groovy.control.MultipleCompilationErrorsException.class.isInstance(ex)){
			String message = ex.getMessage();
			System.out.println(message);
			if(message.contentEquals(fileEngineRunByName.getName())){
				int linNum =  Integer.parseInt(message.split(":")[1]);
				try {
					widgets.get(fileEngineRunByName).setHighlight(linNum,Color.RED);
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}else{
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			System.out.println(sw.toString());
		}
	}
	

	// Custom function for creation of New Tabs.
	private void createAndSelectNewTab(final BowlerStudioController tabPane,
			final String title) {


			Platform.runLater(() -> {
				try {
					if(ScriptingEngine.getLoginID() != null)
						
						addTab(new ScriptingGistTab(title,getHomeUrl(), true), false);
				} catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});


	}
	
	public void openUrlInNewTab(URL url){
		String urlstr=url.toExternalForm();
		
		if(webTabs.get(urlstr)!=null){
			setSelectedTab(webTabs.get(urlstr));
		}else
			Platform.runLater(() -> {
				try {
					if(ScriptingEngine.getLoginID() != null){
						ScriptingGistTab newTab = new ScriptingGistTab("Web",url.toExternalForm(), false);
						newTab.setOnCloseRequest(event -> {
							webTabs.remove(urlstr );
						});
						webTabs.put(urlstr,newTab );
						addTab(newTab, true);
					}
				} catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
	}

	private Tab createTab() throws IOException, InterruptedException {
		final ScriptingGistTab tab = new ScriptingGistTab(null,
				 null);

		return tab;
	}

	public void addTab(Tab tab, boolean closable) {

		//new RuntimeException().printStackTrace();

		Platform.runLater(() -> {
			final ObservableList<Tab> tabs = getTabs();
			tab.setClosable(closable);
			int index = tabs.size() - 1;
			//new RuntimeException().printStackTrace();
			tabs.add(index, tab);
			setSelectedTab(tab);
		});
		
	}

	public void createScene() {

		// BorderPane borderPane = new BorderPane();

		// Placement of TabPane.
		setSide(Side.TOP);

		/*
		 * To disable closing of tabs.
		 * tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
		 */

		final Tab newtab = new Tab();
		newtab.setText("+");
		newtab.setClosable(false);

		// Addition of New Tab to the tabpane.
		getTabs().addAll(newtab);

		Tab t=new Tab();
		try {
			t = new ScriptingGistTab("Tutorial",getHomeUrl(), true);
		} catch (IOException | InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Tab tab =t;
		Platform.runLater(() -> {
			final ObservableList<Tab> tabs = getTabs();
			ConnectionManager.getConnectionManager().setClosable(false);
			int index = tabs.size() - 1;
			//new RuntimeException().printStackTrace();
			tabs.add(index, ConnectionManager.getConnectionManager());
			setSelectedTab(ConnectionManager.getConnectionManager());

			tab.setClosable(false);
			index = tabs.size() - 1;
			//new RuntimeException().printStackTrace();
			tabs.add(index, tab);
			setSelectedTab(tab);

		});

		// Function to add and display new tabs with default URL display.
		getSelectionModel().selectedItemProperty().addListener(
				new ChangeListener<Tab>() {
					@Override
					public void changed(
							ObservableValue<? extends Tab> observable,
							Tab oldSelectedTab, Tab newSelectedTab) {
						if (newSelectedTab == newtab) {

							try {
								addTab(createTab(), true);

							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}
				});
	}

	public static String getHomeUrl() {

		return HOME_URL;
	}

	public void open() {
		File last = FileSelectionFactory.GetFile(
				ScriptingEngine.getWorkspace(), new ExtensionFilter("Groovy Scripts","*.groovy","*.java","*.txt"));
		if (last != null) {
			createFileTab(last);
		}
	}

	private void removeObject(Object p) {
		if (CSG.class.isInstance(p)) {
			Platform.runLater(() -> {
				jfx3dmanager.removeObjects();
			});
		} else if (Tab.class.isInstance(p)) {
			Platform.runLater(() -> {
				// new RuntimeException().printStackTrace();
				getTabs().remove(p);
				Tab t = (Tab) p;
				TabPaneBehavior behavior = ((TabPaneSkin) getSkin())
						.getBehavior();
				if (behavior.canCloseTab(t)) {
					behavior.closeTab(t);
				}
			});
		}
	}
	
	public static void setCsg(List<CSG> toadd){
		Platform.runLater(() -> {
			getBowlerStudio().jfx3dmanager.removeObjects();
			if(toadd!=null)
			for(CSG c:toadd){
				Platform.runLater(() ->getBowlerStudio().jfx3dmanager.addObject(c));
			}
		});
	}
	public static void addCsg(CSG toadd){
		Platform.runLater(() -> {
			if(toadd!=null)
				getBowlerStudio().jfx3dmanager.addObject(toadd);
			
		});
	}
	private void addObject(Object o) {
		if (CSG.class.isInstance(o)) {
			CSG csg = (CSG) o;
			Platform.runLater(() -> {
				// new RuntimeException().printStackTrace();
				jfx3dmanager.addObject(csg);
			});
		} else if (Tab.class.isInstance(o)) {

			addTab((Tab) o, true);

		}if (BowlerAbstractDevice.class.isInstance(o)) {
			BowlerAbstractDevice bad = (BowlerAbstractDevice) o;
			ConnectionManager.addConnection((BowlerAbstractDevice) o,
					bad.getScriptingName());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onGroovyScriptFinished(Object result, Object Previous) {
		Log.warning("Loading script results " + result + " previous "
				+ Previous);
		// this is added in the script engine when the connection manager is
		// loaded
		if (ArrayList.class.isInstance(Previous)) {
			ArrayList<Object> c = (ArrayList<Object>) Previous;
			for (int i = 0; i < c.size(); i++) {
				removeObject(c.get(i));
			}
		} else {
			removeObject(Previous);
		}
		if (ArrayList.class.isInstance(result)) {
			ArrayList<Object> c = (ArrayList<Object>) result;
			for (int i = 0; i < c.size(); i++) {
				Log.warning("Loading array Lists with removals " + c.get(i));
				addObject(c.get(i));
			}
		} else {
			addObject(result);
		}
	}

	@Override
	public void onGroovyScriptChanged(String previous, String current) {

	}

	@Override
	public void onGroovyScriptError(Exception except) {
		// TODO Auto-generated method stub

	}

	public void disconnect() {
		ConnectionManager.disconnectAll();
	}

	public Stage getPrimaryStage() {
		// TODO Auto-generated method stub
		return BowlerStudio.getPrimaryStage();
	}

	public void setSelectedTab(Tab tab) {
		if(getSelectionModel().getSelectedItem()!=tab){
			Platform.runLater(() -> {
				System.out.println("Selecting "+tab.getText());
				getSelectionModel().select(tab);
			});
		}
	}

	public AbstractImageProvider getVrCamera() {
		return vrCamera;
	}

	public void setVrCamera(AbstractImageProvider vrCamera) {
		this.vrCamera = vrCamera;
	}

	public static BowlerStudioController getBowlerStudio() {
		return bowlerStudio;
	}


	public static void setup() {
		// TODO Auto-generated method stub
		
	}

	

}