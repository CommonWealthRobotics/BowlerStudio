package com.neuronrobotics.bowlerstudio.creature;

import java.time.Duration;

import net.java.games.input.Component;

import org.reactfx.util.FxTimer;

import com.neuronrobotics.bowlerstudio.ConnectionManager;
import com.neuronrobotics.sdk.addons.gamepad.BowlerJInputDevice;
import com.neuronrobotics.sdk.addons.gamepad.IJInputEventListener;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.ITaskSpaceUpdateListenerNR;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.DeviceManager;
import com.neuronrobotics.sdk.common.Log;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

public class JogWidget extends GridPane implements ITaskSpaceUpdateListenerNR, IOnTransformChange,IJInputEventListener {
	
	private AbstractKinematicsNR kin;
	private MobileBase mobilebase=null;
	Button px = new Button("+X");
	Button nx = new Button("-X");
	Button py = new Button("+Y");
	Button ny = new Button("-Y");
	Button pz = new Button("+Z");
	Button nz = new Button("-Z");
	Button home = new Button("home");
	Button game = new Button("Add Game Controller");
	TextField increment=new TextField("5");
	TextField sec=new TextField(".03");
	private TransformWidget transform;
	BowlerJInputDevice gameController=null;
	double x,y,rz,slider=0;
	private boolean stop=true;
	public JogWidget(AbstractKinematicsNR kinimatics){
		this.setKin(kinimatics);
		

		getKin().addPoseUpdateListener(this);

		
		px.setOnMousePressed(	event -> handle( (Button)event.getSource() ));
		nx.setOnMousePressed(	event -> handle( (Button)event.getSource() ));
		py.setOnMousePressed(	event -> handle( (Button)event.getSource() ));
		ny.setOnMousePressed(	event -> handle( (Button)event.getSource() ));
		pz.setOnMousePressed(	event -> handle( (Button)event.getSource() ));
		nz.setOnMousePressed(	event -> handle( (Button)event.getSource() ));
		home.setOnMousePressed(	event -> handle( (Button)event.getSource() ));
		
		px.setOnMouseReleased(	event -> handle( (Button)event.getSource() ));
		nx.setOnMouseReleased(	event -> handle( (Button)event.getSource() ));
		py.setOnMouseReleased(	event -> handle( (Button)event.getSource() ));
		ny.setOnMouseReleased(	event -> handle( (Button)event.getSource() ));
		pz.setOnMouseReleased(	event -> handle( (Button)event.getSource() ));
		nz.setOnMouseReleased(	event -> handle( (Button)event.getSource() ));
		home.setOnMouseReleased(	event -> handle( (Button)event.getSource() ));
		game.setOnAction(event -> {
			if(gameController == null){
				gameController = (BowlerJInputDevice) DeviceManager.getSpecificDevice(BowlerJInputDevice.class, "jogController");
				if(gameController==null){
					ConnectionManager.onConnectGamePad("jogController");
					gameController = (BowlerJInputDevice) DeviceManager.getSpecificDevice(BowlerJInputDevice.class, "jogController");
				}
				if(gameController!=null){
					gameController.addListeners(this);
					game.setText("Remove Game Controller");
					controllerLoop();
					//TODO open a configuration panel here
				}else{
					//the controller must not be availible, bailing
				}
				
			}else{
				gameController.removeListeners(this);
				game.setText("Add Game Controller");
				gameController=null;
			}
		});
		
		
		GridPane buttons = new GridPane();
		buttons.getColumnConstraints().add(new ColumnConstraints(50)); // column 1 is 75 wide
		buttons.getColumnConstraints().add(new ColumnConstraints(60)); // column 2 is 300 wide
		buttons.getColumnConstraints().add(new ColumnConstraints(50)); // column 2 is 100 wide
		buttons. getColumnConstraints().add(new ColumnConstraints(50)); // column 2 is 100 wide
	    
		buttons.getRowConstraints().add(new RowConstraints(30)); // 
		buttons. getRowConstraints().add(new RowConstraints(30)); // 
		buttons. getRowConstraints().add(new RowConstraints(30)); // 
		buttons.getRowConstraints().add(new RowConstraints(30)); // 
		
		buttons.add(	py, 
				0, 
				1);
		buttons.add(	home, 
				1, 
				1);
		buttons.add(	ny, 
				2, 
				1);
		
		
		buttons.add(	px, 
				1, 
				0);
		
		buttons.add(	nx, 
				1, 
				2);
		buttons.add(	increment, 
				0, 
				3);
		buttons.add(	new Label("mm"), 
				1, 
				3);
		
		buttons.add(	sec, 
				2, 
				3);
		buttons.add(	new Label("seconds"), 
				3, 
				3);
		
		buttons.add(	pz, 
				3, 
				0);
		buttons.add(	nz, 
				3, 
				1);
		buttons.add(	game, 
				4, 
				0);
		add(	buttons, 
				0, 
				0);
		transform = new TransformWidget("Current Pose", getKin().getCurrentPoseTarget(), this);
		Accordion advancedPanel = new Accordion();
		advancedPanel.getPanes().add(new TitledPane("Exact Positioning", transform));
		add(	advancedPanel, 
				0, 
				1);
		controllerLoop();
	}
	
