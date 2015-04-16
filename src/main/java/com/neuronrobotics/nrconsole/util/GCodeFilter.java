package com.neuronrobotics.nrconsole.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class GCodeFilter extends FileFilter {
	
	public String getDescription() {
		return "GCode File";
	}
	public boolean accept(File f) {
		if(f.isDirectory()) {
			return true;
		}
		String path = f.getAbsolutePath().toLowerCase();
		if ((path.endsWith("gcode") && (path.charAt(path.length() - 5)) == '.')) {
			return true;
		}
		if ((path.endsWith("ngc") && (path.charAt(path.length() - 3)) == '.')) {
			return true;
		}
		return f.getName().matches(".+\\.gcode$") || f.getName().matches(".+\\.ngc$");
	}

}
