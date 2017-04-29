package com.neuronrobotics.bowlerstudio.creature;

import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;
import com.neuronrobotics.sdk.addons.gamepad.BowlerJInputDevice;
import com.neuronrobotics.sdk.addons.gamepad.IJInputEventListener;
import com.neuronrobotics.sdk.addons.kinematics.*;
import com.neuronrobotics.sdk.pid.PIDLimitEvent;
import com.neuronrobotics.sdk.util.ThreadUtil;
import javafx.scene.Group;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import org.reactfx.util.FxTimer;

import java.time.Duration;

public class LinkSliderWidget extends Group
		implements  IJInputEventListener, IOnEngineeringUnitsChange, ILinkListener {
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
	private AbstractLink abstractLink;
	private EngineeringUnitsSliderWidget slide;

	public LinkSliderWidget(int linkIndex, DHLink dhlink, AbstractKinematicsNR d) {
		this.linkIndex = linkIndex;
		this.device = d;
		if (DHParameterKinematics.class.isInstance(device))
			dhdevice = (DHParameterKinematics) device;

		abstractLink = device.getAbstractLink(linkIndex);

		TextField name = new TextField(abstractLink.getLinkConfiguration().getName());
		name.setMaxWidth(100.0);
		name.setOnAction(event -> abstractLink.getLinkConfiguration().setName(name.getText()));

		setpoint = new EngineeringUnitsSliderWidget(this, 
													abstractLink.getMinEngineeringUnits(),
													abstractLink.getMaxEngineeringUnits(), 
													device.getCurrentJointSpaceVector()[linkIndex],
													180,
													dhlink.getLinkType() == DhLinkType.ROTORY ? "degrees" : "mm");

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

		panel.add(new Text("#" + linkIndex), 0, 0);
		panel.add(name, 1, 0);
		panel.add(setpoint, 2, 0);

		getChildren().add(panel);
		abstractLink.addLinkListener(this);
		//device.addJointSpaceListener(this);

	}

	// public void changed(ObservableValue<? extends Boolean> observableValue,
	// Boolean wasChanging,
	// Boolean changing) {
	//
	// }
//
//	@Override
//	public void onJointSpaceUpdate(AbstractKinematicsNR source, double[] joints) {
//
//		try {
//			setpoint.setValue(joints[linkIndex]);
//		} catch (ArrayIndexOutOfBoundsException ex) {
//			return;
//		}
//		
//
//	}
//
//	@Override
//	public void onJointSpaceTargetUpdate(AbstractKinematicsNR source, double[] joints) {
//		System.out.println("targe update");
//	}
//
//	@Override
//	public void onJointSpaceLimit(AbstractKinematicsNR source, int axis, JointLimit event) {
//		System.out.println("limit update");
//
//	}

	private void controllerLoop() {
		seconds = .1;
		if (getGameController() != null || !stop) {
			if (!stop)
				jogTHreadHandle.setToSet(slider + setpoint.getValue(), seconds);
			FxTimer.runLater(Duration.ofMillis((int) (seconds * 1000.0)), this::controllerLoop);
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
						setpoint.setValue(newValue);
					} catch (Exception e) {
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
		// System.out.println("Stoping on="+comp.getName());
		stop = slider == 0;
	}

	@Override
	public void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
		try {
			device.setDesiredJointAxisValue(linkIndex, setpoint.getValue(), 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onSliderDoneMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
	}

	@Override
	public void onLinkLimit(AbstractLink arg0, PIDLimitEvent arg1) {
	}

	@Override
	public void onLinkPositionUpdate(AbstractLink arg0, double arg1) {
		try {
			setpoint.setValue(arg1);
		} catch (ArrayIndexOutOfBoundsException ignored) {}
	}
}
