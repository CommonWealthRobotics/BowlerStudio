package com.neuronrobotics.bowlerstudio.utils;

import java.io.File;

import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.svg.SVGExporter;

@SuppressWarnings("restriction")
public class SVGFactory {

	public static File exportSVG(CSG currentCsg, File defaultDir) {
		com.neuronrobotics.sdk.common.Log.error("Starting SVG ...");

		File baseDirForFiles = FileSelectionFactory.GetFile(defaultDir, true);

		if (!baseDirForFiles.getAbsolutePath().toLowerCase().endsWith(".svg"))
			baseDirForFiles = new File(baseDirForFiles.getAbsolutePath() + ".svg");
		try {
			SVGExporter.export(currentCsg, baseDirForFiles);
		} catch (Exception e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		com.neuronrobotics.sdk.common.Log.error("SVG at " + baseDirForFiles);
		return baseDirForFiles.getParentFile();

	}
}
