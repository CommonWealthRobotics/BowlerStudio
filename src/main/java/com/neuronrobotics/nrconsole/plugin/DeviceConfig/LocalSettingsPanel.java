package com.neuronrobotics.nrconsole.plugin.DeviceConfig;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javafx.stage.FileChooser.ExtensionFilter;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;

import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.nrconsole.util.PrefsLoader;
import com.neuronrobotics.nrconsole.util.Slic3rFilter;

public class LocalSettingsPanel extends SettingsPanel {
	private JTextField tfSlic3rLocation;
	private JButton btnChangeSlicr;
	private JLabel lblCurrentLocationOf;
	String slic3rPath;
	PrefsLoader prefs = new PrefsLoader();
	private JButton btnLoadDefaults;
	/**
	 * Create the panel.
	 */
	public LocalSettingsPanel() {
		initComponents();
	}

	
	
	
	@Override
	public String getPanelName() {
		return "Local Settings";
	}

	@Override
	public void initComponents() {
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addComponent(getTfSlic3rLocation(), GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getBtnChangeSlicr()))
						.addComponent(getBtnLoadDefaults(), Alignment.TRAILING)
						.addComponent(getLblCurrentLocationOf()))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(getLblCurrentLocationOf())
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(getTfSlic3rLocation(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(getBtnChangeSlicr()))
					.addPreferredGap(ComponentPlacement.RELATED, 212, Short.MAX_VALUE)
					.addComponent(getBtnLoadDefaults())
					.addContainerGap())
		);
		setLayout(groupLayout);
		getTfSlic3rLocation().setText(prefs.getSlic3rLocation());
		
	}
	public void reloadAllSettings(){
		getTfSlic3rLocation().setText(prefs.getSlic3rLocation());
	}
	private void changeSlic3rLocation(){
		slic3rPath = FileSelectionFactory.GetFile(null, 	new ExtensionFilter("Slicer Executable","*")).getPath();
		prefs.setSlic3rLocation(slic3rPath);
		getTfSlic3rLocation().setText(prefs.getSlic3rLocation());
	}
	private JTextField getTfSlic3rLocation() {
		if (tfSlic3rLocation == null) {
			tfSlic3rLocation = new JTextField();
			tfSlic3rLocation.setHorizontalAlignment(SwingConstants.LEFT);
			tfSlic3rLocation.setEditable(false);
			tfSlic3rLocation.setColumns(10);
		}
		return tfSlic3rLocation;
	}
	private JButton getBtnChangeSlicr() {
		if (btnChangeSlicr == null) {
			btnChangeSlicr = new JButton("Change Slic3r Location");
			btnChangeSlicr.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					changeSlic3rLocation();
				}
			});
		}
		return btnChangeSlicr;
	}
	private JLabel getLblCurrentLocationOf() {
		if (lblCurrentLocationOf == null) {
			lblCurrentLocationOf = new JLabel("Current Location of slic3r-console.exe:");
		}
		return lblCurrentLocationOf;
	}
	private JButton getBtnLoadDefaults() {
		if (btnLoadDefaults == null) {
			btnLoadDefaults = new JButton("Load Defaults");
			btnLoadDefaults.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					
					java.awt.EventQueue.invokeLater(new Runnable() {
						public void run() {
							if (JOptionPane.showConfirmDialog(null, "This operation could be dangerous and break things.\n"
									+ "You should restart NrConsole immediately after performing this action to avoid problems.", "This could be risky...", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){
								prefs.loadDefaults();
							}
							
						}
					});
					reloadAllSettings();
				}
			});
		}
		return btnLoadDefaults;
	}
}
