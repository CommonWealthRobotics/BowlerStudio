package com.neuronrobotics.nrconsole.plugin.DyIO;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.neuronrobotics.nrconsole.util.NRConsoleDocumentationFactory;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.common.MACAddress;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.DyIOChannelMode;
import com.neuronrobotics.sdk.dyio.DyIOPowerEvent;
import com.neuronrobotics.sdk.dyio.DyIOPowerState;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class DyIOPanel extends JPanel {
	private DyIOPanel self = this;//used for NRConsoleDocumentationFactory.getDocumentationURL input
	private static final long serialVersionUID = 1L;
	private ImageIcon image;
	private JLabel voltage = new JLabel("Battery Voltage");
	private bankLED A = new bankLED ();
	private bankLED B = new bankLED ();
	private JButton refresh = new JButton("Refresh");
	private JButton reset = new JButton("Set Defaults");
	private JLabel mac = new JLabel("MAC: 00:00:00:00:00:00");
	private JLabel fw = new JLabel("FW Version: ?.?.?");
	private JButton fwInfo = new JButton("About FW...");
	private JCheckBox brownOutDetect = new JCheckBox("Safe Mode");
	private DyIO dyio;
	public DyIOPanel(DyIO dyio) {
		this.dyio = dyio;
		try{
			image = new ImageIcon(DyIOPanel.class.getResource("images/dyio-red2.png"));
		}catch (Exception e){
			e.printStackTrace();
			image = new ImageIcon(DyIOPanel.class.getResource("images/dyio.png"));
		}
	    initPanel();
	    setName("DyIO");
	}
	
	private void initPanel() {
		Dimension size = new Dimension(image.getIconWidth(), image.getIconHeight());
	    setSize(size);
	    setMaximumSize(size);
	    setMinimumSize(size);
	    setPreferredSize(size);
	    setLayout(new MigLayout());
	    refresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dyio.getBatteryVoltage(true);
			}
		});
	    reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dyio.killAllPidGroups();
				for(int i =0;i<24;i++){
					if(dyio.getMode(i) != DyIOChannelMode.DIGITAL_IN)
						dyio.setMode(i, DyIOChannelMode.DIGITAL_IN);
				}
			}
		});
	    fwInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					GettingStartedPanel.openPage(NRConsoleDocumentationFactory.getDocumentationURL(self));
				} catch (Exception e1) {
					
				}
			}
		});
	    int pr = Log.getMinimumPrintLevel();
	    //Log.enableInfoPrint();
	    //brownOutDetect.setSelected(dyio.isServoPowerSafeMode());
	    Log.setMinimumPrintLevel(pr);
	    brownOutDetect.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(brownOutDetect.isSelected()){
					System.out.println("Enabling DyIO Brown Out Detect");
					dyio.setServoPowerSafeMode(true);
				}else{
					System.out.println("Disabling DyIO Brown Out Detect");
					dyio.setServoPowerSafeMode(false);
				}
				dyio.fireDyIOEvent(new DyIOPowerEvent(	dyio.getBankAState(),
																		dyio.getBankBState(),
																		dyio.getBatteryVoltage(true)
																		)
				);
			}
		});
	    
	    int allignment = 220;
	    add(voltage, "pos "+allignment+" 82");
	    add(refresh, "pos "+allignment+" 102");
	    
	   
	    add(new JLabel("MAC:"), "pos "+allignment+" 130");
	    add(mac, "pos "+allignment+" 150");
	    add(fw, "pos "+allignment+" 175");
	    add(fwInfo, "pos "+allignment+" 200");
	    add(brownOutDetect, "pos "+allignment+" 465");
	    add(reset , "pos  "+allignment+" 490");
		int ledPos = 12*34+135;
		add(A, "pos 390 "+ledPos);
		add(B, "pos 155 "+ledPos);
		
		
	}
	private void setFw(byte[] f){
		fw.setText("FW Version: \n"+f[0]+"."+f[1]+"."+f[2]);
	}
	private void setMac(MACAddress m){
		mac.setText(m.toString());
	}
	
	public void addChannels(List<ChannelManager> list, boolean alignedLeft) {
		int index = 0;
		for(ChannelManager cp : list) {
			cp.getChannelPanel().setAlignedLeft(alignedLeft);
			int x = (alignedLeft ? 125 : 350);
			int y = ((alignedLeft ? 11 - (index % 12) : index % 12) * 34) + 130;
			
			JLabel channelLabel = new JLabel(new DecimalFormat("00").format(cp.getChannel().getChannelNumber()));
			channelLabel.setFont(new Font("Sans-Serif", Font.BOLD, 18));
			
			add(cp.getChannelPanel(), "pos " + x + " " + y);
			add(channelLabel, "pos " + ((alignedLeft ? - 25 : 82) + x) + " " + y);
			
			index++;
		}

	}
	private void setVoltage(double v){
		voltage.setText("Battery Voltage = "+new DecimalFormat("00.00").format(v));
	}

	@Override
	public void paintComponent (Graphics g) {
    	super.paintComponent(g);
    	try {
    		g.drawImage(image.getImage(), 0,0,this.getWidth(),this.getHeight(),this);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
	
	public void setPowerEvent(DyIOPowerEvent dyIOPowerEvent) {
		setVoltage(dyIOPowerEvent.getVoltage());
		A.setState(dyIOPowerEvent.getChannelAMode());
		B.setState(dyIOPowerEvent.getChannelBMode());
	    setMac(dyio.getAddress());
	    setFw(dyio.getFirmwareRev());
		repaint();
	}
	
	private class bankLED extends JPanel{
		/**
		 * 
		 */
		private static final long serialVersionUID = 3204367369543884223L;
		private DyIOPowerState state = DyIOPowerState.BATTERY_UNPOWERED;
		showOption thread;
		
		public void setState(DyIOPowerState s){
			state = s;
			if( thread==null) {
				 thread= new showOption();
				 thread.start();
			}
			thread.setState(s);
		}
		@Override
		public void paintComponent (Graphics g) {
	    	super.paintComponent(g);
	    	
	    	try {
	    		switch(state){
	    		case BATTERY_POWERED:
	    			g.setColor(Color.red);
	    			break;
	    		case BATTERY_UNPOWERED:
	    			g.setColor(Color.orange);
	    			break;
	    		case REGULATED:
	    			g.setColor(Color.green);
	    			break;
	    		}
	    		Graphics2D g2 = (Graphics2D)g;
	    		g2.fillRect(0, 0,75, 75);
	    	} catch (Exception e) {
	    		
	    	}
	    }
		
	}
	
	private class showOption extends Thread{
		DyIOPowerState old;
		DyIOPowerState state;
		boolean newState =false;
		public void setState(DyIOPowerState s){
			if(!dyio.isServoPowerSafeMode())
				return;
			if(!newState) {
				old = state;
			}
			state = s;
			if(old!=state)
				newState =true;
		}
		public void run(){
			setName("showOption DyIO thread");
			while(dyio.isAvailable()) {
				if(newState) {
					newState =false;
					if(old == DyIOPowerState.BATTERY_POWERED && state !=DyIOPowerState.BATTERY_POWERED){
						//JOptionPane.showMessageDialog(null, "WARNING!\nBattery needs to be charged or has been disconnected \nServos have been disabled for safety", "DyIO Power Warning", JOptionPane.WARNING_MESSAGE);
					}else 
					if(old != DyIOPowerState.BATTERY_POWERED && state ==DyIOPowerState.BATTERY_POWERED){
						//JOptionPane.showMessageDialog(null, "Battery is connected \nServos/Motors need to be re-enabled to start up", "DyIO Power Warning", JOptionPane.INFORMATION_MESSAGE);
					}
				}else {
					ThreadUtil.wait(5);
				}
			}
		}
	}

	public void setBrownOutMode(boolean servoPowerSafeMode) {
		// TODO Auto-generated method stub
		brownOutDetect.setSelected(servoPowerSafeMode);
	}

}
