/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;
import com.neuronrobotics.bowlerstudio.scripting.CommandLineWidget;
import com.neuronrobotics.bowlerstudio.scripting.IGithubLoginListener;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingFileWidget;
//import com.neuronrobotics.bowlerstudio.scripting.*;
import com.neuronrobotics.bowlerstudio.threed.BowlerStudio3dEngine;
import com.neuronrobotics.bowlerstudio.twod.TwoDCad;
import com.neuronrobotics.bowlerstudio.twod.TwoDCadFactory;
import com.neuronrobotics.imageprovider.CHDKImageProvider;
import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.nrconsole.util.PromptForGit;
import com.neuronrobotics.pidsim.LinearPhysicsEngine;
import com.neuronrobotics.replicator.driver.NRPrinter;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.pid.VirtualGenericPIDDevice;
import com.neuronrobotics.sdk.util.ThreadUtil;

import eu.mihosoft.vrl.v3d.Polygon;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;
import org.reactfx.util.FxTimer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.*;

//import javafx.scene.control.ScrollPane;

/**
 * FXML Controller class
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 * @author Kevin Harrington madhephaestus:github mad.hephaestus@gmail.com
 */
public class MainController implements Initializable {

	private SubScene subScene;
	private BowlerStudio3dEngine jfx3dmanager;
	private File openFile;
	private BowlerStudioController application;
	private MainController mainControllerRef;
	protected EventHandler<? super KeyEvent> normalKeyPessHandle = null;

	// private CommandLineWidget cmdLine;
	// protected EventHandler<? super KeyEvent> normalKeyPessHandle;

	/**
	 * FXML Widgets
	 */

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

	@FXML // fx:id="addMarlinGCODEDevice"
	private MenuItem addMarlinGCODEDevice; // Value injected by FXMLLoader

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

	public void setCadSplit(double value) {
		Platform.runLater(() -> {
			CadTextSplit.setDividerPosition(0, value);
		});
	}



	// private final CodeArea codeArea = new CodeArea();

