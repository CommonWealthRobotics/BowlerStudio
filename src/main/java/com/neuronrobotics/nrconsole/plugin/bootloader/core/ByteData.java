package com.neuronrobotics.nrconsole.plugin.bootloader.core;

import java.util.ArrayList;

public class ByteData {
	private long address;
	private ArrayList<Byte> dataBytes = new ArrayList<Byte>();
	public ByteData(long address){
		this.setAddress(address);
	}

	private void setAddress(long address) {
		this.address = address;
	}

	public long getStartAddress() {
		return address;
	}
	public long getEndAddress() {
		return address+dataBytes.size();
	}

	public void setData(byte data) {
		dataBytes.add(new Byte(data));
	}

	public byte [] getData() {
		byte [] b = new byte[dataBytes.size()];
		int i=0;
		for (Byte bld:dataBytes){
			b[i++]=bld.byteValue();
		}
		return b;
	}
	
	public String toString(){
		return "Address: "+address+" Number of bytes:" + dataBytes.size()+" Data: "+dataBytes;
	}
}
