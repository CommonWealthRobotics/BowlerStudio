
package com.neuronrobotics.pidsim;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

class SettingsDialog extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private PIDSim sim;
	private JTextField massTxt = new JTextField(5);
	private JTextField lenTxt = new JTextField(5);
	private JTextField stFricTxt = new JTextField(5);
	private JTextField dyFricTxt = new JTextField(5);
	private JButton saveBtn = new JButton("Save");
	private PIDConstantsDialog constants;
	
	public SettingsDialog(PIDSim sim, PIDConstantsDialog constants) {
		this.sim = sim;
		this.constants = constants;
		
		saveBtn.addActionListener(this);
				
		JPanel p = new JPanel(new MigLayout());
		p.add(new JLabel("Mass (Kg):"), "cell 0 0");
		p.add(massTxt, " cell 1 0");
		p.add(new JLabel("Link Length (m):"), "cell 0 1");
		p.add(lenTxt, " cell 1 1");
		p.add(new JLabel("Static Friction Coefficient:"), "cell 0 2");
		p.add(stFricTxt, " cell 1 2");
		p.add(new JLabel("Dynamic Friction Coefficient:"), "cell 0 3");
		p.add(dyFricTxt, " cell 1 3");
		p.add(saveBtn, "cell 0 4, spanx");
		p.add(constants, "cell 0 5, spanx");
		refreshValues();
		
		add(p);
	}
	
	public void refreshValues() {
		massTxt.setText(Double.toString(sim.getMass()));
		lenTxt.setText(Double.toString(sim.getLength()));
		stFricTxt.setText(Double.toString(sim.getStaticFriction()));
		dyFricTxt.setText(Double.toString(sim.getDynamicFriction()));
	}
	
	private double cleanOrZero(String txt) {
		double d;
		try {
			d = Double.parseDouble(txt);
		} catch (Exception e) {
			d = 0;
		}
		
		return d;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		sim.setMass(cleanOrZero(massTxt.getText()));
		sim.setLength(cleanOrZero(lenTxt.getText()));
		sim.setStaticFriction(cleanOrZero(stFricTxt.getText()));
		sim.setDynamicFriction(cleanOrZero(dyFricTxt.getText()));
		refreshValues();
		setVisible(true);
	}
}
