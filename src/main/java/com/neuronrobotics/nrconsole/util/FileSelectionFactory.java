package com.neuronrobotics.nrconsole.util;

import java.io.File;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.imageprovider.StaticFileProvider;


public class FileSelectionFactory {
	public static File GetFile(File start, ExtensionFilter... filter) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(filter);
		fileChooser.setTitle("Bowler File Chooser");
		return fileChooser.showOpenDialog(BowlerStudio.getPrimaryStage());
	}

}
