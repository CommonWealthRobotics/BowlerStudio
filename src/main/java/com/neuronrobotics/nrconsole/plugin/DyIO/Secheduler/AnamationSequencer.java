package com.neuronrobotics.nrconsole.plugin.DyIO.Secheduler;

import javafx.embed.swing.SwingNode;
import javafx.scene.control.ScrollPane;

import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;

public class AnamationSequencer extends AbstractBowlerStudioTab {
	public static final String[] myNames ={"neuronrobotics.dyio.*"};
	private SchedulerGui gui=new SchedulerGui();;


	@Override
	public void onTabClosing() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String[] getMyNameSpaces() {
		return new String[] {"neuronrobotics.dyio.*"};
	}

	@Override
	public void initializeUI(BowlerAbstractDevice pm) {
		// TODO Auto-generated method stub
		gui.setConnection(pm);
		SwingNode sn = new SwingNode();
        sn.setContent(gui);
        ScrollPane s1 = new ScrollPane();
       
        s1.setContent(sn);
        setContent(s1);
        setText("Anamation Sequencer");
		onTabReOpening();
	}

	@Override
	public void onTabReOpening() {
		// TODO Auto-generated method stub
		
	}

}
