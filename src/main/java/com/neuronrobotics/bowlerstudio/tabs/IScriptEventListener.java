package com.neuronrobotics.bowlerstudio.tabs;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

public interface IScriptEventListener {
	
	void onGroovyScriptFinished(GroovyShell shell, Script script, Object result);
	
	void onGroovyScriptChanged(String previous, String current);
	
	void onGroovyScriptError(GroovyShell shell, Script script, Exception except);

}
