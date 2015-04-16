package com.neuronrobotics.nrconsole.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class Slic3rFilter extends FileFilter {
	
	public String getDescription() {
		return "Slic3r Executable";
	}
	public boolean accept(File f) {
		if(f.isDirectory()) {
			return true;
		}
		String path = f.getAbsolutePath().toLowerCase();
		if (f.getName().toLowerCase().contains("slic3r")) {
			return true;
		}

		return f.getName().toLowerCase().matches("slic3r");
	}

}
