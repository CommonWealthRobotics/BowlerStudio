package com.neuronrobotics.bowlerstudio.scripting;


import java.util.ArrayList;

import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

/**
 * Class containing static utility methods for Java->Clojure interop
 * 
 * @author Mike https://github.com/mikera/clojure-utils/blob/master/src/main/java/mikera/cljutils/Clojure.java
 *
 */
public class ClojureHelper implements IScriptingLanguage{
	public static Var REQUIRE=var("clojure.core", "require");
	public static Var META=var("clojure.core", "meta");
	public static Var EVAL=var("clojure.core", "eval");
	public static Var READ_STRING=var("clojure.core", "load-string");
	
	/**
	 * Require a namespace by name, loading it if necessary.
	 * 
	 * Calls clojure.core/require
	 * 
	 * @param nsName
	 * @return
	 */
	public static Object require(String nsName) {
		return REQUIRE.invoke(Symbol.intern(nsName));
	}
	
	public static Object readString(String s) {
		return READ_STRING.invoke(s);
	}
	
	/**
	 * Looks up a var by name in the clojure.core namespace.
	 * 
	 * The var can subsequently be invoked if it is a function.
	 * 
	 * @param varName
	 * @return
	 */
	public static Var var(String varName) {
		return var("clojure.core",varName);
	}
	
	/**
	 * Looks up a var by name in the given namespace.
	 * 
	 * The var can subsequently be invoked if it is a function.
	 * @param nsName
	 * @param varName
	 * @return
	 */
	public static Var var(String nsName, String varName) {
		return RT.var(nsName,varName);
	}

	/**
	 * Evaluates a String, which should contain valid Clojure code.
	 * 
	 * @param string
	 * @return
	 */
	public static Object eval(String string) {
		return EVAL.invoke(readString(string));
	}

	@Override
	public Object inlineScriptRun(String code, ArrayList<Object> args) {
		Object ret = ClojureHelper.eval(code);
		System.out.println("Clojure returned of type="+ret.getClass()+" value="+ret);
		return ret;
	}

	@Override
	public ShellType getShellType() {
		return ShellType.CLOJURE;
	}

	@Override
	public boolean isSupportedFileExtenetion(String filename) {
		if (filename.toLowerCase().endsWith(".clj")
				|| filename.toLowerCase().endsWith(".cljs")
				|| filename.toLowerCase().endsWith(".cljc")) {
			return true;
		}		
		return false;
	}
	
}

