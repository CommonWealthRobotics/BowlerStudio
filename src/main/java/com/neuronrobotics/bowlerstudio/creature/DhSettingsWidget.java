package com.neuronrobotics.bowlerstudio.creature;

import javafx.application.Platform;
import javafx.scene.control.Accordion;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;

import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;


public class DhSettingsWidget extends javafx.scene.Group implements IOnEngineeringUnitsChange {

	private DHLink dhLink;
	private EngineeringUnitsSliderWidget delta;
	private EngineeringUnitsSliderWidget theta;
	private EngineeringUnitsSliderWidget alpha;
	private EngineeringUnitsSliderWidget radius;
	private DHParameterKinematics device2;
	private IOnEngineeringUnitsChange externalListener;
	
	public DhSettingsWidget(DHLink dhLink,DHParameterKinematics device2,IOnEngineeringUnitsChange externalListener ){
		this.dhLink = dhLink;
		this.device2 = device2;
	
		this.externalListener = externalListener;
		
		delta= new EngineeringUnitsSliderWidget(this,
				0,
				200,
				dhLink.getDelta(),
				180," mm ");
		
		theta = new EngineeringUnitsSliderWidget(this,
				-180,
				180,
				Math.toDegrees(dhLink.getTheta()),
				180,"degrees");
		

		
		radius= new EngineeringUnitsSliderWidget(this,
				60,
				200,
				dhLink.getRadius(),
				180," mm ");
		
		alpha = new EngineeringUnitsSliderWidget(this,
														-180,
														180,
														Math.toDegrees(dhLink.getAlpha()),
														180,"degrees");
		
		GridPane gridpane = new GridPane();
		gridpane.getColumnConstraints().add(new ColumnConstraints(120)); // column 1 is 75 wide
	    gridpane.getColumnConstraints().add(new ColumnConstraints(320)); // column 2 is 300 wide
	    gridpane.getColumnConstraints().add(new ColumnConstraints(100)); // column 2 is 100 wide
	    gridpane.getRowConstraints().add(new RowConstraints(50)); // 
	    gridpane.getRowConstraints().add(new RowConstraints(50)); // 
	    gridpane.getRowConstraints().add(new RowConstraints(50)); // 
	    gridpane.getRowConstraints().add(new RowConstraints(50)); // 
		gridpane.add(new Text("Delta (Height)"), 0, 0);
		gridpane.add(delta, 1, 0);
		gridpane.add(new Text("Radius (Length)"), 0, 1);
		gridpane.add(radius, 1, 1);
		Accordion advancedPanel = new Accordion();
		GridPane agridpane = new GridPane();
		agridpane.getColumnConstraints().add(new ColumnConstraints(120)); // column 1 is 75 wide
	    agridpane.getColumnConstraints().add(new ColumnConstraints(320)); // column 2 is 300 wide
	    agridpane.getColumnConstraints().add(new ColumnConstraints(100)); // column 2 is 100 wide
	    agridpane.getRowConstraints().add(new RowConstraints(50)); // 
	    agridpane.getRowConstraints().add(new RowConstraints(50)); // 
		agridpane.add(new Text("Theta"), 0, 0);
		agridpane.add(theta, 1, 0);
		agridpane.add(new Text("Alpha"), 0, 1);
		agridpane.add(alpha, 1, 1);
		
		TitledPane rp =new TitledPane("Advance D-H ", agridpane);
		advancedPanel.getPanes().add(rp);
		
		gridpane.add(advancedPanel, 1, 2);
		
		getChildren().add(gridpane);
	}
	

	@Override
	public void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
		dhLink.setTheta(Math.toRadians(theta.getValue()));
		dhLink.setAlpha(Math.toRadians(alpha.getValue()));
		dhLink.setRadius(radius.getValue());
		dhLink.setDelta(delta.getValue());

		if(externalListener!=null)
			externalListener.onSliderMoving(source, newAngleDegrees);
		//this calls the render update function attachec as the on jointspace update	
		double[] joint=device2.getCurrentJointSpaceVector();
		device2.getChain().getChain(joint);
		Platform.runLater(()->device2.onJointSpaceUpdate(device2, joint));
	}

	@Override
	public void onSliderDoneMoving(EngineeringUnitsSliderWidget source,
			double newAngleDegrees) {
		// TODO Auto-generated method stub
		if(externalListener!=null)
			externalListener.onSliderDoneMoving(source, newAngleDegrees);
	}
}
