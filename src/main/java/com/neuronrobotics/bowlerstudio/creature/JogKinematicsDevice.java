package com.neuronrobotics.bowlerstudio.creature;

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
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

        setContent(new JogWidget((AbstractKinematicsNR) pm));
        setText("Jog Kinematics Devices");
		onTabReOpening();
	}

	@Override
	public void onTabReOpening() {
		// TODO Auto-generated method stub
		
	}

}
