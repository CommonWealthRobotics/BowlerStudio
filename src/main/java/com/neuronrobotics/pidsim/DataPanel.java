/* ---------------------
 * DynamicDataDemo2.java
 * ---------------------
 * (C) Copyright 2003-2009, by Object Refinery Limited.
 *
 */

package com.neuronrobotics.pidsim;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

public class DataPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private XYPlot plot;
	
    private XYSeries positionSer;
    private Marker setpoint = new ValueMarker(0);
    private JButton exportXlsBtn = new JButton("Export Excel");
    private JButton exportCsvBtn = new JButton("Export CSV");
    
    /**
     * Creates a new self-contained demo panel.
     */
    public DataPanel(String title) {
        //setTitle(title);
        setLayout(new BorderLayout());
        
        positionSer = new XYSeries("True Position");
        
        XYSeriesCollection dataset1 = new XYSeriesCollection(positionSer);
        
        JFreeChart chart = ChartFactory.createTimeSeriesChart("Position", "Time (seconds)", "Angle (degrees)", dataset1,true, true, false);

        plot = (XYPlot) chart.getPlot();
        plot.setRenderer(1, new DefaultXYItemRenderer());
        plot.mapDatasetToRangeAxis(1, 1);
        
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(20000.0);  // 20 seconds
        
        ChartUtilities.applyCurrentTheme(chart);

        ChartPanel chartPanel = new ChartPanel(chart);        
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        
        exportXlsBtn.setActionCommand("EXPORT_XLS");
        exportCsvBtn.setActionCommand("EXPORT_CSV");
        
        exportXlsBtn.addActionListener(this);
        exportCsvBtn.addActionListener(this);
        
        JPanel exportPanel = new JPanel();
        exportPanel.add(exportXlsBtn);
        exportPanel.add(exportCsvBtn);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(chartPanel);
        mainPanel.add(exportPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
   }

	public void addPosition(double position, long time) {
		positionSer.add(time, position);
	}

	public void setSetpoint(double sp) {
		plot.removeRangeMarker(setpoint);
		setpoint = new ValueMarker(sp);
		setpoint.setLabelOffsetType(LengthAdjustmentType.EXPAND);
		setpoint.setPaint(Color.red);
		setpoint.setStroke(new BasicStroke(2.0f));
		setpoint.setLabel("Setpoint");
		setpoint.setLabelFont(new Font("SansSerif", Font.PLAIN, 11));
		setpoint.setLabelPaint(Color.red);
		setpoint.setLabelAnchor(RectangleAnchor.TOP_LEFT);
		setpoint.setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);
        plot.addRangeMarker(setpoint);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
		int fcRtn = fc.showSaveDialog(this);
		
		if(fcRtn != JFileChooser.APPROVE_OPTION) {
			return;
		}
		
		File f = fc.getSelectedFile();
		
		if(e.getActionCommand().equalsIgnoreCase("EXPORT_XLS")) {
			ExcelWriter ew = new ExcelWriter();
			ew.setFile(f);
			ew.addData(positionSer);
			ew.cleanup();
		} else {
			if (!f.getName().endsWith(".csv")){
			    f = new File(f.getAbsolutePath()+".csv");
			}
			CSVWriter cw = new CSVWriter();
			cw.setFile(f);
			cw.addData(positionSer);
			cw.cleanup();
		}
		
	}
}
