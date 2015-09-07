package com.neuronrobotics.bowlerstudio.scripting;

import java.util.ArrayList;
/**
 * Adding additional language support to bowler studio
 * THis interface is for adding new scripting languages
 * Add the new langauge in the Static declaration of ScriptingEngine 
 * or dynamically via:
 * 
 * ScriptingEngine.addScriptingLanguage(new IScriptingLanguage());
 * 
 * @author hephaestus
 *
 */
public interface IScriptingLanguage {
	/**
	 * This interface is for adding additional language support. 
	 * @param code the text content of the code to be executed
	 * @param args the incoming arguments as a list of objects
	 * @return the objects returned form the code that ran
	 */
	public abstract Object inlineScriptRun(String code, ArrayList<Object> args);
	
	/**
	 * Returns the shell type of this language
	 * @return
	 */
	public abstract ShellType getShellType();
	/**
	 * This function should return true is the filename provided is of a supported file extension. 
	 * This function may never be called if this language is only used internally. 
	 * @param filename the filename of the file to be executed
	 * @return true if the file extension is supported, false otherwise.
	 */
	public abstract boolean isSupportedFileExtenetion(String filename);
}
