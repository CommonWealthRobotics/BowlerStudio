package com.neuronrobotics.bowlerstudio.creature;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Group;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngineWidget;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.common.DeviceManager;

public class MobleBaseFactory {

	public static void load(MobileBase device, TreeItem<String> rootItem,
			HashMap<TreeItem, Runnable> callbackMapForTreeitems,
			HashMap<TreeItem, Group> widgetMapForTreeitems) {
		TreeItem<String> legs =loadLimbs(device.getLegs(), "Legs", rootItem, callbackMapForTreeitems,
				widgetMapForTreeitems);
		TreeItem<String> arms =loadLimbs(device.getAppendages(), "Arms", rootItem,
				callbackMapForTreeitems, widgetMapForTreeitems);
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
					addAppendage(device.getLegs(), newLeg, legs, rootItem, callbackMapForTreeitems, widgetMapForTreeitems);
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
					String xmlContent = ScriptingEngineWidget.codeFromGistID("b5b9450f869dd0d2ea30","defaultarm.xml")[0];
					DHParameterKinematics newArm = new DHParameterKinematics(null,IOUtils.toInputStream(xmlContent, "UTF-8"));
					System.out.println("Arm has "+newArm.getNumberOfLinks()+" links");
					addAppendage(device.getAppendages(), newArm, arms, rootItem, callbackMapForTreeitems, widgetMapForTreeitems);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		
		rootItem.getChildren().addAll(item, addleg);
	}
	
	private static void addAppendage(
			ArrayList<DHParameterKinematics> deviceList,
			DHParameterKinematics newDevice, 
			TreeItem<String> rootItem,
			TreeItem<String> topLevel,
			HashMap<TreeItem, Runnable> callbackMapForTreeitems,
			HashMap<TreeItem, Group> widgetMapForTreeitems){
		DeviceManager.addConnection(newDevice, newDevice.getScriptingName());
		if(deviceList.size()==0){
			topLevel.getChildren().add(rootItem);			
		}
		deviceList.add(newDevice);
		
		rootItem.setExpanded(true);
		loadSingleLimb(newDevice,rootItem,callbackMapForTreeitems, widgetMapForTreeitems);
	}

	private static TreeItem<String> loadLimbs(ArrayList<DHParameterKinematics> drivable,
			String label, TreeItem<String> rootItem,
			HashMap<TreeItem, Runnable> callbackMapForTreeitems,
			HashMap<TreeItem, Group> widgetMapForTreeitems) {

	
		TreeItem<String> apps = new TreeItem<String>(label);
		if (drivable.size() == 0)
			return apps;
		for (DHParameterKinematics dh : drivable) {
			loadSingleLimb(dh,apps,callbackMapForTreeitems, widgetMapForTreeitems);
		}
		rootItem.getChildren().add(apps);
		return apps;
	}
	
	private static void loadSingleLimb(DHParameterKinematics dh,
			TreeItem<String> rootItem,
			HashMap<TreeItem, Runnable> callbackMapForTreeitems,
			HashMap<TreeItem, Group> widgetMapForTreeitems){
		
		TreeItem<String> dhItem = new TreeItem<String>(
				dh.getScriptingName());
		rootItem.getChildren().add(dhItem);
		
	}

}
