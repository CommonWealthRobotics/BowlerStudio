package com.neuronrobotics.nrconsole.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class DirectoryFilter extends FileFilter {
	
	public String getDescription() {
		return "Select Directory";
	}
	public boolean accept(File f) {
		if(f.isDirectory()) {
			return true;
		}
		return false;
	}

}
