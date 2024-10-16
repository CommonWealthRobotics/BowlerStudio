package com.neuronrobotics.bowlerstudio.creature;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.assets.FontSizeManager;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.scene.control.Label;

@SuppressWarnings("restriction")
public class EngineeringUnitsSliderWidget extends GridPane implements ChangeListener<Number> {
	private TextField spv;
	private Label increment;
	private Slider setpoint;
	private IOnEngineeringUnitsChange listener;
	private boolean intCast = false;
	private boolean allowResize = true;
	private Button jogplus = new Button("+");
	private Button jogminus = new Button("-");
	private double instantValueStore = 0;
	private boolean editing = false;
	private double jogIncrement = 1.0;
	private String units;
	private double min;
	private double max;

	public EngineeringUnitsSliderWidget(IOnEngineeringUnitsChange listener, double min, double max, double current,
			double width, String units, boolean intCast) {
		this(listener, min, max, current, width, units);
		this.intCast = intCast;
	}

	public EngineeringUnitsSliderWidget(IOnEngineeringUnitsChange listener, double current, double width,
			String units) {
		this(listener, -Float.MAX_VALUE, Float.MAX_VALUE, current, width, units);
		
	}

	private void onSliderMovingInternal(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
		editing = true;
		//System.out.println("Slider moving ");
		getListener().onSliderMoving(this, newAngleDegrees);
	}

	private void onSliderDoneMovingInternal(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
		editing = false;
		instantValueStore=(newAngleDegrees);
		//System.out.println("Slider done");
		getListener().onSliderDoneMoving(this, newAngleDegrees);
	}

	public EngineeringUnitsSliderWidget(IOnEngineeringUnitsChange listener, double minIn, double maxIn, double current,
			double width, String units) {
		this.min = minIn;
		this.max = maxIn;
		this.units = units;
		this.setListener(listener);
		setpoint = new Slider();
		increment = new Label(jogIncrement+"");
		instantValueStore = current;
		if (min > max) {
			double minStart = min;
			min = max;
			max = minStart;
		}
		if (min > current)
			min = current;
		if (max < current)
			max = current;
		double range = Math.abs(max - min);
		if (range < 1) {
			min = min - 100;
			max = max + 100;
			range = 200;
		}
		setpoint.setMin(min);
		setpoint.setMax(max);
		setpoint.setValue(current);
		setpoint.setShowTickLabels(true);
		setpoint.setShowTickMarks(true);
		// setpoint.setSnapToTicks(true);
		setpoint.setMajorTickUnit(range);
		setpoint.setMinorTickCount(5);
		// setpoint.setBlockIncrement(range/100);
		spv = new TextField(getFormatted(current));
		spv.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue,
					Boolean newPropertyValue) {
				if (newPropertyValue) {
					//System.out.println("Textfield on focus");
					editing = true;
				} else {
					//System.out.println("Textfield out focus");
					editing = false;
				}
			}
		});
		spv.setOnAction(event -> {
			try {				
				localSetValue(Double.parseDouble(spv.getText()));
			} catch (Throwable t) {
				t.printStackTrace();
				localSetValue(instantValueStore);

			}
		});
		setpoint.setMaxWidth(width);
		setpoint.valueChangingProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
			try {
				//System.err.println("Slider moving = "+newValue);
				if (!newValue)
					onSliderDoneMovingInternal(this, setpoint.getValue());
			} catch (java.lang.NumberFormatException ex) {
				ex.printStackTrace();
				setValue(0);
				return;
			}

		});
		setpoint.valueProperty().addListener(this);

		String unitsString = "(" + units + ")";
		double scale = (double)(FontSizeManager.getDefaultSize())/12.0;
		getColumnConstraints().add(new ColumnConstraints(30*scale)); // column 2 is 100 wide
		getColumnConstraints().add(new ColumnConstraints(40*scale)); // column 2 is 100 wide
		getColumnConstraints().add(new ColumnConstraints(30*scale)); // column 2 is 100 wide
		getColumnConstraints().add(new ColumnConstraints(100*scale)); // column 2 is 100 wide
		getColumnConstraints().add(new ColumnConstraints(unitsString.length() * 7*scale)); // column 2 is 100 wide

		add(setpoint, 3, 1);
		add(jogplus, 2, 0);
		add(increment, 1, 0);
		add(jogminus, 0, 0);
		add(spv, 3, 0);
		add(new Text(unitsString), 4, 0);

		jogplus.setOnAction(event -> {
			jogPlusOne();
		});
		jogminus.setOnAction(event -> {
			jogMinusOne();
		});
	}

	private void localSetValue(double val) {
		BowlerStudio.runLater(() -> {
			editing = false;
			setValue(val);

			onSliderMovingInternal(this, val);
			onSliderDoneMovingInternal(this, val);
		});
	}

	public void jogMinusOne() {
		jog(-getJogIncrement());
	}

	public void jogPlusOne() {
		jog(getJogIncrement());
	}
	public void jog(double amount) {
		double value = getValue() + amount;
		double max2 = setpoint.getMax();
		if (value > max2)
			return;
		double min2 = setpoint.getMin();
		if (value < min2)
			return;
		setValue(value);
		onSliderMovingInternal(this, value);
		onSliderDoneMovingInternal(this, value);
	}

	public void setUpperBound(double newBound) {
		setpoint.setMax(newBound);
	}

	public void setLowerBound(double newBound) {
		setpoint.setMin(newBound);
	}

	@Override
	public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
		updateValue();
		//System.out.println("Updating value to "+newValue);
	}

	private void updateValue() {
		BowlerStudio.runLater(() -> {
			spv.setText(getFormatted(setpoint.getValue()));
			onSliderMovingInternal(this, setpoint.getValue());
		});
	}

	public void setValue(double value) {
		if (editing)
			return;// do not overwrite an editing field
		double val = value;
		if (val > setpoint.getMax()) {
			if (isAllowResize())
				setpoint.setMax(val);
			else
				val = setpoint.getMax();
		}
		if (val < setpoint.getMin()) {
			if (isAllowResize())
				setpoint.setMin(val);
			else
				val = setpoint.getMin();
		}
		instantValueStore = val;
		double toSet = val;
		
		BowlerStudio.runLater(() -> {
			setValueLocal(toSet);
		});

	}

	private void setValueLocal(double value) {
		setpoint.valueProperty().removeListener(this);
		double range = Math.abs(setpoint.getMax() - setpoint.getMin());
		if (range > 0)
			setpoint.setMajorTickUnit(range);
		setpoint.setValue(value);
		spv.setText(getFormatted(setpoint.getValue()));
		setpoint.valueProperty().addListener(this);
		// System.out.println("Setpoint changed to "+val);
	}

	public double getValue() {
		return instantValueStore;
	}

	public String getFormatted(double value) {
		if (intCast)
			return String.valueOf((int) value);
		return String.format("%8.3f", (double) value);
	}

	public IOnEngineeringUnitsChange getListener() {
		if (listener == null)
			return new IOnEngineeringUnitsChange() {

				@Override
				public void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onSliderDoneMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
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

	public void showSlider(boolean b) {
		setpoint.setVisible(b);
	}

	public boolean isEditing() {
		return editing;
	}

	/**
	 * @return the jogIncrement
	 */
	public double getJogIncrement() {
		return jogIncrement;
	}

	/**
	 * @param jogIncrement the jogIncrement to set
	 */
	public void setJogIncrement(double j) {
		//System.out.println("Increment set to "+j+" "+units);
		jogIncrement=Math.abs(j);
		BowlerStudio.runLater(()->{
			increment.setText(""+jogIncrement);
		});
	}
}
