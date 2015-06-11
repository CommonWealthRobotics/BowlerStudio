package com.neuronrobotics.nrconsole.plugin.cartesian;

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;

public class JogKinematicsDevice   extends AbstractBowlerStudioTab{

	private AbstractKinematicsNR kin;
	Button px = new Button("+X");
	Button nx = new Button("-X");
	Button py = new Button("+Y");
	Button ny = new Button("-Y");
	Button pz = new Button("+Z");
	Button nz = new Button("-Z");
	TextField increment=new TextField("100");



	@Override
	public void onTabClosing() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String[] getMyNameSpaces() {
		// TODO Auto-generated method stub
		return new String[]{"bcs.cartesian.*"};
	}

	@Override
	public void initializeUI(BowlerAbstractDevice pm) {
		kin = (AbstractKinematicsNR)pm;

        ScrollPane s1 = new ScrollPane();
		HBox all = new HBox();
		VBox xy = new VBox();
		HBox x = new HBox();
		VBox z = new VBox();
		
		EventHandler<ActionEvent> l = new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				TransformNR current = kin.getCurrentTaskSpaceTransform();
				double inc;
				try{
					inc = Double.parseDouble(increment.getText());
				}catch(Exception e){
					Platform.runLater(() -> {
						increment.setText("100");
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
				try {
					kin.setDesiredTaskSpaceTransform(current, 5);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		
		px.setOnAction(l);
		nx.setOnAction(l);
		py.setOnAction(l);
		ny.setOnAction(l);
		pz.setOnAction(l);
		nz.setOnAction(l);
		
		
		z.getChildren().add(pz);
		z.getChildren().add(nz);
		
		x.getChildren().add(nx);

		x.getChildren().add(px);
		xy.getChildren().add(py);
		xy.getChildren().add(x);
		xy.getChildren().add(ny);
		HBox incrementbox = new HBox();
		incrementbox.getChildren().add(increment);
		incrementbox.getChildren().add(new Label("mm"));
		xy.getChildren().add(incrementbox);
		all.getChildren().add(xy);
		all.getChildren().add(z);

		
        s1.setContent(all);
        setContent(s1);
        setText("Kinematics Control");
		onTabReOpening();
	}

	@Override
	public void onTabReOpening() {
		// TODO Auto-generated method stub
		
	}

}
