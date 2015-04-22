package com.neuronrobotics.nrconsole.plugin.bootloader.gui;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import com.neuronrobotics.nrconsole.plugin.bootloader.BootloaderPanel;

public class StatusLabel extends JLabel {
	private static final long serialVersionUID = 1L;
	public static final int OK = 1;
	public static final int ERROR = 2;
	
	
	public StatusLabel() {
		setStatus(0);
	}
	
	public void setStatus(int status) {
		switch (status) {
		case OK:
			setIcon(createImageIcon("images/ok.png"));
			break;
		case ERROR:
			setIcon(createImageIcon("images/error.png"));
			break;
		default:
			setIcon(createImageIcon("images/blank.png"));
			break;
		}
		
		invalidate();
		//repaint();
	}
	
	protected ImageIcon createImageIcon(String path) {
	    java.net.URL imgURL = BootloaderPanel.class.getResource(path);
	    if (imgURL != null) {
	        return new ImageIcon(imgURL);
	    } else {
	        System.err.println("Couldn't find file: " + path);
	        return null;
	    }
	}
}
