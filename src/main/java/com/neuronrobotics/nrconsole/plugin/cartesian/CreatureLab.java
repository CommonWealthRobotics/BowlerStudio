package com.neuronrobotics.nrconsole.plugin.cartesian;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.ConnectionManager;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngineWidget;
import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.nrconsole.util.XmlFilter;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.Log;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;
import eu.mihosoft.vrl.v3d.Transform;

public class CreatureLab extends AbstractBowlerStudioTab {

	private CreatureLab cadEngine;
	private BowlerAbstractDevice pm;
	private File openMobileBaseConfiguration;

	@Override
	public void onTabClosing() {
		// TODO Auto-generated method stub

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
			GridPane mobileBaseControls=new GridPane();
			mobileBaseControls.add(save, 0, 0);
			mobileBaseControls.add(refresh, 1, 0);
			
			dhlabTopLevel.add(mobileBaseControls, 0, 0);
			dhlabTopLevel.add(new DhChainWidget(device), 0, 1);
			
			Accordion advancedPanel = new Accordion();
			addAppendagePanel(device.getLegs(),"Legs",advancedPanel);
			addAppendagePanel(device.getAppendages(),"Appandges",advancedPanel);
			addAppendagePanel(device.getSteerable(),"Steerable",advancedPanel);
			addAppendagePanel(device.getDrivable(),"Drivable",advancedPanel);
			
			dhlabTopLevel.add(advancedPanel, 0, 2);
			
			
		}else if(AbstractKinematicsNR.class.isInstance(pm)) {
			AbstractKinematicsNR device=(AbstractKinematicsNR)pm;
			dhlabTopLevel.add(new DhChainWidget(device), 0, 0);
		}
		generateCad();
		
		setContent(new ScrollPane(dhlabTopLevel));
	}
	
	private void addAppendagePanel(ArrayList<DHParameterKinematics> apps,String title,Accordion advancedPanel){
		if(apps.size()>0){
			Accordion legPanels = new Accordion();
			for(DHParameterKinematics l:apps){
				legPanels.getPanes().add(new TitledPane(l.getScriptingName(), new DhChainWidget(l)));
			}
			advancedPanel.getPanes().add(new TitledPane(title, legPanels));
		}
	}
	
	private void generateCad(){
		ArrayList<CSG> allCad=new ArrayList<>();
		if(MobileBase.class.isInstance(pm)) {
			MobileBase device=(MobileBase)pm;
			for(DHParameterKinematics l:device.getAllDHChains()){
				for(CSG csg:generateCad(l.getChain().getLinks())){
					allCad.add(csg);
				}
			}
			
		}else if(DHParameterKinematics.class.isInstance(pm)){
			for(CSG csg:generateCad(((DHParameterKinematics)pm).getChain().getLinks())){
				allCad.add(csg);
			}
		}
		BowlerStudioController.setCsg(allCad);
	}
	
	public ArrayList<CSG> generateCad(ArrayList<DHLink> dhLinks ){
		if(cadEngine!=null)
			return cadEngine.generateCad(dhLinks);
		ArrayList<CSG> csg = new ArrayList<CSG>();
		if(dhLinks!=null){
			for(int i=0;i<dhLinks.size();i++){
				Log.warning("Adding Link Widget: "+i);
				DHLink dh  =dhLinks.get(i);
				// Create an axis to represent the link
				double y = dh.getD()>0?dh.getD():2;
				double  x= dh.getRadius()>0?dh.getRadius():2;
	
				CSG cube = new Cube(x,y,2).toCSG();
				cube=cube.transformed(new Transform().translateX(-x/2));
				cube=cube.transformed(new Transform().translateY(y/2));
				//add listner to axis
				cube.setManipulator(dh.getListener());
				cube.setColor(Color.GOLD);
				// add ax to list of objects to be returned
				csg.add(cube);
			}
		}
		return csg;
	}


	@Override
	public void onTabReOpening() {
		
	}
	
	public static String getFormatted(double value){
	    return String.format("%4.3f%n", (double)value);
	}

}
