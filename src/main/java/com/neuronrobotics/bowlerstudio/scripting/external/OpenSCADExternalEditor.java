package com.neuronrobotics.bowlerstudio.scripting.external;

import static com.neuronrobotics.bowlerstudio.scripting.DownloadManager.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.NoWorkTreeException;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.DownloadManager;
import com.neuronrobotics.bowlerstudio.scripting.IExternalEditor;
import com.neuronrobotics.bowlerstudio.scripting.OpenSCADLoader;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;

import eu.mihosoft.vrl.v3d.JavaFXInitializer;
import javafx.scene.control.Button;
import javafx.scene.image.Image;

public class OpenSCADExternalEditor implements IExternalEditor {

	private Button advanced;

	@Override
	public void launch(File file, Button advanced, Runnable OnComplete) {
		new Thread(() -> {
			this.advanced = advanced;
			String filename = file.getAbsolutePath();

			try {
				File dir = file.getAbsoluteFile().getParentFile();
				File openscad = DownloadManager.getRunExecutable("openscad", null);

				List<String> asList = Arrays.asList(openscad.getAbsolutePath(), filename);

				Thread t = run(this, dir, System.err, asList);
				t.join();
			} catch (NoWorkTreeException e) {
				// Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// Auto-generated catch block
				e.printStackTrace();
			}
			onProcessExit(0);

		}).start();
	}

	public void onProcessExit(int ev) {
		advanced.setDisable(false);
	}

	@Override
	public URL getInstallURL() throws MalformedURLException {
		try {
			return new URI("https://openscad.org/downloads.html").toURL();
		} catch (URISyntaxException e) {
			throw new MalformedURLException(e.getMessage());
		}
	}

	@Override
	public String nameOfEditor() {
		return "OpenSCAD";
	}

	public Image getImage() {
		try {
			return AssetFactory.loadAsset("Script-Tab-OpenSCAD.png");
		} catch (Exception e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args)
			throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		JavaFXInitializer.go();
		String url = "https://github.com/madhephaestus/TestRepo.git";
		ScriptingEngine.pull(url);
		File f = ScriptingEngine.fileFromGit(url, "test.scad");

		new OpenSCADExternalEditor().launch(f, new Button(), () -> {
		});
	}

	@Override
	public List<Class> getSupportedLangauge() {
		return Arrays.asList(OpenSCADLoader.class);
	}
}
