package com.neuronrobotics.bowlerstudio.threed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;

/**
 * Tracks the camera-control handlers installed on a node so they can be removed
 * as a set.
 *
 * <p>
 * Re-installing is a replace, not an append: every {@code rebuild()} calls
 * {@link BowlerStudio3dEngine#reattachMouseHandlers()}, and two live copies of
 * the drag filter would apply each mouse delta twice.
 *
 * <p>
 * Kept separate from the engine so the bookkeeping is testable without a
 * JavaFX toolkit; the engine's constructor needs a started toolkit, this does
 * not.
 */
final class MouseHandlerSet {

	private final Map<Node, List<Runnable>> installed = new HashMap<>();

	/** Installs a filter and records how to undo it. */
	<E extends Event> void addFilter(Node node, EventType<E> type, EventHandler<? super E> filter) {
		node.addEventFilter(type, filter);
		installed.computeIfAbsent(node, k -> new ArrayList<>()).add(() -> node.removeEventFilter(type, filter));
	}

	/** Installs a handler and records how to undo it. */
	<E extends Event> void addHandler(Node node, EventType<E> type, EventHandler<? super E> handler) {
		node.addEventHandler(type, handler);
		installed.computeIfAbsent(node, k -> new ArrayList<>()).add(() -> node.removeEventHandler(type, handler));
	}

	/** Number of handlers currently installed on {@code node}; 0 when none. */
	int count(Node node) {
		List<Runnable> removed = installed.get(node);
		return removed == null ? 0 : removed.size();
	}

	/** Removes the set installed on {@code node}, leaving any other set alone. */
	void remove(Node node) {
		List<Runnable> removed = installed.remove(node);
		if (removed == null)
			return;
		for (Runnable undo : removed)
			undo.run();
	}
}
