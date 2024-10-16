package com.neuronrobotics.bowlerstudio.scripting.external;

import static com.neuronrobotics.bowlerstudio.scripting.DownloadManager.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.NoWorkTreeException;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.BashLoader;
import com.neuronrobotics.bowlerstudio.scripting.DownloadManager;
import com.neuronrobotics.bowlerstudio.scripting.FXMLBowlerLoader;
import com.neuronrobotics.bowlerstudio.scripting.GroovyHelper;
import com.neuronrobotics.bowlerstudio.scripting.IExternalEditor;
import com.neuronrobotics.bowlerstudio.scripting.JsonRunner;
import com.neuronrobotics.bowlerstudio.scripting.RobotHelper;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.SvgLoader;
import com.neuronrobotics.video.OSUtil;

import javafx.scene.control.Button;
import javafx.scene.image.Image;

public class SceneBuilderExternalEditor implements IExternalEditor {

	private Button advanced;

	@Override
	public void launch(File file, Button advanced) {
		new Thread(() -> {
			this.advanced = advanced;
			String filename = file.getAbsolutePath();

			try {
				File dir = file.getAbsoluteFile().getParentFile();
				File scenebuilder = DownloadManager.getRunExecutable("scenebuilder", null);
				File java = DownloadManager.getRunExecutable("java8", null);

				List<String> asList = Arrays.asList(
						java.getAbsolutePath(),
						"-jar",
						scenebuilder.getAbsolutePath(),
						filename);

				Thread t=run(this, dir, System.err, asList);
				t.join();
			} catch (NoWorkTreeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			onProcessExit(0) ;

		}).start();
	}

	public void onProcessExit(int ev) {
		advanced.setDisable(false);
	}

	@Override
	public URL getInstallURL() throws MalformedURLException {
		return new URL("https://gluonhq.com/products/scene-builder/");
	}

	@Override
	public String nameOfEditor() {
		return "SceneBuilder";
	}

	public Image getImage() {
		try {
			return AssetFactory.loadAsset("Script-Tab-fxml.png");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args)
			throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		File f = ScriptingEngine.fileFromGit("https://github.com/madhephaestus/HortonsLinkages.git",
				"main.fxml");

		new SceneBuilderExternalEditor().launch(f, new Button());
	}

	@Override
	public List<Class> getSupportedLangauge() {
		return Arrays.asList( FXMLBowlerLoader.class);
	}
}
