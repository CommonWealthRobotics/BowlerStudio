package com.neuronrobotics.nrconsole.plugin.cartesian;


import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;

import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.Log;

public class CreatureLab extends AbstractBowlerStudioTab {

	@Override
	public void onTabClosing() {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] getMyNameSpaces() {
		// TODO Auto-generated method stub
		return new String[0];
	}

	@Override
	public void initializeUI(BowlerAbstractDevice pm) {
		// TODO Auto-generated method stub
		setText("Creature Lab");

		GridPane dhlabTopLevel=new GridPane();
		
		if(DHParameterKinematics.class.isInstance(pm)){
			DHParameterKinematics device=(DHParameterKinematics)pm;
			Log.debug("Loading xml: "+device.getXml());
			dhlabTopLevel.add(new DhChainWidget(device), 0, 0);
		}else if(MobileBase.class.isInstance(pm)) {
			MobileBase device=(MobileBase)pm;
			Log.debug("Loading xml: "+device.getXml());
			
			dhlabTopLevel.add(new DhChainWidget(device), 0, 0);
			
		}else if(AbstractKinematicsNR.class.isInstance(pm)) {
			AbstractKinematicsNR device=(AbstractKinematicsNR)pm;
			Log.debug("Loading xml: "+device.getXml());
			dhlabTopLevel.add(new DhChainWidget(device), 0, 0);
		}
		
		setContent(new ScrollPane(dhlabTopLevel));
	}


	@Override
	public void onTabReOpening() {
		
	}
	
	public static String getFormatted(double value){
	    return String.format("%4.3f%n", (double)value);
	}

}
