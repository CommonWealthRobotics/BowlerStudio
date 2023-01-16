package com.neuronrobotics.bowlerkernel.Bezier3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cylinder;
import eu.mihosoft.vrl.v3d.Vector3d;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;

public class CartesianManipulator{
	public Affine manipulationMatrix= new Affine();
	CSG manip1 = new Cylinder(0,5,40,10).toCSG()
	.setColor(Color.BLUE);
	CSG manip2 = new Cylinder(0,5,40,10).toCSG()
	.roty(-90)
	.setColor(Color.RED);
	CSG manip3 = new Cylinder(0,5,40,10).toCSG()
	.rotx(90)
	.setColor(Color.GREEN);
	public CartesianManipulator(TransformNR globalPose,Runnable ev,Runnable moving) {
		new manipulation( manipulationMatrix, new Vector3d(0,0,1), manip1, globalPose,ev,moving);
		new manipulation( manipulationMatrix, new Vector3d(0,1,0), manip3, globalPose,ev,moving);
		new manipulation( manipulationMatrix, new Vector3d(1,0,0), manip2, globalPose,ev,moving);
	}
	public List<CSG> get(){
		return Arrays.asList(manip1, manip2, manip3);
	}
}
