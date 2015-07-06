package com.neuronrobotics.nrconsole.plugin.cartesian;

public interface IOnAngleChange {
	public void onSliderMoving(AngleSliderWidget source,double newAngleDegrees);
	public void onSliderDoneMoving(AngleSliderWidget source,double newAngleDegrees);
}
