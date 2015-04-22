/**
 * 
 */
package com.neuronrobotics.nrconsole.plugin.bootloader.core;

import java.io.IOException;
import java.util.ArrayList;

import com.neuronrobotics.sdk.common.ByteList;

/**
 * @author hephaestus
 *
 */
public class IntelHexParser {
	private long highAddress=0;
	ArrayList<ByteData> packetList = new ArrayList<ByteData>();
	private long dataIndex=0; 
	private long base = 0x1D00A000L;
	private long head =  0x1D01FFFFL;
	
	public static String hex(long n) {
	    // call toUpperCase() if that's required
	    return String.format("0x%8s", Long.toHexString(n)).replace(' ', '0');
	}
	
	private void checkAddressValidity(long currentAddress, NRBootCoreType type){
		if(type==NRBootCoreType.PIC32){
			if(currentAddress>head ){
				throw new RuntimeException("Address "+hex(currentAddress)+" is larger than "+hex(head));
			}
			if(currentAddress<base){
				throw new RuntimeException("Address "+hex(currentAddress)+" is less than "+hex(base));
			}	
		}
		
	}
	
	public IntelHexParser(ArrayList<hexLine> lines, NRBootCoreType type) throws IOException{
	     ByteData tmp=null;
	     hexLine  previousLine=null;
	     for (hexLine l : lines){
	    	 long currentAddress;
	    	 long endOfLastAddress = 0;
	    	 
	    	 if (l.getRecordType()==4){
	    		 byte[] haddr=l.getDataBytes();
	    		 highAddress = ByteList.convertToInt(haddr, false)*65536;
	    		 ////System.out.println("High Address :" + highAddress);
	    	 } if (l.getRecordType()==0){
	    		 
	    		 l.setHighAddress(highAddress);
	    		 ////System.out.println(l);
	    		 
	    		 currentAddress=l.getStartAddress();
	    		 checkAddressValidity(currentAddress,type);
	    		 if (previousLine != null){
	    			 endOfLastAddress = previousLine.getEndAddress();
	    		 }
	    		 boolean isSequential = (currentAddress==(endOfLastAddress));
	    		 if(tmp==null){
		    		 tmp =  new ByteData(currentAddress);
		    	 }
	    		 if(type==NRBootCoreType.PIC32){
	    			 if(!isSequential||(tmp.getData().length > 50)){
			    		 packetList.add(tmp);
			    		 tmp =  new ByteData(currentAddress);
			    	 } 
	    		 }
	    		 for (byte b : l.getDataBytes()){
	    			 if(type == NRBootCoreType.AVRxx4p){
	    				 if(!isSequential||(tmp.getData().length == 128)){
				    		 packetList.add(tmp);
				    		 tmp =  new ByteData(currentAddress);
				    	 }
	    			 }
	    			 tmp.setData(b);
	    			 
	    			 currentAddress++;

		    		 checkAddressValidity(currentAddress,type);
	    		 }
	    		 previousLine=l;
	    	 }
	    	 
	    
	     }
	     if (tmp.getData().length!=0){
	    	 packetList.add(tmp); 
	     }
	    
	}
	public int size(){
		return packetList.size();
	}
	public ByteData getNext(){
		if (dataIndex < packetList.size()){
			return packetList.get((int) dataIndex++);
		}
		return null;
	}
	
}
