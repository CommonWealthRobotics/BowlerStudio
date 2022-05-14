package com.neuronrobotics.bowlerstudio;

import java.util.HashMap;

public class MeasurmentConfig {
	private String key;
	private HashMap<String, Object> configs;
	public MeasurmentConfig(String key,HashMap<String, Object> configs){
		if(configs==null)
			throw new RuntimeException("Null configs not allowed!");
		this.configs = configs;
		this.setKey(key);
		System.out.println("Adding Measurment "+key+" "+getMeasurment());
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getMeasurment() {
		return configs.get(key).toString();
	}
	public void setMeasurment(String measurment) {
		System.out.println("Setting field "+key+" to "+measurment);
		configs.put(key, measurment);
	}
}
