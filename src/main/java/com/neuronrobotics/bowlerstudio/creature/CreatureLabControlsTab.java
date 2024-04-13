package com.neuronrobotics.bowlerstudio.creature;
import com.neuronrobotics.bowlerstudio.BowlerStudio;

import javafx.fxml.FXML;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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


	public void setOverlayTop(GridPane radioOptions) {
		// TODO Auto-generated method stub
		BowlerStudio.runLater(()->{
			progressBar.getChildren().clear();
			progressBar.getChildren().add(radioOptions);
			AnchorPane.setTopAnchor(radioOptions, 0.0);
			AnchorPane.setLeftAnchor(radioOptions, 0.0);
	     	AnchorPane.setRightAnchor(radioOptions, 0.0);
	     	AnchorPane.setBottomAnchor(radioOptions, 0.0);
		});
	}

	public void setOverlayTopRight(JogMobileBase walkWidget) {
		// TODO Auto-generated method stub
		// @JansenSmith - placed contents in llambda runnable - 20220915
		BowlerStudio.runLater(()->{
			walkingBox.getChildren().clear();
			walkingBox.getChildren().add(walkWidget);
			AnchorPane.setTopAnchor(walkWidget, 0.0);
			AnchorPane.setLeftAnchor(walkWidget, 0.0);
	     	AnchorPane.setRightAnchor(walkWidget, 0.0);
	     	AnchorPane.setBottomAnchor(walkWidget, 0.0);
		});
	}

    

}
