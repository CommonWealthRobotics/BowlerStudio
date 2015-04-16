package com.neuronrobotics.nrconsole.util;

import javax.swing.JPanel;

public class CompoundSlider extends JPanel {

	/**
	 * long 
	 */
	private static final long serialVersionUID = 6803306324376497720L;
	private int lb,ub;
	public  CompoundSlider(int lowerBound, int upperBound,int center, double scale) {
		lb=lowerBound;
		ub=upperBound;
	}

}
