package com.neuronrobotics.bowlerstudio.scripting;

import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;

import java.io.File;
import java.util.ArrayList;

public class StlLoader implements IScriptingLanguage {
    @Override
    public Object inlineScriptRun(File code, ArrayList<Object> args) throws Exception {
        return Vitamins.get(code);
    }

    @Override
    public Object inlineScriptRun(String code, ArrayList<Object> args) throws Exception {
        throw new RuntimeException("This engine only supports files");
    }

    @Override
    public String getShellType() {
        return "Stl";
    }

    @Override
    public boolean isSupportedFileExtenetion(String filename) {
        return filename.toLowerCase().endsWith(".stl");
    }

    @Override
    public boolean getIsTextFile() {
        return false;
    }

}
