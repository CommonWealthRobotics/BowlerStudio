package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.ui.AbstractConnectionPanel;
import com.neuronrobotics.sdk.ui.ConnectionImageIconFactory;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ConnectionManager extends Tab {
	
	private TreeItem<String> rootItem;
	private Node getIcon(String s){
		return new ImageView(
        		new Image(
        				AbstractConnectionPanel.class.getResourceAsStream(s)
        				)
        		);
	}

	public ConnectionManager(){
		setText("Connections");
		
        rootItem = new TreeItem<String> ("Connections", getIcon(
        		"images/ethernet-icon.png"
        		//"images/usb-icon.png"
        		));
        rootItem.setExpanded(true);
        for (int i = 1; i < 6; i++) {
            TreeItem<String> item = new TreeItem<String> ("Message" + i);            
            rootItem.getChildren().add(item);
        }        
        TreeView<String> tree = new TreeView<String> (rootItem); 
        
        setContent(tree);
	}
	
	public void addConnection(BowlerAbstractDevice c){
		
	}
	
	
}
