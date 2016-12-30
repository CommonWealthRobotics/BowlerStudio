package com.neuronrobotics.bowlerstudio.creature;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.BowlerStudioModularFrame;
import com.neuronrobotics.bowlerstudio.ConnectionManager;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.IScriptEventListener;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingFileWidget;
import com.neuronrobotics.bowlerstudio.threed.MobileBaseCadManager;
import com.neuronrobotics.nrconsole.util.CommitWidget;
import com.neuronrobotics.nrconsole.util.PromptForGit;
import com.neuronrobotics.sdk.addons.gamepad.BowlerJInputDevice;
import com.neuronrobotics.sdk.addons.kinematics.DHChain;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.LinkFactory;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.common.DeviceManager;
import com.neuronrobotics.sdk.util.ThreadUtil;

import eu.mihosoft.vrl.v3d.CSG;

public class MobleBaseMenueFactory {

	private MobleBaseMenueFactory() {
	}
	
	
	public static String [] copyGitFile(String sourceGit, String targetGit, String filename){
		
		String[] WalkingEngine;
		try {
			WalkingEngine = ScriptingEngine.codeFromGit(sourceGit, filename);
			try {
				if( null==ScriptingEngine.fileFromGit(targetGit, filename)){
					
					ScriptingEngine.createFile(targetGit, filename, "copy file");
					while (true) {
						try {
							ScriptingEngine.fileFromGit(targetGit, filename);
							break;
						} catch (Exception e) {

						}
						ThreadUtil.wait(500);
						Log.warn(targetGit +"/"+filename+ " not built yet");
					}
					
				}
			} catch (InvalidRemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransportException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GitAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String[] newFileCode;
			try {
				newFileCode = ScriptingEngine.codeFromGit(targetGit, filename);
				if(newFileCode==null)
					newFileCode=new String[]{""};
				if(!WalkingEngine[0].contentEquals(newFileCode[0])){
					System.out.println("Copy Content to "+targetGit+"/"+filename);
					ScriptingEngine.pushCodeToGit(targetGit, "master", filename, WalkingEngine[0], "copy file content");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		return new String[]{targetGit,filename};
	}

	
	@SuppressWarnings("unchecked")
	public static void load(MobileBase device, TreeView<String> view, TreeItem<String> rootItem,
			HashMap<TreeItem<String>, Runnable> callbackMapForTreeitems,
			HashMap<TreeItem<String>, Group> widgetMapForTreeitems, CreatureLab creatureLab) throws Exception {

		boolean creatureIsOwnedByUser = false;
		
		TreeItem<String> physics = new TreeItem<String>("Physics Simulation",AssetFactory.loadIcon("Physics-Creature-Simulation.png"));
		callbackMapForTreeitems.put(physics, () -> {
			if (widgetMapForTreeitems.get(physics) == null) {
				widgetMapForTreeitems.put(physics, new Group(new CreaturePhysicsWidget(device)));

			}
		});
		
		
		TreeItem<String> publish;
		publish = new TreeItem<String>("Publish",AssetFactory.loadIcon("Publish.png"));


		if (!(device.getGitSelfSource()[0] == null || device.getGitSelfSource()[1] == null)) {
			try {
				File source = ScriptingEngine.fileFromGit(device.getGitSelfSource()[0], device.getGitSelfSource()[1]);
				creatureIsOwnedByUser = ScriptingEngine.checkOwner(source);
				callbackMapForTreeitems.put(publish, () -> {

					CommitWidget.commit(source, device.getXml());

				});
			} catch (Exception e) {
				Log.error(device.getGitSelfSource()[0] + " " + device.getGitSelfSource()[1] + " failed to load");
				e.printStackTrace();
			}
		}
		TreeItem<String> legs = loadLimbs(device, view, device.getLegs(), "Legs", rootItem, callbackMapForTreeitems,
				widgetMapForTreeitems, creatureLab, creatureIsOwnedByUser);
		TreeItem<String> arms = loadLimbs(device, view, device.getAppendages(), "Arms", rootItem,
				callbackMapForTreeitems, widgetMapForTreeitems, creatureLab, creatureIsOwnedByUser);
		TreeItem<String> steer = loadLimbs(device, view, device.getSteerable(), "Steerable Wheels", rootItem,
				callbackMapForTreeitems, widgetMapForTreeitems, creatureLab, creatureIsOwnedByUser);
		TreeItem<String> drive = loadLimbs(device, view, device.getDrivable(), "Fixed Wheels", rootItem,
				callbackMapForTreeitems, widgetMapForTreeitems, creatureLab, creatureIsOwnedByUser);

		TreeItem<String> addleg;
		try {
			addleg = new TreeItem<String>("Add Leg",AssetFactory.loadIcon("Add-Leg.png"));
		} catch (Exception e1) {
			addleg = new TreeItem<String>("Add Leg");
		}
		boolean creatureIsOwnedByUserTmp = creatureIsOwnedByUser;
		callbackMapForTreeitems.put(addleg, () -> {
			// TODO Auto-generated method stub
			System.out.println("Adding Leg");
			String xmlContent;
			try {
				xmlContent = ScriptingEngine.codeFromGit("https://gist.github.com/b5b9450f869dd0d2ea30.git",
						"defaultleg.xml")[0];
				DHParameterKinematics newLeg = new DHParameterKinematics(null,
						IOUtils.toInputStream(xmlContent, "UTF-8"));
				System.out.println("Leg has " + newLeg.getNumberOfLinks() + " links");
				addAppendage(device, view, device.getLegs(), newLeg, legs, rootItem, callbackMapForTreeitems,
						widgetMapForTreeitems, creatureLab, creatureIsOwnedByUserTmp);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});
		TreeItem<String> regnerate = new TreeItem<String>("Generate Cad",AssetFactory.loadIcon("Generate-Cad.png"));

		callbackMapForTreeitems.put(regnerate, () -> {
			creatureLab.generateCad();

		});
		TreeItem<String> printable = new TreeItem<String>("Printable Cad",AssetFactory.loadIcon("Printable-Cad.png"));
	
		callbackMapForTreeitems.put(printable, () -> {
			File defaultStlDir = new File(System.getProperty("user.home") + "/bowler-workspace/STL/");
			if (!defaultStlDir.exists()) {
				defaultStlDir.mkdirs();
			}
			Platform.runLater(()->{
				DirectoryChooser chooser = new DirectoryChooser();
				chooser.setTitle("Select Output Directory For .STL files");

				chooser.setInitialDirectory(defaultStlDir);
				File baseDirForFiles = chooser.showDialog(BowlerStudioModularFrame.getPrimaryStage());
				new Thread() {

					public void run() {
						MobileBaseCadManager baseManager = MobileBaseCadManager.get(device) ;
						if (baseDirForFiles == null) {
							return;
						}
						ArrayList<File> files;
						try {
							files = baseManager.generateStls((MobileBase) device, baseDirForFiles);
							Platform.runLater(() -> {
								Alert alert = new Alert(AlertType.INFORMATION);
								alert.setTitle("Stl Export Success!");
								alert.setHeaderText("Stl Export Success");
								alert.setContentText(
										"All SLT's for the Creature Generated at\n" + files.get(0).getAbsolutePath());
								alert.setWidth(500);
								alert.initModality(Modality.APPLICATION_MODAL);
								alert.show();
							});
						} catch (Exception e) {
							BowlerStudioController.highlightException(baseManager.getCadScript(), e);
						}

					}
				}.start();
			});
		
		});

		TreeItem<String> makeCopy = new TreeItem<>("Make Copy of Creature",AssetFactory.loadIcon("Make-Copy-of-Creature.png"));
		callbackMapForTreeitems.put(makeCopy, () -> {
			Platform.runLater(() -> {
				String oldname = device.getScriptingName();
				TextInputDialog dialog = new TextInputDialog(oldname + "_copy");
				dialog.setTitle("Making a copy of " + oldname);
				dialog.setHeaderText("Set the scripting name for this creature");
				dialog.setContentText("Please the name of the new creature:");

				// Traditional way to get the response value.
				Optional<String> result = dialog.showAndWait();
				if (result.isPresent()) {
					view.getSelectionModel().select(rootItem);
					new Thread() {
						public void run() {
							System.out.println("Your new creature: " + result.get());
							String newName = result.get();
							device.setScriptingName(newName);
							
							
							
							GitHub github = ScriptingEngine.getGithub();
							GHGistBuilder builder = github.createGist();
							builder.description(newName + " copy of " + oldname);
							String filename = newName + ".xml";
							builder.file(filename, "<none>");
							builder.public_(true);
							GHGist gist;
							try {
								gist = builder.create();
								String gitURL = "https://gist.github.com/"
										+ ScriptingEngine.urlToGist(gist.getHtmlUrl()) + ".git";

								System.out.println("Creating new Robot repo");
								while (true) {
									try {
										ScriptingEngine.fileFromGit(gitURL, filename);
										break;
									} catch (Exception e) {

									}
									ThreadUtil.wait(500);
									Log.warn(gist + " not built yet");
								}
								BowlerStudio.openUrlInNewTab(new URL(gist.getHtmlUrl()));
								System.out.println("Creating gist at: " + gitURL);
								
								
								
								System.out.println("copy Cad engine ");
								device.setGitCadEngine(
										copyGitFile( device.getGitCadEngine()[0],
												gitURL,
												 device.getGitCadEngine()[1]));
								System.out.println("copy walking engine Was: "+device.getGitWalkingEngine());
								device.setGitWalkingEngine(
										copyGitFile(device.getGitWalkingEngine()[0],
												gitURL,
												device.getGitWalkingEngine()[1]));
								//System.out.println("is now "+device.getGitWalkingEngine());									
								for (DHParameterKinematics dh : device.getAllDHChains()) {
									//System.out.println("copy Leg Cad engine "+dh.getGitCadEngine());
									dh.setGitCadEngine(
											copyGitFile(dh.getGitCadEngine()[0],
													gitURL,
													dh.getGitCadEngine()[1]));
									
									//System.out.println("copy Leg  Dh engine ");
									dh.setGitDhEngine(
											copyGitFile(
													dh.getGitDhEngine()[0],
													gitURL,
													dh.getGitDhEngine()[1]));
								}
							
								String xml = device.getXml();
								
								ScriptingEngine.pushCodeToGit(gitURL, "master", filename, xml, "new Robot content");

								MobileBase mb = new MobileBase(IOUtils.toInputStream(xml, "UTF-8"));

								mb.setGitSelfSource(new String[] { gitURL, newName + ".xml" });
								device.disconnect();
								
								ConnectionManager.addConnection(mb, mb.getScriptingName());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							// DeviceManager.addConnection(newDevice,
							// newDevice.getScriptingName());
						}
					}.start();
				}
			});
		});

		TreeItem<String> owner = new TreeItem<>("Scripts",AssetFactory.loadIcon("Owner.png"));
		TreeItem<String> setCAD = new TreeItem<>("Set CAD Engine...",AssetFactory.loadIcon("Set-CAD-Engine.png"));
		callbackMapForTreeitems.put(setCAD, () -> {
			PromptForGit.prompt("Select a CAD Engine From a Gist", device.getGitCadEngine()[0], (gitsId, file) -> {
				Log.warn("Loading cad engine");
				try {
					creatureLab.setGitCadEngine(gitsId, file, device);
					File code = ScriptingEngine.fileFromGit(gitsId, file);
					BowlerStudio.createFileTab(code);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		});
		TreeItem<String> editCAD = new TreeItem<>("Edit CAD Engine...",AssetFactory.loadIcon("Edit-CAD-Engine.png"));
		callbackMapForTreeitems.put(editCAD, () -> {
			try {
				File code = ScriptingEngine.fileFromGit(device.getGitCadEngine()[0], device.getGitCadEngine()[1]);
				BowlerStudio.createFileTab(code);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		TreeItem<String> resetWalking = new TreeItem<>("Set Walking Engine...",AssetFactory.loadIcon("Set-Walking-Engine.png"));
		callbackMapForTreeitems.put(resetWalking, () -> {
			PromptForGit.prompt("Select a Walking Engine From a Gist", device.getGitWalkingEngine()[0],
					(gitsId, file) -> {
				Log.warn("Loading walking engine");
				try {
					creatureLab.setGitWalkingEngine(gitsId, file, device);
					File code = ScriptingEngine.fileFromGit(gitsId, file);
					BowlerStudio.createFileTab(code);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		});
		TreeItem<String> editWalking = new TreeItem<>("Edit Walking Engine...",AssetFactory.loadIcon("Edit-Walking-Engine.png"));
		callbackMapForTreeitems.put(editWalking, () -> {
			try {
				File code = ScriptingEngine.fileFromGit(device.getGitWalkingEngine()[0],
						device.getGitWalkingEngine()[1]);
				BowlerStudio.createFileTab(code);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

		TreeItem<String> addFixed = new TreeItem<>("Add Fixed Wheel",AssetFactory.loadIcon("Add-Fixed-Wheel.png"));

		callbackMapForTreeitems.put(addFixed, () -> {
			// TODO Auto-generated method stub
			System.out.println("Adding Fixed Wheel");
			try {
				String xmlContent = ScriptingEngine.codeFromGit("https://gist.github.com/b5b9450f869dd0d2ea30.git",
						"defaultFixed.xml")[0];
				DHParameterKinematics newArm = new DHParameterKinematics(null,
						IOUtils.toInputStream(xmlContent, "UTF-8"));
				System.out.println("Arm has " + newArm.getNumberOfLinks() + " links");
				addAppendage(device, view, device.getDrivable(), newArm, drive, rootItem, callbackMapForTreeitems,
						widgetMapForTreeitems, creatureLab, creatureIsOwnedByUserTmp);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});
		TreeItem<String> addsteerable = new TreeItem<>("Add Steerable Wheel",AssetFactory.loadIcon("Add-Steerable-Wheel.png"));

		callbackMapForTreeitems.put(addsteerable, () -> {
			// TODO Auto-generated method stub
			System.out.println("Adding Steerable Wheel");
			try {
				String xmlContent = ScriptingEngine.codeFromGit("https://gist.github.com/b5b9450f869dd0d2ea30.git",
						"defaultSteerable.xml")[0];
				DHParameterKinematics newArm = new DHParameterKinematics(null,
						IOUtils.toInputStream(xmlContent, "UTF-8"));
				System.out.println("Arm has " + newArm.getNumberOfLinks() + " links");
				addAppendage(device, view, device.getSteerable(), newArm, steer, rootItem, callbackMapForTreeitems,
						widgetMapForTreeitems, creatureLab, creatureIsOwnedByUserTmp);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});
		TreeItem<String> item = new TreeItem<>("Add Arm",AssetFactory.loadIcon("Add-Arm.png"));

		callbackMapForTreeitems.put(item, () -> {
			// TODO Auto-generated method stub
			System.out.println("Adding Arm");
			try {
				String xmlContent = ScriptingEngine.codeFromGit("https://gist.github.com/b5b9450f869dd0d2ea30.git",
						"defaultarm.xml")[0];
				DHParameterKinematics newArm = new DHParameterKinematics(null,
						IOUtils.toInputStream(xmlContent, "UTF-8"));
				System.out.println("Arm has " + newArm.getNumberOfLinks() + " links");
				addAppendage(device, view, device.getAppendages(), newArm, arms, rootItem, callbackMapForTreeitems,
						widgetMapForTreeitems, creatureLab, creatureIsOwnedByUserTmp);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});

		rootItem.getChildren().addAll(physics,regnerate, printable,item, addleg,addFixed,addsteerable, makeCopy);

		if (creatureIsOwnedByUser) {
			owner.getChildren().addAll(publish, editWalking, editCAD, resetWalking, setCAD);
			rootItem.getChildren().add(owner);
		}
	}

	private static void getNextChannel(MobileBase base, LinkConfiguration confOfChannel) {
		HashMap<String, HashMap<Integer, Boolean>> deviceMap = new HashMap<>();

		for (DHParameterKinematics dh : base.getAllDHChains()) {
			for (LinkConfiguration conf : dh.getLinkConfigurations()) {
				HashMap<Integer, Boolean> channelMap;
				if (deviceMap.get(conf.getDeviceScriptingName()) == null) {
					deviceMap.put(conf.getDeviceScriptingName(), new HashMap<Integer, Boolean>());
				}
				channelMap = deviceMap.get(conf.getDeviceScriptingName());
				channelMap.put(conf.getHardwareIndex(), true);
				for (LinkConfiguration sl : conf.getSlaveLinks()) {
					HashMap<Integer, Boolean> slavechannelMap;
					if (deviceMap.get(sl.getDeviceScriptingName()) == null) {
						deviceMap.put(sl.getDeviceScriptingName(), new HashMap<Integer, Boolean>());
					}
					slavechannelMap = deviceMap.get(sl.getDeviceScriptingName());
					slavechannelMap.put(sl.getHardwareIndex(), true);
				}
			}
		}
		for (Map.Entry<String, HashMap<Integer, Boolean>> entry : deviceMap.entrySet()) {
			HashMap<Integer, Boolean> chans = entry.getValue();
			for (int i = 0; i < 24; i++) {

				if (chans.get(i) == null) {
					System.err.println("Channel free: " + i + " on device " + entry.getKey());
					confOfChannel.setDeviceScriptingName(entry.getKey());
					confOfChannel.setHardwareIndex(i);
					return;
				}
			}
		}

		throw new RuntimeException("No channels are availible on given devices");
	}

	private static void addAppendage(MobileBase base, TreeView<String> view,
			ArrayList<DHParameterKinematics> deviceList, DHParameterKinematics newDevice, TreeItem<String> rootItem,
			TreeItem<String> topLevel, HashMap<TreeItem<String>, Runnable> callbackMapForTreeitems,
			HashMap<TreeItem<String>, Group> widgetMapForTreeitems, CreatureLab creatureLab,
			boolean creatureIsOwnedByUser) {

		Platform.runLater(() -> {
			TextInputDialog dialog = new TextInputDialog(newDevice.getScriptingName());
			dialog.setTitle("Add a new limb of");
			dialog.setHeaderText("Set the scripting name for this limb");
			dialog.setContentText("Please the name of the new limb:");

			// Traditional way to get the response value.
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent()) {
				view.getSelectionModel().select(rootItem);
				new Thread() {
					public void run() {
						System.out.println("Your new limb: " + result.get());
						newDevice.setScriptingName(result.get());
						DeviceManager.addConnection(newDevice, newDevice.getScriptingName());
						deviceList.add(newDevice);
						for (LinkConfiguration conf : newDevice.getLinkConfigurations()) {
							try {
								getNextChannel(base, conf);
							} catch (RuntimeException exc) {
								String newname = conf.getDeviceScriptingName() + "_new";
								System.err.println("Adding new device to provide new channels: " + newname);
								conf.setDeviceScriptingName(newname);
								getNextChannel(base, conf);
							}
							newDevice.getFactory().refreshHardwareLayer(conf);

						}

						rootItem.setExpanded(true);
						try {
							loadSingleLimb(base, view, newDevice, rootItem, callbackMapForTreeitems, widgetMapForTreeitems,
									creatureLab, creatureIsOwnedByUser);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						creatureLab.generateCad();
					}
				}.start();
			}
		});

	}

	private static TreeItem<String> loadLimbs(MobileBase base, TreeView<String> view,
			ArrayList<DHParameterKinematics> drivable, String label, TreeItem<String> rootItem,
			HashMap<TreeItem<String>, Runnable> callbackMapForTreeitems,
			HashMap<TreeItem<String>, Group> widgetMapForTreeitems, CreatureLab creatureLab,
			boolean creatureIsOwnedByUser) throws Exception {

		TreeItem<String> apps = new TreeItem<>(label,AssetFactory.loadIcon("Load-Limb-"+label.replace(' ', '-')+".png"));
		rootItem.getChildren().add(apps);
		if (drivable.isEmpty())
			return apps;
		for (DHParameterKinematics dh : drivable) {
			loadSingleLimb(base, view, dh, apps, callbackMapForTreeitems, widgetMapForTreeitems, creatureLab,
					creatureIsOwnedByUser);
		}

		return apps;
	}

	private static void setHardwareConfig(MobileBase base,LinkConfiguration conf, LinkFactory factory, TreeItem<String> rootItem,
			HashMap<TreeItem<String>, Runnable> callbackMapForTreeitems,
			HashMap<TreeItem<String>, Group> widgetMapForTreeitems) throws Exception {

		TreeItem<String> hwConf = new TreeItem<>("Hardware Config " + conf.getName(),AssetFactory.loadIcon("Hardware-Config.png"));
		callbackMapForTreeitems.put(hwConf, () -> {
			if (widgetMapForTreeitems.get(hwConf) == null) {
				// create the widget for the leg when looking at it for the
				// first time
				widgetMapForTreeitems.put(hwConf, new Group(new LinkConfigurationWidget(conf, factory)));
			}
			BowlerStudio.select( base,conf);
		});
		rootItem.getChildren().add(hwConf);
	}

	private static void loadSingleLink(int linkIndex, MobileBase base, TreeView<String> view, LinkConfiguration conf,
			DHParameterKinematics dh, TreeItem<String> rootItem,
			HashMap<TreeItem<String>, Runnable> callbackMapForTreeitems,
			HashMap<TreeItem<String>, Group> widgetMapForTreeitems, CreatureLab creatureLab) throws Exception {

		DHLink dhLink = dh.getChain().getLinks().get(linkIndex);
		LinkSliderWidget lsw = new LinkSliderWidget(linkIndex, dhLink, dh);
		TreeItem<String> link = new TreeItem<>(conf.getName(),AssetFactory.loadIcon("Move-Single-Motor.png"));
		callbackMapForTreeitems.put(link, () -> {
			if (widgetMapForTreeitems.get(link) == null) {
				// create the widget for the leg when looking at it for the
				// first time
				widgetMapForTreeitems.put(link,lsw);
			}
			 BowlerJInputDevice controller = creatureLab.getController();
			 if(controller!=null){
				 lsw.setGameController(controller); 
			 }
			 BowlerStudio.select( base,conf);
			 //select( base, dh);
			// activate controller
		});

		TreeItem<String> slaves = new TreeItem<>("Slaves to " + conf.getName(),AssetFactory.loadIcon("Slave-Links.png"));
		LinkFactory slaveFactory = dh.getFactory().getLink(conf).getSlaveFactory();
		for (LinkConfiguration co : conf.getSlaveLinks()) {

			setHardwareConfig(base,co, slaveFactory, slaves, callbackMapForTreeitems, widgetMapForTreeitems);
		}

		TreeItem<String> addSlaves = new TreeItem<>("Add Slave to " + conf.getName(),AssetFactory.loadIcon("Add-Slave-Links.png"));

		callbackMapForTreeitems.put(addSlaves, () -> {
			// if(widgetMapForTreeitems.get(advanced)==null){
			// //create the widget for the leg when looking at it for the first
			// time
			// widgetMapForTreeitems.put(advanced, new DhChainWidget(dh,
			// creatureLab));
			// }
			Platform.runLater(() -> {
				TextInputDialog dialog = new TextInputDialog(conf.getName() + "_SLAVE_" + conf.getSlaveLinks().size());
				dialog.setTitle("Add a new Slave link of");
				dialog.setHeaderText("Set the scripting name for this Slave link");
				dialog.setContentText("Please the name of the new Slave link:");

				// Traditional way to get the response value.
				Optional<String> result = dialog.showAndWait();
				if (result.isPresent()) {
					view.getSelectionModel().select(rootItem);
					new Thread() {
						public void run() {
							System.out.println("Your new link: " + result.get());
							LinkConfiguration newLink = new LinkConfiguration();
							newLink.setType(conf.getTypeEnum());
							newLink.setTypeString(conf.getTypeString());
							getNextChannel(base, newLink);
							newLink.setName(result.get());
							conf.getSlaveLinks().add(newLink);
							slaveFactory.getLink(newLink);
							try {
								setHardwareConfig(base,newLink, slaveFactory, slaves, callbackMapForTreeitems,
										widgetMapForTreeitems);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}.start();
				}
			});
		});

		slaves.getChildren().add(0, addSlaves);
		TreeItem<String> remove = new TreeItem<>("Remove " + conf.getName(),AssetFactory.loadIcon("Remove-Link.png"));
		callbackMapForTreeitems.put(remove, () -> {
			Platform.runLater(() -> {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Confirm removing link");
				alert.setHeaderText("This will remove " + conf.getName());
				alert.setContentText("Are sure you wish to remove this link?");

				Optional<ButtonType> result = alert.showAndWait();
				view.getSelectionModel().select(rootItem);
				if (result.get() == ButtonType.OK) {

					rootItem.getChildren().remove(link);
					LinkFactory factory = dh.getFactory();
					// remove the link listener while the number of links could
					// chnage
					factory.removeLinkListener(dh);
					DHChain chain = dh.getDhChain();
					chain.getLinks().remove(linkIndex);
					factory.deleteLink(linkIndex);
					// set the modified kinematics chain
					dh.setChain(chain);
					// once the new link configuration is set up, re add the
					// listener
					factory.addLinkListener(dh);
					creatureLab.generateCad();
				}
			});

		});

		TreeItem<String> design = new TreeItem<>("Design Parameters " + conf.getName(),AssetFactory.loadIcon("Design-Parameter-Adjustment.png"));

		callbackMapForTreeitems.put(design, () -> {
			if (widgetMapForTreeitems.get(design) == null) {
				// create the widget for the leg when looking at it for the
				// first time
				widgetMapForTreeitems.put(design, new DhSettingsWidget(dhLink, dh, new IOnEngineeringUnitsChange() {

					@Override
					public void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onSliderDoneMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
						creatureLab.onSliderDoneMoving(source,newAngleDegrees);
					}
				}));
			}
			BowlerStudio.select( base,conf);
		});

		link.getChildren().addAll(design);

		setHardwareConfig(base,conf, dh.getFactory(), link, callbackMapForTreeitems, widgetMapForTreeitems);

		link.getChildren().addAll(slaves, remove);
		rootItem.getChildren().add(0, link);

	}

	@SuppressWarnings("unchecked")
	private static void loadSingleLimb(MobileBase base, TreeView<String> view, DHParameterKinematics dh,
			TreeItem<String> rootItem, HashMap<TreeItem<String>, Runnable> callbackMapForTreeitems,
			HashMap<TreeItem<String>, Group> widgetMapForTreeitems, CreatureLab creatureLab,
			boolean creatureIsOwnedByUser) throws Exception {
		
		TreeItem<String> dhItem = new TreeItem<>(dh.getScriptingName(),AssetFactory.loadIcon("Move-Limb.png"));
		JogWidget widget = new JogWidget(dh);
		callbackMapForTreeitems.put(dhItem, () -> {
			if (widgetMapForTreeitems.get(dhItem) == null) {
				// create the widget for the leg when looking at it for the
				// first time
				
				VBox jog = new VBox(10);
				jog.getChildren().add(new Label("Jog Limb"));
				jog.getChildren().add(widget);
				widgetMapForTreeitems.put(dhItem, new Group(new JogWidget(dh)));
			}
			 BowlerJInputDevice controller = creatureLab.getController();
			 if(controller!=null){
				 widget.setGameController(controller); 
			 }
			 BowlerStudio.select( base, dh);
			
		});

		TreeItem<String> remove = new TreeItem<>("Remove " + dh.getScriptingName(),AssetFactory.loadIcon("Remove-Limb.png"));

		callbackMapForTreeitems.put(remove, () -> {
			Platform.runLater(() -> {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Confirm removing limb");
				alert.setHeaderText("This will remove " + dh.getScriptingName());
				alert.setContentText("Are sure you wish to remove this limb?");

				Optional<ButtonType> result = alert.showAndWait();
				view.getSelectionModel().select(rootItem);
				if (result.get() == ButtonType.OK) {

					rootItem.getChildren().remove(dhItem);
					if (base.getLegs().contains(dh)) {
						base.getLegs().remove(dh);
					}
					if (base.getAppendages().contains(dh)) {
						base.getAppendages().remove(dh);
					}
					if (base.getSteerable().contains(dh)) {
						base.getSteerable().remove(dh);
					}
					if (base.getDrivable().contains(dh)) {
						base.getDrivable().remove(dh);
					}
					creatureLab.generateCad();
				}
			});

		});
		int j = 0;
		for (LinkConfiguration conf : dh.getFactory().getLinkConfigurations()) {
			loadSingleLink(j++, base, view, conf, dh, dhItem, callbackMapForTreeitems, widgetMapForTreeitems,
					creatureLab);
		}

		TreeItem<String> addLink = new TreeItem<>("Add Link",AssetFactory.loadIcon("Add-Link.png"));

		callbackMapForTreeitems.put(addLink, () -> {
			// if(widgetMapForTreeitems.get(advanced)==null){
			// //create the widget for the leg when looking at it for the first
			// time
			// widgetMapForTreeitems.put(advanced, new DhChainWidget(dh,
			// creatureLab));
			// }
			Platform.runLater(() -> {
				TextInputDialog dialog = new TextInputDialog("Link_" + dh.getLinkConfigurations().size());
				dialog.setTitle("Add a new link of");
				dialog.setHeaderText("Set the scripting name for this link");
				dialog.setContentText("Please the name of the new link:");

				// Traditional way to get the response value.
				Optional<String> result = dialog.showAndWait();
				if (result.isPresent()) {
					view.getSelectionModel().select(rootItem);
					new Thread() {
						public void run() {
							System.out.println("Your new link: " + result.get());
							LinkConfiguration newLink = new LinkConfiguration();
							newLink.setType(dh.getFactory().getLinkConfigurations().get(0).getTypeEnum());
							getNextChannel(base, newLink);
							newLink.setName(result.get());
							if (dh != null)
								dh.addNewLink(newLink, new DHLink(0, 0, 0, 0));

							try {
								loadSingleLink(dh.getLinkConfigurations().size() - 1, base, view, newLink, dh, dhItem,
										callbackMapForTreeitems, widgetMapForTreeitems, creatureLab);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							creatureLab.generateCad();
						}
					}.start();
				}
			});
		});


		TreeItem<String> advanced = new TreeItem<>("Advanced Configuration",AssetFactory.loadIcon("Advanced-Configuration.png"));

		callbackMapForTreeitems.put(advanced, () -> {
			if (widgetMapForTreeitems.get(advanced) == null) {
				// create the widget for the leg when looking at it for the
				// first time
				try{
					widgetMapForTreeitems.put(advanced, new DhChainWidget(dh, creatureLab));
				}catch(Exception ex){
					BowlerStudio.printStackTrace(ex);
				}
			}

		});
		dhItem.getChildren().addAll(addLink, advanced, remove);
		if (creatureIsOwnedByUser) {
			TreeItem<String> owner = new TreeItem<>("Scripts",AssetFactory.loadIcon("Owner.png"));
			TreeItem<String> setCAD = new TreeItem<>("Set CAD Engine...",AssetFactory.loadIcon("Set-CAD-Engine.png"));
			callbackMapForTreeitems.put(setCAD, () -> {
				PromptForGit.prompt("Select a CAD Engine From Git", dh.getGitCadEngine()[0], (gitsId, file) -> {
					Log.warn("Loading cad engine");
					try {
						creatureLab.setGitCadEngine(gitsId, file, dh);
						openCadTab(creatureLab, gitsId, file);

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
			});
			TreeItem<String> editCAD = new TreeItem<>("Edit CAD Engine...",AssetFactory.loadIcon("Edit-CAD-Engine.png"));
			callbackMapForTreeitems.put(editCAD, () -> {
				try {
					openCadTab(creatureLab, dh.getGitCadEngine()[0], dh.getGitCadEngine()[1]);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			TreeItem<String> resetWalking = new TreeItem<>("Set Dh Kinematics Engine...",AssetFactory.loadIcon("Set-DH-Kinematics.png"));
			callbackMapForTreeitems.put(resetWalking, () -> {
				PromptForGit.prompt("Select a DH Solver Engine From Git", dh.getGitDhEngine()[0], (gitsId, file) -> {
					Log.warn("Loading walking engine");
					try {
						creatureLab.setGitDhEngine(gitsId, file, dh);
						File code = ScriptingEngine.fileFromGit(gitsId, file);
						BowlerStudio.createFileTab(code);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
			});
			TreeItem<String> editWalking = new TreeItem<>("Edit Kinematics Engine...",AssetFactory.loadIcon("Edit-Kinematics-Engine.png"));
			callbackMapForTreeitems.put(editWalking, () -> {
				try {
					File code = ScriptingEngine.fileFromGit(dh.getGitDhEngine()[0], dh.getGitDhEngine()[1]);
					BowlerStudio.createFileTab(code);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			owner.getChildren().addAll(editWalking, editCAD, resetWalking, setCAD);

			dhItem.getChildren().add(owner);
		}
		rootItem.getChildren().add(dhItem);
		double[] vect = dh.getCurrentJointSpaceVector();
		for (int i = 0; i < vect.length; i++) {
			vect[i] = 0;
		}
		try {
			dh.setDesiredJointSpaceVector(vect, 1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dh.updateCadLocations();

	}

	private static void openCadTab(CreatureLab creatureLab, String gitsId, String file)
			throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		File code = ScriptingEngine.fileFromGit(gitsId, file);
		ScriptingFileWidget wid = BowlerStudio.createFileTab(code);
	
	}

}
