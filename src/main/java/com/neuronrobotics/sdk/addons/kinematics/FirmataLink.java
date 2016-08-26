package com.neuronrobotics.sdk.addons.kinematics;

import java.io.IOException;

import org.firmata4j.IOEvent;
import org.firmata4j.Pin;
import org.firmata4j.PinEventListener;

public class FirmataLink extends AbstractLink implements  PinEventListener{

	
	private FirmataBowler device;
	private Pin pin;

	public FirmataLink(LinkConfiguration arg0,FirmataBowler device) throws InterruptedException, IllegalArgumentException, IOException {
		super(arg0);
		this.device = device;
		
		pin = device.getFirmataDevice().getPin(arg0.getHardwareIndex());
		pin.setMode(Pin.Mode.SERVO); // our listeners will get event about this change
	}

	@Override
	public void cacheTargetValueDevice() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void flushAllDevice(double arg0) {
		// TODO Auto-generated method stub
		flushDevice(arg0);
	}

	@Override
	public void flushDevice(double arg0) {
		try {
			pin.setValue((long) getTargetValue());
		} catch (IllegalStateException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public double getCurrentPosition() {
		// TODO Auto-generated method stub
		return pin.getValue();
	}

	@Override
	public void onModeChange(IOEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onValueChange(IOEvent event) {
		// TODO Auto-generated method stub
		fireLinkListener(getCurrentPosition());
		
	}

}
