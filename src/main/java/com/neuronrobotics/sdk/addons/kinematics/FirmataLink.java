package com.neuronrobotics.sdk.addons.kinematics;

import com.neuronrobotics.sdk.common.DeviceManager;
import java.io.IOException;
import org.firmata4j.IOEvent;
import org.firmata4j.Pin;
import org.firmata4j.PinEventListener;

public class FirmataLink extends AbstractLink implements PinEventListener {

  private Pin pin;

  public FirmataLink(LinkConfiguration arg0, FirmataBowler device)
      throws InterruptedException, IllegalArgumentException, IOException {
    super(arg0);

    pin = device.getFirmataDevice().getPin(arg0.getHardwareIndex());
    pin.setMode(Pin.Mode.SERVO);
    // our listeners will get event about this change
    pin.addEventListener(this);
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
      e.printStackTrace();
    }
  }

  @Override
  public double getCurrentPosition() {
    return pin.getValue();
  }

  @Override
  public void onModeChange(IOEvent event) {}

  @Override
  public void onValueChange(IOEvent event) {
    fireLinkListener(getCurrentPosition());
  }

  public static void addLinkFactory() {
    INewLinkProvider lp =
        new INewLinkProvider() {
          @Override
          public AbstractLink generate(LinkConfiguration config) {
            FirmataBowler dev =
                (FirmataBowler)
                    DeviceManager.getSpecificDevice(
                        FirmataBowler.class, config.getDeviceScriptingName());
            if (dev != null)
              try {
                return new FirmataLink(config, dev);
              } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
            return null;
          }
        };
    LinkFactory.addLinkProvider("firmata-servo-rotory", lp);
    LinkFactory.addLinkProvider("firmata-servo-prismatic", lp);
  }
}
