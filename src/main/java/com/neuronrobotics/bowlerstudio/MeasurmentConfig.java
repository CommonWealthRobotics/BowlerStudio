package com.neuronrobotics.bowlerstudio;

import java.util.HashMap;
import java.util.Map;

import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;

public class MeasurmentConfig {
	private String key;
	private String type;
	private String id;

	public MeasurmentConfig(String key, String type, String id) {
		this.type = type;
		this.id = id;
		this.setKey(key);
		System.out.println("Adding Measurment " + key + " " + getMeasurment());
		getMeasurment();
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	

	public String getMeasurment() {
//		if(configs.get(key)==null)
//			configs.put(key, "");

		try {
			return Vitamins.getMeasurement(type, id,key).toString();
		} catch (Exception ex) {
			System.out.print("\n\tGetting measurement of " + key);
			ex.printStackTrace(System.out);
			return "";
		}
	}

	public void setMeasurment(String measurment) {
		System.out.println("Setting field "+type+", "+ id +", "+ key + " to " + measurment);
		Vitamins.putMeasurment(type, id,key, measurment);
	}
}
