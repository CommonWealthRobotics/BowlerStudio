package com.neuronrobotics.nrconsole.util;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;


public class FileSelectionFactory {
	public static File GetFile(File start, FileFilter... filter) {
		JFileChooser fc =new JFileChooser();
    	File dir1 = new File (".");
    	if(start!=null){
    		if(start.isDirectory())
    			fc.setCurrentDirectory(start);
    		else
    			fc.setSelectedFile(start);
    	}else{
    		fc.setCurrentDirectory(dir1);
    	}
    	if(filter!=null)
	    	for (FileFilter fileFilter : filter) {
	    		fc.setAcceptAllFileFilterUsed(false);
	    		fc.addChoosableFileFilter(fileFilter);
			}
    	 fc.setDialogTitle("Select a file");
        int returnVal = fc.showDialog(null, "Open");
       
        if (returnVal == JFileChooser.APPROVE_OPTION) {
        	return fc.getSelectedFile();
        }
        return null;
	}
	public static File GetFile(File start,String title, String btnText, FileFilter... filter) {
		JFileChooser fc =new JFileChooser();
    	File dir1 = new File (".");
    	if(start!=null){
    		fc.setSelectedFile(start);
    	}else{
    		fc.setCurrentDirectory(dir1);
    	}
    	for (FileFilter fileFilter : filter) {
    		fc.setAcceptAllFileFilterUsed(false);
    		fc.addChoosableFileFilter(fileFilter);
		}
    	 fc.setDialogTitle(title);
        int returnVal = fc.showDialog(null, btnText);
       
        if (returnVal == JFileChooser.APPROVE_OPTION) {
        	return fc.getSelectedFile();
        }
        return null;
	}
}
