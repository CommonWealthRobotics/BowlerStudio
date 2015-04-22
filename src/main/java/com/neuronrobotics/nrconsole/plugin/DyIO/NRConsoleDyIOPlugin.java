package com.neuronrobotics.nrconsole.plugin.DyIO;

import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.common.IConnectionEventListener;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.DyIOChannel;
import com.neuronrobotics.sdk.dyio.DyIOFirmwareOutOfDateException;
import com.neuronrobotics.sdk.dyio.DyIOPowerEvent;
import com.neuronrobotics.sdk.dyio.DyIORegestry;
import com.neuronrobotics.sdk.dyio.IDyIOEvent;
import com.neuronrobotics.sdk.dyio.IDyIOEventListener;

public class NRConsoleDyIOPlugin implements IChannelPanelListener,IDyIOEventListener , IConnectionEventListener  {
	private boolean active=false;
	private DyIOPanel devicePanel =null;
	private DyIOControlsPanel deviceControls = new DyIOControlsPanel();
	private ArrayList<ChannelManager> channels = new ArrayList<ChannelManager>();
	//private HexapodConfigPanel hex=null;
	//private JFrame hexFrame;
	private JPanel wrapper;
	public NRConsoleDyIOPlugin() {
		DyIORegestry.addConnectionEventListener(this);
		//hex = new HexapodNRConsolePulgin();
	}
	
	
	public JPanel getTabPane() {
		wrapper = new JPanel(new MigLayout()){
			/**
			 * 
			 */
			private static final long serialVersionUID = -5581797073561156394L;

			
			public void repaint(){
				super.repaint();
				getDeviceDisplay().repaint();
				getDeviceControls().repaint();
			}
		};
		
		wrapper.add(getDeviceDisplay(), "pos 5 5");
		wrapper.add(getDeviceControls(), "pos 560 5");
		wrapper.setName("DyIO");
		wrapper.setBorder(BorderFactory.createLoweredBevelBorder());
		return wrapper;
	}

	
	public boolean isMyNamespace(ArrayList<String> names) {
		for(String s:names){
			if(s.contains("neuronrobotics.dyio.*")){
				active=true;
			}
		}
		return isAcvive();
	}

	private boolean setUp = false;
	public boolean setConnection(BowlerAbstractConnection connection){
		//System.err.println(this.getClass()+" setConnection");
		if(setUp)
			return true;
		DyIO.disableFWCheck();
		
		DyIORegestry.setConnection(connection);
		DyIORegestry.get().connect();
		try {
			DyIO.enableFWCheck();
			DyIORegestry.get().checkFirmwareRev();
		}catch(DyIOFirmwareOutOfDateException ex) {
//			try {
//				GettingStartedPanel.openPage("http://wiki.neuronrobotics.com/NR_Console_Update_Firmware");
//			} catch (Exception e) {
//			}
//			JOptionPane.showMessageDialog(null, "DyIO Firmware mis-match Warning\n"+ex.getMessage(), "DyIO Warning", JOptionPane.WARNING_MESSAGE);
		}
		DyIO.disableFWCheck();
		
		DyIORegestry.get().addDyIOEventListener(this);
		DyIORegestry.get().setMuteResyncOnModeChange(true);
		setupDyIO();
		DyIORegestry.get().setMuteResyncOnModeChange(false);
		DyIORegestry.get().getBatteryVoltage(true);
		setUp = true;

		return true;
	}
	private void setupDyIO(){
		
		int index = 0;
		ArrayList<DyIOChannel> chans =(ArrayList<DyIOChannel>) DyIORegestry.get().getChannels();
		Log.debug("DyIO state: "+DyIORegestry.get()+ " \nchans: "+chans );
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
		DyIORegestry.get().getBatteryVoltage(true);
		getDeviceDisplay().setBrownOutMode(DyIORegestry.get().isServoPowerSafeMode());
		//System.out.println(this.getClass()+" setupDyIO: "+ channels.size());
		getDeviceDisplay().addChannels(channels.subList(00, 12), false);
		getDeviceDisplay().addChannels(channels.subList(12, 24), true);
	    
	}

	
	public boolean isAcvive() {
		// TODO Auto-generated method stub
		return active;
	}
	
	
	public void selectChannel(ChannelManager cm) {
		cm.getControlPanel();
	}

	public DyIOPanel getDeviceDisplay() {
		if(devicePanel == null)
			devicePanel  = new DyIOPanel();
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


}
