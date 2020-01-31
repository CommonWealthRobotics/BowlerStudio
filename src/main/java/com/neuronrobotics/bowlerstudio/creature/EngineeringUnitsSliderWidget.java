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

@SuppressWarnings("restriction")
public class EngineeringUnitsSliderWidget extends GridPane implements ChangeListener<Number>{
	private TextField setpointValue;
	private Slider setpoint;
	private IOnEngineeringUnitsChange listener;
	private boolean intCast=false;
	private boolean allowResize=true;
	public EngineeringUnitsSliderWidget(IOnEngineeringUnitsChange listener,double min, double max,  double current, double width, String units, boolean intCast){
		this(listener, min, max, current, width, units);
		this.intCast = intCast;
	}
	public EngineeringUnitsSliderWidget(IOnEngineeringUnitsChange listener,  double current, double width, String units){
		this(listener, current/2, current*2, current, width, units);
	
	}
	public EngineeringUnitsSliderWidget(IOnEngineeringUnitsChange listener, double min, double max, double current, double width, String units){
		this.setListener(listener);
		setpoint = new Slider();
		
		if(min>max){
			double minStart = min;
			min=max;
			max=minStart;
		}
		if(min>current)
			min=current;
		if(max<current)
			max=current;
		double range = Math.abs(max-min);
		if(range<1){
			min=min-100;
			max = max+100;
			range=200;
		}
		setpoint.setMin(min);
		setpoint.setMax(max);
		setpoint.setValue(current);
		setpoint.setShowTickLabels(true);
		setpoint.setShowTickMarks(true);
		//setpoint.setSnapToTicks(true);
		setpoint.setMajorTickUnit(range);
		setpoint.setMinorTickCount(5);
		//setpoint.setBlockIncrement(range/100);
		setpointValue = new TextField(getFormatted(current));
		setpointValue.setOnAction(event -> {
			String txt =setpointValue.getText();
			double val =Double.parseDouble(txt);
			System.out.println("Setpoint Text changed to "+val);
			
			Platform.runLater(() -> {
				setValueLocal(val);

				getListener().onSliderMoving(this,val);
				getListener().onSliderDoneMoving(this,val);
			});
		});
		setpoint.setMaxWidth(width);
		setpoint.valueChangingProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
			try {
				double val =Double.parseDouble(setpointValue.getText());
				System.err.println("Slider done moving = "+newValue);
				if(!newValue)
					getListener().onSliderDoneMoving(this,val);
			}catch(java.lang.NumberFormatException ex) {
				setValueLocal(0);
				return;
			}
			
		});
		setpoint.valueProperty().addListener(this);
		
		String unitsString = "("+units+")";
		getColumnConstraints().add(new ColumnConstraints(width+20)); // column 2 is 100 wide
		getColumnConstraints().add(new ColumnConstraints(100)); // column 2 is 100 wide
		getColumnConstraints().add(new ColumnConstraints(unitsString.length()*7)); // column 2 is 100 wide
		
		
		add(	setpoint, 
				0, 
				0);
		add(	setpointValue, 
				1, 
				0);
		add(	new Text(unitsString), 
				2, 
				0);
	}
	public void setUpperBound(double newBound){
		setpoint.setMax(newBound);
	}
	public void setLowerBound(double newBound){
		setpoint.setMin(newBound);
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
				setValueLocal(value);
		});

	}
	private void setValueLocal(double value) {
		setpoint.valueProperty().removeListener(this);
		double val = value;
		if(val>setpoint.getMax()){
			if(isAllowResize())
				setpoint.setMax(val);
			else
				val=setpoint.getMax();
		}if(val<setpoint.getMin()){
			if(isAllowResize())
				setpoint.setMin(val);
			else
				val=setpoint.getMin();
		}
		double range = Math.abs(setpoint.getMax()-setpoint.getMin());
		setpoint.setMajorTickUnit(range);
		setpoint.setValue(val);
		setpointValue.setText(getFormatted(setpoint.getValue()));
		setpoint.valueProperty().addListener(this);
		//System.out.println("Setpoint changed to "+val);
	}
	
	public double getValue(){
		return setpoint.getValue();
	}
	
	public  String getFormatted(double value){
		if(intCast)
			return String.valueOf((int)value);
	    return String.format("%8.2f", (double)value);
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
	public boolean isAllowResize() {
		return allowResize;
	}
	public void setAllowResize(boolean allowResize) {
		this.allowResize = allowResize;
	}
}
