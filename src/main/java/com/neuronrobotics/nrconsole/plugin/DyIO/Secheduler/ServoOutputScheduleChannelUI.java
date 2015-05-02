package com.neuronrobotics.nrconsole.plugin.DyIO.Secheduler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import com.neuronrobotics.nrconsole.util.IntegerComboBox;
import com.neuronrobotics.sdk.dyio.peripherals.IServoPositionUpdateListener;
import com.neuronrobotics.sdk.dyio.peripherals.ServoChannel;
import com.neuronrobotics.sdk.dyio.sequencer.CoreScheduler;
import com.neuronrobotics.sdk.dyio.sequencer.ISchedulerListener;
import com.neuronrobotics.sdk.dyio.sequencer.ServoOutputScheduleChannel;

public class ServoOutputScheduleChannelUI extends JPanel implements IServoPositionUpdateListener,ActionListener,ISchedulerListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7112414698561768276L;
	private ServoOutputScheduleChannel channel;
	//private JCheckBox record = new JCheckBox("Record");
	//private JButton startRecording = new JButton("Start Recording");
	//private JButton startTest = new JButton("Start Test");
	private JSlider position  = new JSlider();
	private JCheckBox useSlider = new JCheckBox("Record");
	private JPanel recordConfig = new JPanel();
	IntegerComboBox inputChannelNumber;
	private JTextField scale = new JTextField(5);
	private JTextField zero = new JTextField(5);
	
	private double currentScale=.25;
	
	private int currentZero = 512;

	ChangeListener posListener = new ChangeListener() {
		
		@Override
		public void stateChanged(ChangeEvent e) {
			//System.out.println("Pos listener");
			
			if(useSlider.isSelected() ){
				flush();
				if(!getCb().isPlaying())
					channel.flush();
			}else{
				//System.out.println("Not flushing");
			}
			
		}
	};

	private CoreScheduler cb;	
	
	public void flush(){
		channel.setCurrentTargetValue(position.getValue());
		
	}
	public ServoOutputScheduleChannelUI(ServoOutputScheduleChannel chan, CoreScheduler cb){
		
		this.setCb(cb);
		chan.addIServoPositionUpdateListener(this);
		setChannel(chan);
		setLayout(new MigLayout());
		inputChannelNumber=new IntegerComboBox();
		for(int i=8;i<16;i++){
			inputChannelNumber.addInteger(i);
		}
		setBorder(BorderFactory.createLoweredBevelBorder());
//		record.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent arg0) {
//				if(record.isSelected()){
//					recordConfig.setVisible(true);
//				}else{
//					recordConfig.setVisible(false);
//					pause();
//				}
//			}
//		});
//		
//		startRecording.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent arg0) {
//				if(!getChannel().isRecording()){
//					resume();
//				}
//				else{
//					pause();
//				}
//			}
//		});
//		startTest.addActionListener(new ActionListener() {
//			
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				if(getChannel().isTesting()) {
//					stopTest();
//				}else
//					startTest();
//			}
//		});
		
		useSlider.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(useSlider.isSelected()){
					position.setEnabled(true);
					getChannel().setRecording(true);
					flush();
					channel.flush();
				}else{
					position.setEnabled(false);
					getChannel().setRecording(false);
				}
			}
		});
		
		recordConfig.add(inputChannelNumber);
		//recordConfig.add(startRecording);
		//recordConfig.add(startTest);
		recordConfig.setVisible(false);
		
//		JPanel config = new JPanel(new MigLayout());
//		config.add(new JLabel("Input Scale:"));
//		config.add(scale,"wrap");
//		config.add(new JLabel("Output Center:"));
//		config.add(zero,"wrap");
		scale.addActionListener(this);
		zero.addActionListener(this);
		
		//recordConfig.add(config);
		
		position.setEnabled(false);
		position.setMaximum(0);
		position.setMaximum(255);
		position.setMajorTickSpacing(5);
		position.setPaintTicks(true);
		position.setValue(chan.getCurrentTargetValue());
		position.addChangeListener(posListener);
		
		
		add(new JLabel("Output Channel: "+getChannel().getChannelNumber()));
		add(position);
		add(useSlider);
		//add(record);
		add(recordConfig);
		
		//record.setSelected(getChannel().isRecording());
		try{
			inputChannelNumber.setSelectedInteger(getChannel().getInputChannelNumber());
		}catch(Exception ex){
			ex.printStackTrace();
		}
		//recordConfig.setVisible(record.isSelected());
		zero.setText(new Integer(getChannel().getInputCenter()).toString());
		scale.setText(new Double(getChannel().getInputScale()).toString());
		setScaleingInfo();
	}
	private void setScaleingInfo() {
		currentZero = Integer.parseInt(zero.getText());
		currentScale = Double.parseDouble(scale.getText());
		getChannel().setInputScale(getInputScale());
		getChannel().setInputCenter(getInputZero());
		getChannel().setAnalogInputChannelNumber(inputChannelNumber.getSelectedInteger());
	}
	private int getInputZero() {
		return currentZero;
	}
	private double getInputScale() {
		return currentScale;
	}
	
	public int getChannelNumber() {
		// TODO Auto-generated method stub
		return getChannel().getChannelNumber();
	}
	public void setChannel(ServoOutputScheduleChannel channel) {
		this.channel = channel;
	}
	public ServoOutputScheduleChannel getChannel() {
		return channel;
	}
	

	@Override
	public void onServoPositionUpdate(ServoChannel srv, int position,double time) {
		if(useSlider.isSelected()){
			channel.removeIServoPositionUpdateListener(this);
			//flush();
			channel.addIServoPositionUpdateListener(this);
			return;
		}
		this.position.removeChangeListener(posListener);
		this.position.setValue(position);
		this.position.addChangeListener(posListener);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		 setScaleingInfo();
	}
	@Override
	public void onTimeUpdate(double ms) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setIntervalTime(int msInterval, int totalTime) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onReset() {

	}
	@Override
	public void onPlay() {

		//getChannel().setRecording(true);
	}
	@Override
	public void onPause() {
		useSlider.setSelected(false);
		position.setEnabled(false);
		getChannel().setRecording(false);
		//System.out.println("Setting the pause in output UI");
	}
	public CoreScheduler getCb() {
		return cb;
	}
	public void setCb(CoreScheduler cb) {
		this.cb = cb;
	}
	
	
}
