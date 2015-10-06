package com.neuronrobotics.bowlerstudio.creature;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.HashMap;

import org.python.core.exceptions;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Callback;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.ConnectionManager;
import com.neuronrobotics.bowlerstudio.scripting.IScriptEventListener;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngineWidget;
import com.neuronrobotics.bowlerstudio.scripting.ShellType;
import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.nrconsole.util.DirectoryFilter;
import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.nrconsole.util.GroovyFilter;
import com.neuronrobotics.nrconsole.util.XmlFilter;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.DhInverseSolver;
import com.neuronrobotics.sdk.addons.kinematics.DrivingType;
import com.neuronrobotics.sdk.addons.kinematics.IDriveEngine;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.IDeviceConnectionEventListener;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.util.FileChangeWatcher;
import com.neuronrobotics.sdk.util.IFileChangeListener;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;
import eu.mihosoft.vrl.v3d.STL;
import eu.mihosoft.vrl.v3d.Transform;

public class CreatureLab extends AbstractBowlerStudioTab implements ICadGenerator, IOnEngineeringUnitsChange {

	private ICadGenerator cadEngine;
	private BowlerAbstractDevice pm;
	private File openMobileBaseConfiguration;
	private File cadScript;
	private FileChangeWatcher watcher;
	private IDriveEngine defaultDriveEngine;
	private DhInverseSolver defaultDHSolver;
	private Menu localMenue;
	private ProgressIndicator pi;

	@Override
	public void onTabClosing() {
		// TODO Auto-generated method stub
		if (watcher != null) {
			watcher.close();
		}
	}

	@Override
	public String[] getMyNameSpaces() {
		// TODO Auto-generated method stub
		return new String[0];
	}

