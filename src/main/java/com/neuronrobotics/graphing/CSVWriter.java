package com.neuronrobotics.graphing;

import com.neuronrobotics.sdk.common.SDKInfo;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class CSVWriter {
    private CSVWriter() {
    }

    public static void WriteToCSV(ArrayList<GraphDataElement> dataTable, String filename) {
        String out = "";
        synchronized (dataTable) {
            for (GraphDataElement aDataTable : dataTable) {
                out += aDataTable.getTimestamp();
                for (int i = 0; i < aDataTable.getData().length; i++) {
                    out += "," + aDataTable.getData()[i];
                }
                out += "\r\n";
            }
        }

        try {
            // Create file
            FileWriter fstream = new FileWriter(filename);
            BufferedWriter outPut = new BufferedWriter(fstream);
            outPut.write(out);
            //Close the output stream
            outPut.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
        File dir1 = new File(".");

        try {
            String dir;
            if (SDKInfo.isWindows)
                dir = dir1.getCanonicalPath() + "\\";
            else
                dir = dir1.getCanonicalPath() + "/";
            JOptionPane.showMessageDialog(null, "Saved data to file: " + dir + filename, "PID Save", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ignored) {
        }
    }
}
