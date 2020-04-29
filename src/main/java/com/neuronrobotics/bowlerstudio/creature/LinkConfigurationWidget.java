package com.neuronrobotics.bowlerstudio.creature;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.reactfx.util.FxTimer;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.scripting.PasswordManager;
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
import java.time.Duration;

@SuppressWarnings("restriction")
public class LinkConfigurationWidget extends GridPane {

	// private int index;
	private LinkConfiguration conf;
	private EngineeringUnitsSliderWidget zero;
	private EngineeringUnitsSliderWidget lowerBound;
	private EngineeringUnitsSliderWidget upperBound;
	private AbstractLink activLink;
	private MobileBaseCadManager manager;
	private EngineeringUnitsSliderWidget setpointSLider;
	
	private double zeroValue =0;

	double textToNum(TextField mass) {
		try {
			return Double.parseDouble(mass.getText().trim());
		} catch (Throwable t) {
			mass.setText("0");
			return 0;
		}
	}

	public LinkConfigurationWidget(LinkConfiguration congiuration, LinkFactory factory,
			EngineeringUnitsSliderWidget slide, MobileBaseCadManager manager) {
		// this.index = index;
		// this.congiuration = congiuration;
		conf = congiuration;
		this.setpointSLider = slide;
		this.manager = manager;
		activLink = factory.getLink(conf);
		getColumnConstraints().add(new ColumnConstraints(150)); // column 1 is 75 wide
		getColumnConstraints().add(new ColumnConstraints(200)); // column 2 is 300 wide
		getColumnConstraints().add(new ColumnConstraints(200)); // column 2 is 300 wide
		setHgap(20);

		TextField mass = new TextField(CreatureLab.getFormatted(conf.getMassKg()));
		mass.setOnAction(event -> {
			conf.setMassKg(textToNum(mass));
			activLink.setTargetEngineeringUnits(0);
			activLink.flush(0);
			if (manager != null)
				manager.generateCad();
		});
		TransformNR currentCentroid = conf.getCenterOfMassFromCentroid();
		TextField massx = new TextField(CreatureLab.getFormatted(currentCentroid.getX()));
		massx.setOnAction(event -> {
			currentCentroid.setX(textToNum(massx));
			conf.setCenterOfMassFromCentroid(currentCentroid);
			;
			activLink.setTargetEngineeringUnits(0);
			activLink.flush(0);
			if (manager != null)
				manager.generateCad();

		});

		TextField massy = new TextField(CreatureLab.getFormatted(currentCentroid.getY()));
		massy.setOnAction(event -> {
			currentCentroid.setY(textToNum(massy));
			conf.setCenterOfMassFromCentroid(currentCentroid);
			;
			activLink.setTargetEngineeringUnits(0);
			activLink.flush(0);
			if (manager != null)
				manager.generateCad();

		});

		TextField massz = new TextField(CreatureLab.getFormatted(currentCentroid.getZ()));
		massz.setOnAction(event -> {
			currentCentroid.setZ(textToNum(massz));
			conf.setCenterOfMassFromCentroid(currentCentroid);
			;
			activLink.setTargetEngineeringUnits(0);
			activLink.flush(0);
			if (manager != null)
				manager.generateCad();

		});

		TextField scale = new TextField(CreatureLab.getFormatted(conf.getScale()));
		scale.setOnAction(event -> {
			conf.setScale(textToNum(scale));
			activLink.setTargetEngineeringUnits(0);
			activLink.flush(0);
			if (manager != null)
				manager.generateCad();

		});

		final ComboBox<String> shaftSize = new ComboBox<>();
		final ComboBox<String> shaftType = new ComboBox<>();
		for (String s : Vitamins.listVitaminSizes(conf.getShaftType())) {
			shaftSize.getItems().add(s);
		}
		shaftSize.setOnAction(event -> {
			String motorsize = shaftSize.getSelectionModel().getSelectedItem();
			String motortype = shaftType.getSelectionModel().getSelectedItem();
			if (motorsize == null || motortype == null)
				return;
			conf.setShaftSize(motorsize);
			conf.setShaftType(motortype);
			setShaftSize( motorsize);
			if (manager != null)
				manager.generateCad();

		});
		shaftSize.getSelectionModel().select(conf.getShaftSize());

		for (String vitaminsType : Vitamins.listVitaminTypes()) {
			HashMap<String, Object> meta = Vitamins.getMeta(vitaminsType);
			if (meta != null && meta.containsKey("shaft"))
				shaftType.getItems().add(vitaminsType);
		}

		shaftType.setOnAction(event -> {
			String selectedItem = shaftType.getSelectionModel().getSelectedItem();
			setShaftType( shaftSize, selectedItem);
		});
		shaftType.getSelectionModel().select(conf.getShaftType());
		final ComboBox<String> emHardwareType = new ComboBox<>();
		final ComboBox<String> emHardwareSize = new ComboBox<>();
		for (String s : Vitamins.listVitaminSizes(conf.getElectroMechanicalType())) {
			emHardwareSize.getItems().add(s);
		}
		emHardwareSize.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				String motortype = emHardwareType.getSelectionModel().getSelectedItem();
				String motorsize = emHardwareSize.getSelectionModel().getSelectedItem();
				if (motorsize == null || motortype == null)
					return;
				conf.setElectroMechanicalType(motortype);
				conf.setElectroMechanicalSize(motorsize);
				//newHardware.setText("New " + conf.getElectroMechanicalType());
				//editHardware.setText("Edit " + conf.getElectroMechanicalSize());
				HashMap<String, Object> vitaminData = Vitamins.getConfiguration(conf.getElectroMechanicalType(),
						conf.getElectroMechanicalSize());
				System.out.println("New size " + vitaminData);
				String shafttype = (String) vitaminData.get("shaftType");
				String shaftsize = (String) vitaminData.get("shaftSize");

				Platform.runLater(() -> {
					setShaftType( shaftSize, shafttype);
					FxTimer.runLater(Duration.ofMillis(20), () -> {
						setShaftSize( shaftsize);
						FxTimer.runLater(Duration.ofMillis(200), new Runnable() {
							@Override
							public void run() {

								System.out.println("Settting shaft size: " + shaftsize + " of " + shafttype);

								Platform.runLater(() -> shaftType.getSelectionModel().select(shafttype));
								Platform.runLater(() -> shaftSize.getSelectionModel().select(shaftsize));
							}
						});
					});
				});
				conf.setShaftSize(shaftsize);
				conf.setShaftType(shafttype);
				if (manager != null)
					manager.generateCad();

			}
		});
		emHardwareSize.getSelectionModel().select(conf.getElectroMechanicalSize());

		for (String vitaminsType : Vitamins.listVitaminTypes()) {
			HashMap<String, Object> meta = Vitamins.getMeta(vitaminsType);
			if (meta != null && meta.containsKey("actuator"))
				emHardwareType.getItems().add(vitaminsType);
		}
		emHardwareType.setOnAction(event -> {
			String selectedItem = emHardwareType.getSelectionModel().getSelectedItem();
			if (selectedItem == null)
				return;
			System.out.println("New hwType " + selectedItem);

			emHardwareSize.getItems().clear();
			for (String s : Vitamins.listVitaminSizes(selectedItem)) {
				emHardwareSize.getItems().add(s);
			}
			//newHardware.setText("New " + conf.getElectroMechanicalType());

		});
		emHardwareType.getSelectionModel().select(conf.getElectroMechanicalType());


		TextField deviceName = new TextField(congiuration.getDeviceScriptingName());
		deviceName.setOnAction(event -> {
			conf.setDeviceScriptingName(deviceName.getText());
			factory.refreshHardwareLayer(conf);
			activLink = factory.getLink(conf);
			System.out.println("Link device to " + conf.getDeviceScriptingName());
			if (manager != null)
				manager.generateCad();

		});

		add(new Text("Scale To Degrees "), 0, 0);
		add(scale, 1, 0);
		add(new Text("(unitless)"), 2, 0);

		double min = activLink.getDeviceMinimumValue();
		lowerBound = new EngineeringUnitsSliderWidget(new IOnEngineeringUnitsChange() {

			@Override
			public void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
				double eng = setLowerBound(newAngleDegrees);
				activLink.setUseLimits(false);
				activLink.setTargetEngineeringUnits(eng);
				activLink.flush(0);
				activLink.setUseLimits(true);
			}

			@Override
			public void onSliderDoneMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
				try {
					activLink.setUseLimits(false);
					activLink.setTargetEngineeringUnits(setLowerBound(newAngleDegrees) + 0.01);
					activLink.flush(0);
					activLink.setUseLimits(true);
					if (manager != null)
						manager.generateCad();
					zero.setLowerBound(newAngleDegrees);

				} catch (Exception ex) {
					BowlerStudio.printStackTrace(ex);
				}
			}
		}, min, // min
				conf.getStaticOffset(), // max
				conf.getLowerLimit(), // current
				150, "device units", true);

		double max = activLink.getDeviceMaximumValue();
		upperBound = new EngineeringUnitsSliderWidget(new IOnEngineeringUnitsChange() {

			@Override
			public void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
				double eng = setUpperBound(newAngleDegrees - 0.00001);
				activLink.setUseLimits(false);
				activLink.setTargetEngineeringUnits(eng);
				activLink.flush(0);
				activLink.setUseLimits(true);
			}

			@Override
			public void onSliderDoneMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
				activLink.setUseLimits(false);
				activLink.setTargetEngineeringUnits(setUpperBound(newAngleDegrees) - 0.00001);
				activLink.flush(0);
				activLink.setUseLimits(true);
				zero.setUpperBound(newAngleDegrees);
				if (manager != null)
					manager.generateCad();

			}
		}, conf.getStaticOffset(), max, conf.getUpperLimit(), 150,
				"device units", true);
		
		zeroValue = conf.getStaticOffset();
		
		zero = new EngineeringUnitsSliderWidget(new IOnEngineeringUnitsChange() {

			@Override
			public void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
				conf.setStaticOffset(newAngleDegrees);

				activLink.setTargetEngineeringUnits(0);
				activLink.flush(0);
			}

			@Override
			public void onSliderDoneMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
				double diff = zeroValue-newAngleDegrees;
				zeroValue=newAngleDegrees;
				setLowerBound(conf.getLowerLimit()-diff);
				setUpperBound(conf.getUpperLimit()-diff);
				setpointSLider.setValue(0);
				activLink.setTargetEngineeringUnits(0);
				activLink.flush(0);
				upperBound.setLowerBound(newAngleDegrees);
				lowerBound.setUpperBound(newAngleDegrees);
				if (manager != null)
					manager.generateCad();

			}
		}, conf.getLowerLimit(), conf.getUpperLimit(), conf.getStaticOffset(), 150, "device units", true);
		zero.setAllowResize(false);
		upperBound.setAllowResize(false);
		lowerBound.setAllowResize(false);
		final ComboBox<String> channel = new ComboBox<>();
		for (int i = 0; i < 24; i++) {
			channel.getItems().add(Integer.toString(i));
		}
		channel.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				conf.setHardwareIndex(Integer.parseInt(channel.getSelectionModel().getSelectedItem()));
				factory.refreshHardwareLayer(conf);
				activLink = factory.getLink(conf);
				System.out.println("Link channel to " + conf.getTypeString());
				if (manager != null)
					manager.generateCad();

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
				System.out.println("Link type changed to " + conf.getTypeString());
				if (manager != null)
					manager.generateCad();

			}
		});
		comboBox.getSelectionModel().select(conf.getTypeString().toString());

		add(new Text("Zero Degrees Value"), 0, 1);
		add(zero, 1, 1);

		add(new Text("Upper bound"), 0, 2);
		add(upperBound, 1, 2);

		add(new Text("Lower bound"), 0, 3);
		add(lowerBound, 1, 3);

		add(new Text("Link Type"), 0, 4);
		add(comboBox, 1, 4);
		add(new Text("Link Hardware Index"), 0, 5);
		add(channel, 1, 5);

		add(new Text("Device Scripting Name"), 0, 6);
		add(deviceName, 1, 6);

		add(new Text("Mass"), 0, 7);
		add(mass, 1, 7);

		add(new Text("Mass Centroid x"), 0, 8);
		add(massx, 1, 8);

		add(new Text("Mass Centroid y"), 0, 9);
		add(massy, 1, 9);
		add(new Text("Mass Centroid z"), 0, 10);
		add(massz, 1, 10);
		// link hardware
		add(new Text("Hardware Type"), 0, 11);
		add(emHardwareType, 1, 11);
		add(new Text("Hardware Size"), 0, 12);
		add(emHardwareSize, 1, 12);
		//add(newHardware, 1, 13);

		// link shaft
		add(new Text("Shaft Type"), 0, 14);
		add(shaftType, 1, 14);
		add(new Text("Shaft Size"), 0, 15);
		add(shaftSize, 1, 15);
