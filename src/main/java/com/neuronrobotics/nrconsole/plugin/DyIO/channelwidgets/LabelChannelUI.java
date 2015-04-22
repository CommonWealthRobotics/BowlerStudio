package com.neuronrobotics.nrconsole.plugin.DyIO.channelwidgets;

import javax.swing.JLabel;

import com.neuronrobotics.nrconsole.plugin.DyIO.ChannelManager;
import com.neuronrobotics.sdk.dyio.peripherals.DyIOAbstractPeripheral;

public class LabelChannelUI extends ControlWidget {
	
	private static final long serialVersionUID = 1L;

	public LabelChannelUI(ChannelManager channel, String text) {
		super(channel);
		add(new JLabel(text));
	}

	@Override
	public DyIOAbstractPeripheral getPerpheral() {
		// TODO Auto-generated method stub
		return null;
	}
}
