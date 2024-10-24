package com.neuronrobotics.bowlerstudio;

import java.util.function.Supplier;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class NameGetter  implements Supplier<String>{

	@Override
	public String get() {
		String sec = "45a3ab20cd9960c24e691458ccd5bac69a7718d9";
		if(sec.contains("REPLACE")) {
			String line = System.getProperty("API-SECRET");
			if(line!=null)
				return line;
			JFrame jframe = new JFrame();
			String answer = JOptionPane.showInputDialog(jframe, "Enter API secret");
			jframe.dispose();
			return answer;
		}
		return sec;
	}

}
