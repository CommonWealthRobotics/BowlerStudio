package com.neuronrobotics.nrconsole.plugin.cartesian;

import javafx.scene.layout.VBox;

import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;

public class DHKinematicsLab extends AbstractBowlerStudioTab {
	DHParameterKinematics device;
	
	
	@Override
	public void onTabClosing() {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] getMyNameSpaces() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initializeUI(BowlerAbstractDevice pm) {
		// TODO Auto-generated method stub
		device=(DHParameterKinematics)pm;
		VBox links = new VBox();
		for(DHLink l:device.getChain().getLinks()){
			links.getChildren().add(new DHLinkWidget(l));
		}
		setContent(links);
	}

	@Override
	public void onTabReOpening() {
		// TODO Auto-generated method stub

	}

}
