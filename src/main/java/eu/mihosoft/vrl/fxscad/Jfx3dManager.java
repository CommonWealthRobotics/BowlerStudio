package eu.mihosoft.vrl.fxscad;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.ITaskSpaceUpdateListenerNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.dypid.DyPIDConfiguration;
import com.neuronrobotics.sdk.dyio.peripherals.DigitalInputChannel;
import com.neuronrobotics.sdk.javaxusb.UsbCDCSerialConnection;
import com.neuronrobotics.sdk.pid.PIDConfiguration;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SnapshotParameters;
import javafx.scene.SubScene;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;

public class Jfx3dManager extends SubScene {
	private final Group manipulator = new Group();
	private final Group lookGroup = new Group();
	private final static Group baseGroup = new Group();
	private PerspectiveCamera subSceneCamera;

	private int boxSize = 50;
	// private Box myBox = new Box(1, 1,boxSize);
	private ArrayList<Sphere> joints = new ArrayList<Sphere>();

	private final Affine rotations = new Affine();

	private DHParameterKinematics model;
	private DyIO master;

	private boolean buttonPressed = false;

	public Jfx3dManager(Pane viewContainer) {
		super(baseGroup, 500, 500, true, SceneAntialiasing.BALANCED);

		subSceneCamera = new PerspectiveCamera(false);

		setCamera(subSceneCamera);
		
		baseGroup.getTransforms().addAll(
		// new Rotate(90, Rotate.X_AXIS),
				new Rotate(180, Rotate.Y_AXIS), new Rotate(180, Rotate.Z_AXIS));
		widthProperty().bind(viewContainer.widthProperty());
		heightProperty().bind(viewContainer.heightProperty());

		Platform.runLater(() -> {

			subSceneCamera.setTranslateX(viewContainer.widthProperty()
					.divide(-1).doubleValue());
			subSceneCamera.setTranslateY(viewContainer.heightProperty()
					.divide(-1).doubleValue());

			baseGroup.setTranslateX(-viewContainer.widthProperty().divide(2)
					.doubleValue());
			baseGroup.setTranslateY(-viewContainer.heightProperty().divide(2)
					.doubleValue());
			// viewGroup.setTranslateZ(viewContainer.heightProperty().divide(2).doubleValue());
			manipulator.setTranslateX(0);
			manipulator.setTranslateY(150);
			manipulator.setTranslateZ(120);

			manipulator.getTransforms().add(new Rotate(45, Rotate.Z_AXIS));
		});

		// rotateZ.setAngle(-15);
		// rotateX.setAngle(-50);
		// Platform.runLater(() -> {
		// n.setTranslateX(-302.99);
		// n.setTranslateY(-156.00);
		// });
		
		baseGroup.getChildren().add(lookGroup);
		
		VFX3DUtil.addMouseBehavior(lookGroup, viewContainer);
	}
	
	public MeshView replaceObject(MeshView previous, MeshView current){
        if(previous!=null){
        	lookGroup.getChildren().remove(previous);
        }
        
        PhongMaterial m = new PhongMaterial(Color.RED);

        current.setCullFace(CullFace.NONE);

        current.setMaterial(m);
        
        lookGroup.getChildren().add(current);
        return current;
	}
	
