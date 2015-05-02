package com.neuronrobotics.nrconsole.plugin.PID;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.miginfocom.swing.MigLayout;

import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.DyIORegestry;
import com.neuronrobotics.sdk.namespace.bcs.pid.IPidControlNamespace;

public class PIDControlGui extends JPanel {
	private static final long serialVersionUID = 1L;
	//private DyIO dyio=null;
	private IPidControlNamespace pid=null;
	private boolean DyPID=false;
	
	private ArrayList<PIDControlWidget> widgits = new ArrayList<PIDControlWidget> ();
	private JTabbedPane tabbedPane;
	private JButton stopAll = new JButton("Stop All PID");

	
	
	public PIDControlGui() {
		Log.info("Connecteing DyPID panel");
		setPidDevice(DyIORegestry.get());
		setDyPID(true);
		//dyio.addDyIOEventListener(this);
		init();
	}
	
	public PIDControlGui(IPidControlNamespace d) {
		Log.info("Connecteing PID panel");
		setPidDevice(d);
		setDyPID(false);
		init();
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
	private void init() {
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		setName("P.I.D.");
		Log.info("Begining PID Control Gui");
		setLayout(new MigLayout());
		
		int [] initVals = {};
		try{
			initVals = getPidDevice().GetAllPIDPosition();
			
		}catch (Exception e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "DyIO Firmware is out of date", "DyPID ERROR", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		for(int i=0;i<initVals.length;i++) {
			try{
				widgits.add(new PIDControlWidget(i, initVals[i], this));
			}catch (Exception e){
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Failed to create a PID widget", "DyPID ERROR", JOptionPane.ERROR_MESSAGE);
				return;
			}
			tabbedPane.addTab("PID "+i,widgits.get(i));
			//groupSelector.addItem(widgits.get(i));
		}

		stopAll.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				stopAll();
			}
		});
		JPanel groups = new JPanel(new MigLayout());
		groups.add(stopAll,"wrap");
		add(groups,"wrap");
		add(tabbedPane,"wrap");
		Log.info("Started PID Control Gui");
		//stopAll();
		setMinimumSize(new Dimension(1095,1300));
	}
	
	private void setDyPID(boolean hadDyPID) {
		this.DyPID = hadDyPID;
	}
	
	public boolean isDyPID() {
		return DyPID;
	}
	
	public DyIO getDyio() {
		return DyIORegestry.get();
	}
	
	public void setPidDevice(IPidControlNamespace pid) {
		this.pid = pid;
	}
	
	public IPidControlNamespace getPidDevice() {
		return pid;
	}


}
