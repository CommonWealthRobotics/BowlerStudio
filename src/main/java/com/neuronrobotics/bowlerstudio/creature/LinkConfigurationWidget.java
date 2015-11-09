package com.neuronrobotics.bowlerstudio.creature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.AbstractLink;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.LinkFactory;
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
	
//	private int index;
	private AbstractKinematicsNR congiuration;
	private LinkConfiguration conf;
	private EngineeringUnitsSliderWidget zero;
	private EngineeringUnitsSliderWidget lowerBound;
	private EngineeringUnitsSliderWidget upperBound;
	private AbstractLink activLink;
	public LinkConfigurationWidget(LinkConfiguration congiuration, LinkFactory factory){
//		this.index = index;
//		this.congiuration = congiuration;
		conf = congiuration;
		activLink = factory.getLink(conf);
		getColumnConstraints().add(new ColumnConstraints(150)); // column 1 is 75 wide
	    getColumnConstraints().add(new ColumnConstraints(200)); // column 2 is 300 wide
	    getColumnConstraints().add(new ColumnConstraints(10)); // column 2 is 300 wide
	    setHgap(20);
	    
	    TextField scale = new TextField(CreatureLab.getFormatted(conf.getScale()));
	    scale.setOnAction(event -> {
			conf.setScale(Double.parseDouble(scale.getText()));
			activLink.setTargetEngineeringUnits(0);
			activLink.flush(0);
		});
	    
	    TextField deviceName = new TextField(CreatureLab.getFormatted(conf.getScale()));
	    deviceName.setOnAction(event -> {
			conf.setDeviceScriptingName(deviceName.getText());
			factory.refreshHardwareLayer(conf);
			activLink = factory.getLink(conf);
			System.out.println("Link device to "+conf.getDeviceScriptingName());
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


		lowerBound=new EngineeringUnitsSliderWidget(new IOnEngineeringUnitsChange() {
			
			@Override
			public void onSliderMoving(EngineeringUnitsSliderWidget source,
					double newAngleDegrees) {
				conf.setLowerLimit(newAngleDegrees);
				if(conf.getScale()>0)
					activLink.setTargetEngineeringUnits(activLink.getMinEngineeringUnits());
				else
					activLink.setTargetEngineeringUnits(activLink.getMaxEngineeringUnits());
				activLink.flush(0);
			}
			
			@Override
			public void onSliderDoneMoving(EngineeringUnitsSliderWidget source,
					double newAngleDegrees) {
				activLink.setTargetEngineeringUnits(0);
				activLink.flush(0);
			}
		}, 0, 255, conf.getLowerLimit(), 150, "device units", true);

		 upperBound=new EngineeringUnitsSliderWidget(new IOnEngineeringUnitsChange() {
				
				@Override
				public void onSliderMoving(EngineeringUnitsSliderWidget source,
						double newAngleDegrees) {
					conf.setUpperLimit(newAngleDegrees);
					if(conf.getScale()>0)
						activLink.setTargetEngineeringUnits(activLink.getMaxEngineeringUnits());
					else
						activLink.setTargetEngineeringUnits(activLink.getMinEngineeringUnits());
					activLink.flush(0);
				}
				
				@Override
				public void onSliderDoneMoving(EngineeringUnitsSliderWidget source,
						double newAngleDegrees) {
					activLink.setTargetEngineeringUnits(0);
					activLink.flush(0);
				}
			}, 0, 255, conf.getUpperLimit(), 150, "device units", true);

	    zero= new EngineeringUnitsSliderWidget(new IOnEngineeringUnitsChange() {
			
			@Override
			public void onSliderMoving(EngineeringUnitsSliderWidget source,
					double newAngleDegrees) {
				conf.setStaticOffset(newAngleDegrees);
				
				activLink.setTargetEngineeringUnits(0);
				activLink.flush(0);
			}
			
			@Override
			public void onSliderDoneMoving(EngineeringUnitsSliderWidget source,
					double newAngleDegrees) {
				// TODO Auto-generated method stub
				
			}
		}, 
		conf.getLowerLimit(), conf.getUpperLimit(), conf.getStaticOffset(), 150, "device units", true);

		final ComboBox<String> channel = new ComboBox<String>();
		for (int i=0;i<24;i++) {
			channel.getItems().add(new Integer(i).toString());
		}
		channel.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				conf.setHardwareIndex(Integer.parseInt(channel.getSelectionModel().getSelectedItem()));
				factory.refreshHardwareLayer(conf);
				activLink = factory.getLink(conf);
				System.out.println("Link channel to "+conf.getType());
			}
		});
		channel.getSelectionModel().select(conf.getHardwareIndex());
		
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
		
	    add(	new Text("Zero Degrees Value"), 
	    		0, 
	    		1);
		add(	zero, 
				1, 
				1);

		 
		 
		 add(	new Text("Upper bound"), 
		    		0, 
		    		2);
		add(	upperBound, 
				1, 
				2);

		 
		 add(	new Text("Lower bound"), 
		    		0, 
		    		3);
		add(	lowerBound, 
				1, 
				3);

		 
		 add(	new Text("Link Type"), 
		    		0, 
		    		4);
		add(	comboBox, 
				1, 
				4);
		 add(	new Text("Link Hardware Index"), 
		    		0, 
		    		5);
		add(	channel, 
				1, 
				5);
		
		add(	new Text("Device Scripting Name"), 
	    		0, 
	    		6);
		add(	deviceName, 
				1, 
				6);
		 
	}

}