//		add(newShaft, 1, 16);

	}

	private double setUpperBound(double newAngleDegrees) {
		
		double upperLimit = newAngleDegrees<=activLink.getDeviceMaximumValue()?newAngleDegrees:activLink.getDeviceMaximumValue();
		conf.setUpperLimit(upperLimit);
		upperBound.setValue(upperLimit);
		double eng = 0;
		if (conf.getScale() < 0) {
			eng = (activLink.getMinEngineeringUnits());
			setpointSLider.setLowerBound(eng);
		} else {
			eng = (activLink.getMaxEngineeringUnits());
			setpointSLider.setUpperBound(eng);
		}

		return eng;
	}

	private double setLowerBound(double newAngleDegrees) {
		double lowerLimit = newAngleDegrees>=activLink.getDeviceMinimumValue()?newAngleDegrees:activLink.getDeviceMinimumValue();
		conf.setLowerLimit(lowerLimit);
		lowerBound.setValue(lowerLimit);

		double eng = 0;
		if (conf.getScale() > 0) {
			eng = (activLink.getMinEngineeringUnits());
			setpointSLider.setLowerBound(eng);
		} else {
			eng = (activLink.getMaxEngineeringUnits());
			setpointSLider.setUpperBound(eng);
		}

		return eng;

	}

	private void setShaftSize( String selectedItem) {
		if (selectedItem == null) {
			return;
		}
	}

	private void setShaftType(  final ComboBox<String> shaftSize,
			String selectedItem) {
		shaftSize.getItems().clear();
		if (selectedItem == null)
			return;
		for (String s : Vitamins.listVitaminSizes(selectedItem)) {
			shaftSize.getItems().add(s);
		}
		// editShaft.setText("Edit "+ conf.getShaftSize());
	}

//	private void test(String type) throws IOException {
//		try {
//			Vitamins.saveDatabase(type);
//
//		} catch (org.eclipse.jgit.api.errors.TransportException e) {
//			GitHub github = PasswordManager.getGithub();
//
//			GHRepository repo = github.getUser("madhephaestus").getRepository("Hardware-Dimensions");
//			GHRepository forked = repo.fork();
//			System.out.println("Vitamins forked to " + forked.getGitTransportUrl());
//			Vitamins.setGitRepoDatabase(
//					"https://github.com/" + github.getMyself().getLogin() + "/Hardware-Dimensions.git");
//			System.out.println("Loading new files");
//			//
//
//		} catch (Exception ex) {
//			// ex.printStackTrace(MainController.getOut());
//		}
//	}



}
