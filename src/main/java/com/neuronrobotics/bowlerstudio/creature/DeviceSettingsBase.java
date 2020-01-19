package com.neuronrobotics.bowlerstudio.creature;

import com.neuronrobotics.replicator.driver.NRPrinter;
import com.neuronrobotics.replicator.driver.StateBasedControllerConfiguration;
import javax.swing.JPanel;

public abstract class DeviceSettingsBase extends JPanel {

  private NRPrinter printer;

  public DeviceSettingsBase(NRPrinter _printer) {
    printer = _printer;
  }

  public abstract StateBasedControllerConfiguration getStateBasedControllerSettings();

  public abstract void setStateBasedControllerSettings(StateBasedControllerConfiguration _state);
}
