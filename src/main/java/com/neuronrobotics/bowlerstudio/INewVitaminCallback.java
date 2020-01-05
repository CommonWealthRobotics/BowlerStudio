package com.neuronrobotics.bowlerstudio;

import javafx.scene.control.Menu;

public interface INewVitaminCallback {
	void addVitaminType(String s);
	
	Menu getTypeMenu(String type);
	
	void addSizesToMenu(String size,String type);
}
