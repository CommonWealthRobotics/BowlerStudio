package com.neuronrobotics.nrconsole.plugin.cartesian;

import java.time.Duration;

import org.reactfx.util.FxTimer;

import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.AbstractLink;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.IJointSpaceUpdateListenerNR;
import com.neuronrobotics.sdk.addons.kinematics.JointLimit;
import com.neuronrobotics.sdk.common.Log;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;

public class DHLinkWidget extends Group implements  IJointSpaceUpdateListenerNR {
	private DHParameterKinematics device;
	private int linkIndex;
	private Label setpointValue;
	private AngleSliderWidget setpoint;
	private Button del;
	public DHLinkWidget(int linkIndex, DHLink dhLink, DHParameterKinematics device, Button del ) {

		this.linkIndex = linkIndex;
		this.device = device;
		this.del = del;
		AbstractLink abstractLink  = device.getAbstractLink(linkIndex);
		
		
		TextField delta = new TextField(getFormatted(dhLink.getDelta()));
		delta.setOnAction(event -> {
			dhLink.setDelta(Double.parseDouble(delta.getText()));
			device.getCurrentTaskSpaceTransform();
			Log.debug("Setting the setpoint on #"+linkIndex+" to "+delta.getText());
		});
		
		AngleSliderWidget theta = new AngleSliderWidget(null,
				-180,
				180,
				Math.toDegrees(dhLink.getTheta()),
				180);
		
		TextField radius = new TextField(getFormatted(dhLink.getRadius()));
		radius.setOnAction(event -> {
			dhLink.setRadius(Double.parseDouble(radius.getText()));
			device.getCurrentTaskSpaceTransform();
			Log.debug("Setting the setpoint on #"+linkIndex+" to "+radius.getText());
		});
		
		AngleSliderWidget Alpha = new AngleSliderWidget(null,
														-180,
														180,
														Math.toDegrees(dhLink.getAlpha()),
														180);
		
		
		TextField name = new TextField(abstractLink.getLinkConfiguration().getName());
		name.setMaxWidth(100.0);
		name.setOnAction(event -> {
			abstractLink.getLinkConfiguration().setName(name.getText());
		});
		
		setpoint = new AngleSliderWidget(new IOnAngleChange() {
			
			@Override
			public void onSliderMoving(AngleSliderWidget source, double newAngleDegrees) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onSliderDoneMoving(AngleSliderWidget source,
					double newAngleDegrees) {
	    		try {
					device.setDesiredJointAxisValue(linkIndex, setpoint.getValue(), 2);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				};
			}
		}, 
		abstractLink.getMinEngineeringUnits(), 
		abstractLink.getMaxEngineeringUnits(), 
		device.getCurrentJointSpaceVector()[linkIndex], 
		180);
		
		
		
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
		gridpane.add(new Text("mm"), 2, 0);
		gridpane.add(new Text("Theta"), 0, 1);
		gridpane.add(theta, 1, 1);
		gridpane.add(new Text("Radius"), 0, 2);
		gridpane.add(radius, 1, 2);
		gridpane.add(new Text("mm"), 2, 2);
		gridpane.add(new Text("Alpha"), 0, 3);
		gridpane.add(Alpha, 1, 3);
		accordion.getPanes().add(new TitledPane("Configure D-H", gridpane));
		accordion.getPanes().add(new TitledPane("Configure Link", new LinkConfigurationWidget(linkIndex, device)));
		
		GridPane panel = new GridPane();
		
		panel.getColumnConstraints().add(new ColumnConstraints(80)); // column 1 is 75 wide
		panel.getColumnConstraints().add(new ColumnConstraints(30)); // column 1 is 75 wide
		panel.getColumnConstraints().add(new ColumnConstraints(120)); // column 2 is 300 wide
		panel.getColumnConstraints().add(new ColumnConstraints(320)); // column 2 is 100 wide
		
		
		panel.add(	del, 
				0, 
				0);
		panel.add(	new Text("#"+linkIndex), 
				1, 
				0);
		panel.add(	name, 
				2, 
				0);
		panel.add(	setpoint, 
				3, 
				0);
		panel.add(	accordion, 
				4, 
				0);

		getChildren().add(panel);
	}
	
	public static String getFormatted(double value){
	    return String.format("%4.3f%n", (double)value);
	}
	public void changed(ObservableValue<? extends Boolean> observableValue,
            Boolean wasChanging,
            Boolean changing) {

        }

	@Override
	public void onJointSpaceUpdate(AbstractKinematicsNR source, double[] joints) {
		Platform.runLater(()->{
			try{
				setpoint.setValue(joints[linkIndex]);
			}catch(ArrayIndexOutOfBoundsException ex){
				return;
			}
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
