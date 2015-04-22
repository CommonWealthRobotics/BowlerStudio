package com.neuronrobotics.nrconsole.plugin.DyIO.channelwidgets;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import com.neuronrobotics.nrconsole.plugin.DyIO.ChannelManager;
import com.neuronrobotics.nrconsole.plugin.DyIO.GettingStartedPanel;
import com.neuronrobotics.sdk.common.BowlerDocumentationFactory;
import com.neuronrobotics.sdk.dyio.peripherals.DigitalOutputChannel;
import com.neuronrobotics.sdk.dyio.peripherals.DyIOAbstractPeripheral;

public class DigitalOutputWidget extends ControlWidget implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	private boolean state;
	
	private JButton button = new JButton();
	
	private DigitalOutputChannel doc;
	
	public DigitalOutputWidget(ChannelManager channel) {
		super(channel);
		setRecordable(true);
		
		doc = new DigitalOutputChannel(getChannel());
		
		//Button to launch info page for Digital Output panel
		JButton helpButton = new JButton("Help");
		
		//Label for Digital Output Panel
		JLabel helpLabel = new JLabel("Digital Output Panel");
		add(helpLabel, "split 2, span 2, align left");
		add(helpButton, "gapleft 200, wrap, align right");
		helpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					GettingStartedPanel.openPage(BowlerDocumentationFactory.getDocumentationURL(doc));
				} catch (Exception exceptE) {}
			}
		});
		
		//Help button formating
		helpButton.setFont((helpButton.getFont()).deriveFont(8f));
		helpButton.setBackground(Color.green);
		
		//Digital Output Panel label formating
		helpLabel.setHorizontalTextPosition(JLabel.LEFT);
		helpLabel.setForeground(Color.GRAY);
		
		add(button);
		
		button.addActionListener(this);
		
		setValue(doc.isHigh());
	}
	
	private void setValue(boolean value) {
		state=value;
		if(value) {
			button.setText("High");
			recordValue(255);
		} else {
			button.setText("Low");
			recordValue(0);
		}
	}
	
	public void actionPerformed(ActionEvent e) { 
		if(doc.getChannel().getDevice().getCachedMode()){
			doc.getChannel().getDevice().setCachedMode(false);
		}
		
		if(doc.setHigh(!state)) {
			setValue(!state);
		}
	}
	
	public void pollValue() {
		setValue(doc.isHigh());
	}

	public DyIOAbstractPeripheral getPerpheral() {
		// TODO Auto-generated method stub
		return null;
	}
}
