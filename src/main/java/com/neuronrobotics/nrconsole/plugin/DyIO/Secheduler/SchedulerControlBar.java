package com.neuronrobotics.nrconsole.plugin.DyIO.Secheduler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javafx.stage.FileChooser.ExtensionFilter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.nrconsole.util.GroovyFilter;
import com.neuronrobotics.nrconsole.util.Mp3Filter;
import com.neuronrobotics.sdk.dyio.sequencer.CoreScheduler;
import com.neuronrobotics.sdk.dyio.sequencer.ISchedulerListener;

public class SchedulerControlBar extends JPanel implements ISchedulerListener {
	
	private JSlider slider = new JSlider();
	private JButton play = new JButton("Play ");
	private JButton step = new JButton("Step ");
	private JCheckBox loop = new JCheckBox("Loop");
	private JLabel time = new JLabel("Seconds");

	private JTextField length = new JTextField(4);
	private JButton selectSong = new JButton("Select Audio Track");
	private JLabel trackName = new JLabel("none");
	private CoreScheduler cs;
	private File mp3File=null;
	private ChangeListener sliderListener;
	private ArrayList<ActionListener> pauseListeners = new ArrayList<ActionListener> ();
	private ArrayList<ActionListener> playListeners = new ArrayList<ActionListener> ();
	/**
	 * long 
	 */
	private static final long serialVersionUID = -5636481366169943501L;
	public SchedulerControlBar(CoreScheduler core) {
		core.addISchedulerListener(this);
		setLayout(new MigLayout());
		setBorder(BorderFactory.createLoweredBevelBorder());
		
		
		slider.setMajorTickSpacing(1000);
		slider.setPaintTicks(true);
		setTrackLegnth(60000);
		setCurrentTime(0);
		cs =core;
		sliderListener = new ChangeListener() {
			private boolean wasAdjusting = false;
			@Override
			public void stateChanged(ChangeEvent e) {
				slider.removeChangeListener(sliderListener);
				if(slider.getValueIsAdjusting()) {
					if(cs.isPlaying()) {
						wasAdjusting=true;
						pause();
					}
				}else {
					setCurrentTime(slider.getValue());
					if(wasAdjusting) {
						wasAdjusting = false;
						play();
					}
				}
				slider.addChangeListener(sliderListener);
			}
		};
		slider.addChangeListener(sliderListener);
		
		play.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent arg0) {
				if(!cs.isPlaying()){
					play();
				}else{

					pause();
				}
			}
			
		});
		
		step.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setSequenceParams();
				cs.playStep();
			}
		});
		
		selectSong.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				getFile();
			}
		});
		
		loop.setSelected(false);
		loop.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				cs.setLooping(loop.isSelected());
			}
		});
		
		JPanel mp3Bar = new JPanel(new MigLayout());
		mp3Bar.add(selectSong);
		mp3Bar.add(new JLabel("Current Track:"));
		mp3Bar.add(trackName);
		
		JPanel trackBar = new JPanel(new MigLayout());
		trackBar.add(length);
		trackBar.add(time);
		trackBar.add (slider);
		trackBar.add(step);
		trackBar.add(play);
		trackBar.add(loop);
		

		setBorder(BorderFactory.createRaisedBevelBorder());
		add(mp3Bar,"wrap");
		add(trackBar,"wrap");
	}
	
	private void setSequenceParams(){
		int start =slider.getValue(); 

		int setpoint;
		try{
			setpoint = (int)(1000*Double.parseDouble(length.getText()));
		}catch (NumberFormatException n){
			setpoint=1000;
		}
		setTrackLegnth(setpoint);
		cs.setSequenceParams(setpoint, start);
	}
	
	private void play() {
		setSequenceParams();
		cs.play();
		play.setText("Pause");
		step.setEnabled(false);
		for(ActionListener a:playListeners)
			a.actionPerformed(null);
	}
	private void pause() {
		if(cs != null)
			cs.pause();
		play.setText("Play ");
		step.setEnabled(true);
		for(ActionListener a:pauseListeners)
			a.actionPerformed(null);
		
	}
	
	private void setTrackLegnth(int ms){
		length.setText(new Double(((double)ms)/1000.0).toString());
		setBounds(ms);
	}
	

	private void setCurrentTime(long  val){
		//System.out.println("Setting current time="+val);
		try{
			slider.setValue((int) (val));
		}catch(Exception e){
			e.printStackTrace();
		}
		double cTime = ((double)val)/1000;
		time.setText("Seconds: "+new DecimalFormat("000.00").format(cTime));

		//System.out.println("Setting current time="+val+" slider="+slider.getValue());
	}
	private void setBounds(double top){
		slider.setMaximum(0);
		slider.setMaximum((int) (top));
	}

	
	private void getFile() {
        setAudioFile(FileSelectionFactory.GetFile(mp3File==null?ScriptingEngine.getWorkspace():mp3File, new ExtensionFilter("WAV file","*.wav","*.WAV")));
	}
	public void setAudioFile(File f) {
		cs.setAudioFile(f);
    	setTrackLegnth(cs.getTrackLength());
    	trackName.setText(f.getName());
    	length.setEditable(false);
    	setCurrentTime(0);
	}

	@Override
	public void onTimeUpdate(double ms) {
		setCurrentTime((long) ms);
	}

	@Override
	public void onReset() {
		play.setText("Play");
	}

	@Override
	public void setIntervalTime(int msInterval, int totalTime) {
		// TODO Auto-generated method stub
		
	}

	public void addPauseListener(ActionListener actionListener) {
		if(!pauseListeners.contains(actionListener))
			pauseListeners.add(actionListener);
	}

	public void addPlayListener(ActionListener actionListener) {
		if(!playListeners.contains(actionListener))
			playListeners.add(actionListener);
	}

	@Override
	public void onPlay() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		
	}

}
