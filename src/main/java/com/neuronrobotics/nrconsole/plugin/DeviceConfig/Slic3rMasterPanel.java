package com.neuronrobotics.nrconsole.plugin.DeviceConfig;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import com.neuronrobotics.nrconsole.util.PrefsLoader;

public class Slic3rMasterPanel extends SettingsPanel {
	private JScrollPane scrollPane;
	private JPanel panel;
	private JRadioButton rdbtnShowAllSettings;
	private JPanel panel_1;
	PrefsLoader prefs = new PrefsLoader();
	private Slic3rAll pnlAll = new Slic3rAll(this);
	private Slic3rPrinter pnlPrinter = new Slic3rPrinter(this);
	private Slic3rPrints pnlPrints = new Slic3rPrints(this);
	private JRadioButton rdbtnShowOnlyPrinter;
	private JRadioButton rdbtnShowOnlyPrint;
	private final ButtonGroup buttonGroup = new ButtonGroup();

	/**
	 * Create the panel.
	 */
	public Slic3rMasterPanel() {
		
		initComponents();
		

	}
	
	
	
	
	private SettingsPanel whichPanel(){
		if (rdbtnShowAllSettings.isSelected()){			
			removeListeners();
			addListener(pnlAll);
			notifySettingsChanged();
			return pnlAll;
		}
		else if (rdbtnShowOnlyPrinter.isSelected()){		
			removeListeners();
			addListener(pnlPrinter);
			notifySettingsChanged();
			return pnlPrinter;
		}
		else if(rdbtnShowOnlyPrint.isSelected()){			
			removeListeners();
			addListener(pnlPrints);
			notifySettingsChanged();
			return pnlPrints;
		}		
		removeListeners();
		addListener(pnlAll);
		notifySettingsChanged();
		return pnlAll;
		
		
	}
	private void changePanels(){
		scrollPane.setViewportView(whichPanel());
	}
	
	
	private JScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new JScrollPane();
			scrollPane.setColumnHeaderView(getPanel());
			
		}
		return scrollPane;
	}
	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.add(getRdbtnShowOnlyPrinter());
			panel.add(getRdbtnShowOnlyPrint());
			panel.add(getRdbtnShowAllSettings());
		}
		return panel;
	}
	private JRadioButton getRdbtnShowAllSettings() {
		if (rdbtnShowAllSettings == null) {
			rdbtnShowAllSettings = new JRadioButton("Show All Settings");
			rdbtnShowAllSettings.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (rdbtnShowAllSettings.isSelected()){
						prefs.setSlic3rRDBTNLast(0);
					}
					changePanels();
				}
			});
			
			buttonGroup.add(rdbtnShowAllSettings);
		}
		return rdbtnShowAllSettings;
	}
	
		
	@Override
	public String getPanelName() {
		
		return "Slic3r Settings";
	}

	@Override
	public void initComponents() {
		setLayout(new MigLayout("", "[grow]", "[grow]"));
		add(getScrollPane(), "cell 0 0,grow");
		switch (prefs.getSlic3rRDBTNLast()) {
		case 0:
			getRdbtnShowAllSettings().setSelected(true);
			break;
		case 1:
			getRdbtnShowOnlyPrinter().setSelected(true);
			break;
		case 2:
			getRdbtnShowOnlyPrint().setSelected(true);
			break;
		default:
			getRdbtnShowAllSettings().setSelected(true);
			break;
		}
		changePanels();
		
	}
	private JRadioButton getRdbtnShowOnlyPrinter() {
		if (rdbtnShowOnlyPrinter == null) {
			rdbtnShowOnlyPrinter = new JRadioButton("Show Only Printer Settings");
			rdbtnShowOnlyPrinter.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if (rdbtnShowOnlyPrinter.isSelected()){
						prefs.setSlic3rRDBTNLast(1);
					}
					changePanels();
				}
			});
			
			buttonGroup.add(rdbtnShowOnlyPrinter);
			
		}
		return rdbtnShowOnlyPrinter;
	}
	
	private JRadioButton getRdbtnShowOnlyPrint() {
		if (rdbtnShowOnlyPrint == null) {
			rdbtnShowOnlyPrint = new JRadioButton("Show Only Print Job Settings");
			rdbtnShowOnlyPrint.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (rdbtnShowOnlyPrint.isSelected()){
						prefs.setSlic3rRDBTNLast(2);
					}
					changePanels();
				}
			});
			
			buttonGroup.add(rdbtnShowOnlyPrint);
		}
		return rdbtnShowOnlyPrint;
	}
}
