package com.neuronrobotics.nrconsole.util;

import java.io.File;

import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.imageprovider.StaticFileProvider;
import com.neuronrobotics.sdk.util.ThreadUtil;


public class FileSelectionFactory {

	public static File GetFile(File start, ExtensionFilter... filter) {
		class fileHolder{
			private boolean done=false;
			private File file=null;
			public boolean isDone() {
				return done;
			}
			public void setDone(boolean done) {
				this.done = done;
			}
			public File getFile() {
				return file;
			}
			public void setFile(File file) {
				this.file = file;
			}
		}
		final fileHolder file=new fileHolder();
		Platform.runLater(() -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setInitialDirectory(start.isDirectory()?start:start.getParentFile());
			
			fileChooser.getExtensionFilters().addAll(filter);
			fileChooser.setTitle("Bowler File Chooser");
			file.setFile(fileChooser.showOpenDialog(BowlerStudio.getPrimaryStage()));
			file.setDone(true);
			
		});
		while(!file.isDone()){
			ThreadUtil.wait(16);
		}
			
		return file.getFile();
	}

}
