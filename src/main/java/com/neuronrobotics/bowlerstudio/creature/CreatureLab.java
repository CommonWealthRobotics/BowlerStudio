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

import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
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
import com.neuronrobotics.bowlerstudio.scripting.ScriptingFileWidget;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingWebWidget;
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
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.STL;
import eu.mihosoft.vrl.v3d.Transform;

public class CreatureLab extends AbstractBowlerStudioTab implements  IOnEngineeringUnitsChange {

	private ICadGenerator cadEngine;
	private BowlerAbstractDevice pm;
	private File openMobileBaseConfiguration;
	private File cadScript;
	private FileChangeWatcher watcher;
	private FileChangeWatcher driveEngineWitcher;
	private HashMap<DHParameterKinematics,FileChangeWatcher> dhKinematicsFileWatchers = new HashMap<>();
	private HashMap<DHParameterKinematics,FileChangeWatcher> dhCadWatchers = new HashMap<>();
	private HashMap<DHParameterKinematics,ICadGenerator> dhCadGen = new HashMap<>();
	private IDriveEngine defaultDriveEngine;
	//private DhInverseSolver defaultDHSolver;
	private Menu localMenue;
	private ProgressIndicator pi;
	private boolean cadGenerating = false;
	private AbstractGameController gameController = new AbstractGameController() {
		
		@Override
		public double getNavUp() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public double getNavRight() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public double getNavLeft() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public double getNavDown() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public double getControls3Plus() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public double getControls3Minus() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public double getControls2Plus() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public double getControls2Minus() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public double getControls1Plus() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public double getControls1Minus() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public double getControls0Plus() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public double getControls0Minus() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public double getActionRight() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public double getActionLeft() {
			// TODO Auto-generated method stub
			return 0;
		}
	};
	private ArrayList<CSG> allCad;

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
						
		    	        ArrayList<File> files;
						try {
							files = generateStls((MobileBase) pm, baseDirForFiles);
							 Platform.runLater(()->{
				    				Alert alert = new Alert(AlertType.INFORMATION);
				    				alert.setTitle("Stl Export Success!");
				    				alert.setHeaderText("Stl Export Success");
				    				alert.setContentText("All SLT's for the Creature Generated at\n"+files.get(0).getAbsolutePath());
				    				alert.setWidth(500);
				    				alert .initModality(Modality.APPLICATION_MODAL);
				    				alert.show();
				    	        });
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		    	       
		    		}
		    	}.start();
			});
			
			