	@Override
	public void initializeUI(BowlerAbstractDevice pm) {
		this.pm = pm;
		// TODO Auto-generated method stub
		setText(pm.getScriptingName());

		GridPane dhlabTopLevel=new GridPane();
		
		


		if(DHParameterKinematics.class.isInstance(pm)){
			DHParameterKinematics device=(DHParameterKinematics)pm;
        	try {
        		setDefaultDhParameterKinematics(device);
        		
			} catch (Exception e) {
				  StringWriter sw = new StringWriter();
			      PrintWriter pw = new PrintWriter(sw);
			      e.printStackTrace(pw);
			      System.out.println(sw.toString());
			}
			Log.debug("Loading xml: "+device.getXml());
			dhlabTopLevel.add(new DhChainWidget(device, null), 0, 0);
		}else if(MobileBase.class.isInstance(pm)) {
			MobileBase device=(MobileBase)pm;
			Menu CreaturLabMenue =BowlerStudio.getCreatureLabMenue();
			localMenue = new Menu(pm.getScriptingName());
			MenuItem printable = new MenuItem("Generate Printable CAD");
			printable.setOnAction(event -> {
				File defaultStlDir =new File(System.getProperty("user.home")+"/bowler-workspace/STL/");
				if(!defaultStlDir.exists()){
					defaultStlDir.mkdirs();
				}
				DirectoryChooser chooser = new DirectoryChooser();
				chooser.setTitle("Select Output Directory For .STL files");
			
				chooser.setInitialDirectory(defaultStlDir);
    	    	File baseDirForFiles = chooser.showDialog(BowlerStudio.getPrimaryStage());

    	        if (baseDirForFiles == null) {
    	            return;
    	        }
		    	new Thread(){

					public void run(){
						
		    	        ArrayList<File> files = cadEngine.generateStls((MobileBase) pm, baseDirForFiles);
		    	        Platform.runLater(()->{
		    				Alert alert = new Alert(AlertType.INFORMATION);
		    				alert.setTitle("Stl Export Success!");
		    				alert.setHeaderText("Stl Export Success");
		    				alert.setContentText("All SLT's for the Creature Generated at\n"+files.get(0).getAbsolutePath());
		    				alert.setWidth(500);
		    				alert .initModality(Modality.APPLICATION_MODAL);
		    				alert.show();
		    	        });
		    		}
		    	}.start();
			});
			
			
			MenuItem saveConfig = new MenuItem("Save Configuration");
			saveConfig.setOnAction(event -> {
		    	new Thread(){

					public void run(){
						if(device.getSelfSource()[0]==null ||device.getSelfSource()[1]==null ){
							// this was loaded form a file not a gist
							if(openMobileBaseConfiguration==null)
								openMobileBaseConfiguration=ScriptingEngineWidget.getLastFile();
			    	    	openMobileBaseConfiguration = FileSelectionFactory.GetFile(openMobileBaseConfiguration,
			    	    			new ExtensionFilter("MobileBase XML","*.xml","*.XML"));

			    	        if (openMobileBaseConfiguration == null) {
			    	            return;
			    	        }
			    	        try {
								PrintWriter out = new PrintWriter(openMobileBaseConfiguration.getAbsoluteFile());
								out.println(device.getXml());
								out.flush();
								out.close();
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}else{
							String owner = ScriptingEngine.getUserIdOfGist(device.getSelfSource()[0]);
							String currentLoggedIn =  ScriptingEngine.getLoginID();
							Log.warning("Gist owned by "+owner+" logged in as "+currentLoggedIn);
							if(currentLoggedIn!=null && owner!=null){
								if(currentLoggedIn.toLowerCase().contentEquals(owner.toLowerCase())){
									try {
										ScriptingEngine.pushCodeToGistID( 
												device.getSelfSource()[0], 
												device.getSelfSource()[1],
												device.getXml());
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
						}

		    	        
		    		}
		    	}.start();
			});
			
			
			MenuItem setCadScript = new MenuItem("Set Cad Generation Script");
			
			setCadScript.setOnAction(event -> {
		    	new Thread(){

					public void run(){
						setName("Cad generation thread");
					
		    	    	setCadScript(FileSelectionFactory.GetFile(getCadScript()!=null?getCadScript():ScriptingEngineWidget.getLastFile(),
		    	    			new ExtensionFilter("Kinematics Script","*.groovy","*.java","*.txt")));

		    	        if (getCadScript() == null) {
		    	            return;
		    	        }
		    	        generateCad();
		    	        
		    		}
		    	}.start();
			});
			MenuItem updateRobotScripts = new MenuItem("Pull Scripts from Server");
			updateRobotScripts.setOnAction(event -> {
		    	new Thread(){

					public void run(){
						setName("Cad generation thread");
						cadEngine=null;
						defaultDriveEngine=null;
						try {
							setDefaultLinkLevelCadEngine();
							setDefaultWalkingEngine(device);
			    	        generateCad();
						} catch (Exception e) {
							  StringWriter sw = new StringWriter();
						      PrintWriter pw = new PrintWriter(sw);
						      e.printStackTrace(pw);
						      System.out.println(sw.toString());
						}
						
		    	        
		    		}
		    	}.start();
			});
			localMenue.getItems().addAll(printable, saveConfig, setCadScript, updateRobotScripts);
			
			
			CreaturLabMenue.getItems().add(localMenue);
			CreaturLabMenue.setDisable(false);
			pm.addConnectionEventListener(new IDeviceConnectionEventListener() {
				@Override
				public void onDisconnect(BowlerAbstractDevice source) {
					// cleanup menues after add
					CreaturLabMenue.getItems().remove(localMenue);
					if(CreaturLabMenue.getItems().size()==0)
						CreaturLabMenue.setDisable(true);
					BowlerStudioController.setCsg(null);
				}
				
				@Override
				public void onConnect(BowlerAbstractDevice source) {}
			});
			
	
			//Button save = new Button("Save Configuration");
			
			
			try {
				setDefaultWalkingEngine(device);
			} catch (Exception e) {
				  StringWriter sw = new StringWriter();
			      PrintWriter pw = new PrintWriter(sw);
			      e.printStackTrace(pw);
			      System.out.println(sw.toString());
			}
			
			Group controls = new Group();
			Accordion advancedPanel = new Accordion();
			TitledPane rp =new TitledPane("Walking Engine", new JogWidget(device));
			advancedPanel.getPanes().add(rp);

			TreeItem<String> rootItem = new TreeItem<String>("Body "+device.getScriptingName());
			rootItem.setExpanded(true);
			HashMap<TreeItem<String>, Runnable> callbackMapForTreeitems = new HashMap<>();
			HashMap<TreeItem<String>, Group> widgetMapForTreeitems = new HashMap<>();
			
			TreeView<String> tree = new TreeView<String>(rootItem);
			MobleBaseFactory.load(device,tree,rootItem,callbackMapForTreeitems,widgetMapForTreeitems, this);

			
			tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
	        tree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Object>() {
	            
	            @Override
	            public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
	                @SuppressWarnings("unchecked")
					TreeItem<String> treeItem = (TreeItem<String>)newValue;
	                new Thread(){
	                	public void run(){
	    	                if(callbackMapForTreeitems.get(treeItem)!=null){
	    	                	callbackMapForTreeitems.get(treeItem).run();
	    	                }
	    	                if(widgetMapForTreeitems.get(treeItem)!=null){
	    	                	
	    	                	Platform.runLater(()->{
	    	                		controls.getChildren().clear();
	    	                		controls.getChildren().add(widgetMapForTreeitems.get(treeItem));
	    	                	});
	    	                }else{
	    	                	Platform.runLater(()->{
	    	                		controls.getChildren().clear();
	    	                	});
	    	                }
	                	}
	                }.start();

	            }     
	        });
//			addAppendagePanel(device.getLegs(),"Legs",advancedPanel);
//			addAppendagePanel(device.getAppendages(),"Appandges",advancedPanel);
//			addAppendagePanel(device.getSteerable(),"Steerable",advancedPanel);
//			addAppendagePanel(device.getDrivable(),"Drivable",advancedPanel);
			HBox progress = new HBox(10);
			pi = new ProgressIndicator(0);
			progress.getChildren().addAll(new Label("Cad Progress:"),pi);
			dhlabTopLevel.add(advancedPanel, 0, 0);
	        dhlabTopLevel.add(progress, 0, 1);
	        
			dhlabTopLevel.add(tree, 0, 2);
			
			dhlabTopLevel.add(controls, 0, 3);
			
			if(device.getDriveType() != DrivingType.NONE){
				advancedPanel.setExpandedPane(rp);
			}
			
		}else if(AbstractKinematicsNR.class.isInstance(pm)) {
			AbstractKinematicsNR device=(AbstractKinematicsNR)pm;
			dhlabTopLevel.add(new DhChainWidget(device,null), 0, 0);
		}
		generateCad();
		
		setContent(new ScrollPane(dhlabTopLevel));
		
		
	}

	private void setDefaultLinkLevelCadEngine() throws Exception {
		String [] cad =null;
		if(MobileBase.class.isInstance(pm)) {
			cad = ((MobileBase)pm).getCadEngine();
		}else if(DHParameterKinematics.class.isInstance(pm)){
			DHParameterKinematics device=(DHParameterKinematics)pm;
			cad = device.getCadEngine();
		}
		if(cadEngine==null){
			String code = ScriptingEngineWidget.codeFromGistID(cad[0],cad[1])[0];
			cadEngine = (ICadGenerator) ScriptingEngine.inlineScriptRun(code, null,ShellType.GROOVY);
		}
	}
	private void setDefaultDhParameterKinematics(DHParameterKinematics device) throws Exception {
		String code = ScriptingEngineWidget.codeFromGistID(device.getDhEngine()[0],device.getDhEngine()[1])[0];
		defaultDHSolver = (DhInverseSolver) ScriptingEngine.inlineScriptRun(code, null,ShellType.GROOVY);
		
		device.setInverseSolver(defaultDHSolver);
	}

	private void setDefaultWalkingEngine(MobileBase device) throws Exception {
		if(defaultDriveEngine==null){
			String code = ScriptingEngineWidget.codeFromGistID(device.getWalkingEngine()[0],device.getWalkingEngine()[1])[0];
			defaultDriveEngine = (IDriveEngine) ScriptingEngine.inlineScriptRun(code, null,ShellType.GROOVY);
		}
		device.setWalkingDriveEngine( defaultDriveEngine);
		for(DHParameterKinematics dh : device.getAllDHChains()){
			setDefaultDhParameterKinematics(dh);
		}
	}
	
	private void addAppendagePanel(ArrayList<DHParameterKinematics> apps,String title,Accordion advancedPanel){
		if(apps.size()>0){
			for(DHParameterKinematics l:apps){
				TitledPane rp =new TitledPane(title+" - "+l.getScriptingName(),new DhChainWidget(l, this));
				rp.setMaxWidth(200);
				rp.setMaxHeight(200);
				advancedPanel.getPanes().add(rp);
				advancedPanel.setExpandedPane(rp);
			}
		}
		
	}
	void generateCad(){
		new Thread(){
			public void run(){
				System.out.print("\r\nGenerating cad...");
				//new Exception().printStackTrace();
				ArrayList<CSG> allCad=new ArrayList<>();
				if(MobileBase.class.isInstance(pm)) {
					MobileBase device=(MobileBase)pm;
					allCad=generateBody(device);
					
				}else if(DHParameterKinematics.class.isInstance(pm)){

					allCad=generateCad(((DHParameterKinematics)pm).getChain().getLinks() );
				}
				System.out.print("Done!\r\n");
				BowlerStudioController.setCsg(allCad);
				
			}


		}.start();
	}

	public ArrayList<CSG> generateCad(ArrayList<DHLink> dhLinks ){
		if (getCadScript() != null) {
			try{
			cadEngine = (ICadGenerator) ScriptingEngine.inlineFileScriptRun(getCadScript(), null);
			}catch(Exception e){
			      StringWriter sw = new StringWriter();
			      PrintWriter pw = new PrintWriter(sw);
			      e.printStackTrace(pw);
			      System.out.println(sw.toString());
			}
        }
		if(cadEngine==null){
			try {
				setDefaultLinkLevelCadEngine();
				} catch (Exception e) {
				  StringWriter sw = new StringWriter();
			      PrintWriter pw = new PrintWriter(sw);
			      e.printStackTrace(pw);
			      System.out.println(sw.toString());
			}
		}
		try {
			return cadEngine.generateCad(dhLinks);
		} catch (Exception e) {
			  StringWriter sw = new StringWriter();
		      PrintWriter pw = new PrintWriter(sw);
		      e.printStackTrace(pw);
		      System.out.println(sw.toString());
		}
		return null;
		
	}


	@Override
	public void onTabReOpening() {
		setCadScript(getCadScript());
		try{
			generateCad();
		}catch(Exception ex){
			
		}
	}
	
	public static String getFormatted(double value){
	    return String.format("%4.3f%n", (double)value);
	}

	public File getCadScript() {
		return cadScript;
	}

	public void setCadScript(File cadScript) {
		if(cadScript==null)
			return;
		if (watcher != null) {
		
			watcher.close();
		}
		 try {
		 watcher = new FileChangeWatcher(cadScript);
		 watcher.addIFileChangeListener(new IFileChangeListener() {
			
			@Override
			public void onFileChange(File fileThatChanged, WatchEvent event) {
				try{
					generateCad();
				}catch(Exception ex){
					 StringWriter sw = new StringWriter();
				      PrintWriter pw = new PrintWriter(sw);
				      ex.printStackTrace(pw);
				      System.out.println(sw.toString());
				}
				
			}
		});
		 watcher.start();
		 } catch (IOException e) {
		 // TODO Auto-generated catch block
		 e.printStackTrace();
		 }
		this.cadScript = cadScript;
		ScriptingEngineWidget scripting = BowlerStudio.createFileTab(cadScript);
		scripting.addIScriptEventListener(new IScriptEventListener() {
			
			@Override
			public void onGroovyScriptFinished(Object result, Object pervious) {
				// TODO Auto-generated method stub
				cadEngine = (ICadGenerator)result;
				generateCad();
			}
			
			@Override
			public void onGroovyScriptError(Exception except) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onGroovyScriptChanged(String previous, String current) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	@Override
	public ArrayList<CSG> generateBody(MobileBase base) {
		pi.setProgress(0);
		ArrayList<CSG> allCad=new ArrayList<>();
		if(MobileBase.class.isInstance(pm)) {
			MobileBase device=(MobileBase)pm;
			if (getCadScript() != null) {
				try{
					cadEngine = (ICadGenerator) ScriptingEngine.inlineFileScriptRun(getCadScript(), null);
				}catch(Exception e){
				      StringWriter sw = new StringWriter();
				      PrintWriter pw = new PrintWriter(sw);
				      e.printStackTrace(pw);
				      System.out.println(sw.toString());
				}
	        }
			if(cadEngine==null){
				try {
					setDefaultLinkLevelCadEngine();
				} catch (Exception e) {
					  StringWriter sw = new StringWriter();
				      PrintWriter pw = new PrintWriter(sw);
				      e.printStackTrace(pw);
				      System.out.println(sw.toString());
				}
			}
			pi.setProgress(0.5);
			try {
				allCad= cadEngine.generateBody(device);
			} catch (Exception e) {
				  StringWriter sw = new StringWriter();
			      PrintWriter pw = new PrintWriter(sw);
			      e.printStackTrace(pw);
			      System.out.println(sw.toString());
			}
			
		}else if(DHParameterKinematics.class.isInstance(pm)){
			for(CSG csg:generateCad(((DHParameterKinematics)pm).getChain().getLinks())){
				allCad.add(csg);
			}
		}
		BowlerStudio.setSelectedTab(this);
		pi.setProgress(1);
		return allCad;
	}

	@Override
	public ArrayList<File> generateStls(MobileBase base, File baseDirForFiles) {
		// TODO Auto-generated method stub
		return  cadEngine.generateStls(base, baseDirForFiles);
	}

	@Override
	public void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSliderDoneMoving(EngineeringUnitsSliderWidget source,
			double newAngleDegrees) {
		generateCad();
	}

}
