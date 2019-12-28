package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.sdk.common.Log;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.ArrayList;

public class PluginManagerWidget extends TitledPane {
	private PluginManager manager;
	private TextField deviceName = new TextField();
	private Button disconnectTHis;
	final Accordion accordion = new Accordion (); 

	public PluginManagerWidget(PluginManager m, Node graphic){
		HBox content = new HBox(20);

		content.setPadding(new Insets(0, 20, 10, 20)); 
		this.manager = m;
		ArrayList<TitledPane> plugins = manager.getPlugins();
		accordion.getPanes().addAll(plugins);
		disconnectTHis = new Button("Disconnect "+manager.getName(), AssetFactory.loadIcon("Disconnect-Device.png"));

		disconnectTHis.setOnMousePressed(	event -> {
			new Thread(){
				public void run(){
					Thread.currentThread().setUncaughtExceptionHandler(new IssueReportingExceptionHandler());

					setName("disconnect plugins");
				    	Log.warning("Disconnect button for "+manager.getName()+" pressed");
				    	getManager().getDevice().disconnect();
			    	
				}
			}.start();

		});
		setGraphic(AssetFactory.loadIcon("Bowler-Device-In-Manager.png"));
		deviceName.setOnAction(event -> {
			getManager().setName(deviceName.getText());
			setText(manager.getName());
			disconnectTHis.setText("Disconnect "+manager.getName());
		});
		Platform.runLater(()->deviceName.setText(manager.getName()));
		content.setHgrow(accordion, Priority.ALWAYS);
		content.getChildren().addAll(graphic,disconnectTHis,deviceName,accordion);
		setContent(content);
		setText(manager.getName());
	}

	public PluginManager getManager() {
		return manager;
	}
}
