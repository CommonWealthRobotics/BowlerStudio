package com.neuronrobotics.bowlerstudio.creature;

import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;

public class JogKinematicsDevice extends AbstractBowlerStudioTab {
    @Override
    public void onTabClosing() {
    }

    @Override
    public String[] getMyNameSpaces() {
        return new String[]{"bcs.cartesian.*"};
    }

    @Override
    public void initializeUI(BowlerAbstractDevice pm) {
        setContent(new JogWidget((AbstractKinematicsNR) pm));
        setText("Jog Kinematics Devices");
        onTabReOpening();
    }

    @Override
    public void onTabReOpening() {
    }
}