	/**
	 * Initializes the controller class.
	 *
	 * @param url
	 * @param rb
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		assert BowlerStudioMenue != null : "fx:id=\"BowlerStudioMenue\" was not injected: check your FXML file 'Main.fxml'.";
		assert CadControlsAnchor != null : "fx:id=\"CadControlsAnchor\" was not injected: check your FXML file 'Main.fxml'.";
		assert CommandLine != null : "fx:id=\"CommandLine\" was not injected: check your FXML file 'Main.fxml'.";
		assert CreaturesMenu != null : "fx:id=\"CreaturesMenu\" was not injected: check your FXML file 'Main.fxml'.";
		assert DriveControlsAnchor != null : "fx:id=\"DriveControlsAnchor\" was not injected: check your FXML file 'Main.fxml'.";
		assert GitHubRoot != null : "fx:id=\"GitHubRoot\" was not injected: check your FXML file 'Main.fxml'.";
		assert TempControlsAnchor != null : "fx:id=\"TempControlsAnchor\" was not injected: check your FXML file 'Main.fxml'.";
		assert clearCache != null : "fx:id=\"clearCache\" was not injected: check your FXML file 'Main.fxml'.";
		assert commandLineTitledPane != null : "fx:id=\"commandLineTitledPane\" was not injected: check your FXML file 'Main.fxml'.";
		assert createNewGist != null : "fx:id=\"createNewGist\" was not injected: check your FXML file 'Main.fxml'.";
		assert editorContainer != null : "fx:id=\"editorContainer\" was not injected: check your FXML file 'Main.fxml'.";
		assert jfx3dControls != null : "fx:id=\"jfx3dControls\" was not injected: check your FXML file 'Main.fxml'.";
		assert logView != null : "fx:id=\"logView\" was not injected: check your FXML file 'Main.fxml'.";
		assert logViewRef != null : "fx:id=\"logViewRef\" was not injected: check your FXML file 'Main.fxml'.";
		assert logoutGithub != null : "fx:id=\"logoutGithub\" was not injected: check your FXML file 'Main.fxml'.";
		assert myGists != null : "fx:id=\"myGists\" was not injected: check your FXML file 'Main.fxml'.";
		assert myOrganizations != null : "fx:id=\"myOrganizations\" was not injected: check your FXML file 'Main.fxml'.";
		assert myRepos != null : "fx:id=\"myRepos\" was not injected: check your FXML file 'Main.fxml'.";
		assert overlayScrollPanel != null : "fx:id=\"overlayScrollPanel\" was not injected: check your FXML file 'Main.fxml'.";
		assert viewContainer != null : "fx:id=\"viewContainer\" was not injected: check your FXML file 'Main.fxml'.";
		assert watchingRepos != null : "fx:id=\"watchingRepos\" was not injected: check your FXML file 'Main.fxml'.";
		assert addMarlinGCODEDevice != null : "fx:id=\"addMarlinGCODEDevice\" was not injected: check your FXML file 'Main.fxml'.";
		
		BowlerStudio.setLogViewRefStatic(logViewRef);
		System.out.println("Main controller inializing");
		mainControllerRef = this;
		addMarlinGCODEDevice.setOnAction(event->{
			Platform.runLater(() -> ConnectionManager.onMarlinGCODE());
		});
		new Thread(new Runnable() {

			@Override
			public void run() {
				ThreadUtil.wait(200);

				// ScriptingEngine.getGithub().getMyself().getGravatarId()
				// System.out.println("Loading 3d engine");
				jfx3dmanager = new BowlerStudio3dEngine();

				//setApplication(new BowlerStudioController(jfx3dmanager));
				Platform.runLater(() -> {
//					editorContainer.getChildren().add(getApplication());
//					AnchorPane.setTopAnchor(getApplication(), 0.0);
//					AnchorPane.setRightAnchor(getApplication(), 0.0);
//					AnchorPane.setLeftAnchor(getApplication(), 0.0);
//					AnchorPane.setBottomAnchor(getApplication(), 0.0);

					subScene = jfx3dmanager.getSubScene();
					subScene.setFocusTraversable(false);
					subScene.setOnMouseEntered(mouseEvent -> {
						// System.err.println("3d window requesting focus");
						Scene topScene = BowlerStudio.getScene();
						normalKeyPessHandle = topScene.getOnKeyPressed();
						// jfx3dmanager.handleKeyboard(topScene);
					});

					subScene.setOnMouseExited(mouseEvent -> {
						// System.err.println("3d window dropping focus");
						Scene topScene = BowlerStudio.getScene();
						if (normalKeyPessHandle != null)
							topScene.setOnKeyPressed(normalKeyPessHandle);
					});

					subScene.widthProperty().bind(viewContainer.widthProperty());
					subScene.heightProperty().bind(viewContainer.heightProperty());
				});

				Platform.runLater(() -> {
					jfx3dControls.getChildren().add(jfx3dmanager.getControlsBox());
					viewContainer.getChildren().add(subScene);
				});

				FxTimer.runLater(Duration.ofMillis(100), () -> {
					if (ScriptingEngine.getLoginID() != null) {
						//setToLoggedIn(ScriptingEngine.getLoginID());
					} else {
						//setToLoggedOut();
					}

				});

				ScriptingEngine.addIGithubLoginListener(new IGithubLoginListener() {

					@Override
					public void onLogout(String oldUsername) {
						//setToLoggedOut();
					}

					@Override
					public void onLogin(String newUsername) {
						//setToLoggedIn(newUsername);

					}
				});
				// System.out.println("Laoding ommand line widget");
				CommandLineWidget cmdLine = new CommandLineWidget();

				Platform.runLater(() -> {
//					// CadDebugger.getChildren().add(jfx3dmanager.getDebuggerBox());
//					AnchorPane.setTopAnchor(jfx3dmanager.getDebuggerBox(), 0.0);
//					AnchorPane.setRightAnchor(jfx3dmanager.getDebuggerBox(), 0.0);
//					AnchorPane.setLeftAnchor(jfx3dmanager.getDebuggerBox(), 0.0);
//					AnchorPane.setBottomAnchor(jfx3dmanager.getDebuggerBox(), 0.0);
					CommandLine.getChildren().add(cmdLine);
					AnchorPane.setTopAnchor(cmdLine, 0.0);
					AnchorPane.setRightAnchor(cmdLine, 0.0);
					AnchorPane.setLeftAnchor(cmdLine, 0.0);
					AnchorPane.setBottomAnchor(cmdLine, 0.0);
				});
				
				
			}
		}).start();
		Platform.runLater(() -> {
			commandLineTitledPane.setGraphic(AssetFactory.loadIcon("Command-Line.png"));
		});

	}




}
