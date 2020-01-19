package com.neuronrobotics.bowlerstudio.creature;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class CreatureLabControlsTab {
  @FXML private AnchorPane walkingBox;

  @FXML private AnchorPane controlsBox;

  @FXML private AnchorPane progressBar;

  @FXML private AnchorPane treeBox;

  public AnchorPane getWalkingBox() {
    return walkingBox;
  }

  public void setWalkingBox(AnchorPane walkingBox) {
    this.walkingBox = walkingBox;
  }

  public AnchorPane getControlsBox() {
    return controlsBox;
  }

  public void setControlsBox(AnchorPane controlsBox) {
    this.controlsBox = controlsBox;
  }

  //	public AnchorPane getProgressBar() {
  //		return progressBar;
  //	}

  public void setProgressBar(AnchorPane progressBar) {
    this.progressBar = progressBar;
  }

  public AnchorPane getTreeBox() {
    return treeBox;
  }

  public void setTreeBox(AnchorPane treeBox) {
    this.treeBox = treeBox;
  }

  public void setOverlayTop(VBox progress) {
    // TODO Auto-generated method stub
    progressBar.getChildren().clear();
    progressBar.getChildren().add(progress);
    AnchorPane.setTopAnchor(progress, 0.0);
    AnchorPane.setLeftAnchor(progress, 0.0);
    AnchorPane.setRightAnchor(progress, 0.0);
    AnchorPane.setBottomAnchor(progress, 0.0);
  }

  public void setOverlayTopRight(JogWidget walkWidget) {
    // TODO Auto-generated method stub
    walkingBox.getChildren().clear();
    walkingBox.getChildren().add(walkWidget);
    AnchorPane.setTopAnchor(walkWidget, 0.0);
    AnchorPane.setLeftAnchor(walkWidget, 0.0);
    AnchorPane.setRightAnchor(walkWidget, 0.0);
    AnchorPane.setBottomAnchor(walkWidget, 0.0);
  }
}
