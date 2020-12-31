package com.neuronrobotics.bowlerstudio.scripting;

import java.io.File;

import com.neuronrobotics.video.OSUtil;

public class SVGExternalEditor implements IExternalEditor {

	@Override
	public boolean isSupportedByExtention(File file) {
		if(SvgLoader.class.isInstance(ScriptingEngine.getLangaugeByExtention(file.getAbsolutePath()))){
			return true;
		}
		return false;
	}

	@Override
	public void launch(File file) {
		String filename = file.getAbsolutePath();
		if(OSUtil.isWindows()) {
			filename="\""+filename+"\"";
		}
		run("inkscape "+filename);
	}
	
	@Override
	public String nameOfEditor() {
		return "Inkscape Vector Editor";
	}

}
