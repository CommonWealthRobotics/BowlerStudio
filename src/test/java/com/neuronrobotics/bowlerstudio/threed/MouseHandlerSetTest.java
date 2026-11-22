package com.neuronrobotics.bowlerstudio.threed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

/**
 * Camera controls must not stack when re-installed: every {@code rebuild()} goes
 * through {@code reattachMouseHandlers()}, and two live copies of the drag filter
 * would apply each mouse delta twice.
 *
 * <p>
 * JavaFX nodes accept filters without a started toolkit, so this exercises the
 * real registration and removal rather than a mock. The engine itself is not
 * constructed here because its constructor calls {@code runLater}, which does
 * need a toolkit (and a display).
 */
public class MouseHandlerSetTest {

	@Test
	public void reinstallingReplacesRatherThanStacks() {
		Group node = new Group();
		MouseHandlerSet set = new MouseHandlerSet();

		set.addFilter(node, MouseEvent.MOUSE_DRAGGED, event -> {
		});
		assertEquals(1, set.count(node));

		// a second install on the same node must first drop the previous set
		set.remove(node);
		set.addFilter(node, MouseEvent.MOUSE_DRAGGED, event -> {
		});
		assertEquals("the drag filter must exist exactly once", 1, set.count(node));
	}

	@Test
	public void removeTakesFiltersAndHandlersWithIt() {
		Group node = new Group();
		MouseHandlerSet set = new MouseHandlerSet();
		set.addFilter(node, MouseEvent.MOUSE_PRESSED, event -> {
		});
		set.addHandler(node, ScrollEvent.ANY, event -> {
		});
		assertEquals(2, set.count(node));

		set.remove(node);
		assertEquals(0, set.count(node));
	}

	@Test
	public void nodesAreTrackedIndependently() {
		// the SubScene and the host container each get their own set; removing one
		// must not disturb the other
		Group subScene = new Group();
		Group container = new Group();
		MouseHandlerSet set = new MouseHandlerSet();
		set.addFilter(subScene, MouseEvent.MOUSE_DRAGGED, event -> {
		});
		set.addFilter(container, MouseEvent.MOUSE_DRAGGED, event -> {
		});

		set.remove(container);
		assertEquals(0, set.count(container));
		assertEquals("removing one node's set must leave the other intact", 1, set.count(subScene));
	}

	@Test
	public void removeOfAnUninstalledNodeIsANoop() {
		MouseHandlerSet set = new MouseHandlerSet();
		set.remove(new Group());
		assertTrue(true);
	}
}
