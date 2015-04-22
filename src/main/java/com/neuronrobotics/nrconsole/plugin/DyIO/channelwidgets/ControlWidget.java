package com.neuronrobotics.nrconsole.plugin.DyIO.channelwidgets;

import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import com.neuronrobotics.nrconsole.plugin.DyIO.ChannelManager;
import com.neuronrobotics.sdk.dyio.DyIOChannel;
import com.neuronrobotics.sdk.dyio.peripherals.DyIOAbstractPeripheral;

public abstract class ControlWidget extends JPanel {
	
	private static final long serialVersionUID = 1L;

	private Dimension size = new Dimension(300, 50);
	private ChannelManager manager;
	private boolean isRecordable = false;
	
	public ControlWidget(ChannelManager c) {
		manager = c;
		
		setLayout(new MigLayout());
		setPreferredSize(size);
	}
	
	public DyIOChannel getChannel() {
		return manager.getChannel();
	}
	
	public void pollValue() { }
	
	public void recordValue(int value) {
//		SwingUtilities.invokeLater(() -> {
//			manager.getChannelRecorder().recordValue(value);
//		});
		
	}
	
	public void recordValue(double value) {
//		SwingUtilities.invokeLater(() -> {
//			manager.getChannelRecorder().recordValue(value);
//		});
	}
	
	public void setRecordable(boolean enable) {
		isRecordable = enable;
	}
	
	public boolean isRecordable() {
		return isRecordable;
	}

	abstract public DyIOAbstractPeripheral getPerpheral();
	
}