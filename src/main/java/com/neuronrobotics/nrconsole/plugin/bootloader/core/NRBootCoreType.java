package com.neuronrobotics.nrconsole.plugin.bootloader.core;

public enum NRBootCoreType {
	AVRxx4p		(2, "avr_atmegaXX4p"),
	PIC32       (4, "pic32mx440f128h");
	
	private int bytesPerWord;
	private String readableName;
	
	private NRBootCoreType(int bytesPerWord, String name) {
		this.setBytesPerWord(bytesPerWord);
		setReadableName(name);
	}

	public void setBytesPerWord(int bytesPerWord) {
		this.bytesPerWord = bytesPerWord;
	}

	public int getBytesPerWord() {
		return bytesPerWord;
	}

	public void setReadableName(String readableName) {
		this.readableName = readableName;
	}

	public String getReadableName() {
		return readableName;
	}

	public static NRBootCoreType find(String tagValue) {
		if (NRBootCoreType.AVRxx4p.getReadableName().toLowerCase().contentEquals(tagValue.toLowerCase())){
			return NRBootCoreType.AVRxx4p;
		}
		if (NRBootCoreType.PIC32.getReadableName().toLowerCase().contentEquals(tagValue.toLowerCase())){
			return NRBootCoreType.PIC32;
		}
		return null;
	}
	@Override
	public String toString() {
		return getReadableName();
	}
}
