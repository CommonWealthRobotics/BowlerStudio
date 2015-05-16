package com.neuronrobotics.nrconsole.plugin.DeviceConfig;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public abstract class SettingsPanel extends JPanel {
private ArrayList<MachineSetting> settings = new ArrayList<MachineSetting>();
private List<SettingsChangeListener> listeners = new ArrayList<SettingsChangeListener>();



	public abstract String getPanelName();
	
	public abstract void initComponents();
	public void addListener(SettingsChangeListener toAdd) {
        listeners.add(toAdd);
    }
	public void removeListeners() {
        listeners.clear();
    }
	public void notifySettingsChanged(){
		for (SettingsChangeListener ptl : listeners) {
			ptl.settingsChanged();
		}
	}
	public void notifySettingsRequest(){
		for (SettingsChangeListener ptl : listeners) {
			ptl.settingsRequest();
		}
	}
	
	public void checkNewSettings(){
		notifySettingsRequest();
	}
	
	public ArrayList<MachineSetting> getValues(){
		
		return settings;
	}
	
	public void setValues(ArrayList<MachineSetting> values) {
		settings = values;
		notifySettingsChanged();
	}
	
	public boolean settingExists(String _name){
		for (MachineSetting machineSetting : settings) {
			if (machineSetting.getName() == _name){
				return true;
			}
		}
		return false;
	}
	
	public MachineSetting getSetting(String _name){
		
		for (MachineSetting machineSetting : settings) {
			if (machineSetting.getName() == _name){
				return machineSetting;
			}
		}
		return null;
	}
	public int numSettings(){
		return settings.size();
	}
	
	public MachineSetting getSetting(int index){
		
		return settings.get(index);
	}
	public void setValue(int index, MachineSetting item){
		
		if (settings.size() <= index){
			settings.add(index, item);
		}
		else{
			settings.set(index, item);
		}
		notifySettingsChanged();
	}
	
	public Object getSetValue(String _name){
		
		for (MachineSetting machineSetting : settings) {
			if (machineSetting.getName() == _name){
				return machineSetting.getValue();
			}
		}
		return null;
	}
	public Object getSetValue(int index){		
		
		return settings.get(index).getValue();
	}
	public int getIntValue(String _name){
	
		for (MachineSetting machineSetting : settings) {
			if (machineSetting.getName() == _name){
				return (Integer) machineSetting.getValue();
			}
		}
		return -1;
	}
	public boolean getBooleanValue(String _name){
		
		for (MachineSetting machineSetting : settings) {
			if (machineSetting.getName() == _name){
				return (Boolean) machineSetting.getValue();
			}
		}
		return false;
	}
	public double getDoubleValue(String _name){
	
		for (MachineSetting machineSetting : settings) {
			if (machineSetting.getName() == _name){
				return (Double) machineSetting.getValue();
			}
		}
		return -1;
	}
	public double getDoubleValue(int index){
		
		return (Double) settings.get(index).getValue();
	}
	
	public int getIntegerValue(int index){
			
			return (Integer) settings.get(index).getValue();
		}
	
	public boolean getBooleanValue(int index){
		
		return (Boolean) settings.get(index).getValue();
	}
}
