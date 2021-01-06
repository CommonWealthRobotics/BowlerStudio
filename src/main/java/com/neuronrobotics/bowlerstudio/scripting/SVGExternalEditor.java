package com.neuronrobotics.bowlerstudio.scripting;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.NoWorkTreeException;

import com.neuronrobotics.video.OSUtil;

import javafx.scene.control.Button;

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
			File dir = ScriptingEngine.locateGit(file).getRepository().getWorkTree();
			if(OSUtil.isLinux())
				run(dir,"inkscape",filename);
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
				
				run(dir,"\""+exe+"\"",filename);	
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
