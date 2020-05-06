package com.neuronrobotics.bowlerstudio.creature;

import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;

public class JogKinematicsDevice   extends AbstractBowlerStudioTab{





	@Override
	public void onTabClosing() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String[] getMyNameSpaces() {
		// TODO Auto-generated method stub
		return new String[]{"bcs.cartesian.*"};
	}

	@Override
	public void initializeUI(BowlerAbstractDevice pm) {
		if(DHParameterKinematics.class.isInstance(pm))
			setContent(new JogWidget((DHParameterKinematics) pm));
		else if(MobileBase.class.isInstance(pm))
			setContent(new JogMobileBase((MobileBase) pm));
        setText("Jog Kinematics Devices");
		onTabReOpening();
	}

	@Override
	public void onTabReOpening() {
		// TODO Auto-generated method stub
		
	}

}