//			MenuItem saveConfig = new MenuItem("Save Configuration");
//			saveConfig.setOnAction(event -> {
//		    	new Thread(){
//
//					public void run(){
//						if(device.getSelfSource()[0]==null ||device.getSelfSource()[1]==null ){
//							// this was loaded form a file not a gist
//							if(openMobileBaseConfiguration==null)
//								openMobileBaseConfiguration=ScriptingEngine.getLastFile();
//			    	    	openMobileBaseConfiguration = FileSelectionFactory.GetFile(openMobileBaseConfiguration,
//			    	    			new ExtensionFilter("MobileBase XML","*.xml","*.XML"));
//
//			    	        if (openMobileBaseConfiguration == null) {
//			    	            return;
//			    	        }
//			    	        try {
//								PrintWriter out = new PrintWriter(openMobileBaseConfiguration.getAbsoluteFile());
//								out.println(device.getXml());
//								out.flush();
//								out.close();
//							} catch (FileNotFoundException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//						}else{
//							String owner = ScriptingEngine.getUserIdOfGist(device.getSelfSource()[0]);
//							String currentLoggedIn =  ScriptingEngine.getLoginID();
//							Log.warning("Gist owned by "+owner+" logged in as "+currentLoggedIn);
//							if(currentLoggedIn!=null && owner!=null){
//								if(currentLoggedIn.toLowerCase().contentEquals(owner.toLowerCase())){
//									try {
//										ScriptingEngine.pushCodeToGistID( 
//												device.getSelfSource()[0], 
//												device.getSelfSource()[1],
//												device.getXml(), "Updated from "+this.getClass());
//									} catch (Exception e) {
//										// TODO Auto-generated catch block
//										e.printStackTrace();
//									}
//								}
//							}
//						}
//
//		    	        
//		    		}
//		    	}.start();
//			});
			
			
//			MenuItem setCadScript = new MenuItem("Set Cad Generation Script");
//			
//			setCadScript.setOnAction(event -> {
//		    	new Thread(){
//
//					public void run(){
//						setName("Cad generation thread");
//					
//		    	    	setCadScript(FileSelectionFactory.GetFile(getCadScript()!=null?getCadScript():ScriptingEngine.getLastFile(),
//		    	    			new ExtensionFilter("Kinematics Script","*.groovy","*.java","*.txt")));
//
//		    	        if (getCadScript() == null) {
//		    	            return;
//		    	        }
//		    	        generateCad();
//		    	        
//		    		}
//		    	}.start();
//			});
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
			localMenue.getItems().addAll(printable);
			
			
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
			
			
			
			setDefaultWalkingEngine(device);
	
			Group controls = new Group();
			Accordion advancedPanel = new Accordion();
			if(device.getDriveType() ==DrivingType.WALKING){
				TitledPane rp =new TitledPane("Walking Engine", new JogWidget(device));
				advancedPanel.getPanes().add(rp);
				advancedPanel.setExpandedPane(rp);
			}

			TreeItem<String> rootItem = new TreeItem<String>("Move Group "+device.getScriptingName());
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
	    	                		controls.getChildren().add(advancedPanel);
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
			//dhlabTopLevel.add(advancedPanel, 0, 0);
	        dhlabTopLevel.add(progress, 0, 0);
	        
			dhlabTopLevel.add(tree, 0, 1);
			
			dhlabTopLevel.add(controls, 1, 1);
			

			
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
			setCadEngine(cad[0],cad[1],(MobileBase)pm);
		}
	}
	private void setDefaultDhParameterKinematics(DHParameterKinematics device) {
		File code=null;
		try {
			code = ScriptingEngine.fileFromGistID(device.getDhEngine()[0],device.getDhEngine()[1]);
			DhInverseSolver defaultDHSolver = (DhInverseSolver) ScriptingEngine.inlineScriptRun(code, null,ShellType.GROOVY);
			
			if(dhKinematicsFileWatchers.get(device)!=null){
				dhKinematicsFileWatchers.get(device).close();
			}
			FileChangeWatcher w;
			try {
				w = new FileChangeWatcher(code);
				dhKinematicsFileWatchers.put(device, w);
				File c=code;
				w.addIFileChangeListener((fileThatChanged, event) -> {
					try{
						DhInverseSolver d = (DhInverseSolver) ScriptingEngine.inlineScriptRun(c, null,ShellType.GROOVY);
						device.setInverseSolver(d);
					}catch(Exception ex){
						BowlerStudioController.highlightException(c, ex);
					}
				});
				w.start();
			} catch (IOException e) {
				BowlerStudioController.highlightException(code, e);
			}
			
			device.setInverseSolver(defaultDHSolver);
		} catch (Exception e1) {
			BowlerStudioController.highlightException(code, e1);
		}

	}

	private void setDefaultWalkingEngine(MobileBase device){
		if(defaultDriveEngine==null){
			setWalkingEngine(device.getWalkingEngine()[0],device.getWalkingEngine()[1],device);
		}
		for(DHParameterKinematics dh : device.getAllDHChains()){
			setDefaultDhParameterKinematics(dh);
		}
	}
	
	public void setWalkingEngine(String gistID,String file,MobileBase device){
		
		
		device.setWalkingEngine(new String[]{gistID,file});
		File code=null;
		try {
			code = ScriptingEngine.fileFromGistID(gistID,file);
		} catch (GitAPIException | IOException e) {
			BowlerStudioController.highlightException(code, e);
		}
		
		if(driveEngineWitcher!=null)
			driveEngineWitcher.close();
		
		try {
			driveEngineWitcher = new FileChangeWatcher(code);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File c=code;
		driveEngineWitcher.addIFileChangeListener((fileThatChanged, event) -> {
			
			try{

				defaultDriveEngine = (IDriveEngine) ScriptingEngine.inlineScriptRun(c, null,ShellType.GROOVY);
				device.setWalkingDriveEngine( defaultDriveEngine);
			}catch(Exception ex){
				BowlerStudioController.highlightException(c, ex);
			}
			
		});
		driveEngineWitcher.start();
		try{
			defaultDriveEngine = (IDriveEngine) ScriptingEngine.inlineScriptRun(c, null,ShellType.GROOVY);
			device.setWalkingDriveEngine( defaultDriveEngine);
		}catch(Exception ex){
			BowlerStudioController.highlightException(c, ex);
		}
	}
	
	public void setCadEngine(String gitsId, String file, MobileBase device) throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		setCadScript(ScriptingEngine.fileFromGistID(gitsId,file));

		
	}
	public synchronized void generateCad(){
		if(cadGenerating)
			return;
		cadGenerating=true;
		//new RuntimeException().printStackTrace();
		new Thread(){
			public void run(){
				System.out.print("\r\nGenerating cad...");
				//new Exception().printStackTrace();
				ArrayList<CSG> allCad=new ArrayList<>();
				if(MobileBase.class.isInstance(pm)) {
					MobileBase device=(MobileBase)pm;
					allCad=generateBody(device,false);
					
				}else if(DHParameterKinematics.class.isInstance(pm)){

					allCad=generateCad(((DHParameterKinematics)pm),false );
				}
				System.out.print("Done!\r\n");
				BowlerStudioController.setCsg(allCad);
				cadGenerating=false;
			}


		}.start();
	}

	public ArrayList<CSG> generateCad(DHParameterKinematics dh, boolean b ){
		ArrayList<DHLink> dhLinks=dh.getChain().getLinks();
		if (getCadScript() != null) {
			try{
				cadEngine = (ICadGenerator) ScriptingEngine.inlineFileScriptRun(getCadScript(), null);
			}catch(Exception e){
				BowlerStudioController.highlightException(getCadScript(), e);
			}
        }
		if(cadEngine==null){
			try {
				setDefaultLinkLevelCadEngine();
			} catch (Exception e) {
				BowlerStudioController.highlightException(getCadScript(), e);
			}
		}
		try {
			if(dhCadGen.get(dh)!=null){
				try {
					return dhCadGen.get(dh).generateCad(dh,false);
				} catch (Exception e) {
					BowlerStudioController.highlightException(dhCadWatchers.get(dh).getFileToWatch(), e);
				}
			}
			
			return cadEngine.generateCad(dh,false);
		} catch (Exception e) {
			BowlerStudioController.highlightException(getCadScript(), e);
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
					generateCad();
			}
		});
		 watcher.start();
		 } catch (IOException e) {
		 // TODO Auto-generated catch block
		 e.printStackTrace();
		 }
		this.cadScript = cadScript;		
	}


	public ArrayList<CSG> generateBody(MobileBase base, boolean b) {
		pi.setProgress(0);
		allCad = new ArrayList<>();
		if(MobileBase.class.isInstance(pm)) {
			MobileBase device=(MobileBase)pm;

			if(cadEngine==null){
				try {
					setDefaultLinkLevelCadEngine();
				} catch (Exception e) {
					BowlerStudioController.highlightException(null, e);
				}
				if (getCadScript() != null) {
					try{
						cadEngine = (ICadGenerator) ScriptingEngine.inlineFileScriptRun(getCadScript(), null);
					}catch(Exception e){
						BowlerStudioController.highlightException(getCadScript(), e);
					}
		        }
			}
			pi.setProgress(0.3);
			try {
				allCad= cadEngine.generateBody(device,b);

			} catch (Exception e) {
				BowlerStudioController.highlightException(getCadScript(), e);
			}
			//clears old robot and places base
			BowlerStudioController.setCsg(allCad);
			
			pi.setProgress(0.4);
			ArrayList<DHParameterKinematics> limbs = base.getAllDHChains();
			double numLimbs = limbs.size();
			double i=0;
			for(DHParameterKinematics l:limbs){
				
				for(CSG csg:generateCad(l,b)){
					allCad.add(csg);
					BowlerStudioController.addCsg(csg);
				}
				
				i+=1;
				double progress = (1.0-((numLimbs-i)/numLimbs))/2;
				//System.out.println(progress);
				pi.setProgress(0.5+progress);
			}
			
		}else if(DHParameterKinematics.class.isInstance(pm)){
			for(CSG csg:generateCad(((DHParameterKinematics)pm),b)){
				allCad.add(csg);
			}
		}
		BowlerStudio.setSelectedTab(this);
		pi.setProgress(1);
		return allCad;
	}

	public ArrayList<File> generateStls(MobileBase base, File baseDirForFiles) throws IOException {
		ArrayList<File> allCadStl = new ArrayList<>();
		int leg=0;
		//Start by generating the legs using the DH link based generator
		for(DHParameterKinematics l:base.getAllDHChains()){
			int link=0;
			for(CSG csg:generateCad(l,true)){
				File dir = new File(baseDirForFiles.getAbsolutePath()+"/"+base.getScriptingName()+"/"+l.getScriptingName());
				if(!dir.exists())
					dir.mkdirs();
				File stl = new File(dir.getAbsolutePath()+"/Leg_"+leg+"_part_"+link+".stl");
				FileUtil.write(
						Paths.get(stl.getAbsolutePath()),
						csg.toStlString()
				);
				allCadStl.add(stl);
				link++;
			}
			leg++;
		}
		int link=0;
		//now we genrate the base pieces
		for(CSG csg:generateBody( base,true )){
			File dir = new File(baseDirForFiles.getAbsolutePath()+"/"+base.getScriptingName()+"/");
			if(!dir.exists())
				dir.mkdirs();
			File stl = new File(dir.getAbsolutePath()+"/Body_part_"+link+".stl");
			FileUtil.write(
					Paths.get(stl.getAbsolutePath()),
					csg.toStlString()
			);
			allCadStl.add(stl);
			link++;
		}
		 
		return allCadStl;
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

	public AbstractGameController getController() {

		return gameController;
	}

	public void setDhEngine(String gitsId, String file, DHParameterKinematics dh) {
		dh.setDhEngine(new String[]{gitsId,file});
		
		setDefaultDhParameterKinematics(dh);
		
	}

	public void setCadEngine(String gitsId, String file, DHParameterKinematics dh) throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		
		File code = ScriptingEngine.fileFromGistID(gitsId,file);
		try{
			ICadGenerator defaultDHSolver = (ICadGenerator) ScriptingEngine.inlineScriptRun(code, null,ShellType.GROOVY);
			dhCadGen.put(dh, defaultDHSolver);
		}catch(Exception e){
			BowlerStudioController.highlightException(code, e);
		}
		
		if(dhCadWatchers.get(dh)!=null){
			dhCadWatchers.get(dh).close();
		}
		FileChangeWatcher w = new FileChangeWatcher(code);
		dhCadWatchers.put(dh, w);
		w.addIFileChangeListener((fileThatChanged, event) -> {
			try{
				ICadGenerator d = (ICadGenerator) ScriptingEngine.inlineScriptRun(code, null,ShellType.GROOVY);
				dhCadGen.put(dh, d);
				generateCad();
			}catch(Exception ex){
				BowlerStudioController.highlightException(code, ex);
			}
		});
		w.start();
		
	}

	

}
