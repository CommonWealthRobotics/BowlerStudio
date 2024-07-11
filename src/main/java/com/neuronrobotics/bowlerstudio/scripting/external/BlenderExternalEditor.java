package com.neuronrobotics.bowlerstudio.scripting.external;

import static com.neuronrobotics.bowlerstudio.scripting.DownloadManager.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.NoWorkTreeException;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.BlenderLoader;
import com.neuronrobotics.bowlerstudio.scripting.DownloadManager;
import com.neuronrobotics.bowlerstudio.scripting.IExternalEditor;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingFileWidget;
import com.neuronrobotics.bowlerstudio.scripting.StlLoader;
import com.neuronrobotics.bowlerstudio.scripting.SvgLoader;
import com.neuronrobotics.video.OSUtil;

import eu.mihosoft.vrl.v3d.JavaFXInitializer;
import javafx.scene.control.Button;
import javafx.scene.image.Image;

public class BlenderExternalEditor implements IExternalEditor {

	private Button advanced;

	@Override
	public void launch(File file, Button advanced) {
		new Thread(() -> {
			this.advanced = advanced;
			String filename = file.getAbsolutePath();
			File dir = new File(filename).getParentFile();
			File exe = DownloadManager.getRunExecutable("blender", null);

			if(filename.toLowerCase().endsWith(".stl")) {
				File blenderfile = new File(dir.getAbsolutePath()+delim()+file.getName()+".blend");
				if(ScriptingFileWidget.askToDeleteFile(blenderfile.getName())) {
					blenderfile.delete();
				}
				BlenderLoader.toBlenderFile(file, blenderfile);
				filename=blenderfile.getAbsolutePath();
				try {
					BowlerStudio.createFileTab(blenderfile);
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
			if(filename.toLowerCase().endsWith(".stl") || !new File(filename).exists()) {
				System.out.println("ERROR blender conversion failed!");
				return;
			}
			try {
				List<String> asList = Arrays.asList(exe.getAbsolutePath(), filename);
				if(isMac()) {
					asList = Arrays.asList("open","-a",exe.getAbsolutePath(), filename);
					
				}
				Thread t=run(this, dir, System.out, asList);
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
		return new URL("https://www.blender.org/download/release/Blender4.1/blender-4.1.1-linux-x64.tar.xz/");
	}

	@Override
	public String nameOfEditor() {
		return "Blender";
	}

	public Image getImage() {
		try {
			return AssetFactory.loadAsset("Blender.png");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args)
			throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		JavaFXInitializer.go();
		File f = ScriptingEngine.fileFromGit("https://github.com/NeuronRobotics/NASACurisoity.git",
				"STL/upper-arm.STL");

		new BlenderExternalEditor().launch(f, new Button());
	}

	@Override
	public List<Class> getSupportedLangauge() {
		return Arrays.asList( StlLoader.class,BlenderLoader.class);
	}

}
