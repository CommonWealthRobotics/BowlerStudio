package com.neuronrobotics.nrconsole.plugin.cartesian;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import sun.security.action.GetLongAction;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.nrconsole.util.GroovyFilter;
import com.neuronrobotics.nrconsole.util.XmlFilter;
import com.neuronrobotics.sdk.addons.kinematics.AbstractLink;
import com.neuronrobotics.sdk.addons.kinematics.DHChain;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.LinkFactory;
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
		HBox controls = new HBox(10);
		controls.getChildren().add(new JogWidget(device));
		Button save = new Button("Save Configuration");
		Button add = new Button("Add Link");
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
		add.setOnAction(event -> {
			LinkConfiguration newLink = new LinkConfiguration();
			LinkFactory factory  =device.getFactory();
			//remove the link listener while the number of links could chnage
			factory.removeLinkListener(device);
			AbstractLink link = factory.getLink(newLink);
			DHChain chain =  device.getDhChain() ;
			chain.addLink(new DHLink(0, 0, 0, 0));
			//set the modified kinematics chain
			device.setChain(chain);
			//once the new link configuration is set up, re add the listener
			factory.addLinkListener(device);
		});
		
		
		ArrayList<DHLink> dhLinks = device.getChain().getLinks();
		controls.getChildren().add(save);
		controls.getChildren().add(add);
		links.getChildren().add(controls);
		for(int i=0;i<dhLinks.size();i++){
			
			links.getChildren().add(
									new DHLinkWidget(i,
													dhLinks.get(i),
													device
													)
									);
		}

		setContent(new ScrollPane(links));
	}

	@Override
	public void onTabReOpening() {
		// TODO Auto-generated method stub

	}

}
