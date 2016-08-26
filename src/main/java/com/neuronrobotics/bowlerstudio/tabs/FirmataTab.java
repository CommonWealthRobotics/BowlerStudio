package com.neuronrobotics.bowlerstudio.tabs;

import javax.swing.JFrame;

import org.firmata4j.IODevice;
import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.ui.JPinboard;

import com.neuronrobotics.sdk.addons.kinematics.FirmataBowler;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;

import javafx.embed.swing.SwingNode;
import javafx.scene.control.ScrollPane;

public class FirmataTab extends AbstractBowlerStudioTab {

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
		FirmataDevice device  =( (FirmataBowler) pm).getFirmataDevice();
		// TODO Auto-generated method stub
		JFrame frame = new JFrame("Pinboard Example");
		frame.add(new JPinboard(device  ));
		frame.pack();
		frame.setVisible(true);
		
		JPinboard pinboard = new JPinboard(device  );
		pinboard.setVisible(true);
		SwingNode sn = new SwingNode();
        sn.setContent(pinboard);
        ScrollPane s1 = new ScrollPane();
       
        s1.setContent(sn);
        setContent(s1);
        setText("Firmata Pinpoard");
		onTabReOpening();
	}

	@Override
	public void onTabReOpening() {
		// TODO Auto-generated method stub

	}

}
