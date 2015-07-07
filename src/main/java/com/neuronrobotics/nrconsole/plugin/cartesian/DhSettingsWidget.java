package com.neuronrobotics.nrconsole.plugin.cartesian;

import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;

import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;


public class DhSettingsWidget extends javafx.scene.Group implements IOnAngleChange {

	private DHLink dhLink;
	private TextField delta;
	private AngleSliderWidget theta;
	private AngleSliderWidget alpha;
	private TextField radius;
	private DHParameterKinematics device2;
	public DhSettingsWidget(DHLink dhLink,DHParameterKinematics device2){
		this.dhLink = dhLink;
		this.device2 = device2;
		delta = new TextField(DHKinematicsLab.getFormatted(dhLink.getDelta()));
		delta.setOnAction(event -> {
			dhLink.setDelta(Double.parseDouble(delta.getText()));
			device2.getCurrentTaskSpaceTransform();
		});
		
		theta = new AngleSliderWidget(this,
				-180,
				180,
				Math.toDegrees(dhLink.getTheta()),
				180);
		
		radius = new TextField(DHKinematicsLab.getFormatted(dhLink.getRadius()));
		radius.setOnAction(event -> {
			dhLink.setRadius(Double.parseDouble(radius.getText()));
			device2.getCurrentTaskSpaceTransform();
		});
		
		alpha = new AngleSliderWidget(this,
														-180,
														180,
														Math.toDegrees(dhLink.getAlpha()),
														180);
		
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
		gridpane.add(alpha, 1, 3);
		
		getChildren().add(gridpane);
	}
	

	@Override
	public void onSliderMoving(AngleSliderWidget source, double newAngleDegrees) {
		dhLink.setTheta(Math.toRadians(theta.getValue()));
		dhLink.setAlpha(Math.toRadians(alpha.getValue()));
		device2.getCurrentTaskSpaceTransform();

	}

	@Override
	public void onSliderDoneMoving(AngleSliderWidget source,
			double newAngleDegrees) {
		// TODO Auto-generated method stub
		
	}
}
