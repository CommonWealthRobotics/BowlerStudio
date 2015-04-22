package com.neuronrobotics.nrconsole.plugin.DyIO.channelwidgets;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import com.neuronrobotics.nrconsole.plugin.DyIO.ChannelManager;
import com.neuronrobotics.nrconsole.plugin.DyIO.GettingStartedPanel;
import com.neuronrobotics.sdk.common.BowlerDocumentationFactory;
import com.neuronrobotics.sdk.common.ByteList;
import com.neuronrobotics.sdk.dyio.peripherals.DyIOAbstractPeripheral;
import com.neuronrobotics.sdk.dyio.peripherals.SPIChannel;

public class SPIChannelWidget extends ControlWidget {

	private static final long serialVersionUID = 1L;
	private JComboBox ss = new JComboBox();
	private ByteList dataStream = new ByteList();
	private JTextArea  rx = new JTextArea(5, 15);
	private JTextArea  tx = new JTextArea(5, 15);
	private JTextField addByte = new JTextField(3);
	private JButton send = new JButton("Send");
	private JButton clear = new JButton("Clear");
	private JButton addByteButton = new JButton("Add Byte");
	private SPIChannel spi;
	public SPIChannelWidget(ChannelManager c) {
		super(c);
		spi = new SPIChannel(getChannel().getDevice());
		
		ss.addItem(null);
		for(int i = 3; i < 24; i++) {
			ss.addItem(new Integer(i));
		}
		
		addByteButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				try {
					int toAdd = Integer.parseInt(addByte.getText());
					addByte.setText("");
					if (toAdd>255 || toAdd<0) {
						return;
					}
					dataStream.add(toAdd);
					tx.setText(dataStream.toString());
				}catch (NumberFormatException e) {
					
				}
				
			}	
		});
		
		clear.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				dataStream.clear();
				tx.setText("");
			}	
		});
		
		send.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				Integer s = (Integer)ss.getSelectedItem();
				if(s == null) {
					JOptionPane.showMessageDialog(null, "Please select  Chip Select pin", "Chip Select error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				byte [] back = spi.write(s.intValue(), dataStream.getBytes());
				
				rx.setText(new ByteList(back).toString());
			}	
		});
		
		//Button to launch info page for SPIChannel panel
		JButton helpButton = new JButton("Help");
		
		//Label for SPIChannel Panel
		JLabel helpLabel = new JLabel("SPI Panel");
		JPanel pan1 = new JPanel(new MigLayout()); 
		pan1.add(helpLabel, "split 2, span 2, align left");
		pan1.add(helpButton, "gapleft 150, wrap, align right");
		add(pan1,"wrap");
		helpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					GettingStartedPanel.openPage(BowlerDocumentationFactory.getDocumentationURL(spi));
				} catch (Exception exceptE) {}
			}
		});
		
		//Help button formating
		helpButton.setFont((helpButton.getFont()).deriveFont(8f));
		helpButton.setBackground(Color.green);
		
		//SPIChannel Panel label formating
		helpLabel.setHorizontalTextPosition(JLabel.LEFT);
		helpLabel.setForeground(Color.GRAY);
		JPanel pan = new JPanel(new MigLayout()); 
		
		pan.add(new JLabel("Tx: "));
		pan.add(tx);
		pan.add(send);
		pan.add(clear,"wrap");
		pan.add(new JLabel("Rx: "));
		pan.add(rx,"wrap");
		pan.add(new JLabel("Chip Select: "));
		pan.add(ss);
		pan.add(new JLabel("Add a byte: "));
		pan.add(addByte);
		pan.add(addByteButton);
		
		add(pan);
	}
	
	public DyIOAbstractPeripheral getPerpheral() {
		// TODO Auto-generated method stub
		return null;
	}

}
