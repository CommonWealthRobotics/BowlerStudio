package com.neuronrobotics.bowlerstudio.creature;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import com.neuronrobotics.replicator.driver.NRPrinter;
import com.neuronrobotics.replicator.driver.StateBasedControllerConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.gui.SampleGuiNR;

public class PrinterConfiguration extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1890177802795201269L;
	private SampleGuiNR gui = new SampleGuiNR();
	private StateBasedControllerConfiguration state;
	private JTextField kp = new JTextField(10);
	private JTextField ki = new JTextField(10);
	private JTextField kd = new JTextField(10);
	private JTextField vkp = new JTextField(10);
	private JTextField vkd = new JTextField(10);
	private JTextField mmPos = new JTextField(10);
	private JTextField maxVel = new JTextField(10);
	private JTextField baseRad = new JTextField(10);
	private JTextField EErad = new JTextField(10);
	private JTextField maxz = new JTextField(10);
	private JTextField minz = new JTextField(10);
	private JTextField rodlen = new JTextField(10);
	private JCheckBox hardPos = new JCheckBox("Use Hard Positioning");
	
	private JButton update=new JButton("Update");
	private NRPrinter printer;
	private JPanel controls;
	
	public PrinterConfiguration(){
		setLayout(new MigLayout());
		controls = new JPanel(new MigLayout());
		controls.add(new JLabel("kP"));controls.add(kp,"wrap");
		controls.add(new JLabel("kI"));controls.add(ki,"wrap");
		controls.add(new JLabel("kD"));controls.add(kd,"wrap");
		controls.add(new JLabel("VkP"));controls.add(vkp,"wrap");
		controls.add(new JLabel("VkD"));controls.add(vkd,"wrap");
		controls.add(new JLabel("Resolution (mm)"));controls.add(mmPos,"wrap");
		controls.add(new JLabel("Maximum Velocity (mm/s)"));controls.add(maxVel,"wrap");
		controls.add(new JLabel("Base Radius(mm)"));controls.add(baseRad,"wrap");
		controls.add(new JLabel("End Effector Radius(mm)"));controls.add(EErad,"wrap");
		controls.add(new JLabel("Maximum Z(mm)"));controls.add(maxz,"wrap");
		controls.add(new JLabel("Minimum Z(mm)"));controls.add(minz,"wrap");
		controls.add(new JLabel("Rod Length(mm)"));controls.add(rodlen,"wrap");
		controls.add(hardPos,"wrap");
		
		controls.add(update,"wrap");

		
		update.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				state.setkP(Double.parseDouble(kp.getText()));
				state.setkI(Double.parseDouble(ki.getText()));
				state.setkD(Double.parseDouble(kd.getText()));
				state.setvKP(Double.parseDouble(vkp.getText()));
				state.setvKD(Double.parseDouble(vkd.getText()));
				state.setMmPositionResolution(Double.parseDouble(mmPos.getText()));
				state.setMaximumMMperSec(Double.parseDouble(maxVel.getText()));
				state.setBaseRadius(Double.parseDouble(baseRad.getText()));
				state.setEndEffectorRadius(Double.parseDouble(EErad.getText()));
				state.setMaxZ(Double.parseDouble(maxz.getText()));
				state.setMinZ(Double.parseDouble(minz.getText()));
				state.setRodLength(Double.parseDouble(rodlen.getText()));
				state.setUseHardPositioning(hardPos.isSelected());
				printer.setStateBasedControllerConfiguration(state);
			}
		});
	}
	
	public void setKinematicsModel(AbstractKinematicsNR p) {
		
		gui.setKinematicsModel(p);
		if(NRPrinter.class.isInstance(p)){
			this.printer = (NRPrinter)p;
			state = printer.getStateBasedControllerConfiguration();
			kp.setText(new Double(state.getkP()).toString());
			ki.setText(new Double(state.getkI()).toString());
			kd.setText(new Double(state.getkD()).toString());
			vkp.setText(new Double(state.getvKP()).toString());
			vkd.setText(new Double(state.getvKD()).toString());
			mmPos.setText(new Double(state.getMmPositionResolution()).toString());
			maxVel.setText(new Double(state.getMaximumMMperSec()).toString());
			baseRad.setText(new Double(state.getBaseRadius()).toString());
			EErad.setText(new Double(state.getEndEffectorRadius()).toString());
			maxz.setText(new Double(state.getMaxZ()).toString());
			minz.setText(new Double(state.getMinZ()).toString());
			rodlen.setText(new Double(state.getRodLength()).toString());
			hardPos.setSelected(state.isUseHardPositioning());
			add(controls,"wrap");
		}
		add(gui,"wrap");
	}
}
