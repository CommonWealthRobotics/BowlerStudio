package com.neuronrobotics.bowlerstudio.creature;

import java.time.Duration;

import org.reactfx.util.FxTimer;

import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;
import com.neuronrobotics.sdk.addons.gamepad.BowlerJInputDevice;
import com.neuronrobotics.sdk.addons.gamepad.IJInputEventListener;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.AbstractLink;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.DhLinkType;
import com.neuronrobotics.sdk.addons.kinematics.IJointSpaceUpdateListenerNR;
import com.neuronrobotics.sdk.addons.kinematics.ILinkConfigurationChangeListener;
import com.neuronrobotics.sdk.addons.kinematics.ILinkListener;
import com.neuronrobotics.sdk.addons.kinematics.JointLimit;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.pid.PIDLimitEvent;
import com.neuronrobotics.sdk.util.ThreadUtil;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.Gauge.SkinType;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.medusa.LcdDesign;
import eu.hansolo.medusa.LcdFont;
import eu.hansolo.medusa.TickLabelLocation;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Event;

@SuppressWarnings("restriction")
public class LinkSliderWidget extends Group implements IJInputEventListener, IOnEngineeringUnitsChange, ILinkListener, ILinkConfigurationChangeListener {
	private AbstractKinematicsNR device;
	private DHParameterKinematics dhdevice;

	private int linkIndex;
	private EngineeringUnitsSliderWidget setpoint;
	private BowlerJInputDevice controller;
	private jogThread jogTHreadHandle;
	private double slider;
	private boolean stop;
	private double seconds;
	private String paramsKey;
	private ITrimControl trimController=null;
	// private EngineeringUnitsSliderWidget slide;
	private Button jogplus= new Button("+");
	private Button jogminus= new Button("-");
	private LinkConfiguration conf;
	private Gauge gauge;
	
	
	public LinkSliderWidget(int linkIndex, DHParameterKinematics d) {

		this.linkIndex = linkIndex;
		this.device = d;
		this.conf = d.getLinkConfiguration(linkIndex);
		conf.addChangeListener(this);
		if (DHParameterKinematics.class.isInstance(device)) {
			dhdevice = (DHParameterKinematics) device;
		}

		TextField name = new TextField(getAbstractLink().getLinkConfiguration().getName());
		name.setMaxWidth(100.0);
		name.setOnAction(event -> {
			getAbstractLink().getLinkConfiguration().setName(name.getText());
		});

		setSetpoint(new EngineeringUnitsSliderWidget(this, getAbstractLink().getMinEngineeringUnits(),
				getAbstractLink().getMaxEngineeringUnits(), device.getCurrentJointSpaceVector()[linkIndex], 180,
				d.getDhChain().getLinks().get(linkIndex).getLinkType() == DhLinkType.ROTORY ? "degrees" : "mm"));

		GridPane panel = new GridPane();

		panel.getColumnConstraints().add(new ColumnConstraints(30)); // column 1
																		// is 75
																		// wide
		panel.getColumnConstraints().add(new ColumnConstraints(120)); // column
																		// 1 is
																		// 75
																		// wide
		panel.getColumnConstraints().add(new ColumnConstraints(120)); // column
																		// 2 is
																		// 300
																		// wide
		jogminus.setOnAction(event->{
			getTrimController().trimMinus();
		});
		jogplus.setOnAction(event->{
			getTrimController().trimPlus();
		});
		HBox trimBox = new HBox();
		trimBox.getChildren().add(new Label("Trim"));
		trimBox.getChildren().add(jogminus);
		trimBox.getChildren().add(jogplus);
		panel.add(new Text("#" + linkIndex), 0, 0);
		panel.add(name, 1, 0);
		panel.add(getSetpoint(), 2, 0);
		panel.add(trimBox, 2, 1);
		
		VBox allParts = new VBox();
		allParts.getChildren().add(panel);
		
		gauge = GaugeBuilder.create().skinType(SkinType.GAUGE).animated(false)
				.decimals(2)
				.thresholdVisible(true)
				.lcdVisible(true)
				.lcdDesign(LcdDesign.STANDARD) 
                .lcdFont(LcdFont.DIGITAL_BOLD)  
                .tickLabelDecimals(1) 
                .tickLabelLocation(TickLabelLocation.INSIDE)                                     // Should tick labels be inside or outside Scale (INSIDE, OUTSIDE)
                .tickLabelSectionsVisible(true)
                .tickMarkSectionsVisible(true)
		        .title("Link Bounds").unit("degrees").build();
		event(conf);
		allParts.getChildren().add(gauge);
		getChildren().add(allParts);
		getAbstractLink().addLinkListener(this);
		// device.addJointSpaceListener(this);

	}
	@Override
	public void event(LinkConfiguration newConf) {
		double rANGE = getAbstractLink().getMaxEngineeringUnits()-getAbstractLink().getMinEngineeringUnits();
	
		gauge.setMaxValue(getAbstractLink().getDeviceMaxEngineeringUnits());
		gauge.setMinValue(getAbstractLink().getDeviceMinEngineeringUnits());
		gauge.setStartAngle(getAbstractLink().getMinEngineeringUnits()) ;                                                                // Start angle of Scale (bottom -> 0, direction -> CCW)
		gauge.setAngleRange(rANGE) ;
		gauge.setTitle("Link range = "+rANGE);
		getSetpoint().setLowerBound(getAbstractLink().getMinEngineeringUnits());
		getSetpoint().setUpperBound(getAbstractLink().getMaxEngineeringUnits());

	}
	public void setUpperBound(double newBound) {
		getSetpoint().setUpperBound(newBound);
	}

