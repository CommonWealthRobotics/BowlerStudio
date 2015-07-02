package com.neuronrobotics.nrconsole.plugin.cartesian;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;


import sun.security.action.GetLongAction;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;


import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.nrconsole.util.GroovyFilter;
import com.neuronrobotics.nrconsole.util.XmlFilter;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.Log;

public class DHKinematicsLab extends AbstractBowlerStudioTab {
	DHParameterKinematics device;
	private File currentFile=null;
	
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
		setText("DH Lab");
		device=(DHParameterKinematics)pm;
		Log.debug("Loading xml: "+device.getXml());
		VBox links = new VBox(20);
		Button save = new Button("Save Configuration");
		save.setOnAction(event -> {
			new Thread(){
				public void run(){
					File last = FileSelectionFactory.GetFile(currentFile==null?
										ScriptingEngine.getWorkspace():
										new File(ScriptingEngine.getWorkspace().getAbsolutePath()+"/"+currentFile.getName()),
							new XmlFilter());
					if (last != null) {
						try {
							Files.write(Paths.get(last.getAbsolutePath()),device.getXml().getBytes() );
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}.start();
		});
		ArrayList<DHLink> dhLinks = device.getChain().getLinks();
		links.getChildren().add(save);
		for(int i=0;i<dhLinks.size();i++){
			
			links.getChildren().add(
									new DHLinkWidget(i,
													dhLinks.get(i),
													device
													)
									);
		}
		setContent(links);
	}

	@Override
	public void onTabReOpening() {
		// TODO Auto-generated method stub

	}

}
