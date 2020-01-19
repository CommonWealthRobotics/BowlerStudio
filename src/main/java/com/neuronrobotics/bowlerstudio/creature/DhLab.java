package com.neuronrobotics.bowlerstudio.creature;

import com.neuronrobotics.sdk.common.BowlerAbstractDevice;

public class DhLab extends CreatureLab {

  @Override
  public void initializeUI(BowlerAbstractDevice pm) {
    super.initializeUI(pm);
    setText("DH Lab");
  }
}
