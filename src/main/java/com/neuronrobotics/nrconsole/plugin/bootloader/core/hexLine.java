package com.neuronrobotics.nrconsole.plugin.bootloader.core;
//import java.util.LinkedList;
//import java.util.Queue;
import java.util.ArrayList;

public class hexLine {
	private ArrayList<Byte> dataBytes = new ArrayList<Byte>();
	private int address;
	private int byteCount;
	private int recordType;
	private int checkSum;
	private boolean hasSetHighAddress=false;
	
	public int getCheckSum() {
		return checkSum;
	}

	public hexLine(String s) throws Exception{
		char data[] = s.toCharArray();
		if ((data.length<11)||data[0]!=':')
			throw new Exception("This line is not a hex line");
		
		char[] bc={data[1],data[2]};
		byteCount = Integer.parseInt(new String(bc), 16); 
		
		char[] ad={data[3],data[4],data[5],data[6]};
		address = Integer.parseInt(new String(ad), 16); 
		
		char[] rt={data[7],data[8]};
		recordType = Integer.parseInt(new String(rt), 16); 
		
		char[] cs={data[data.length-2],data[data.length-1]};
		checkSum = Integer.parseInt(new String(cs), 16); 
		
		for (int i=0;i<byteCount;i++){
			char[] d={data[9+(i*2)],data[9+1+(i*2)]};
			Byte b =new Byte((byte) Integer.parseInt(new String(d), 16));
			dataBytes.add(b);
		}
		
	}

	public int getStartAddress() {
		return address;
	}
	
	public int getEndAddress() {
		return address+dataBytes.size();
	}
	
	public void setHighAddress(long highAddress){
		if(!hasSetHighAddress){
			hasSetHighAddress=true;
			address+=highAddress;
		}
	}

	public int getByteCount() {
		return byteCount;
	}

	public int getRecordType() {
		return recordType;
	}
	
	public byte[] getDataBytes() { 
		if (dataBytes.size()==0) 
			return null;
		return dataToArray(dataBytes);
	}
	
	public Boolean hasData(){
		if (dataBytes==null) return false;
		return true;
	}
	
	private byte [] dataToArray( ArrayList<Byte> bl){
		byte [] b = new byte[bl.size()];
		int i=0;
		for (Byte bld:bl){
			b[i++]=bld.byteValue();
		}
		return b;
	}
	
	public String toString(){
		String s="";
		if (getRecordType()==0){
			s+="Address = "+getStartAddress();
			s+=" Data: [";
			for (byte b: getDataBytes()){
				s+=b+",";
			}
			s+="]";
		}else if (getRecordType()==4){
			s="High Address Set: ";
		}
		return s;
	}
	
}
