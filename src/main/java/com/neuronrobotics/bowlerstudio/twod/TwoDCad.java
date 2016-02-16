package com.neuronrobotics.bowlerstudio.twod;

import java.util.ArrayList;

import eu.mihosoft.vrl.v3d.Polygon;
import javafx.scene.Group;
import javafx.scene.control.Tab;
import javafx.scene.text.Text;

public class TwoDCad extends Tab {
	
	public TwoDCad(ArrayList<Polygon> points){
		setText("2D CAD workspace");
		
		setContent(new Group(new Text("Hello world")));
	}

}
