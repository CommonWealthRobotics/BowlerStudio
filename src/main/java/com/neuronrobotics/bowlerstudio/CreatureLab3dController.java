package com.neuronrobotics.bowlerstudio;

/**
 * Sample Skeleton for "CreatureLab.fxml" Controller Class You can copy and paste this code into
 * your favorite IDE
 */
import com.neuronrobotics.bowlerstudio.threed.BowlerStudio3dEngine;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

public class CreatureLab3dController {

  @FXML // ResourceBundle that was given to the FXMLLoader
  private ResourceBundle resources;

  @FXML // URL location of the FXML file that was given to the FXMLLoader
  private URL location;

  @FXML // fx:id="CadControlsAnchor"
  private AnchorPane CadControlsAnchor; // Value injected by FXMLLoader

  @FXML // fx:id="DriveControlsAnchor"
  private AnchorPane DriveControlsAnchor; // Value injected by FXMLLoader

  @FXML // fx:id="TempControlsAnchor"
  private AnchorPane TempControlsAnchor; // Value injected by FXMLLoader

  @FXML // fx:id="jfx3dControls"
  private AnchorPane jfx3dControls; // Value injected by FXMLLoader

  @FXML // fx:id="overlayScrollPanel"
  private ScrollPane overlayScrollPanel; // Value injected by FXMLLoader

  @FXML // fx:id="viewContainer"
  private AnchorPane viewContainer; // Value injected by FXMLLoader
  // private SubScene subScene;
  protected EventHandler<? super KeyEvent> normalKeyPessHandle = null;

  private static BowlerStudio3dEngine engine;

  public CreatureLab3dController() {

    setEngine(new BowlerStudio3dEngine());
  }

  public void setOverlayLeft(Node content) {
    Platform.runLater(
        () -> {
          overlayScrollPanel.setFitToHeight(true);
          overlayScrollPanel.setContent(content);
          content.setOpacity(1);
          overlayScrollPanel
              .viewportBoundsProperty()
              .addListener(
                  (ObservableValue<? extends Bounds> arg0, Bounds arg1, Bounds arg2) -> {
                    // Node content = overlayScrollPanel.getContent();
                    //
                    // System.out.println("Resizing " + arg2);
                    Platform.runLater(
                        () -> {
                          overlayScrollPanel.setFitToHeight(true);
                          /// content.seth
                          overlayScrollPanel.setContent(content);
                        });
                  });
          overlayScrollPanel.setVisible(true);
        });
  }

  public void clearOverlayLeft() {
    Platform.runLater(
        () -> {
          overlayScrollPanel.setContent(null);
          overlayScrollPanel.setVisible(false);
        });
  }

  public void setOverlayTop(Group content) {
    Platform.runLater(
        () -> {
          CadControlsAnchor.getChildren().clear();
          CadControlsAnchor.getChildren().add(content);

          AnchorPane.setTopAnchor(content, 0.0);
          AnchorPane.setRightAnchor(content, 0.0);
          AnchorPane.setLeftAnchor(content, 0.0);
          AnchorPane.setBottomAnchor(content, 0.0);
          CadControlsAnchor.setVisible(true);
        });
  }

  public void clearOverlayTop() {
    Platform.runLater(
        () -> {
          CadControlsAnchor.getChildren().clear();
          CadControlsAnchor.setVisible(false);
        });
  }

  public void setOverlayTopRight(Group content) {
    Platform.runLater(
        () -> {
          DriveControlsAnchor.getChildren().clear();
          DriveControlsAnchor.getChildren().add(content);
          AnchorPane.setTopAnchor(content, 0.0);
          AnchorPane.setRightAnchor(content, 0.0);
          AnchorPane.setLeftAnchor(content, 0.0);
          AnchorPane.setBottomAnchor(content, 0.0);
          DriveControlsAnchor.setVisible(true);
        });
  }

  public void clearOverlayTopRight() {
    Platform.runLater(
        () -> {
          DriveControlsAnchor.getChildren().clear();
          DriveControlsAnchor.setVisible(false);
        });
  }

  public void setOverlayBottomRight(Group content) {
    Platform.runLater(
        () -> {
          TempControlsAnchor.getChildren().clear();
          TempControlsAnchor.getChildren().add(content);
          AnchorPane.setTopAnchor(content, 0.0);
          AnchorPane.setRightAnchor(content, 0.0);
          AnchorPane.setLeftAnchor(content, 0.0);
          AnchorPane.setBottomAnchor(content, 0.0);
          TempControlsAnchor.setVisible(true);
        });
  }

  public void clearOverlayBottomRight() {
    Platform.runLater(
        () -> {
          TempControlsAnchor.getChildren().clear();
          TempControlsAnchor.setVisible(false);
        });
  }

  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert CadControlsAnchor != null
        : "fx:id=\"CadControlsAnchor\" was not injected: check your FXML file 'CreatureLab.fxml'.";
    assert DriveControlsAnchor != null
        : "fx:id=\"DriveControlsAnchor\" was not injected: check your FXML file 'CreatureLab.fxml'.";
    assert TempControlsAnchor != null
        : "fx:id=\"TempControlsAnchor\" was not injected: check your FXML file 'CreatureLab.fxml'.";
    assert jfx3dControls != null
        : "fx:id=\"jfx3dControls\" was not injected: check your FXML file 'CreatureLab.fxml'.";
    assert overlayScrollPanel != null
        : "fx:id=\"overlayScrollPanel\" was not injected: check your FXML file 'CreatureLab.fxml'.";
    assert viewContainer != null
        : "fx:id=\"viewContainer\" was not injected: check your FXML file 'CreatureLab.fxml'.";
    clearOverlayLeft();
    // Initialize your logic here: all @FXML variables will have been injected
    setupUi();
  }

  private void setupUi() {
    Platform.runLater(
        () -> {
          getEngine().getSubScene().setFocusTraversable(false);
          getEngine().getSubScene().widthProperty().bind(viewContainer.widthProperty());
          getEngine().getSubScene().heightProperty().bind(viewContainer.heightProperty());
        });
    Platform.runLater(
        () -> {
          jfx3dControls.getChildren().add(getEngine().getControlsBox());
          viewContainer.getChildren().add(getEngine().getSubScene());
          AnchorPane.setTopAnchor(getEngine().getSubScene(), 0.0);
          AnchorPane.setRightAnchor(getEngine().getSubScene(), 0.0);
          AnchorPane.setLeftAnchor(getEngine().getSubScene(), 0.0);
          AnchorPane.setBottomAnchor(getEngine().getSubScene(), 0.0);
        });
  }

  public static BowlerStudio3dEngine getEngine() {
    return engine;
  }

  public static void setEngine(BowlerStudio3dEngine engine) {
    CreatureLab3dController.engine = engine;
  }
}
