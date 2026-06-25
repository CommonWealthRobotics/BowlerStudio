package com.neuronrobotics.bowlerstudio.threed;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.physics.TransformFactory;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;

import Jama.Matrix;
import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Affine;

public class VirtualCameraMobileBase {
	public static final TransformNR CameraGlobalOffset = new TransformNR(0, 0, 0, new RotationNR(180, 0, 0));
	private TransformNR myGlobal = new TransformNR();
	private static final int DEFAULT_ZOOM_DEPTH = -1500;
	private PerspectiveCamera camera;
	private Group hand;
	private final Group cameraFrame = new Group();

	private double zoomDepth = getDefaultZoomDepth();
	private Affine zoomAffine = new Affine();
	private static final Affine offset = new Affine();
	private Group manipulationFrame;
	long timeSinceLastUpdate = System.currentTimeMillis() - 17;
	boolean error = false;
	private boolean move = true;

	private Affine camerUserPerspective = new Affine();
	private ArrayList<VirtualCameraMobileBase> flyingCamera = new ArrayList<>();
	private boolean zoomlock;
	private ArrayList<ICameraChangeListener> listeners = new ArrayList<>();
	private String name;
	private double zoomScale = 1;

	// ---------------------------------------------------------------
	// Orthographic state
	// ---------------------------------------------------------------
	public enum ProjectionMode {
		PERSPECTIVE, ORTHOGRAPHIC
	}

	private ProjectionMode projectionMode = ProjectionMode.PERSPECTIVE;

	private double savedFov = 45.0;
	private double savedNearClip = 0.1;
	private double savedFarClip = 60000.0;

	/** World units from viewport centre to top edge — controls ortho zoom. */
	private double orthoScale = 750.0;

	/** Keep current so the matrix is correct after a resize. */
	private double viewportWidth = 800.0;
	private double viewportHeight = 600.0;

	// Reflection targets — resolved once on first ortho activation
	private Object ngCamera = null;
	private Object projMatrix = null; // NGCamera.projViewTx (GeneralTransform3D)
	private Field matField = null; // GeneralTransform3D.mat (double[])
	private boolean reflectionReady = false;

	/**
	 * Post-pulse listener — fires AFTER JavaFX syncs the scene graph to Prism but
	 * BEFORE the frame is rendered. This wins the race against JavaFX's own matrix
	 * recomputation and eliminates the flicker.
	 *
	 * Requires: --add-exports javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED
	 * --add-opens javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED
	 */
	private final com.sun.javafx.tk.TKPulseListener postPulseListener = this::injectOrthoMatrix;

	private boolean pulseListenerRegistered = false;

	// ---------------------------------------------------------------
	// Constructor — unchanged
	// ---------------------------------------------------------------
	public VirtualCameraMobileBase(PerspectiveCamera camera, Group hand, ICameraChangeListener lis, String name) {
		this.hand = hand;
		this.name = name;
		this.setCamera(camera);
		addListener(lis);

		manipulationFrame = new Group();
		camera.getTransforms().add(zoomAffine);
		BowlerStudio.runLater(() -> TransformFactory.nrToAffine(CameraGlobalOffset, offset));
		cameraFrame.getTransforms().add(getOffset());
		manipulationFrame.getChildren().addAll(camera, hand);
		manipulationFrame.getTransforms().add(camerUserPerspective);
		cameraFrame.getChildren().add(manipulationFrame);
		setZoomDepth(DEFAULT_ZOOM_DEPTH);
	}

	// ---------------------------------------------------------------
	// Public projection API
	// ---------------------------------------------------------------

	/** Toggle between PERSPECTIVE and ORTHOGRAPHIC. */
	public void toggleProjectionMode() {
		setProjectionMode(projectionMode == ProjectionMode.PERSPECTIVE ? ProjectionMode.ORTHOGRAPHIC
				: ProjectionMode.PERSPECTIVE);
	}

	/**
	 * Explicitly set the projection mode. Thread-safe — dispatches to FX thread.
	 */
	public void setProjectionMode(ProjectionMode mode) {
		this.projectionMode = mode;
		BowlerStudio.runLater(() -> {
			if (mode == ProjectionMode.ORTHOGRAPHIC)
				activateOrtho();
			else
				activatePerspective();
		});
	}

