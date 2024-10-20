package com.neuronrobotics.bowlerstudio.creature;

import com.neuronrobotics.sdk.addons.kinematics.VitaminLocation;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;

public interface ITransformProvider {
	TransformNR get(VitaminLocation selectedVitamin);
}
