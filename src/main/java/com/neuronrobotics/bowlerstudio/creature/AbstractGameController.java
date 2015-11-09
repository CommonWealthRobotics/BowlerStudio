package com.neuronrobotics.bowlerstudio.creature;

import java.util.ArrayList;

public abstract class AbstractGameController {
	private ArrayList<IGameControllerUpdateListener> listeners = new ArrayList<IGameControllerUpdateListener>();
	
	public void addIGameControllerUpdateListener(IGameControllerUpdateListener l){
		if(!listeners.contains(l))
			listeners.add(l);
	}
	public void removeIGameControllerUpdateListener(IGameControllerUpdateListener l){
		if(listeners.contains(l))
			listeners.remove(l);
	}
	public void clearIGameControllerUpdateListener(){
		listeners.clear();
	}
	
	protected void fireGameControllerUpdate(){
		
	}
	
	public abstract double getControls0Plus();
	public abstract double getControls0Minus();
	
	public abstract double getControls1Plus();
	public abstract double getControls1Minus();
	
	public abstract double getControls2Plus();
	public abstract double getControls2Minus();
	
	public abstract double getControls3Plus();
	public abstract double getControls3Minus();
	
	public abstract double getNavUp();
	public abstract double getNavDown();
	
	public abstract double getNavLeft();
	public abstract double getNavRight();
	
	
	public abstract double getActionLeft();
	public abstract double getActionRight();
}
