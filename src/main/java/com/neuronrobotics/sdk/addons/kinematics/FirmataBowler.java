package com.neuronrobotics.sdk.addons.kinematics;

import com.neuronrobotics.sdk.common.NonBowlerDevice;
import org.firmata4j.firmata.FirmataDevice;

import java.io.IOException;
import java.util.ArrayList;

public class FirmataBowler extends NonBowlerDevice {

	private FirmataDevice device;
	public FirmataBowler(String port){
		setFirmataDevice(new FirmataDevice(port));
		
	}
		
	@Override
	public boolean connectDeviceImp() {
		
		try {
			getFirmataDevice().start(); // initiate communication to the device
			getFirmataDevice().ensureInitializationIsDone();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} // wait for initialization is done
		return true;
	}

	@Override
	public void disconnectDeviceImp() {
		System.out.println("Closing Firmata");
		try {
			getFirmataDevice().stop();
		} catch (IOException e) {
			e.printStackTrace();
		} // stop communication to the device
	}

	@Override
	public ArrayList<String> getNamespacesImp() {
		return new ArrayList<String>();
	}

	public FirmataDevice getFirmataDevice() {
		return device;
	}

	public void setFirmataDevice(FirmataDevice device) {
		this.device = device;
	}

}
