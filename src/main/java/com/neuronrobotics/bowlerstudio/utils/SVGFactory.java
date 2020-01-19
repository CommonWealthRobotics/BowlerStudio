package com.neuronrobotics.bowlerstudio.utils;

import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.svg.SVGExporter;
import java.io.File;
import java.io.IOException;

@SuppressWarnings("restriction")
public class SVGFactory {

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
    System.out.println("SVG at " + baseDirForFiles);
    return baseDirForFiles.getParentFile();
  }
}
