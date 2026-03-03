package com.neuronrobotics.bowlerstudio.scripting;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.video.OSUtil;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.image.Image;

public interface IExternalEditor {

	
	public  abstract List<Class> getSupportedLangauge();
	
	default  boolean isSupportedByExtension(File file) {
		if(getSupportedLangauge()!=null)
			for(Class c:getSupportedLangauge())
			if (c.isInstance(ScriptingEngine.getLangaugeByExtension(file.getAbsolutePath()))) {
				return true;
			}
		return false;
	}
	
	public  abstract void launch(File file, Button advanced,Runnable onExit);
	
	public  abstract String nameOfEditor();
	
	public  abstract URL getInstallURL() throws MalformedURLException;
	
	public  abstract void onProcessExit(int ev);
	
	public abstract Image getImage();
	




}
