package com.neuronrobotics.bowlerstudio.creature;

import javafx.application.Platform;
import javafx.scene.control.Accordion;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;

import com.neuronrobotics.bowlerstudio.IssueReportingExceptionHandler;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;


public class DhSettingsWidget extends javafx.scene.Group implements IOnEngineeringUnitsChange {

	private DHLink dhLink;
	private EngineeringUnitsSliderWidget delta;
	private EngineeringUnitsSliderWidget theta;
	private EngineeringUnitsSliderWidget alpha;
	private EngineeringUnitsSliderWidget radius;
	private DHParameterKinematics dh;
	private IOnEngineeringUnitsChange externalListener;
	
	public DhSettingsWidget(DHLink dhLink,DHParameterKinematics device2,IOnEngineeringUnitsChange externalListener ){
		this.dhLink = dhLink;
		this.dh = device2;
	
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
				0,
				200,
				dhLink.getRadius(),
				180," mm ");
		
		alpha = new EngineeringUnitsSliderWidget(this,
														-180,
														180,
														Math.toDegrees(dhLink.getAlpha()),
														180,"degrees");
		delta.showSlider(false);
		radius.showSlider(false);
		alpha.showSlider(false);
		GridPane gridpane = new GridPane();
		gridpane.getColumnConstraints().add(new ColumnConstraints(120)); // column 1 is 75 wide
	    gridpane.getColumnConstraints().add(new ColumnConstraints(320)); // column 2 is 300 wide
	    gridpane.getColumnConstraints().add(new ColumnConstraints(100)); // column 2 is 100 wide
	    gridpane.getRowConstraints().add(new RowConstraints(70)); // 
	    gridpane.getRowConstraints().add(new RowConstraints(70)); // 
	    gridpane.getRowConstraints().add(new RowConstraints(70)); // 
	    gridpane.getRowConstraints().add(new RowConstraints(70)); // 
		gridpane.add(new Text("Height (D value)"), 0, 0);
		gridpane.add(delta, 1, 0);
		gridpane.add(new Text("Length (A value)"), 0, 1);
		gridpane.add(radius, 1, 1);

		gridpane.getColumnConstraints().add(new ColumnConstraints(120)); // column 1 is 75 wide
	    gridpane.getColumnConstraints().add(new ColumnConstraints(320)); // column 2 is 300 wide
	    gridpane.getColumnConstraints().add(new ColumnConstraints(100)); // column 2 is 100 wide
	    gridpane.getRowConstraints().add(new RowConstraints(70)); // 
	    gridpane.getRowConstraints().add(new RowConstraints(70)); // 
		gridpane.add(new Text("Theta"), 0, 2);
		gridpane.add(theta, 1, 2);
		gridpane.add(new Text("Alpha"), 0, 3);
		gridpane.add(alpha, 1, 3);
		
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
//		//this calls the render update function attachec as the on jointspace update	
//		double[] joint=device2.getCurrentJointSpaceVector();
//		device2.getChain().getChain(joint);
//		Platform.runLater(()->device2.onJointSpaceUpdate(device2, joint));
		if(dh.checkTaskSpaceTransform(dh.getCurrentPoseTarget())) {
			try {
				dh.setDesiredTaskSpaceTransform(dh.getCurrentPoseTarget(), 0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				new IssueReportingExceptionHandler().uncaughtException(Thread.currentThread(), e);
				
			}
		}else {
			dh.getCurrentTaskSpaceTransform();
			// this calls the render update function attachec as the on jointspace
			// update
			double[] joint = dh.getCurrentJointSpaceVector();
			dh.getChain().getChain(joint);
			Platform.runLater(() -> dh.onJointSpaceUpdate(dh, joint));
		}
	}

	@Override
	public void onSliderDoneMoving(EngineeringUnitsSliderWidget source,
			double newAngleDegrees) {
		onSliderMoving(source,newAngleDegrees);
		if(externalListener!=null)
			externalListener.onSliderDoneMoving(source, newAngleDegrees);
	}
}
