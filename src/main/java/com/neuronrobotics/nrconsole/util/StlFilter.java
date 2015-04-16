package com.neuronrobotics.nrconsole.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class StlFilter extends FileFilter {
	
	public String getDescription() {
		return "STL File";
	}
	public boolean accept(File f) {
		if(f.isDirectory()) {
			return true;
		}
		String path = f.getAbsolutePath().toLowerCase();
		if ((path.toLowerCase().endsWith("stl") && (path.charAt(path.length() - 3)) == '.')) {
			return true;
		}

		return f.getName().matches(".+\\.stl$");
	}

}
