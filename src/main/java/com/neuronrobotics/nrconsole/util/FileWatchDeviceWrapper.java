package com.neuronrobotics.nrconsole.util;

import java.io.File;
import java.io.IOException;

import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.IDeviceConnectionEventListener;
import com.neuronrobotics.sdk.util.IFileChangeListener;

public class FileWatchDeviceWrapper {
	
	public static FileChangeWatcher watch(BowlerAbstractDevice device, File code,IFileChangeListener cadWatcher){
		try {
			FileChangeWatcher watcher = FileChangeWatcher.watch(code);
			watcher.addIFileChangeListener(cadWatcher);
			device.addConnectionEventListener(new IDeviceConnectionEventListener() {
				
				@Override
				public void onDisconnect(BowlerAbstractDevice arg0) {
					// TODO Auto-generated method stub
					watcher.removeIFileChangeListener(cadWatcher);
				}
				
				@Override
				public void onConnect(BowlerAbstractDevice arg0) {
					// TODO Auto-generated method stub
					
				}
			});
			return watcher;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			BowlerStudioController.highlightException(code, e);
		}
		return null;
	}

}
