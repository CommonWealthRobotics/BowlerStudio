package com.neuronrobotics.bowlerstudio.scripting;

import java.io.File;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

public interface IScriptEventListener {
	
	void onScriptFinished( Object result,Object pervious, File source);
	
	void onScriptChanged(String previous, String current, File source);
	
	void onScriptError( Exception except, File source);

}
