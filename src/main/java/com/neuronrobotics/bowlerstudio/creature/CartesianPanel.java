package com.neuronrobotics.bowlerstudio.creature;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.neuronrobotics.replicator.driver.BowlerBoardDevice;
import com.neuronrobotics.replicator.driver.NRPrinter;


public class CartesianPanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BowlerBoardDevice delt;
	private NRPrinter printer;
	private JLabel jLabel0;
	private JLabel jLabel1;
	private JLabel jLabel2;
	private JLabel jLabel3;
	private JLabel jLabel4;
	private JTextField jTextField0;
	private JTextField jTextField1;
	private JTextField jTextField2;
	private JTextField jTextField3;
	private JTextField jTextField4;
	private JButton jButton0;

	public void setDevices(BowlerBoardDevice delt, NRPrinter printer) {
		this.delt = delt;
		this.printer = printer;
		
	}

	private void initComponents() {
		setLayout(new GridLayout(6, 4));
		add(getJLabel0());
		add(getJTextField0());
		add(getJLabel1());
		add(getJTextField1());
		add(getJLabel2());
		add(getJTextField2());
		add(getJLabel3());
		add(getJTextField3());
		add(getJLabel4());
		add(getJTextField4());
		add(getJButton0());
		setSize(358, 291);
	}

	private JButton getJButton0() {
		if (jButton0 == null) {
			jButton0 = new JButton();
			jButton0.setText("Update Robot");
		}
		return jButton0;
	}

	private JTextField getJTextField4() {
		if (jTextField4 == null) {
			jTextField4 = new JTextField();
			jTextField4.setText("tempTarget");
			jTextField4.addActionListener(new ActionListener() {
	
				public void actionPerformed(ActionEvent event) {
					jTextField4ActionActionPerformed(event);
				}
			});
		}
		return jTextField4;
	}

	private JTextField getJTextField3() {
		if (jTextField3 == null) {
			jTextField3 = new JTextField();
			jTextField3.setText("extrudeTarget");
			jTextField3.addActionListener(new ActionListener() {
	
				public void actionPerformed(ActionEvent event) {
					jTextField3ActionActionPerformed(event);
				}
			});
		}
		return jTextField3;
	}

	private JTextField getJTextField2() {
		if (jTextField2 == null) {
			jTextField2 = new JTextField();
			jTextField2.setText("ztarget");
			jTextField2.addActionListener(new ActionListener() {
	
				public void actionPerformed(ActionEvent event) {
					jTextField2ActionActionPerformed(event);
				}
			});
		}
		return jTextField2;
	}

	private JTextField getJTextField1() {
		if (jTextField1 == null) {
			jTextField1 = new JTextField();
			jTextField1.setText("ytarget");
			jTextField1.addActionListener(new ActionListener() {
	
				public void actionPerformed(ActionEvent event) {
					jTextField1ActionActionPerformed(event);
				}
			});
		}
		return jTextField1;
	}

	private JTextField getJTextField0() {
		if (jTextField0 == null) {
			jTextField0 = new JTextField();
			jTextField0.setText("xtarget");
			jTextField0.addActionListener(new ActionListener() {
	
				public void actionPerformed(ActionEvent event) {
					jTextField0ActionActionPerformed(event);
				}
			});
		}
		return jTextField0;
	}

	private JLabel getJLabel4() {
		if (jLabel4 == null) {
			jLabel4 = new JLabel();
			jLabel4.setText("Temp");
		}
		return jLabel4;
	}

	private JLabel getJLabel3() {
		if (jLabel3 == null) {
			jLabel3 = new JLabel();
			jLabel3.setText("Extrude");
		}
		return jLabel3;
	}

	private JLabel getJLabel2() {
		if (jLabel2 == null) {
			jLabel2 = new JLabel();
			jLabel2.setText("Z");
		}
		return jLabel2;
	}

	private JLabel getJLabel1() {
		if (jLabel1 == null) {
			jLabel1 = new JLabel();
			jLabel1.setText("Y");
		}
		return jLabel1;
	}

	private JLabel getJLabel0() {
		if (jLabel0 == null) {
			jLabel0 = new JLabel();
			jLabel0.setText("X");
		}
		return jLabel0;
	}

	public CartesianPanel() {
		initComponents();
	}

	private void jTextField4ActionActionPerformed(ActionEvent event) {
		//set temp
	}

	private void jTextField3ActionActionPerformed(ActionEvent event) {
		//set extrude
		
	}

	private void jTextField2ActionActionPerformed(ActionEvent event) {
		//set z
	}

	private void jTextField1ActionActionPerformed(ActionEvent event) {
		//set y
	}

	private void jTextField0ActionActionPerformed(ActionEvent event) {
		// set x
	}
	


}
