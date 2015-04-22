package com.neuronrobotics.nrconsole.plugin.DyIO;

import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.dyio.DyIOChannel;
import com.neuronrobotics.sdk.dyio.DyIOChannelMode;
import com.neuronrobotics.sdk.dyio.IDyIOChannelModeChangeListener;

public class ChannelManager implements IDyIOChannelModeChangeListener   {
	public static final int ALIGNED_LEFT = 0;
	public static final int ALIGNED_RIGHT = 1;
	
	private DyIOChannel channel;
	private ControlPanel controlPanel;
	private ChannelPanel channelPanel;
	//private ChannelRecorder channelRecorder;
	private ArrayList<IChannelPanelListener> listeners = new ArrayList<IChannelPanelListener>();
	
	public ChannelManager(DyIOChannel c){
		channel = c;
		
		//channelRecorder = new ChannelRecorder(this);
		controlPanel = new ControlPanel(this);
		channelPanel = new ChannelPanel(this);
		
		
		channel.addChannelEventListener(controlPanel);
		channel.addChannelEventListener(channelPanel);

		onModeChange(channel.getMode());
		
		channel.addChannelModeChangeListener(this);
	}

	public void refresh() {
		//System.out.println(this.getClass()+" Refresh");
		//TODO this does not change the actual channel control planel type
		setMode(channel.getMode());
	}
	
	public void setActive(boolean active) {
		channelPanel.setSelected(active);
	}
	
	public ControlPanel getControlPanel() {
		return controlPanel;
	}

	public ChannelPanel getChannelPanel() {
		return channelPanel;
	}
	
//	public ChannelRecorder getChannelRecorder() {
//		return channelRecorder;
//	}
	
	public void displayError(String msg) {
		JOptionPane.showMessageDialog(null, msg, "Channel Error", JOptionPane.ERROR_MESSAGE);
	}
	
	public void fireOnClick(MouseEvent e) {
		int type = IChannelPanelListener.SINGLE_CLICK;
		
		if(e.isControlDown()) {
			type = IChannelPanelListener.CTRL_CLICK;
		}
		
		if(e.isShiftDown()) {
			type = IChannelPanelListener.SHIFT_CLICK;
		}
		
		for(IChannelPanelListener l : listeners) {
			l.onClick(this, type);
		}
	}
	
	public void fireOnModeChange() {
		for(IChannelPanelListener l : listeners) {
			l.onModeChange();
		}
	}
	
	public void fireOnRecordingEvent() {
		for(IChannelPanelListener l : listeners) {
			l.onRecordingEvent(this, controlPanel.isRecordingEnabled());
		}
	}
	
	public void addListener(IChannelPanelListener l) {
		if(listeners.contains(l)) {
			return;
		}
		
		listeners.add(l);
	}
	
	public DyIOChannel getChannel() {
		return channel;
	}

	public void setMode(DyIOChannelMode mode) {
		try{
			channel.setMode(mode);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	@Override
	public void onModeChange(DyIOChannelMode newMode) {
		Log.debug("Updating mode on channel: "+channel.getChannelNumber()+" mode: "+newMode);
		channelPanel.setMode(newMode);
		controlPanel.setUpModeUI(newMode);
	}


	public void onDyIOPowerEvent() {
		controlPanel.setupModesComboBox();
	}

	public void removeListener(IChannelPanelListener l) {
		if(listeners.contains(l)) {
			listeners.remove(l);
		}
	}

}
