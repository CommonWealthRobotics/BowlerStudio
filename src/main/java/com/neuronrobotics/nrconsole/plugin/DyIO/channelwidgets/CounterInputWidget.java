package com.neuronrobotics.nrconsole.plugin.DyIO.channelwidgets;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import com.neuronrobotics.nrconsole.plugin.DyIO.ChannelManager;
import com.neuronrobotics.nrconsole.plugin.DyIO.GettingStartedPanel;
import com.neuronrobotics.sdk.common.BowlerDocumentationFactory;
import com.neuronrobotics.sdk.dyio.DyIOChannelMode;
import com.neuronrobotics.sdk.dyio.peripherals.CounterInputChannel;
import com.neuronrobotics.sdk.dyio.peripherals.DyIOAbstractPeripheral;
import com.neuronrobotics.sdk.dyio.peripherals.ICounterInputListener;

public class CounterInputWidget extends ControlWidget implements ICounterInputListener,ActionListener{
	
	private static final long serialVersionUID = 1L;
	CounterInputChannel ci;
	private JTextField field = new JTextField();
	private JButton refresh = new JButton("Refresh");
	private JCheckBox async = new JCheckBox("Async");
	
	public CounterInputWidget(ChannelManager c, DyIOChannelMode mode) {
		super(c);
		setRecordable(true);
		ci = new CounterInputChannel(getChannel(),true);
		field.setColumns(10);
		field.setEnabled(false);
		
		//Button to launch info page for Counter Input panel
		JButton helpButton = new JButton("Help");
		
		//Label for Counter Input Panel
		JLabel helpLabel = new JLabel("Encoder/Counter input Panel");
		JPanel pan1 = new JPanel(new MigLayout()); 
		pan1.add(helpLabel, "split 2, span 2, align left");
		pan1.add(helpButton, "gapleft 150, wrap, align right");
		add(pan1,"wrap");
		helpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					GettingStartedPanel.openPage(BowlerDocumentationFactory.getDocumentationURL(ci));
				} catch (Exception exceptE) {}
			}
		});
		
		//Help button formating
		helpButton.setFont((helpButton.getFont()).deriveFont(8f));
		helpButton.setBackground(Color.green);
		
		//Digital Input Panel label formating
		helpLabel.setHorizontalTextPosition(JLabel.LEFT);
		helpLabel.setForeground(Color.GRAY);

		add(field);
		add(refresh);
		
		ci.addCounterInputListener(this);
		refresh.addActionListener(this);
		async.addActionListener(this);
		
		setValue(0);
		ci.addCounterInputListener(this);
	}
	private void setValue(int value) {
		recordValue(value);
		field.setText(new Integer(value).toString());
	}
	
	public void onCounterValueChange(CounterInputChannel source, int value) {
		setValue(value);
		async.setSelected(true);
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == field) { 
			
		}else if(e.getSource() == refresh) {
			setValue(ci.getValue());
		}else if(e.getSource() == async) {
			if(!async.isSelected()) {
				ci.setAsync(false);
				ci.removeCounterInputListener(this);
			} else {
				ci.addCounterInputListener(this);
				ci.setAsync(true);
			}
		}
		
	}
	
	public DyIOAbstractPeripheral getPerpheral() {
		// TODO Auto-generated method stub
		return ci;
	}

}
