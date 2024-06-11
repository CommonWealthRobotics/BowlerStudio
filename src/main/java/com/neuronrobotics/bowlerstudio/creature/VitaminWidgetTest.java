package com.neuronrobotics.bowlerstudio.creature;

import java.io.File;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.assets.FontSizeManager;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;

import eu.mihosoft.vrl.v3d.JavaFXInitializer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;

public class VitaminWidgetTest extends Application {
	private VitatminWidget tw;

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = AssetFactory.loadLayout("layout/AddRemoveVitamins.fxml");
		loader.setClassLoader(VitatminWidget.class.getClassLoader());
		Parent w = loader.load();
		
		tw = loader.getController();

		File layoutFile = AssetFactory.loadFile("layout/default.css");
		String nwfile = layoutFile.toURI().toString().replace("file:/", "file:///");
		Scene scene = new Scene(w);

		scene.getStylesheets().clear();
		scene.getStylesheets().add(nwfile);
		System.err.println("Loading CSS from " + nwfile);
		FontSizeManager.addListener(fontNum->{
			int tmp = fontNum-10;
			if(tmp<12)
				tmp=12;
			System.out.println("Setting font size to "+fontNum);
			w.setStyle("-fx-font-size: "+tmp+"pt");
		});
		primaryStage.setOnCloseRequest(arg0 -> {
			System.exit(0);
		});
		primaryStage.setScene(scene);
		primaryStage.setWidth(600);
		primaryStage.setHeight(777);
		primaryStage.setTitle("Test Application");
		primaryStage.show();
	}

	public static void main(String[] args) {
		JavaFXInitializer.go();
		BowlerStudio.runLater(() -> {
			Stage s = new Stage();
			//
			VitaminWidgetTest controller = new VitaminWidgetTest();
			try {
				controller.start(s);
			} catch (Exception e) {
				e.printStackTrace();
			}
			VitatminWidget tw = controller.getTw();
			new Thread(() -> {
				try {
					MobileBase mb = (MobileBase) ScriptingEngine
							.gitScriptRun("https://github.com/NeuronRobotics/NASACurisoity.git", "NASA_Curiosity.xml");
					tw.setVitaminProvider(mb.getAllDHChains().get(0).getLinkConfiguration(0),selected->{
						 return mb.forwardOffset(new TransformNR()); 
					});
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}).start();
		});
	}

	/**
	 * @return the tw
	 */
	public VitatminWidget getTw() {
		return tw;
	}

	/**
	 * @param tw the tw to set
	 */
	public void setTw(VitatminWidget tw) {
		if (tw == null)
			throw new RuntimeException();
		this.tw = tw;
	}
}
