package com.neuronrobotics.nrconsole.plugin.DeviceConfig;

import javafx.embed.swing.SwingNode;
import javafx.scene.control.ScrollPane;

import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.replicator.driver.BowlerBoardDevice;
import com.neuronrobotics.replicator.driver.NRPrinter;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;

public class PrinterConiguration extends AbstractBowlerStudioTab{

	private DeviceConfigPanel gui = new DeviceConfigPanel();

	@Override
	public void onTabClosing() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String[] getMyNameSpaces() {
		return new String[]{"bcs.cartesian.*"};
	}

	@Override
	public void initializeUI(BowlerAbstractDevice pm) {
		NRPrinter printer = (NRPrinter)pm;
		BowlerBoardDevice delt = printer.getDeltaDevice();
		
		if (delt.isAvailable()){
			gui.setDevices(delt,printer);
		}
		if (delt.isAvailable()){
			gui.updateSettings();
		}
		
		SwingNode sn = new SwingNode();
        sn.setContent(gui);
        ScrollPane s1 = new ScrollPane();
       
        s1.setContent(sn);
        setContent(s1);
        setText("Printer Config");
		onTabReOpening();
	}

	@Override
	public void onTabReOpening() {
		// TODO Auto-generated method stub
		
	}

}
