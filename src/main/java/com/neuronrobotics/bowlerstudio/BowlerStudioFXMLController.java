package com.neuronrobotics.bowlerstudio;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;

public class BowlerStudioFXMLController {

  @FXML // ResourceBundle that was given to the FXMLLoader
  private ResourceBundle resources;

  @FXML // URL location of the FXML file that was given to the FXMLLoader
  private URL location;

  @FXML // fx:id="BowlerStudioMenue"
  private MenuBar BowlerStudioMenue; // Value injected by FXMLLoader

  @FXML // fx:id="CadControlsAnchor"
  private AnchorPane CadControlsAnchor; // Value injected by FXMLLoader

  @FXML // fx:id="CadTextSplit"
  private SplitPane CadTextSplit; // Value injected by FXMLLoader

  @FXML // fx:id="CommandLine"
  private AnchorPane CommandLine; // Value injected by FXMLLoader

  @FXML // fx:id="CreaturesMenu"
  private Menu CreaturesMenu; // Value injected by FXMLLoader

  @FXML // fx:id="DriveControlsAnchor"
  private AnchorPane DriveControlsAnchor; // Value injected by FXMLLoader

  @FXML // fx:id="GitHubRoot"
  private Menu GitHubRoot; // Value injected by FXMLLoader

  @FXML // fx:id="TempControlsAnchor"
  private AnchorPane TempControlsAnchor; // Value injected by FXMLLoader

  @FXML // fx:id="clearCache"
  private MenuItem clearCache; // Value injected by FXMLLoader

  @FXML // fx:id="commandLineTitledPane"
  private TitledPane commandLineTitledPane; // Value injected by FXMLLoader

  @FXML // fx:id="createNewGist"
  private MenuItem createNewGist; // Value injected by FXMLLoader

  @FXML // fx:id="editorContainer"
  private AnchorPane editorContainer; // Value injected by FXMLLoader

  @FXML // fx:id="jfx3dControls"
  private AnchorPane jfx3dControls; // Value injected by FXMLLoader

  @FXML // fx:id="logView"
  private AnchorPane logView; // Value injected by FXMLLoader

  @FXML // fx:id="logViewRef"
  private TextArea logViewRef; // Value injected by FXMLLoader

  @FXML // fx:id="logoutGithub"
  private MenuItem logoutGithub; // Value injected by FXMLLoader

  @FXML // fx:id="myGists"
  private Menu myGists; // Value injected by FXMLLoader

  @FXML // fx:id="myOrganizations"
  private Menu myOrganizations; // Value injected by FXMLLoader

  @FXML // fx:id="myRepos"
  private Menu myRepos; // Value injected by FXMLLoader

  @FXML // fx:id="overlayScrollPanel"
  private ScrollPane overlayScrollPanel; // Value injected by FXMLLoader

  @FXML // fx:id="viewContainer"
  private AnchorPane viewContainer; // Value injected by FXMLLoader

  @FXML // fx:id="watchingRepos"
  private Menu watchingRepos; // Value injected by FXMLLoader

  // Handler for MenuItem[fx:id="clearCache"] onAction
  @FXML
  void clearScriptCache(ActionEvent event) {
    // handle the event here
  }

  // Handler for MenuItem[javafx.scene.control.MenuItem@3d05e65e] onAction
  @FXML
  void onClose(ActionEvent event) {
    // handle the event here
  }

  // Handler for MenuItem[javafx.scene.control.MenuItem@6127215f] onAction
  @FXML
  void onConnect(ActionEvent event) {
    // handle the event here
  }

  // Handler for MenuItem[javafx.scene.control.MenuItem@1d163e1a] onAction
  @FXML
  void onConnectCHDKCamera(ActionEvent event) {
    // handle the event here
  }

  // Handler for MenuItem[javafx.scene.control.MenuItem@3cad92b8] onAction
  @FXML
  void onConnectCVCamera(ActionEvent event) {
    // handle the event here
  }

  // Handler for MenuItem[javafx.scene.control.MenuItem@542487b1] onAction
  @FXML
  void onConnectFileSourceCamera(ActionEvent event) {
    // handle the event here
  }

  // Handler for MenuItem[javafx.scene.control.MenuItem@4845d9d8] onAction
  @FXML
  void onConnectGamePad(ActionEvent event) {
    // handle the event here
  }

  // Handler for MenuItem[javafx.scene.control.MenuItem@36100c4d] onAction
  @FXML
  void onConnectHokuyoURG(ActionEvent event) {
    // handle the event here
  }

  // Handler for MenuItem[javafx.scene.control.MenuItem@7a7cbb68] onAction
  @FXML
  void onConnectPidSim(ActionEvent event) {
    // handle the event here
  }

