package com.neuronrobotics.bowlerstudio.physics;

public interface IPhysicsUpdate {
	/**
	 * An event listener for the event of the physics engine updating
	 * This event is stored in the CSG manager and called by the update function.
	 * @param timeStep
	 */
	void update(float timeStep);
}
