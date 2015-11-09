package com.neuronrobotics.bowlerstudio.creature;

import java.time.Duration;

import org.reactfx.util.FxTimer;

import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.AbstractLink;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.DhLinkType;
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

public class LinkSliderWidget extends Group implements  IJointSpaceUpdateListenerNR {
	private AbstractKinematicsNR device;
	private DHParameterKinematics dhdevice;

	private int linkIndex;
	private EngineeringUnitsSliderWidget setpoint;

	
	
	
	public LinkSliderWidget(int linkIndex, DHLink dhlink, AbstractKinematicsNR device2) {

		this.linkIndex = linkIndex;
		this.device = device2;
		if(DHParameterKinematics.class.isInstance(device2)){
			dhdevice=(DHParameterKinematics)device2;
		}

		AbstractLink abstractLink  = device2.getAbstractLink(linkIndex);
		
		

		TextField name = new TextField(abstractLink.getLinkConfiguration().getName());
		name.setMaxWidth(100.0);
		name.setOnAction(event -> {
			abstractLink.getLinkConfiguration().setName(name.getText());
		});
		
		setpoint = new EngineeringUnitsSliderWidget(new IOnEngineeringUnitsChange() {
			
			@Override
			public void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onSliderDoneMoving(EngineeringUnitsSliderWidget source,
					double newAngleDegrees) {
	    		try {
					device2.setDesiredJointAxisValue(linkIndex, setpoint.getValue(), 2);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				};
			}
		}, 
		abstractLink.getMinEngineeringUnits(), 
		abstractLink.getMaxEngineeringUnits(), 
		device2.getCurrentJointSpaceVector()[linkIndex], 
		180,dhlink.getLinkType()==DhLinkType.ROTORY?"degrees":"mm");
		
		
		GridPane panel = new GridPane();
		
		panel.getColumnConstraints().add(new ColumnConstraints(30)); // column 1 is 75 wide
		panel.getColumnConstraints().add(new ColumnConstraints(120)); // column 1 is 75 wide
		panel.getColumnConstraints().add(new ColumnConstraints(120)); // column 2 is 300 wide
		
		

		panel.add(	new Text("#"+linkIndex), 
				0, 
				0);
		panel.add(	name, 
				1, 
				0);
		panel.add(	setpoint, 
				2, 
				0);

		getChildren().add(panel);
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
