package com.neuronrobotics.nrconsole.plugin.bootloader.gui;

import com.neuronrobotics.sdk.bootloader.NRBootCoreType;


public class BootloaderParams {
	private int core;
	private String hexFilePath;
	private NRBootCoreType type;
	public String toString(){
		String s;
		s="Core #"+getCore()+" of type:"+getType().getReadableName()+" Using file:"+getHexFilePath();
		return s;
	}
	public void setCore(int core) {
		this.core = core;
	}
	public int getCore() {
		return core;
	}
	public void setHexFilePath(String hexFilePath) {
		this.hexFilePath = hexFilePath;
	}
	public String getHexFilePath() {
		return hexFilePath;
	}
	public void setType(NRBootCoreType type) {
		this.type = type;
	}
	public NRBootCoreType getType() {
		return type;
	}
}
