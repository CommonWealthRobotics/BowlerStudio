package com.neuronrobotics.bowlerstudio.scripting;

import java.util.ArrayList;
import java.util.Properties;

import javafx.scene.control.Tab;

import org.python.util.PythonInterpreter;

import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.DeviceManager;
import com.neuronrobotics.sdk.common.Log;

import eu.mihosoft.vrl.v3d.CSG;

public class JythonHelper implements IScriptingLanguage{

	@Override
	public Object inlineScriptRun(String code, ArrayList<Object> args) {
		Properties props = new Properties();
		PythonInterpreter.initialize(System.getProperties(), props,
				new String[] { "" });
		PythonInterpreter interp = new PythonInterpreter();

		interp.exec("import sys");
		for (String s : ScriptingEngine.getImports()) {

			// s = "import "+s;
			//System.err.println(s);
			if(!s.contains("mihosoft")&&
					!s.contains("haar")&&
					!s.contains("com.neuronrobotics.sdk.addons.kinematics")
					) {
				interp.exec("import "+s);
			} else {
				//from http://stevegilham.blogspot.com/2007/03/standalone-jython-importerror-no-module.html
				try {
					String[] names = s.split("\\.");
					String packname = (names.length>0?names[names.length-1]:s);
					Log.error("Forcing "+s+" as "+packname);
					interp.exec("sys.packageManager.makeJavaPackage(" + s
							+ ", " +packname + ", None)");

					interp.exec("import "+packname);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		for (String pm : DeviceManager.listConnectedDevice(null)) {
			BowlerAbstractDevice bad = DeviceManager.getSpecificDevice(null, pm);
				// passing into the scipt
			try{
				interp.set(bad.getScriptingName(),
						Class.forName(bad.getClass().getName())
								.cast(bad));
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.err.println("Device " + bad.getScriptingName() + " is "
					+ bad);
		}
		interp.set("args", args);

		interp.exec(code);
		ArrayList<Object> results = new ArrayList<>();
		try{
			results.add(interp.get("csg",CSG.class));
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			results.add(interp.get("tab",Tab.class));
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			results.add(interp.get("device",BowlerAbstractDevice.class));
		}catch(Exception e){
			e.printStackTrace();
		}

		Log.debug("Jython return = "+results);
		return results;
	}

	@Override
	public ShellType getShellType() {
		return ShellType.JYTHON;
	}

	@Override
	public boolean isSupportedFileExtenetion(String filename) {
		if (filename.toString().toLowerCase().endsWith(".py")
				|| filename.toString().toLowerCase().endsWith(".jy")) {
			return true;
		}
		return false;
	}

}
