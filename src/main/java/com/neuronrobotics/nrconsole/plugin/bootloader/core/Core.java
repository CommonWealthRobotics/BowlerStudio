package com.neuronrobotics.nrconsole.plugin.bootloader.core;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Core {
	private int index;
	private NRBootCoreType type;
	private ArrayList<hexLine> lines = new ArrayList<hexLine>();
	
	public Core(int core,ArrayList<hexLine> lines,NRBootCoreType type){
		setIndex(core);
		this.setType(type);
		this.lines=lines;
	}
	
	public Core(int core,String file, NRBootCoreType type){
		setIndex(core);
		this.setType(type);
		try {
			ArrayList<hexLine> tmp = new ArrayList<hexLine>();
			 // Get the object of DataInputStream
		    DataInputStream in = new DataInputStream(new FileInputStream(file));
		    BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    String strLine;
		    while ((strLine = br.readLine()) != null)   {
		        // Print the content on the console
		        try {
					tmp.add(new hexLine(strLine));
				} catch (Exception e) {
					System.err.println("This is not a valid hex file");
				}
		     }
		     //Close the input stream
		     in.close();
			setLines(tmp);
		}catch (Exception e) {
			////System.out.println("File not found!!");
		}
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public void setType(NRBootCoreType type) {
		this.type = type;
	}

	public NRBootCoreType getType() {
		return type;
	}

	public void setLines(ArrayList<hexLine> lines) {
		this.lines = lines;
	}

	public ArrayList<hexLine> getLines() {
		return lines;
	}
	@Override
	public String toString() {
		return "Core: "+index+" of type: "+type; 
	}
}
