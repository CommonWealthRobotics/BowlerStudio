package com.neuronrobotics.bowlerstudio.scripting;

import java.io.File;
import java.util.ArrayList;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ExternalEditorController {
	private File currentFile;
	boolean hasEditor = false;
	private Button advanced = new Button();
	private ImageView image=new ImageView();
	private static ArrayList<IExternalEditor> editors=new ArrayList<IExternalEditor>();
	static {
		editors.add(new SVGExternalEditor());
		editors.add(new GroovyEclipseExternalEditor());
		editors.add(new ArduinoExternalEditor());
	}
	private IExternalEditor myEditor=null;
	public ExternalEditorController(File f, CheckBox autoRun){
		this.currentFile = f;
		try {
			Image loadAsset = AssetFactory.loadAsset("Script-Tab-"+ScriptingEngine.getShellType(currentFile.getName())+".png");
			image.setImage(loadAsset);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		for(IExternalEditor e:editors) {
			if(e.isSupportedByExtention(f)) {
				hasEditor=true;
				myEditor=e;
				image.setImage(e.getImage());
				break;
			}else {
				System.err.println("File "+f.getName()+" is not supported by "+e.getClass());
			}
			
		}
		if(hasEditor) {
			
			advanced.setGraphic(image);
			advanced.setTooltip(new Tooltip("Click here to launch "+myEditor.nameOfEditor()+" the advanced editor for this file"));
			advanced.setText(myEditor.nameOfEditor());
			
			advanced.setOnAction(event -> {
				advanced.setDisable(true);
				myEditor.launch(currentFile,advanced);
				//autoRun.setSelected(true);
			});
		}

	}
	
	public Node getControl() {
		if(hasEditor)
			return advanced;
		else
			return image;
	}
	
}
