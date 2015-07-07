package com.neuronrobotics.nrconsole.plugin.cartesian;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import sun.security.action.GetLongAction;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.nrconsole.util.GroovyFilter;
import com.neuronrobotics.nrconsole.util.XmlFilter;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.AbstractLink;
import com.neuronrobotics.sdk.addons.kinematics.DHChain;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.LinkFactory;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.Log;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;
import eu.mihosoft.vrl.v3d.Transform;

public class CreatureLab extends AbstractBowlerStudioTab {

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
		// TODO Auto-generated method stub
		setText("Creature Lab");

		GridPane dhlabTopLevel=new GridPane();
		
		if(DHParameterKinematics.class.isInstance(pm)){
			DHParameterKinematics device=(DHParameterKinematics)pm;
			Log.debug("Loading xml: "+device.getXml());
			dhlabTopLevel.add(new DhChainWidget(device), 0, 0);
		}else if(AbstractKinematicsNR.class.isInstance(pm)) {
			AbstractKinematicsNR device=(AbstractKinematicsNR)pm;
			Log.debug("Loading xml: "+device.getXml());
			dhlabTopLevel.add(new DhChainWidget(device), 0, 0);
		}
		
		setContent(new ScrollPane(dhlabTopLevel));
	}


	@Override
	public void onTabReOpening() {
		
	}
	
	public static String getFormatted(double value){
	    return String.format("%4.3f%n", (double)value);
	}

}
