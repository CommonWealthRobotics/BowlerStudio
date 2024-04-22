package com.neuronrobotics.bowlerstudio.tabs;

import java.io.File;
import java.time.Duration;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.assets.FontSizeManager;
import com.neuronrobotics.bowlerstudio.creature.VitaminWidgetTest;
import com.neuronrobotics.bowlerstudio.creature.VitatminWidget;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;

import eu.mihosoft.vrl.v3d.JavaFXInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.stage.Stage;

public class WebTabTest extends Application {
	private WebTabController tw;

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = AssetFactory.loadLayout("layout/WebTabLayout.fxml");
		loader.setClassLoader(WebTabController.class.getClassLoader());
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
		primaryStage.setTitle("Web Tab Test Application");
		primaryStage.show();

	}

	public static void main(String[] args) {
		JavaFXInitializer.go();

		BowlerStudio.runLater(() -> {
			Stage s = new Stage();
			//
			WebTabTest controller = new WebTabTest();
			try {
				controller.start(s);

			} catch (Exception e) {
				e.printStackTrace();
			}
			WebTabController tw = controller.getTw();
			new Thread(() -> {
				FontSizeManager.setFontSize(48);
			}).start();
		});
	}

	/**
	 * @return the tw
	 */
	public WebTabController getTw() {
		return tw;
	}


}
