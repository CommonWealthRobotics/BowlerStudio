package com.neuronrobotics.nrconsole.plugin.DyIO;


public interface IChannelPanelListener {
	public static final int SINGLE_CLICK = 0;
	public static final int SHIFT_CLICK = 1;
	public static final int CTRL_CLICK = 2;
	public void onClick(ChannelManager source, int type);
	public void onModeChange();
	public void onRecordingEvent(ChannelManager source, boolean enabled);
}
