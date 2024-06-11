package com.neuronrobotics.bowlerstudio.scripting.external;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.NoWorkTreeException;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.IExternalEditor;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.SvgLoader;
import com.neuronrobotics.video.OSUtil;

import javafx.scene.control.Button;
import javafx.scene.image.Image;

public class SVGExternalEditor implements IExternalEditor {

	private Button advanced;


	@Override
	public void launch(File file, Button advanced) {
		this.advanced = advanced;
		String filename = file.getAbsolutePath();
		if(OSUtil.isWindows()) {
			filename="\""+filename+"\"";
		}
		try {
			Git locateGit = ScriptingEngine.locateGit(file);
			File dir = locateGit.getRepository().getWorkTree();
			ScriptingEngine.closeGit(locateGit);
			if(OSUtil.isLinux())
				run(dir,System.err,"inkscape",filename);
			if(OSUtil.isWindows()) {
				String exe="inkscape.exe";
				String [] options = {"C:\\Program Files\\Inkscape\\bin\\inkscape.exe",
						"C:\\Program Files\\Inkscape\\inkscape.exe"};
				for (int i=0;i<options.length;i++) {
					if(new File(options[i]).exists()) {
						exe=options[i];
						break;
					}
				}
				
				run(dir,System.err,"\""+exe+"\"",filename);	
			}
		} catch (NoWorkTreeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
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
	public static void main(String [] args) throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		File f =ScriptingEngine.fileFromGit("https://github.com/Technocopia/Graphics.git", "Graphics/SimplifiedLogo/simplified logo.svg");
		
		new SVGExternalEditor().launch(f,new Button());
	}

	@Override
	public Class getSupportedLangauge() {
		if (OSUtil.isLinux() || OSUtil.isWindows())
			return SvgLoader.class;
		return null;
	}

}
