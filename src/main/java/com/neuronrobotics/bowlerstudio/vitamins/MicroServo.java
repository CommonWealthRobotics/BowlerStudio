package com.neuronrobotics.bowlerstudio.vitamins;

import java.io.File;
import java.io.IOException;

import com.neuronrobotics.bowlerstudio.creature.CreatureLab;
import com.neuronrobotics.jniloader.NativeResource;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.STL;
import eu.mihosoft.vrl.v3d.Transform;

public class MicroServo implements IVitamin {
	
	private static CSG servoModel;

	static{
		
		try {
			File stl = NativeResource.inJarLoad(IVitamin.class.getResourceAsStream("hxt900-servo.stl"),"hxt900-servo.stl");
			servoModel = STL.file(stl.toPath());
			servoModel=servoModel.transformed(new Transform().translateZ(-19.3));
			servoModel=servoModel.transformed(new Transform().translateX(5.4));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public CSG toCSG() {
		return servoModel.clone();
	}

}
