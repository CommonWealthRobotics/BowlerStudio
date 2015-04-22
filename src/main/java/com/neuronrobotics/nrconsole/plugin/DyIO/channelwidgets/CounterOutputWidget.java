package com.neuronrobotics.nrconsole.plugin.DyIO.channelwidgets;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import com.neuronrobotics.nrconsole.plugin.DyIO.ChannelManager;
import com.neuronrobotics.nrconsole.plugin.DyIO.GettingStartedPanel;
import com.neuronrobotics.sdk.common.BowlerDocumentationFactory;
import com.neuronrobotics.sdk.dyio.DyIOChannelMode;
import com.neuronrobotics.sdk.dyio.peripherals.CounterOutputChannel;
import com.neuronrobotics.sdk.dyio.peripherals.DyIOAbstractPeripheral;
import com.neuronrobotics.sdk.dyio.peripherals.ICounterOutputListener;

public class CounterOutputWidget extends ControlWidget implements ActionListener,ICounterOutputListener{
	
	private static final long serialVersionUID = 1L;
	CounterOutputChannel outChannel;
	private JTextField field = new JTextField();
	private JButton set = new JButton("Set");
	private JButton refresh = new JButton("Refresh");
	
	public CounterOutputWidget(ChannelManager c, DyIOChannelMode mode){
		super(c);
		setRecordable(true);
		outChannel = new CounterOutputChannel(getChannel());
		field.setColumns(10);
		
		//Button to launch info page for Counter Output panel
		JButton helpButton = new JButton("Help");
		
		//Label for Counter Output Panel
		JLabel helpLabel = new JLabel("Stepper/Counter Panel");
		JPanel pan1 = new JPanel(new MigLayout()); 
		pan1.add(helpLabel, "split 2, span 2, align left");
		pan1.add(helpButton, "gapleft 150, wrap, align right");
		add(pan1,"wrap");
		helpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					GettingStartedPanel.openPage(BowlerDocumentationFactory.getDocumentationURL(outChannel));
				} catch (Exception exceptE) {}
			}
		});
		
		//Help button formating
		helpButton.setFont((helpButton.getFont()).deriveFont(8f));
		helpButton.setBackground(Color.green);
		
		//Counter Output Panel label formating
		helpLabel.setHorizontalTextPosition(JLabel.LEFT);
		helpLabel.setForeground(Color.GRAY);
		JPanel pan = new JPanel(new MigLayout()); 
		
		pan.add(field);
		pan.add(set);
		add(pan);
		
		field.setEnabled(true);
		set.addActionListener(this);
		setValue(outChannel.getValue());
		outChannel.addCounterOutputListener(this);
	}
	private void setValue(int value) {
		
		recordValue(value);
		field.setText(new Integer(value).toString());
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == field) { 
			
		}else if(e.getSource()==set){
			try{
				int out = new Integer(field.getText()).intValue();
				if(outChannel.getChannel().getDevice().getCachedMode()){
					outChannel.getChannel().getDevice().setCachedMode(false);
				}
				outChannel.SetPosition(out);
			}catch(Exception e1){
				field.setText("0");
			}
		}else if(e.getSource() == refresh) {
			setValue(outChannel.getValue());
		}
		
	}
	
	public void pollValue() {
		
	}
	
	public void onCounterValueChange(CounterOutputChannel source, int value) {
		setValue(value);
	}
	
	public DyIOAbstractPeripheral getPerpheral() {
		// TODO Auto-generated method stub
		return outChannel;
	}
}