	public ProjectionMode getProjectionMode() {
		return projectionMode;
	}

	public boolean isOrthographic() {
		return projectionMode == ProjectionMode.ORTHOGRAPHIC;
	}

	/**
	 * Call from your SubScene width/height listeners so the ortho matrix stays
	 * correct after a resize.
	 */
	public void setViewportSize(double width, double height) {
		this.viewportWidth = width;
		this.viewportHeight = height;
		if (projectionMode == ProjectionMode.ORTHOGRAPHIC)
			injectOrthoMatrix();
	}

	/**
	 * Ortho zoom — call from scroll-wheel handler instead of setZoomDepth() when in
	 * ortho mode. factor < 1 zooms in, factor > 1 zooms out.
	 */
	public void orthoZoom(double factor) {
		orthoScale = Math.max(1.0, orthoScale * factor);
		if (projectionMode == ProjectionMode.ORTHOGRAPHIC)
			injectOrthoMatrix();
	}

	public double getOrthoScale() {
		return orthoScale;
	}

	public void setOrthoScale(double s) {
		orthoScale = s;
	}

	// ---------------------------------------------------------------
	// Internal: activate / deactivate
	// ---------------------------------------------------------------

	private void activateOrtho() {
		// Seed orthoScale from perspective state so the view doesn't jump
		double distance = -zoomDepth;
		double fovRad = Math.toRadians(camera.getFieldOfView());
		orthoScale = Math.tan(fovRad / 2.0) * distance;

		savedFov = camera.getFieldOfView();
		savedNearClip = camera.getNearClip();
		savedFarClip = camera.getFarClip();

		camera.setNearClip(0.1);
		camera.setFarClip(savedFarClip);

		if (!reflectionReady)
			reflectionReady = resolveReflection();

		if (reflectionReady) {
			startPostPulseListener();
		} else {
			com.neuronrobotics.sdk.common.Log.error("OrthoCamera: reflection failed — check --add-opens JVM flags.");
		}
	}

	private void activatePerspective() {
		stopPostPulseListener();
		camera.setFieldOfView(savedFov);
		camera.setNearClip(savedNearClip);
		camera.setFarClip(savedFarClip);
		// Nudge FOV so JavaFX recomputes and overwrites our injected matrix
		camera.setFieldOfView(savedFov + 0.00001);
		camera.setFieldOfView(savedFov);
	}

	// ---------------------------------------------------------------
	// Internal: post-pulse listener (replaces AnimationTimer)
	// Fires AFTER scene graph sync, BEFORE render — no flicker.
	// ---------------------------------------------------------------

	private void startPostPulseListener() {
		if (pulseListenerRegistered)
			return;
		try {
			com.sun.javafx.tk.Toolkit.getToolkit().addSceneTkPulseListener(postPulseListener);
			pulseListenerRegistered = true;
			log("post-pulse listener registered");
		} catch (Exception e) {
			log("could not register pulse listener: " + e.getMessage() + " — falling back to AnimationTimer");
			startFallbackTimer();
		}
	}

	private void stopPostPulseListener() {
		if (pulseListenerRegistered) {
			try {
				com.sun.javafx.tk.Toolkit.getToolkit().removeSceneTkPulseListener(postPulseListener);
			} catch (Exception ignored) {
			}
			pulseListenerRegistered = false;
		}
		stopFallbackTimer();
	}

	// Fallback in case Toolkit pulse API is unavailable
	private AnimationTimer fallbackTimer = null;

