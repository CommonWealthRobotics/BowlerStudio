package com.neuronrobotics.nrconsole.plugin.DeviceConfig;

import java.text.NumberFormat;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;

public class Slic3rPrints extends SettingsPanel implements SettingsChangeListener{
	private JLabel lblLayerHeight;
	private JLabel lblWallThickness;
	private JLabel lblUseSupportMaterial;
	private JLabel lblRetractLength;
	private JLabel lblTravelSpeed;
	private JLabel lblPerimeterSpeed;
	private JLabel lblBridgeSpeed;
	private JLabel lblGapFillSpeed;
	private JLabel lblInfillSpeed;
	private JLabel lblSupportMaterialSpeed;
	private JLabel lblSmallPerimeterSpeed;
	private JLabel lblExternalPerimeterSpeed;
	private JLabel lblSolidInfillSpeed;
	private JLabel lblTopSolidInfill;
	private JLabel lblSupportMaterialInterface;
	private JLabel lblFirstLayerSpeed;
	private JFormattedTextField tfLayerHeight;
	private JFormattedTextField tfWallThickness;
	private JFormattedTextField tfRetractLength;
	private JFormattedTextField tfTravelSpeed;
	private JFormattedTextField tfPerimeterSpeed;
	private JFormattedTextField tfBridgeSpeed;
	private JFormattedTextField tfGapFillSpeed;
	private JFormattedTextField tfInfillSpeed;
	private JFormattedTextField tfSupportMaterialSpeed;
	private JFormattedTextField tfSmallPerimeterSpeedPercent;
	private JFormattedTextField tfExternalPerimeterSpeedPercent;
	private JFormattedTextField tfSolidInfillSpeedPercent;
	private JFormattedTextField tfTopSolidInfillSpeedPercent;
	private JFormattedTextField tfSupportMaterialInterSpeedPercent;
	private JFormattedTextField tfFirstLayerSpeedPercent;
	private JLabel lblMm_4;
	private JLabel lblMm_5;
	private JLabel lblMm_6;
	private JLabel lblMms;
	private JLabel lblMms_1;
	private JLabel lblMms_2;
	private JLabel lblMms_3;
	private JLabel lblMms_4;
	private JLabel lblMms_5;
	private JLabel label;
	private JLabel label_1;
	private JLabel label_2;
	private JLabel label_3;
	private JLabel label_4;
	private JLabel label_5;
	private Slic3rMasterPanel master;
	public Slic3rPrints(Slic3rMasterPanel _master) {	
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
		tfLayerHeight.setValue(master.getSetting(7).getValue());
		tfWallThickness.setValue(master.getSetting(8).getValue());
		//TODO Use Support Material?
		tfRetractLength.setValue(master.getSetting(10).getValue());
		tfTravelSpeed.setValue(master.getSetting(11).getValue());
		tfPerimeterSpeed.setValue(master.getSetting(12).getValue());
		tfBridgeSpeed.setValue(master.getSetting(13).getValue());
		tfGapFillSpeed.setValue(master.getSetting(14).getValue());
		tfInfillSpeed.setValue(master.getSetting(15).getValue());
		tfSupportMaterialSpeed.setValue(master.getSetting(16).getValue());
		tfSmallPerimeterSpeedPercent.setValue(master.getSetting(17).getValue());
		tfExternalPerimeterSpeedPercent.setValue(master.getSetting(18).getValue());
		tfSolidInfillSpeedPercent.setValue(master.getSetting(19).getValue());
		tfTopSolidInfillSpeedPercent.setValue(master.getSetting(20).getValue());
		tfSupportMaterialInterSpeedPercent.setValue(master.getSetting(21).getValue());
		tfFirstLayerSpeedPercent.setValue(master.getSetting(22).getValue());
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
							.addComponent(getLblLayerHeight())
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getTfLayerHeight(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getLblMm_4()))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(getLblWallThickness())
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getTfWallThickness(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getLblMm_5()))
						.addComponent(getLblUseSupportMaterial())
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(getLblRetractLength())
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getTfRetractLength(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getLblMm_6()))
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(getLblTravelSpeed())
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(getTfTravelSpeed(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(getLblPerimeterSpeed())
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(getTfPerimeterSpeed(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(getLblMms_1())
								.addComponent(getLblMms())))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(getLblBridgeSpeed())
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getTfBridgeSpeed(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getLblMms_2()))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(getLblGapFillSpeed())
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getTfGapFillSpeed(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getLblMms_3()))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(getLblInfillSpeed())
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getTfInfillSpeed(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getLblMms_4()))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(getLblSupportMaterialSpeed())
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getTfSupportMaterialSpeed(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getLblMms_5()))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(getLblSmallPerimeterSpeed())
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getTfSmallPerimeterSpeedPercent(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getLabel()))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(getLblExternalPerimeterSpeed())
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getTfExternalPerimeterSpeedPercent(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getLabel_1()))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(getLblSolidInfillSpeed())
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getTfSolidInfillSpeedPercent(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getLabel_2()))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(getLblTopSolidInfill())
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getTfTopSolidInfillSpeedPercent(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getLabel_3()))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(getLblSupportMaterialInterface())
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getTfSupportMaterialInterSpeedPercent(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getLabel_4()))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(getLblFirstLayerSpeed())
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getTfFirstLayerSpeedPercent(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getLabel_5())))
					.addContainerGap(175, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(getLblLayerHeight())
						.addComponent(getTfLayerHeight(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(getLblMm_4()))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(getLblWallThickness())
						.addComponent(getTfWallThickness(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(getLblMm_5()))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(getLblUseSupportMaterial())
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(getLblRetractLength())
						.addComponent(getTfRetractLength(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(getLblMm_6()))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(getLblTravelSpeed())
						.addComponent(getTfTravelSpeed(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(getLblMms()))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(getLblPerimeterSpeed())
						.addComponent(getTfPerimeterSpeed(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(getLblMms_1()))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(getLblBridgeSpeed())
						.addComponent(getTfBridgeSpeed(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(getLblMms_2()))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(getLblGapFillSpeed())
						.addComponent(getTfGapFillSpeed(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(getLblMms_3()))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(getLblInfillSpeed())
						.addComponent(getTfInfillSpeed(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(getLblMms_4()))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(getLblSupportMaterialSpeed())
						.addComponent(getTfSupportMaterialSpeed(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(getLblMms_5()))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(getLblSmallPerimeterSpeed())
						.addComponent(getTfSmallPerimeterSpeedPercent(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(getLabel()))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(getLblExternalPerimeterSpeed())
						.addComponent(getTfExternalPerimeterSpeedPercent(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(getLabel_1()))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(getLblSolidInfillSpeed())
						.addComponent(getTfSolidInfillSpeedPercent(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(getLabel_2()))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(getLblTopSolidInfill())
						.addComponent(getTfTopSolidInfillSpeedPercent(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(getLabel_3()))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(getLblSupportMaterialInterface())
						.addComponent(getTfSupportMaterialInterSpeedPercent(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(getLabel_4()))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(getLblFirstLayerSpeed())
						.addComponent(getTfFirstLayerSpeedPercent(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(getLabel_5()))
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
	private JLabel getLblLayerHeight() {
		if (lblLayerHeight == null) {
			lblLayerHeight = new JLabel("Layer Height");
		}
		return lblLayerHeight;
	}
	private JLabel getLblWallThickness() {
		if (lblWallThickness == null) {
			lblWallThickness = new JLabel("Wall Thickness");
		}
		return lblWallThickness;
	}
	private JLabel getLblUseSupportMaterial() {
		if (lblUseSupportMaterial == null) {
			lblUseSupportMaterial = new JLabel("Use Support Material");
		}
		return lblUseSupportMaterial;
	}
	private JLabel getLblRetractLength() {
		if (lblRetractLength == null) {
			lblRetractLength = new JLabel("Retract Length");
		}
		return lblRetractLength;
	}
	private JLabel getLblTravelSpeed() {
		if (lblTravelSpeed == null) {
			lblTravelSpeed = new JLabel("Travel Speed");
		}
		return lblTravelSpeed;
	}
	private JLabel getLblPerimeterSpeed() {
		if (lblPerimeterSpeed == null) {
			lblPerimeterSpeed = new JLabel("Perimeter Speed");
		}
		return lblPerimeterSpeed;
	}
	private JLabel getLblBridgeSpeed() {
		if (lblBridgeSpeed == null) {
			lblBridgeSpeed = new JLabel("Bridge Speed");
		}
		return lblBridgeSpeed;
	}
	private JLabel getLblGapFillSpeed() {
		if (lblGapFillSpeed == null) {
			lblGapFillSpeed = new JLabel("Gap Fill Speed");
		}
		return lblGapFillSpeed;
	}
	private JLabel getLblInfillSpeed() {
		if (lblInfillSpeed == null) {
			lblInfillSpeed = new JLabel("Infill Speed");
		}
		return lblInfillSpeed;
	}
	private JLabel getLblSupportMaterialSpeed() {
		if (lblSupportMaterialSpeed == null) {
			lblSupportMaterialSpeed = new JLabel("Support Material Speed");
		}
		return lblSupportMaterialSpeed;
	}
	private JLabel getLblSmallPerimeterSpeed() {
		if (lblSmallPerimeterSpeed == null) {
			lblSmallPerimeterSpeed = new JLabel("Small Perimeter Speed Percent");
		}
		return lblSmallPerimeterSpeed;
	}
	private JLabel getLblExternalPerimeterSpeed() {
		if (lblExternalPerimeterSpeed == null) {
			lblExternalPerimeterSpeed = new JLabel("External Perimeter Speed Percent");
		}
		return lblExternalPerimeterSpeed;
	}
	private JLabel getLblSolidInfillSpeed() {
		if (lblSolidInfillSpeed == null) {
			lblSolidInfillSpeed = new JLabel("Solid Infill Speed Percent");
		}
		return lblSolidInfillSpeed;
	}
	private JLabel getLblTopSolidInfill() {
		if (lblTopSolidInfill == null) {
			lblTopSolidInfill = new JLabel("Top Solid Infill Speed Percent");
		}
		return lblTopSolidInfill;
	}
	private JLabel getLblSupportMaterialInterface() {
		if (lblSupportMaterialInterface == null) {
			lblSupportMaterialInterface = new JLabel("Support Material Interface Speed Percent");
		}
		return lblSupportMaterialInterface;
	}
	private JLabel getLblFirstLayerSpeed() {
		if (lblFirstLayerSpeed == null) {
			lblFirstLayerSpeed = new JLabel("First Layer Speed Percent");
		}
		return lblFirstLayerSpeed;
	}
	private JFormattedTextField getTfLayerHeight() {
		if (tfLayerHeight == null) {
			
				tfLayerHeight = new JFormattedTextField(getNumFormat(2,2));
				tfLayerHeight.setColumns(5);
		
		}
		return tfLayerHeight;
	}
	private JFormattedTextField getTfWallThickness() {
		if (tfWallThickness == null) {
			
				tfWallThickness = new JFormattedTextField(getNumFormat(2,2));
				tfWallThickness.setColumns(5);
			
		}
		return tfWallThickness;
	}
	private JFormattedTextField getTfRetractLength() {
		if (tfRetractLength == null) {
		
				tfRetractLength = new JFormattedTextField(getNumFormat(3,0));
				tfRetractLength.setColumns(5);
		
		}
		return tfRetractLength;
	}
	private JFormattedTextField getTfTravelSpeed() {
		if (tfTravelSpeed == null) {
			
				tfTravelSpeed = new JFormattedTextField(getNumFormat(3,0));
				tfTravelSpeed.setColumns(5);
		
		}
		return tfTravelSpeed;
	}
	private JFormattedTextField getTfPerimeterSpeed() {
		if (tfPerimeterSpeed == null) {
			
				tfPerimeterSpeed = new JFormattedTextField(getNumFormat(3,0));
				tfPerimeterSpeed.setColumns(5);
		
		}
		return tfPerimeterSpeed;
	}
	private JFormattedTextField getTfBridgeSpeed() {
		if (tfBridgeSpeed == null) {
			
				tfBridgeSpeed = new JFormattedTextField(getNumFormat(3,0));
				tfBridgeSpeed.setColumns(5);
			
		}
		return tfBridgeSpeed;
	}
	private JFormattedTextField getTfGapFillSpeed() {
		if (tfGapFillSpeed == null) {
			
				tfGapFillSpeed = new JFormattedTextField(getNumFormat(3,0));
				tfGapFillSpeed.setColumns(5);
			
		}
		return tfGapFillSpeed;
	}
	private JFormattedTextField getTfInfillSpeed() {
		if (tfInfillSpeed == null) {
			
				tfInfillSpeed = new JFormattedTextField(getNumFormat(3,0));
				tfInfillSpeed.setColumns(5);
			
		}
		return tfInfillSpeed;
	}
	private JFormattedTextField getTfSupportMaterialSpeed() {
		if (tfSupportMaterialSpeed == null) {
			
				tfSupportMaterialSpeed = new JFormattedTextField(getNumFormat(3,0));
				tfSupportMaterialSpeed.setColumns(5);
		
		}
		return tfSupportMaterialSpeed;
	}
	private JFormattedTextField getTfSmallPerimeterSpeedPercent() {
		if (tfSmallPerimeterSpeedPercent == null) {
			
				tfSmallPerimeterSpeedPercent = new JFormattedTextField(getNumFormat(3,0));
				tfSmallPerimeterSpeedPercent.setColumns(5);
			
		}
		return tfSmallPerimeterSpeedPercent;
	}
	private JFormattedTextField getTfExternalPerimeterSpeedPercent() {
		if (tfExternalPerimeterSpeedPercent == null) {
			
				tfExternalPerimeterSpeedPercent = new JFormattedTextField(getNumFormat(3,0));
				tfExternalPerimeterSpeedPercent.setColumns(5);
		
		}
		return tfExternalPerimeterSpeedPercent;
	}
	private JFormattedTextField getTfSolidInfillSpeedPercent() {
		if (tfSolidInfillSpeedPercent == null) {
			
				tfSolidInfillSpeedPercent = new JFormattedTextField(getNumFormat(3,0));
				tfSolidInfillSpeedPercent.setColumns(5);
		
		}
		return tfSolidInfillSpeedPercent;
	}
	private JFormattedTextField getTfTopSolidInfillSpeedPercent() {
		if (tfTopSolidInfillSpeedPercent == null) {
			
				tfTopSolidInfillSpeedPercent = new JFormattedTextField(getNumFormat(3,0));
				tfTopSolidInfillSpeedPercent.setColumns(5);
		
		}
		return tfTopSolidInfillSpeedPercent;
	}
	private JFormattedTextField getTfSupportMaterialInterSpeedPercent() {
		if (tfSupportMaterialInterSpeedPercent == null) {
			
				tfSupportMaterialInterSpeedPercent = new JFormattedTextField(getNumFormat(3,0));
				tfSupportMaterialInterSpeedPercent.setColumns(5);
			
		}
		return tfSupportMaterialInterSpeedPercent;
	}
	private JFormattedTextField getTfFirstLayerSpeedPercent() {
		if (tfFirstLayerSpeedPercent == null) {
			
				tfFirstLayerSpeedPercent = new JFormattedTextField(getNumFormat(3,0));
				tfFirstLayerSpeedPercent.setColumns(5);
	
		}
		return tfFirstLayerSpeedPercent;
	}
	private JLabel getLblMm_4() {
		if (lblMm_4 == null) {
			lblMm_4 = new JLabel("mm");
		}
		return lblMm_4;
	}
	private JLabel getLblMm_5() {
		if (lblMm_5 == null) {
			lblMm_5 = new JLabel("mm");
		}
		return lblMm_5;
	}
	private JLabel getLblMm_6() {
		if (lblMm_6 == null) {
			lblMm_6 = new JLabel("mm");
		}
		return lblMm_6;
	}
	private JLabel getLblMms() {
		if (lblMms == null) {
			lblMms = new JLabel("mm/s");
		}
		return lblMms;
	}
	private JLabel getLblMms_1() {
		if (lblMms_1 == null) {
			lblMms_1 = new JLabel("mm/s");
		}
		return lblMms_1;
	}
	private JLabel getLblMms_2() {
		if (lblMms_2 == null) {
			lblMms_2 = new JLabel("mm/s");
		}
		return lblMms_2;
	}
	private JLabel getLblMms_3() {
		if (lblMms_3 == null) {
			lblMms_3 = new JLabel("mm/s");
		}
		return lblMms_3;
	}
	private JLabel getLblMms_4() {
		if (lblMms_4 == null) {
			lblMms_4 = new JLabel("mm/s");
		}
		return lblMms_4;
	}
	private JLabel getLblMms_5() {
		if (lblMms_5 == null) {
			lblMms_5 = new JLabel("mm/s");
		}
		return lblMms_5;
	}
	private JLabel getLabel() {
		if (label == null) {
			label = new JLabel("%");
		}
		return label;
	}
	private JLabel getLabel_1() {
		if (label_1 == null) {
			label_1 = new JLabel("%");
		}
		return label_1;
	}
	private JLabel getLabel_2() {
		if (label_2 == null) {
			label_2 = new JLabel("%");
		}
		return label_2;
	}
	private JLabel getLabel_3() {
		if (label_3 == null) {
			label_3 = new JLabel("%");
		}
		return label_3;
	}
	private JLabel getLabel_4() {
		if (label_4 == null) {
			label_4 = new JLabel("%");
		}
		return label_4;
	}
	private JLabel getLabel_5() {
		if (label_5 == null) {
			label_5 = new JLabel("%");
		}
		return label_5;
	}


	@Override
	public void settingsRequest() {	
		
		
		master.setValue(7, new MachineSetting<Double>("LayerHeight" ,(Double) tfLayerHeight.getValue()));
		master.setValue(8, new MachineSetting<Integer>("WallThickness" ,(Integer) tfWallThickness.getValue()));
		//master.setValue(9, new MachineSetting<Double>("UseSupport" ,(Double) tfRetractLength.getValue()));
		master.setValue(10, new MachineSetting<Double>("RetractLength" ,(Double) tfRetractLength.getValue()));
		master.setValue(11, new MachineSetting<Integer>("TravelSpd" ,(Integer) tfTravelSpeed.getValue()));
		master.setValue(12, new MachineSetting<Integer>("PeriSpd" ,(Integer) tfPerimeterSpeed.getValue()));
		master.setValue(13, new MachineSetting<Integer>("BridgeSpd" ,(Integer) tfBridgeSpeed.getValue()));
		master.setValue(14, new MachineSetting<Integer>("GapFillSpd" ,(Integer) tfGapFillSpeed.getValue()));
		master.setValue(15, new MachineSetting<Integer>("InfillSpd" ,(Integer) tfInfillSpeed.getValue()));
		master.setValue(16, new MachineSetting<Integer>("SupportMatSpd" ,(Integer) tfSupportMaterialSpeed.getValue()));
		master.setValue(17, new MachineSetting<Integer>("SmPeriSpdPcnt" ,(Integer) tfSmallPerimeterSpeedPercent.getValue()));
		master.setValue(18, new MachineSetting<Integer>("ExtPeriSpdPcnt" ,(Integer) tfExternalPerimeterSpeedPercent.getValue()));
		master.setValue(19, new MachineSetting<Integer>("SolidInfillSpdPcnt" ,(Integer) tfSolidInfillSpeedPercent.getValue()));
		master.setValue(20, new MachineSetting<Integer>("TopSolidInfillSpdPcnt" ,(Integer) tfTopSolidInfillSpeedPercent.getValue()));
		master.setValue(21, new MachineSetting<Integer>("SupportMatIntSpdPcnt" ,(Integer) tfSupportMaterialInterSpeedPercent.getValue()));
		master.setValue(22, new MachineSetting<Integer>("FirstLayerSpdPcnt" ,(Integer) tfFirstLayerSpeedPercent.getValue()));
	}
}
