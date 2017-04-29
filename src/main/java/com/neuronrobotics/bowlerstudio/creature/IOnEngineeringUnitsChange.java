package com.neuronrobotics.bowlerstudio.creature;

public interface IOnEngineeringUnitsChange {
	void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees);
	void onSliderDoneMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees);
}
