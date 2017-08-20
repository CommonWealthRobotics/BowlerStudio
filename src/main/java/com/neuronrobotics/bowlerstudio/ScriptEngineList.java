package com.neuronrobotics.bowlerstudio;

import java.util.List;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

public class ScriptEngineList {

	public static void main(String[] args) {
		ScriptEngineManager mgr = new ScriptEngineManager();
		
	    List<ScriptEngineFactory> factories = mgr.getEngineFactories();

	    for (ScriptEngineFactory factory : factories) {

	        System.out.println("ScriptEngineFactory Info");

	        String engName = factory.getEngineName();
	        String engVersion = factory.getEngineVersion();
	        String langName = factory.getLanguageName();
	        String langVersion = factory.getLanguageVersion();

	        System.out.printf("\tScript Engine: %s (%s)%n", engName, engVersion);

	        List<String> engNames = factory.getNames();
	        for(String name : engNames) {
	            System.out.printf("\tEngine Alias: %s%n", name);
	        }

	        System.out.printf("\tLanguage: %s (%s)%n", langName, langVersion);
	        
	    }

	}

}
