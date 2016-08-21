package com.neuronrobotics.bowlerstudio.creature;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.AbstractLink;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.LinkFactory;
import com.neuronrobotics.sdk.addons.kinematics.LinkType;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
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
	    getColumnConstraints().add(new ColumnConstraints(200)); // column 2 is 300 wide
	    setHgap(20);
	    
	    TextField mass = new TextField(CreatureLab.getFormatted(conf.getMassKg()));
	    mass.setOnAction(event -> {
			conf.setMassKg(Double.parseDouble(mass.getText()));
			activLink.setTargetEngineeringUnits(0);
			activLink.flush(0);
		});
	    TransformNR currentCentroid =conf.getCenterOfMassFromCentroid();
	    TextField massx = new TextField(CreatureLab.getFormatted(currentCentroid.getX()));
	    massx.setOnAction(event -> {
	    	currentCentroid.setX(Double.parseDouble(massx.getText()));
			conf.setCenterOfMassFromCentroid(currentCentroid);;
			activLink.setTargetEngineeringUnits(0);
			activLink.flush(0);
		});
	    
	    TextField massy = new TextField(CreatureLab.getFormatted(currentCentroid.getY()));
	    massy.setOnAction(event -> {
	    	currentCentroid.setY(Double.parseDouble(massy.getText()));
			conf.setCenterOfMassFromCentroid(currentCentroid);;
			activLink.setTargetEngineeringUnits(0);
			activLink.flush(0);
		});
	    
	    TextField massz = new TextField(CreatureLab.getFormatted(currentCentroid.getZ()));
	    massz.setOnAction(event -> {
	    	currentCentroid.setZ(Double.parseDouble(massz.getText()));
			conf.setCenterOfMassFromCentroid(currentCentroid);;
			activLink.setTargetEngineeringUnits(0);
			activLink.flush(0);
		});
	    
	    TextField scale = new TextField(CreatureLab.getFormatted(conf.getScale()));
	    scale.setOnAction(event -> {
			conf.setScale(Double.parseDouble(scale.getText()));
			activLink.setTargetEngineeringUnits(0);
			activLink.flush(0);
		});
		Button editShaft = new Button("Edit " + conf.getShaftSize());
		editShaft.setOnAction(event -> {
			new Thread() {
				public void run() {
					try {
						String type =conf.getShaftType();
						String id = conf.getShaftSize();
						edit(type,
								id,
								Vitamins.getConfiguration(type,id));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}.start();

		});
	    Button newShaft = new Button("New "+ conf.getShaftType());
	    newShaft.setOnAction(event -> {
	    	TextInputDialog d = new TextInputDialog("NewSize");
	    	d.setTitle("Wizard for new "+conf.getShaftType());
	    	d.setHeaderText("Enter th Side ID for a new "+conf.getShaftType());
	    	d.setContentText("Size:");

	    	// Traditional way to get the response value.
	    	Optional<String> result = d.showAndWait();
	    	if (result.isPresent()){
	    		// Create the custom dialog.
	    		String id = result.get();
	    		String type =conf.getShaftType();
	
				new Thread() {
					public void run() {
						
						try {
							test( type);
							Vitamins.newVitamin(id, type);
							edit(type, id,Vitamins.getConfiguration(type,conf.getShaftSize()));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}.start();
				

	    	}
		});
	    
		final ComboBox<String> shaftSize = new ComboBox<>();
		for (String s : Vitamins.listVitaminSizes( conf.getShaftType())) {
			shaftSize.getItems().add(s);
		}
		shaftSize.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				conf.setShaftSize(shaftSize.getSelectionModel().getSelectedItem());
				newShaft.setText("New "+ conf.getShaftType());
				editShaft.setText("Edit "+ conf.getShaftSize());
			}
		});
		shaftSize.getSelectionModel().select(conf.getShaftSize());

		final ComboBox<String> shaftType = new ComboBox<>();
		
		for(String vitaminsType: Vitamins.listVitaminTypes()){
			HashMap<String, Object> meta = Vitamins.getMeta(vitaminsType);
			if(meta!=null && meta.containsKey("shaft"))
				shaftType.getItems().add(vitaminsType);
		}

		shaftType.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				conf.setShaftType(shaftType.getSelectionModel().getSelectedItem());
				shaftSize.getItems().clear();
				for (String s : Vitamins.listVitaminSizes( conf.getShaftType())) {
					shaftSize.getItems().add(s);
				}
				newShaft.setText("New "+ conf.getShaftType());
				editShaft.setText("Edit "+ conf.getShaftSize());
			}
		});
		shaftType.getSelectionModel().select(conf.getShaftType());
		
		// Actuator editing
		
		Button editHardware = new Button("Edit " + conf.getElectroMechanicalSize());
		editHardware.setOnAction(event -> {
			new Thread() {
				public void run() {
					try {
						String type =conf.getElectroMechanicalType();
						String id = conf.getElectroMechanicalSize();
						edit(type,
								id,
								Vitamins.getConfiguration(type,id));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}.start();

		});
	    Button newHardware = new Button("New "+ conf.getElectroMechanicalType());
	    newHardware.setOnAction(event -> {
	    	TextInputDialog d = new TextInputDialog("NewSize");
	    	d.setTitle("Wizard for new "+conf.getElectroMechanicalType());
	    	d.setHeaderText("Enter th Side ID for a new "+conf.getElectroMechanicalType());
	    	d.setContentText("Size:");

	    	// Traditional way to get the response value.
	    	Optional<String> result = d.showAndWait();
	    	if (result.isPresent()){
	    		// Create the custom dialog.
	    		String id = result.get();
	    		String type =conf.getElectroMechanicalType();
	
				new Thread() {
					public void run() {
						
						try {
							test( type);
							Vitamins.newVitamin(id, type);
							edit(type, id,Vitamins.getConfiguration(type,conf.getElectroMechanicalSize()));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}.start();
				

	    	}
		});
	    
		final ComboBox<String> emHardwareSize = new ComboBox<>();
		for (String s : Vitamins.listVitaminSizes( conf.getElectroMechanicalType())) {
			emHardwareSize.getItems().add(s);
		}
		emHardwareSize.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				conf.setElectroMechanicalSize(emHardwareSize.getSelectionModel().getSelectedItem());
				newHardware.setText("New "+ conf.getElectroMechanicalType());
				editHardware.setText("Edit "+ conf.getElectroMechanicalSize());
			}
		});
		emHardwareSize.getSelectionModel().select(conf.getElectroMechanicalSize());

		final ComboBox<String> emHardwareType = new ComboBox<>();
		for(String vitaminsType: Vitamins.listVitaminTypes()){
			HashMap<String, Object> meta = Vitamins.getMeta(vitaminsType);
			if(meta !=null && meta.containsKey("actuator"))
				emHardwareType.getItems().add(vitaminsType);
		}
		emHardwareType.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				conf.setElectroMechanicalType(emHardwareType.getSelectionModel().getSelectedItem());
				emHardwareSize.getItems().clear();
				for (String s : Vitamins.listVitaminSizes( conf.getElectroMechanicalType())) {
					emHardwareSize.getItems().add(s);
				}
				newHardware.setText("New "+ conf.getElectroMechanicalType());
				editHardware.setText("Edit "+ conf.getElectroMechanicalSize());
			}
		});
		emHardwareType.getSelectionModel().select(conf.getElectroMechanicalType());



	    TextField deviceName = new TextField(congiuration.getDeviceScriptingName());
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
		    		2, 
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

		final ComboBox<String> channel = new ComboBox<>();
		for (int i=0;i<24;i++) {
			channel.getItems().add(Integer.toString(i));
		}
		channel.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				conf.setHardwareIndex(Integer.parseInt(channel.getSelectionModel().getSelectedItem()));
				factory.refreshHardwareLayer(conf);
				activLink = factory.getLink(conf);
				System.out.println("Link channel to "+conf.getTypeString());
			}
		});
		channel.getSelectionModel().select(conf.getHardwareIndex());
		
		final ComboBox<String> comboBox = new ComboBox<>();
		for (LinkType type : LinkType.values()) {
			comboBox.getItems().add(type.getName());
		}
		comboBox.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				conf.setType(LinkType.fromString(comboBox.getSelectionModel().getSelectedItem()));
				System.out.println("Link type changed to "+conf.getTypeString());
			}
		});
		comboBox.getSelectionModel().select(conf.getTypeString().toString());
		
		
		
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
		
		add(	new Text("Mass"), 
	    		0, 
	    		7);
		add(	mass, 
				1, 
				7);
		
		add(	new Text("Mass Centroid x"), 
	    		0, 
	    		8);
		add(	massx, 
				1, 
				8);
		
		add(	new Text("Mass Centroid y"), 
	    		0, 
	    		9);
		add(	massy, 
				1, 
				9);
		add(	new Text("Mass Centroid z"), 
	    		0, 
	    		10);
		add(	massz, 
				1, 
				10);
		// link hardware
		add(	new Text("Hardware Type"), 
	    		0, 
	    		11);
		add(	emHardwareType, 
				1, 
				11);
		add(	new Text("Hardware Size"), 
	    		0, 
	    		12);
		add(	emHardwareSize, 
				1, 
				12);
		add(	editHardware, 
	    		2, 
	    		12);
		add(	newHardware, 
	    		1, 
	    		13);
		
		// link shaft
		add(	new Text("Shaft Type"), 
	    		0, 
	    		14);
		add(	shaftType, 
				1, 
				14);
		add(	new Text("Shaft Size"), 
	    		0, 
	    		15);
		add(	shaftSize, 
				1, 
				15);
		add(	editShaft, 
	    		2, 
	    		15);
		add(	newShaft, 
	    		1, 
	    		16);

	
	}
	
	private void test(String type) throws IOException{
		try {
			Vitamins.saveDatabase(type);
			
		} catch (org.eclipse.jgit.api.errors.TransportException e) {
			GitHub github = ScriptingEngine.getGithub();
	
			GHRepository repo = github.getUser("madhephaestus").getRepository("Hardware-Dimensions");
			GHRepository forked =repo.fork();
			System.out.println("Vitamins forked to "+forked.getGitTransportUrl());
			Vitamins.setGitRpoDatabase("https://github.com/"+github.getMyself().getLogin()+"/Hardware-Dimensions.git");
			System.out.println("Loading new files");
			//

		}catch (Exception ex){
			//ex.printStackTrace(MainController.getOut());
		}
	}
	
	private void edit(String type, String id,HashMap<String, Object> startingConf) throws Exception{
		System.out.println("Configuration for "+conf.getElectroMechanicalSize());
		System.out.println("Saving to for "+id);
		test( type);
		Platform.runLater(()->{
			Alert dialog = new Alert(AlertType.CONFIRMATION);
			dialog.setTitle("Edit Hardware Wizard");
			dialog.setHeaderText("Update the hardare configurations");

			// Create the username and password labels and fields.
			GridPane grid = new GridPane();
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(20, 150, 10, 10));
			
			
			HashMap<String,TextField> valueFields=new HashMap<> ();
			
			int row=0;
			for(Map.Entry<String, Object> entry : startingConf.entrySet()){
				TextField username = new TextField();
	    		username.setText(entry.getValue().toString());
	    		grid.add(new Label(entry.getKey()), 0, row);
	    		grid.add(username, 1, row);
	    		valueFields.put(entry.getKey(), username);
	    		row++;
			}


			dialog.getDialogPane().setContent(grid);
			Optional<ButtonType> r = dialog.showAndWait();
			if(r.get() == ButtonType.OK){
				new Thread(){
						public void run(){
				    		for(Map.Entry<String, TextField> entry : valueFields.entrySet()){
				    			try {
									Vitamins.setParameter(type,id, entry.getKey(),(Object) entry.getValue().getText());
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
				    		}
				    		try {
								Vitamins.saveDatabase(type);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
				    		
						}
				}.start();
			}
		});
		
	}

}
