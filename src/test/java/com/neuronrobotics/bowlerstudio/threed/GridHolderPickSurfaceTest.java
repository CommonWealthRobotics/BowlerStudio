package com.neuronrobotics.bowlerstudio.threed;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import com.neuronrobotics.bowlerstudio.threed.BowlerStudio3dEngine.GridHolder;

import javafx.application.Platform;
import javafx.scene.Node;

/**
 * The workplane is picked through one of two alternating background surfaces:
 * the plain fill while the camera is still and the textured quad while it moves.
 * {@link GridHolder#setMouseTransparent(boolean)} used to flag only whichever of
 * the two {@code getIntersectionNode()} returned at call time, leaving the other
 * pickable.
 *
 * <p>
 * That leaks: a workplane pick session ends while the camera is moving, so the
 * moving surface is the one made transparent. Once the camera settles the still
 * surface is visible again, is still pickable, and swallows every background
 * press before it can reach the selection plane. Clearing the selection and
 * dragging a selection box on the background both stop working.
 */
public class GridHolderPickSurfaceTest {

	private static boolean toolkitStarted;

	@BeforeClass
	public static void startToolkit() {
		CountDownLatch up = new CountDownLatch(1);
		Thread t = new Thread(() -> {
			try {
				Platform.startup(() -> {
				});
			} finally {
				up.countDown();
			}
		}, "fx-grid-test-init");
		t.setDaemon(true);
		t.start();
		try {
			toolkitStarted = up.await(30, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			toolkitStarted = false;
		}
	}

	private static void onFxThread(Runnable r) throws InterruptedException {
		CountDownLatch done = new CountDownLatch(1);
		Platform.runLater(() -> {
			try {
				r.run();
			} finally {
				done.countDown();
			}
		});
		done.await(30, TimeUnit.SECONDS);
	}

	/** Drains the queued {@code runLater} work the grid applies on every change. */
	private static void settle() throws InterruptedException {
		for (int i = 0; i < 8; i++) {
			onFxThread(() -> {
			});
			Thread.sleep(25);
		}
	}

	@Test(timeout = 120000)
	public void everyBackgroundSurfaceFollowsTheMouseTransparentFlag() throws Exception {
		Assume.assumeTrue(toolkitStarted);

		BowlerStudio3dEngine engine = new BowlerStudio3dEngine("grid-test");
		GridHolder grid = engine.createTexturedWorkplane(1000, 1000);
		settle();

		onFxThread(() -> grid.setMouseTransparent(false));
		settle();
		Node stillSurface = grid.getIntersectionNode();
		assertNotNull("the still background surface must exist", stillSurface);
		assertFalse("the background surface must be pickable during a pick session", stillSurface.isMouseTransparent());

		// A moving camera swaps in the other pick surface.
		onFxThread(() -> grid.setCameraMoving(true));
		settle();
		Node movingSurface = grid.getIntersectionNode();
		assertFalse("the moving background surface must be pickable during a pick session",
				movingSurface.isMouseTransparent());

		// The pick session ends while the camera is moving.
		onFxThread(() -> grid.setMouseTransparent(true));
		settle();
		assertTrue("the picked background surface must stop being pickable when the session ends",
				grid.getIntersectionNode().isMouseTransparent());

		// The camera settles, swapping the first surface back in.
		onFxThread(() -> grid.setCameraMoving(false));
		settle();
		assertTrue("whichever surface is shown must stay transparent to the mouse after the session ends",
				grid.getIntersectionNode().isMouseTransparent());
	}
}
