package com.neuronrobotics.bowlerstudio.creature;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.ConnectionManager;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;
import com.neuronrobotics.bowlerstudio.scripting.IScriptEventListener;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.addons.gamepad.BowlerJInputDevice;
import com.neuronrobotics.sdk.addons.gamepad.IGameControlEvent;
import com.neuronrobotics.sdk.addons.gamepad.JogTrainerWidget;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.ITaskSpaceUpdateListenerNR;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.DeviceManager;
import com.neuronrobotics.sdk.common.IDeviceConnectionEventListener;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.util.ThreadUtil;

import eu.mihosoft.vrl.v3d.parametrics.CSGDatabase;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.RuntimeErrorException;

public class JogMobileBase extends GridPane implements IGameControlEvent,IJogProvider {
	double defauletSpeed = 0.1;
	private MobileBase mobilebase = null;
	Button px = new Button("", AssetFactory.loadIcon("Plus-X.png"));
	Button nx = new Button("", AssetFactory.loadIcon("Minus-X.png"));
	Button py = new Button("", AssetFactory.loadIcon("Plus-Y.png"));
	Button ny = new Button("", AssetFactory.loadIcon("Minus-Y.png"));
	Button pz = new Button("", AssetFactory.loadIcon("Plus-Z.png"));
	Button nz = new Button("", AssetFactory.loadIcon("Minus-Z.png"));
	Button home = new Button("", AssetFactory.loadIcon("Home.png"));
	Button game = new Button("Run Game Controller", AssetFactory.loadIcon("Add-Game-Controller.png"));
	Button conf = new Button("Configure...", AssetFactory.loadIcon("Configure-Game-Controller.png"));
	TextField increment = new TextField(Double.toString(defauletSpeed));
	TextField sec = new TextField("0.01");
	double x, y, rz, slider = 0;
	private boolean stop = true;
	private String paramsKey;
	private GridPane buttons;
	private static ArrayList<JogMobileBase> allWidgets = new ArrayList<JogMobileBase>();
	private boolean running = false;
	private Thread scriptRunner=null;
	private File currentFile = null;
	public JogMobileBase(MobileBase kinimatics) {
		allWidgets.add(this);
		if (!kinimatics.isAvailable())
			kinimatics.connect();
		mobilebase = kinimatics;
		py = new Button("", AssetFactory.loadIcon("Rotation-Z.png"));
		ny = new Button("", AssetFactory.loadIcon("Rotation-Neg-Z.png"));
		px.setOnMousePressed(event -> {
			try {
				handle((Button) event.getSource());
			} catch (Throwable T) {
				T.printStackTrace();
			}
		});
		nx.setOnMousePressed(event -> {
			try {
				handle((Button) event.getSource());
			} catch (Throwable T) {
				T.printStackTrace();
			}
		});
		py.setOnMousePressed(event -> {
			try {
				handle((Button) event.getSource());
			} catch (Throwable T) {
				T.printStackTrace();
			}
		});
		ny.setOnMousePressed(event -> {
			try {
				handle((Button) event.getSource());
			} catch (Throwable T) {
				T.printStackTrace();
			}
		});
		pz.setOnMousePressed(event -> {
			try {
				handle((Button) event.getSource());
			} catch (Throwable T) {
				T.printStackTrace();
			}
		});
		nz.setOnMousePressed(event -> {
			try {
				handle((Button) event.getSource());
			} catch (Throwable T) {
				T.printStackTrace();
			}
		});
		home.setOnMousePressed(event -> {
			try {
				handle((Button) event.getSource());
			} catch (Throwable T) {
				T.printStackTrace();
			}
		});

		px.setOnMouseReleased(event -> {
			try {
				handle((Button) event.getSource());
			} catch (Throwable T) {
				T.printStackTrace();
			}
		});
		nx.setOnMouseReleased(event -> {
			try {
				handle((Button) event.getSource());
			} catch (Throwable T) {
				T.printStackTrace();
			}
		});
		py.setOnMouseReleased(event -> {
			try {
				handle((Button) event.getSource());
			} catch (Throwable T) {
				T.printStackTrace();
			}
		});
		ny.setOnMouseReleased(event -> {
			try {
				handle((Button) event.getSource());
			} catch (Throwable T) {
				T.printStackTrace();
			}
		});
		pz.setOnMouseReleased(event -> {
			try {
				handle((Button) event.getSource());
			} catch (Throwable T) {
				T.printStackTrace();
			}
		});
		nz.setOnMouseReleased(event -> {
			try {
				handle((Button) event.getSource());
			} catch (Throwable T) {
				T.printStackTrace();
			}
		});
		home.setOnMouseReleased(event -> {
			try {
				handle((Button) event.getSource());
			} catch (Throwable T) {
				T.printStackTrace();
			}
		});
		game.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
		game.setOnAction(event -> {
			new Thread(){
	    		public void run(){
	    			pushThisMobileBaseAsKatapult();
	    			startStopAction();
	    		}
	    	}.start();
			
		});
		conf.setOnAction(event -> {
			new Thread(){
	    		public void run(){
	    			pushThisMobileBaseAsKatapult();
	    			ConfigurationDatabase.save();
	    		}
	    	}.start();
		});
		
		game.setTooltip(new Tooltip("Connect game controllers and use them to control your robot"));
		conf.setTooltip(new Tooltip("Save this robot to be used in Katapult plauncher"));

		buttons = new GridPane();
		buttons.getColumnConstraints().add(new ColumnConstraints(80)); // column 1 is 75 wide
		buttons.getColumnConstraints().add(new ColumnConstraints(80)); // column 2 is 300 wide
		buttons.getColumnConstraints().add(new ColumnConstraints(80)); // column 2 is 100 wide

		buttons.getRowConstraints().add(new RowConstraints(40)); //
		buttons.getRowConstraints().add(new RowConstraints(40)); //
		buttons.getRowConstraints().add(new RowConstraints(40)); //
		buttons.getRowConstraints().add(new RowConstraints(40)); //

		buttons.add(py, 0, 1);
		buttons.add(home, 1, 1);
		buttons.add(ny, 2, 1);

		buttons.add(px, 1, 0);

		buttons.add(nx, 1, 2);
		buttons.add(increment, 0, 3);
		buttons.add(new Label("m/s"), 1, 3);
		buttons.add(game, 0, 4);
		buttons.add(conf, 1, 4);
		

		buttons.add(sec, 2, 3);
		buttons.add(new Label("sec"), 3, 3);

		add(buttons, 0, 0);

		try {
			
			currentFile = ScriptingEngine.fileFromGit("https://github.com/OperationSmallKat/Katapult.git", "launch.groovy");
		} catch (GitAPIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mobilebase.addConnectionEventListener(new IDeviceConnectionEventListener() {
			
			@Override
			public void onDisconnect(BowlerAbstractDevice source) {
				stop();
			}
			
			@Override
			public void onConnect(BowlerAbstractDevice source) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	private void startStopAction(){
		game.setDisable(true);
		if (running)
			stop();
		else
			start();
		game.setDisable(false);
	}
	private void pushThisMobileBaseAsKatapult() {
		ConfigurationDatabase.setObject("katapult", "robotName", mobilebase.getScriptingName());
		ConfigurationDatabase.setObject("katapult", "robotGit", mobilebase.getGitSelfSource()[0]);
		ConfigurationDatabase.setObject("katapult", "robotGitFile", mobilebase.getGitSelfSource()[1]);
		ConfigurationDatabase.setObject("katapult", "linkDeviceName", mobilebase.getAllDHChains().get(0).getLinkConfiguration(0).getDeviceScriptingName());
	    @SuppressWarnings("unchecked")
		List<String> asList = (List<String>) ConfigurationDatabase.getObject("katapult", "gameControllerNames", 
				Arrays.asList("Dragon","X-Box","Game","Play"));
		
		ArrayList<String> fromLookup = BowlerJInputDevice.getControllers();
		fromLookup.addAll(asList);
		Set<String> uniques = new HashSet<String>(asList);
		
		ConfigurationDatabase.setObject("katapult", "gameControllerNames", 
				Arrays.asList(uniques.toArray())
				);
	}
	private void reset() {
		running = false;
		BowlerStudio.runLater(() -> {
			game.setText("Run Game Controller");
			//game.setGraphic(AssetFactory.loadIcon("Run.png"));
			game.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
			
		});

	}

	public void stop() {
		reset();
		Thread tmp = scriptRunner;
		if (tmp != null)
			while (tmp.isAlive()) {

				Log.debug("Interrupting");
				ThreadUtil.wait(10);
				try {
					tmp.interrupt();
					tmp.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
		scriptRunner=null;
	}
	
	private void start() {


		running = true;
		BowlerStudio.runLater(()->{
			game.setText("Stop Game Controller");
			//game.setGraphic(AssetFactory.loadIcon("Stop.png"));
			game.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
		});
		scriptRunner = new Thread() {

			public void run() {
				try {
					ScriptingEngine.inlineFileScriptRun(currentFile, null);
					reset();

				} 
				catch (Throwable ex) {

					reset();
				}

			}
		};

		try {

			scriptRunner.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void handle(final Button button) {
		JogThread.setProvider(this, mobilebase);
		if (!button.isPressed()) {
			// button released
			// Log.info(button.getText()+" Button released ");
//			try {
//				TransformNR t = getKin().getCurrentTaskSpaceTransform();
//				if(getKin().checkTaskSpaceTransform(t))
//					getKin().setDesiredTaskSpaceTransform(t,  0);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			if (button == px) {
				x = 0;
			}
			if (button == nx) {
				x = 0;
			}
			if (mobilebase == null) {
				if (button == py) {
					y = 0;
				}
				if (button == ny) {
					y = 0;
				}
			} else {
				if (button == py) {
					rz = 0;
				}
				if (button == ny) {
					rz = 0;
				}
			}
			if (button == pz) {
				slider = 0;
			}
			if (button == nz) {
				slider = 0;
			}
			stop = true;
			return;
		} else {
			Log.warning(button.getText() + " Button pressed ");
		}
		if (button == px) {
			x = 1;
		}
		if (button == nx) {
			x = -1;
		}
		if (mobilebase == null) {
			if (button == py) {
				y = 1;
			}
			if (button == ny) {
				y = -1;
			}
		} else {
			if (button == py) {
				rz = 1;
			}
			if (button == ny) {
				rz = -1;
			}
		}
		if (button == pz) {
			slider = 1;
		}
		if (button == nz) {
			slider = -1;
		}
		if (button == home) {
			home();
			stop = true;
			return;
		}
		stop = false;
	}

	public void home() {

		getMobilebase().setGlobalToFiducialTransform(new TransformNR());
		for (DHParameterKinematics c : getMobilebase().getAllDHChains()) {
			homeLimb(c);
		}
	}

	private void homeLimb(AbstractKinematicsNR c) {
		double[] joints = c.getCurrentJointSpaceVector();
		for (int i = 0; i < c.getNumberOfLinks(); i++) {
			joints[i] = 0;
		}
		try {
			c.setDesiredJointSpaceVector(joints, 0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	@Override
	public void onEvent(String name, float value) {
		JogThread.setProvider(this, mobilebase);
		if (name.toLowerCase()
				.contentEquals((String) ConfigurationDatabase.getObject(paramsKey, "jogKiny", "y")))
			x = value;
		if (name.toLowerCase()
				.contentEquals((String) ConfigurationDatabase.getObject(paramsKey, "jogKinz", "rz")))
			y = value;
		if (name.toLowerCase()
				.contentEquals((String) ConfigurationDatabase.getObject(paramsKey, "jogKinx", "x")))
			rz = -value;
		if (name.toLowerCase()
				.contentEquals((String) ConfigurationDatabase.getObject(paramsKey, "jogKinslider", "slider")))
			slider = -value;
		if (Math.abs(x) < .01)
			x = 0;
		if (Math.abs(y) < .01)
			y = 0;
		if (Math.abs(rz) < .01)
			rz = 0;
		if (Math.abs(slider) < .01)
			slider = 0;
		if (x == 0.0 && y == 0.0 && rz == 0.0 && slider == 0) {
			// System.out.println("Stoping on="+comp.getName());
			stop = true;

		} else
			stop = false;

	}

	public MobileBase getMobilebase() {
		return mobilebase;
	}

	public void setMobilebase(MobileBase mobilebase) {
		this.mobilebase = mobilebase;
	}

	@Override
	public TransformNR getJogIncrement() {
		if (!stop) {

			double inc;
			try {
				inc = Double.parseDouble(increment.getText()) * 10;// convert to mm

			} catch (Exception e) {
				inc = defauletSpeed;
				BowlerStudio.runLater(() -> {
					try {
						increment.setText(Double.toString(defauletSpeed));
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				});
			}
			// double rxl=0;
			double ryl = inc / 20 * slider;
			double rzl = inc / 2 * rz;
			TransformNR current = new TransformNR(0, 0, 0, new RotationNR(0, rzl, 0));
			current.translateX(inc * x);
			current.translateY(inc * y);
			current.translateZ(inc * slider);
			TransformNR toSet = current.copy();
			return toSet;
		}
		return null;
	}



}
