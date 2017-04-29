package com.neuronrobotics.bowlerstudio.creature;

import javafx.embed.swing.SwingNode;
import javafx.scene.control.ScrollPane;

import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;

public class AdvancedKinematicsController  extends AbstractBowlerStudioTab {

	private PrinterConfiguration gui = new PrinterConfiguration();

	@Override
	public void onTabClosing() {
	}

	@Override
	public String[] getMyNameSpaces() {
		return new String[]{"bcs.cartesian.*"};
	}

	@Override
	public void initializeUI(BowlerAbstractDevice pm) {
		AbstractKinematicsNR kin = (AbstractKinematicsNR)pm;
		gui.setKinematicsModel(kin);
		SwingNode sn = new SwingNode();
        sn.setContent(gui);
        ScrollPane s1 = new ScrollPane();
       
        s1.setContent(sn);
        setContent(s1);
        setText("Advanced Kinematics");
		onTabReOpening();
	}

	@Override
	public void onTabReOpening() {
	}
}
