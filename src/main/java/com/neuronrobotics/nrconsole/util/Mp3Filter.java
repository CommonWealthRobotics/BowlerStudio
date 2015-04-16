package com.neuronrobotics.nrconsole.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class Mp3Filter extends FileFilter {
	
	public String getDescription() {
		return "wav Audio File (wav)";
	}
	public boolean accept(File f) {
		if(f.isDirectory()) {
			return true;
		}
		String path = f.getAbsolutePath().toLowerCase();
		if ( (path.endsWith("wav")&& (path.charAt(path.length() - 3)) == '.')) {
			return true;
		}
		return f.getName().matches(".+\\.wav$");
	}

}
