package com.neuronrobotics.bowlerstudio.creature;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Group;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngineWidget;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.common.DeviceManager;

public class MobleBaseFactory {

	@SuppressWarnings("unchecked")
	public static void load(MobileBase device, TreeItem<String> rootItem,
			HashMap<TreeItem<String>, Runnable> callbackMapForTreeitems,
			HashMap<TreeItem<String>, Group> widgetMapForTreeitems, CreatureLab creatureLab) {
		TreeItem<String> legs =loadLimbs(device.getLegs(), "Legs", rootItem, callbackMapForTreeitems,
				widgetMapForTreeitems,creatureLab);
		TreeItem<String> arms =loadLimbs(device.getAppendages(), "Arms", rootItem,
				callbackMapForTreeitems, widgetMapForTreeitems,creatureLab);
//		TreeItem<String> steer =loadLimb(device.getSteerable(), "Steerable", rootItem,
//				callbackMapForTreeitems, widgetMapForTreeitems);
//		TreeItem<String> drive =loadLimb(device.getDrivable(), "Drivable", rootItem,
//				callbackMapForTreeitems, widgetMapForTreeitems);
		
		TreeItem<String> addleg = new TreeItem<String>("Add Leg");

		callbackMapForTreeitems.put(addleg, () -> {
			// TODO Auto-generated method stub
				System.out.println("Adding Leg");
				String xmlContent;
				try {
					xmlContent = ScriptingEngineWidget.codeFromGistID("b5b9450f869dd0d2ea30","defaultleg.xml")[0];
					DHParameterKinematics newLeg = new DHParameterKinematics(null,IOUtils.toInputStream(xmlContent, "UTF-8"));
					System.out.println("Leg has "+newLeg.getNumberOfLinks()+" links");
					addAppendage(device.getLegs(), newLeg, legs, rootItem, callbackMapForTreeitems, widgetMapForTreeitems,creatureLab);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				BowlerStudioController.setCsg(creatureLab.generateBody(device));
			});
		TreeItem<String> regnerate = new TreeItem<String>("Generate Cad");

		callbackMapForTreeitems.put(regnerate, () -> {
				creatureLab.generateCad();
				
			});
		
		TreeItem<String> item = new TreeItem<String>("Add Arm");

		callbackMapForTreeitems.put(item, () -> {
			// TODO Auto-generated method stub
				System.out.println("Adding Arm");
				try {
					String xmlContent = ScriptingEngineWidget.codeFromGistID("b5b9450f869dd0d2ea30","defaultarm.xml")[0];
					DHParameterKinematics newArm = new DHParameterKinematics(null,IOUtils.toInputStream(xmlContent, "UTF-8"));
					System.out.println("Arm has "+newArm.getNumberOfLinks()+" links");
					addAppendage(device.getAppendages(), newArm, arms, rootItem, callbackMapForTreeitems, widgetMapForTreeitems,creatureLab);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				BowlerStudioController.setCsg(creatureLab.generateBody(device));
				
			});
		rootItem.getChildren().addAll(item, addleg,regnerate);
	}
	
	
	
	private static void addAppendage(
			ArrayList<DHParameterKinematics> deviceList,
			DHParameterKinematics newDevice, 
			TreeItem<String> rootItem,
			TreeItem<String> topLevel,
			HashMap<TreeItem<String>, Runnable> callbackMapForTreeitems,
			HashMap<TreeItem<String>, Group> widgetMapForTreeitems, CreatureLab creatureLab){
		DeviceManager.addConnection(newDevice, newDevice.getScriptingName());
		deviceList.add(newDevice);
		
		rootItem.setExpanded(true);
		loadSingleLimb(newDevice,rootItem,callbackMapForTreeitems, widgetMapForTreeitems,creatureLab);
		
	}

	private static TreeItem<String> loadLimbs(ArrayList<DHParameterKinematics> drivable,
			String label, TreeItem<String> rootItem,
			HashMap<TreeItem<String>, Runnable> callbackMapForTreeitems,
			HashMap<TreeItem<String>, Group> widgetMapForTreeitems, CreatureLab creatureLab) {

	
		TreeItem<String> apps = new TreeItem<String>(label);
		rootItem.getChildren().add(apps);
		if (drivable.size() == 0)
			return apps;
		for (DHParameterKinematics dh : drivable) {
			loadSingleLimb(dh,apps,callbackMapForTreeitems, widgetMapForTreeitems,creatureLab);
		}
		
		return apps;
	}
	
	private static void loadSingleLimb(DHParameterKinematics dh,
			TreeItem<String> rootItem,
			HashMap<TreeItem<String>, Runnable> callbackMapForTreeitems,
			HashMap<TreeItem<String>, Group> widgetMapForTreeitems, CreatureLab creatureLab){
		
		TreeItem<String> dhItem = new TreeItem<String>(
				dh.getScriptingName());
		TreeItem<String> advanced = new TreeItem<String>("Advanced Configuration");
		
		callbackMapForTreeitems.put(advanced, ()->{
			if(widgetMapForTreeitems.get(advanced)==null){
				//create the widget for the leg when looking at it for the first time
				widgetMapForTreeitems.put(advanced, new DhChainWidget(dh, creatureLab));
			}
			
		});
		
		dhItem.getChildren().add(advanced);
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
