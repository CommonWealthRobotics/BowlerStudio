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
import eu.hansolo.medusa.Gauge.KnobType;
import eu.hansolo.medusa.Gauge.NeedleShape;
import eu.hansolo.medusa.Gauge.SkinType;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.medusa.LcdDesign;
import eu.hansolo.medusa.LcdFont;
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.TickLabelLocation;
import eu.hansolo.medusa.TickLabelOrientation;
import eu.hansolo.medusa.TickMarkType;
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
import javafx.scene.paint.Color;
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
	private Section bounds;
	private Section boundsPossible;
	private LinkConfigurationWidget theWidget;
	private TextField engineeringUpper=new TextField("0");
	private TextField engineeringLower=new TextField("0");
	
	private Label engineeringUpperPossible=new Label("0");
	private Label engineeringLowerPossible=new Label("0");
	
	public LinkSliderWidget(int linkIndex, DHParameterKinematics d, LinkConfigurationWidget theWidget) {
		this.theWidget = theWidget;
		setTrimController(theWidget);
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
		double spread = 0;
		bounds = new Section(0, 0, Color.rgb(60, 130, 145, 0.7));
		boundsPossible = new Section(0, 0, Color.ORANGE);
		gauge=GaugeBuilder.create()
        .foregroundBaseColor(Color.BLACK)
        .prefSize(200,200)
        .startAngle(360-(spread/2))
        .angleRange(360-spread)
        .minValue(-180+(spread/2))
        .maxValue(180-(spread/2))
        .tickLabelLocation(TickLabelLocation.OUTSIDE)
        .tickLabelOrientation(TickLabelOrientation.ORTHOGONAL)
        .minorTickMarksVisible(false)
        .majorTickMarkType(TickMarkType.BOX)
        .valueVisible(true)
        .knobType(KnobType.FLAT)
        .needleShape(NeedleShape.FLAT)
        .needleColor(Color.RED)
        .sectionsVisible(true)
        .sections(boundsPossible,bounds)
        .tickLabelsVisible(false)        
        .decimals(2)  
        .build();
		event(conf);
		
		engineeringUpper.setOnAction(event->{
			try {
				double num = Double.parseDouble(engineeringUpper.getText());
				if(num>getAbstractLink().getDeviceMaxEngineeringUnits()) {
					throw new RuntimeException();
				}
				double linkUnits = getAbstractLink().toLinkUnits(num);
				if(conf.getScale()>0)
					theWidget.setUpperBound(linkUnits);
				else
					theWidget.setLowerBound(linkUnits);
			}catch(Exception e){
				Platform.runLater(()-> engineeringUpper.setText(String.format("%.2f",getAbstractLink().getMaxEngineeringUnits())));
			}
		});
		engineeringLower.setOnAction(event->{
			try {
				double num = Double.parseDouble(engineeringLower.getText());
				if(num<getAbstractLink().getDeviceMinEngineeringUnits()) {
					throw new RuntimeException();
				}
				double linkUnits = getAbstractLink().toLinkUnits(num);
				if(conf.getScale()<0)
					theWidget.setUpperBound(linkUnits);
				else
					theWidget.setLowerBound(linkUnits);
			}catch(Exception e){
				Platform.runLater(()-> engineeringLower.setText(String.format("%.2f",getAbstractLink().getMinEngineeringUnits())));
			}
		});
		
		HBox upperLimBox1 = new HBox();
		HBox lowerLimBox1 = new HBox();
		VBox limits1 = new VBox();
		engineeringUpper.setPrefWidth(80);
		engineeringLower.setPrefWidth(80);
		upperLimBox1.getChildren().addAll(new Label("Upper: "),engineeringUpperPossible);
		lowerLimBox1.getChildren().addAll(new Label("Lower: "),engineeringLowerPossible);
		limits1.getChildren().addAll(new Label("Range"),upperLimBox1,lowerLimBox1);
		
		HBox trimBox = new HBox();
		HBox upperLimBox = new HBox();
		HBox lowerLimBox = new HBox();
		VBox limits = new VBox();
		engineeringUpper.setPrefWidth(80);
		engineeringLower.setPrefWidth(80);
		upperLimBox.getChildren().addAll(new Label("Upper: "),engineeringUpper);
		lowerLimBox.getChildren().addAll(new Label("Lower: "),engineeringLower);
		limits.getChildren().addAll(new Label("Limits"),upperLimBox,lowerLimBox);
		
		trimBox.getChildren().add(new Label("Trim"));
		trimBox.getChildren().add(jogminus);
		trimBox.getChildren().add(jogplus);
		panel.setHgap(5);
		panel.setVgap(5);
		panel.add(new Text("#" + linkIndex), 0, 0);
		panel.add(name, 1, 0);
		panel.add(getSetpoint(), 2, 0);
		
		
		GridPane calibration = new GridPane();
		calibration.setHgap(5);
		calibration.setVgap(5);
		calibration.getColumnConstraints().add(new ColumnConstraints(180));
		calibration.getColumnConstraints().add(new ColumnConstraints(100));
		calibration.getColumnConstraints().add(new ColumnConstraints(120)); 
		calibration.getRowConstraints().add(new RowConstraints(80)); 
		calibration.getRowConstraints().add(new RowConstraints(150)); 

		calibration.add(trimBox, 1, 1);
		calibration.add(limits, 0, 0);
		calibration.add(limits1, 1, 0);
		calibration.add(gauge, 0, 1);
		
		VBox allParts = new VBox();
		allParts.getChildren().addAll(panel,calibration,theWidget);
		getChildren().add(allParts);
		getAbstractLink().addLinkListener(this);
		// device.addJointSpaceListener(this);

	}
	@Override
	public void event(LinkConfiguration newConf) {
		double rANGE = getAbstractLink().getMaxEngineeringUnits()-getAbstractLink().getMinEngineeringUnits();
		double theoreticalRange = getAbstractLink().getDeviceMaxEngineeringUnits()-getAbstractLink().getDeviceMinEngineeringUnits();
		Platform.runLater(()-> {
			engineeringUpper.setText(String.format("%.2f",getAbstractLink().getMaxEngineeringUnits()));
			engineeringLower.setText(String.format("%.2f",getAbstractLink().getMinEngineeringUnits()));
			engineeringUpperPossible.setText(String.format("%.2f",getAbstractLink().getDeviceMaxEngineeringUnits()));
			engineeringLowerPossible.setText(String.format("%.2f",getAbstractLink().getDeviceMinEngineeringUnits()));
			bounds.setStart(getAbstractLink().getMinEngineeringUnits());
			bounds.setStop(getAbstractLink().getMaxEngineeringUnits());
			boundsPossible.setStart(getAbstractLink().getDeviceMinEngineeringUnits());
			boundsPossible.setStop(getAbstractLink().getDeviceMaxEngineeringUnits());
//			gauge.setMaxValue(getAbstractLink().getDeviceMaxEngineeringUnits());
//			gauge.setMinValue(getAbstractLink().getDeviceMinEngineeringUnits());
			gauge.setTitle("Link Range "+String.format("%.2f", rANGE)+"\nOf Possible "+String.format("%.2f", theoreticalRange));
		});
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
			Platform.runLater(()->gauge.setValue(arg1));
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
