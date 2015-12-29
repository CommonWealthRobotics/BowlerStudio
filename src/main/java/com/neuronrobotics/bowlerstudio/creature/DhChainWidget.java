package com.neuronrobotics.bowlerstudio.creature;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import eu.mihosoft.vrl.v3d.STL;

import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.util.ArrayList;

import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.nrconsole.util.GroovyFilter;
import com.neuronrobotics.nrconsole.util.XmlFilter;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHChain;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.DhInverseSolver;
import com.neuronrobotics.sdk.addons.kinematics.IDriveEngine;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.LinkFactory;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.IDeviceConnectionEventListener;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.util.FileChangeWatcher;
import com.neuronrobotics.sdk.util.IFileChangeListener;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;
import eu.mihosoft.vrl.v3d.Transform;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser.ExtensionFilter;

public class DhChainWidget extends Group implements  IDeviceConnectionEventListener{
	private File currentFile=null;
	private VBox links;
	private VBox controls;
	JogWidget jog = null;
	
	private AbstractKinematicsNR device;
	private DHParameterKinematics dhdevice=null;
	private MobileBase mbase=null;
	private File kinematicsFile;

	private ArrayList<DHLinkWidget> widgets = new ArrayList<>();
	private FileChangeWatcher watcher;
	private IOnEngineeringUnitsChange externalListener;
	public DhChainWidget(AbstractKinematicsNR device2,IOnEngineeringUnitsChange externalListener){
		this.device = device2;
		this.externalListener = externalListener;
	
		device.addConnectionEventListener(this);
		if(DHParameterKinematics.class.isInstance(device2)){
			dhdevice=(DHParameterKinematics)device2;
		}
		if(MobileBase.class.isInstance(device2)){
			mbase=(MobileBase)device2;
		}
		links = new VBox(20);
		controls = new VBox(10);
		jog = new JogWidget(device);
		
		VBox advanced = new VBox(10);
		
		
		
		Button save = new Button("Save Configuration");
		Button add = new Button("Add Link");
		Button refresh = new Button("Generate CAD");
		Button kinematics = new Button("Set Kinematics");
		kinematics.setOnAction(event -> {
	    	new Thread(){



				public void run(){
					if(getKinematicsFile()==null)
						setKinematicsFile(ScriptingEngine.getLastFile());
					setKinematicsFile(FileSelectionFactory.GetFile(getKinematicsFile(),
							new ExtensionFilter("Kinematics Script","*.groovy","*.java","*.txt")));

	    	        if (getKinematicsFile() == null) {
	    	            return;
	    	        }

	    	        setKinematics();
	    		}
	    	}.start();
		});
		save.setOnAction(event -> {
			new Thread(){
				public void run(){
					File last = FileSelectionFactory.GetFile(currentFile==null?
										ScriptingEngine.getWorkspace():
										new File(ScriptingEngine.getWorkspace().getAbsolutePath()+"/"+currentFile.getName()),
										new ExtensionFilter("MobileBase XML","*.xml","*.XML"));
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
			if(dhdevice!=null)dhdevice.addNewLink(newLink,new DHLink(0, 0, 0, 0));
			onTabReOpening();
		});
		refresh.setOnAction(event -> {
			onTabReOpening();
		});
		

		advanced.getChildren().add(new TransformWidget("Base to Global", 
				device.getFiducialToGlobalTransform(),new IOnTransformChange() {
					
					@Override
					public void onTransformFinished(TransformNR newTrans) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onTransformChaging(TransformNR newTrans) {
						Log.debug("Base to Global"+newTrans.toString());
						device.setGlobalToFiducialTransform(newTrans);
						device.getCurrentTaskSpaceTransform();
					}
				}
				));
		

		advanced.getChildren().add(save);
		advanced.getChildren().add(add);
		advanced.getChildren().add(refresh);
		advanced.getChildren().add(kinematics);
		Accordion advancedPanel = new Accordion();
		advancedPanel.getPanes().add(new TitledPane("Advanced Options", advanced));
		controls.getChildren().add(jog);
		if(mbase==null){
			
			controls.getChildren().add(advancedPanel);
			controls.getChildren().add(new TransformWidget("Place Limb", 
					device.getRobotToFiducialTransform(), new IOnTransformChange() {
				
				@Override
				public void onTransformFinished(TransformNR newTrans) {
					// Force a cad regeneration
					externalListener.onSliderDoneMoving(null, 0);
				}
				
				@Override
				public void onTransformChaging(TransformNR newTrans) {
					Log.debug("Limb to base"+newTrans.toString());
					device.setRobotToFiducialTransform(newTrans);
					device.getCurrentTaskSpaceTransform();
					//this calls the render update function attachec as the on jointspace update	
					double[] joint=device2.getCurrentJointSpaceVector();
					dhdevice.getChain().getChain(joint);
					Platform.runLater(()->dhdevice.onJointSpaceUpdate(dhdevice, joint));
				}
			}
			));
		}else{
			controls.getChildren().add(kinematics);

		}
		onTabReOpening();
		getChildren().add(new ScrollPane(links));
	}
	
	private void setKinematics(){
		if(getKinematicsFile()!=null){
			try{
		        if(mbase!=null){
		        	mbase.setWalkingDriveEngine( (IDriveEngine) ScriptingEngine.inlineFileScriptRun(getKinematicsFile(), null));
		        }else if (dhdevice != null){
		        	dhdevice.setInverseSolver((DhInverseSolver) ScriptingEngine.inlineFileScriptRun(getKinematicsFile(), null));
		        }
			}catch(Exception e){
				  StringWriter sw = new StringWriter();
			      PrintWriter pw = new PrintWriter(sw);
			      e.printStackTrace(pw);
			      System.out.println(sw.toString());
			}
		}
	}
	
	public void onTabReOpening() {
		for(DHLinkWidget wid:widgets){
			device.removeJointSpaceUpdateListener(wid);
		}
		widgets.clear();
		links.getChildren().clear();
		ArrayList<DHLink> dhLinks=null;
		if(dhdevice!=null)
			dhLinks = dhdevice.getChain().getLinks();
		links.getChildren().add(controls);

		
		for(int i=0;i<device.getFactory().getLinkConfigurations().size();i++){
			Log.warning("Adding Link Widget: "+i);
			
			DHLink dh=null;
			if(dhdevice!=null)
				dh=dhLinks.get(i);
			Button del = new Button("Delete");
			final int linkIndex=i;
			del.setOnAction(event -> {
				LinkFactory factory  =device.getFactory();
				//remove the link listener while the number of links could chnage
				factory.removeLinkListener(device);
				if(dhdevice!=null){
					DHChain chain = dhdevice.getDhChain() ;
					chain.getLinks().remove(linkIndex);
					factory.deleteLink(linkIndex);
					//set the modified kinematics chain
					dhdevice.setChain(chain);
				}else{
					factory.deleteLink(linkIndex);
				}
				

				//once the new link configuration is set up, re add the listener
				factory.addLinkListener(device);
				onTabReOpening();
				
			});
			DHLinkWidget w = new DHLinkWidget(i,
					dh,
					device,
					del,
					externalListener);
			widgets.add(w);
			links.getChildren().add(w);
			device.addJointSpaceListener(w);
		}
		//BowlerStudioController.setCsg(csg);
		jog.home();

	}
	

	public File getKinematicsFile() {
		return kinematicsFile;
	}

	public void setKinematicsFile(File kinematicsFile) {
		if(kinematicsFile!=null){
			if (watcher != null) {

				watcher.close();
			}
			try {
				watcher = new FileChangeWatcher(kinematicsFile);
				watcher.addIFileChangeListener(new IFileChangeListener() {

					@Override
					public void onFileChange(File fileThatChanged,
							WatchEvent event) {
						try {
							setKinematics();
						} catch (Exception ex) {
							ex.printStackTrace();
						}

					}
				});
				watcher.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.kinematicsFile = kinematicsFile;
	}

	@Override
	public void onDisconnect(BowlerAbstractDevice source) {
		// TODO Auto-generated method stub
		if (watcher != null) {
			
			watcher.close();
		}
	}

	@Override
	public void onConnect(BowlerAbstractDevice source) {
		// TODO Auto-generated method stub
		
	}
}
