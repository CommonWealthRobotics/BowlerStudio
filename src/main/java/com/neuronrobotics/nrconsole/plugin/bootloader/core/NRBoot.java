package com.neuronrobotics.nrconsole.plugin.bootloader.core;


import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.serial.SerialConnection;

public class NRBoot {
	private NRBootLoader boot;
	private CoreLoader loader;
	private int progressMax=0;
	private int progressValue=0;
	public NRBoot(BowlerAbstractConnection ac){
		try {
			boot=new NRBootLoader(ac);
		}catch(RuntimeException e) {
			//e.printStackTrace();
			String message = "Not a bootloader device";
    		//JOptionPane.showMessageDialog(null, message, message, JOptionPane.ERROR_MESSAGE);
    		throw e;
		}
		//System.out.println("Connection to bowler device ready");
	}
	
	public NRBoot(String serialPort){
		this.boot=new NRBootLoader(new SerialConnection(serialPort));
		boot.connect();
		if (boot.ping()){
			//System.out.println("Connection to bowler device ready");
			return;
		}
		//System.out.println("Not a Bowler Device");
		boot.disconnect();
		boot=null;
	}
	
	public boolean load(Core core) {
		
		String id = getDevice().getBootloaderID();
		if (id==null){
			System.err.println("Device is not a bootloader");
			return false;
		}else if (id.contains(core.getType().getReadableName())) {
			//System.out.println("Bootloader ID:"+core.getType().getReadableName());
		}else{
			System.err.println("##core is Invalid##\nExpected:"+core.getType().getReadableName()+" got: "+id);
			return false;
		}
		
		IntelHexParser parse = getParser(core);
		if(parse==null)
			return false;
		send(parse,core.getIndex());
		return true;
	}
	private IntelHexParser getParser(Core core) {
		try {
			return new IntelHexParser(core.getLines(),core.getType());
		} catch (IOException e) {
			return null;
		}
	}
	
	private void send(IntelHexParser parse,int core){
		boot.erase(core);
		//System.out.println("Writing to flash");
		int printLine=0;
		ByteData line = parse.getNext();
		while (line != null){
	
			if(!boot.write(core, line)){
				//System.out.println("Failed to write, is the device in bootloader mode?");
				return;
			}

			line = parse.getNext();
			//System.out.print(".");
			progressValue++;
			printLine++;
			if (printLine>100){
				printLine=0;
				//System.out.print("\n");
			}
		}
		//System.out.print("\n");
	}
	
	public void reset(){
		try{
			boot.reset();
		}catch(Exception e){
			boot.disconnect();
		}
	}

	public NRBootLoader getDevice() {
		return boot;
	}
	
	public void loadCores(ArrayList<Core> cores) {
		loader = new CoreLoader(cores);
		loader.start();
	}
	public boolean isLoadDone() {
		return loader.isDone;
	}
	private class CoreLoader extends Thread{
		ArrayList<Core> cores;
		public boolean isDone=false;
		public int val=0;
		public CoreLoader(ArrayList<Core> cores){
			this.cores=cores;
			progressMax=0;
			for (Core b:cores){
				progressMax += getParser(b).size();
			}
			progressValue=0;
		}
		public void run() {
			try {
				for (Core b:cores){
					val++;
					load(b);
				}
				reset();
			}catch(Exception e) {
				e.printStackTrace();
				String message = "This device is not a bootloader";
        		JOptionPane.showMessageDialog(null, message, message, JOptionPane.ERROR_MESSAGE);
			}
			progressValue=0;
			isDone=true;
		}
	}

	public int getProgressMax() {
		// TODO Auto-generated method stub
		return progressMax;
	}
	public int getProgressValue() {
		return progressValue;
	}

}
