package com.neuronrobotics.nrconsole.plugin.DyIO.hexapod;

import java.awt.Dimension;

import javafx.embed.swing.SwingNode;
import javafx.scene.control.ScrollPane;

import javax.swing.JPanel;

import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.dyio.DyIO;

public class HexapodController extends AbstractBowlerStudioTab {
	private HexapodConfigPanel hex;

	@Override
	public void onTabClosing() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String[] getMyNameSpaces() {
		// TODO Auto-generated method stub
		return new String[] {"neuronrobotics.dyio.*"};
	}

	@Override
	public void initializeUI(BowlerAbstractDevice pm) {
		if(hex == null)
			hex = new HexapodConfigPanel();
		hex.setDyIO((DyIO)pm);
		
		SwingNode sn = new SwingNode();
        sn.setContent(hex);
        ScrollPane s1 = new ScrollPane();
       
        s1.setContent(sn);
        setContent(s1);
        setText("Hexapod Control");
		onTabReOpening();
	}

	@Override
	public void onTabReOpening() {
		// TODO Auto-generated method stub
		
	}

}
