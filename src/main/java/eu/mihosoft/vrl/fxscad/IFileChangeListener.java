package eu.mihosoft.vrl.fxscad;

import java.io.File;
import java.nio.file.WatchEvent;

public interface IFileChangeListener {
	
	public void onFileChange(File fileThatChanged,WatchEvent event);

}
