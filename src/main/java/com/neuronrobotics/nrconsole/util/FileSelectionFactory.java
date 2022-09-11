package com.neuronrobotics.nrconsole.util;

import java.io.File;

import javafx.application.Platform;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.BowlerStudioModularFrame;
import com.neuronrobotics.sdk.util.ThreadUtil;


public class FileSelectionFactory {

	private FileSelectionFactory() {
	}
	private static class fileHolder{
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
	public static File GetFile(File start, boolean save, ExtensionFilter... filter) {
		if(start==null)
			throw new NullPointerException();
	
		final fileHolder file=new fileHolder();
		BowlerStudio.runLater(() -> {
			FileChooser fileChooser = new FileChooser();
			
			fileChooser.setInitialDirectory(start.isDirectory()?start:start.getParentFile());
			if(filter!=null)
				fileChooser.getExtensionFilters().addAll(filter);
			fileChooser.setTitle("Bowler File Chooser");
			if(save)
				file.setFile(fileChooser.showSaveDialog(BowlerStudioModularFrame.getPrimaryStage()));
			else
				file.setFile(fileChooser.showOpenDialog(BowlerStudioModularFrame.getPrimaryStage()));
			file.setDone(true);
			
		});
		while(!file.isDone()){
			ThreadUtil.wait(16);
		}
			
		return file.getFile();
	}
	public static File GetFile(File start, ExtensionFilter... filter) {
		return GetFile(start, false,filter);
	}

	public static File GetDirectory(File start) {
		if(start==null)
			throw new NullPointerException();
	
		final fileHolder file=new fileHolder();
		BowlerStudio.runLater(() -> {
			DirectoryChooser fileChooser = new DirectoryChooser();
			
			fileChooser.setInitialDirectory(start.isDirectory()?start:start.getParentFile());
			fileChooser.setTitle("Bowler File Chooser");
			file.setFile(fileChooser.showDialog(BowlerStudioModularFrame.getPrimaryStage()));
			file.setDone(true);
			
		});
		while(!file.isDone()){
			ThreadUtil.wait(16);
		}
			
		return file.getFile();
	}

}
