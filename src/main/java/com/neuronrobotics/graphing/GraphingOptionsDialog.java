package com.neuronrobotics.graphing;

import javax.swing.JFrame;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

public class GraphingOptionsDialog extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private GraphingWindow window;
	
	public GraphingOptionsDialog(GraphingWindow window) {
		this.window = window;
		setLayout(new MigLayout());
		setTitle("Graphing Options");
		
		add(new JLabel("Graphing Options"));
		
		pack();
		setLocationRelativeTo(null);
	}
}