  // Handler for MenuItem[javafx.scene.control.MenuItem@7d3d14a7] onAction
  @FXML
  void onConnectURLSourceCamera(ActionEvent event) {
    // handle the event here
  }

  // Handler for MenuItem[javafx.scene.control.MenuItem@68e8dfb1] onAction
  @FXML
  void onConnectVirtual(ActionEvent event) {
    // handle the event here
  }

  // Handler for MenuItem[fx:id="createNewGist"] onAction
  @FXML
  void onCreatenewGist(ActionEvent event) {
    // handle the event here
  }

  // Handler for MenuItem[javafx.scene.control.MenuItem@6bfd67ba] onAction
  @FXML
  void onLoadFile(ActionEvent event) {
    // handle the event here
  }

  // Handler for MenuItem[javafx.scene.control.MenuItem@41a2e40f] onAction
  @FXML
  void onLogin(ActionEvent event) {
    // handle the event here
  }

  // Handler for MenuItem[fx:id="logoutGithub"] onAction
  @FXML
  void onLogout(ActionEvent event) {
    // handle the event here
  }

  // Handler for MenuItem[javafx.scene.control.MenuItem@7e3e1a61] onAction
  @FXML
  void onMobileBaseFromFile(ActionEvent event) {
    // handle the event here
  }

  // Handler for MenuItem[javafx.scene.control.MenuItem@57184345] onAction
  @FXML
  void onMobileBaseFromGit(ActionEvent event) {
    // handle the event here
  }

  // Handler for MenuItem[javafx.scene.control.MenuItem@d523af5] onAction
  @FXML
  void onOpenGitter(ActionEvent event) {
    // handle the event here
  }

  // Handler for MenuItem[javafx.scene.control.MenuItem@2a1c81e] onAction
  @FXML
  void onPrint(ActionEvent event) {
    // handle the event here
  }

  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert BowlerStudioMenue != null
        : "fx:id=\"BowlerStudioMenue\" was not injected: check your FXML file 'Main.fxml'.";
    assert CadControlsAnchor != null
        : "fx:id=\"CadControlsAnchor\" was not injected: check your FXML file 'Main.fxml'.";
    assert CadTextSplit != null
        : "fx:id=\"CadTextSplit\" was not injected: check your FXML file 'Main.fxml'.";
    assert CommandLine != null
        : "fx:id=\"CommandLine\" was not injected: check your FXML file 'Main.fxml'.";
    assert CreaturesMenu != null
        : "fx:id=\"CreaturesMenu\" was not injected: check your FXML file 'Main.fxml'.";
    assert DriveControlsAnchor != null
        : "fx:id=\"DriveControlsAnchor\" was not injected: check your FXML file 'Main.fxml'.";
    assert GitHubRoot != null
        : "fx:id=\"GitHubRoot\" was not injected: check your FXML file 'Main.fxml'.";
    assert TempControlsAnchor != null
        : "fx:id=\"TempControlsAnchor\" was not injected: check your FXML file 'Main.fxml'.";
    assert clearCache != null
        : "fx:id=\"clearCache\" was not injected: check your FXML file 'Main.fxml'.";
    assert commandLineTitledPane != null
        : "fx:id=\"commandLineTitledPane\" was not injected: check your FXML file 'Main.fxml'.";
    assert createNewGist != null
        : "fx:id=\"createNewGist\" was not injected: check your FXML file 'Main.fxml'.";
    assert editorContainer != null
        : "fx:id=\"editorContainer\" was not injected: check your FXML file 'Main.fxml'.";
    assert jfx3dControls != null
        : "fx:id=\"jfx3dControls\" was not injected: check your FXML file 'Main.fxml'.";
    assert logView != null
        : "fx:id=\"logView\" was not injected: check your FXML file 'Main.fxml'.";
    assert logViewRef != null
        : "fx:id=\"logViewRef\" was not injected: check your FXML file 'Main.fxml'.";
    assert logoutGithub != null
        : "fx:id=\"logoutGithub\" was not injected: check your FXML file 'Main.fxml'.";
    assert myGists != null
        : "fx:id=\"myGists\" was not injected: check your FXML file 'Main.fxml'.";
    assert myOrganizations != null
        : "fx:id=\"myOrganizations\" was not injected: check your FXML file 'Main.fxml'.";
    assert myRepos != null
        : "fx:id=\"myRepos\" was not injected: check your FXML file 'Main.fxml'.";
    assert overlayScrollPanel != null
        : "fx:id=\"overlayScrollPanel\" was not injected: check your FXML file 'Main.fxml'.";
    assert viewContainer != null
        : "fx:id=\"viewContainer\" was not injected: check your FXML file 'Main.fxml'.";
    assert watchingRepos != null
        : "fx:id=\"watchingRepos\" was not injected: check your FXML file 'Main.fxml'.";

    // Initialize your logic here: all @FXML variables will have been injected

  }
}
