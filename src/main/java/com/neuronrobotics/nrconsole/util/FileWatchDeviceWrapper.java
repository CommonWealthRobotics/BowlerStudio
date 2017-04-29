package com.neuronrobotics.nrconsole.util;

import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.IDeviceConnectionEventListener;
import com.neuronrobotics.sdk.util.IFileChangeListener;

import java.io.File;
import java.io.IOException;

public class FileWatchDeviceWrapper {

    public static FileChangeWatcher watch(BowlerAbstractDevice device, File code, IFileChangeListener cadWatcher) {
        try {
            FileChangeWatcher watcher = FileChangeWatcher.watch(code);
            watcher.addIFileChangeListener(cadWatcher);
            device.addConnectionEventListener(new IDeviceConnectionEventListener() {

                @Override
                public void onDisconnect(BowlerAbstractDevice arg0) {
                    watcher.removeIFileChangeListener(cadWatcher);
                }

                @Override
                public void onConnect(BowlerAbstractDevice arg0) {
                }
            });
            return watcher;
        } catch (IOException e) {
            BowlerStudioController.highlightException(code, e);
        }
        return null;
    }

}
