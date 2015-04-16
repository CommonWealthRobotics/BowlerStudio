package com.neuronrobotics.bowlerstudio.scripting;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

public interface IScriptEventListener {
	
	void onGroovyScriptFinished( Object result,Object pervious);
	
	void onGroovyScriptChanged(String previous, String current);
	
	void onGroovyScriptError( Exception except);

}
