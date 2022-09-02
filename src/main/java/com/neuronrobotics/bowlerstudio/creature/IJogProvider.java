package com.neuronrobotics.bowlerstudio.creature;

import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;

public interface IJogProvider {
	
	public TransformNR getJogIncrement();
	
}
