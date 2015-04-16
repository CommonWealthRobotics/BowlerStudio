package com.neuronrobotics.nrconsole.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class XmlFilter extends FileFilter {

	public String getDescription() {
		return "Configuration (xml)";
	}
	
	public boolean accept(File f) {
		if(f.isDirectory()) {
			return true;
		}
		String path = f.getAbsolutePath().toLowerCase();
		if ((path.endsWith("xml") && (path.charAt(path.length() - 3)) == '.')) {
			return true;
		}
		return f.getName().matches(".+\\.xml$");
	}

}
