package com.neuronrobotics.nrconsole.plugin.PID;

import java.util.ArrayList;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingNode;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.neuronrobotics.bowlerstudio.RpcCommandPanel;
import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.namespace.bcs.pid.IExtendedPIDControl;
import com.neuronrobotics.sdk.namespace.bcs.pid.IPidControlNamespace;

public class PIDControl extends AbstractBowlerStudioTab {

	private ArrayList<PIDControlWidget> widgits = new ArrayList<PIDControlWidget> ();
	private TreeView<String> tree;
	private DyIO dyio=null;
	private IPidControlNamespace pid=null;
	private boolean DyPID=false;



	@Override
	public String[] getMyNameSpaces() {
		// TODO Auto-generated method stub
		return new String []{"bcs.pid.*"};
	}

	@Override
	public void initializeUI(BowlerAbstractDevice pm) {
		
		CheckBoxTreeItem<String> rootItem = new CheckBoxTreeItem<String>("Closed-Loop Channels");
		rootItem.setExpanded(true);
		rootItem.setSelected(true);
		rootItem.selectedProperty().addListener(b -> {
			if (!rootItem.isSelected()) {
				stopAll();
			}
		});
		
		tree = new TreeView<String>(rootItem);

		tree.setCellFactory(CheckBoxTreeCell.forTreeView());
		
        setText("P.I.D. Closed-Loop");
		onTabReOpening();
		pid = (IPidControlNamespace)pm;
		setPidDevice(pid);
		
		final int [] initVals;
		try{
			initVals = getPidDevice().GetAllPIDPosition();
			
		}catch (Exception e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "DyIO Firmware is out of date", "DyPID ERROR", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		for(int i=0;i<initVals.length;i++) {
			final int index = i;

				// TODO Auto-generated method stub
				try{
					PIDControlWidget w = new PIDControlWidget(index, initVals[index], this);
					widgits.add(w);
					Platform.runLater(() -> {
						Stage dialog = new Stage();
						dialog.initStyle(StageStyle.UTILITY);
						dialog.setTitle("Channel: "+index);
						CheckBoxTreeItem<String> chan = new CheckBoxTreeItem<String>("Channel: "+index);
						
						SwingNode sn = new SwingNode();
				        sn.setContent(w);
						Scene scene = new Scene(new Group(sn));
						dialog.setScene(scene);
						dialog.setOnCloseRequest(event -> {
							chan.setSelected(false);
						});
						chan.selectedProperty().addListener(b ->{
							 if(chan.isSelected()){
								 dialog.show();
							 }else{
								 w.stopPID(true);
								 dialog.hide();
							 }
				        });
						rootItem.getChildren().add(chan);
					});
					
				}catch (Exception e){
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Failed to create a PID widget", "DyPID ERROR", JOptionPane.ERROR_MESSAGE);
					return;
				}
				//tabbedPane.addTab("PID "+index,widgits.get(index));

		}

		

		
		
		
		setContent(tree);
		
	}
	
	private void stopAll(){
		System.out.println("Stopping all PID");
		try{
			try{
				getPidDevice().killAllPidGroups();
			}catch (Exception ex){
				
			}
			
			for(PIDControlWidget w:widgits) {
				w.stopPID(false);
			}
		}catch (Exception ex){
			ex.printStackTrace();
			for(PIDControlWidget w:widgits) {
				w.stopPID(true);
			}
		}
	}
	
	@Override
	public void onTabClosing() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabReOpening() {
		// TODO Auto-generated method stub
		
	}
	
	private void setDyPID(boolean hadDyPID) {
		this.DyPID = hadDyPID;
	}
	
	public boolean isDyPID() {
		return DyPID;
	}
	
	public DyIO getDyio() {
		return dyio;
	}
	
	public void setPidDevice(IPidControlNamespace pid) {
		setDyPID(DyIO.class.isInstance(pid));
		if(isDyPID()){
			dyio = (DyIO)pid;
		}
		this.pid = pid;
	}
	
	public IPidControlNamespace getPidDevice() {
		return pid;
	}

}
