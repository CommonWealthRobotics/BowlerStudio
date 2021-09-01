package com.neuronrobotics.bowlerstudio;

import java.util.function.Supplier;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class NameGetter  implements Supplier<String>{

	@Override
	public String get() {
		String line = System.getProperty("API-SECRET");
		if(line!=null)
			return line;
		JFrame jframe = new JFrame();
		String answer = JOptionPane.showInputDialog(jframe, "Kevin Fucked up and left this in the release build\nEnter API secret");
		jframe.dispose();
		return answer;

	}

}
