package com.neuronrobotics.nrconsole.plugin.cartesian;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.LinkType;

import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;

public class LinkConfigurationWidget extends GridPane {
	
	private int index;
	private AbstractKinematicsNR congiuration;
	private LinkConfiguration conf;
	public LinkConfigurationWidget(int index, AbstractKinematicsNR congiuration){
		this.index = index;
		this.congiuration = congiuration;
		conf = congiuration.getLinkConfiguration(index);
		getColumnConstraints().add(new ColumnConstraints(130)); // column 1 is 75 wide
	    getColumnConstraints().add(new ColumnConstraints(150)); // column 2 is 300 wide
	    getColumnConstraints().add(new ColumnConstraints(10)); // column 2 is 300 wide
	    setHgap(20);
	    
	    TextField scale = new TextField(DHKinematicsLab.getFormatted(conf.getScale()));
	    scale.setOnAction(event -> {
			conf.setScale(Double.parseDouble(scale.getText()));
		});
	    
	    add(	new Text("Scale To Degrees "), 
	    		0, 
	    		0);
		add(	scale, 
				1, 
				0);
		 add(	new Text("(unitless)"), 
		    		3, 
		    		0);

		
	    TextField lbound = new TextField(DHKinematicsLab.getFormatted(conf.getLowerLimit()));
	    lbound.setOnAction(event -> {
			conf.setLowerLimit(Double.parseDouble(lbound.getText()));
		});
	    
	    TextField ubound = new TextField(DHKinematicsLab.getFormatted(conf.getUpperLimit()));
	    ubound.setOnAction(event -> {
			conf.setUpperLimit(Double.parseDouble(ubound.getText()));
		});
	    
		TextField offset = new TextField(DHKinematicsLab.getFormatted(conf.getStaticOffset()));
			offset.setOnAction(event -> {
			conf.setStaticOffset(Double.parseDouble(offset.getText()));
		});

		final ComboBox<String> comboBox = new ComboBox<String>();
		for (LinkType type : LinkType.values()) {
			comboBox.getItems().add(type.getName());
		}
		comboBox.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				conf.setType(LinkType.fromString(comboBox.getSelectionModel().getSelectedItem()));
				System.out.println("Link type changed to "+conf.getType());
			}
		});
		comboBox.getSelectionModel().select(conf.getType().toString());
	    add(	new Text("Offset"), 
	    		0, 
	    		1);
		add(	offset, 
				1, 
				1);
		 add(	new Text("(device units)"), 
		    		3, 
		    		1);
		 
		 
		 add(	new Text("Upper bound"), 
		    		0, 
		    		2);
		add(	ubound, 
				1, 
				2);
		 add(	new Text("(device units)"), 
			    		3, 
			    		2);
		 
		 add(	new Text("Lower bound"), 
		    		0, 
		    		3);
		add(	lbound, 
				1, 
				3);
		 add(	new Text("(device units)"), 
			    		3, 
			    		3);
		 
		 add(	new Text("Link Type"), 
		    		0, 
		    		4);
		add(	comboBox, 
				1, 
				4);
		 
	}

}
