package com.neuronrobotics.nrconsole.plugin.DyIO;


public enum ChannelPanelStatus {
	DEFAULT("images/channel-default.png"),
	SELECTED("images/channel-selected.png"),
	HIGHLIGHT("images/channel-highlight.png"),
	UPDATE("images/channel-update.png");
	private String imagePath;
	
	private ChannelPanelStatus(String path) {
		imagePath = path;
	}
	
	public String getPath() {
		return imagePath;
	}
}
