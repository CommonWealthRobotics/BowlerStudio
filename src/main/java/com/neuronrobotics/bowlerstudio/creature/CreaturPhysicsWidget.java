package com.neuronrobotics.bowlerstudio.creature;

import java.util.HashMap;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.physics.MobileBasePhysicsManager;
import com.neuronrobotics.bowlerstudio.physics.PhysicsEngine;
import com.neuronrobotics.bowlerstudio.threed.MobileBaseCadManager;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.util.ThreadUtil;

import eu.mihosoft.vrl.v3d.CSG;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class CreaturPhysicsWidget extends GridPane {
	Button runstop = new Button("Run", AssetFactory.loadIcon("Run.png"));
	Button pauseresume = new Button("Pause", AssetFactory.loadIcon("Pause.png"));
	Button step = new Button("Step", AssetFactory.loadIcon("Step.png"));
	TextField msLoopTime =new TextField("200") ;
	int msLoopTimeInt =0;
	boolean run=false;
	boolean takestep=false;
	Thread physicsThread =null;
	public CreaturPhysicsWidget(MobileBase base){
		while(MobileBaseCadManager.get( base).getProcesIndictor().getProgress()<1){
			ThreadUtil.wait(1000);
		}

		HashMap<DHLink, CSG> simplecad = MobileBaseCadManager.getSimplecad(base) ;
		CSG baseCad=MobileBaseCadManager.getBaseCad(base);
		new MobileBasePhysicsManager(base, baseCad, simplecad);
		
		add(runstop,0,0);
		add(pauseresume,1,0);
		add(step,2,0);
		add(new Label("MS loop"),3,0);
		add(msLoopTime,4,0);
		pauseresume.setDisable(true);
		
		runstop.setOnAction(event->{
			if(run){
				runstop.setGraphic(AssetFactory.loadIcon("Run.png"));
				runstop.setText("Run");
				physicsThread.interrupt();
				PhysicsEngine.clear();
				msLoopTime.setDisable(false);
				pauseresume.setDisable(true);
			}else{
				runstop.setGraphic(AssetFactory.loadIcon("Stop.png"));
				runstop.setText("Stop");
				msLoopTime.setDisable(true);
				pauseresume.setDisable(false);
				new Thread(){
					public void run(){
						new MobileBasePhysicsManager(base, baseCad, simplecad);
						int loopTiming = (int) Double.parseDouble(msLoopTime.getText());
						physicsThread = new Thread(){
							public void run(){
								while(!Thread.interrupted() && run){
									
									long start = System.currentTimeMillis();
									PhysicsEngine.stepMs(loopTiming);
									long took = (System.currentTimeMillis() - start);
									ThreadUtil.wait((int) (loopTiming - took)/4);
								}
							}
						};
						physicsThread.start();
					}
				}.start();
				
			}
			run=!run;
		});
		
	}
}
