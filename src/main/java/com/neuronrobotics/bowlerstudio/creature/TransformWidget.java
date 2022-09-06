package com.neuronrobotics.bowlerstudio.creature;

import Jama.Matrix;

import com.neuronrobotics.bowlerstudio.threed.BowlerStudio3dEngine;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.Log;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

public class TransformWidget extends GridPane implements IOnEngineeringUnitsChange, EventHandler<ActionEvent> {
	
	private IOnTransformChange onChange;
	//EngineeringUnitsSliderWidget rw;
	private EngineeringUnitsSliderWidget tilt;
	private EngineeringUnitsSliderWidget elevation;
	private EngineeringUnitsSliderWidget azimeth;
	private EngineeringUnitsSliderWidget tx;
	private EngineeringUnitsSliderWidget ty;
	private EngineeringUnitsSliderWidget tz;
//	private TextField tx;
//	private TextField ty;
//	private TextField tz;
	private TransformNR initialState;
	private RotationNR storeRotation;
	private boolean isNowVis=false;
	public TransformWidget(String title, TransformNR is, IOnTransformChange onChange){
		this.initialState = is;
		this.onChange = onChange;
		parentProperty().addListener((observable, oldValue, newValue) ->        {
		    System.out.println("Changed visibility of TransformWidget " + newValue);
		    isNowVis=newValue!=null;
		    if(isNowVis) {
		    	updatePose(initialState);
		    }
		    	
		});
//		tx = new TextField(CreatureLab.getFormatted(initialState.getX()));
//		ty = new TextField(CreatureLab.getFormatted(initialState.getY()));
//		tz = new TextField(CreatureLab.getFormatted(initialState.getZ()));
//		tx.setOnAction(this);
//		ty.setOnAction(this);
//		tz.setOnAction(this);
		tx = new EngineeringUnitsSliderWidget(this,  initialState.getX(), 100,"mm");
		ty = new EngineeringUnitsSliderWidget(this,  initialState.getY(), 100,"mm");
		tz = new EngineeringUnitsSliderWidget(this,  initialState.getZ(), 100,"mm");
		
		storeRotation = initialState.getRotation();
		double  t=0;
		try{
			t=Math.toDegrees(storeRotation.getRotationTilt());
		}catch(Exception ex){
			ex.printStackTrace();
		}
		double  e=0;
		try{
			e=Math.toDegrees(storeRotation.getRotationElevation());
		}catch(Exception ex){
			ex.printStackTrace();
		}
		double  a=0;
		try{
			a=Math.toDegrees(storeRotation.getRotationAzimuth());
		}catch(Exception ex){
			ex.printStackTrace();
		}
		tilt = new EngineeringUnitsSliderWidget(new IOnEngineeringUnitsChange() {
			
			@Override
			public void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
				initialState.setTiltDegrees(newAngleDegrees);
				onChange.onTransformChaging(getCurrent());
			}
			
			@Override
			public void onSliderDoneMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
				initialState.setTiltDegrees(newAngleDegrees);				
				onChange.onTransformFinished(getCurrent());

			}
		}, -179.99, 179.99, t, 100,"degrees");
		elevation = new EngineeringUnitsSliderWidget(new IOnEngineeringUnitsChange() {
			
			@Override
			public void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
				initialState.setElevationDegrees(newAngleDegrees);
				onChange.onTransformChaging(getCurrent());
			}
			
			@Override
			public void onSliderDoneMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
				initialState.setElevationDegrees(newAngleDegrees);
				onChange.onTransformFinished(getCurrent());
			}
		}, -89.99, 89.99, e, 100,"degrees");
		azimeth = new EngineeringUnitsSliderWidget(new IOnEngineeringUnitsChange() {
			
			@Override
			public void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
				initialState.setAzimuthDegrees(newAngleDegrees);
				onChange.onTransformChaging(getCurrent());
			}
			
			@Override
			public void onSliderDoneMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
				initialState.setAzimuthDegrees(newAngleDegrees);
				onChange.onTransformFinished(getCurrent());
			}
		}, -179.99, 179.99, a, 100,"degrees");
		tilt.setAllowResize(false);
		elevation.setAllowResize(false);
		azimeth.setAllowResize(false);
		getColumnConstraints().add(new ColumnConstraints(60)); // translate text
	    getColumnConstraints().add(new ColumnConstraints(200)); // translate values
	    getColumnConstraints().add(new ColumnConstraints(60)); // units
	    getColumnConstraints().add(new ColumnConstraints(60)); // rotate text
	    setHgap(20);// gab between elements
	    
	    tx.showSlider(false);
	    ty.showSlider(false);
	    tz.showSlider(false);
//	    tilt.showSlider(false);
//	    azimeth.showSlider(false);
//	    elevation.showSlider(false);
	    
	    add(	new Text(title), 
	    		1,  0);

	    // These all seem out of order here, but it is because the 
	    // screen is rotating the orenation of this interface from BowlerStudio3dEngine.getOffsetforvisualization()
	    //X line
	    add(	new Text("X"), 
	    		0,  1);
		add(	tx, 
				1,  1);
	
		 add(	new Text("Tilt"), 
	    		0,  4);
		 add(	tilt, 
	    		1,  4);
	    //Y line
	    add(	new Text("Y"), 
	    		0,  2);
		add(	ty, 
				1,  2);
	
		 add(	new Text("Elevation"), 
	    		0,  5);
		 add(	elevation, 
				1,  5);
	    //Z line
	    add(	new Text("Z"), 
	    		0,  3);
		add(	tz, 
				1,  3);
	
		 add(	new Text("Azimuth"), 
	    		0,  6);
		 add(	azimeth, 
	    		1,  6);
	}
	
	private 	TransformNR getCurrent(){
		TransformNR tmp = new TransformNR(
				tx.getValue(),
				ty.getValue(),
				tz.getValue(),
				initialState.getRotation());

		
		return tmp;
	}

	@Override
	public void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
		onChange.onTransformChaging(getCurrent());
	}

	@Override
	public void onSliderDoneMoving(EngineeringUnitsSliderWidget source,
			double newAngleDegrees) {
		handle(null);
	}

	@Override
	public void handle(ActionEvent event) {
		onChange.onTransformChaging(getCurrent());
		onChange.onTransformFinished(getCurrent());
	}

	public void updatePose(TransformNR p) {
		if(!isNowVis)
			return;
		TransformNR pose = p;
		
		
		MobileBaseCadManager.runLater(() -> tx.setValue(pose.getX()));
		MobileBaseCadManager.runLater(() -> ty.setValue(pose.getY()));
		MobileBaseCadManager.runLater(() -> tz.setValue(pose.getZ()));
		
		RotationNR rot = pose.getRotation();
		double  t=0;
		try{
			t=Math.toDegrees(rot.getRotationTilt());
		}catch(Exception ex){
			ex.printStackTrace();
		}
		double  e=0;
		try{
			e=Math.toDegrees(rot.getRotationElevation());
		}catch(Exception ex){
			ex.printStackTrace();
		}
		double  a=0;
		try{
			a=Math.toDegrees(rot.getRotationAzimuth());
		}catch(Exception ex){
			ex.printStackTrace();
		}
		double tiltVar=t;
		double eVar=e;
		double aVar=a;
		MobileBaseCadManager.runLater(() -> tilt.setValue(tiltVar));
		MobileBaseCadManager.runLater(() -> elevation .setValue(eVar));
		MobileBaseCadManager.runLater(() -> azimeth .setValue(aVar));
		// Set the rotation after setting the UI so the read will load the rotation in its pure form
		initialState = p;
	}

}
