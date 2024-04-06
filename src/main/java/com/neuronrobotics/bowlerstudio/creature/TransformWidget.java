package com.neuronrobotics.bowlerstudio.creature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.NewVitaminWizardController;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.util.ThreadUtil;

import eu.mihosoft.vrl.v3d.JavaFXInitializer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import javafx.scene.control.Label;

public class TransformWidget extends GridPane implements IOnEngineeringUnitsChange, EventHandler<ActionEvent> {

	public IOnTransformChange onChange;
	// EngineeringUnitsSliderWidget rw;
	public EngineeringUnitsSliderWidget tilt;
	public EngineeringUnitsSliderWidget elevation;
	public EngineeringUnitsSliderWidget azimuth;
	public EngineeringUnitsSliderWidget tx;
	public EngineeringUnitsSliderWidget ty;
	public EngineeringUnitsSliderWidget tz;
//	public TextField tx;
//	public TextField ty;
//	public TextField tz;
	public TransformNR initialState;
	public RotationNR storeRotation;
	public double linearIncrement = 1;
	public double rotationIncrement = 5;
	public Button game = new Button("Jog With Game Controller", AssetFactory.loadIcon("Add-Game-Controller.png"));
	public boolean running = false;
	public Thread scriptRunner = null;
	private String title;
	private TransformWidget self;
	private Label mode= new Label("");

