package com.neuronrobotics.nrconsole.plugin.PID;

import javax.swing.JPanel;

import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.namespace.bcs.pid.IExtendedPIDControl;

public class PIDControl extends AbstractBowlerStudioTab {
	//private DyIO dyio;
	private IExtendedPIDControl pid;
	private PIDControlGui gui;
	//private JButton display = new JButton("Display PID configuration");
	//private JScrollPane scrollPanel = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
	private JPanel holder ;

	public IExtendedPIDControl getPidDevice() {
		return pid;
	}

	@Override
	public void onTabClosing() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String[] getMyNameSpaces() {
		// TODO Auto-generated method stub
		return new String []{"bcs.pid.*"};
	}

	@Override
	public void initializeUI(BowlerAbstractDevice pm) {
		if(gui == null){
			pid = (IExtendedPIDControl)pm;
			gui = new PIDControlGui(pid);
			holder.add(gui,"wrap");
			holder.invalidate();
			onTabReOpening();
		}
	}

	@Override
	public void onTabReOpening() {
		// TODO Auto-generated method stub
		
	}

}
