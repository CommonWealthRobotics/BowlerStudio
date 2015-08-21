package com.neuronrobotics.nrconsole.plugin.DyIO;

import java.util.ArrayList;

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ScrollPane;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.IConnectionEventListener;
import com.neuronrobotics.sdk.common.IDeviceConnectionEventListener;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.DyIOChannel;
import com.neuronrobotics.sdk.dyio.DyIOFirmwareOutOfDateException;
import com.neuronrobotics.sdk.dyio.DyIOPowerEvent;
import com.neuronrobotics.sdk.dyio.IDyIOEvent;
import com.neuronrobotics.sdk.dyio.IDyIOEventListener;

public class DyIOConsole extends AbstractBowlerStudioTab implements IChannelPanelListener,IDyIOEventListener , IConnectionEventListener, IDeviceConnectionEventListener  {
	private DyIOPanel devicePanel =null;
	private DyIOControlsPanel deviceControls=null;
	private ArrayList<ChannelManager> channels =null;
	//private HexapodConfigPanel hex=null;
	//private JFrame hexFrame;
	private SwingNode wrapper;
	private DyIO dyio;
	
	//set this variable to make this tab auto open when a device is connected

	private void setupDyIO(){
		
		int index = 0;
		ArrayList<DyIOChannel> chans =(ArrayList<DyIOChannel>) dyio.getChannels();
		Log.debug("DyIO state: "+dyio+ " \nchans: "+chans );
		for(DyIOChannel c : chans) {
			//System.out.println(this.getClass()+" Adding channel: "+index+" as mode: "+c.getMode());
			ChannelManager cm = new ChannelManager(c);
			cm.addListener(this);
			if(index == 0) {
				selectChannel(cm);
			}
			channels.add(cm);
			index++;


		}
		dyio.getBatteryVoltage(true);
		getDeviceDisplay().setBrownOutMode(dyio.isServoPowerSafeMode());
		//System.out.println(this.getClass()+" setupDyIO: "+ channels.size());
		getDeviceDisplay().addChannels(channels.subList(00, 12), false);
		getDeviceDisplay().addChannels(channels.subList(12, 24), true);
	    
	}

	
	public void selectChannel(ChannelManager cm) {
		cm.getControlPanel();
	}

	public DyIOPanel getDeviceDisplay() {
		if(devicePanel == null)
			devicePanel  = new DyIOPanel(dyio);
		devicePanel.invalidate();
		return devicePanel;
	}
	
	public DyIOControlsPanel getDeviceControls() {
		deviceControls.invalidate();
		return deviceControls;
	}
	
	
	public void onClick(ChannelManager source, int type) {
		switch(type) {
		case SINGLE_CLICK:
			for(ChannelManager cm: channels) {
				cm.setActive(false);
			}
			deviceControls.setChannel(source.getControlPanel());
			break;
		case SHIFT_CLICK:
		case CTRL_CLICK:
			deviceControls.addChannel(source.getControlPanel());
			break;
		}
		source.setActive(true);
	}

	
	public void onModeChange() {
		for(ChannelManager cm : channels) {
			cm.refresh();
		}
	}

	
	public void onDyIOEvent(IDyIOEvent e) {
		if(e.getClass() == DyIOPowerEvent.class){
			//System.out.println("Got power event: "+e);
			getDeviceDisplay().setPowerEvent(((DyIOPowerEvent)e));
			try{
				for(ChannelManager cm : channels) {
					cm.onDyIOPowerEvent();
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}


	@Override
	public void onDisconnect(BowlerAbstractConnection source) {
	
	}


	@Override
	public void onConnect(BowlerAbstractConnection source) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onRecordingEvent(ChannelManager source, boolean enabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabClosing() {
		
		dyio.removeDyIOEventListener(this);
		for(ChannelManager c : channels) {
			c.removeListener(this);
		}
	}

	@Override
	public String[] getMyNameSpaces() {
		return new String[]{"neuronrobotics.dyio.*"};
	}

	@Override
	public void initializeUI(BowlerAbstractDevice pm) {
		this.dyio = (DyIO)pm;
		deviceControls = new DyIOControlsPanel();
		channels = new ArrayList<ChannelManager>();

		DyIO.disableFWCheck();
		try {
			DyIO.enableFWCheck();
			dyio.checkFirmwareRev();
		}catch(DyIOFirmwareOutOfDateException ex) {
//			try {
//				GettingStartedPanel.openPage("http://wiki.neuronrobotics.com/NR_Console_Update_Firmware");
//			} catch (Exception e) {
//			}
//			JOptionPane.showMessageDialog(null, "DyIO Firmware mis-match Warning\n"+ex.getMessage(), "DyIO Warning", JOptionPane.WARNING_MESSAGE);
		}
		
		setupDyIO();
		dyio.setMuteResyncOnModeChange(false);
		JPanel jp = new JPanel(new MigLayout());
		jp.add(getDeviceDisplay(), "pos 5 5");
		jp.add(getDeviceControls(), "pos 560 5");
		jp.setBorder(BorderFactory.createLoweredBevelBorder());
		onTabReOpening();
		Platform.runLater(() -> {
			wrapper = new SwingNode();
			wrapper.setContent(jp);
		    ScrollPane s1 = new ScrollPane();
		    s1.setContent(wrapper);
		    setContent(s1);
			setText(pm.getScriptingName()+" Console");
		});

	}
	
	


	@Override
	public void onTabReOpening() {
		dyio.addDyIOEventListener(this);
		dyio.addConnectionEventListener(this);
		for(ChannelManager c : channels) {
			c.addListener(this);
		}
		dyio.getBatteryVoltage(true);
	}


	@Override
	public void onDisconnect(BowlerAbstractDevice source) {
		onDisconnect(source.getConnection());
	}


	@Override
	public void onConnect(BowlerAbstractDevice source) {
		// TODO Auto-generated method stub
		onConnect(source.getConnection());
	}


}
