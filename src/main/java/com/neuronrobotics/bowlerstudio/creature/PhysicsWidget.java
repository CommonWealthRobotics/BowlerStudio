package com.neuronrobotics.bowlerstudio.creature;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.CreatureLab3dController;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.mujoco.xml.attributetypes.IntegratorType;

@SuppressWarnings("restriction")
public class PhysicsWidget extends GridPane  implements IMUUpdateListener {
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
	private ComboBox<String> filesStatic;
	private ComboBox<String> filesMoving;
	private TextField gitStatic;
	private TextField gitMoving;
	private ArrayList<CSG> staticObjects=null;

	private ArrayList<CSG> movingObjects=null;
	
	@SuppressWarnings("restriction")
	public PhysicsWidget(MobileBase base){

		this.base = base;
		base.addConnectionEventListener(new IDeviceConnectionEventListener() {
			
			@Override
			public void onDisconnect(BowlerAbstractDevice arg0) {
				BowlerStudio.runLater(()->{
					stopPhysics();
				});
			}
			
			@Override
			public void onConnect(BowlerAbstractDevice arg0) {}
		});
		GridPane controls=new GridPane();
		BowlerStudio.setToRunButton(runstop);
		controls.add(runstop,0,0);
		controls.add(pauseresume,1,0);
		controls.add(step,2,0);
		controls.add(new Label("MS loop"),3,0);
		controls.add(msLoopTime,4,0);
		add(controls,0,0);
		add(new Label("Static Objects:"),0,1);
		gitStatic = new TextField();
		filesStatic = new ComboBox<>();
		add(gitStatic,0,2);
		add(filesStatic,1,2);

		
		add(new Label("Moving Objects:"),0,4);
		gitMoving = new TextField();
		filesMoving = new ComboBox<>();
		add(gitMoving,0,5);
		add(filesMoving,1,5);
		

		filesMoving.setDisable(true);
		filesStatic.setDisable(true);
		filesMoving.setOnAction(event->{
			updateObjects();
		});
		filesStatic.setOnAction(event->{
			updateObjects();
		});
		String string = ConfigurationDatabase.getObject("PhysicsWidget", "gitMoving", "").toString();
		gitMoving.setText( string);
		String string2 = ConfigurationDatabase.getObject("PhysicsWidget", "gitStatic", "").toString();
		gitStatic.setText( string2);
		validateInput();
		gitMoving.textProperty().addListener((observable, oldValue, newValue) -> {
			validateInput();
		});
		gitStatic.textProperty().addListener((observable, oldValue, newValue) -> {
			validateInput();
		});

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
				stopPhysics();
//				new Thread(){
//					public void run(){
//						ThreadUtil.wait(50);
//						System.gc();// clean up any objects created by the physics engine
//					}
//				}.start();
			}else{
				//System.gc();// clean up any objects created by the physics engine
//				runstop.setGraphic(AssetFactory.loadIcon("Stop.png"));
//				runstop.setText("Stop");
				BowlerStudio.setToStopButton(runstop);
				msLoopTime.setDisable(true);
				pauseresume.setDisable(false);
				base.getImu().addvirtualListeners(this);
				new Thread(){
					

					public void run(){
						while(MobileBaseCadManager.get( base).getProcesIndictor().get()<1){
							ThreadUtil.wait(100);
						}
						base.DriveArc(new TransformNR(.01,0,0,new RotationNR()), 0);
						ArrayList<MobileBase> bases=new ArrayList<>();
						bases.add(base);
						File cache = new File(ScriptingEngine.getWorkspace().getAbsolutePath()+"/physics-"+base.getScriptingName());
						try {
							mujoco = new MuJoCoPhysicsManager(base.getScriptingName(),bases,movingObjects,staticObjects,cache);
							
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
							e.printStackTrace(System.out);
							close();
							return;
						} // generate model before start counting time
						physicsThread = new Thread(){
							public void run(){
								blockingUpdateObjects();
								BowlerStudioController.clearCSG();
								BowlerStudioController.clearUserNodes();
								BowlerStudioController.addObject(mujoco.getAllCSG(),null );
								ConfigurationDatabase.save();
								try{
									while(isRun()){
										while(isPause() && isTakestep()==false){
											ThreadUtil.wait(0,100);
										}
										setTakestep(false);
										long now;
										if((now=mujoco.stepAndWait())>mujoco.getTimestepMilliSeconds()) {
											System.err.println("MuJoCo Real time broken, expected "+mujoco.getTimestepMilliSeconds()+" took: "+now);
										}
									}
								}catch(Exception e){
									e.printStackTrace();
								}
								close();
							}
						};
						physicsThread.start();
					}

					private void close() {
						if(mujoco!=null)
							mujoco.close();
						mujoco=null;
						BowlerStudio.runLater(()->{
							stopPhysics();
						});
					}
				}.start();
				
			}
			setRun(!isRun());
		});
		
	}
	private void validateInput() {
		new Thread(()->{
			validateInput(gitMoving,filesMoving,"gitMoving","movingObjects");
			validateInput(gitStatic,filesStatic,"gitStatic","staticObjects");
		}).start();
	}
	private void validateInput(TextField text,ComboBox<String> box,String key,String key2) {
		BowlerStudio.runLater(()->box.getItems().clear());
		BowlerStudio.runLater(()->box.setDisable(true));
		String text2 = text.getText();
		ConfigurationDatabase.setObject("PhysicsWidget",key ,text2);
		if(!text2.endsWith(".git"))
			return;
		if(text2.length()<=5)
			return;
		try {
			new java.net.URL(text2);
		}catch(Exception ex) {
			if(!text2.startsWith("git@")) {
				ex.printStackTrace();
				return;
			}
		}
		BowlerStudio.runLater(()->box.setDisable(false));
		
		try {
			ArrayList<String> files = ScriptingEngine.filesInGit(text2);
			
			for(String name:files) {
				BowlerStudio.runLater(()->box.getItems().add(name));
			}
			String file=ConfigurationDatabase.getObject("PhysicsWidget",key2 ,"").toString();
			if(file.length()>0) {
				boolean hasFile=false;
				for(String s:files)
					if(s.contentEquals(file)) hasFile=true;
				if(!hasFile)
					return;
				Thread.sleep(16*files.size());
				BowlerStudio.runLater(()->box.getSelectionModel().select(file));
				updateObjects();
			}
		} catch (Exception e) {
			BowlerStudio.runLater(()->box.setDisable(true));
			e.printStackTrace();
		}
	
	}
	private void updateObjects() {
		new Thread(()->{
			blockingUpdateObjects();
			
		}).start();

		
	}
	private void blockingUpdateObjects() {
		if(movingObjects!=null) {
			for(CSG c:movingObjects) {
				BowlerStudioController.removeObject(c);
			}
		}
		if(staticObjects!=null) {
			for(CSG c:staticObjects) {
				BowlerStudioController.removeObject(c);
			}
		}
		try {
			movingObjects=null;
			String selectedItem = filesMoving.getSelectionModel().getSelectedItem();
			movingObjects=loadObjects(gitMoving.getText(),selectedItem);
			ConfigurationDatabase.setObject("PhysicsWidget","movingObjects" ,selectedItem);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			staticObjects=null;
			String selectedItem = filesStatic.getSelectionModel().getSelectedItem();
			staticObjects=loadObjects(gitStatic.getText(), selectedItem);
			ConfigurationDatabase.setObject("PhysicsWidget","staticObjects" ,selectedItem);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(movingObjects!=null) {
			for(CSG c:movingObjects) {
				BowlerStudioController.addObject(c,null);
			}
		}
		if(staticObjects!=null) {
			for(CSG c:staticObjects) {
				BowlerStudioController.addObject(c,null);
			}
		}
	}
	private ArrayList<CSG> loadObjects(String url,String selectedItem) throws Exception {
		ArrayList<CSG> ret= new ArrayList<CSG>();
		if(url==null||selectedItem==null)
			return ret;
		ScriptingEngine.cloneRepo(url, null);
		ScriptingEngine.pull(url);
		Object o=ScriptingEngine.gitScriptRun(url, selectedItem);
		load(o,ret);
		return ret;
	}
	private void load(Object o,ArrayList<CSG> storage) {
		if(CSG.class.isInstance(o))
			storage.add((CSG)o);
		if(ArrayList.class.isInstance(o)) {
			ArrayList l=(ArrayList)o;
			for(int i=0;i<l.size();i++) {
				load(l.get(i),storage);
			}
		}
		if(Map.class.isInstance(o)) {
			Map l=(Map)o;
			for(Object x:l.keySet()) {
				load(l.get(x),storage);
			}
		}
	}
	
	private void stopPhysics() {
		BowlerStudio.setToRunButton(runstop);
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
