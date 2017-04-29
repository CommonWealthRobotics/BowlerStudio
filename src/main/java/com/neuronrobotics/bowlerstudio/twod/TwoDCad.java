package com.neuronrobotics.bowlerstudio.twod;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import eu.mihosoft.vrl.v3d.Polygon;
import javafx.scene.Group;
import javafx.scene.control.Tab;
import javafx.scene.text.Text;

import java.util.ArrayList;

public class TwoDCad extends Tab {
	public TwoDCad(ArrayList<Polygon> points){
		setText("2D CAD workspace");
		// add all UI code here
		setContent(new Group(new Text("Hello world")));
		setGraphic(AssetFactory.loadIcon("2d-Cad-Workspace-Tab.png"));
	}
}