	public void saveToPng(File f){
		 String fName = f.getAbsolutePath();

	        if (!fName.toLowerCase().endsWith(".png")) {
	            fName += ".png";
	        }

	        int snWidth = 1024;
	        int snHeight = 1024;

	        double realWidth = baseGroup.getBoundsInLocal().getWidth();
	        double realHeight = baseGroup.getBoundsInLocal().getHeight();

	        double scaleX = snWidth / realWidth;
	        double scaleY = snHeight / realHeight;

	        double scale = Math.min(scaleX, scaleY);

	        PerspectiveCamera snCam = new PerspectiveCamera(false);
	        snCam.setTranslateZ(-200);

	        SnapshotParameters snapshotParameters = new SnapshotParameters();
	        snapshotParameters.setTransform(new Scale(scale, scale));
	        snapshotParameters.setCamera(snCam);
	        snapshotParameters.setDepthBuffer(true);
	        snapshotParameters.setFill(Color.TRANSPARENT);

	        WritableImage snapshot = new WritableImage(snWidth, (int) (realHeight * scale));

	        baseGroup.snapshot(snapshotParameters, snapshot);

	        try {
	            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null),
	                    "png", new File(fName));
	        } catch (IOException ex) {
	            Logger.getLogger(MainController.class.getName()).
	                    log(Level.SEVERE, null, ex);
	        }
	}

	private void attachArm(String usbDev) {
		System.out.println("Using arm: " + usbDev);
		master = new DyIO(new UsbCDCSerialConnection(usbDev));

		master.connect();
		for (int i = 0; i < master.getPIDChannelCount(); i++) {
			// disable PID controller, default PID and dypid configurations are
			// disabled.
			master.ConfigureDynamicPIDChannels(new DyPIDConfiguration(i));
			master.ConfigurePIDController(new PIDConfiguration());
		}
		model = new DHParameterKinematics(master, "TrobotMaster.xml");
		Log.enableWarningPrint();
		model.addPoseUpdateListener(new ITaskSpaceUpdateListenerNR() {
			int packetIndex = 0;
			int numSkip = 1;
			int armScale = 1;

			@Override
			public void onTaskSpaceUpdate(AbstractKinematicsNR source,
					TransformNR pose) {
				ArrayList<TransformNR> jointLocations = model
						.getChainTransformations();

				if (packetIndex++ == numSkip) {
					packetIndex = 0;
					Platform.runLater(() -> {
						for (int i = 0; i < joints.size(); i++) {
							joints.get(i).setTranslateX(
									jointLocations.get(i).getX() * armScale);
							joints.get(i).setTranslateY(
									jointLocations.get(i).getY() * armScale);
							joints.get(i).setTranslateZ(
									jointLocations.get(i).getZ() * armScale);

						}
						try {
							if (buttonPressed) {
								double[][] poseRot = pose
										.getRotationMatrixArray();
								rotations.setMxx(poseRot[0][0]);
								rotations.setMxy(poseRot[0][1]);
								rotations.setMxz(poseRot[0][2]);
								rotations.setMyx(poseRot[1][0]);
								rotations.setMyy(poseRot[1][1]);
								rotations.setMyz(poseRot[1][2]);
								rotations.setMzx(poseRot[2][0]);
								rotations.setMzy(poseRot[2][1]);
								rotations.setMzz(poseRot[2][2]);
								rotations.setTx(pose.getX() * armScale);
								rotations.setTy(pose.getY() * armScale);
								rotations.setTz(pose.getZ() * armScale);
								// System.out.println("Camera Transform z="+subSceneCamera.getTranslateZ()+
								// " y="+subSceneCamera.getTranslateY()+
								// " x="+subSceneCamera.getTranslateX()+
								// " o="+subSceneCamera.getNodeOrientation());

								for (Transform t : subSceneCamera
										.getTransforms()) {
									// System.out.println(t);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					});

				}
			}

			@Override
			public void onTargetTaskSpaceUpdate(AbstractKinematicsNR source,
					TransformNR pose) {
			}
		});
		new DigitalInputChannel(master, 23).addDigitalInputListener((source,
				isHigh) -> {
			buttonPressed = !isHigh;
		});

		ArrayList<TransformNR> jointLocations = model.getChainTransformations();
		for (int i = 0; i < jointLocations.size(); i++) {
			Sphere s = new Sphere(10);
			s.setMaterial(new PhongMaterial(Color.rgb(0, i * (128 / 6), 255 - i
					* (128 / 6))));
			joints.add(s);
			manipulator.getChildren().add(s);
		}

	}
	
	public void disconnect() {
		if(master!=null){
			master.disconnect();
		}
	}
}
