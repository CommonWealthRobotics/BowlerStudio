package com.neuronrobotics.bowlerstudio.creature;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.util.ArrayList;

import org.python.core.exceptions;

import javafx.application.Platform;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.ConnectionManager;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngineWidget;
import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.nrconsole.util.GroovyFilter;
import com.neuronrobotics.nrconsole.util.XmlFilter;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.DrivingType;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.util.FileChangeWatcher;
import com.neuronrobotics.sdk.util.IFileChangeListener;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;
import eu.mihosoft.vrl.v3d.STL;
import eu.mihosoft.vrl.v3d.Transform;

public class CreatureLab extends AbstractBowlerStudioTab implements ICadGenerator {

	private ICadGenerator cadEngine;
	private BowlerAbstractDevice pm;
	private File openMobileBaseConfiguration;
	private File cadScript;
	private FileChangeWatcher watcher;

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
		setText("Creature Lab");

		GridPane dhlabTopLevel=new GridPane();
		
		
		if(DHParameterKinematics.class.isInstance(pm)){
			DHParameterKinematics device=(DHParameterKinematics)pm;
			Log.debug("Loading xml: "+device.getXml());
			dhlabTopLevel.add(new DhChainWidget(device), 0, 0);
		}else if(MobileBase.class.isInstance(pm)) {
			Button refresh = new Button("Generate CAD");
			refresh.setOnAction(event -> generateCad());
			Button save = new Button("Save Configuration");
			Button script = new Button("Set Cad Script");
			MobileBase device=(MobileBase)pm;
			save.setOnAction(event -> {
		    	new Thread(){

					public void run(){
						if(openMobileBaseConfiguration==null)
							openMobileBaseConfiguration=ScriptingEngineWidget.getLastFile();
		    	    	openMobileBaseConfiguration = FileSelectionFactory.GetFile(openMobileBaseConfiguration,
		    					new XmlFilter());

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
		    	        
		    		}
		    	}.start();
			});
			script.setOnAction(event -> {
		    	new Thread(){

					public void run(){
						if(getCadScript()==null)
							setCadScript(ScriptingEngineWidget.getLastFile());
		    	    	setCadScript(FileSelectionFactory.GetFile(getCadScript(),
		    					new GroovyFilter()));

		    	        if (getCadScript() == null) {
		    	            return;
		    	        }
		    	        generateCad();
		    	        
		    		}
		    	}.start();
			});
			GridPane mobileBaseControls=new GridPane();
			mobileBaseControls.add(save, 0, 0);
			mobileBaseControls.add(refresh, 1, 0);
			mobileBaseControls.add(script, 2, 0);
			dhlabTopLevel.add(mobileBaseControls, 0, 0);
			
			
			Accordion advancedPanel = new Accordion();
			TitledPane rp =new TitledPane("Multi-Appendage Cordinated Motion", new DhChainWidget(device));
			advancedPanel.getPanes().add(rp);


			addAppendagePanel(device.getLegs(),"Legs",advancedPanel);
			addAppendagePanel(device.getAppendages(),"Appandges",advancedPanel);
			addAppendagePanel(device.getSteerable(),"Steerable",advancedPanel);
			addAppendagePanel(device.getDrivable(),"Drivable",advancedPanel);
			
			dhlabTopLevel.add(advancedPanel, 0, 2);
			
			if(device.getDriveType() != DrivingType.NONE){
				advancedPanel.setExpandedPane(rp);
			}
			
		}else if(AbstractKinematicsNR.class.isInstance(pm)) {
			AbstractKinematicsNR device=(AbstractKinematicsNR)pm;
			dhlabTopLevel.add(new DhChainWidget(device), 0, 0);
		}
		generateCad();
		
		setContent(new ScrollPane(dhlabTopLevel));
		
	}
	
	private void addAppendagePanel(ArrayList<DHParameterKinematics> apps,String title,Accordion advancedPanel){
		if(apps.size()>0){
			for(DHParameterKinematics l:apps){
				TitledPane rp =new TitledPane(title+" - "+l.getScriptingName(), new DhChainWidget(l));
				advancedPanel.getPanes().add(rp);
				advancedPanel.setExpandedPane(rp);
			}
		}
		
	}
	
	private void generateCad(){
		Log.warning("Generating cad");
		//new Exception().printStackTrace();
		ArrayList<CSG> allCad=new ArrayList<>();
		if(MobileBase.class.isInstance(pm)) {
			MobileBase device=(MobileBase)pm;
			for(DHParameterKinematics l:device.getAllDHChains()){
				for(CSG csg:generateCad(l.getChain().getLinks())){
					allCad.add(csg);
//					new Thread(){
//						public void run(){
//							BowlerStudioController.setCsg(allCad);
//						}
//					}.start();
				}
			}
			
		}else if(DHParameterKinematics.class.isInstance(pm)){
			for(CSG csg:generateCad(((DHParameterKinematics)pm).getChain().getLinks())){
				allCad.add(csg);
//				new Thread(){
//					public void run(){
//						BowlerStudioController.setCsg(allCad);
//					}
//				}.start();
				
			}
		}
		new Thread(){
			public void run(){
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
				cadEngine = (ICadGenerator) ScriptingEngine.inlineUrlScriptRun(ScriptingEngine.class.getResource("ThreeDPrintArmCad.groovy"),null);
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
					
				}
				
			}
		});
		 watcher.start();
		 } catch (IOException e) {
		 // TODO Auto-generated catch block
		 e.printStackTrace();
		 }
		this.cadScript = cadScript;
		
	}

}
