package com.neuronrobotics.bowlerstudio.utils;

import java.net.InetAddress;

/**
 * Sample Skeleton for "BowlerConnectionMenue.fxml" Controller Class
 * You can copy and paste this code into your favorite IDE
 **/

import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.sdk.common.BowlerDatagram;
import com.neuronrobotics.sdk.common.DeviceManager;
import com.neuronrobotics.sdk.common.InvalidConnectionException;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.genericdevice.GenericDevice;
import com.neuronrobotics.sdk.network.BowlerTCPClient;
import com.neuronrobotics.sdk.network.UDPBowlerConnection;
import com.neuronrobotics.sdk.serial.SerialConnection;
import com.neuronrobotics.sdk.util.IProgressMonitorListener;
import com.neuronrobotics.sdk.util.ProcessMonitor;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class BowlerConnectionMenu extends Application {

	@FXML // ResourceBundle that was given to the FXMLLoader
	private ResourceBundle resources;

	@FXML // URL location of the FXML file that was given to the FXMLLoader
	private URL location;

	@FXML // fx:id="baudrate"
	private TextField baudrate; // Value injected by FXMLLoader

	@FXML // fx:id="connectNetwork"
	private Button connectNetwork; // Value injected by FXMLLoader

	@FXML // fx:id="connectSerial"
	private Button connectSerial; // Value injected by FXMLLoader

	@FXML // fx:id="portOptions"
	private ComboBox<String> portOptions; // Value injected by FXMLLoader
	@FXML
	private ComboBox<String> ipSelector;

	@FXML // fx:id="portType"
	private ToggleGroup portType; // Value injected by FXMLLoader

	@FXML // fx:id="searchNetwork"
	private Button searchNetwork; // Value injected by FXMLLoader

	@FXML // fx:id="searchSerial"
	private Button searchSerial; // Value injected by FXMLLoader

	@FXML // fx:id="tcpPort"
	private TextField tcpPort; // Value injected by FXMLLoader

	@FXML // fx:id="tcpSelect"
	private RadioButton tcpSelect; // Value injected by FXMLLoader

	@FXML // fx:id="udpPort"
	private TextField udpPort; // Value injected by FXMLLoader

	@FXML // fx:id="udpSelect"
	private RadioButton udpSelect; // Value injected by FXMLLoader

	private UDPBowlerConnection clnt;

	private int defaultPortNum = 1865;

	private Stage primaryStage;

	private String port;

	private int baud;

	@FXML // This method is called by the FXMLLoader when initialization is
			// complete
	void initialize() {
		assert baudrate != null : "fx:id=\"baudrate\" was not injected: check your FXML file 'BowlerConnectionMenue.fxml'.";
		assert connectNetwork != null : "fx:id=\"connectNetwork\" was not injected: check your FXML file 'BowlerConnectionMenue.fxml'.";
		assert connectSerial != null : "fx:id=\"connectSerial\" was not injected: check your FXML file 'BowlerConnectionMenue.fxml'.";
		assert portOptions != null : "fx:id=\"portOptions\" was not injected: check your FXML file 'BowlerConnectionMenue.fxml'.";
		assert portType != null : "fx:id=\"portType\" was not injected: check your FXML file 'BowlerConnectionMenue.fxml'.";
		assert searchNetwork != null : "fx:id=\"searchNetwork\" was not injected: check your FXML file 'BowlerConnectionMenue.fxml'.";
		assert searchSerial != null : "fx:id=\"searchSerial\" was not injected: check your FXML file 'BowlerConnectionMenue.fxml'.";
		assert tcpPort != null : "fx:id=\"tcpPort\" was not injected: check your FXML file 'BowlerConnectionMenue.fxml'.";
		assert tcpSelect != null : "fx:id=\"tcpSelect\" was not injected: check your FXML file 'BowlerConnectionMenue.fxml'.";
		assert udpPort != null : "fx:id=\"udpPort\" was not injected: check your FXML file 'BowlerConnectionMenue.fxml'.";
		assert udpSelect != null : "fx:id=\"udpSelect\" was not injected: check your FXML file 'BowlerConnectionMenue.fxml'.";
		assert ipSelector != null : "fx:id=\"ipSelector\" was not injected: check your FXML file 'BowlerConnectionMenu.fxml'.";
		runsearchSerial();
		runsearchNetwork();

		searchNetwork.setOnAction(event -> {
			runsearchNetwork();
		});
		searchSerial.setOnAction(event -> {
			runsearchSerial();
		});

		connectNetwork.setOnAction(event -> {
			runconnectNetwork();
			primaryStage.hide();
		});
		connectSerial.setOnAction(event -> {
			runconnectSerial();
			primaryStage.hide();
		});

	}

	private void runconnectSerial() {
		new Thread(() -> {
			for (int i = 0; i < 3; i++) {
				SerialConnection ser=null;
				try {
					BowlerDatagram.setUseBowlerV4(true);
					baud = Integer.parseInt(baudrate.getText());
					if (baud < 0) {
						throw new NumberFormatException();
					}
					port = portOptions.getSelectionModel().getSelectedItem().toString();
					int level = Log.getMinimumPrintLevel();
					Log.enableInfoPrint();
					 ser = new SerialConnection(port, baud);
					GenericDevice gen = new GenericDevice(ser);
					gen.connect();
					gen.ping();
					gen.getNamespaces();
					Log.setMinimumPrintLevel(level);
					gen.setConnection(null);
					gen.disconnect();
					DeviceManager.addConnection(ser);
					return;
				} catch (Exception e) {
					System.out.println("false start " + port + " at baud " + baud + " is not responding");
					BowlerStudioController.highlightException(null, e);
					if (ser!=null)
						ser.disconnect();
				}
			}
			System.out.println("Connection failed! " + port + " at baud " + baud + " is not responding");
		}).start();

	}

	private void runconnectNetwork() {
		new Thread(() -> {
			int port;
			String ip = ipSelector.getSelectionModel().getSelectedItem().toString();

			if (udpSelect.isSelected()) {
				port = Integer.parseInt(udpPort.getText());
				try {
					clnt = new UDPBowlerConnection(InetAddress.getByName(ip), port);
					DeviceManager.addConnection(clnt);
				} catch (Exception e) {
					System.out.println("Connection failed! " + ip + " at port " + ip + " is not responding");
					BowlerStudioController.highlightException(null, e);
					if (clnt != null)
						clnt.disconnect();
				}

			} else {
				port = Integer.parseInt(tcpPort.getText());
				BowlerTCPClient tcp = null;
				try {
					tcp = new BowlerTCPClient(ip, port);
					DeviceManager.addConnection(tcp);
				} catch (Exception e) {
					System.out.println("Connection failed! " + ip + " at port " + ip + " is not responding");
					BowlerStudioController.highlightException(null, e);
					if (tcp != null)
						tcp.disconnect();
				}
			}

		}).start();
	}

	private void runsearchSerial() {
		Platform.runLater(() -> {
			portOptions.getItems().clear();
			new Thread(() -> {

				for (String s : SerialConnection.getAvailableSerialPorts()) {
					Platform.runLater(() -> portOptions.getItems().add(s));
				}
			}).start();
		});

	}

	private void runsearchNetwork() {
		Platform.runLater(() -> {
			ipSelector.getItems().clear();
			Platform.runLater(() -> ipSelector.getItems().add("127.0.0.1"));
			new Thread(() -> {
				// System.out.println("Searching for UDP devices, please
				// wait...");
				int prt;
				try {
					prt = new Integer(udpPort.getText());
				} catch (NumberFormatException e) {
					prt = defaultPortNum;
					Platform.runLater(() -> udpPort.setText(new Integer(defaultPortNum).toString()));
				}
				clnt = new UDPBowlerConnection(prt);
				ArrayList<InetAddress> addrs = clnt.getAllAddresses();

				for (InetAddress i : addrs) {
					Platform.runLater(() -> ipSelector.getItems().add(i.getHostAddress()));
				}

			}).start();
		});
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		FXMLLoader loader = AssetFactory.loadLayout("layout/BowlerConnectionMenu.fxml", true);
		Parent root;
		loader.setController(this);
		root = loader.load();

		Platform.runLater(() -> {
			primaryStage.setTitle("Bowler Device Connection");

			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.initModality(Modality.WINDOW_MODAL);
			primaryStage.setResizable(true);
			primaryStage.show();

		});
	}

}
