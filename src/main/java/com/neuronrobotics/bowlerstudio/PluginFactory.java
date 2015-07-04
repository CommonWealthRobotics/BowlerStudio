package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;

public interface PluginFactory {
	AbstractBowlerStudioTab generateNewPlugin() throws ClassNotFoundException, InstantiationException, IllegalAccessException ;
}
