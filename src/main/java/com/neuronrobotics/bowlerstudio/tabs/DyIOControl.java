package com.neuronrobotics.bowlerstudio.tabs;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.assets.BowlerStudioResourceFactory;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.dyio.DyIO;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class DyIOControl extends AbstractBowlerStudioTab {

  private DyIO dyio;
  DyIOPanel controller;

  @Override
  public void onTabClosing() {
    // TODO Auto-generated method stub

  }

  @Override
  public String[] getMyNameSpaces() {
    // TODO Auto-generated method stub
    return new String[] {"neuronrobotics.dyio.*"};
  }

  @Override
  public void initializeUI(BowlerAbstractDevice pm) {
    this.dyio = (DyIO) pm;
    setGraphic(AssetFactory.loadIcon("DyIO-Tab.png"));

    setText(dyio.getScriptingName());
    FXMLLoader fxmlLoader = BowlerStudioResourceFactory.getMainPanel();
    Parent root = fxmlLoader.getRoot();
    controller = fxmlLoader.getController();
    controller.setDyIO(dyio);
    setContent(root);
  }

  @Override
  public void onTabReOpening() {
    // TODO Auto-generated method stub

  }
}
