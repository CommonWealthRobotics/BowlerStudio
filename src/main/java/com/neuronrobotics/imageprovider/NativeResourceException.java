package com.neuronrobotics.imageprovider;

public class NativeResourceException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message;
	
	public NativeResourceException(String msg) {
		message = msg;
	}
	
	public String toString() {
		return message;
	}
}
