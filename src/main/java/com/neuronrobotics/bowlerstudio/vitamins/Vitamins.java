package com.neuronrobotics.bowlerstudio.vitamins;

import java.io.File;
import java.io.IOException;

import com.neuronrobotics.imageprovider.NativeResource;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.STL;

public class Vitamins {
	public static CSG get(String resource ){
			try {
				return  STL.file(NativeResource.inJarLoad(IVitamin.class,resource).toPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return null;
	}
	public static CSG get(File resource ){
		try {
			return  STL.file(resource.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	return null;
	}
}
