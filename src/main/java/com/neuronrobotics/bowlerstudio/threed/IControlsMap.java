package com.neuronrobotics.bowlerstudio.threed;

public interface IControlsMap {
	public boolean timeToCancel(javafx.scene.input.MouseEvent event);
	public boolean isSlowMove(javafx.scene.input.MouseEvent event);
	public boolean isMove(javafx.scene.input.MouseEvent ev);
	public boolean isRotate(javafx.scene.input.MouseEvent me);
	public boolean isZoom(javafx.scene.input.ScrollEvent t);
		
}
