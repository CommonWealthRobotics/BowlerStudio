package com.neuronrobotics.bowlerstudio.creature;

import java.io.File;
import java.util.ArrayList;

import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;

import eu.mihosoft.vrl.v3d.CSG;

public interface ICadGenerator {
	/**
	 * This function should use the D-H parameters to generate cad objects to build this configuration
	 * the user should attach any listeners from the DH link for simulation
	 * @param dhLinks the list of DH configurations
	 * @return simulatable CAD objects
	 */
	ArrayList<CSG> generateCad(ArrayList<DHLink> dhLinks );
	/**
	 * This function should generate the body and any limbs of a given base. 
	 * the user should attach any listeners from the DH link for simulation
	 * @param base the base to generate
	 * @return simulatable CAD objects
	 */
	ArrayList<CSG> generateBody(MobileBase base );
	/**
	 * This function takes the CAD objects for simulation and generates manufacturable files out of them.
	 * @param base the robot to generate files for
	 * @param baseDirForFiles the directory where the files should be put
	 * @return manufacturable CAD objects
	 */
	ArrayList<File> generateStls(MobileBase base, File baseDirForFiles );
}
