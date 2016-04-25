package com.neuronrobotics.bowlerstudio.creature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.MainController;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.physics.MobileBasePhysicsManager;
import com.neuronrobotics.bowlerstudio.physics.PhysicsEngine;
import com.neuronrobotics.bowlerstudio.threed.BowlerStudio3dEngine;
import com.neuronrobotics.bowlerstudio.threed.MobileBaseCadManager;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.util.ThreadUtil;

import eu.mihosoft.vrl.v3d.CSG;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class CreaturePhysicsWidget extends GridPane {
	Button runstop = new Button("Run", AssetFactory.loadIcon("Run.png"));
	Button pauseresume = new Button("Pause", AssetFactory.loadIcon("Pause.png"));
	Button step = new Button("Step", AssetFactory.loadIcon("Step.png"));
	TextField msLoopTime =new TextField("200") ;
	int msLoopTimeInt =0;
	boolean run=false;
	boolean takestep=false;
	boolean pause=false;
	Thread physicsThread =null;
	private Set<CSG> oldParts=null;
	public CreaturePhysicsWidget(MobileBase base){

		
		add(runstop,0,0);
		add(pauseresume,1,0);
		add(step,2,0);
		add(new Label("MS loop"),3,0);
		add(msLoopTime,4,0);
		pauseresume.setDisable(true);
		step.setDisable(true);
		pauseresume.setOnAction(event->{
			if(!pause){
				pauseresume.setGraphic(AssetFactory.loadIcon("Resume.png"));
				pauseresume.setText("Resume");
				step.setDisable(false);
			}else{
				pauseresume.setGraphic(AssetFactory.loadIcon("Pause.png"));
				pauseresume.setText("Pause");
				step.setDisable(true);
			}
			pause=!pause;
		});
		step.setOnAction(event->{
			takestep=true;
		});
		runstop.setOnAction(event->{
			if(run){
				runstop.setGraphic(AssetFactory.loadIcon("Run.png"));
				runstop.setText("Run");
				physicsThread.interrupt();
				
				msLoopTime.setDisable(false);
				pauseresume.setDisable(true);
				if(oldParts!=null){
					ArrayList<CSG>oldp=new ArrayList<>();
					for(CSG c:oldParts){
						oldp.add(c);
					}
					BowlerStudioController.setCsg(oldp);
					oldParts=null;
				}
			}else{
				runstop.setGraphic(AssetFactory.loadIcon("Stop.png"));
				runstop.setText("Stop");
				msLoopTime.setDisable(true);
				pauseresume.setDisable(false);
				new Thread(){
					public void run(){
						while(MobileBaseCadManager.get( base).getProcesIndictor().getProgress()<1){
							ThreadUtil.wait(1000);
						}
						HashMap<DHLink, CSG> simplecad = MobileBaseCadManager.getSimplecad(base) ;
						CSG baseCad=MobileBaseCadManager.getBaseCad(base);
						base.DriveArc(new TransformNR(), 0);
						PhysicsEngine.clear();
						new MobileBasePhysicsManager(base, baseCad, simplecad);
						BowlerStudio3dEngine threeD = BowlerStudioController.getBowlerStudio().getJfx3dmanager();
						oldParts = threeD.getCsgMap().keySet();
						BowlerStudioController.setCsg(PhysicsEngine.getCsgFromEngine());
						int loopTiming = (int) Double.parseDouble(msLoopTime.getText());
						physicsThread = new Thread(){
							public void run(){
								try{
									while(!Thread.interrupted() && run){
										while(!Thread.interrupted() && pause && takestep==false){
											ThreadUtil.wait(loopTiming);
										}
										takestep=false;
										long start = System.currentTimeMillis();
										PhysicsEngine.stepMs(loopTiming);
										long took = (System.currentTimeMillis() - start);
										if (took < loopTiming)
											ThreadUtil.wait((int) (loopTiming - took)/4);
									}
								}catch(Exception e){
									
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
