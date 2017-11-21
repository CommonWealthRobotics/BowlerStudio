package com.neuronrobotics.bowlerstudio.threed;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.neuronrobotics.bowlerstudio.BowlerStudioController;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.svg.SVGExporter;

public class CadFileExporter {
	
	public static ArrayList<File> generateManufacturingParts(List<CSG> totalAssembly , File baseDirForFiles) throws IOException {
		ArrayList<File> allCadStl = new ArrayList<>();
		if(!baseDirForFiles.isDirectory()){
			String fileNameWithOutExt = FilenameUtils.removeExtension(baseDirForFiles.getAbsolutePath());
			baseDirForFiles = new File(fileNameWithOutExt);
			if (!baseDirForFiles.exists())
				baseDirForFiles.mkdirs();
		}
		File dir = new File(baseDirForFiles.getAbsolutePath() + "/manufacturing/");
		if (!dir.exists())
			dir.mkdirs();
		
		int index=0;
		ArrayList<CSG> svgParts = new ArrayList<>();
		String svgName =null;
		for(CSG part: totalAssembly){
			String name = part.toString();
			String nameBase = dir.getAbsolutePath()+"/"+index+"-"+name;
			if(part.getExportFormats()==null){
				makeStl(nameBase,part);// default to stl
			}else{
				CSG manufactured = part.prepForManufacturing();
				manufactured.setName(part.getName());
				for(String format:part.getExportFormats()){
					
					if(format.toLowerCase().contains("stl")){
						allCadStl.add(makeStl(nameBase,manufactured));// default to stl
						BowlerStudioController.setCsg(manufactured , null);
					}
					if(format.toLowerCase().contains("svg")){
						if(svgName==null){
							svgName =part.toString();
						}
						svgParts.add(manufactured);
						BowlerStudioController.setCsg(svgParts , null);
					}
				}

			}
		}
		if(svgParts.size()>0){
			allCadStl.add(makeSvg(svgName,svgParts));// default to stl
		}
		return allCadStl;
	}
	private static File makeStl(String nameBase,CSG tmp ) throws IOException{
		File stl = new File(nameBase + ".stl");
		FileUtil.write(Paths.get(stl.getAbsolutePath()), tmp.toStlString());
		return stl;
	}
	private static  File makeSvg(String nameBase,List<CSG> currentCsg ) throws IOException{
		File stl = new File(nameBase + ".svg");
		SVGExporter.export(currentCsg, stl);
		return stl;
	}
	
}
