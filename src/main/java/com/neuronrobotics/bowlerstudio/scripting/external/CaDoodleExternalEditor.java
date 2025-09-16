package com.neuronrobotics.bowlerstudio.scripting.external;

import static com.neuronrobotics.bowlerstudio.scripting.DownloadManager.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.NoWorkTreeException;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.BashLoader;
import com.neuronrobotics.bowlerstudio.scripting.CaDoodleLoader;
import com.neuronrobotics.bowlerstudio.scripting.DownloadManager;
import com.neuronrobotics.bowlerstudio.scripting.FXMLBowlerLoader;
import com.neuronrobotics.bowlerstudio.scripting.GroovyHelper;
import com.neuronrobotics.bowlerstudio.scripting.IExternalEditor;
import com.neuronrobotics.bowlerstudio.scripting.JsonRunner;
import com.neuronrobotics.bowlerstudio.scripting.OpenSCADLoader;
import com.neuronrobotics.bowlerstudio.scripting.RobotHelper;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.SvgLoader;
import com.neuronrobotics.video.OSUtil;

import eu.mihosoft.vrl.v3d.JavaFXInitializer;
import javafx.scene.control.Button;
import javafx.scene.image.Image;

public class CaDoodleExternalEditor extends IExternalEditor {

	private Button advanced;

	
	public void launch(File file, Button advanced,Runnable onExit) {
		new Thread(() -> {
			this.advanced = advanced;
			String filename = "\""+file.getAbsolutePath()+"\"";

			try {
				File dir = file.getAbsoluteFile().getParentFile();
				File exe;
				if(OSUtil.isOSX()) {
					exe = DownloadManager.getConfigExecutable("cadoodle", null);
				}else {
					exe = DownloadManager.getRunExecutable("cadoodle", null);
				}

				List<String> asList = Arrays.asList(
							exe.getAbsolutePath(),
						filename);

				DownloadManager.legacySystemRun(new HashMap<String, String>(),dir, System.err, asList);

			} catch (NoWorkTreeException e) {
				// Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// Auto-generated catch block
				e.printStackTrace();
			}
			onProcessExit(0) ;
			onExit.run();

		}).start();
	}

	public void onProcessExit(int ev) {
		advanced.setDisable(false);

	}

	
	public URL getInstallURL() throws MalformedURLException {
		return new URL("https://github.com/CommonWealthRobotics/CaDoodle/blob/main/README.md");
	}

	
	public String nameOfEditor() {
		return "CaDoodle";
	}

	public Image getImage() {
		try {
			Image asset = AssetFactory.loadAsset("Script-Tab-CaDoodle.png");
			return asset;
		} catch (Exception e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args)
			throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		JavaFXInitializer.go();
		String url="https://github.com/madhephaestus/TestRepo.git";
		ScriptingEngine.pull("https://github.com/CommonWealthRobotics/ExternalEditorsBowlerStudio.git");
		ScriptingEngine.pull(url);
		File f = ScriptingEngine.fileFromGit(url,
				"Doodle1/TestRepo.doodle");

		new CaDoodleExternalEditor().launch(f, new Button(),()->{});
	}

	
	public List<Class> getSupportedLangauge() {
		return Arrays.asList( CaDoodleLoader.class);
	}


}
