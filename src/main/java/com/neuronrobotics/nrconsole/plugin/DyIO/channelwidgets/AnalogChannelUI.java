package com.neuronrobotics.nrconsole.plugin.DyIO.channelwidgets;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import net.miginfocom.swing.MigLayout;

import com.neuronrobotics.nrconsole.plugin.DyIO.ChannelManager;
import com.neuronrobotics.nrconsole.plugin.DyIO.GettingStartedPanel;
import com.neuronrobotics.sdk.common.BowlerDocumentationFactory;
import com.neuronrobotics.sdk.dyio.peripherals.AnalogInputChannel;
import com.neuronrobotics.sdk.dyio.peripherals.DyIOAbstractPeripheral;
import com.neuronrobotics.sdk.dyio.peripherals.IAnalogInputListener;

public class AnalogChannelUI extends ControlWidget implements IAnalogInputListener, ActionListener {
	private static final long serialVersionUID = 1L;
	
	private JSlider sliderUI = new JSlider();
	private JPanel vals = new JPanel(new MigLayout());
	private JLabel valueUI = new JLabel();
	private JLabel valueUIADC = new JLabel();
	private JButton refresh = new JButton("Update");
	private JCheckBox async = new JCheckBox("Async");
	
	private AnalogInputChannel aic;
	
	public AnalogChannelUI(ChannelManager channel) {
		super(channel);
		setRecordable(true);
		
		aic = new AnalogInputChannel(getChannel(), true);
		
		sliderUI.setMaximum(0);
		sliderUI.setMaximum(1024);
		sliderUI.setMajorTickSpacing(100);
		sliderUI.setPaintTicks(true);
		sliderUI.setEnabled(false);
		
		//Button to launch info page for Digital Input panel
		JButton helpButton = new JButton("Help");
		
		//Label for Digital Input Panel
		JLabel helpLabel = new JLabel("Analog Input Panel");
		add(helpLabel, "split 2, span 2, align left");
		add(helpButton, "gapleft 200, wrap, align right");
		helpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					GettingStartedPanel.openPage(BowlerDocumentationFactory.getDocumentationURL(aic));
				} catch (Exception exceptE) {}
			}
		});
		
		//Help button formating
		helpButton.setFont((helpButton.getFont()).deriveFont(8f));
		helpButton.setBackground(Color.green);
		
		//Digital Input Panel label formating
		helpLabel.setHorizontalTextPosition(JLabel.LEFT);
		helpLabel.setForeground(Color.GRAY);
		
		JPanel pan = new JPanel(new MigLayout()); 
		
		pan.add(sliderUI);
		vals.add(valueUI,"wrap");
		vals.add(valueUIADC,"wrap");
		pan.add(vals);
		pan.add(refresh);
		add(pan);

		aic.addAnalogInputListener(this);
		refresh.addActionListener(this);
		async.addActionListener(this);
		
		pollValue();
		
		aic.addAnalogInputListener(this);
	}

	private String formatValue(double value) {
		return new DecimalFormat("00.00").format(value)+" V";
	}

	private void setValue(double value) {
		sliderUI.setValue((int) (value));
		valueUI.setText(formatValue( value/1024*4.9));
		valueUIADC.setText(new Integer((int) value).toString()+" ADC");
		recordValue(value );
	}

	public void onAnalogValueChange(AnalogInputChannel channel,double value) {
		setValue(value);
		async.setSelected(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		setValue(aic.getScaledValue());
		if(e.getSource() == refresh) {
			setValue(aic.getValue());
		}else if(e.getSource() == async) {
			if(!async.isSelected()) {
				aic.setAsync(false);
				aic.removeAnalogInputListener(this);
			} else {
				aic.setAsync(true);
				aic.addAnalogInputListener(this);
			}
		}
	}
	
	public void pollValue() {
		if(!async.isSelected())
			setValue(aic.getValue());
	}

	public DyIOAbstractPeripheral getPerpheral() {
		return aic;
	}

}
