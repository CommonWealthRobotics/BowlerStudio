package com.neuronrobotics.bowlerstudio;

import java.util.ArrayList;

import com.neuronrobotics.sdk.common.DeviceManager;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class PluginManagerWidget extends HBox {
	private PluginManager manager;
	private TextField deviceName = new TextField();
	private Button disconnectAll;
	final Accordion accordion = new Accordion (); 

	public PluginManagerWidget(PluginManager manager, Node graphic){

		setSpacing(20);
		
		
		setPadding(new Insets(0, 20, 10, 20)); 
		this.setManager(manager);
		ArrayList<TitledPane> plugins = manager.getPlugins();
		accordion.getPanes().addAll(plugins);
		disconnectAll = new Button("Disconnect "+manager.getName());
		disconnectAll.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		    	getManager().getDevice().disconnect();
		    	DeviceManager.remove(getManager().getDevice());
		    }
		});
		deviceName.setOnAction(event -> {
			getManager().setName(deviceName.getText());
			disconnectAll.setText("Disconnect "+manager.getName());
		});
		Platform.runLater(()->deviceName.setText(manager.getName()));
		setHgrow(accordion, Priority.ALWAYS);
		getChildren().addAll(graphic,disconnectAll,deviceName,accordion);
		
	}

	public PluginManager getManager() {
		return manager;
	}

	public void setManager(PluginManager manager) {
		this.manager = manager;
	}
}
