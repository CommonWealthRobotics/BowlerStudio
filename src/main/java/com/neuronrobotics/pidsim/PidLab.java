package com.neuronrobotics.pidsim;

import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;

public class PidLab extends AbstractBowlerStudioTab {
	LinearPhysicsEngine engine;
	@Override
	public void onTabClosing() {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] getMyNameSpaces() {
		return new String[0];
	}

	@Override
	public void initializeUI(BowlerAbstractDevice pm) {
		engine = (LinearPhysicsEngine)pm;
		
		
	}

	@Override
	public void onTabReOpening() {
		// TODO Auto-generated method stub

	}

}
