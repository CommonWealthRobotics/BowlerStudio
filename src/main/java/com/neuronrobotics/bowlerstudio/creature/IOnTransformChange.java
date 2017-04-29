package com.neuronrobotics.bowlerstudio.creature;

import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;

public interface IOnTransformChange {
	void onTransformChaging(TransformNR newTrans);
	void onTransformFinished(TransformNR newTrans);
}
