package com.neuronrobotics.pidsim;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

public class PIDConstantsDialog extends JPanel {
	private double kp=1;
	private double ki=0;
	private double kd=0;

	private JTextField pData=new JTextField(5);
	private JTextField iData=new JTextField(5);
	private JTextField dData=new JTextField(5);
	private JButton set=new JButton("Set");
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public PIDConstantsDialog(double p,double i,double d){
		setLayout(new MigLayout());
		setKp(p);
		setKi(i);
		setKd(d);
		add(new JLabel("Kp"));
		add(pData,"wrap");
		add(new JLabel("Ki"));
		add(iData,"wrap");
		add(new JLabel("Kd"));
		add(dData,"wrap");
		pData.setText(new Double(getKp()).toString());
		iData.setText(new Double(getKi()).toString());
		dData.setText(new Double(getKd()).toString());
		set.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					setKp(Double.parseDouble(pData.getText()));
				}catch (NumberFormatException ex){}
				try{
					setKi(Double.parseDouble(iData.getText()));
				}catch (NumberFormatException ex){}
				try{
					setKd(Double.parseDouble(dData.getText()));
				}catch (NumberFormatException ex){}
			}
		});
		add(set);
	}
	public void setKp(double kp) {
		this.kp = kp;
	}
	public double getKp() {
		return kp;
	}
	public void setKi(double ki) {
		this.ki = ki;
	}
	public double getKi() {
		return ki;
	}
	public void setKd(double kd) {
		this.kd = kd;
	}
	public double getKd() {
		return kd;
	}

}
