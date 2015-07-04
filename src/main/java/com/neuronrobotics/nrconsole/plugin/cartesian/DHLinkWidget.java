package com.neuronrobotics.nrconsole.plugin.cartesian;

import java.text.DecimalFormat;
import java.time.Duration;

import org.reactfx.util.FxTimer;

import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.AbstractLink;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.IJointSpaceUpdateListenerNR;
import com.neuronrobotics.sdk.addons.kinematics.JointLimit;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.common.Log;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;

public class DHLinkWidget extends Group implements ChangeListener<Boolean>, IJointSpaceUpdateListenerNR {
	private DHParameterKinematics device;
	private int linkIndex;
	private Label setpointValue;
	private Slider setpoint;
	public DHLinkWidget(int linkIndex, DHLink dhLink, DHParameterKinematics device ) {

		this.linkIndex = linkIndex;
		this.device = device;
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
		theta.setMaxWidth(300);
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
		Alpha.setMaxWidth(300);
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
		
		setpoint = new Slider();
		setpoint.setMin(abstractLink.getMinEngineeringUnits());
		setpoint.setMax(abstractLink.getMaxEngineeringUnits());
		setpoint.setValue(0);
		setpoint.setShowTickLabels(true);
		setpoint.setShowTickMarks(true);
		//setpoint.setSnapToTicks(true);
		setpoint.setMajorTickUnit(50);
		setpoint.setMinorTickCount(5);
		setpoint.setBlockIncrement(10);
		setpointValue = new Label(getFormatted(setpoint.getValue()));
		
		setpoint.valueChangingProperty().addListener(this);
		setpoint.valueProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				setpointValue.setText(getFormatted(newValue.doubleValue()));
				
			}

		});
		
		
		
		final Accordion accordion = new Accordion();
		GridPane gridpane = new GridPane();
		gridpane.getColumnConstraints().add(new ColumnConstraints(75)); // column 1 is 75 wide
	    gridpane.getColumnConstraints().add(new ColumnConstraints(320)); // column 2 is 300 wide
	    gridpane.getColumnConstraints().add(new ColumnConstraints(100)); // column 2 is 100 wide
	    gridpane.getRowConstraints().add(new RowConstraints(50)); // 
	    gridpane.getRowConstraints().add(new RowConstraints(50)); // 
	    gridpane.getRowConstraints().add(new RowConstraints(50)); // 
	    gridpane.getRowConstraints().add(new RowConstraints(50)); // 
		gridpane.add(new Text("Delta"), 0, 0);
		gridpane.add(delta, 1, 0);
		gridpane.add(new Text("Theta"), 0, 1);
		gridpane.add(theta, 1, 1);
		gridpane.add(thetaValue, 2, 1);
		gridpane.add(new Text("Radius"), 0, 2);
		gridpane.add(radius, 1, 2);
		gridpane.add(new Text("Alpha"), 0, 3);
		gridpane.add(Alpha, 1, 3);
		gridpane.add(AlphaValue, 2, 3);
		accordion.getPanes().add(new TitledPane("Configure D-H", gridpane));
		accordion.getPanes().add(new TitledPane("Configure Link", new LinkConfigurationWidget(linkIndex, device)));
		
		panel.getChildren().addAll(	new Text("#"+linkIndex),
									name,
									setpoint,
									setpointValue,
									accordion
									);
		getChildren().add(panel);
		device.addJointSpaceListener(this);
	}
	
	public static String getFormatted(double value){
	    return String.format("%4.3f%n", (double)value);
	}
	public void changed(ObservableValue<? extends Boolean> observableValue,
            Boolean wasChanging,
            Boolean changing) {
    		try {
				device.setDesiredJointAxisValue(linkIndex, setpoint.getValue(), 0);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			};
        }

	@Override
	public void onJointSpaceUpdate(AbstractKinematicsNR source, double[] joints) {
		Platform.runLater(()->{
			setpoint.valueChangingProperty().removeListener(this);
			setpoint.setValue(joints[linkIndex]);
			FxTimer.runLater(
					Duration.ofMillis(10) ,() -> {
						setpoint.valueChangingProperty().addListener(this);
			});
		});
		
		
	}

	@Override
	public void onJointSpaceTargetUpdate(AbstractKinematicsNR source,
			double[] joints) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onJointSpaceLimit(AbstractKinematicsNR source, int axis,
			JointLimit event) {
		// TODO Auto-generated method stub
		
	}
}
