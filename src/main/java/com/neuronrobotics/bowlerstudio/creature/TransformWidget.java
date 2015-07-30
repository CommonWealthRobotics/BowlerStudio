package com.neuronrobotics.bowlerstudio.creature;

import Jama.Matrix;

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
	EngineeringUnitsSliderWidget rw;
	EngineeringUnitsSliderWidget rx;
	EngineeringUnitsSliderWidget ry;
	EngineeringUnitsSliderWidget rz;
	private TextField tx;
	private TextField ty;
	private TextField tz;
	private TransformNR initialState;

	public TransformWidget(String title, TransformNR initialState, IOnTransformChange onChange){
		this.initialState = initialState;
		this.onChange = onChange;
		tx = new TextField(CreatureLab.getFormatted(initialState.getX()));
		ty = new TextField(CreatureLab.getFormatted(initialState.getY()));
		tz = new TextField(CreatureLab.getFormatted(initialState.getZ()));
		tx.setOnAction(this);
		ty.setOnAction(this);
		tz.setOnAction(this);
		RotationNR rot = initialState.getRotation();
		rx = new EngineeringUnitsSliderWidget(this, -180, 180, Math.toDegrees(rot.getRotationX()), 100,"degrees");
		ry = new EngineeringUnitsSliderWidget(this, -180, 180, Math.toDegrees(rot.getRotationY()), 100,"degrees");
		rz = new EngineeringUnitsSliderWidget(this, -180, 180, Math.toDegrees(rot.getRotationZ()), 100,"degrees");

		getColumnConstraints().add(new ColumnConstraints(15)); // translate text
	    getColumnConstraints().add(new ColumnConstraints(60)); // translate values
	    getColumnConstraints().add(new ColumnConstraints(50)); // units
	    getColumnConstraints().add(new ColumnConstraints(20)); // rotate text
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
		 add(	new Text("mm"), 
	    		2,  1);
		 add(	new Text("(r)X"), 
	    		3,  1);
		 add(	rx, 
	    		4,  1);
	    //Y line
	    add(	new Text("Y"), 
	    		0,  2);
		add(	ty, 
				1,  2);
		 add(	new Text(" mm"), 
	    		2,  2);
		 add(	new Text("(r)Y"), 
	    		3,  2);
		 add(	ry, 
				4,  2);
	    //Z line
	    add(	new Text("Z"), 
	    		0,  3);
		add(	tz, 
				1,  3);
		 add(	new Text(" mm"), 
	    		2,  3);
		 add(	new Text("(r)Z"), 
	    		3,  3);
		 add(	rz, 
	    		4,  3);
		
	}
	
	private 	TransformNR getCurrent(){
		TransformNR tmp = new TransformNR(Double.parseDouble(tx.getText()),
				Double.parseDouble(ty.getText()),
				Double.parseDouble(tz.getText()),new RotationNR( rx.getValue(),ry.getValue(), rz.getValue()));

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

	public void updatePose(TransformNR pose) {
		//Log.debug("Transform widget is updating to: "+pose);
		Platform.runLater(() -> {
			tx.setText(CreatureLab.getFormatted(pose.getX()));
			ty.setText(CreatureLab.getFormatted(pose.getY()));
			tz.setText(CreatureLab.getFormatted(pose.getZ()));
		});
		RotationNR rot = pose.getRotation();
		rx.setValue(Math.toDegrees(rot.getRotationX()));
		ry .setValue(Math.toDegrees(rot.getRotationY()));
		rz .setValue(Math.toDegrees(rot.getRotationZ()));
	}

}
