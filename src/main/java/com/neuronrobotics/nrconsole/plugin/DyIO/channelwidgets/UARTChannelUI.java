package com.neuronrobotics.nrconsole.plugin.DyIO.channelwidgets;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import com.neuronrobotics.nrconsole.plugin.DyIO.ChannelManager;
import com.neuronrobotics.nrconsole.plugin.DyIO.GettingStartedPanel;
import com.neuronrobotics.sdk.common.BowlerDocumentationFactory;
import com.neuronrobotics.sdk.common.ByteList;
import com.neuronrobotics.sdk.dyio.DyIOChannelEvent;
import com.neuronrobotics.sdk.dyio.DyIOChannelMode;
import com.neuronrobotics.sdk.dyio.peripherals.DyIOAbstractPeripheral;
import com.neuronrobotics.sdk.dyio.peripherals.IUARTStreamListener;
import com.neuronrobotics.sdk.dyio.peripherals.UARTChannel;

public class UARTChannelUI extends ControlWidget implements ActionListener,IUARTStreamListener{

	private static final long serialVersionUID = 1L;
	private JTextField transmit = new JTextField(20);
	private JTextArea  receive = new JTextArea(5, 15);
	private JButton send = new JButton("Send");
	private JButton clear = new JButton("Clear");
	private UARTChannel uart;
	private int baud = 19200;
	private JComboBox baudrates = new JComboBox();
	
	public UARTChannelUI(ChannelManager channel, DyIOChannelMode mode){
		super(channel);
		
		baudrates.addItem(  2400);
		baudrates.addItem(  4800);
		baudrates.addItem(  9600);
		baudrates.addItem( 14400);
		baudrates.addItem( 19200);
		baudrates.addItem( 28800);
		baudrates.addItem( 38400);
		baudrates.addItem( 57600);
		baudrates.addItem( 76800);
		baudrates.addItem(115200);
		baudrates.addItem(230400);
		for(int i=0;i<11;i++) {
			Integer in = (Integer)baudrates.getItemAt(i);
			if(in.intValue() == baud) {
				baudrates.setSelectedIndex(i);
			}
		}
		baudrates.addActionListener(this);
		
		uart = new UARTChannel(getChannel().getDevice());
		uart.setUARTBaudrate(baud);
		receive.setSize(40, 20);
		
		send.addActionListener(this);
		if(mode ==  DyIOChannelMode.USART_RX)
			uart.addUARTStreamListener(this);
		clear.addActionListener(this);
		
		//Button to launch info page for UART panel
		JButton helpButton = new JButton("Help");
		
		//Label for UART Panel
		JLabel helpLabel = new JLabel("UART Serial Panel");
		JPanel pan1 = new JPanel(new MigLayout()); 
		pan1.add(helpLabel, "split 2, span 2, align left");
		pan1.add(helpButton, "gapleft 150, wrap, align right");
		add(pan1,"wrap");
		helpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					GettingStartedPanel.openPage(BowlerDocumentationFactory.getDocumentationURL(uart));
				} catch (Exception exceptE) {}
			}
		});
		
		//Help button formating
		helpButton.setFont((helpButton.getFont()).deriveFont(8f));
		helpButton.setBackground(Color.green);
		
		//UART Panel label formating
		helpLabel.setHorizontalTextPosition(JLabel.LEFT);
		helpLabel.setForeground(Color.GRAY);
		JPanel pan = new JPanel(new MigLayout()); 
		
		if(mode ==  DyIOChannelMode.USART_TX) {
			pan.add(transmit);
			pan.add(send, "wrap");
			pan.add(baudrates,"wrap");
		}else {
			pan.add(clear);
			pan.add(baudrates,"wrap");
			pan.add(receive, "wrap");
			
		}
		add(pan);
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == send) { 
			try {
				String send =transmit.getText();
				transmit.setText("");
				uart.sendBytes(new ByteList(send));
			} catch (IOException e1) {
				transmit.setText("Send FAILED!");
			}
			transmit.setText("");
		}
		if(e.getSource() == clear) { 
			transmit.setText("");
			receive.setText("");
		}
		if(e.getSource() == baudrates) {
			Integer in = (Integer)baudrates.getSelectedItem();
			baud = in.intValue();
			uart.setUARTBaudrate(baud);
		}
		
	}
	
	public void onChannelEvent(DyIOChannelEvent event) {
		String s = receive.getText();
		try {
			String got = new ByteList(uart.getBytes()).toString();
			s+="\n"+got;
		} catch (Exception e) {}
		receive.setText(s);
	}

	public DyIOAbstractPeripheral getPerpheral() {
		// TODO Auto-generated method stub
		return null;
	}
}
