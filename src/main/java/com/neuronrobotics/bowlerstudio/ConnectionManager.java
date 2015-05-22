package com.neuronrobotics.bowlerstudio;

import gnu.io.NRSerialPort;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.usb.UsbDevice;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbException;

import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

//import org.bytedeco.javacv.OpenCVFrameGrabber;

import com.neuronrobotics.addons.driving.HokuyoURGDevice;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngineWidget;
import com.neuronrobotics.bowlerstudio.tabs.CameraTab;
import com.neuronrobotics.jniloader.AbstractImageProvider;
import com.neuronrobotics.jniloader.JavaCVImageProvider;
import com.neuronrobotics.jniloader.OpenCVImageProvider;
import com.neuronrobotics.jniloader.StaticFileProvider;
import com.neuronrobotics.jniloader.URLImageProvider;
import com.neuronrobotics.nrconsole.plugin.DyIO.DyIOConsole;
import com.neuronrobotics.nrconsole.plugin.bootloader.core.NRBoot;
import com.neuronrobotics.nrconsole.plugin.bootloader.core.NRBootLoader;
import com.neuronrobotics.replicator.driver.BowlerBoardDevice;
import com.neuronrobotics.replicator.driver.NRPrinter;
import com.neuronrobotics.sdk.addons.gamepad.BowlerJInputDevice;
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
import com.neuronrobotics.sdk.pid.GenericPIDDevice;
import com.neuronrobotics.sdk.serial.SerialConnection;
import com.neuronrobotics.sdk.ui.AbstractConnectionPanel;
import com.neuronrobotics.sdk.ui.ConnectionDialog;
import com.neuronrobotics.sdk.util.ThreadUtil;
import com.neuronrobotics.sdk.wireless.bluetooth.BluetoothSerialConnection;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Tab;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ConnectionManager extends Tab implements EventHandler<ActionEvent> {

	private CheckBoxTreeItem<String> rootItem;
	private static final ArrayList<PluginManager> devices = new ArrayList<PluginManager>();
	private BowlerStudioController bowlerStudioController;

	private Node getIcon(String s) {
		return new ImageView(new Image(
				AbstractConnectionPanel.class.getResourceAsStream(s)));
	}

	public ConnectionManager(BowlerStudioController bowlerStudioController) {
		this.setBowlerStudioController(bowlerStudioController);
		setText("My Devices");

		rootItem = new CheckBoxTreeItem<String>("",
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

			addConnection(dyio, name);

		} else if (gen.hasNamespace("bcs.cartesian.*")) {
			BowlerBoardDevice delt = new BowlerBoardDevice();
			delt.setConnection(gen.getConnection());
			delt.connect();
			String name = "bowlerBoard";
			addConnection(delt, name);
			addConnection(new NRPrinter(delt), "cnc");
			
		} else if (gen.hasNamespace("bcs.pid.*")) {
			GenericPIDDevice delt = new GenericPIDDevice();
			delt.setConnection(gen.getConnection());
			delt.connect();
			String name = "pid";

			addConnection(delt, name);
		} else if (gen.hasNamespace("bcs.bootloader.*")
				|| gen.hasNamespace("neuronrobotics.bootloader.*")) {
			NRBootLoader delt = new NRBootLoader(gen.getConnection());
			String name = "bootloader";

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

	public void addConnection(BowlerAbstractDevice newDevice, String name) {
		int numOfThisDeviceType=0;
		for (int i = 0; i < devices.size(); i++) {
			if(newDevice.getClass().isInstance(devices.get(i).getDevice()))
				numOfThisDeviceType++;
		}
		if(numOfThisDeviceType>0)
			name = name+numOfThisDeviceType;
		
		newDevice.setScriptingName(name);
		PluginManager mp;
		Log.debug("Adding a "+newDevice.getClass().getName()+" with name "+name );
		mp = new PluginManager(newDevice, getBowlerStudioController());
		devices.add(mp);

		BowlerAbstractConnection con = newDevice.getConnection();
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
				+ newDevice.getAddress(), icon);

		mp.setTree(item);
		item.setExpanded(true);
		rootItem.getChildren().add(item);
		mp.setName(name);

		newDevice.addConnectionEventListener(
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
		
		item.setSelected(true);
		item.selectedProperty().addListener(b -> {
			if (!item.isSelected()) {
				System.out.println("Disconnecting " + mp.getName());
				newDevice.disconnect();
				devices.remove(mp);
				rootItem.getChildren().remove(item);
			}
		});
		getBowlerStudioController().setSelectedTab(this);
	}

	@Override
	public void handle(ActionEvent event) {
		// TODO Auto-generated method stub

	}

	public static ArrayList<PluginManager>  getConnections() {
		return devices;
	}

	public BowlerStudioController getBowlerStudioController() {
		return bowlerStudioController;
	}

	public void setBowlerStudioController(
			BowlerStudioController bowlerStudioController) {
		this.bowlerStudioController = bowlerStudioController;
	}
	
	public BowlerAbstractDevice pickConnectedDevice(Class class1) {
		if (devices.size() == 0)
			return null;
		List<String> choices = new ArrayList<>();
		for (int i = 0; i < devices.size(); i++) {
			if(class1==null)
				choices.add(devices.get(i).getName());
			else if(class1.isInstance(devices.get(i).getDevice())){
				choices.add(devices.get(i).getName());
			}
		}
		
		if(choices.size()>0){
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
		}
		return null;
	}


//	public BowlerAbstractDevice pickConnectedDevice() {
//
//		return pickConnectedDevice(null);
//	}

	public void disconnectAll() {
		for (int i = 0; i < devices.size(); i++) {
			devices.get(i).getDevice().disconnect();
		}

	}
	
	 public OpenCVImageProvider onConnectCVCamera() {
		List<String> choices = new ArrayList<>();
		choices.add("0");
		choices.add("1");
		choices.add("2");
		choices.add("3");
		choices.add("4");
		
		ChoiceDialog<String> dialog = new ChoiceDialog<>("0", choices);
		dialog.setTitle("OpenCV Camera Index Chooser");
		dialog.setHeaderText("Choose an OpenCV camera");
		dialog.setContentText("Camera Index:");

		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		
		// The Java 8 way to get the response value (with lambda expression).
		if (result !=null) {
			String letter = result.get();
			OpenCVImageProvider p = new OpenCVImageProvider(Integer.parseInt(letter));
			String name = "camera"+letter;
			addConnection(p,name);
			return p;
		}
		return null;
//		OpenCVImageProvider p = new OpenCVImageProvider(0);
//		String name = "camera0";
//		application.addConnection(p,name);
		
	}


	 public void onConnectJavaCVCamera() {
		 onConnectCVCamera();
//		List<String> choices = new ArrayList<>();
//		try {
//			String[] des = OpenCVFrameGrabber.getDeviceDescriptions();
//			if(des.length==0)
//				return;
//			for (String s: des){
//				choices.add(s);
//			}
//		} catch (org.bytedeco.javacv.FrameGrabber.Exception |UnsupportedOperationException e1) {
//			choices.add("0");
//			choices.add("1");
//			choices.add("2");
//			choices.add("3");
//			choices.add("4");
//		}
//		
//		ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
//		dialog.setTitle("JavaCV Camera Index Chooser");
//		dialog.setHeaderText("Choose an JavaCV camera");
//		dialog.setContentText("Camera Index:");
//
//		// Traditional way to get the response value.
//		Optional<String> result = dialog.showAndWait();
//		
//		// The Java 8 way to get the response value (with lambda expression).
//		result.ifPresent(letter -> {
//			JavaCVImageProvider p;
//			try {
//				p = new JavaCVImageProvider(Integer.parseInt(letter));
//				String name = "camera"+letter;
//				addConnection(p,name);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		});
//		

	}


	 public void onConnectFileSourceCamera() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Image File");
		File f = fileChooser.showOpenDialog(BowlerStudio.getPrimaryStage());
		if(f!=null){
			StaticFileProvider p = new StaticFileProvider(f);
			String name = "image";
			addConnection(p,name);
		}
	}


	public void onConnectURLSourceCamera() {
		TextInputDialog dialog = new TextInputDialog("http://upload.wikimedia.org/wikipedia/en/2/24/Lenna.png");
		dialog.setTitle("URL Image Source");
		dialog.setHeaderText("This url will be loaded each capture.");
		dialog.setContentText("URL ");

		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
			URLImageProvider p;
			try {
				p = new URLImageProvider(new URL(result.get()));
				String name = "url";
				addConnection(p,name);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}


	 public void onConnectHokuyoURG() {
		Set<String> ports = NRSerialPort.getAvailableSerialPorts();
		List<String> choices = new ArrayList<>();
		if(ports.size()==0)
			return;
		for (String s: ports){
			choices.add(s);
		}

		
		ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
		dialog.setTitle("LIDAR Serial Port Chooser");
		dialog.setHeaderText("Supports URG-04LX-UG01");
		dialog.setContentText("Lidar Port:");

		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		
		// The Java 8 way to get the response value (with lambda expression).
		result.ifPresent(letter -> {
			HokuyoURGDevice p = new HokuyoURGDevice(new NRSerialPort(letter, 115200));
			p.connect();
			String name = "lidar";
			addConnection(p,name);
		});
		
	}


	public void onConnectGamePad() {
		Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();
		
		List<String> choices = new ArrayList<>();
		if(ca.length==0)
			return;
		for (Controller s: ca){
			choices.add(s.getName());
		}

		
		ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
		dialog.setTitle("JInput Game Controller Select");
		dialog.setHeaderText("Connect a game controller");
		dialog.setContentText("Controller:");

		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		
		// The Java 8 way to get the response value (with lambda expression).
		result.ifPresent(letter -> {
			for(Controller s: ca){
				if(letter.contains(s.getName())){
					BowlerJInputDevice p =new BowlerJInputDevice(s);
					p.connect();
//					p.addListeners((comp, event1, value, eventString) -> {
//						System.out.println(eventString);
//					});
					String name = "gamepad";
					addConnection(p,name);
					return;
				}
			}

		});
		
	}


}
