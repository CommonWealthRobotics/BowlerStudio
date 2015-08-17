package com.neuronrobotics.nrconsole.plugin.bootloader.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.filechooser.FileFilter;

import net.miginfocom.swing.MigLayout;

import com.neuronrobotics.sdk.bootloader.Hexml;
import com.neuronrobotics.sdk.bootloader.NRBoot;
import com.neuronrobotics.sdk.common.InvalidResponseException;
import com.neuronrobotics.sdk.common.NoConnectionAvailableException;

public class NR_Bootloader_GUI implements ActionListener {
	private NRBoot blApp;
	
	private JButton fileButton,loadButton;
	private JFrame frame;
	private Hexml hex=null;
	private JProgressBar progress = new JProgressBar();
	private StatusLabel fileStatus = new StatusLabel();
	private StatusLabel portStatus = new StatusLabel();
	private StatusLabel loadStatus = new StatusLabel();
	
	private static final long serialVersionUID = 1L;
	private boolean portSelect=false;
	private String revision;
	File file = null;
	
	public NR_Bootloader_GUI(){
		////System.out.println("Starting GUI");
		
		fileButton = new JButton();
		fileButton.addActionListener(this);
		resetFile();
		
//		portButton = new JButton();
//		portButton.addActionListener(this);
		resetPort();
		
		loadButton = new JButton();
		loadButton.addActionListener(this);
		resetLoad();
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new MigLayout());
        buttonPanel.add(fileButton, "wrap");
//        buttonPanel.add(portButton, "wrap");
        buttonPanel.add(loadButton, "wrap");
        buttonPanel.add(new JLabel("Progress:"), "wrap");
        buttonPanel.add(progress,"wrap");
        
        frame = new JFrame("NR Bootloader");
        frame.add(buttonPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
	}


	public void actionPerformed(ActionEvent e) {
        //Handle open button action.
        if (e.getSource() == fileButton) {
        	loadFile();
        }
//        if (e.getSource() == portButton) {
//        	////System.out.println("Go for port selection");
//        	setPortSelect(false);
//        	try {
//				Thread.sleep(50);
//			} catch (InterruptedException e1) {}
//        	setPortSelect(true);
//        	try{
//        		getBlApp().getDevice().disconnect();
//        	}catch(Exception ex){}
//        	resetLoad();
//        	
//        }
        if (e.getSource() == loadButton) {
        	try{
	        	if (getBlApp()!=null){
	        		////System.out.println("Loading firmware");
	        		reloadFile();
	        		getBlApp().loadCores(hex.getCores());
		    		loadButton.setText("Loading....");
		    		loadButton.setEnabled(false);
		    		LoaderChecker l = new LoaderChecker();
		    		l.start();
	        	}	
        	}catch(InvalidResponseException ex){
        		ex.printStackTrace();
        		String message = "Device is not in bootloader mode!";
        		JOptionPane.showMessageDialog(null, message, message, JOptionPane.ERROR_MESSAGE);
        	}catch(NoConnectionAvailableException ex){
        		ex.printStackTrace();
        		String message = "Device is not no longer connected to bootloader";
        		JOptionPane.showMessageDialog(null, message, message, JOptionPane.ERROR_MESSAGE);
        		resetPort();
        		resetLoad();
        	}
        }
	}

	public void setPortSelect(boolean portSelect) {
		this.portSelect = portSelect;
	}

	public boolean isPortSelect() {
		return portSelect;
	}
	
	public void setBlApp(NRBoot blApp) {
		this.blApp = blApp;
//		portButton.setText("Using:"+blApp.getDevice().getConnection().toString());
    	frame.pack();
		loadButton.setEnabled(true);
		portStatus.setStatus(StatusLabel.OK);
	}

	public NRBoot getBlApp() {
		return blApp;
	}
	
	public void resetFile() {
		fileButton.setText("Select a NR Firmware File..");
	}
	
	public void resetPort() {
//		portButton.setEnabled(true);
//		portButton.setText("Select Port");
		portStatus.setStatus(0);
		this.portSelect=false;
	}
	
	public void resetLoad() {
		loadButton.setEnabled(false);
		loadButton.setText("Load Firmware");
		loadStatus.setStatus(0);
		reloadFile();
	}
	
	public void loadFile() {
		//System.out.println("Loading file:");
    	JFileChooser fc = new JFileChooser();
    	File dir1 = new File (".");
    	File dir2=null; 
    	try {
    		dir2 = new File (System.getProperty("user.home")+"/git/dyio/FirmwarePublish/Dev/");
    	}catch(Exception ex) {
    		ex.printStackTrace();
    	}
    	if(file!=null){
    		fc.setSelectedFile(file);
    		System.out.println("Starting with: "+file.getAbsolutePath());
    	}else{
    		if(dir2 == null) {
    			fc.setCurrentDirectory(dir1);
    			System.out.println("Starting in: "+dir1.getAbsolutePath());
    		}
    		else {
    			System.out.println("Starting in: "+dir2.getAbsolutePath());
    			fc.setCurrentDirectory(dir2);
    		}
    	}
    	fc.setFileFilter(new FileFilter() {
			
			@Override
			public String getDescription() {
				return "NR Firmware Format (xml)";
			}
			@Override
			public boolean accept(File f) {
				if(f.isDirectory()) {
					return true;
				}
				
				String path = f.getAbsolutePath().toLowerCase();
				if ((path.endsWith("xml") && (path.charAt(path.length() - 3)) == '.')) {
					return true;
				}
				
				return f.getName().matches(".+\\.xml$");
			}
		});
    	
        int returnVal = fc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
            if(!file.getName().matches(".+\\.xml$")){
            	String message = "Invalid file type. Must be .xml";
            	JOptionPane.showMessageDialog(null, message, message, JOptionPane.ERROR_MESSAGE);
            	fileStatus.setStatus(StatusLabel.ERROR);
            	resetFile();
            	resetLoad();
            	resetPort();
            	return;
            }
            
            reloadFile();
        }
	}
	private void reloadFile(){
		if(file!=null){
        	try {
        		hex = new Hexml(file);
        	}catch (Exception e) {
        		e.printStackTrace();
        		String message = "Invalid xml file";
        		JOptionPane.showMessageDialog(null, message, message, JOptionPane.ERROR_MESSAGE);
        		return;
        	}
        	fileButton.setText("Using: "+file.getName());
        	frame.pack();
            revision = hex.getRevision();
//            portButton.setEnabled(true);
            fileStatus.setStatus(StatusLabel.OK);
		}
	}
	
	public String getRevision() {
		return revision;
	}
	private void resetAll() {
		getBlApp().getDevice().disconnect();
		resetPort();
		resetLoad();
	}
	public class LoaderChecker extends Thread{
		public void run() {
			progress.setMaximum(getBlApp().getProgressMax());
			progress.setMinimum(0);
			//progress.setIndeterminate(true);
			while(getBlApp().isLoadDone() == false) {
				try {Thread.sleep(100);} catch (InterruptedException e) {}
				progress.setValue(getBlApp().getProgressValue());
			}
			progress.setIndeterminate(false);
			String message = "Success! Your Bowler device is now updated to version: "+hex.getRevision();
    		JOptionPane.showMessageDialog(null, message, message, JOptionPane.INFORMATION_MESSAGE);
    		resetAll();
		}
	}
}
