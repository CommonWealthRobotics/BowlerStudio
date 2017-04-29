package com.neuronrobotics.bowlerstudio.creature;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

public class CreatureLabControlsTab {
    @FXML
    private AnchorPane walkingBox;
    @FXML
    private AnchorPane controlsBox;
    @FXML
    private AnchorPane progressBar;
    @FXML
    private AnchorPane treeBox;
    
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

	public AnchorPane getProgressBar() {
		return progressBar;
	}

	public void setProgressBar(AnchorPane progressBar) {
		this.progressBar = progressBar;
	}

	public AnchorPane getTreeBox() {
		return treeBox;
	}

	public void setTreeBox(AnchorPane treeBox) {
		this.treeBox = treeBox;
	}

	public void setOverlayTop(HBox progress) {
		progressBar.getChildren().clear();
		progressBar.getChildren().add(progress);
		setAnchorsToZero(progress);
	}

	public void setOverlayTopRight(JogWidget walkWidget) {
		walkingBox.getChildren().clear();
		walkingBox.getChildren().add(walkWidget);
		setAnchorsToZero(walkWidget);
	}

	private void setAnchorsToZero(Node node) {
		AnchorPane.setTopAnchor(node, 0.0);
		AnchorPane.setLeftAnchor(node, 0.0);
		AnchorPane.setRightAnchor(node, 0.0);
		AnchorPane.setBottomAnchor(node, 0.0);
	}
}
