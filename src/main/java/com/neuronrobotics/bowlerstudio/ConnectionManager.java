package com.neuronrobotics.bowlerstudio;

import gnu.io.NRSerialPort;

import java.io.File;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.usb.UsbDisconnectedException;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

//import org.bytedeco.javacv.OpenCVFrameGrabber;

import net.java.games.input.Event;

import com.neuronrobotics.addons.driving.HokuyoURGDevice;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngineWidget;
import com.neuronrobotics.jniloader.OpenCVImageProvider;
import com.neuronrobotics.jniloader.StaticFileProvider;
import com.neuronrobotics.jniloader.URLImageProvider;
import com.neuronrobotics.replicator.driver.BowlerBoardDevice;
import com.neuronrobotics.replicator.driver.NRPrinter;
import com.neuronrobotics.sdk.addons.gamepad.BowlerJInputDevice;
import com.neuronrobotics.sdk.addons.gamepad.IJInputEventListener;
import com.neuronrobotics.sdk.bootloader.NRBootLoader;
import com.neuronrobotics.sdk.bowlercam.device.BowlerCamDevice;
import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.BowlerDatagram;
import com.neuronrobotics.sdk.common.DeviceManager;
import com.neuronrobotics.sdk.common.IConnectionEventListener;
import com.neuronrobotics.sdk.common.IDeviceAddedListener;
import com.neuronrobotics.sdk.common.InvalidConnectionException;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.common.NonBowlerDevice;
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

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Tab;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

public class ConnectionManager extends Tab implements IDeviceAddedListener ,EventHandler<ActionEvent> {

	private CheckBoxTreeItem<String> rootItem;
	private static final ArrayList<PluginManager> plugins = new ArrayList<PluginManager>();
	private BowlerStudioController bowlerStudioController;
	String formatStr="%1$-40s %2$-60s  %3$-40s";


	private Node getIcon(String s) {
		return new ImageView(new Image(
				AbstractConnectionPanel.class.getResourceAsStream(s)));
	}

	public ConnectionManager(BowlerStudioController bowlerStudioController) {
		this.setBowlerStudioController(bowlerStudioController);
		setText("My Devices");

		rootItem = new CheckBoxTreeItem<String>( String.format("  "+formatStr, "SCRIPTING NAME","DEVICE TYPE","MAC ADDRESS"),
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
		
		DeviceManager.addDeviceAddedListener(this);
		
//		new Thread(){
//			public void run(){
//				ThreadUtil.wait(5000);
//		        DeviceManager.addConnection(new StaticFileProvider(new File("/home/jonlee/my_photo-41.jpg")), "devName");
//
//			}
//		}.start();

//		UsbCDCSerialConnection
//				.addUsbDeviceEventListener(device -> new Thread() {
//					public void run() {
//						ThreadUtil.wait(750);
//						addConnection();
//					}
//				}.start());
		
		

	}



	public void addConnection(BowlerAbstractDevice newDevice, String name) {
		DeviceManager.addConnection(newDevice, name);

	}

	@Override
	public void handle(ActionEvent event) {
		// TODO Auto-generated method stub

	}

	public static ArrayList<PluginManager>  getPlugins() {
		return plugins;
	}

	public BowlerStudioController getBowlerStudioController() {
		return bowlerStudioController;
	}

	public void setBowlerStudioController(
			BowlerStudioController bowlerStudioController) {
		this.bowlerStudioController = bowlerStudioController;
	}
	
	public BowlerAbstractDevice pickConnectedDevice(Class class1) {
		List<String> choices = DeviceManager.listConnectedDevice(class1);
		
		if(choices.size()>0){
			ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0),
					choices);
			dialog.setTitle("Bowler Device Chooser");
			dialog.setHeaderText("Choose connected bowler device");
			dialog.setContentText("Device Name:");
	
			// Traditional way to get the response value.
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent()) {
				for (int i = 0; i < plugins.size(); i++) {
					if (plugins.get(i).getName().contains(result.get())) {
						return plugins.get(i).getDevice();
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

		//extract list int thread safe object
		Object [] pms= plugins.toArray();
		for (int i=0;i<pms.length;i++) {
			disconectAndRemoveDevice((PluginManager)pms[i]);
			ThreadUtil.wait(50);
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
					IJInputEventListener l=new IJInputEventListener() {
						@Override
						public void onEvent(Component comp, Event event1,
								float value, String eventString) {
									//System.out.println(comp.getName()+" is value= "+value);
								}
					};
					p.addListeners(l);
					String name = "gamepad";
					addConnection(p,name);
					return;
				}
			}

		});
		
	}

	@Override
	public void onNewDeviceAdded(BowlerAbstractDevice newDevice) {
		PluginManager mp;
		Log.debug("Adding a "+newDevice.getClass().getName()+" with name "+newDevice.getScriptingName() );
		mp = new PluginManager(newDevice, getBowlerStudioController());
		plugins.add(mp);

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
		String line = String.format(formatStr, 
				newDevice.getScriptingName(),
				newDevice.getClass().getSimpleName(),
				newDevice.getAddress());
		CheckBoxTreeItem<String> item = new CheckBoxTreeItem<String>(line, icon);
		

		mp.setTree(item);
		item.setExpanded(true);
		
		mp.setName(newDevice.getScriptingName());

		newDevice.addConnectionEventListener(
				new IConnectionEventListener() {
					@Override
					public void onDisconnect(BowlerAbstractConnection source) {
						// clean up after yourself...
						//disconectAndRemoveDevice(mp);
						
						for(int i=0;i<plugins.size();i++){
							PluginManager p=plugins.get(i);
							if(p.getDevice().getConnection()==source){
								Platform.runLater(() -> {
									plugins.remove(p);
									DeviceManager.remove(p.getDevice());
									refreshItemTree();
								});

								return;
							}
						}
						
					}

					// ignore
					@Override
					public void onConnect(BowlerAbstractConnection source) {
					}
				});
		
		item.setSelected(true);
		item.selectedProperty().addListener(b -> {
			if (!item.isSelected()) {
				disconectAndRemoveDevice(mp);
			}
		});
		refreshItemTree();
		if(	getBowlerStudioController()!=null)
		getBowlerStudioController().setSelectedTab(this);
	}
	//this is needed because if you just remove one they all disapear
	private void refreshItemTree(){
		rootItem.getChildren().clear();
		for(PluginManager p:plugins){
			rootItem.getChildren().add(p.getCheckBoxItem());
		}
	}
	
	private void disconectAndRemoveDevice(PluginManager mp){
		System.out.println("Disconnecting " + mp.getName());
		Log.warning("Disconnecting " + mp.getName());
		if(mp.getDevice().isAvailable() || NonBowlerDevice.class.isInstance(mp))
			mp.getDevice().disconnect();
		Platform.runLater(() -> {
			plugins.remove(mp);
			DeviceManager.remove(mp.getDevice());
			refreshItemTree();
		});
	}

	@Override
	public void onDeviceRemoved(BowlerAbstractDevice bad) {
		// TODO Auto-generated method stub

	}

	public void addConnection() {
		DeviceManager.addConnection();
	}

	public void onConnectPidSim() {
		// TODO Auto-generated method stub
		
	}


}
