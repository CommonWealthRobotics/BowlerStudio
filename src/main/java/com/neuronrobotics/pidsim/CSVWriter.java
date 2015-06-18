package com.neuronrobotics.pidsim;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

public class CSVWriter {

	private BufferedWriter writer;
	
	public void setFile(File f) {
		if (!f.getName().endsWith(".csv")){
		    f = new File(f.getAbsolutePath()+".csv");
		}

		FileWriter fstream;
		try {
			fstream = new FileWriter(f.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		writer = new BufferedWriter(fstream);
	}

	public void addData(XYSeries data) {
		
		XYSeries cache;
		try {
			cache = data.createCopy(0, data.getItemCount() - 1);
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		try {
			writer.write("Time (s),Position (degrees)\n");
			for(Object o : cache.getItems()) {
				XYDataItem i = (XYDataItem) o;
				writer.write(i.getXValue() + "," + i.getYValue() + "\n");
			}
		} catch (IOException e) {

		}

	}

	public void cleanup() {
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
