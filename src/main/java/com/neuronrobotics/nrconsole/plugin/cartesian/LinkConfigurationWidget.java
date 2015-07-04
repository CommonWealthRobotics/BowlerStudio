package com.neuronrobotics.nrconsole.plugin.cartesian;

import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;

import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;

public class LinkConfigurationWidget extends GridPane {
	
	private int index;
	private AbstractKinematicsNR congiuration;
	private LinkConfiguration conf;
	public LinkConfigurationWidget(int index, AbstractKinematicsNR congiuration){
		this.index = index;
		this.congiuration = congiuration;
		conf = congiuration.getLinkConfiguration(index);
		getColumnConstraints().add(new ColumnConstraints(200)); // column 1 is 75 wide
	    getColumnConstraints().add(new ColumnConstraints(100)); // column 2 is 300 wide
 
	    
	    TextField scale = new TextField(DHLinkWidget.getFormatted(conf.getScale()));
	    scale.setOnAction(event -> {
			conf.setScale(Double.parseDouble(scale.getText()));
		});
	    
	    add(	new Text("Scale To Degrees "), 
	    		0, 
	    		0);
		add(	scale, 
				1, 
				0);
		 add(	new Text("(unitless)"), 
		    		2, 
		    		0);
		getRowConstraints().add(new RowConstraints(50)); // 
		
	    TextField offset = new TextField(DHLinkWidget.getFormatted(conf.getStaticOffset()));
		offset.setOnAction(event -> {
			conf.setStaticOffset(Integer.parseInt(offset.getText()));
		});
	    
	    add(	new Text("Offset (device units)"), 
	    		0, 
	    		1);
		add(	offset, 
				1, 
				1);
		 add(	new Text("(device units)"), 
		    		2, 
		    		1);
		getRowConstraints().add(new RowConstraints(50)); // 
	}

}
