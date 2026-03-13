package com.neuronrobotics.bowlerstudio;

import java.util.function.Supplier;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class NameGetter implements Supplier<String> {

	@Override
	public String get() {
		String sec = "d842796170cb0b155f8508ab00822b72e1dfa9bf";
		if (sec.contains("REPLACE")) {
			String line = System.getProperty("API-SECRET");
			if (line != null)
				return line;
			JFrame jframe = new JFrame();
			String answer = JOptionPane.showInputDialog(jframe, "Enter API secret");
			jframe.dispose();
			return answer;
		}
		return sec;
	}

}
