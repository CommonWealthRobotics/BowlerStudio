package com.neuronrobotics.bowlerstudio;
/**
 * Sample Skeleton for "Terminal.fxml" Controller Class
 * You can copy and paste this code into your favorite IDE
 **/

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
//import com.neuronrobotics.imageprovider.OpenCVImageProvider;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.util.ThreadUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Terminal {

	@FXML // ResourceBundle that was given to the FXMLLoader
	private ResourceBundle resources;

	@FXML // URL location of the FXML file that was given to the FXMLLoader
	private URL location;

	@FXML // fx:id="executionBox"
	private TextField executionBox; // Value injected by FXMLLoader

	@FXML // fx:id="langaugeIcon"
	private ImageView langaugeIcon; // Value injected by FXMLLoader

	@FXML // fx:id="langauges"
	private ComboBox<String> langauges; // Value injected by FXMLLoader

	@FXML // fx:id="outputBox"
	private TextArea outputBox; // Value injected by FXMLLoader

	private ArrayList<String> history = new ArrayList<>();

	private int historyIndex = 0;
	private boolean running = false;
	private Thread scriptRunner = null;

	@SuppressWarnings("restriction")
	@FXML // This method is called by the FXMLLoader when initialization is
			// complete
	void initialize() {
		assert executionBox != null : "fx:id=\"executionBox\" was not injected: check your FXML file 'Terminal.fxml'.";
		assert langaugeIcon != null : "fx:id=\"langaugeIcon\" was not injected: check your FXML file 'Terminal.fxml'.";
		assert langauges != null : "fx:id=\"langauges\" was not injected: check your FXML file 'Terminal.fxml'.";
		assert outputBox != null : "fx:id=\"outputBox\" was not injected: check your FXML file 'Terminal.fxml'.";
		BowlerStudio.setLogViewRefStatic(outputBox);
		// Initialize your logic here: all @FXML variables will have been
		// injected
		langauges.getItems().clear();
		executionBox.setOnAction(event -> {
			new Thread() {
				public void run() {

					startStopAction();
				}
			}.start();

		});
		executionBox.setPrefWidth(80 * 4);
		executionBox.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			// Platform.runLater(() -> {
			if ((event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN)) {
				System.err.println("Key pressed " + event.getCode() + " history index = " + historyIndex
						+ " history size= " + history.size());
				if (historyIndex == 0) {
					String text = executionBox.getText();
					if (text.length() > 0) {
						// store what was in the box into he history
						history.add(text);
					}
				}

				if (event.getCode() == KeyCode.UP)
					historyIndex++;
				else
					historyIndex--;
				if (!history.isEmpty()) {
					if (historyIndex > history.size()) {
						historyIndex = history.size();
					}
					if (historyIndex < 0)
						historyIndex = 0;
					// History index established
					if (historyIndex > 0)
						Platform.runLater(() -> {
							executionBox.setText(history.get(history.size() - historyIndex));
						});
					else
						Platform.runLater(() -> {
							executionBox.setText("");
						});
				}
				event.consume();
			}
			// });
		});
		try {
			history = BowlerKernel.loadHistory();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {

				BowlerKernel.writeHistory(history);
			}
		});
		List<String> langs = ScriptingEngine.getAllLangauges();
		ObservableList<String> options = FXCollections.observableArrayList(langs);
		//
		for(String s:options){
			langauges.getItems().add(s);
		}
		langauges.getSelectionModel().select("Groovy");
		Image icon;
		try {
			icon = AssetFactory.loadAsset("Script-Tab-" + langauges.getSelectionModel().getSelectedItem() + ".png");
			langaugeIcon.setImage(icon);
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		langauges.setOnAction(event -> {
			try {

				langaugeIcon.setImage(AssetFactory
						.loadAsset("Script-Tab-" + langauges.getSelectionModel().getSelectedItem() + ".png"));
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		});

	}

	private void reset() {
		running = false;

	}

	private void start(String code) {

		running = true;

		scriptRunner = new Thread() {

			@SuppressWarnings("restriction")
			public void run() {

				try {
					ScriptingEngine.inlineScriptStringRun(code, null,
							langauges.getSelectionModel().getSelectedItem());
					reset();
				} catch (groovy.lang.MissingPropertyException | org.python.core.PyException d) {
					Platform.runLater(() -> {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Device missing error");
						String message = "This script needs a device connected: ";
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						d.printStackTrace(pw);
						BowlerStudioController.highlightException(null, d);

						String stackTrace = sw.toString();

						if (stackTrace.contains("dyio"))
							message += "dyio";
						else if (stackTrace.contains("camera"))
							message += "camera";
						else if (stackTrace.contains("gamepad"))
							message += "gamepad";
						else
							message += stackTrace;
						alert.setHeaderText(message);
						alert.setContentText("You need to connect it before running again");
						alert.showAndWait();
						if (stackTrace.contains("dyio"))
							ConnectionManager.addConnection();
//						else if (stackTrace.contains("camera"))
//							ConnectionManager.addConnection(new OpenCVImageProvider(0), "camera0");
						else if (stackTrace.contains("gamepad"))
							ConnectionManager.onConnectGamePad("gamepad");
						reset();
					});

				} catch (Exception ex) {
					System.err.println("Script exception of type= " + ex.getClass().getName());
					Platform.runLater(() -> {
						reset();
					});

					BowlerStudioController.highlightException(null, ex);
				}

			}
		};

		scriptRunner.start();

	}

	private void startStopAction() {
		String text = executionBox.getText();
		text += "\r\n";
		Platform.runLater(() -> {
			executionBox.setText("");
		});
		System.out.println(text);
		history.add(text);
		BowlerKernel.writeHistory(history);
		if (historyIndex != 0)
			historyIndex--;
		else
			historyIndex = 0;
		if (running)
			stop();
		else
			start(text);
	}

	public void stop() {
		// TODO Auto-generated method stub

		reset();
		if (scriptRunner != null)
			while (scriptRunner.isAlive()) {

				Log.debug("Interrupting");
				ThreadUtil.wait(10);
				try {
					scriptRunner.interrupt();
					scriptRunner.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

	}

}
