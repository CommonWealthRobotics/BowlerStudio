package com.neuronrobotics.nrconsole.plugin.cartesian;

import java.text.DecimalFormat;

import com.neuronrobotics.sdk.addons.kinematics.AbstractLink;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.common.Log;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class DHLinkWidget extends Group{
	public DHLinkWidget(int linkIndex, DHLink dhLink, DHParameterKinematics device ) {

		HBox panel = new HBox(10);
		AbstractLink abstractLink  = device.getAbstractLink(linkIndex);
		
		
		TextField delta = new TextField(getFormatted(dhLink.getDelta()));
		delta.setOnAction(event -> {
			dhLink.setDelta(Double.parseDouble(delta.getText()));
			device.getCurrentTaskSpaceTransform();
			Log.debug("Setting the setpoint on #"+linkIndex+" to "+delta.getText());
		});
		
		Slider theta = new Slider();
		theta.setMin(-180);
		theta.setMax(180);
		theta.setValue(Math.toDegrees(dhLink.getTheta()));
		theta.setShowTickLabels(true);
		theta.setShowTickMarks(true);
		theta.setMajorTickUnit(50);
		theta.setMinorTickCount(5);
		theta.setBlockIncrement(10);
		//theta.setSnapToTicks(true);
		final Label thetaValue = new Label(getFormatted(theta.getValue()));
		theta.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                    Number old_val, Number new_val) {
            		dhLink.setTheta(Math.toRadians(new_val.doubleValue()));
            		device.getCurrentTaskSpaceTransform();

            		thetaValue.setText(getFormatted(new_val.doubleValue()));
                }
        });
		
		TextField radius = new TextField(getFormatted(dhLink.getRadius()));
		radius.setOnAction(event -> {
			dhLink.setRadius(Double.parseDouble(radius.getText()));
			device.getCurrentTaskSpaceTransform();
			Log.debug("Setting the setpoint on #"+linkIndex+" to "+radius.getText());
		});
		
		Slider Alpha = new Slider();
		Alpha.setMin(-180);
		Alpha.setMax(180);
		Alpha.setValue(Math.toDegrees(dhLink.getAlpha()));
		Alpha.setShowTickLabels(true);
		Alpha.setShowTickMarks(true);
		Alpha.setMajorTickUnit(50);
		Alpha.setMinorTickCount(5);
		Alpha.setBlockIncrement(10);
		//Alpha.setSnapToTicks(true);
		final Label AlphaValue = new Label(getFormatted(Alpha.getValue()));
		Alpha.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                    Number old_val, Number new_val) {
            		dhLink.setAlpha(Math.toRadians(new_val.doubleValue()));
            		device.getCurrentTaskSpaceTransform();
            		AlphaValue.setText(getFormatted(new_val.doubleValue()));
                }
        });
		
		TextField name = new TextField(abstractLink.getLinkConfiguration().getName());
		name.setOnAction(event -> {
			abstractLink.getLinkConfiguration().setName(name.getText());
		});
		
		Slider setpoint = new Slider();
		setpoint.setMin(abstractLink.getMinEngineeringUnits());
		setpoint.setMax(abstractLink.getMaxEngineeringUnits());
		setpoint.setValue(0);
		setpoint.setShowTickLabels(true);
		setpoint.setShowTickMarks(true);
		//setpoint.setSnapToTicks(true);
		setpoint.setMajorTickUnit(50);
		setpoint.setMinorTickCount(5);
		setpoint.setBlockIncrement(10);
		final Label setpointValue = new Label(getFormatted(setpoint.getValue()));
		setpoint.valueProperty().addListener(new ChangeListener<Number>() {

            public void changed(ObservableValue<? extends Number> ov,
                    Number old_val, Number new_val) {
            		try {
            			
        				device.setDesiredJointAxisValue(linkIndex, new_val.doubleValue(), 0);
        				Log.debug("Setting the setpoint on #"+linkIndex+" to "+new_val);
        				setpointValue.setText(getFormatted(new_val.doubleValue()));
        			} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					};
                }
        });
		
		
		
		
		
		
		panel.getChildren().addAll(	new Text("#"+linkIndex),
									name,
									setpoint,
									setpointValue,
									new Text("Delta"),
									delta,
									new Text("Theta"),
									theta,thetaValue,
									new Text("Radius"),
									radius,
									new Text("Alpha"),
									Alpha,AlphaValue
									);
		getChildren().add(panel);
	}
	
	private String getFormatted(double value){
	    return String.format("%4.3f%n", (double)value);
	}
}
