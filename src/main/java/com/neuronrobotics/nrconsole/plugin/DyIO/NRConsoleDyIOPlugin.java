package com.neuronrobotics.nrconsole.plugin.DyIO;

import java.util.ArrayList;

import javafx.embed.swing.SwingNode;
import javafx.event.Event;
import javafx.event.EventHandler;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.common.IConnectionEventListener;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.DyIOChannel;
import com.neuronrobotics.sdk.dyio.DyIOFirmwareOutOfDateException;
import com.neuronrobotics.sdk.dyio.DyIOPowerEvent;
import com.neuronrobotics.sdk.dyio.IDyIOEvent;
import com.neuronrobotics.sdk.dyio.IDyIOEventListener;

public class NRConsoleDyIOPlugin extends AbstractBowlerStudioTab implements IChannelPanelListener,IDyIOEventListener , IConnectionEventListener  {
	private DyIOPanel devicePanel =null;
	private DyIOControlsPanel deviceControls = new DyIOControlsPanel();
	private ArrayList<ChannelManager> channels = new ArrayList<ChannelManager>();
	//private HexapodConfigPanel hex=null;
	//private JFrame hexFrame;
	private SwingNode wrapper;
	private DyIO dyio;
	public NRConsoleDyIOPlugin(DyIO dyio) {
		super(new String[]{"neuronrobotics.dyio.*"},dyio);
		this.dyio = dyio;
		dyio.addConnectionEventListener(this);

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
		DyIO.disableFWCheck();
		
		dyio.addDyIOEventListener(this);
		dyio.setMuteResyncOnModeChange(true);
		setupDyIO();
		dyio.setMuteResyncOnModeChange(false);
		dyio.getBatteryVoltage(true);

		wrapper = new SwingNode();
		JPanel jp = new JPanel(new MigLayout());

		jp.add(getDeviceDisplay(), "pos 5 5");
		jp.add(getDeviceControls(), "pos 560 5");
		jp.setBorder(BorderFactory.createLoweredBevelBorder());
		wrapper.setContent(jp);
		setContent(wrapper);
		setText("DyIO Console");
		
	}

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
		return devicePanel;
	}
	
	public DyIOControlsPanel getDeviceControls() {
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
			System.out.println("Got power event: "+e);
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
		ArrayList<DyIOChannel> chans =(ArrayList<DyIOChannel>) dyio.getChannels();
		for(DyIOChannel c : chans) {
			c.removeAllChannelEventListeners();
		}
	}


}
