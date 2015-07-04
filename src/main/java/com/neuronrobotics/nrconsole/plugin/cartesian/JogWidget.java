package com.neuronrobotics.nrconsole.plugin.cartesian;

import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.ITaskSpaceUpdateListenerNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

public class JogWidget extends GridPane implements ITaskSpaceUpdateListenerNR {
	
	private AbstractKinematicsNR kin;
	Button px = new Button("+X");
	Button nx = new Button("-X");
	Button py = new Button("+Y");
	Button ny = new Button("-Y");
	Button pz = new Button("+Z");
	Button nz = new Button("-Z");
	Button home = new Button("home");
	TextField increment=new TextField("10");
	TextField sec=new TextField("1");
	Label positionx = new Label();
	Label positiony = new Label();
	Label positionz = new Label();
	Label targetx = new Label();
	Label targety = new Label();
	Label targetz = new Label();
	public JogWidget(AbstractKinematicsNR kin){
		this.kin = kin;
		
		getColumnConstraints().add(new ColumnConstraints(50)); // column 1 is 75 wide
	    getColumnConstraints().add(new ColumnConstraints(60)); // column 2 is 300 wide
	    getColumnConstraints().add(new ColumnConstraints(50)); // column 2 is 100 wide
	    getColumnConstraints().add(new ColumnConstraints(50)); // column 2 is 100 wide
	    
	    getColumnConstraints().add(new ColumnConstraints(30)); // column 1 is 75 wide
	    getColumnConstraints().add(new ColumnConstraints(80)); // column 2 is 300 wide
	    getColumnConstraints().add(new ColumnConstraints(60)); // column 2 is 100 wide
	    getColumnConstraints().add(new ColumnConstraints(80)); // column 2 is 100 wide
	    
	    getRowConstraints().add(new RowConstraints(30)); // 
	    getRowConstraints().add(new RowConstraints(30)); // 
	    getRowConstraints().add(new RowConstraints(30)); // 
	    getRowConstraints().add(new RowConstraints(30)); // 
		EventHandler<ActionEvent> l = new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				TransformNR current = kin.getCurrentTaskSpaceTransform();
				double inc;
				try{
					inc = Double.parseDouble(increment.getText());
				}catch(Exception e){
					Platform.runLater(() -> {
						increment.setText("10");
					});
					inc=100;
				}
				if(event.getSource() == px){
					current.translateX(inc);
				}
				if(event.getSource() == nx){
					current.translateX(-inc);
				}
				if(event.getSource() == py){
					current.translateY(inc);
				}
				if(event.getSource() == ny){
					current.translateY(-inc);
				}
				if(event.getSource() == pz){
					current.translateZ(inc);
				}
				if(event.getSource() == nz){
					current.translateZ(-inc);
				}
				if(event.getSource() == home){
					for(int i=0;i<kin.getNumberOfLinks();i++){
						try {
							kin.setDesiredJointAxisValue(i, 0, Double.parseDouble(sec.getText()));
							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}else{
					try {
						kin.setDesiredTaskSpaceTransform(current,  Double.parseDouble(sec.getText()));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		kin.addPoseUpdateListener(this);

		
		px.setOnAction(l);
		nx.setOnAction(l);
		py.setOnAction(l);
		ny.setOnAction(l);
		pz.setOnAction(l);
		nz.setOnAction(l);
		home.setOnAction(l);
		
		add(	ny, 
				0, 
				1);
		add(	home, 
				1, 
				1);
		add(	py, 
				2, 
				1);
		
		
		add(	px, 
				1, 
				0);
		
		add(	nx, 
				1, 
				2);
		add(	increment, 
				0, 
				3);
		add(	new Label("mm"), 
				1, 
				3);
		
		add(	sec, 
				2, 
				3);
		add(	new Label("sec"), 
				3, 
				3);
		
		add(	pz, 
				3, 
				0);
		add(	nz, 
				3, 
				1);
		
		add(	new Label("X="), 
				4, 
				0);
		add(	new Label("Y="), 
				4, 
				1);
		add(	new Label("Z="), 
				4, 
				2);
		
		add(	positionx, 
				5, 
				0);
		add(	positiony, 
				5, 
				1);
		add(	positionz, 
				5, 
				2);
		
		add(	new Label("  (to)X="), 
				6, 
				0);
		add(	new Label("  (to)Y="), 
				6, 
				1);
		add(	new Label("  (to)Z="), 
				6, 
				2);
		
		add(	targetx, 
				7, 
				0);
		add(	targety, 
				7, 
				1);
		add(	targetz, 
				7, 
				2);
	}

	@Override
	public void onTaskSpaceUpdate(AbstractKinematicsNR source, TransformNR pose) {
		// TODO Auto-generated method stub
		Platform.runLater(() -> {
			positionx.setText(DHLinkWidget.getFormatted(pose.getX()));
			positiony.setText(DHLinkWidget.getFormatted(pose.getY()));
			positionz.setText(DHLinkWidget.getFormatted(pose.getZ()));
		});
	}

	@Override
	public void onTargetTaskSpaceUpdate(AbstractKinematicsNR source,
			TransformNR pose) {
		Platform.runLater(() -> {
			targetx.setText(DHLinkWidget.getFormatted(pose.getX()));
			targety.setText(DHLinkWidget.getFormatted(pose.getY()));
			targetz.setText(DHLinkWidget.getFormatted(pose.getZ()));
		});
	}

}
