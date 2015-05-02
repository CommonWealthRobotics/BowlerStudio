package com.neuronrobotics.bowlerstudio;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.usb.UsbDevice;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbException;

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngineWidget;
import com.neuronrobotics.bowlerstudio.tabs.CameraTab;
import com.neuronrobotics.jniloader.OpenCVImageProvider;
import com.neuronrobotics.nrconsole.plugin.DyIO.DyIOConsole;
import com.neuronrobotics.nrconsole.plugin.bootloader.core.NRBoot;
import com.neuronrobotics.nrconsole.plugin.bootloader.core.NRBootLoader;
import com.neuronrobotics.replicator.driver.BowlerBoardDevice;
import com.neuronrobotics.sdk.bowlercam.device.BowlerCamDevice;
import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.BowlerDatagram;
import com.neuronrobotics.sdk.common.IConnectionEventListener;
import com.neuronrobotics.sdk.common.InvalidConnectionException;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.genericdevice.GenericDevice;
import com.neuronrobotics.sdk.javaxusb.UsbCDCSerialConnection;
import com.neuronrobotics.sdk.network.BowlerTCPClient;
import com.neuronrobotics.sdk.network.UDPBowlerConnection;
import com.neuronrobotics.sdk.serial.SerialConnection;
import com.neuronrobotics.sdk.ui.AbstractConnectionPanel;
import com.neuronrobotics.sdk.ui.ConnectionDialog;
import com.neuronrobotics.sdk.util.ThreadUtil;
import com.neuronrobotics.sdk.wireless.bluetooth.BluetoothSerialConnection;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ConnectionManager extends Tab implements EventHandler<ActionEvent> {

	private CheckBoxTreeItem<String> rootItem;
	private ArrayList<PluginManager> devices = new ArrayList<PluginManager>();
	private BowlerStudioController bowlerStudioController;

	private Node getIcon(String s) {
		return new ImageView(new Image(
				AbstractConnectionPanel.class.getResourceAsStream(s)));
	}

	public ConnectionManager(BowlerStudioController bowlerStudioController) {
		this.setBowlerStudioController(bowlerStudioController);
		setText("Connections");

		rootItem = new CheckBoxTreeItem<String>("Connections",
				getIcon("images/connection-icon.png"
				// "images/usb-icon.png"
				));
		rootItem.setExpanded(true);
		rootItem.setSelected(true);
		rootItem.selectedProperty().addListener(b -> {
			if (!rootItem.isSelected()) {
				disconnectAll();
			}
		});

		TreeView<String> tree = new TreeView<String>(rootItem);

		tree.setCellFactory(CheckBoxTreeCell.forTreeView());

		setContent(tree);

		ScriptingEngineWidget.setConnectionmanager(this);
		try {
			List<String> devs = SerialConnection.getAvailableSerialPorts();
			if (devs.size() == 0) {
				return;
			} else {
				new Thread() {
					public void run() {
						ThreadUtil.wait(750);
						addConnection();
//						for (String d : devs) {
//							if(d.contains("DyIO") || d.contains("Bootloader")||d.contains("COM"))
//								addConnection(new SerialConnection(d));
//						}
					}
				}.start();

			}
		} catch (Error 
				| UsbDisconnectedException | SecurityException  e) {
			e.printStackTrace();
		}
//		UsbCDCSerialConnection
//				.addUsbDeviceEventListener(device -> new Thread() {
//					public void run() {
//						ThreadUtil.wait(750);
//						addConnection();
//					}
//				}.start());

	}

	public void addConnection(BowlerAbstractConnection connection) {
		if (connection == null) {
			return;
		}
		Log.error("Switching to v4 parser");

		GenericDevice gen = new GenericDevice(connection);
		try {
			if (!gen.connect()) {
				throw new InvalidConnectionException("Connection is invalid");
			}
			if (!gen.ping(true)) {
				throw new InvalidConnectionException("Communication failed");
			}
		} catch (Exception e) {
			// connection.disconnect();
			ThreadUtil.wait(1000);
			BowlerDatagram.setUseBowlerV4(false);
			if (!gen.connect()) {
				throw new InvalidConnectionException("Connection is invalid");
			}
			if (!gen.ping()) {
				connection = null;
				throw new InvalidConnectionException("Communication failed");
			}
			throw e;
		}
		if (gen.hasNamespace("neuronrobotics.dyio.*")) {
			DyIO dyio = new DyIO(gen.getConnection());
			dyio.connect();
			String name = "dyio";
			if (rootItem.getChildren().size() > 0)
				name += rootItem.getChildren().size() + 1;
			addConnection(dyio, name);

		} else if (gen.hasNamespace("bcs.cartesian.*")) {
			BowlerBoardDevice delt = new BowlerBoardDevice();
			delt.setConnection(gen.getConnection());
			delt.connect();
			String name = "bowlerBoard";
			if (rootItem.getChildren().size() > 0)
				name += rootItem.getChildren().size() + 1;
			
			addConnection(delt, name);
		} else if (gen.hasNamespace("bcs.bootloader.*")
				|| gen.hasNamespace("neuronrobotics.bootloader.*")) {
			NRBootLoader delt = new NRBootLoader(gen.getConnection());
			String name = "bootloader";
			if (rootItem.getChildren().size() > 0)
				name += rootItem.getChildren().size() + 1;
			addConnection(delt, name);
		} else if (gen.hasNamespace("neuronrobotics.bowlercam.*")) {
			BowlerCamDevice delt = new BowlerCamDevice();
			delt.setConnection(gen.getConnection());
			delt.connect();
			String name = "bowlercam";
			if (rootItem.getChildren().size() > 0)
				name += rootItem.getChildren().size() + 1;
			addConnection(delt, name);
		} else {
			addConnection(gen, "device");
		}
	}

	public void addConnection() {
		new Thread() {
			public void run() {
				BowlerDatagram.setUseBowlerV4(true);
				addConnection(ConnectionDialog.promptConnection());
			}
		}.start();
	}

	public void addConnection(BowlerAbstractDevice c, String name) {
		c.setScriptingName(name);
		PluginManager mp;
		Log.debug("Adding a "+c.getClass().getName()+" with name "+name );
		mp = new PluginManager(c, getBowlerStudioController());
		devices.add(mp);

		BowlerAbstractConnection con = c.getConnection();
		Node icon = getIcon("images/connection-icon.png"
		// "images/usb-icon.png"
		);
		if (SerialConnection.class.isInstance(con)) {
			icon = getIcon(
			// "images/ethernet-icon.png"
			"images/usb-icon.png");
		} else if (UsbCDCSerialConnection.class.isInstance(con)) {
			icon = getIcon(
			// "images/ethernet-icon.png"
			"images/usb-icon.png");
		} else if (BluetoothSerialConnection.class.isInstance(con)) {
			icon = getIcon(
			// "images/ethernet-icon.png"
			"images/bluetooth-icon.png");
		} else if (UDPBowlerConnection.class.isInstance(con)
				|| BowlerTCPClient.class.isInstance(con)) {
			icon = getIcon(
			// "images/ethernet-icon.png"
			"images/ethernet-icon.png");
		}

		CheckBoxTreeItem<String> item = new CheckBoxTreeItem<String>(name + " "
				+ c.getAddress(), icon);

		mp.setTree(item);
		item.setExpanded(false);
		rootItem.getChildren().add(item);
		mp.setName(name);
		if (c.getConnection() != null) {
			c.getConnection().addConnectionEventListener(
					new IConnectionEventListener() {
						@Override
						public void onDisconnect(BowlerAbstractConnection source) {
							// clean up after yourself...
							devices.remove(mp);
							rootItem.getChildren().remove(item);
						}

						// ignore
						@Override
						public void onConnect(BowlerAbstractConnection source) {
						}
					});
		}
		item.setSelected(true);
		item.selectedProperty().addListener(b -> {
			if (!item.isSelected()) {
				System.out.println("Disconnecting " + mp.getName());
				c.disconnect();
				devices.remove(mp);
				rootItem.getChildren().remove(item);
			}
		});
	}

	@Override
	public void handle(ActionEvent event) {
		// TODO Auto-generated method stub

	}

	public ArrayList<PluginManager> getConnections() {
		return devices;
	}

	public BowlerStudioController getBowlerStudioController() {
		return bowlerStudioController;
	}

	public void setBowlerStudioController(
			BowlerStudioController bowlerStudioController) {
		this.bowlerStudioController = bowlerStudioController;
	}

	public BowlerAbstractDevice pickConnectedDevice() {
		if (devices.size() == 0)
			return null;
		List<String> choices = new ArrayList<>();
		for (int i = 0; i < devices.size(); i++) {
			choices.add(devices.get(i).getName());
		}

		ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0),
				choices);
		dialog.setTitle("Bowler Device Chooser");
		dialog.setHeaderText("Choose connected bowler device");
		dialog.setContentText("Device Name:");

		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()) {
			for (int i = 0; i < devices.size(); i++) {
				if (devices.get(i).getName().contains(result.get())) {
					return devices.get(i).getDevice();
				}
			}
		}
		return null;

	}

	public void disconnectAll() {
		for (int i = 0; i < devices.size(); i++) {
			devices.get(i).getDevice().disconnect();
		}

	}

}
