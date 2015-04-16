package com.neuronrobotics.nrconsole.util;

import java.util.prefs.Preferences;

import com.neuronrobotics.replicator.driver.Slic3r;

public class PrefsLoader {
	private static final String SLIC3R_LOCATION = "slic3r_path";
	private static final String SLIC3R_RDBTN_LAST = "slic3r_rdbtn_last";
	static String path = "/usr/local/Slic3r/bin/slic3r";
	Preferences prefs = Preferences.userNodeForPackage(this.getClass());
	
	
	public String getSlic3rLocation(){
		return prefs.get(SLIC3R_LOCATION, path);
	}
	public void setSlic3rLocation(String _path){
		prefs.put(SLIC3R_LOCATION, _path);
		Slic3r.setExecutableLocation(_path);
	}
	
	public int getSlic3rRDBTNLast(){
		return prefs.getInt(SLIC3R_RDBTN_LAST, 0);
	}
	public void setSlic3rRDBTNLast(int _btn){
		prefs.putInt(SLIC3R_RDBTN_LAST, _btn);
	}
	
	
	public void loadDefaults(){
		setSlic3rLocation(path);
		setSlic3rRDBTNLast(0);
		Slic3r.setExecutableLocation(path);
	}
	
}
