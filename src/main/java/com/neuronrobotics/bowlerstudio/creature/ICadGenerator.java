package com.neuronrobotics.bowlerstudio.creature;

import java.io.File;
import java.util.ArrayList;

import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;

import eu.mihosoft.vrl.v3d.CSG;

public interface ICadGenerator {
	ArrayList<CSG> generateCad(ArrayList<DHLink> dhLinks );
	ArrayList<CSG> generateBody(MobileBase base );
	ArrayList<File> generateStls(MobileBase base, File baseDirForFiles );
}
