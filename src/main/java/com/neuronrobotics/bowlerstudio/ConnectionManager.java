package com.neuronrobotics.bowlerstudio;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngineWidget;
import com.neuronrobotics.bowlerstudio.tabs.CameraTab;
import com.neuronrobotics.jniloader.OpenCVImageProvider;
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
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ConnectionManager extends Tab implements EventHandler<ActionEvent>, ChangeListener<TreeItem<String>>  {
	
	private TreeItem<String> rootItem;
	private ArrayList<PluginManager> devices = new ArrayList<PluginManager>();
	private BowlerStudioController bowlerStudioController;
	private Node getIcon(String s){
		return new ImageView(
        		new Image(
        				AbstractConnectionPanel.class.getResourceAsStream(s)
        				)
        		);
	}

	public ConnectionManager(BowlerStudioController bowlerStudioController){
		this.setBowlerStudioController(bowlerStudioController);
		setText("Connections");
		
        rootItem = new TreeItem<String> ("Connections", getIcon(
        		"images/connection-icon.png"
        		//"images/usb-icon.png"
        		));
        rootItem.setExpanded(true);
       
        TreeView<String> tree = new TreeView<String> (rootItem); 
        
        setContent(tree);
        tree.getSelectionModel().selectedItemProperty().addListener(this); 
        new Thread(){
        	public void run(){
        		addConnection();
        	}
        }.start();
        ScriptingEngineWidget.setConnectionmanager(this);
	}

	
	
	public void addConnection(){
		BowlerAbstractConnection connection = ConnectionDialog.promptConnection();
		if(connection == null) {
			return;
		}
		Log.error("Switching to v4 parser");
		BowlerDatagram.setUseBowlerV4(true);
		
		GenericDevice gen = new GenericDevice(connection);
		try{
			if(!gen.connect()) {
				throw new InvalidConnectionException("Connection is invalid");
			}
			if(!gen.ping(true)){
				throw new InvalidConnectionException("Communication failed");
			}
		} catch(Exception e) {
			//connection.disconnect();
			ThreadUtil.wait(1000);
			BowlerDatagram.setUseBowlerV4(false);
			if(!gen.connect()) {
				throw new InvalidConnectionException("Connection is invalid");
			}
			if(!gen.ping()){
				connection = null;
				throw new InvalidConnectionException("Communication failed");
			}
			throw e;
		}
		if(gen.hasNamespace("neuronrobotics.dyio.*")){
			DyIO dyio = new DyIO(gen.getConnection());
			dyio.connect();
			String name = "dyio";
			if(rootItem.getChildren().size()>0)
				name+=rootItem.getChildren().size()+1;
			addConnection(dyio,name);
		}else{
			addConnection(gen,"device");
		}
	}
	
	public void addConnection(BowlerAbstractDevice c, String name){
		PluginManager mp;
		
		mp= new PluginManager(c,getBowlerStudioController());
		devices.add(mp);

		BowlerAbstractConnection con =  c.getConnection();
		Node icon = getIcon(
        		"images/connection-icon.png"
				//"images/usb-icon.png"
        		);
		if(	
			SerialConnection.class.isInstance(con)	){
			icon = getIcon(
	        		//"images/ethernet-icon.png"
					"images/usb-icon.png"
	        		);
		}else if(	UsbCDCSerialConnection.class.isInstance(con)
				){
				icon = getIcon(
		        		//"images/ethernet-icon.png"
						"images/usb-icon.png"
		        		);
		}else if(BluetoothSerialConnection.class.isInstance(con)){
			icon = getIcon(
	        		//"images/ethernet-icon.png"
					"images/bluetooth-icon.png"
	        		);
		}else if(	UDPBowlerConnection.class.isInstance(con)||
				BowlerTCPClient.class.isInstance(con)	){
				icon = getIcon(
		        		//"images/ethernet-icon.png"
						"images/ethernet-icon.png"
		        		);
			}
		
		
		TreeItem<String> item = new TreeItem<String> (name+" "+c.getAddress(), icon); 
		mp.setTree(item);
		item.setExpanded(false);
        rootItem.getChildren().add(item);
        mp.setName(name);
        if(c.getConnection()!=null){
	        c.getConnection().addConnectionEventListener( new IConnectionEventListener() {
				@Override
				public void onDisconnect(BowlerAbstractConnection source) {
					// clean up after yourself...
					devices.remove(mp);
					rootItem.getChildren().remove(item);
				}
				//ignore
				@Override public void onConnect(BowlerAbstractConnection source) {}
			});
        }
	}

	@Override
	public void changed(ObservableValue<? extends TreeItem<String>> observable,
			TreeItem<String> oldValue, TreeItem<String> newValue) {
		// TODO Auto-generated method stub
		
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

	public void setBowlerStudioController(BowlerStudioController bowlerStudioController) {
		this.bowlerStudioController = bowlerStudioController;
	}
	
	public BowlerAbstractDevice pickConnectedDevice(){
		if(devices.size()==0)
			return null;
		List<String> choices = new ArrayList<>();
		for(int i=0;i<devices.size();i++){
			choices.add(devices.get(i).getName());
		}
		
		ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
		dialog.setTitle("Bowler Device Chooser");
		dialog.setHeaderText("Choose connected bowler device");
		dialog.setContentText("Device Name:");

		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
			for(int i=0;i<devices.size();i++){
				if(devices.get(i).getName().contains(result.get())){
					return devices.get(i).getDevice();
				}
			}
		}
		return null;
		
	}

	public void disconnectAll() {
		for(int i=0;i<devices.size();i++){
			devices.get(i).getDevice().disconnect();
		}
		
	}
	
	
}
