package com.neuronrobotics.bowlerstudio.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Slice;
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.svg.SVGExporter;

@SuppressWarnings("restriction")
public class SVGFactory{


	public static File exportSVG(CSG currentCsg, File defaultDir) {
		System.out.println("Starting SVG ...");

		File baseDirForFiles = FileSelectionFactory.GetFile(defaultDir, true);

		if (!baseDirForFiles.getAbsolutePath().toLowerCase().endsWith(".svg"))
			baseDirForFiles = new File(baseDirForFiles.getAbsolutePath() + ".svg");
		try {
			SVGExporter.export(currentCsg, baseDirForFiles);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("SVG at "+baseDirForFiles);
		return baseDirForFiles.getParentFile();

	}
}