	public TransformWidget(String title, TransformNR is, IOnTransformChange onChange) {
		this.title = title;
		self=this;
		initialState = is.copy();
		this.onChange=(onChange);
//		tx = new TextField(CreatureLab.getFormatted(initialState.getX()));
//		ty = new TextField(CreatureLab.getFormatted(initialState.getY()));
//		tz = new TextField(CreatureLab.getFormatted(initialState.getZ()));
//		tx.setOnAction(this);
//		ty.setOnAction(this);
//		tz.setOnAction(this);
		tx = new EngineeringUnitsSliderWidget(this, initialState.getX(), 100, "mm");
		ty = new EngineeringUnitsSliderWidget(this, initialState.getY(), 100, "mm");
		tz = new EngineeringUnitsSliderWidget(this, initialState.getZ(), 100, "mm");

		storeRotation = initialState.getRotation();
		double t = 0;
		try {
			t = Math.toDegrees(storeRotation.getRotationTilt());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		double e = 0;
		try {
			e = Math.toDegrees(storeRotation.getRotationElevation());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		double a = 0;
		try {
			a = Math.toDegrees(storeRotation.getRotationAzimuth());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		tilt = new EngineeringUnitsSliderWidget(new IOnEngineeringUnitsChange() {

			@Override
			public void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
				initialState.setTiltDegrees(newAngleDegrees);
				onChange.onTransformChaging(getCurrent());
			}

			@Override
			public void onSliderDoneMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
				initialState.setTiltDegrees(newAngleDegrees);
				onChange.onTransformFinished(getCurrent());

			}
		}, -179.99, 179.99, t, 100, "degrees");
		elevation = new EngineeringUnitsSliderWidget(new IOnEngineeringUnitsChange() {

			@Override
			public void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
				initialState.setElevationDegrees(newAngleDegrees);
				onChange.onTransformChaging(getCurrent());
			}

			@Override
			public void onSliderDoneMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
				initialState.setElevationDegrees(newAngleDegrees);
				onChange.onTransformFinished(getCurrent());
			}
		}, -89.99, 89.99, e, 100, "degrees");
		azimuth = new EngineeringUnitsSliderWidget(new IOnEngineeringUnitsChange() {

			@Override
			public void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
				initialState.setAzimuthDegrees(newAngleDegrees);
				onChange.onTransformChaging(getCurrent());
			}

			@Override
			public void onSliderDoneMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
				initialState.setAzimuthDegrees(newAngleDegrees);
				onChange.onTransformFinished(getCurrent());
			}
		}, -179.99, 179.99, a, 100, "degrees");
		tilt.setAllowResize(false);
		elevation.setAllowResize(false);
		azimuth.setAllowResize(false);
		getColumnConstraints().add(new ColumnConstraints(60)); // translate text
		getColumnConstraints().add(new ColumnConstraints(200)); // translate values
		getColumnConstraints().add(new ColumnConstraints(60)); // units
		getColumnConstraints().add(new ColumnConstraints(60)); // rotate text
		setHgap(20);// gab between elements

		tx.showSlider(false);
		ty.showSlider(false);
		tz.showSlider(false);
		setIncrements();

		reset();
		game.setOnAction(event -> {
			new Thread() {
				public void run() {
					startStopAction();
				}
			}.start();

		});
		game.setTooltip(new Tooltip("Connect game controllers and use them jog the item around. Use the joysticks to move. \nPress X to Translate. \nPress Y to rotate. \nPress A to exit"));

		add(new Label(title), 1, 0);
		add(mode, 1, 1);
		add(game, 0, 0);

		// These all seem out of order here, but it is because the
		// screen is rotating the orenation of this interface from
		// BowlerStudio3dEngine.getOffsetforvisualization()
		// X line

		int startIndex = 3;
		TextField lin = new TextField(linearIncrement + "");
		TextField rot = new TextField(rotationIncrement + "");
		lin.setOnAction(ac -> {
			linearIncrement = Double.parseDouble(lin.getText());
			setIncrements();
		});
		rot.setOnAction(ac -> {
			rotationIncrement = Double.parseDouble(rot.getText());
			setIncrements();
		});

		add(new Label("Linear "), 0, startIndex-1);
		add(new Label("Rotation "), 0, startIndex);

		add(lin, 1, startIndex-1);
		add(rot, 1, startIndex);
		add(new Label("(mm)"), 2, startIndex-1);
		add(new Label("(degrees)"), 2, startIndex);

		add(new Label("X"), 0, 1 + startIndex);
		add(tx, 1, 1 + startIndex);

		// Y line
		add(new Label("Y"), 0, 2 + startIndex);
		add(ty, 1, 2 + startIndex);

		// Z line
		add(new Label("Z"), 0, 3 + startIndex);
		add(tz, 1, 3 + startIndex);
		add(new Label("Tilt"), 0, 4 + startIndex);
		add(tilt, 1, 4 + startIndex);

		add(new Label("Elevation"), 0, 5 + startIndex);
		add(elevation, 1, 5 + startIndex);

		add(new Label("Azimuth"), 0, 6 + startIndex);
		add(azimuth, 1, 6 + startIndex);
		// game
		

		updatePose(is);
	}
	
	public String toString() {
		return title+" "+initialState.toSimpleString();
	}

	public void startStopAction() {
		game.setDisable(true);
		if (running)
			stop();
		else
			try {
				start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		game.setDisable(false);
	}
	public void setMode(String m) {
		BowlerStudio.runLater(()->{
			mode.setText(m+" Mode");
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

		scriptRunner = null;
	}

	public void start() throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		File currentFile = ScriptingEngine.fileFromGit("https://github.com/OperationSmallKat/Katapult.git",
				"jogWidget.groovy");
		

		running = true;
		BowlerStudio.runLater(() -> {
			BowlerStudio.setToStopButton(game);
		});
		scriptRunner = new Thread() {

			public void run() {
				try {
					ArrayList<Object> args = new ArrayList<>();
					args.add(self);
					ScriptingEngine.inlineFileScriptRun(currentFile, args);
					reset();

				} catch (Throwable ex) {
					ex.printStackTrace();
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

	public void reset() {
		running = false;
		BowlerStudio.runLater(() -> {
			game.setText("Run Game Controller");
			// game.setGraphic(AssetFactory.loadIcon("Run.png"));
			for(String classes : game.getStyleClass()) {
				System.out.println("Clearing "+classes);
			}
			BowlerStudio.setToRunButton(game);
			game.setGraphic(AssetFactory.loadIcon("Add-Game-Controller.png"));

		});

	}

	public void setIncrements() {
		tx.setJogIncrement(linearIncrement);
		ty.setJogIncrement(linearIncrement);
		tz.setJogIncrement(linearIncrement);
		tilt.setJogIncrement(rotationIncrement);
		elevation.setJogIncrement(rotationIncrement);
		azimuth.setJogIncrement(rotationIncrement);
	}

	public TransformNR getCurrent() {
		TransformNR tmp = new TransformNR(tx.getValue(), ty.getValue(), tz.getValue(), initialState.getRotation());

		return tmp;
	}

	@Override
	public void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
		getOnChange().onTransformChaging(getCurrent());
	}

	@Override
	public void onSliderDoneMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
		handle(null);
	}

	@Override
	public void handle(ActionEvent event) {
		getOnChange().onTransformChaging(getCurrent());
		getOnChange().onTransformFinished(getCurrent());
	}

	public void updatePose(TransformNR p) {
		initialState = p.copy();

		tx.setValue(initialState.getX());
		ty.setValue(initialState.getY());
		tz.setValue(initialState.getZ());

		RotationNR rot = initialState.getRotation();
		double t = 0;
		try {
			t = Math.toDegrees(rot.getRotationTilt());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		double e = 0;
		try {
			e = Math.toDegrees(rot.getRotationElevation());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		double a = 0;
		try {
			a = Math.toDegrees(rot.getRotationAzimuth());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		double tiltVar = t;
		double eVar = e;
		double aVar = a;
		tilt.setValue(tiltVar);
		elevation.setValue(eVar);
		azimuth.setValue(aVar);
		// Set the rotation after setting the UI so the read will load the rotation in
		// its pure form

	}

	public static void main(String[] args) {
		JavaFXInitializer.go();
		BowlerStudio.runLater(() -> {
			Stage s = new Stage();
			// new Thread(() -> {
			TransformWidgetTest controller = new TransformWidgetTest();

			try {
				controller.start(s);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// }).start();
		});
	}

	/**
	 * @return the onChange
	 */
	public IOnTransformChange getOnChange() {
		return onChange;
	}

	/**
	 * @param onChange the onChange to set
	 */
	public void setOnChange(IOnTransformChange onChange, TransformNR initial) {
		this.onChange = onChange;
		initialState = initial;
		updatePose(initial);
	}

}
