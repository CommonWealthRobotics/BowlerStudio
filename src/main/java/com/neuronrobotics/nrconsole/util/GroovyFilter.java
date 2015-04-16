package com.neuronrobotics.nrconsole.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class GroovyFilter extends FileFilter {

	public String getDescription() {
		return "Script (.groovy .java)";
	}
	
	public boolean accept(File f) {
		if(f.isDirectory()) {
			return true;
		}
		
		return f.getName().matches("([^\\s]+(\\.(?i)(groovy|java))$)");
	}

}
