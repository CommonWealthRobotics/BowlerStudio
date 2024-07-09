package com.neuronrobotics.bowlerstudio.scripting.external;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.FreecadLoader;
import com.neuronrobotics.bowlerstudio.scripting.IExternalEditor;
import com.neuronrobotics.bowlerstudio.scripting.SvgLoader;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.image.Image;

public class FreeCADExternalEditor implements IExternalEditor {
	private Button advanced;

	@Override
	public List<Class> getSupportedLangauge() {
		return Arrays.asList( FreecadLoader.class);
	}

	@Override
	public void launch(File file, Button button) {
		advanced=button;
		new Thread(()->{
			try {
				FreecadLoader.open(file);
			}catch(Throwable t) {
				t.printStackTrace();
			}
			onProcessExit(0);
		}).start();
	}

	@Override
	public String nameOfEditor() {
		return "FreeCAD";
	}

	@Override
	public URL getInstallURL() throws MalformedURLException {
		return new URL("https://github.com/FreeCAD/FreeCAD-Bundle/releases");
	}

	@Override
	public void onProcessExit(int ev) {
		if(advanced!=null)
		Platform.runLater(()->advanced.setDisable(false));
	}

	@Override
	public Image getImage() {
		try {
			return AssetFactory.loadAsset("Script-Tab-FreeCAD.png");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		return null;
	}

}
