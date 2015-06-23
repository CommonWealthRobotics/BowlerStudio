package com.neuronrobotics.nrconsole.plugin.DyIO.channelwidgets;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import com.neuronrobotics.nrconsole.plugin.DyIO.ChannelManager;
import com.neuronrobotics.nrconsole.plugin.DyIO.GettingStartedPanel;
import com.neuronrobotics.sdk.common.BowlerDocumentationFactory;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.dyio.DyIOChannelMode;
import com.neuronrobotics.sdk.dyio.peripherals.DyIOAbstractPeripheral;
import com.neuronrobotics.sdk.dyio.peripherals.IServoPositionUpdateListener;
import com.neuronrobotics.sdk.dyio.peripherals.ServoChannel;

public class ServoWidget extends ControlWidget implements ChangeListener, ActionListener, IServoPositionUpdateListener {
	private static final long serialVersionUID = 1L;
	
	private JSlider sliderUI = new JSlider();
	private JSlider speed = new JSlider();
	private JLabel valueUI = new JLabel();
	private JLabel timeUI = new JLabel("0.00s");
	private JCheckBox liveUpdate = new JCheckBox("Live");
	private JButton save = new JButton("Set");
	
	private ServoChannel sc;
	private boolean startup = true;
	private int saveValue = 256;
	public ServoWidget(ChannelManager channel, DyIOChannelMode mode) {
		super(channel);
		setRecordable(true);
		try{
			sc = new ServoChannel(getChannel());
			sc.addIServoPositionUpdateListener(this);
		}catch (Exception e){
			return;
		}
		saveValue = sc.getConfiguration();
		setLayout(new MigLayout());

		speed.setMaximum(0);
		speed.setMaximum(5000);
		speed.setMajorTickSpacing(1000);
		speed.setPaintTicks(true);
		speed.setValue(0);
		speed.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent arg0) {
				timeUI.setText(String.format("%1.2f s", ((float)(speed.getValue()))/1000.0));
			}
		});
		
		sliderUI.setMaximum(0);
		sliderUI.setMaximum(255);
		sliderUI.setMajorTickSpacing(15);
		sliderUI.setPaintTicks(true);
		
		//Button to launch info page for Servo Panel
		JButton helpButton = new JButton("Help");
		
		//Label for Servo Panel
		JLabel helpLabel = new JLabel("Servo Panel");
		add(helpLabel, "split 2, span 2, align left");
		add(helpButton, "gapleft 200, wrap, align right");
		helpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					GettingStartedPanel.openPage(BowlerDocumentationFactory.getDocumentationURL(sc));
				} catch (Exception exceptE) {}
			}
		});
		
		//Help button formating
		helpButton.setFont((helpButton.getFont()).deriveFont(8f));
		helpButton.setBackground(Color.green);
		
		//Servo Panel label formating
		helpLabel.setHorizontalTextPosition(JLabel.LEFT);
		helpLabel.setForeground(Color.GRAY);
		
		JPanel pan = new JPanel(new MigLayout()); 
		pan.add(new JLabel("Set Speed"), "wrap");
		pan.add(new JLabel("Max"));
		pan.add(speed);
		pan.add(timeUI, "wrap");
		pan.add(new JLabel("Value"));
		pan.add(sliderUI);
		pan.add(valueUI);
		pan.add(liveUpdate, "wrap");
		pan.add(new JLabel("Set Default"));
		pan.add(save);
		add(pan);
		int val = getChannel().getValue();
		setValue(val);
		valueUI.setText(formatValue(val));
		liveUpdate.setSelected(true);
		
		sliderUI.addChangeListener(this);
		save.addActionListener(this);
		startup = false;
	}
	
	private String formatValue(int value) {
		return String.format("%03d", value & 0x000000ff);
	}

	private void setValue(int value) {
		if(value < 0) {
			value = 0;
		}
		
		if(value > 255) {
			value = 255;
		}
		
		pollValue();
		
		sliderUI.setValue(value);
		//valueUI.setText(formatValue(value));
	}

	public void stateChanged(ChangeEvent e) {
		//valueUI.setText(formatValue(sliderUI.getValue()));
		
		if((!liveUpdate.isSelected() && sliderUI.getValueIsAdjusting()) ||(sliderUI.getValueIsAdjusting() && speed.getValue()>0)) {
			return;
		}
		try{
			pollValue();
		}catch (Exception ex){
			ex.printStackTrace();
		}
		
		
		
		if(sliderUI.getValue() !=saveValue )
			save.setEnabled(true);
		else
			save.setEnabled(false);
		
		if( startup == false ) {
			sc.SetPosition(sliderUI.getValue(),((float)(speed.getValue()))/1000);
			if(sc.getChannel().getCachedMode()){
				sc.getChannel().flush();
			}
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == save){
			saveValue=sliderUI.getValue();
			sc.SavePosition(saveValue);
			save.setEnabled(false);
		}
	}
	
	public void pollValue() {
		//recordValue(sliderUI.getValue());
	}

	public DyIOAbstractPeripheral getPerpheral() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onServoPositionUpdate(ServoChannel srv, int position,double time) {
		if(srv == sc){
			//Log.warning("Changing the servo from async "+srv.getChannel().getChannelNumber()+" to val: "+position);
			sliderUI.removeChangeListener(this);
			sliderUI.setValue(position);
			valueUI.setText(formatValue(position));
			recordValue(position);
			sliderUI.addChangeListener(this);
		}
	}
}
