package com.neuronrobotics.bowlerstudio.scripting;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.NoWorkTreeException;

public class SVGExternalEditor implements IExternalEditor {

	@Override
	public boolean isSupportedByExtention(File file) {
		if(SvgLoader.class.isInstance(ScriptingEngine.getLangaugeByExtention(file.getAbsolutePath()))){
			return true;
		}
		return false;
	}

	@Override
	public void launch(File file) {
		String filename = file.getAbsolutePath();
		//if(OSUtil.isWindows()) {
		//	filename="\""+filename+"\"";
		//}
		try {
			File dir = ScriptingEngine.locateGit(file).getRepository().getWorkTree();
			run(dir,"inkscape",filename);
			
		} catch (NoWorkTreeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
	}
	@Override
	public URL getInstallURL() throws MalformedURLException {
		return new URL("https://inkscape.org/release/inkscape-1.0.1/");
	}
	@Override
	public String nameOfEditor() {
		return "Inkscape";
	}
	
	public static void main(String [] args) throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		File f =ScriptingEngine.fileFromGit("https://github.com/Technocopia/Graphics.git", "Graphics/SimplifiedLogo/simplified logo.svg");
		
		new SVGExternalEditor().launch(f);
	}

}