	private void handle(final Button button ){

		if(!button.isPressed()){
			// button released
			Log.warning(button.getText()+" Button released ");
			try {
				getKin().setDesiredTaskSpaceTransform(getKin().getCurrentTaskSpaceTransform(),  0);
			} catch (Exception e) {}
			if(button == px){
				x=0;
			}
			if(button == nx){
				x=0;
			}
			if(button == py){
				y=0;
			}
			if(button == ny){
				y=0;
			}
			if(button == pz){
				slider=0;
			}
			if(button == nz){
				slider=0;
			}
			stop=true;
			return;
		}else{
			Log.warning(button.getText()+" Button pressed ");
		}
		if(button == px){
			x=1;
		}
		if(button == nx){
			x=-1;
		}
		if(button == py){
			y=1;
		}
		if(button == ny){
			y=-1;
		}
		if(button == pz){
			slider=1;
		}
		if(button == nz){
			slider=-1;
		}
		if(button == home){
			home();
			stop=true;
			return;
		}else{
			try {
				double seconds =Double.parseDouble(sec.getText());
				
				FxTimer.runLater(
						Duration.ofMillis((int)(seconds*1000.0)) ,() -> {
							Log.warning(button.getText()+" Completion handler");
							if(button.isPressed()){
								handle( button);
								return;
							}
							Log.warning(button.getText()+" Still pressed");
						});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		controllerLoop();
		stop=false;
	}
	
	public void home(){
		if(getMobilebase()!=null){
			getMobilebase().setGlobalToFiducialTransform(new TransformNR());
		}
		for(int i=0;i<getKin().getNumberOfLinks();i++){
			try {
				getKin().setDesiredJointAxisValue(i, 0, Double.parseDouble(sec.getText()));
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onTaskSpaceUpdate(AbstractKinematicsNR source, TransformNR pose) {
		// TODO Auto-generated method stub
		Platform.runLater(() -> {
			transform.updatePose(pose);
		});
	}

	@Override
	public void onTargetTaskSpaceUpdate(AbstractKinematicsNR source,
			TransformNR pose) {
		Platform.runLater(() -> {
			transform.updatePose(pose);
		});
	}
	
	@Override
	public void onTransformChaging(TransformNR newTrans) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onTransformFinished(TransformNR newTrans) {
		try {
			getKin().setDesiredTaskSpaceTransform(newTrans,  Double.parseDouble(sec.getText()));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public AbstractKinematicsNR getKin() {
		return kin;
	}
	public void setKin(AbstractKinematicsNR kin) {
		if(MobileBase.class.isInstance(kin))
			setMobilebase((MobileBase)kin);
		this.kin = kin;
	}
	
	
	private void controllerLoop(){
		double seconds=.1;
		if(gameController!=null || stop==false){
			try{
				seconds =Double.parseDouble(sec.getText());
				if(!stop){ 
					
					
					double inc;
					try{
						inc = Double.parseDouble(increment.getText());
					}catch(Exception e){
						Platform.runLater(() -> {
							increment.setText("2");
						});
						inc=10;
					}
					double rxl=0;
					double ryl=inc*slider;
					double rzl=inc/4*rz;
					TransformNR current = new TransformNR(0,0,0,new RotationNR(rxl, ryl, rzl, 0));
					current.translateX(inc*x);
					current.translateY(inc*y);
					current.translateZ(inc*slider);
					
					try {
						if(getMobilebase()==null){
							current = getKin().getCurrentTaskSpaceTransform();
							current.translateX(inc*x);
							current.translateY(inc*y);
							current.translateZ(inc*slider);
							getKin().setDesiredTaskSpaceTransform(current,  seconds);
						}else{
							TransformNR toSet = current.copy();
							double toSeconds=seconds;
							new Thread(){
								public void run(){
									setName("Jog Widget Set Drive Arc Command");
									toSet.setZ(0);
									
									getMobilebase().DriveArc(toSet, toSeconds);
								}
							}.start();
						}
							
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}catch(Exception e){
				
			}
			if(seconds<.01){
				seconds=.01;
				sec.setText(".01");
			}
			FxTimer.runLater(
					Duration.ofMillis((int)(seconds*1000.0)) ,() -> {
						controllerLoop();
					});
		}
	}
	

	@Override
	public void onEvent(Component comp, net.java.games.input.Event event,
			float value, String eventString) {

		if(comp.getName().toLowerCase().contentEquals("x"))
			x=value;
		if(comp.getName().toLowerCase().contentEquals("y"))
			y=-value;
		if(comp.getName().toLowerCase().contentEquals("rz"))
			rz=value;
		if(comp.getName().toLowerCase().contentEquals("slider"))
			slider=-value;
		if(Math.abs(x)<.1)
			x=0;
		if(Math.abs(y)<.1)
			y=0;
		if(Math.abs(rz)<.1)
			rz=0;
		if(Math.abs(slider)<.1)
			slider=0;
		if(x==0.0&&y==0.0 &&rz==0.0&&slider==0) {
			//System.out.println("Stoping on="+comp.getName());
			stop=true;
			try {
				getKin().setDesiredTaskSpaceTransform(getKin().getCurrentTaskSpaceTransform(),  0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else
			stop=false;
		
		
	}

	public MobileBase getMobilebase() {
		return mobilebase;
	}

	public void setMobilebase(MobileBase mobilebase) {
		this.mobilebase = mobilebase;
	}

}
