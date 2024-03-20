package com.neuronrobotics.bowlerstudio.creature;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.CreatureLab3dController;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.physics.MuJoCoPhysicsManager;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.imu.IMUUpdate;
import com.neuronrobotics.sdk.addons.kinematics.imu.IMUUpdateListener;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.IDeviceConnectionEventListener;
import com.neuronrobotics.sdk.util.ThreadUtil;
import eu.mihosoft.vrl.v3d.CSG;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.mujoco.xml.attributetypes.IntegratorType;

@SuppressWarnings("restriction")
public class CreaturePhysicsWidget extends GridPane  implements IMUUpdateListener {
	Button runstop = new Button("Run", AssetFactory.loadIcon("Run.png"));
	Button pauseresume = new Button("Pause", AssetFactory.loadIcon("Pause.png"));
	Button step = new Button("Step", AssetFactory.loadIcon("Step.png"));
	TextField msLoopTime =new TextField("1") ;
	int msLoopTimeInt =0;
	private boolean run=false;
	private boolean takestep=false;
	private boolean pause=false;
	Thread physicsThread =null;
	private Set<CSG> oldParts=null;
	private MobileBase base;
	private  MuJoCoPhysicsManager mujoco;
	@SuppressWarnings("restriction")
	public CreaturePhysicsWidget(MobileBase base){

		this.base = base;
		base.addConnectionEventListener(new IDeviceConnectionEventListener() {
			
			@Override
			public void onDisconnect(BowlerAbstractDevice arg0) {
				//BowlerStudio.runLater(()->{
					stop();
				//});
			}
			
			@Override
			public void onConnect(BowlerAbstractDevice arg0) {}
		});
		add(runstop,0,0);
		add(pauseresume,1,0);
		add(step,2,0);
		add(new Label("MS loop"),3,0);
		add(msLoopTime,4,0);
		pauseresume.setDisable(true);
		step.setDisable(true);
		pauseresume.setOnAction(event->{
			if(!isPause()){
				pauseresume.setGraphic(AssetFactory.loadIcon("Resume.png"));
				pauseresume.setText("Resume");
				step.setDisable(false);
			}else{
				pauseresume.setGraphic(AssetFactory.loadIcon("Pause.png"));
				pauseresume.setText("Pause");
				step.setDisable(true);
			}
			setPause(!isPause());
		});
		step.setOnAction(event->{
			setTakestep(true);
		});
		runstop.setOnAction(event->{
			if(isRun()){
				stop();
//				new Thread(){
//					public void run(){
//						ThreadUtil.wait(50);
//						System.gc();// clean up any objects created by the physics engine
//					}
//				}.start();
			}else{
				//System.gc();// clean up any objects created by the physics engine
				runstop.setGraphic(AssetFactory.loadIcon("Stop.png"));
				runstop.setText("Stop");
				msLoopTime.setDisable(true);
				pauseresume.setDisable(false);
				base.getImu().addvirtualListeners(this);
				new Thread(){
					

					public void run(){
						while(MobileBaseCadManager.get( base).getProcesIndictor().get()<1){
							ThreadUtil.wait(100);
						}
						base.DriveArc(new TransformNR(.01,0,0,new RotationNR()), 0);
						if(mujoco!=null)
							mujoco.close();
						ArrayList<MobileBase> bases=new ArrayList<>();
						bases.add(base);
						File cache = new File(
								ScriptingEngine.getRepositoryCloneDirectory(
										base.getGitSelfSource()[0]).getAbsolutePath()+"/physics/");
						try {
							mujoco = new MuJoCoPhysicsManager(base.getScriptingName(),bases,null,null,cache);
							
						} catch (IOException | JAXBException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return;
						}
						//MobileBasePhysicsManager m =new MobileBasePhysicsManager(base, baseCad, simplecad);
						//BowlerStudio3dEngine threeD = BowlerStudioController.getBowlerStudio().getJfx3dmanager();
						oldParts = CreatureLab3dController.getEngine().getCsgMap().keySet();
						double loopTiming = (int) Double.parseDouble(msLoopTime.getText());
						mujoco.setTimestep(loopTiming/1000.0);
						mujoco.setIntegratorType(IntegratorType.IMPLICIT);
						mujoco.setCondim(4);
						try {
							mujoco.generateNewModel();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							mujoco.close();
							return;
						} // generate model before start counting time
						BowlerStudioController.clearCSG();
						BowlerStudioController.clearUserNodes();
						BowlerStudioController.addObject(mujoco.getAllCSG(),null );
						physicsThread = new Thread(){
							public void run(){
								try{
									while(isRun()){
										while(isPause() && isTakestep()==false){
											ThreadUtil.wait(0,100);
										}
										setTakestep(false);
										if(!mujoco.stepAndWait()) {
											System.err.println("MuJoCo Real time broken, expected "+mujoco.getTimestepMilliSeconds());
										}
									}
								}catch(Exception e){
									e.printStackTrace();
								}
								mujoco.close();
							}
						};
						physicsThread.start();
					}
				}.start();
				
			}
			setRun(!isRun());
		});
		
	}
	private void stop() {
		runstop.setGraphic(AssetFactory.loadIcon("Run.png"));
		runstop.setText("Run");
		if(physicsThread!=null)
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
		base.getImu().removevirtualListeners(this);
	}
	public boolean isTakestep() {
		return takestep;
	}
	public void setTakestep(boolean takestep) {
		this.takestep = takestep;
	}
	public boolean isPause() {
		return pause;
	}
	public void setPause(boolean pause) {
		this.pause = pause;
	}
	public boolean isRun() {
		return run;
	}
	public void setRun(boolean run) {
		this.run = run;
	}
	@Override
	public void onIMUUpdate(IMUUpdate arg0) {
//		System.err.println("X = "+arg0.getxAcceleration()+
//				" Y = "+arg0.getyAcceleration()+
//				" Z = "+arg0.getzAcceleration()+
//				" rX = "+arg0.getRotxAcceleration()+
//				" rY = "+arg0.getRotyAcceleration()+
//				" rZ = "+arg0.getRotzAcceleration()
//		
//				);
	}
}