	public void setLowerBound(double newBound) {
		getSetpoint().setLowerBound(newBound);
	}

	private void controllerLoop() {
		seconds = .1;
		if (getGameController() != null || stop == false) {

			if (!stop) {
				jogTHreadHandle.setToSet(slider + getSetpoint().getValue(), seconds);
			}

			FxTimer.runLater(Duration.ofMillis((int) (seconds * 1000.0)), new Runnable() {
				@Override
				public void run() {
					controllerLoop();
				}
			});
		}
	}

	private class jogThread extends Thread {
		private boolean controlThreadRunning = false;

		private double toSeconds = seconds;

		private double newValue;

		public void run() {
			setName("Jog Link Slider");
			while (device.isAvailable()) {
				if (controlThreadRunning) {
					try {
						device.setDesiredJointAxisValue(linkIndex, newValue, toSeconds);
						getSetpoint().setValue(newValue);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					controlThreadRunning = false;
				}
				ThreadUtil.wait((int) (toSeconds * 1000));
			}
		}

		public void setToSet(double newValue, double toSeconds) {

			this.newValue = newValue;
			this.toSeconds = toSeconds;
			controlThreadRunning = true;
		}

	}

	public void setGameController(BowlerJInputDevice controller) {
		this.controller = controller;
		if (controller != null && jogTHreadHandle == null) {
			jogTHreadHandle = new jogThread();
			jogTHreadHandle.start();
		}

		if (controller != null) {
			Controller hwController = controller.getController();
			paramsKey = hwController.getName();
			System.err.println("Controller key: " + paramsKey);
			getGameController().clearListeners();
			getGameController().addListeners(this);
			controllerLoop();
		}
	}

	public BowlerJInputDevice getGameController() {
		return controller;
	}

	@Override
	public void onEvent(Component comp, net.java.games.input.Event event, float value, String eventString) {

		if (comp.getName().toLowerCase()
				.contentEquals((String) ConfigurationDatabase.getObject(paramsKey, "jogLink", "x")))
			slider = -value;

		if (Math.abs(slider) < .01)
			slider = 0;
		if (slider == 0) {
			// System.out.println("Stoping on="+comp.getName());
			stop = true;
		} else
			stop = false;
	}

	@Override
	public void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
		// TODO Auto-generated method stub
		try {
				if (newAngleDegrees > device.getAbstractLink(linkIndex).getMaxEngineeringUnits()) {
					newAngleDegrees=device.getAbstractLink(linkIndex).getMaxEngineeringUnits();
				}
				if(newAngleDegrees <device.getAbstractLink(linkIndex).getMinEngineeringUnits()) {
					newAngleDegrees=device.getAbstractLink(linkIndex).getMinEngineeringUnits();
				}
				device.setDesiredJointAxisValue(linkIndex, newAngleDegrees, 0);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		;

	}

	@Override
	public void onSliderDoneMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {

	}

	@Override
	public void onLinkLimit(AbstractLink arg0, PIDLimitEvent arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLinkPositionUpdate(AbstractLink arg0, double arg1) {
		// TODO Auto-generated method stub
		try {
			getSetpoint().setValue(arg1);
			gauge.setValue(arg1);
		} catch (Exception ex) {
			return;
		}
	}

	public EngineeringUnitsSliderWidget getSetpoint() {
		return setpoint;
	}

	public void setSetpoint(EngineeringUnitsSliderWidget setpoint) {
		this.setpoint = setpoint;
	}

	public ITrimControl getTrimController() {
			return trimController;
	}

	public void setTrimController(ITrimControl trimController) {
		this.trimController = trimController;
	}



	public AbstractLink getAbstractLink() {
		return device.getAbstractLink(linkIndex);
	}

}
