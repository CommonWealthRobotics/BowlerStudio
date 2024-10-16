package com.neuronrobotics.bowlerstudio.creature;

import java.io.File;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.assets.FontSizeManager;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class TransformWidgetTest extends Application {
	private TransformWidget w;

	public TransformWidgetTest() {
		this.w = new TransformWidget("Test Widget", new TransformNR(1,2,3), new IOnTransformChange() {
			
			@Override
			public void onTransformFinished(TransformNR newTrans) {
				System.out.println("Finished "+newTrans.toSimpleString() );
			}
			
			@Override
			public void onTransformChaging(TransformNR newTrans) {
				System.out.println("Changing "+newTrans.toSimpleString() );
			}
		});

	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		File layoutFile = AssetFactory.loadFile("layout/default.css");
		String nwfile = layoutFile.toURI().toString().replace("file:/", "file:///");
		Scene scene = new Scene(w);

		scene.getStylesheets().clear();
		scene.getStylesheets().add(nwfile);
		System.err.println("Loading CSS from " + nwfile);
		double scale = (double)(FontSizeManager.getDefaultSize())/12.0;

		primaryStage.setScene(scene);
        //primaryStage.setWidth(668*scale);
        //primaryStage.setHeight(664*scale);
        primaryStage.setTitle("Test Application");
        primaryStage.show();
	}


}
