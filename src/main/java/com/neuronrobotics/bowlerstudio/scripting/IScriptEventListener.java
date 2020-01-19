package com.neuronrobotics.bowlerstudio.scripting;

import java.io.File;

public interface IScriptEventListener {

  void onScriptFinished(Object result, Object pervious, File source);

  void onScriptChanged(String previous, String current, File source);

  void onScriptError(Throwable except, File source);
}
