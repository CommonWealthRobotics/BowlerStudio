package com.neuronrobotics.bowlerstudio.creature;

public interface IOnEngineeringUnitsChange {
	public void onSliderMoving(EngineeringUnitsSliderWidget source,double newAngleDegrees);
	public void onSliderDoneMoving(EngineeringUnitsSliderWidget source,double newAngleDegrees);
}
