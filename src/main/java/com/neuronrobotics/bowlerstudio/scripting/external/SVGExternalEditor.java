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
import com.neuronrobotics.bowlerstudio.scripting.GroovyHelper;
import com.neuronrobotics.bowlerstudio.scripting.IExternalEditor;
import com.neuronrobotics.bowlerstudio.scripting.JsonRunner;
import com.neuronrobotics.bowlerstudio.scripting.RobotHelper;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.SvgLoader;
import com.neuronrobotics.video.OSUtil;

import javafx.scene.control.Button;
import javafx.scene.image.Image;

public class SVGExternalEditor implements IExternalEditor {

	private Button advanced;

	@Override
	public void launch(File file, Button advanced) {
		new Thread(() -> {
			this.advanced = advanced;
			String filename = file.getAbsolutePath();

			try {
//				Git locateGit = ScriptingEngine.locateGit(file);
//				File dir = locateGit.getRepository().getWorkTree();
//				ScriptingEngine.closeGit(locateGit);

				File exe = DownloadManager.getRunExecutable("inkscape", null);

				List<String> asList = Arrays.asList(exe.getAbsolutePath(), filename);
				if(isMac()) {
					asList = Arrays.asList("open","-a",exe.getAbsolutePath(), filename);
					
				}
				Thread t=run(this, file.getParentFile(), System.err, asList);
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
		return new URL("https://inkscape.org/release/");
	}

	@Override
	public String nameOfEditor() {
		return "Inkscape";
	}

	public Image getImage() {
		try {
			return AssetFactory.loadAsset("Script-Tab-SVG.png");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args)
			throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		File f = ScriptingEngine.fileFromGit("https://github.com/Technocopia/Graphics.git",
				"Graphics/SimplifiedLogo/simplified logo.svg");

		new SVGExternalEditor().launch(f, new Button());
	}

	@Override
	public List<Class> getSupportedLangauge() {
		return Arrays.asList( SvgLoader.class);
	}
}
