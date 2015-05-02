package com.neuronrobotics.nrconsole.plugin.PID;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

public class PIDVelocityWidget extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5325751144652371482L;
	PIDControlWidget widget;
	private JTextField vel=new JTextField(5);
	private JTextField time=new JTextField(5);
	JButton go = new JButton("Run Velocity");
	
	public PIDVelocityWidget(PIDControlWidget w){
		widget=w;
		setLayout(new MigLayout());
		go.addActionListener(this);
		vel.addActionListener(this);
		time.addActionListener(this);
		vel.setText("100");
		time.setText("0");
		add(new JLabel("Velocity Control"),"wrap");
		JPanel p = new JPanel(new MigLayout());
		p.add(vel);p.add(new JLabel("ticks per second"),"wrap");
		p.add(time);p.add(new JLabel("seconds"),"wrap");
		add(p,"wrap");
		add(go,"wrap");
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		System.err.println("Go button pressed");
		try{
			double t = Double.parseDouble(time.getText());
			int v = Integer.parseInt(vel.getText());
			widget.SetPIDVel(v, t);
		}catch (Exception ex){
			vel.setText("100");
			time.setText("0");
			ex.printStackTrace();
		}
	}
}
