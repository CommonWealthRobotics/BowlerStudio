package com.neuronrobotics.bowlerstudio.threed;

import java.util.ArrayList;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.physics.TransformFactory;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Affine;

public class VirtualCameraMobileBase {
	private TransformNR myGlobal = new TransformNR();
	double azOffset = 0;
	double elOffset = 0;
	double tlOffset = 0;
	TransformNR pureTrans = new TransformNR();
	private static final int DEFAULT_ZOOM_DEPTH = -1500;
	private PerspectiveCamera camera;
	private Group hand;
	private final Group cameraFrame = new Group();

	private double zoomDepth = getDefaultZoomDepth();
	private Affine zoomAffine = new Affine();
	private static final Affine offset = new Affine();
	private Group manipulationFrame;
	long timeSinceLastUpdate=System.currentTimeMillis()-17;
	boolean error=false;
	

	private Affine camerUserPerspective=new Affine();
	private VirtualCameraMobileBase flyingCamera;
	private TransformNR newPose=new TransformNR();
	private boolean zoomlock;
	private ArrayList<ICameraChangeListener> listeners = new ArrayList<>();
	
	

	public VirtualCameraMobileBase(PerspectiveCamera camera, Group hand,ICameraChangeListener lis) {
		this.hand = hand;
		this.setCamera(camera);
		addListener(lis);
		// System.out.println("Setting camera frame transform");

		manipulationFrame = new Group();
		camera.getTransforms().add(zoomAffine);
		BowlerStudio.runLater(
				() -> TransformFactory.nrToAffine(new TransformNR(0, 0, 0, new RotationNR(180, 0, 0)), offset));
		cameraFrame.getTransforms().add(getOffset());
		manipulationFrame.getChildren().addAll(camera, hand);
		manipulationFrame.getTransforms().add(camerUserPerspective);
		cameraFrame.getChildren().add(manipulationFrame);
		setZoomDepth(DEFAULT_ZOOM_DEPTH);
	}
	
	public VirtualCameraMobileBase addListener(ICameraChangeListener l) {
		if(!listeners.contains(l))
			listeners.add(l);
		return this;
	}
	public VirtualCameraMobileBase removeListener(ICameraChangeListener l) {
		if(listeners.contains(l))
			listeners.remove(l);
		return this;
	}
	public void fireUpdate() {
		for(ICameraChangeListener c:listeners) {
			try {
				c.onChange(this);
			}catch(Throwable t) {
				t.printStackTrace();
			}
		}
	}

	public void setGlobalToFiducialTransform(TransformNR defautcameraView) {
		myGlobal = defautcameraView;
		updatePositions();
	}

	public void updatePositions() {
		
		if(System.currentTimeMillis()-timeSinceLastUpdate>16) {
			timeSinceLastUpdate=System.currentTimeMillis();
			error=false;
			TransformFactory.nrToAffine(myGlobal, camerUserPerspective);
		}else {
			// too soon
			error=true;
		}
	}

	public TransformNR getFiducialToGlobalTransform() {
		return myGlobal;
	}
	public void DrivePositionAbsolute(double x, double y, double z) {
		TransformNR global = getFiducialToGlobalTransform().copy()
				.translateX(x)
				.translateY(y)
				.translateZ(z);
		setGlobalToFiducialTransform(global);
		fireUpdate();
	}
	public void DriveArc(TransformNR newPose) {
		
		// TODO Auto-generated method stub
		pureTrans.setX(newPose.getX());
		pureTrans.setY(newPose.getY());
		pureTrans.setZ(newPose.getZ());

		TransformNR global = getFiducialToGlobalTransform().times(pureTrans);
		global.setRotation(new RotationNR(
				tlOffset + (Math.toDegrees(
						newPose.getRotation().getRotationTiltRadians() + global.getRotation().getRotationTiltRadians()) % 360),
				azOffset + (Math.toDegrees(
						newPose.getRotation().getRotationAzimuthRadians() + global.getRotation().getRotationAzimuthRadians()) % 360),
				elOffset + Math.toDegrees(
						newPose.getRotation().getRotationElevationRadians() + global.getRotation().getRotationElevationRadians())));
//		 global.getRotation().setStorage(nr);
		//System.err.println("Camera tilt="+global);
		// New target calculated appliaed to global offset
		setGlobalToFiducialTransform(global);
		synchronizePositionWIthOtherFlyingCamera(newPose);
		fireUpdate();
	}
	

	public double getPanAngle() {
		return Math.toDegrees(getFiducialToGlobalTransform().getRotation().getRotationAzimuthRadians());
	}
	public double getTiltAngle() {
		return Math.toDegrees(getFiducialToGlobalTransform().getRotation().getRotationTiltRadians());
	}
	
	public double getGlobalX() {
		return getFiducialToGlobalTransform().getX();
	}
	public double getGlobalY() {
		return getFiducialToGlobalTransform().getY();
	}
	public double getGlobalZ() {
		return getFiducialToGlobalTransform().getZ();
	}
	public TransformNR getCamerFrame() {
		TransformNR offset = TransformFactory.affineToNr(getOffset());
		TransformNR fiducialToGlobalTransform = getFiducialToGlobalTransform();
		return offset.times(fiducialToGlobalTransform);
	}
	
	public PerspectiveCamera getCamera() {
		return camera;
	}

	public Group getCameraGroup() {
		return getCameraFrame();
	}

	private void setCamera(PerspectiveCamera camera) {
		this.camera = camera;
	}

	public Group getCameraFrame() {
		return cameraFrame;
	}

	public double getZoomDepth() {
		return zoomDepth;
	}

	public void setZoomDepth(double zoomDepth) {
		if(zoomlock)
			throw new RuntimeException("Zoom can not be set when locked");
		if (zoomDepth > 2)
			zoomDepth = 2;
		if (zoomDepth < -9000)
			zoomDepth = -9000;
		this.zoomDepth = zoomDepth;
		if(zoomDepth<-5000)
			camera.setFarClip(-zoomDepth*2);
		else
			camera.setFarClip(10000);
		zoomAffine.setTz(getZoomDepth());
		fireUpdate();
	}

	public static int getDefaultZoomDepth() {
		return DEFAULT_ZOOM_DEPTH;
	}

	public static Affine getOffset() {
		return offset;
	}

	public void bind(VirtualCameraMobileBase f) {
		if(flyingCamera!=null) {
			return;
		}
		this.flyingCamera = f;
	}
	private void synchronizePositionWIthOtherFlyingCamera(TransformNR n) {
		if(n.getRotation()==newPose.getRotation())
			return;
		this.newPose = n;
		if(flyingCamera!=null) {
			TransformNR newGlob = flyingCamera.getFiducialToGlobalTransform().copy()
									.setRotation(getFiducialToGlobalTransform().getRotation());
			flyingCamera.setGlobalToFiducialTransform(newGlob);
		}
	}

	public void lockZoom() {
		zoomlock = true;
	}

	public boolean isZoomLocked() {
		// TODO Auto-generated method stub
		return zoomlock;
	}


}