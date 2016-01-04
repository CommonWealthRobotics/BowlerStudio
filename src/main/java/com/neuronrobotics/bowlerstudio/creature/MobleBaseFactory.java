package com.neuronrobotics.bowlerstudio.creature;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.jfree.util.Log;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHGistBuilder;
import org.kohsuke.github.GitHub;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.ConnectionManager;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.nrconsole.util.CommitWidget;
import com.neuronrobotics.nrconsole.util.PromptForGist;
import com.neuronrobotics.sdk.addons.kinematics.DHChain;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.LinkFactory;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.common.DeviceManager;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class MobleBaseFactory {

	@SuppressWarnings("unchecked")
	public static void load(MobileBase device,TreeView<String> view, TreeItem<String> rootItem,
			HashMap<TreeItem<String>, Runnable> callbackMapForTreeitems,
			HashMap<TreeItem<String>, Group> widgetMapForTreeitems, 
			CreatureLab creatureLab) {
		
		boolean creatureIsOwnedByUser=false;
		TreeItem<String> publish = new TreeItem<String>("Publish");


		
		if(!(device.getSelfSource()[0]==null || device.getSelfSource()[1]==null)){
			try {
				 File source = ScriptingEngine.fileFromGistID(device.getSelfSource()[0], device.getSelfSource()[1]);
				 creatureIsOwnedByUser = ScriptingEngine.checkOwner(source);
				 callbackMapForTreeitems.put(publish, () -> {
			
							CommitWidget.commit(source, device.getXml());
									
				});
			} catch (Exception e) {
				Log.error(device.getSelfSource()[0]+" "+device.getSelfSource()[1]+" failed to load");
				e.printStackTrace();
			}
		}
		TreeItem<String> legs =loadLimbs(device,view,device.getLegs(), "Legs", rootItem, callbackMapForTreeitems,
				widgetMapForTreeitems,creatureLab,creatureIsOwnedByUser);
		TreeItem<String> arms =loadLimbs(device,view,device.getAppendages(), "Arms", rootItem,
				callbackMapForTreeitems, widgetMapForTreeitems,creatureLab,creatureIsOwnedByUser);
//		TreeItem<String> steer =loadLimb(device.getSteerable(), "Steerable", rootItem,
//				callbackMapForTreeitems, widgetMapForTreeitems);
//		TreeItem<String> drive =loadLimb(device.getDrivable(), "Drivable", rootItem,
//				callbackMapForTreeitems, widgetMapForTreeitems);
		
		TreeItem<String> addleg = new TreeItem<String>("Add Leg");
		boolean creatureIsOwnedByUserTmp = creatureIsOwnedByUser;
		callbackMapForTreeitems.put(addleg, () -> {
			// TODO Auto-generated method stub
				System.out.println("Adding Leg");
				String xmlContent;
				try {
					xmlContent = ScriptingEngine.codeFromGistID("b5b9450f869dd0d2ea30","defaultleg.xml")[0];
					DHParameterKinematics newLeg = new DHParameterKinematics(null,IOUtils.toInputStream(xmlContent, "UTF-8"));
					System.out.println("Leg has "+newLeg.getNumberOfLinks()+" links");
					addAppendage(device,view,device.getLegs(), newLeg, legs, rootItem, callbackMapForTreeitems, widgetMapForTreeitems,creatureLab, creatureIsOwnedByUserTmp);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			});
		TreeItem<String> regnerate = new TreeItem<String>("Generate Cad");

		callbackMapForTreeitems.put(regnerate, () -> {
				creatureLab.generateCad();
				
			});
		


		TreeItem<String> makeCopy = new TreeItem<String>("Make Copy of Creature");
		callbackMapForTreeitems.put(makeCopy, () -> {
			Platform.runLater(()->{
				String oldname  =device.getScriptingName();
				TextInputDialog dialog = new TextInputDialog(oldname+"_copy");
				dialog.setTitle("Making a copy of "+oldname);
				dialog.setHeaderText("Set the scripting name for this creature");
				dialog.setContentText("Please the name of the new creature:");

				// Traditional way to get the response value.
				Optional<String> result = dialog.showAndWait();
				if (result.isPresent()){
					view.getSelectionModel().select(rootItem);
					new Thread(){
						public void run(){
						    System.out.println("Your new creature: " + result.get());
						    String newName=result.get();
						    device.setScriptingName(newName);
						    device.setWalkingEngine(ScriptingEngine.forkGistFile(device.getWalkingEngine()));
						    device.setCadEngine(ScriptingEngine.forkGistFile(device.getCadEngine()));
						    for(DHParameterKinematics dh:device.getAllDHChains()){
						    	dh.setCadEngine(ScriptingEngine.forkGistFile(dh.getCadEngine()));
						    	dh.setDhEngine(ScriptingEngine.forkGistFile(dh.getDhEngine()));
						    }
						    
						    String xml = device.getXml();
						    device.disconnect();
						    GitHub github = ScriptingEngine.getGithub();
						    GHGistBuilder builder = github.createGist();
						    builder.description(newName+" copy of "+oldname);
						    String filename =newName+".xml";
						    builder.file(filename, xml);
						    builder.public_(true);
						    GHGist gist;
							try {
								gist = builder.create();
								String gistID = ScriptingEngine.urlToGist(gist.getHtmlUrl());
								BowlerStudio.openUrlInNewTab(new URL(gist.getHtmlUrl()));
								System.out.println("Creating repo");
								while(true){
									try {
										ScriptingEngine.fileFromGistID(gistID, filename);
										break;
									} catch (Exception e) {
										
									}
									ThreadUtil.wait(500);
									Log.warn(gist+" not built yet");
								}
								System.out.println("Creating gist at: "+gistID);
								MobileBase mb = new MobileBase(IOUtils.toInputStream(xml, "UTF-8"));
								
								mb.setSelfSource(new String[]{gistID,newName+".xml"});
								
								ConnectionManager.addConnection(mb,mb.getScriptingName());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						    
						    
						    
							//DeviceManager.addConnection(newDevice, newDevice.getScriptingName());
						}
					}.start();
				}
			});
		});
		
		TreeItem<String> owner = new TreeItem<String>("Owner");
		TreeItem<String> setCAD = new TreeItem<String>("Set CAD Engine...");
		callbackMapForTreeitems.put(setCAD, () -> {
			PromptForGist.prompt("Select a CAD Engine From a Gist",device.getCadEngine()[0],(gitsId, file) -> {
				Log.warn("Loading cad engine");
				try {
					creatureLab.setCadEngine(gitsId, file,device);
					File code = ScriptingEngine.fileFromGistID(gitsId,file);
					BowlerStudio.createFileTab(code);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		});
		TreeItem<String> editCAD = new TreeItem<String>("Edit CAD Engine...");
		callbackMapForTreeitems.put(editCAD, () -> {
			try {
				File code = ScriptingEngine.fileFromGistID(device.getCadEngine()[0],device.getCadEngine()[1]);
				BowlerStudio.createFileTab(code);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		TreeItem<String> resetWalking = new TreeItem<String>("Set Walking Engine...");
		callbackMapForTreeitems.put(resetWalking, () -> {
			PromptForGist.prompt("Select a Walking Engine From a Gist",device.getWalkingEngine()[0],(gitsId, file) -> {
				Log.warn("Loading walking engine");
				try {
					creatureLab.setWalkingEngine(gitsId, file,device);
					File code = ScriptingEngine.fileFromGistID(gitsId,file);
					BowlerStudio.createFileTab(code);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		});
		TreeItem<String> editWalking = new TreeItem<String>("Edit Walking Engine...");
		callbackMapForTreeitems.put(editWalking, () -> {
			try {
				File code = ScriptingEngine.fileFromGistID(device.getWalkingEngine()[0],device.getWalkingEngine()[1]);
				BowlerStudio.createFileTab(code);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		
		TreeItem<String> item = new TreeItem<String>("Add Arm");

		callbackMapForTreeitems.put(item, () -> {
			// TODO Auto-generated method stub
				System.out.println("Adding Arm");
				try {
					String xmlContent = ScriptingEngine.codeFromGistID("b5b9450f869dd0d2ea30","defaultarm.xml")[0];
					DHParameterKinematics newArm = new DHParameterKinematics(null,IOUtils.toInputStream(xmlContent, "UTF-8"));
					System.out.println("Arm has "+newArm.getNumberOfLinks()+" links");
					addAppendage(device,view,device.getAppendages(), newArm, arms, rootItem, callbackMapForTreeitems, widgetMapForTreeitems,creatureLab, creatureIsOwnedByUserTmp);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			});
		
		rootItem.getChildren().addAll(regnerate,item, addleg,makeCopy);
		
		if(creatureIsOwnedByUser){
			owner.getChildren().addAll(publish,editWalking,editCAD,resetWalking,setCAD);
			rootItem.getChildren().add(owner);
		}
	}
	


	
	private static void getNextChannel(MobileBase base,LinkConfiguration confOfChannel ){
		HashMap<String,HashMap<Integer, Boolean>> deviceMap = new HashMap<>();
		
		
		for(DHParameterKinematics dh: base.getAllDHChains()){
			for( LinkConfiguration conf :dh.getLinkConfigurations()){
				HashMap<Integer,Boolean> channelMap;
				if(deviceMap.get(conf.getDeviceScriptingName()) == null){
					deviceMap.put(conf.getDeviceScriptingName(), new HashMap<Integer, Boolean>());
				}
				channelMap= deviceMap.get(conf.getDeviceScriptingName());
				channelMap.put(conf.getHardwareIndex(), true);
				for(LinkConfiguration sl:conf.getSlaveLinks()){
					HashMap<Integer,Boolean> slavechannelMap;
					if(deviceMap.get(sl.getDeviceScriptingName()) == null){
						deviceMap.put(sl.getDeviceScriptingName(), new HashMap<Integer, Boolean>());
					}
					slavechannelMap= deviceMap.get(sl.getDeviceScriptingName());
					slavechannelMap.put(sl.getHardwareIndex(), true);
				}
			}
		}
		for(String key:  deviceMap.keySet()){
			HashMap<Integer, Boolean> chans = deviceMap.get(key);
			for(int i=0;i<24;i++){
				
				if(chans.get(i)==null){
					System.err.println("Channel free: "+i+" on device "+key);
					confOfChannel.setDeviceScriptingName(key);
					confOfChannel.setHardwareIndex(i);
					return;
				}
			}
		}
		
		throw new RuntimeException("No channels are availible on given devices");
	}
	
	private static void addAppendage(MobileBase base,
			TreeView<String> view,
			ArrayList<DHParameterKinematics> deviceList,
			DHParameterKinematics newDevice, 
			TreeItem<String> rootItem,
			TreeItem<String> topLevel,
			HashMap<TreeItem<String>, Runnable> callbackMapForTreeitems,
			HashMap<TreeItem<String>, Group> widgetMapForTreeitems, 
			CreatureLab creatureLab, boolean creatureIsOwnedByUser){
		
		Platform.runLater(()->{
			TextInputDialog dialog = new TextInputDialog(newDevice.getScriptingName());
			dialog.setTitle("Add a new limb of");
			dialog.setHeaderText("Set the scripting name for this limb");
			dialog.setContentText("Please the name of the new limb:");

			// Traditional way to get the response value.
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent()){
				view.getSelectionModel().select(rootItem);
				new Thread(){
					public void run(){
					    System.out.println("Your new limb: " + result.get());
					    newDevice.setScriptingName(result.get());
						DeviceManager.addConnection(newDevice, newDevice.getScriptingName());
						deviceList.add(newDevice);
						for( LinkConfiguration conf :newDevice.getLinkConfigurations()){
							try{
								getNextChannel(base, conf);
							}catch(RuntimeException exc){
								String newname =conf.getDeviceScriptingName()+"_new";
								System.err.println("Adding new device to provide new channels: "+newname);
								conf.setDeviceScriptingName(newname);
								getNextChannel(base, conf);
							}
							newDevice.getFactory().refreshHardwareLayer(conf);
							
						}
						
						rootItem.setExpanded(true);
						loadSingleLimb(base, view,newDevice,rootItem,callbackMapForTreeitems, widgetMapForTreeitems,creatureLab,  creatureIsOwnedByUser);
						creatureLab.generateCad();
					}
				}.start();
			}
		});



		
	}

	private static TreeItem<String> loadLimbs(MobileBase base,
			TreeView<String> view,
			ArrayList<DHParameterKinematics> drivable,
			String label, TreeItem<String> rootItem,
			HashMap<TreeItem<String>, Runnable> callbackMapForTreeitems,
			HashMap<TreeItem<String>, Group> widgetMapForTreeitems, 
			CreatureLab creatureLab, boolean creatureIsOwnedByUser) {

	
		TreeItem<String> apps = new TreeItem<String>(label);
		rootItem.getChildren().add(apps);
		if (drivable.size() == 0)
			return apps;
		for (DHParameterKinematics dh : drivable) {
			loadSingleLimb(base, view,dh,apps,callbackMapForTreeitems, widgetMapForTreeitems,creatureLab,creatureIsOwnedByUser);
		}
		
		return apps;
	}
	
	private static void setHardwareConfig(
			LinkConfiguration conf,
			LinkFactory factory,
			TreeItem<String> rootItem,
			HashMap<TreeItem<String>, Runnable> callbackMapForTreeitems,
			HashMap<TreeItem<String>, Group> widgetMapForTreeitems){
		
		TreeItem<String> hwConf = new TreeItem<String>("Hardware Config "+conf.getName());
		callbackMapForTreeitems.put(hwConf, ()->{
			if(widgetMapForTreeitems.get(hwConf)==null){
				//create the widget for the leg when looking at it for the first time
				widgetMapForTreeitems.put(hwConf,new Group( new LinkConfigurationWidget(conf, factory)));
			}
		});
		rootItem.getChildren().add(hwConf);
	}
	
	private static void loadSingleLink(int linkIndex,
			MobileBase base,
			TreeView<String> view,
			LinkConfiguration conf,
			DHParameterKinematics dh,
			TreeItem<String> rootItem,
			HashMap<TreeItem<String>, Runnable> callbackMapForTreeitems,
			HashMap<TreeItem<String>, Group> widgetMapForTreeitems, 
			CreatureLab creatureLab){
		
		
		DHLink dhLink = dh.getChain().getLinks().get(linkIndex);
				
		TreeItem<String> link = new TreeItem<String>(conf.getName());
		callbackMapForTreeitems.put(link, ()->{
			if(widgetMapForTreeitems.get(link)==null){
				//create the widget for the leg when looking at it for the first time
				widgetMapForTreeitems.put(link, new LinkSliderWidget(linkIndex, dhLink, dh));
			}
			//activate controller
		});
		
		TreeItem<String> slaves = new TreeItem<String>("Slaves to "+conf.getName());
		LinkFactory slaveFactory  =dh.getFactory().getLink(conf).getSlaveFactory();
		for(LinkConfiguration co:conf.getSlaveLinks()){
			
			setHardwareConfig(co,  slaveFactory , slaves, callbackMapForTreeitems, widgetMapForTreeitems);
		}
		
		TreeItem<String> addSlaves = new TreeItem<String>("Add Slave to "+conf.getName());
		
		callbackMapForTreeitems.put(addSlaves, ()->{
//			if(widgetMapForTreeitems.get(advanced)==null){
//				//create the widget for the leg when looking at it for the first time
//				widgetMapForTreeitems.put(advanced, new DhChainWidget(dh, creatureLab));
//			}
			Platform.runLater(()->{
				TextInputDialog dialog = new TextInputDialog(conf.getName()+"_SLAVE_"+conf.getSlaveLinks().size());
				dialog.setTitle("Add a new Slave link of");
				dialog.setHeaderText("Set the scripting name for this Slave link");
				dialog.setContentText("Please the name of the new Slave link:");

				// Traditional way to get the response value.
				Optional<String> result = dialog.showAndWait();
				if (result.isPresent()){
					view.getSelectionModel().select(rootItem);
					new Thread(){
						public void run(){
						    System.out.println("Your new link: " + result.get());
							LinkConfiguration newLink = new LinkConfiguration();
							newLink.setType(conf.getType());
							getNextChannel(base,newLink);
							newLink.setName(result.get());
							conf.getSlaveLinks().add(newLink);
							slaveFactory.getLink(newLink);
							setHardwareConfig(newLink,  slaveFactory , slaves, callbackMapForTreeitems, widgetMapForTreeitems);
						}
					}.start();
				}
			});
		});
		
		slaves.getChildren().add(0,addSlaves);
		TreeItem<String> remove = new TreeItem<String>("Remove "+conf.getName());
		callbackMapForTreeitems.put(remove, ()->{
			Platform.runLater(()->{
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Confirm removing link");
				alert.setHeaderText("This will remove " +conf.getName() );
				alert.setContentText("Are sure you wish to remove this link?");
	
				Optional<ButtonType> result = alert.showAndWait();
				view.getSelectionModel().select(rootItem);
				if (result.get() == ButtonType.OK){
					
					rootItem.getChildren().remove(link);
					LinkFactory factory  =dh.getFactory();
					//remove the link listener while the number of links could chnage
					factory.removeLinkListener(dh);
					DHChain chain = dh.getDhChain() ;
					chain.getLinks().remove(linkIndex);
					factory.deleteLink(linkIndex);
					//set the modified kinematics chain
					dh.setChain(chain);
					//once the new link configuration is set up, re add the listener
					factory.addLinkListener(dh);
					creatureLab.generateCad();
				} 
			});
			
		});
		
		TreeItem<String> design = new TreeItem<String>("Design "+conf.getName());
		
		callbackMapForTreeitems.put(design, ()->{
			if(widgetMapForTreeitems.get(design)==null){
				//create the widget for the leg when looking at it for the first time
				widgetMapForTreeitems.put(design, new DhSettingsWidget(dhLink, dh, new IOnEngineeringUnitsChange() {
					
					@Override
					public void onSliderMoving(EngineeringUnitsSliderWidget source,
							double newAngleDegrees) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onSliderDoneMoving(EngineeringUnitsSliderWidget source,
							double newAngleDegrees) {
						creatureLab.generateCad();
					}
				}));
			}
		});
		
		link.getChildren().addAll(design);
		
		setHardwareConfig(conf,  dh.getFactory(), link, callbackMapForTreeitems, widgetMapForTreeitems);
		
		link.getChildren().addAll(slaves,remove);
		rootItem.getChildren().add(0,link);
		
	}
	
	@SuppressWarnings("unchecked")
	private static void loadSingleLimb(MobileBase base,
			TreeView<String> view,
			DHParameterKinematics dh,
			TreeItem<String> rootItem,
			HashMap<TreeItem<String>, Runnable> callbackMapForTreeitems,
			HashMap<TreeItem<String>, Group> widgetMapForTreeitems, CreatureLab creatureLab, boolean creatureIsOwnedByUser){
		
		TreeItem<String> dhItem = new TreeItem<String>(
				dh.getScriptingName());
		
		callbackMapForTreeitems.put(dhItem, ()->{
			if(widgetMapForTreeitems.get(dhItem)==null){
				//create the widget for the leg when looking at it for the first time
				VBox jog = new VBox(10);
				jog.getChildren().add(new Label("Jog Limb"));
				jog.getChildren().add(new JogWidget(dh));
				widgetMapForTreeitems.put(dhItem, new Group( new JogWidget(dh)));
			}
			AbstractGameController controller = creatureLab.getController();
			controller.clearIGameControllerUpdateListener();
			controller.addIGameControllerUpdateListener(new IGameControllerUpdateListener() {
				@Override
				public void onControllerUpdate(AbstractGameController source) {
					// TODO Auto-generated method stub
					
				}
			});
		});
		

		
		
		TreeItem<String> remove = new TreeItem<String>("Remove "+dh.getScriptingName());
		
		callbackMapForTreeitems.put(remove, ()->{
			Platform.runLater(()->{
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Confirm removing limb");
				alert.setHeaderText("This will remove " +dh.getScriptingName() );
				alert.setContentText("Are sure you wish to remove this limb?");
	
				Optional<ButtonType> result = alert.showAndWait();
				view.getSelectionModel().select(rootItem);
				if (result.get() == ButtonType.OK){
					
					rootItem.getChildren().remove(dhItem);
					if(base.getLegs().contains(dh)){
						base.getLegs().remove(dh);
					}
					if(base.getAppendages().contains(dh)){
						base.getAppendages().remove(dh);
					}
					if(base.getSteerable().contains(dh)){
						base.getSteerable().remove(dh);
					}
					if(base.getDrivable().contains(dh)){
						base.getDrivable().remove(dh);
					}
					creatureLab.generateCad();
				} 
			});
			
		});
		int j=0;
		for(LinkConfiguration conf:dh.getFactory().getLinkConfigurations()){
			loadSingleLink(j++,base, view, conf, dh, dhItem, callbackMapForTreeitems, widgetMapForTreeitems, creatureLab);
		}
		
		
		TreeItem<String> addLink = new TreeItem<String>("Add Link");
		
		callbackMapForTreeitems.put(addLink, ()->{
//			if(widgetMapForTreeitems.get(advanced)==null){
//				//create the widget for the leg when looking at it for the first time
//				widgetMapForTreeitems.put(advanced, new DhChainWidget(dh, creatureLab));
//			}
			Platform.runLater(()->{
				TextInputDialog dialog = new TextInputDialog("Link_"+dh.getLinkConfigurations().size());
				dialog.setTitle("Add a new link of");
				dialog.setHeaderText("Set the scripting name for this link");
				dialog.setContentText("Please the name of the new link:");
	
				// Traditional way to get the response value.
				Optional<String> result = dialog.showAndWait();
				if (result.isPresent()){
					view.getSelectionModel().select(rootItem);
					new Thread(){
						public void run(){
						    System.out.println("Your new link: " + result.get());
							LinkConfiguration newLink = new LinkConfiguration();
							newLink.setType(dh.getFactory().getLinkConfigurations().get(0).getType());
							getNextChannel(base,newLink);
							newLink.setName(result.get());
							if(dh!=null)
								dh.addNewLink(newLink,new DHLink(0, 0, 0, 0));
							
							loadSingleLink(dh.getLinkConfigurations().size()-1,base, view, newLink, dh, dhItem, callbackMapForTreeitems, widgetMapForTreeitems, creatureLab);
							creatureLab.generateCad();
						}
					}.start();
				}
			});
		});		
		
		TreeItem<String> advanced = new TreeItem<String>("Advanced Configuration");
		
		callbackMapForTreeitems.put(advanced, ()->{
			if(widgetMapForTreeitems.get(advanced)==null){
				//create the widget for the leg when looking at it for the first time
				widgetMapForTreeitems.put(advanced, new DhChainWidget(dh, creatureLab));
			}
			
		});	
		dhItem.getChildren().addAll(addLink,advanced,remove);
		if(creatureIsOwnedByUser){
			TreeItem<String> owner = new TreeItem<String>("Owner");
			TreeItem<String> setCAD = new TreeItem<String>("Set CAD Engine...");
			callbackMapForTreeitems.put(setCAD, () -> {
				PromptForGist.prompt("Select a CAD Engine From a Gist",dh.getCadEngine()[0],(gitsId, file) -> {
					Log.warn("Loading cad engine");
					try {
						creatureLab.setCadEngine(gitsId, file,dh);
						File code = ScriptingEngine.fileFromGistID(gitsId,file);
						BowlerStudio.createFileTab(code);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
			});
			TreeItem<String> editCAD = new TreeItem<String>("Edit CAD Engine...");
			callbackMapForTreeitems.put(editCAD, () -> {
				try {
					File code = ScriptingEngine.fileFromGistID(dh.getCadEngine()[0],dh.getCadEngine()[1]);
					BowlerStudio.createFileTab(code);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			TreeItem<String> resetWalking = new TreeItem<String>("Set Dh Kinematics Engine...");
			callbackMapForTreeitems.put(resetWalking, () -> {
				PromptForGist.prompt("Select a Walking Engine From a Gist",dh.getDhEngine()[0],(gitsId, file) -> {
					Log.warn("Loading walking engine");
					try {
						creatureLab.setDhEngine(gitsId, file,dh);
						File code = ScriptingEngine.fileFromGistID(gitsId,file);
						BowlerStudio.createFileTab(code);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
			});
			TreeItem<String> editWalking = new TreeItem<String>("Edit Kinematics Engine...");
			callbackMapForTreeitems.put(editWalking, () -> {
				try {
					File code = ScriptingEngine.fileFromGistID(dh.getDhEngine()[0],dh.getDhEngine()[1]);
					BowlerStudio.createFileTab(code);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			owner.getChildren().addAll(editWalking,editCAD,resetWalking,setCAD);
			
			dhItem.getChildren().add(owner);
		}
		rootItem.getChildren().add(dhItem);
		double[] vect = dh.getCurrentJointSpaceVector();
		for(int i=0;i<vect.length;i++){
			vect[i]=0;
		}
		try {
			dh.setDesiredJointSpaceVector(vect, 1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dh.updateCadLocations();
		
		
	}

}
