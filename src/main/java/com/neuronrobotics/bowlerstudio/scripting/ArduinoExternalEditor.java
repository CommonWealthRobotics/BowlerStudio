package com.neuronrobotics.bowlerstudio.scripting;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jgit.lib.Repository;

import com.neuronrobotics.video.OSUtil;

import eu.mihosoft.vrl.v3d.JavaFXInitializer;
import javafx.scene.control.Button;

public class ArduinoExternalEditor implements IExternalEditor {

	private Button advanced;

	@Override
	public void launch(File file, Button advanced) {
		this.advanced = advanced;
		Repository repository;
		try {
			repository = ScriptingEngine.locateGit(file).getRepository();
			File dir = repository.getWorkTree();
			if (OSUtil.isLinux())
				run(dir,System.err, "bash", System.getProperty("user.home")+"/bin/arduino-1.8.13/arduino", file.getAbsolutePath() );
			if (OSUtil.isWindows())
				run(dir,System.err, "C:\\RBE\\arduino-1.8.5\\arduino.exe", file.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}

	@Override
	public String nameOfEditor() {
		// TODO Auto-generated method stub
		return "Arduino";
	}

	@Override
	public URL getInstallURL() throws MalformedURLException {
		return new URL("https://github.com/WPIRoboticsEngineering/RobotInterfaceBoard/blob/master/InstallEclipse.md");
	}

	@Override
	public void onProcessExit(int ev) {
		advanced.setDisable(false);
	}

	@Override
	public Class getSupportedLangauge() {
		if (OSUtil.isLinux() || OSUtil.isWindows())
			return ArduinoLoader.class;
		return null;
	}
	public static void main(String[] args) throws Exception {
		JavaFXInitializer.go();
		ScriptingEngine.pull("https://github.com/OperationSmallKat/LunaMotherboardFirmware.git");
		File f = ScriptingEngine.fileFromGit("https://github.com/OperationSmallKat/LunaMotherboardFirmware.git", "LunaMotherboardFirmware.ino");

		new ArduinoExternalEditor().launch(f, new javafx.scene.control.Button());
	}

	
}
