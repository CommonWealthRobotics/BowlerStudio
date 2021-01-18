package com.neuronrobotics.bowlerstudio.creature;

import com.neuronrobotics.bowlerstudio.ConnectionManager;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;
import com.neuronrobotics.sdk.addons.gamepad.BowlerJInputDevice;
import com.neuronrobotics.sdk.addons.gamepad.IGameControlEvent;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.ITaskSpaceUpdateListenerNR;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.DeviceManager;
import com.neuronrobotics.sdk.common.Log;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import org.reactfx.util.FxTimer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;

public class JogWidget extends GridPane
		implements ITaskSpaceUpdateListenerNR, IOnTransformChange, IGameControlEvent {
	double defauletSpeed = 0.2;
	private DHParameterKinematics kinematics;
	Button px = new Button("", AssetFactory.loadIcon("Plus-X.png"));
	Button nx = new Button("", AssetFactory.loadIcon("Minus-X.png"));
	Button py = new Button("", AssetFactory.loadIcon("Plus-Y.png"));
	Button ny = new Button("", AssetFactory.loadIcon("Minus-Y.png"));
	Button pz = new Button("", AssetFactory.loadIcon("Plus-Z.png"));
	Button nz = new Button("", AssetFactory.loadIcon("Minus-Z.png"));
	Button home = new Button("", AssetFactory.loadIcon("Home.png"));
	Button game = new Button("Add Game Controller", AssetFactory.loadIcon("Add-Game-Controller.png"));
	Button conf = new Button("Configure...", AssetFactory.loadIcon("Configure-Game-Controller.png"));
	TextField increment = new TextField(Double.toString(defauletSpeed));
	TextField sec = new TextField("0.01");
	private TransformWidget transformCurrent;
	private TransformWidget transformTarget;
	private BowlerJInputDevice gameController = null;
	double x, y, rz, slider = 0;
	private boolean stop = true;
	private String paramsKey;
	private GridPane buttons;
	private static ArrayList<JogWidget> allWidgets = new ArrayList<JogWidget>();
	private MobileBase source;

	public JogWidget(DHParameterKinematics k, MobileBase source) {
		this.source = source;
		allWidgets.add(this);
		this.setKin(k);

		getKin().addPoseUpdateListener(this);

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
		game.setOnAction(event -> {
			if (getGameController() == null) {
				setGameController((BowlerJInputDevice) DeviceManager.getSpecificDevice(BowlerJInputDevice.class,
						"jogController"));
				if (getGameController() == null) {
					ConnectionManager.onConnectGamePad();
					setGameController((BowlerJInputDevice) DeviceManager.getSpecificDevice(BowlerJInputDevice.class,
							"jogController"));
				}

			} else {
				RemoveGameController();
			}
		});
		conf.setOnAction(event -> {
			if (getGameController() != null) {
				runControllerMap();
			}
		});

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

		buttons.add(sec, 2, 3);
		buttons.add(new Label("sec"), 3, 3);
		buttons.add(pz, 3, 0);
		buttons.add(nz, 3, 1);

		add(buttons, 0, 0);
		transformCurrent = new TransformWidget("Current Pose", getKin().getCurrentTaskSpaceTransform(), this);
		transformCurrent.setDisable(true);
		transformTarget = new TransformWidget("Current Target", getKin().getCurrentPoseTarget(), this);

		Accordion advancedPanel = new Accordion();
		advancedPanel.getPanes().add(new TitledPane("Current Pose", transformCurrent));
		advancedPanel.getPanes().add(new TitledPane("Current Target", transformTarget));
		add(advancedPanel, 0, 1);

		controllerLoop();

	}

	private BowlerJInputDevice RemoveGameController() {
		BowlerJInputDevice stale = getGameController();
		getGameController().removeListeners(this);
		game.setText("Add Game Controller");
		setGameController(null);
		return stale;
	}

	private void handle(final Button button) {

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

			if (button == py) {
				y = 0;
			}
			if (button == ny) {
				y = 0;
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
		if (button == py) {
			y = 1;
		}
		if (button == ny) {
			y = -1;
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
		controllerLoop();
	}

	public void home() {
		new Thread(() -> {
			homeLimb(getKin());
		}).start();
	}

	private void homeLimb(AbstractKinematicsNR c) {

		TransformNR t = c.calcHome();
		try {
			c.setDesiredTaskSpaceTransform(t, 0);
		} catch (Exception e) {
			double[] joints = c.getCurrentJointSpaceVector();
			for (int i = 0; i < c.getNumberOfLinks(); i++) {
				joints[i] = 0;
			}
			try {
				c.setDesiredJointSpaceVector(joints, 0);
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void onTaskSpaceUpdate(AbstractKinematicsNR source, TransformNR pose) {
		// TODO Auto-generated method stub
		if (pose != null && transformCurrent != null)
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					transformCurrent.updatePose(getKin().getCurrentTaskSpaceTransform());
				}
			});
	}

	@Override
	public void onTargetTaskSpaceUpdate(AbstractKinematicsNR source, TransformNR pose) {
		if (pose != null && transformTarget != null)
			transformTarget.updatePose(getKin().getCurrentPoseTarget());
	}

	@Override
	public void onTransformChaging(TransformNR newTrans) {
		// TODO Auto-generated method stub
		JogThread.setTarget(getKin(), newTrans, 0);
	}

	@Override
	public void onTransformFinished(TransformNR newTrans) {
		JogThread.setTarget(getKin(), newTrans, 0);
//		new Thread(() -> {
//			try {
//				getKin().setDesiredTaskSpaceTransform(newTrans, Double.parseDouble(sec.getText()));
//
//			} catch (NumberFormatException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (Throwable e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}).start();

	}

	public DHParameterKinematics getKin() {
		if (source.getParallelGroup(kinematics) != null) {
			return source.getParallelGroup(kinematics);
		}

		return kinematics;
	}

	public void setKin(DHParameterKinematics kin) {
		if (!kin.isAvailable())
			kin.connect();
		this.kinematics = kin;

//		try {
//			kin.setDesiredTaskSpaceTransform(kin.calcHome(), 0);
//		} catch (Exception e) {
//		}
	}

	private void controllerLoop() {
		new Thread(() -> {
			// System.out.println("controllerLoop");
			double seconds = .1;
			if (getGameController() != null || stop == false) {
				try {
					seconds = Double.parseDouble(sec.getText());
					if (!stop) {

						double inc;
						try {
							inc = Double.parseDouble(increment.getText()) * 1000 * seconds;// convert to mm

						} catch (Exception e) {
							inc = defauletSpeed;
							Platform.runLater(() -> {
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

						try {

							current = getKin().getCurrentPoseTarget().copy();
							current.translateX(inc * x);
							current.translateY(inc * y);
							current.translateZ(inc * slider);
							// current.setRotation(new RotationNR());
							double toSeconds = seconds;
							if (!JogThread.setTarget(getKin(), current, toSeconds)) {
								current.translateX(-inc * x);
								current.translateY(-inc * y);
								current.translateZ(-inc * slider);
							}
							// Log.enableDebugPrint();
							// System.out.println("Loop Jogging to: "+toSet);

						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (seconds < .01) {
					seconds = .01;
					sec.setText(".01");
				}
				FxTimer.runLater(Duration.ofMillis((int) (seconds * 1000.0)), new Runnable() {
					@Override
					public void run() {

						controllerLoop();

					}
				});
			}
		}).start();
	}

	@Override
	public void onEvent(String name, float value) {

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
			try {
				getKin().setDesiredTaskSpaceTransform(getKin().getCurrentTaskSpaceTransform(), 0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else
			stop = false;

	}

	public BowlerJInputDevice getGameController() {
		return gameController;
	}

	public void setGameController(BowlerJInputDevice gameController) {
		this.gameController = gameController;
		if (gameController != null) {
			getGameController().clearListeners();
			getGameController().addListeners(this);
			game.setText("Remove Game Controller");
			controllerLoop();
			paramsKey = gameController.getControllerName();
			HashMap<String, Object> map = ConfigurationDatabase.getParamMap(paramsKey);
			boolean hasmap = false;
			if (map.containsKey("jogKinx") && map.containsKey("jogKiny") && map.containsKey("jogKinz")
					&& map.containsKey("jogKinslider")) {
				hasmap = true;
			}

			if (!hasmap) {
				runControllerMap();
			}
		}
	}

	private void runControllerMap() {
		Stage s = new Stage();
		new Thread() {
			public void run() {
				JogTrainerWidget controller = new JogTrainerWidget(gameController);
				try {
					controller.start(s);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	public void setCurrent(TransformNR currentPoseTarget) {
		JogThread.setTarget(getKin(), currentPoseTarget, 0);
	}

}
