package com.neuronrobotics.graphing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.neuronrobotics.sdk.common.SDKInfo;

public class CSVWriter {
	public static void WriteToCSV(ArrayList<GraphDataElement> dataTable,String filename) {
		String out = "";
		synchronized(dataTable){
			for(int j =0;j< dataTable.size();j++) {
				out+=dataTable.get(j).getTimestamp();
				for (int i=0;i<dataTable.get(j).getData().length;i++) {
					out+=","+dataTable.get(j).getData()[i];
				}
				out+="\r\n";
			}
		}

		try{
			// Create file 
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter outPut = new BufferedWriter(fstream);
			outPut.write(out);
			//Close the output stream
			outPut.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		File dir1 = new File (".");

		try {
			String dir;
			if(SDKInfo.isWindows)
				dir=dir1.getCanonicalPath()+"\\";
			else
				dir=dir1.getCanonicalPath()+"/";
			JOptionPane.showMessageDialog(null, "Saved data to file: "+dir+filename, "PID Save", JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException e) {
		}
	}
}
