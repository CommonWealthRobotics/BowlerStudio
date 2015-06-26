package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.sdk.common.DeviceManager;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.HBox;

public class PluginManagerWidget extends HBox {
	private PluginManager manager;
	private TreeItem<String> rootItem;

	public PluginManagerWidget(PluginManager manager, Node graphic){
		super(20);
		this.setManager(manager);
		rootItem = new TreeItem<String>(manager.getName(), graphic);
		TreeView<String> tree = new TreeView<String>(rootItem);
		manager.setTree(rootItem);
		tree.setCellFactory(CheckBoxTreeCell.forTreeView());
		rootItem.setExpanded(false);
		Button disconnectAll = new Button("Disconnect "+manager.getName());
		disconnectAll.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		    	getManager().getDevice().disconnect();
		    	DeviceManager.remove(getManager().getDevice());
		    }
		});
		
		getChildren().addAll(disconnectAll,tree);
		
	}

	public PluginManager getManager() {
		return manager;
	}

	public void setManager(PluginManager manager) {
		this.manager = manager;
	}
}
