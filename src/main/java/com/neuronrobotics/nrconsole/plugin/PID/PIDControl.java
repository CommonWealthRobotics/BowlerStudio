package com.neuronrobotics.nrconsole.plugin.PID;

import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingNode;
import javafx.scene.control.ScrollPane;

import javax.swing.JPanel;

import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.namespace.bcs.pid.IExtendedPIDControl;
import com.neuronrobotics.sdk.namespace.bcs.pid.IPidControlNamespace;

public class PIDControl extends AbstractBowlerStudioTab {
	//private DyIO dyio;
	private IPidControlNamespace pid;
	private PIDControlGui gui;
	//private JButton display = new JButton("Display PID configuration");
	//private JScrollPane scrollPanel = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
	private JFXPanel holder =  new JFXPanel();

	public IPidControlNamespace getPidDevice() {
		return pid;
	}



	@Override
	public String[] getMyNameSpaces() {
		// TODO Auto-generated method stub
		return new String []{"bcs.pid.*"};
	}

	@Override
	public void initializeUI(BowlerAbstractDevice pm) {
		if(gui == null){
			pid = (IPidControlNamespace)pm;
			gui = new PIDControlGui(pid);
			
			SwingNode sn = new SwingNode();
	        sn.setContent(gui);
	        ScrollPane s1 = new ScrollPane();
	       
	        s1.setContent(sn);
	        setContent(s1);
	        setText("P.I.D. Closed-Loop");
			onTabReOpening();
		}
	}
	
	@Override
	public void onTabClosing() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabReOpening() {
		// TODO Auto-generated method stub
		
	}

}
