package com.neuronrobotics.bowlerstudio.creature;

import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;

import javafx.application.Application;
import javafx.fxml.FXML;
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
		
		primaryStage.setScene(new Scene(w));
        primaryStage.setWidth(400);
        primaryStage.setHeight(600);
        primaryStage.setTitle("Test Application");
        primaryStage.show();
	}


}
