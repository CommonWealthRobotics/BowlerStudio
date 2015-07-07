package com.neuronrobotics.nrconsole.plugin.cartesian;

import java.util.ArrayList;

import com.neuronrobotics.sdk.addons.kinematics.DHLink;

import eu.mihosoft.vrl.v3d.CSG;

public interface ICadGenerator {
	ArrayList<CSG> generateCad(ArrayList<DHLink> dhLinks );
}
