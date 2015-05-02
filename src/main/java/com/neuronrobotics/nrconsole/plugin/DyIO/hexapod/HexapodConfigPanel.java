package com.neuronrobotics.nrconsole.plugin.DyIO.hexapod;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.nrconsole.util.XmlFilter;
import com.neuronrobotics.sdk.addons.walker.BasicWalker;
import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.dyio.DyIO;
//import com.neuronrobotics.sdk.dyio.DyIORegestry;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class HexapodConfigPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6669903574934793581L;
	private JLabel outputFileDisplay = new JLabel("No output file selected");
	private JLabel inputFileDisplay = new JLabel("No input file selected");
	private JButton configFile = new JButton("Load Configuration From File");
	private JButton selectOutput = new JButton("Export Configuration To File");
	private JCheckBox defaultConfig = new JCheckBox("Load Configuration From Default");
	private JButton start = new JButton("Initialize Configuration to DyIO");
	private JPanel controlPanel = new JPanel(new MigLayout());
	private JPanel confPanel = new JPanel(new MigLayout());
	private ServoChannelConfiguration srv = null;
	//private JButton servos = new JButton("Configure Servos Channels");
	private JButton save = new JButton("Save Configuration");
	private JButton test = new JButton("Test Configuration");
	private BasicWalker walker;
	private static File inputFile = null;
	private static File outputFile = null;
	//private JFrame hexFrame;
	private HexapodTester testWidget=new HexapodTester();
	private DyIO dyio;
	
	public HexapodConfigPanel() {
		//this.hexFrame=hexFrame;
	}
	private HexapodConfigPanel getGUI(){
		return this;
	}
	private void initGUI(DyIO dyio){
		setName("Hexapod Configuration");
		setLayout(new MigLayout());
		
		defaultConfig.addActionListener(new ActionListener() {		
			public void actionPerformed(ActionEvent e) {
				setConfigEnabled(false);
				if(defaultConfig.isSelected()){
					configFile.setEnabled(false);
					start.setEnabled(true);
					inputFileDisplay.setText("Default Configuration Loaded");
				}else{
					if(getInputFile() != null){
						start.setEnabled(true);
						inputFileDisplay.setText(getInputFile().getName());
					}else{
						start.setEnabled(false);
					}
					configFile.setEnabled(true);
				}
			}
		});
		selectOutput.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				new outputSelector().start();
			}
		});
		
		configFile.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				selectFile();
			}
		});
		start.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				start.setEnabled(false);
				try{
					dyio.setServoPowerSafeMode(false);
					setConfigEnabled(true);
					if(defaultConfig.isSelected()){
						setWalker(new BasicWalker(dyio));
					}else{
						setWalker(new BasicWalker(getInputFile(),dyio));
					}
					if(srv == null){
						srv=new ServoChannelConfiguration(getGUI());
						srv.setVisible(true);
					}
					confPanel.removeAll();
					confPanel.add(srv);
					confPanel.setBorder(BorderFactory.createRaisedBevelBorder());
					confPanel.setVisible(true);
					invalidate();
					//hexFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
				}catch(Exception ex){
					ex.printStackTrace();
					JOptionPane.showMessageDialog(null, ex.getMessage(), "Hexapod ERROR", JOptionPane.ERROR_MESSAGE);
					setConfigEnabled(false);
					start.setEnabled(true);
				}
				redisplay();
			}
		});
		save.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				//System.out.println("Saving configuration to file: "+getOutputFile().getAbsolutePath());
				getWalker().writeXML(getOutputFile());
			}
		});
		test.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				new tester().start();
			}
		});
		
		
		defaultConfig.setSelected(true);
		configFile.setEnabled(false);
		setConfigEnabled(false);
		
		
		controlPanel.add(start,"wrap");
		controlPanel.add(defaultConfig,"wrap");
		controlPanel.add(configFile,"wrap");
		controlPanel.add(inputFileDisplay,"wrap");
		controlPanel.add(selectOutput,"wrap");
		controlPanel.add(outputFileDisplay,"wrap");
		
		JPanel testPanel = new JPanel(new MigLayout());
		
		testPanel.add(test,"wrap");
		testPanel.add(testWidget,"wrap");
		//testPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		
		
		JPanel cp = new JPanel(new MigLayout());
		cp.add(controlPanel);
		cp.add(testPanel);
		cp.setBorder(BorderFactory.createRaisedBevelBorder());
		
		removeAll();
		add(cp,"wrap");
		add(confPanel);
		

		start.setEnabled(false);
		defaultConfig.setSelected(false);
		configFile.setEnabled(true);
		testWidget.setVisible(false);
		
	}
	private  HexapodConfigPanel getHexapodConfigPanel(){
		return this;
	}
	public void redisplay(){
		if(srv != null)
			srv.redisplay();
	}
	private void selectFile(){
		new inputSelector().start();
	}
	
	void setConfigEnabled(boolean en){
		//servos.setEnabled(en);	
		if(getOutputFile() ==  null){
			save.setEnabled(false);
		}else{
			save.setEnabled(en);
		}
		test.setEnabled(en);
	}
	

	public boolean setDyIO(DyIO dyio){
		this.dyio = dyio;
		initGUI(dyio);
		//System.out.println("Setting DyIO In Hex Panel");
		return dyio.isAvailable();
	}
	private File getFile(File file) {
		return FileSelectionFactory.GetFile(file, new XmlFilter());
	}
	
	private class tester extends Thread{
		public void run(){
			test.setEnabled(false);
			testWidget.setVisible(true);
			srv.setEnabled(false);
			defaultConfig.setEnabled(false);
			//new HexapodTester(getWalker(), getHexapodConfigPanel());
			testWidget.setup(getHexapodConfigPanel());
			testWidget.run();
			ThreadUtil.wait(100);
			while(testWidget.isRunning()){
				ThreadUtil.wait(100);
			}
			redisplay();
			defaultConfig.setEnabled(true);
			srv.setEnabled(true);
			test.setEnabled(true);
			testWidget.setVisible(false);
		}
	}
	private class outputSelector extends Thread{
		public void run(){
			//setConfigEnabled(false); 
			selectOutput.setEnabled(false);
			setOutputFile(getFile(getOutputFile()));
			//System.out.println("Saving configuration to file: "+getOutputFile().getAbsolutePath());
			getWalker().writeXML(getOutputFile());
			selectOutput.setEnabled(true);
			//setConfigEnabled(true); 
		}
	}
	private class inputSelector extends Thread{
		public void run(){
			setConfigEnabled(false); 
	        setInputFile(getFile(getInputFile())); 
	        start.setEnabled(true);
		}
	}


	public void setWalker(BasicWalker walker) {
		this.walker = walker;
		walker.Home();
	}

	public BasicWalker getWalker() {
		return walker;
	}
	public void setInputFile(File inputFile) {
		HexapodConfigPanel.inputFile = inputFile;
		setOutputFile(inputFile);
		if(getInputFile() != null){
            if(!getInputFile().getName().matches(".+\\.xml$")){
            	String message = "Invalid file type. Must be .xml";
            	JOptionPane.showMessageDialog(null, message, message, JOptionPane.ERROR_MESSAGE);
            	return;
            }
            start.setEnabled(true);
            setWalker(new BasicWalker(getInputFile(),dyio));
            inputFileDisplay.setText(getInputFile().getName());
            setConfigEnabled(false);	
        }
	}
	public File getInputFile() {
		return inputFile;
	}
	public void setOutputFile(File outputFile) {
		HexapodConfigPanel.outputFile = outputFile;
		if(getOutputFile() != null){
            if(!getOutputFile().getName().matches(".+\\.xml$")){
            	String message = "Invalid file type. Must be .xml";
            	JOptionPane.showMessageDialog(null, message, message, JOptionPane.ERROR_MESSAGE);
            	return;
            }
            outputFileDisplay.setText(getOutputFile().getName());
        }
	}
	public File getOutputFile() {
		return outputFile;
	}

	

	
	
}
