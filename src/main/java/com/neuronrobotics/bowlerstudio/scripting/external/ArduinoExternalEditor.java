package com.neuronrobotics.bowlerstudio.scripting.external;

import static com.neuronrobotics.bowlerstudio.scripting.DownloadManager.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.ArduinoLoader;
import com.neuronrobotics.bowlerstudio.scripting.DownloadManager;
import com.neuronrobotics.bowlerstudio.scripting.IExternalEditor;
import javafx.scene.control.Button;
import javafx.scene.image.Image;

public class ArduinoExternalEditor implements IExternalEditor {

	private Button advanced;

	@Override
	public void launch(File file, Button advanced, Runnable OnComplete) {
		this.advanced = advanced;
		new Thread(() -> {
			try {
				File exe = DownloadManager.getRunExecutable("arduino2", null);
				List<String> asList = Arrays.asList(exe.getAbsolutePath(), file.getAbsolutePath());
				if (isMac()) {
					asList = Arrays.asList("open", "-a", exe.getAbsolutePath(), file.getAbsolutePath());

				}
				Thread rthread = run(this, file.getParentFile(), System.err, asList);
				try {
					rthread.join();
				} catch (InterruptedException e) {
					// Auto-generated catch block
					e.printStackTrace();
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
			if (advanced != null)
				onProcessExit(0);
		}).start();
	}

	public Image getImage() {
		try {
			return AssetFactory.loadAsset("Script-Tab-Arduino.png");
		} catch (Exception e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String nameOfEditor() {
		// Auto-generated method stub
		return "Arduino";
	}

	@Override
	public URL getInstallURL() throws MalformedURLException {
		try {
			return new URI(
					"https://github.com/WPIRoboticsEngineering/RobotInterfaceBoard/blob/master/InstallEclipse.md")
					.toURL();
		} catch (URISyntaxException e) {
			throw new MalformedURLException(e.getMessage());
		}
	}

	@Override
	public void onProcessExit(int ev) {
		advanced.setDisable(false);
	}

	@Override
	public List<Class> getSupportedLangauge() {
		return Arrays.asList(ArduinoLoader.class);
	}

	public static void main(String[] args) throws Exception {
		/*
		 * JavaFXInitializer.go(); ScriptingEngine.pull(
		 * "https://github.com/OperationSmallKat/LunaMotherboardFirmware.git"); File f =
		 * ScriptingEngine.fileFromGit(
		 * "https://github.com/OperationSmallKat/LunaMotherboardFirmware.git",
		 * "LunaMotherboardFirmware.ino");
		 *
		 * new ArduinoExternalEditor().launch(f, new javafx.scene.control.Button());
		 */
		// File exe = DownloadManager.getRunExecutable("arduino2", null);
		// File file = new File();
		String absolutePath = "C:\\Users\\Kevin Bad Name\\bin\\" + DownloadManager.getSTUDIO_INSTALL()
				+ "\\arduino2\\Arduino IDE.exe";// exe.getAbsolutePath();
		run(null, new File("C:\\Users\\Kevin Bad Name\\bin\\" + DownloadManager.getSTUDIO_INSTALL() + "\\arduino2"),
				System.err, Arrays.asList(absolutePath,
						"C:\\Users\\Kevin Bad Name\\Documents\\bowler-workspace\\gitcache\\github.com\\OperationSmallKat\\LunaMotherboardFirmware\\LunaMotherboardFirmware.ino"));

	}

}
