package com.neuronrobotics.nrconsole.plugin.PID;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import net.miginfocom.swing.MigLayout;

import com.neuronrobotics.nrconsole.util.IntegerComboBox;
import com.neuronrobotics.sdk.dyio.DyIOChannelMode;
import com.neuronrobotics.sdk.dyio.DyIOPowerEvent;

import com.neuronrobotics.sdk.dyio.IDyIOEvent;
import com.neuronrobotics.sdk.dyio.IDyIOEventListener;
import com.neuronrobotics.sdk.dyio.dypid.DyPIDConfiguration;

public class DyPIDControlWidget extends JPanel implements IDyIOEventListener{
	private JButton  DypidSet = new JButton("Configure DyIO channels");
	private IntegerComboBox inChan = new IntegerComboBox(true,0xff);
	private IntegerComboBox outChan = new IntegerComboBox(true,0xff);
	private JComboBox inMode = new JComboBox();
	private JComboBox outMode = new JComboBox();
	/**
	 * long 
	 */
	private static final long serialVersionUID = 1L;
	//DyIO dyio;
	PIDControlWidget widgit;
	public DyPIDControlWidget(PIDControlWidget widg){
		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		setLayout(new MigLayout());
		widgit=widg;
		widgit.getGui().getDyio().addDyIOEventListener(this);
		initDyPID();

		setOpaque(false);
		add(new JLabel("Input"));
		add(inChan,"wrap");
		add(new JLabel("Mode"));
		add(inMode,"wrap");
		add(new JLabel("Output"));
		add(outChan,"wrap");
		add(new JLabel("Mode"));
		add(outMode,"wrap");
		add(DypidSet, "wrap");
		
		populateDyPID();
	}
	private void populateDyPID() {
		DyPIDConfiguration conf = widgit.getGui().getDyio().getDyPIDConfiguration(widgit.getGroup());
		
		if(conf.getInputChannel()==0xff)
			inChan.setNoneItemSelected();
		else
			inChan.setSelectedInteger(conf.getInputChannel());
		if(conf.getOutputChannel()==0xff)
			outChan.setNoneItemSelected();
		else
			outChan.setSelectedInteger(conf.getOutputChannel());
		
//		for(int i=0;i<inChan.getItemCount();i++){
//			Integer selected = (Integer)( inChan.getItemAt(i));
//			if(selected != null){
//				if(selected.intValue() == conf.getInputChannel()){
//					inChan.setSelectedItem(inChan.getItemAt(i));
//				}
//			}
//		}

//		for(int i=0;i<outChan.getItemCount();i++){
//			Integer selected = (Integer) outChan.getItemAt(i);
//			if(selected != null){
//				if(selected.intValue() == conf.getOutputChannel()){
//					outChan.setSelectedItem(outChan.getItemAt(i));
//				}
//			}
//		}
		
		if((conf.getOutputChannel() != 0xff) && (conf.getInputChannel() != 0xff )){
			for(int i=0;i<inMode.getItemCount();i++){
				DyIOChannelMode selected = (DyIOChannelMode)(inMode.getItemAt(i));
				if(selected != null){
					if(selected == conf.getInputMode()){
						inMode.setSelectedItem(inMode.getItemAt(i));
					}
				}
			}
			for(int i=0;i<outMode.getItemCount();i++){
				DyIOChannelMode selected = (DyIOChannelMode)(outMode.getItemAt(i));
				if(selected != null){
					if(selected == conf.getOutputMode()){
						outMode.setSelectedItem(outMode.getItemAt(i));
					}
				}
			}
			widgit.getPidSet().setEnabled(true);
//			if(conf.getInputMode() == DyIOChannelMode.ANALOG_IN){
//				widgit.setSetpoint(512);
//			}
		}
		
	}
	private void updateInChan(){
		inMode.removeAllItems();
		if(inChan.getSelectedItem()==null)
			return;
		//System.out.println("Input channel set to "+inChan.getSelectedItem() );
		int chan = inChan.getSelectedInteger();
		if(chan<24 && chan>0){
			Collection<DyIOChannelMode> m = getAvailableInputModes(widgit.getGui().getDyio().getChannel( chan ).getAvailableModes());
			for(DyIOChannelMode mode :m) {
				inMode.addItem(mode);
			}
			inMode.invalidate();
			inMode.repaint();
		}
	}
	private void updateOutChan(){
		outMode.removeAllItems();
		if(outChan.getSelectedItem()==null)
			return;
		//System.out.println("Output channel set to "+outChan.getSelectedItem() );
		int chan = outChan.getSelectedInteger();
		if(chan<24 && chan>0){
			Collection<DyIOChannelMode> m = getAvailableOutputModes(widgit.getGui().getDyio().getChannel(chan ).getAvailableModes());
			for(DyIOChannelMode mode : m) {
				outMode.addItem(mode);
			}
		}
	}
	private void initDyPID() {
//		inChan.addItem("None");
//		outChan.addItem("None");
		for(int i=0;i<24;i++) {
			inChan.addInteger(i);
			outChan.addInteger(i);
		}
		inChan.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateInChan();
			}
		});
		outChan.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				updateOutChan();
			}
		});
		

		
		widgit.getPidStop().setEnabled(false);
		widgit.getPidSet().setEnabled(false);
		DypidSet.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				if(		inChan.getSelectedItem()!=null &&
						outChan.getSelectedItem()!=null) {
					widgit.getPidSet().setEnabled(true);
					DyIOChannelMode o = (DyIOChannelMode) outMode.getSelectedItem();
					if(o==null)
						o=DyIOChannelMode.OFF;
					DyIOChannelMode i = (DyIOChannelMode) inMode.getSelectedItem();
					if(i==null)
						i=DyIOChannelMode.OFF;
					DyPIDConfiguration config = new DyPIDConfiguration(	widgit.getGroup(),
																		inChan.getSelectedInteger(), 
																		i,
																		outChan.getSelectedInteger(),
																		o);
					widgit.getGui().getDyio().ConfigureDynamicPIDChannels(config);
					widgit.stopPID(true);
				}else {
					JOptionPane.showMessageDialog(null, "DyIO Channel/Modes are not all set", "DyPID ERROR", JOptionPane.ERROR_MESSAGE);
					widgit.stopPID(true);
					widgit.getPidStop().setEnabled(false);
					widgit.getPidSet().setEnabled(false);
				}
			}
		});
		
		
	}
	
	public void setInMode(JComboBox inMode) {
		this.inMode = inMode;
	}
	public JComboBox getInMode() {
		return inMode;
	}
	public void setOutMode(JComboBox outMode) {
		this.outMode = outMode;
	}
	public JComboBox getOutMode() {
		return outMode;
	}
	public Collection<DyIOChannelMode> getAvailableInputModes(Collection<DyIOChannelMode> m ){
		Collection<DyIOChannelMode> back = new ArrayList<DyIOChannelMode>();
		for(DyIOChannelMode mode: m) {
			switch(mode) {
			case ANALOG_IN:
			case COUNT_IN_INT:
			case DIGITAL_IN:
				back.add(mode);
				break;
			default:
				break;
			}
		}
		return back;
	}
	public Collection<DyIOChannelMode> getAvailableOutputModes(Collection<DyIOChannelMode> m ){
		Collection<DyIOChannelMode> back = new ArrayList<DyIOChannelMode>();
		for(DyIOChannelMode mode: m) {
			switch(mode) {
			case SERVO_OUT:
			case DC_MOTOR_VEL:
			case PWM_OUT:
			case DIGITAL_OUT:
				back.add(mode);
				break;
			default:
				break;
			}
		}
		return back;
	}
	
	public void onDyIOEvent(IDyIOEvent e) {
		if(e.getClass() == DyIOPowerEvent.class){
			updateInChan();
			updateOutChan();
		}
		
	}
}
