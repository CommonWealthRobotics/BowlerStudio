package com.neuronrobotics.bowlerstudio.creature;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

public class EngineeringUnitsSliderWidget extends GridPane implements ChangeListener<Number>{
	private TextField setpointValue;
	private Slider setpoint;
	private IOnEngineeringUnitsChange listener;
	private boolean intCast;

	public EngineeringUnitsSliderWidget(IOnEngineeringUnitsChange listener, double min, double max, double current, double width, String units, boolean intCast){
		this(listener, min, max, current, width, units);
		this.intCast = intCast;
		
	}
	
	public EngineeringUnitsSliderWidget(IOnEngineeringUnitsChange listener, double min, double max, double current, double width, String units){
		this.setListener(listener);
		setpoint = new Slider();
		setpoint.setMin(min);
		setpoint.setMax(max);
		setpoint.setValue(current);
		setpoint.setShowTickLabels(true);
		setpoint.setShowTickMarks(true);
		//setpoint.setSnapToTicks(true);
		setpoint.setMajorTickUnit(50);
		setpoint.setMinorTickCount(5);
		setpoint.setBlockIncrement(10);
		setpointValue = new TextField(getFormatted(current));
		setpointValue.setOnAction(event -> {
			Platform.runLater(() -> {
				double val =Double.parseDouble(setpointValue.getText());
				setpoint.valueProperty().removeListener(this);
				if(val>setpoint.getMax()){
					setpoint.setMax(val);
				}if(val<setpoint.getMin()){
					setpoint.setMin(val);
				}
				setpoint.setValue(val);
				setpointValue.setText(getFormatted(setpoint.getValue()));
				setpoint.valueProperty().addListener(this);
				getListener().onSliderMoving(this,setpoint.getValue());
				getListener().onSliderDoneMoving(this,val);
			});
		});
		setpoint.setMaxWidth(width);
		setpoint.valueChangingProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
			double val =Double.parseDouble(setpointValue.getText());
			System.err.println("Slider done moving = "+newValue);
			if(!newValue)
				getListener().onSliderDoneMoving(this,val);
		});
		setpoint.valueProperty().addListener(this);
		
		getColumnConstraints().add(new ColumnConstraints(width+10)); // column 2 is 100 wide
		getColumnConstraints().add(new ColumnConstraints(60)); // column 2 is 100 wide
		getColumnConstraints().add(new ColumnConstraints(60)); // column 2 is 100 wide
		
		
		add(	setpoint, 
				0, 
				0);
		add(	setpointValue, 
				1, 
				0);
		add(	new Text("("+units+")"), 
				2, 
				0);
	}
	@Override
	public void changed(ObservableValue<? extends Number> observable,
			Number oldValue, Number newValue) {
		updateValue();
	}
	
	private void updateValue(){
		Platform.runLater(() -> {
			setpointValue.setText(getFormatted(setpoint.getValue()));
			getListener().onSliderMoving(this,setpoint.getValue());
		});
	}

	
	public void setValue(double value){
		Platform.runLater(() -> {
				setpoint.valueProperty().removeListener(this);
				if(value>setpoint.getMax()){
					setpoint.setMax(value);
				}if(value<setpoint.getMin()){
					setpoint.setMin(value);
				}
				setpoint.setValue(value);
				setpointValue.setText(getFormatted(setpoint.getValue()));
				setpoint.valueProperty().addListener(this);
		});

	}
	
	public double getValue(){
		return setpoint.getValue();
	}
	
	public  String getFormatted(double value){
		if(intCast)
			return ""+((int)value);
	    return String.format("%4.3f%n", (double)value);
	}
	public IOnEngineeringUnitsChange getListener() {
		if(listener==null)
			return new IOnEngineeringUnitsChange() {

				@Override
				public void onSliderMoving(EngineeringUnitsSliderWidget source,
						double newAngleDegrees) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onSliderDoneMoving(EngineeringUnitsSliderWidget source,
						double newAngleDegrees) {
					// TODO Auto-generated method stub
					
				}

			};
		return listener;
	}
	public void setListener(IOnEngineeringUnitsChange listener) {
		this.listener = listener;
	}
}
