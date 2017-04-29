package com.neuronrobotics.graphing;

import java.io.File;

public interface DataWriter {
    void setFile(File f);

    void addData(DataChannel c);

    void cleanup();
}
