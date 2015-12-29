package com.neuronrobotics.nrconsole.plugin.DyIO.Secheduler;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import javafx.stage.FileChooser.ExtensionFilter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.nrconsole.util.IntegerComboBox;
import com.neuronrobotics.nrconsole.util.XmlFilter;
import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.sequencer.CoreScheduler;
import com.neuronrobotics.sdk.dyio.sequencer.ServoOutputScheduleChannel;
import com.neuronrobotics.sdk.serial.SerialConnection;

public class SchedulerGui extends JPanel{

	/**
	 * 
	 */
	//private DyIO d = new DyIO();
	private static final long serialVersionUID = -2532174391435417313L;
	JPanel channelBar = new JPanel(new MigLayout());
	private IntegerComboBox availibleChans = new IntegerComboBox();
	private IntegerComboBox usedChans = new IntegerComboBox();
	private ArrayList< ServoOutputScheduleChannelUI> outputs = new ArrayList< ServoOutputScheduleChannelUI>();
	private File configFile=null;
	CoreScheduler cs;
	SchedulerControlBar cb;
	private int loopTime =50;
	private DyIO dyio;
	public SchedulerGui(){

	}
	private void rmAllChannels() {
		int [] chans = new int[outputs.size()];
		for(int i=0;i<chans.length;i++) {
			chans[i]=outputs.get(i).getChannelNumber();
		}
		for(int i=0;i<chans.length;i++) {
			rmChannel(chans[i]);
		}
	}
	private void rmChannel(int num) {
		
		ServoOutputScheduleChannelUI s=null;
		
		for(ServoOutputScheduleChannelUI so:outputs) {
			if(so.getChannelNumber() == num)
				s=so;
		}
		if(s==null)
			return;
		cs.removeServoOutputScheduleChannel(s.getChannel());
		cs.removeISchedulerListener(s);
		outputs.remove(s);
		channelBar.remove(s);
		usedChans.removeInteger(num);
		availibleChans.addInteger(num);
	}
	private void addServoChannel( ServoOutputScheduleChannel chan){
		int selected = chan.getChannelNumber();
		ServoOutputScheduleChannelUI sosc= 	new ServoOutputScheduleChannelUI(chan,cs);
		cs.addISchedulerListener(sosc);
		outputs.add(sosc);
		channelBar.add(sosc,"wrap");
		availibleChans.removeInteger(selected);
		usedChans.addInteger(selected);
	}
	
	protected void importfromFile() {
		if(configFile==null)
			return;
		cs.loadFromFile(configFile);
		cb.setAudioFile(cs.getAudioFile());
		ArrayList< ServoOutputScheduleChannel> outs = cs.getOutputs();
		for(ServoOutputScheduleChannel so:outs){
			addServoChannel(so);
		}
		for(int i=0;i< get().getChannels().size();i++){
			 get().getValue(i);
		}
	}

	protected void exportToFile() {
		String s = cs.getXml();
		try{
			  // Create file 
			  FileWriter fstream = new FileWriter(configFile.getAbsolutePath());
			  BufferedWriter out = new BufferedWriter(fstream);
			  out.write(s);
			  //Close the output stream
			  out.close();
		}catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
		}
		  
	}

	private void getFile() {
		if(configFile==null)
			configFile=ScriptingEngine.getWorkspace();
		configFile=FileSelectionFactory.GetFile(configFile, new ExtensionFilter("Sequence XML","*.xml","*.XML"));
	}
	
	private DyIO get(){
		return dyio;
	}

	public boolean setConnection(BowlerAbstractDevice connection) {
		dyio = (DyIO)connection;
		setLayout(new MigLayout());
		setBorder(BorderFactory.createLoweredBevelBorder());
		cs = new CoreScheduler( get(), loopTime,6000);
		cb = new SchedulerControlBar(cs);
		
		JPanel addBar = new JPanel(new MigLayout());
		JButton addChannel = new JButton("Add new channel");
		addChannel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try{
					int selected = availibleChans.getSelectedInteger();
					addServoChannel(cs.addServoChannel(selected));

				}catch (Exception ex){
					JOptionPane.showMessageDialog(null, "Failed to select channel, "+ex.getMessage(), "Bowler ERROR", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		for(int i=0;i<24;i++){
			availibleChans.addInteger(i);
		}
		addBar.add(addChannel);
		addBar.add(availibleChans);
		
		JButton removeChannel = new JButton("Remove channel");
		removeChannel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
	
				try{
					int selected = usedChans.getSelectedInteger();
					for(int i=0;i<outputs.size();i++){
						ServoOutputScheduleChannelUI s = outputs.get(i);
						if(s.getChannelNumber()==selected){
							rmChannel(selected);
							return;
						}
					}
					
				}catch (Exception ex){
					JOptionPane.showMessageDialog(null, "Failed to select channel, "+ex.getMessage(), "Bowler ERROR", JOptionPane.ERROR_MESSAGE);
				}

			}
		});
		addBar.add(removeChannel);
		addBar.add(usedChans);
		
		JButton saveConfiguration = new JButton("Save Configuration");
		saveConfiguration.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getFile();
				exportToFile();
			}
		});
		JButton loadConfiguration = new JButton("Load Configuration");
		loadConfiguration.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				rmAllChannels();
				
				getFile();
				importfromFile();
			}
		});
		addBar.add(saveConfiguration );
		addBar.add(loadConfiguration);
		channelBar.setBorder(BorderFactory.createRaisedBevelBorder());
		
		add(cb,"wrap");
		add(addBar,"wrap");
		add(channelBar,"wrap");
		
		return  get().ping();
	}
	
	
	
//	public static void main(String[] args) {
//		 JFrame frame = new JFrame();
//		 SchedulerGui sg =new SchedulerGui();
//		 //sg.setConnection(new SerialConnection("COM48"));
//		 sg.setConnection(new SerialConnection("/dev/DyIO0"));
//		 //sg.setConnection(ConnectionDialog.promptConnection());
//		 frame .add(sg);
//		 frame.setSize(new Dimension(1024,768));
//		 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		 frame.setVisible(true);
//	}
}
