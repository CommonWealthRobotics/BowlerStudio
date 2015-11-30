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
	private TransformNR offset =BowlerStudio3dEngine.getOffsetforvisualization().inverse();
	

	public TransformWidget(String title, TransformNR is, IOnTransformChange onChange){
		this.initialState = offset.times(is);
		this.onChange = onChange;
//		tx = new TextField(CreatureLab.getFormatted(initialState.getX()));
//		ty = new TextField(CreatureLab.getFormatted(initialState.getY()));
//		tz = new TextField(CreatureLab.getFormatted(initialState.getZ()));
//		tx.setOnAction(this);
//		ty.setOnAction(this);
//		tz.setOnAction(this);
		tx = new EngineeringUnitsSliderWidget(this, -200, 200, initialState.getX(), 100,"mm");
		ty = new EngineeringUnitsSliderWidget(this, -200, 200, initialState.getY(), 100,"mm");
		tz = new EngineeringUnitsSliderWidget(this, -200, 200, initialState.getZ(), 100,"mm");
		
		RotationNR rot = initialState.getRotation();
		tilt = new EngineeringUnitsSliderWidget(this, -179.99, 179.99, Math.toDegrees(rot.getRotationTilt()), 100,"degrees");
		elevation = new EngineeringUnitsSliderWidget(this, -179.99, 179.99, Math.toDegrees(rot.getRotationElevation()), 100,"degrees");
		azimeth = new EngineeringUnitsSliderWidget(this, -179.99, 179.99, Math.toDegrees(rot.getRotationAzimuth()), 100,"degrees");
		getColumnConstraints().add(new ColumnConstraints(15)); // translate text
	    getColumnConstraints().add(new ColumnConstraints(130)); // translate values
	    getColumnConstraints().add(new ColumnConstraints(50)); // units
	    getColumnConstraints().add(new ColumnConstraints(40)); // rotate text
	    setHgap(20);// gab between elements
	    
	    
	    add(	new Text(title), 
	    		1,  0);
//	    add(	new Text("(r)W"), 
//	    		3,  0);
//	    add(	rw, 
//	    		4,  0);
	    //X line
	    add(	new Text("X"), 
	    		0,  1);
		add(	tx, 
				1,  1);
	
		 add(	new Text("Tilt"), 
	    		3,  1);
		 add(	tilt, 
	    		4,  1);
	    //Y line
	    add(	new Text("Y"), 
	    		0,  2);
		add(	ty, 
				1,  2);
	
		 add(	new Text("Elevation"), 
	    		3,  2);
		 add(	elevation, 
				4,  2);
	    //Z line
	    add(	new Text("Z"), 
	    		0,  3);
		add(	tz, 
				1,  3);
	
		 add(	new Text("Azimuth"), 
	    		3,  3);
		 add(	azimeth, 
	    		4,  3);
	}
	
	private 	TransformNR getCurrent(){
		TransformNR tmp = new TransformNR(
				tx.getValue(),
				ty.getValue(),
				tz.getValue(),
				new RotationNR( 
						tilt.getValue(),
						azimeth.getValue(),
						elevation.getValue()
						));

		
		return offset.inverse().times(tmp);
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
		TransformNR pose = offset.times(p);
		
		Platform.runLater(() -> {
			tx.setValue(pose.getX());
			ty.setValue(pose.getY());
			tz.setValue(pose.getZ());
		});
		RotationNR rot = pose.getRotation();
		tilt.setValue(Math.toDegrees(rot.getRotationTilt()));
		elevation .setValue(Math.toDegrees(rot.getRotationElevation()));
		azimeth .setValue(Math.toDegrees(rot.getRotationAzimuth()));
	}

}
