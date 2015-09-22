package com.neuronrobotics.nrconsole.plugin.DeviceConfig;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ScrollPaneConstants;

import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.LinkType;
import com.neuronrobotics.sdk.namespace.bcs.pid.IPidControlNamespace;

import java.awt.FlowLayout;

import javax.swing.JList;

import java.awt.BorderLayout;

import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

public class AxisPanel extends JPanel {
private LinkConfiguration thisLink;
private JList list;
private JScrollPane scrollPane;
private JPanel viewPanel;
private JButton btnNewButton;
private JButton btnNewButton_1;

private SettingsListItem setLinkName;
private SettingsListItem setLinkType;
private SettingsListItem setHardwareIndex;
private SettingsListItem setHomingTicksPerSecond;
private SettingsListItem setIndexLatch;
private SettingsListItem setKP;
private SettingsListItem setKI;
private SettingsListItem setKD;
private SettingsListItem setLowerLimit;
private SettingsListItem setUpperLimit;
private SettingsListItem setLowerVelocity;
private SettingsListItem setUpperVelocity;
private SettingsListItem setLinkIndex;
private SettingsListItem setScale;
	public AxisPanel(LinkConfiguration _thisLink){
		setLayout(new BorderLayout(0, 0));
		add(getScrollPane());
		thisLink = _thisLink;
		
		//getScrollPane().add(new SettingsListItem());
		
		viewPanel.add(setLinkName = new SettingsListItem("Link Name", thisLink.getName()), "cell 0 0");
		FlowLayout flowLayout = (FlowLayout) setLinkName.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		viewPanel.add(setLinkType = new SettingsListItem("Link Type", thisLink.getType().getName()), "cell 0 1");
		FlowLayout flowLayout_1 = (FlowLayout) setLinkType.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		viewPanel.add(setHardwareIndex = new SettingsListItem("Hardware Index", thisLink.getHardwareIndex()), "cell 0 2");
		FlowLayout flowLayout_2 = (FlowLayout) setHardwareIndex.getLayout();
		flowLayout_2.setAlignment(FlowLayout.LEFT);
		viewPanel.add(setHomingTicksPerSecond = new SettingsListItem("Homing Ticks per Second", thisLink.getHomingTicksPerSecond()), "cell 0 3");
		FlowLayout flowLayout_3 = (FlowLayout) setHomingTicksPerSecond.getLayout();
		flowLayout_3.setAlignment(FlowLayout.LEFT);
		viewPanel.add(setIndexLatch = new SettingsListItem("Index Latch",thisLink.getIndexLatch()), "cell 0 4");
		FlowLayout flowLayout_4 = (FlowLayout) setIndexLatch.getLayout();
		flowLayout_4.setAlignment(FlowLayout.LEFT);
		viewPanel.add(setKP = new SettingsListItem("KP",thisLink.getKP()), "cell 0 5");
		FlowLayout flowLayout_5 = (FlowLayout) setKP.getLayout();
		flowLayout_5.setAlignment(FlowLayout.LEFT);
		viewPanel.add(setKI = new SettingsListItem("KI", thisLink.getKI()), "cell 0 6");
		FlowLayout flowLayout_6 = (FlowLayout) setKI.getLayout();
		flowLayout_6.setAlignment(FlowLayout.LEFT);
		viewPanel.add(setKD = new SettingsListItem("KD", thisLink.getKD()), "cell 0 7");
		FlowLayout flowLayout_7 = (FlowLayout) setKD.getLayout();
		flowLayout_7.setAlignment(FlowLayout.LEFT);
		viewPanel.add(setLowerLimit = new SettingsListItem("Lower Limit", thisLink.getLowerLimit()), "cell 0 8");
		FlowLayout flowLayout_8 = (FlowLayout) setLowerLimit.getLayout();
		flowLayout_8.setAlignment(FlowLayout.LEFT);
		viewPanel.add(setUpperLimit = new SettingsListItem("Upper Limit", thisLink.getUpperLimit()), "cell 0 9");
		FlowLayout flowLayout_9 = (FlowLayout) setUpperLimit.getLayout();
		flowLayout_9.setAlignment(FlowLayout.LEFT);
		viewPanel.add(setLowerVelocity = new SettingsListItem("Lower Velocity", thisLink.getLowerVelocity()), "cell 0 10");
		FlowLayout flowLayout_10 = (FlowLayout) setLowerVelocity.getLayout();
		flowLayout_10.setAlignment(FlowLayout.LEFT);
		viewPanel.add(setUpperVelocity = new SettingsListItem("Upper Velocity", thisLink.getUpperVelocity()), "cell 0 11");
		FlowLayout flowLayout_11 = (FlowLayout) setUpperVelocity.getLayout();
		flowLayout_11.setAlignment(FlowLayout.LEFT);
		viewPanel.add(setLinkIndex = new SettingsListItem("Link Index",thisLink.getLinkIndex()), "cell 0 12");
		FlowLayout flowLayout_12 = (FlowLayout) setLinkIndex.getLayout();
		flowLayout_12.setAlignment(FlowLayout.LEFT);
		viewPanel.add(setScale = new SettingsListItem("Scale" ,thisLink.getScale()), "cell 0 13");
		FlowLayout flowLayout_13 = (FlowLayout) setScale.getLayout();
		flowLayout_13.setAlignment(FlowLayout.LEFT);
		//viewPanel.add(new SettingsListItem(), "cell 0 14");
		//viewPanel.add(new SettingsListItem(), "cell 0 15");
		//viewPanel.add(new SettingsListItem(), "cell 0 16");
		//viewPanel.add(new SettingsListItem(), "cell 0 17");
		
	
		
		
		}
	public LinkConfiguration getLink(){
		return thisLink;
	}
	private JList getList() {
		if (list == null) {
			list = new JList();
		}
		return list;
	}
	private void reloadSettings(){
		setLinkName.loadValues("Link Name", thisLink.getName());
		setLinkType.loadValues("Link Type", thisLink.getType().getName());
		setHardwareIndex.loadValues("Hardware Index", thisLink.getHardwareIndex());
		setHomingTicksPerSecond.loadValues("Homing Ticks per Second", thisLink.getHomingTicksPerSecond());
		setIndexLatch.loadValues("Index Latch",thisLink.getIndexLatch());
		setKP.loadValues("KP",thisLink.getKP());
		setKI.loadValues("KI", thisLink.getKI());
		setKD.loadValues("KD", thisLink.getKD());
		setLowerLimit.loadValues("Lower Limit", thisLink.getLowerLimit());
		setUpperLimit.loadValues("Upper Limit", thisLink.getUpperLimit());
		setLowerVelocity.loadValues("Lower Velocity", thisLink.getLowerVelocity());
		setUpperVelocity.loadValues("Upper Velocity", thisLink.getUpperVelocity());
		setLinkIndex.loadValues("Link Index",thisLink.getLinkIndex());
		setScale.loadValues("Scale" ,thisLink.getScale());
		
	}
	public void writeSettings(){
		thisLink.setName(setLinkName.getStringValue());
		thisLink.setType(LinkType.fromString(setLinkType.getStringValue()));
		thisLink.setHardwareIndex(setHardwareIndex.getIntValue());
		thisLink.setHomingTicksPerSecond(setHomingTicksPerSecond.getIntValue());
		thisLink.setIndexLatch(setIndexLatch.getIntValue());
		thisLink.setKP(setKP.getDoubleValue());
		thisLink.setKI(setKI.getDoubleValue());
		thisLink.setKD(setKD.getDoubleValue());
		thisLink.setLowerLimit(setLowerLimit.getIntValue());
		thisLink.setUpperLimit(setUpperLimit.getIntValue());
		thisLink.setLowerVelocity(setLowerVelocity.getDoubleValue());
		thisLink.setUpperVelocity(setUpperVelocity.getDoubleValue());
		thisLink.setLinkIndex(setLinkIndex.getIntValue());
		thisLink.setScale(setScale.getDoubleValue());
		reloadSettings();
		}
	
	private JScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new JScrollPane();
			scrollPane.setViewportView(getViewPanel());
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			
		}
		return scrollPane;
	}
	private JPanel getViewPanel() {
		if (viewPanel == null) {
			viewPanel = new JPanel();
			viewPanel.setLayout(new MigLayout("", "[grow,fill]", "[][]"));
			
		}
		return viewPanel;
	}
}
