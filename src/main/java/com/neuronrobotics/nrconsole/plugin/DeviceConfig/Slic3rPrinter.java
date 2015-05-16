package com.neuronrobotics.nrconsole.plugin.DeviceConfig;

import java.text.NumberFormat;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;

public class Slic3rPrinter extends SettingsPanel implements SettingsChangeListener{
	private JLabel lblNozzleDiameter;
	private JLabel lblPrintCenter;
	private JLabel lblFilamentDiameter;
	private JLabel lblExtrusionMultiplier;
	private JLabel lblPrintTemperature;
	private JLabel lblBedTemperature;
	private JFormattedTextField tfNozzleDia;
	private JLabel lblX;
	private JFormattedTextField tfPrintCenterX;
	private JLabel lblY;
	private JFormattedTextField tfPrintCenterY;
	private JFormattedTextField tfFilaDia;
	private JFormattedTextField tfExtrusionMult;
	private JFormattedTextField tfPTemp;
	private JFormattedTextField tfBTemp;
	private JLabel lblMm;
	private JLabel lblMm_1;
	private JLabel lblMm_2;
	private JLabel lblMm_3;
	private JLabel lblc;
	private JLabel lblc_1;
	private Slic3rMasterPanel master;
	public Slic3rPrinter(Slic3rMasterPanel _master) {	
		master = _master;
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				initComponents();
				
			}
		});

	}

	
	@Override
	public String getPanelName() {
		return "Slic3r Settings";
	}


	@Override
	public void settingsChanged() {
		
		if (master.numSettings() > 22){
		tfNozzleDia.setValue(master.getSetting(0).getValue());
		tfPrintCenterX.setValue(master.getSetting(1).getValue());
		tfPrintCenterY.setValue(master.getSetting(2).getValue());
		tfFilaDia.setValue(master.getSetting(3).getValue());
		tfExtrusionMult.setValue(master.getSetting(4).getValue());
		tfPTemp.setValue(master.getSetting(5).getValue());
		tfBTemp.setValue(master.getSetting(6).getValue());
		
		//TODO Use Support Material?
		
		}
		
	}


	@Override
	public void initComponents() {

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(getLblNozzleDiameter())
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getTfNozzleDia(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getLblMm()))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(getLblPrintCenter())
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getLblX())
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getTfPrintCenterX(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getLblMm_1())
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getLblY())
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getTfPrintCenterY(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getLblMm_2()))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(getLblFilamentDiameter())
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getTfFilaDia(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getLblMm_3()))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(getLblExtrusionMultiplier())
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getTfExtrusionMult(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(getLblPrintTemperature())
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getTfPTemp(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getLblc()))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(getLblBedTemperature())
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getTfBTemp(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getLblc_1())))
					.addContainerGap(225, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(getLblNozzleDiameter())
						.addComponent(getTfNozzleDia(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(getLblMm()))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(getLblPrintCenter())
						.addComponent(getLblX())
						.addComponent(getTfPrintCenterX(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(getLblY())
						.addComponent(getTfPrintCenterY(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(getLblMm_1())
						.addComponent(getLblMm_2()))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(getLblFilamentDiameter())
						.addComponent(getTfFilaDia(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(getLblMm_3()))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(getLblExtrusionMultiplier())
						.addComponent(getTfExtrusionMult(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(getLblPrintTemperature())
						.addComponent(getTfPTemp(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(getLblc()))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(getLblBedTemperature())
						.addComponent(getTfBTemp(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(getLblc_1()))
					.addContainerGap(25, Short.MAX_VALUE))
		);
		setLayout(groupLayout);
		this.addListener(this);
		
	}

	
	
	
	public NumberFormat getNumFormat(int numInts, int numDec){
		NumberFormat x = NumberFormat.getNumberInstance();
		x.setMaximumFractionDigits(numDec);
		x.setMinimumFractionDigits(numDec);
		x.setMaximumIntegerDigits(numInts);
		x.setMinimumIntegerDigits(numInts);
		return x;
	}

	private JLabel getLblNozzleDiameter() {
		if (lblNozzleDiameter == null) {
			lblNozzleDiameter = new JLabel("Nozzle Diameter");
		}
		return lblNozzleDiameter;
	}
	private JLabel getLblPrintCenter() {
		if (lblPrintCenter == null) {
			lblPrintCenter = new JLabel("Print Center");
		}
		return lblPrintCenter;
	}
	private JLabel getLblFilamentDiameter() {
		if (lblFilamentDiameter == null) {
			lblFilamentDiameter = new JLabel("Filament Diameter");
		}
		return lblFilamentDiameter;
	}
	private JLabel getLblExtrusionMultiplier() {
		if (lblExtrusionMultiplier == null) {
			lblExtrusionMultiplier = new JLabel("Extrusion Multiplier");
		}
		return lblExtrusionMultiplier;
	}
	private JLabel getLblPrintTemperature() {
		if (lblPrintTemperature == null) {
			lblPrintTemperature = new JLabel("Print Temperature");
		}
		return lblPrintTemperature;
	}
	private JLabel getLblBedTemperature() {
		if (lblBedTemperature == null) {
			lblBedTemperature = new JLabel("Bed Temperature");
		}
		return lblBedTemperature;
	}
	private JLabel getLblX() {
		if (lblX == null) {
			lblX = new JLabel("X:");
		}
		return lblX;
	}
	private JLabel getLblY() {
		if (lblY == null) {
			lblY = new JLabel("Y:");
		}
		return lblY;
	}
	
	
	
	
	private JFormattedTextField getTfNozzleDia() {
		if (tfNozzleDia == null) {			
			tfNozzleDia = new JFormattedTextField(getNumFormat(2,3));
			tfNozzleDia.setColumns(5);
		}
		return tfNozzleDia;
	}
	
	private JFormattedTextField getTfPrintCenterX() {
		if (tfPrintCenterX == null) {
			
			tfPrintCenterX = new JFormattedTextField(getNumFormat(3,0));
			tfPrintCenterX.setColumns(5);
		
		}
		return tfPrintCenterX;
	}
	
	private JFormattedTextField getTfPrintCenterY() {
		if (tfPrintCenterY == null) {
			
				tfPrintCenterY = new JFormattedTextField(getNumFormat(3,0));
				tfPrintCenterY.setColumns(5);
			
		}
		return tfPrintCenterY;
	}
	private JFormattedTextField getTfFilaDia() {
		if (tfFilaDia == null) {
			
				tfFilaDia = new JFormattedTextField(getNumFormat(2,2));
				tfFilaDia.setColumns(5);
		
		}
		return tfFilaDia;
	}
	private JFormattedTextField getTfExtrusionMult() {
		if (tfExtrusionMult == null) {
		
				tfExtrusionMult = new JFormattedTextField(getNumFormat(2,4));
				tfExtrusionMult.setColumns(5);
		
		}
		return tfExtrusionMult;
	}
	private JFormattedTextField getTfPTemp() {
		if (tfPTemp == null) {
			
				tfPTemp = new JFormattedTextField(getNumFormat(3,0));
				tfPTemp.setColumns(5);
			
		}
		return tfPTemp;
	}
	private JFormattedTextField getTfBTemp() {
		if (tfBTemp == null) {
			
				tfBTemp = new JFormattedTextField(getNumFormat(3,0));
				tfBTemp.setColumns(5);
			
		}
		return tfBTemp;
	}
	private JLabel getLblMm() {
		if (lblMm == null) {
			lblMm = new JLabel("mm");
		}
		return lblMm;
	}
	private JLabel getLblMm_1() {
		if (lblMm_1 == null) {
			lblMm_1 = new JLabel("mm");
		}
		return lblMm_1;
	}
	private JLabel getLblMm_2() {
		if (lblMm_2 == null) {
			lblMm_2 = new JLabel("mm");
		}
		return lblMm_2;
	}
	private JLabel getLblMm_3() {
		if (lblMm_3 == null) {
			lblMm_3 = new JLabel("mm");
		}
		return lblMm_3;
	}
	private JLabel getLblc() {
		if (lblc == null) {
			lblc = new JLabel("\u00B0C");
		}
		return lblc;
	}
	private JLabel getLblc_1() {
		if (lblc_1 == null) {
			lblc_1 = new JLabel("\u00B0C");
		}
		return lblc_1;
	}


	@Override
	public void settingsRequest() {	
		
		master.setValue(0, new MachineSetting<Double>("NozzleDia" ,(Double) tfNozzleDia.getValue()));
		master.setValue(1, new MachineSetting<Double>("PCenterX" ,(Double) tfPrintCenterX.getValue()));
		master.setValue(2, new MachineSetting<Double>("PCenterY" ,(Double) tfPrintCenterY.getValue()));
		master.setValue(3, new MachineSetting<Double>("FilaDia" ,(Double) tfFilaDia.getValue()));
		master.setValue(4, new MachineSetting<Double>("ExtMult" ,(Double) tfExtrusionMult.getValue()));
		master.setValue(5, new MachineSetting<Integer>("PTemp" ,(Integer) tfPTemp.getValue()));
		master.setValue(6, new MachineSetting<Integer>("BTemp" ,(Integer) tfBTemp.getValue()));
		
	}
}
