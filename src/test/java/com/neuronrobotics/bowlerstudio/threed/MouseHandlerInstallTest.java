package com.neuronrobotics.bowlerstudio.threed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import javafx.scene.Group;
import javafx.scene.layout.AnchorPane;

/**
 * Camera controls must be installed on both the SubScene and the container it
 * was added to, and re-installing must not stack a second copy.
 *
 * <p>
 * The container matters because its handlers are what keep orbit and zoom
 * working when the pointer is over empty space and the press never reaches the
 * SubScene. Duplicates matter because each drag delta would then be applied
 * once per installed set.
 *
 * <p>
 * Counting uses the engine's own bookkeeping; Node's filter registration
 * methods are final, so they cannot be observed by subclassing.
 */
public class MouseHandlerInstallTest {

	@Test
	public void reinstallingDoesNotStackHandlers() {
		Group node = new Group();
		BowlerStudio3dEngine engine = new BowlerStudio3dEngine("test");

		engine.handleMouse(node);
		int afterFirst = engine.installedHandlerCount(node);
		assertTrue("first install must register something", afterFirst > 0);

		engine.handleMouse(node);
		assertEquals("a re-install must replace the previous set, not stack it", afterFirst,
				engine.installedHandlerCount(node));
	}

	@Test
	public void reattachInstallsOnTheHostContainerAsWellAsTheSubScene() {
		BowlerStudio3dEngine engine = new BowlerStudio3dEngine("test");
		AnchorPane host = new AnchorPane();

		engine.addTo(host);
		assertEquals("addTo alone installs nothing on the container", 0, engine.installedHandlerCount(host));

		engine.reattachMouseHandlers();
		assertTrue("the container must carry the camera controls too", engine.installedHandlerCount(host) > 0);

		int afterFirst = engine.installedHandlerCount(host);
		engine.reattachMouseHandlers();
		assertEquals("repeated reattach must not stack container handlers", afterFirst,
				engine.installedHandlerCount(host));
	}
}