	private void startFallbackTimer() {
		stopFallbackTimer();
		fallbackTimer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				injectOrthoMatrix();
			}
		};
		fallbackTimer.start();
	}

	private void stopFallbackTimer() {
		if (fallbackTimer != null) {
			fallbackTimer.stop();
			fallbackTimer = null;
		}
	}

	// ---------------------------------------------------------------
	// Internal: matrix injection
	// ---------------------------------------------------------------
	private void injectOrthoMatrix() {
		if (!reflectionReady || projMatrix == null || matField == null)
			return;

		// The BEFORE matrix proves JavaFX already computes the correct P×V for
		// perspective. Its rows encode the view direction scaled by the perspective
		// FOV. For ortho we want the same view directions but with ortho scaling
		// instead of perspective scaling.
		//
		// Strategy: read the current Prism matrix (which has correct view baked in),
		// extract the view row vectors (their directions), and re-scale them with
		// ortho factors instead of perspective factors.

		double aspect = viewportWidth / viewportHeight;
		double hw = orthoScale * aspect;
		double hh = orthoScale;
		double near = camera.getNearClip();
		double far = camera.getFarClip();
		double fn = far - near;

		try {
			Object arr = matField.get(projMatrix);
			double[] m;
			boolean isFloat = arr instanceof float[];

			if (arr instanceof double[]) {
				m = (double[]) arr;
			} else if (isFloat) {
				float[] f = (float[]) arr;
				m = new double[16];
				for (int i = 0; i < 16; i++)
					m[i] = f[i];
			} else
				return;

			// The current Prism matrix rows 0,1,2 are:
			// row0 = (P_perspective * view).row0 — encodes X axis scaled by perspective
			// row1 = (P_perspective * view).row1 — encodes Y axis scaled by perspective
			// row2 = (P_perspective * view).row2 — encodes Z axis (depth)
			//
			// For perspective: row scale ≈ cot(fov/2) / distance
			// For ortho: row scale = 1/hw or 1/hh (fixed, not distance-dependent)
			//
			// Recover the pure view row directions by normalizing rows 0 and 1,
			// then re-scale with ortho factors.

			// Extract rows 0 and 1 (xyz components only — col 3 is translation)
			double r0x = m[0], r0y = m[1], r0z = m[2];
			double r1x = m[4], r1y = m[5], r1z = m[6];
			double r2x = m[8], r2y = m[9], r2z = m[10];

			double len0 = Math.sqrt(r0x * r0x + r0y * r0y + r0z * r0z);
			double len1 = Math.sqrt(r1x * r1x + r1y * r1y + r1z * r1z);
			double len2 = Math.sqrt(r2x * r2x + r2y * r2y + r2z * r2z);

			log("row lengths: " + len0 + " " + len1 + " " + len2);

			if (len0 < 1e-12 || len1 < 1e-12) {
				log("degenerate view matrix — skipping inject");
				return;
			}

			// Normalized view axis directions
			double ox = r0x / len0, oy = r0y / len0, oz = r0z / len0; // right axis
			double ux = r1x / len1, uy = r1y / len1, uz = r1z / len1; // up axis
			double fx = r2x / len2, fy = r2y / len2, fz = r2z / len2; // forward axis

			// Ortho scale factors
			double sx = 1.0 / hw;
			double sy = 1.0 / hh;
			double sz = -2.0 / fn;

			// Translation components: col3 of each row
			// For ortho these should be 0 (symmetric frustum, no offset)
			// but preserve the depth translation for row2
			double tz = -(far + near) / fn;

			// Rebuild the matrix with ortho scaling applied to normalized axes
			m[0] = ox * sx;
			m[1] = oy * sx;
			m[2] = oz * sx;
			m[3] = 0.0;
			m[4] = ux * sy;
			m[5] = uy * sy;
			m[6] = uz * sy;
			m[7] = 0.0;
			m[8] = fx * sz;
			m[9] = fy * sz;
			m[10] = fz * sz;
			m[11] = tz;
			m[12] = 0.0;
			m[13] = 0.0;
			m[14] = 0.0;
			m[15] = 1.0;

			// Translation for X and Y: dot(right, camPos) and dot(up, camPos)
			// Read from the original perspective matrix's col3 rows 0,1 — these
			// are already the correct view-space translations, just re-scaled
			double origTx = m[3]; // was already overwritten above — need to save first
			double origTy = m[7];

			// Redo: save translations before overwriting
			// Reset and rebuild properly
			double savedT0 = 0.0, savedT1 = 0.0;
			// The original col3 values from the perspective matrix encode -dot(axis, eye)
			// scaled by the perspective factor. Recover unscaled: divide by len
			if (len0 > 1e-12)
				savedT0 = m[3] / len0; // but m[3] was just set to 0 above
			if (len1 > 1e-12)
				savedT1 = m[7] / len1;

			// We need the raw translations — re-read from original before overwrite
			// This approach is getting complicated. Read the original T from V instead:
			javafx.scene.transform.Transform w2c = camera.getLocalToSceneTransform();
			TransformNR vnr = TransformFactory.affineToNr(w2c).inverse();
			Matrix Vmat = vnr.getMatrixTransform();
			double[][] Vd = Vmat.getArray();
			// V row3 col = translation in view space
			double vTx = Vd[0][3];
			double vTy = Vd[1][3];
			double vTz = Vd[2][3];

			m[0] = ox * sx;
			m[1] = oy * sx;
			m[2] = oz * sx;
			m[3] = vTx * sx;
			m[4] = ux * sy;
			m[5] = uy * sy;
			m[6] = uz * sy;
			m[7] = vTy * sy;
			m[8] = fx * sz;
			m[9] = fy * sz;
			m[10] = fz * sz;
			m[11] = vTz * sz + tz;
			m[12] = 0.0;
			m[13] = 0.0;
			m[14] = 0.0;
			m[15] = 1.0;

			log("injecting ortho M:");
			for (int r = 0; r < 4; r++)
				log("  row" + r + ": " + m[r * 4] + " " + m[r * 4 + 1] + " " + m[r * 4 + 2] + " " + m[r * 4 + 3]);

			if (isFloat) {
				float[] f = (float[]) arr;
				for (int i = 0; i < 16; i++)
					f[i] = (float) m[i];
			}
			// if double[] we edited in-place above

			forcePrismDirty();
		} catch (Exception e) {
			com.neuronrobotics.sdk.common.Log.error("OrthoCamera inject: " + e.getMessage());
		}
	}
	// ---------------------------------------------------------------------------

	// ---------------------------------------------------------------
	// Internal: reflection — resilient peer resolution
	// ---------------------------------------------------------------

	/**
	 * Required JVM flags: --add-opens
	 * javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED --add-opens
	 * javafx.graphics/com.sun.javafx.sg.prism=ALL-UNNAMED --add-opens
	 * javafx.graphics/com.sun.javafx.geom.transform=ALL-UNNAMED --add-opens
	 * javafx.graphics/com.sun.javafx.geom=ALL-UNNAMED --add-opens
	 * javafx.graphics/com.sun.javafx.camera=ALL-UNNAMED --add-opens
	 * javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED --add-exports
	 * javafx.graphics/com.sun.javafx.sg.prism=ALL-UNNAMED --add-exports
	 * javafx.graphics/com.sun.javafx.geom.transform=ALL-UNNAMED --add-exports
	 * javafx.graphics/com.sun.javafx.geom=ALL-UNNAMED --add-exports
	 * javafx.graphics/com.sun.javafx.camera=ALL-UNNAMED --add-exports
	 * javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED
	 */
	private boolean resolveReflection() {
		try {
			ngCamera = resolvePeer();
			if (ngCamera == null) {
				log("Could not resolve NGCamera peer — see diagnostic output above");
				return false;
			}

			log("NGCamera type: " + ngCamera.getClass().getName());

			// JavaFX 25: projection matrix is NGCamera.projViewTx (GeneralTransform3D)
			Field projField = findField(ngCamera.getClass(), "projViewTx");
			if (projField == null) {
				log("projViewTx field not found in " + ngCamera.getClass().getName());
				dumpFieldNames(ngCamera.getClass(), "NGPerspectiveCamera full field hierarchy");
				return false;
			}
			projField.setAccessible(true);
			projMatrix = projField.get(ngCamera);
			if (projMatrix == null) {
				log("projViewTx value is null");
				return false;
			}

			log("projViewTx type: " + projMatrix.getClass().getName());

			matField = findField(projMatrix.getClass(), "mat");
			if (matField == null) {
				log("mat[] field not found in " + projMatrix.getClass().getName());
				dumpFieldNames(projMatrix.getClass(), "GeneralTransform3D fields");
				return false;
			}
			matField.setAccessible(true);

			log("reflection resolved OK — mat[] type: " + matField.get(projMatrix).getClass().getSimpleName());
			return true;
		} catch (Exception e) {
			log("reflection setup exception: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Tries every known way to get the NGCamera peer across JavaFX versions.
	 */
	private Object resolvePeer() {
		Object peer;

		// Strategy 1 — CameraHelper.getPeer(Camera) [FX 17-21]
		peer = tryHelperGetPeer("com.sun.javafx.scene.CameraHelper", camera);
		if (peer != null) {
			log("peer via CameraHelper.getPeer()");
			return peer;
		}

		// Strategy 2 — NodeHelper.getPeer(Node) [FX 22+]
		peer = tryHelperGetPeer("com.sun.javafx.scene.NodeHelper", camera);
		if (peer != null) {
			log("peer via NodeHelper.getPeer()");
			return peer;
		}

		// Strategy 3 — camera.impl_getPeer() [FX 11-16]
		peer = tryInstanceMethod(camera, "impl_getPeer");
		if (peer != null) {
			log("peer via impl_getPeer()");
			return peer;
		}

		// Strategy 4 — wildcard scan of CameraHelper / NodeHelper for any method
		// that accepts a Camera and returns an NG* type
		peer = tryAnyPeerMethod("com.sun.javafx.scene.CameraHelper", camera);
		if (peer != null) {
			log("peer via CameraHelper wildcard method");
			return peer;
		}

		peer = tryAnyPeerMethod("com.sun.javafx.scene.NodeHelper", camera);
		if (peer != null) {
			log("peer via NodeHelper wildcard method");
			return peer;
		}

		// Strategy 5 — direct field on Camera
		peer = tryFieldOnObject(camera, "peer");
		if (peer != null) {
			log("peer via 'peer' field on Camera");
			return peer;
		}

		peer = tryFieldOnObject(camera, "nativePeer");
		if (peer != null) {
			log("peer via 'nativePeer' field on Camera");
			return peer;
		}

		// Nothing worked — dump diagnostics
		dumpClassInfo("com.sun.javafx.scene.CameraHelper");
		dumpClassInfo("com.sun.javafx.scene.NodeHelper");
		dumpFieldNames(camera.getClass(), "Camera fields");
		return null;
	}

	private static Object tryHelperGetPeer(String helperClassName, javafx.scene.Camera cam) {
		try {
			Class<?> helper = Class.forName(helperClassName);
			Method m = helper.getDeclaredMethod("getPeer", javafx.scene.Camera.class);
			m.setAccessible(true);
			return m.invoke(null, cam);
		} catch (Exception ignored) {
			return null;
		}
	}

	private static Object tryInstanceMethod(Object target, String methodName) {
		Class<?> c = target.getClass();
		while (c != null) {
			try {
				Method m = c.getDeclaredMethod(methodName);
				m.setAccessible(true);
				return m.invoke(target);
			} catch (Exception ignored) {
			}
			c = c.getSuperclass();
		}
		return null;
	}

	private static Object tryAnyPeerMethod(String helperClassName, javafx.scene.Camera cam) {
		try {
			Class<?> helper = Class.forName(helperClassName);
			for (Method m : helper.getDeclaredMethods()) {
				Class<?>[] params = m.getParameterTypes();
				if (params.length != 1)
					continue;
				if (!params[0].isAssignableFrom(cam.getClass()))
					continue;
				if (m.getReturnType().isPrimitive() || m.getReturnType() == void.class)
					continue;
				String mn = m.getName().toLowerCase();
				if (mn.contains("listener") || mn.contains("scene") || mn.contains("parent"))
					continue;
				try {
					m.setAccessible(true);
					Object result = m.invoke(null, cam);
					if (result != null && result.getClass().getName().contains("NG"))
						return result;
				} catch (Exception ignored) {
				}
			}
		} catch (Exception ignored) {
		}
		return null;
	}

	private static Object tryFieldOnObject(Object target, String fieldName) {
		Field f = findField(target.getClass(), fieldName);
		if (f == null)
			return null;
		try {
			f.setAccessible(true);
			return f.get(target);
		} catch (Exception ignored) {
			return null;
		}
	}

	// ---------------------------------------------------------------
	// Diagnostics
	// ---------------------------------------------------------------

	private static void dumpClassInfo(String className) {
		try {
			Class<?> c = Class.forName(className);
			StringBuilder sb = new StringBuilder("OrthoCamera DIAGNOSTIC — methods on " + className + ":\n");
			for (Method m : c.getDeclaredMethods()) {
				sb.append("  ").append(m.getName()).append("(");
				Class<?>[] p = m.getParameterTypes();
				for (int i = 0; i < p.length; i++) {
					sb.append(p[i].getSimpleName());
					if (i < p.length - 1)
						sb.append(", ");
				}
				sb.append(") -> ").append(m.getReturnType().getSimpleName()).append("\n");
			}
			com.neuronrobotics.sdk.common.Log.warning(sb.toString());
		} catch (Exception e) {
			com.neuronrobotics.sdk.common.Log
					.error("OrthoCamera: could not inspect " + className + ": " + e.getMessage());
		}
	}

	private static void dumpFieldNames(Class<?> clazz, String label) {
		StringBuilder sb = new StringBuilder("OrthoCamera DIAGNOSTIC — " + label + ":\n");
		Class<?> c = clazz;
		while (c != null) {
			for (Field f : c.getDeclaredFields()) {
				sb.append("  ").append(c.getSimpleName()).append(".").append(f.getName()).append(" : ")
						.append(f.getType().getSimpleName()).append("\n");
			}
			c = c.getSuperclass();
		}
		com.neuronrobotics.sdk.common.Log.warning(sb.toString());
	}

	// ---------------------------------------------------------------
	// Shared helpers
	// ---------------------------------------------------------------

	private void forcePrismDirty() {
		try {
			Class<?> ngNodeClass = Class.forName("com.sun.javafx.sg.prism.NGNode");
			for (Method m : ngNodeClass.getDeclaredMethods()) {
				if (m.getName().contains("markDirty") || m.getName().equals("visualsChanged")) {
					m.setAccessible(true);
					m.invoke(ngCamera);
					return;
				}
			}
		} catch (Exception ignored) {
		}
	}

	private static Field findField(Class<?> c, String name) {
		while (c != null) {
			try {
				return c.getDeclaredField(name);
			} catch (NoSuchFieldException e) {
				c = c.getSuperclass();
			}
		}
		return null;
	}

	private static void log(String msg) {
		com.neuronrobotics.sdk.common.Log.warning("OrthoCamera: " + msg);
	}

	// ---------------------------------------------------------------
	// Everything below is UNCHANGED from original
	// ---------------------------------------------------------------

	public VirtualCameraMobileBase addListener(ICameraChangeListener l) {
		if (!listeners.contains(l))
			listeners.add(l);
		return this;
	}

	public VirtualCameraMobileBase removeListener(ICameraChangeListener l) {
		if (listeners.contains(l))
			listeners.remove(l);
		return this;
	}

	public void fireUpdate() {
		synchronizePositionWithOtherFlyingCamera(myGlobal);
		for (ICameraChangeListener c : listeners) {
			try {
				c.onChange(this);
			} catch (Throwable t) {
				com.neuronrobotics.sdk.common.Log.error(t);
			}
		}
	}

	public void setGlobalToFiducialTransform(TransformNR defautcameraView) {
		myGlobal = defautcameraView;
		updatePositions();
		fireUpdate();
	}

	public void updatePositions() {
		if (System.currentTimeMillis() - timeSinceLastUpdate > 16) {
			timeSinceLastUpdate = System.currentTimeMillis();
			error = false;
			TransformFactory.nrToAffine(myGlobal, camerUserPerspective);
		} else {
			error = true;
		}
	}

	public TransformNR getFiducialToGlobalTransform() {
		return myGlobal;
	}

	public void DrivePositionAbsolute(double x, double y, double z) {
		TransformNR global = getFiducialToGlobalTransform().copy().translateX(x).translateY(y).translateZ(z);
		setGlobalToFiducialTransform(global);
	}

	public void DriveArc(TransformNR newPose) {
		TransformNR pureTrans = new TransformNR();
		if (move) {
			pureTrans.setX(newPose.getX());
			pureTrans.setY(newPose.getY());
			pureTrans.setZ(newPose.getZ());
		}
		TransformNR global = getFiducialToGlobalTransform().times(pureTrans);
		double rotationTiltRadians = newPose.getRotation().getRotationTiltRadians();
		double rotationAzimuthRadians = newPose.getRotation().getRotationAzimuthRadians();
		double rotationElevationRadians = newPose.getRotation().getRotationElevationRadians();
		global.setRotation(new RotationNR(
				(Math.toDegrees(rotationTiltRadians + global.getRotation().getRotationTiltRadians()) % 360),
				(Math.toDegrees(rotationAzimuthRadians + global.getRotation().getRotationAzimuthRadians()) % 360),
				Math.toDegrees(rotationElevationRadians + global.getRotation().getRotationElevationRadians())));
		setGlobalToFiducialTransform(global);
	}

	public void SetPosition(TransformNR newPose) {
		if ((newPose == null) || !move)
			return;
		setGlobalToFiducialTransform(newPose.copy().setRotation(getFiducialToGlobalTransform().getRotation()));
	}

	public void SetOrientation(TransformNR newPose) {
		if (newPose == null)
			return;
		TransformNR global = getFiducialToGlobalTransform().copy();
		double rotationElevationDegrees = -newPose.getRotation().getRotationElevationDegrees() - 90;
		double azimuthDegrees = 90 - newPose.getRotation().getRotationAzimuthDegrees();
		double globalElevationDegrees = global.getRotation().getRotationElevationDegrees();
		global.setRotation(new RotationNR(rotationElevationDegrees, azimuthDegrees, globalElevationDegrees));
		setGlobalToFiducialTransform(global);
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
		TransformNR off = TransformFactory.affineToNr(getOffset());
		TransformNR fiducialToGlobalTransform = getFiducialToGlobalTransform();
		return off.times(fiducialToGlobalTransform);
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
		if (zoomlock)
			throw new RuntimeException("Zoom can not be set when locked");
		zoomDepth = Math.max(-9000 * getZoomScale(), Math.min(-2, zoomDepth));
		this.zoomDepth = zoomDepth;
		camera.setFarClip(Math.max(6000 * getZoomScale(), -zoomDepth * 2));
		zoomAffine.setTz(zoomDepth);
		fireUpdate();
	}

	public static int getDefaultZoomDepth() {
		return DEFAULT_ZOOM_DEPTH;
	}

	public static Affine getOffset() {
		return offset;
	}

	public void bind(VirtualCameraMobileBase f) {
		if (flyingCamera.contains(f))
			return;
		this.flyingCamera.add(f);
	}

	private void synchronizePositionWithOtherFlyingCamera(TransformNR n) {
		for (VirtualCameraMobileBase cam : flyingCamera) {
			RotationNR rotation = getFiducialToGlobalTransform().getRotation();
			if (!zoomlock && !cam.zoomlock && ((int) cam.getZoomDepth()) != ((int) getZoomDepth())) {
				cam.setZoomDepth(zoomDepth);
			}
			if (rotation == cam.myGlobal.getRotation())
				continue;
			if (!cam.move || !move) {
				TransformNR newGlob = cam.getFiducialToGlobalTransform().copy().setRotation(rotation);
				cam.setGlobalToFiducialTransform(newGlob);
			} else {
				cam.setGlobalToFiducialTransform(n.copy().setRotation(rotation));
			}
		}
	}

	public void lockZoom() {
		zoomlock = true;
	}

	public boolean isZoomLocked() {
		return zoomlock;
	}

	public void lockMove() {
		move = false;
	}

	public double getZoomScale() {
		return zoomScale;
	}

	public void setZoomScale(double zoomScale) {
		this.zoomScale = zoomScale;
	}
}
