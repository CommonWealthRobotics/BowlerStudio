package com.neuronrobotics.bowlerstudio.creature;

import java.io.File;
import java.util.ArrayList;

public interface IAmControlled {
	File getScriptFile();
	ArrayList<Object> getArguments();
	javafx.scene.image.ImageView getRunAsset();
	javafx.scene.control.Button getRunStopButton();
	String getButtonRunText();
	String getName();
}
