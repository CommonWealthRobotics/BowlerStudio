package com.neuronrobotics.bowlerstudio.vitamins;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.neuronrobotics.imageprovider.NativeResource;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.STL;

public class Vitamins {
	
	private static final Map<String,CSG> fileLastLoaded = new HashMap<String,CSG>();

	public static CSG get(String resource ){
		if(fileLastLoaded.get(resource) ==null ){
			// forces the first time the files is accessed by the application tou pull an update
			try {
				fileLastLoaded.put(resource,STL.file(NativeResource.inJarLoad(IVitamin.class,resource).toPath()) );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return fileLastLoaded.get(resource).clone() ;
	}
	public static CSG get(File resource ){
		
		if(fileLastLoaded.get(resource.getAbsolutePath()) ==null ){
			// forces the first time the files is accessed by the application tou pull an update
			try {
				fileLastLoaded.put(resource.getAbsolutePath(), STL.file(resource.toPath()) );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return fileLastLoaded.get(resource).clone() ;
	}
}
