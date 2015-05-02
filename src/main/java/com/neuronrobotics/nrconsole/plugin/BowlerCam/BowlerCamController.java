package com.neuronrobotics.nrconsole.plugin.BowlerCam;

import javafx.embed.swing.SwingNode;
import javafx.scene.control.ScrollPane;

import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.sdk.bowlercam.device.BowlerCamDevice;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;

public class BowlerCamController extends AbstractBowlerStudioTab{

	private BowlerCamPanel bcp = new BowlerCamPanel();

	@Override
	public void onTabClosing() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String[] getMyNameSpaces() {
		// TODO Auto-generated method stub
		return new String[] {"neuronrobotics.bowlercam.*"};
	}

	@Override
	public void initializeUI(BowlerAbstractDevice pm) {

		bcp.setConnection((BowlerCamDevice)pm);
		
		SwingNode sn = new SwingNode();
        sn.setContent(bcp);
        ScrollPane s1 = new ScrollPane();
       
        s1.setContent(sn);
        setContent(s1);
        setText("BowlerCam Control");
		onTabReOpening();
	}

	@Override
	public void onTabReOpening() {
		// TODO Auto-generated method stub
		
	}

}
