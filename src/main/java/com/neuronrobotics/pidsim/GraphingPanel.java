package com.neuronrobotics.pidsim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Point;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.dial.DialBackground;
import org.jfree.chart.plot.dial.DialCap;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.dial.DialPointer;
import org.jfree.chart.plot.dial.DialTextAnnotation;
import org.jfree.chart.plot.dial.DialValueIndicator;
import org.jfree.chart.plot.dial.StandardDialFrame;
import org.jfree.chart.plot.dial.StandardDialScale;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.StandardGradientPaintTransformer;

class GraphingPanel extends JPanel implements ChangeListener {
	private static final long serialVersionUID = 1L;
	private DefaultValueDataset dataset1;
	private DefaultValueDataset dataset2;
	private JSlider setpointSlider;
	//private JButton settingBtn = new JButton("Settings");
	//private JButton showDataBtn = new JButton("Data");
	private boolean ignoreUpdate = false;
	private SettingsDialog settingsDialog;
	private PIDSim sim;
	private DataPanel dataFrame;
	private PIDConstantsDialog constants;
	
	public GraphingPanel(PIDSim sim, PIDConstantsDialog constants, String title) {
		this.sim = sim;
		this.constants = constants;
		
    	dataFrame = new DataPanel("Live Data");
    	//dataFrame.pack();
    	
		settingsDialog = new SettingsDialog(sim,constants);
		
		dataset1 = new DefaultValueDataset(0.0);
		dataset2 = new DefaultValueDataset(0.0);

		DialPlot plot = new DialPlot();
		plot.setView(0.0, 0.0, 1.0, 1.0);
		plot.setDataset(0, dataset1);
		plot.setDataset(1, dataset2);
		StandardDialFrame dialFrame = new StandardDialFrame();
		dialFrame.setBackgroundPaint(Color.lightGray);
		dialFrame.setForegroundPaint(Color.darkGray);
		plot.setDialFrame(dialFrame);

		GradientPaint gp = new GradientPaint(new Point(), new Color(255, 255, 255), new Point(), new Color(170, 170, 220));
		DialBackground db = new DialBackground(gp);
		db.setGradientPaintTransformer(new StandardGradientPaintTransformer(GradientPaintTransformType.VERTICAL));
		plot.setBackground(db);

		DialTextAnnotation annotation1 = new DialTextAnnotation("Degrees");
		annotation1.setFont(new Font("Dialog", Font.BOLD, 14));
		annotation1.setRadius(0.7);

		plot.addLayer(annotation1);

		DialValueIndicator dvi = new DialValueIndicator(0);
		dvi.setFont(new Font("Dialog", Font.PLAIN, 10));
		dvi.setOutlinePaint(Color.darkGray);
		dvi.setRadius(0.60);
		dvi.setAngle(-103.0);
		plot.addLayer(dvi);

		DialValueIndicator dvi2 = new DialValueIndicator(1);
		dvi2.setFont(new Font("Dialog", Font.PLAIN, 10));
		dvi2.setOutlinePaint(Color.red);
		dvi2.setRadius(0.60);
		dvi2.setAngle(-77.0);
		plot.addLayer(dvi2);

		StandardDialScale scale = new StandardDialScale(0, 180, 0, 180, 30.0, 4);
		scale.setTickRadius(0.88);
		scale.setTickLabelOffset(0.15);
		scale.setTickLabelFont(new Font("Dialog", Font.PLAIN, 14));
		plot.addScale(0, scale);

		StandardDialScale scale2 = new StandardDialScale(0, 180, 0, 180, 30.0, 4);
		scale2.setTickRadius(0.50);
		scale2.setTickLabelOffset(0.15);
		scale2.setTickLabelFont(new Font("Dialog", Font.PLAIN, 10));
		scale2.setMajorTickPaint(Color.red);
		scale2.setMinorTickPaint(Color.red);
		plot.addScale(1, scale2);
		plot.mapDatasetToScale(1, 1);
		
		DialPointer needle2 = new DialPointer.Pin(1);
		needle2.setRadius(0.55);
		plot.addPointer(needle2);

		DialPointer needle = new DialPointer.Pointer(0);
		plot.addPointer(needle);

		DialCap cap = new DialCap();
		cap.setRadius(0.10);
		plot.setCap(cap);

		JFreeChart chart1 = new JFreeChart(plot);
		chart1.setTitle("Position");
		ChartPanel cp1 = new ChartPanel(chart1);
		cp1.setPreferredSize(new Dimension(400, 400));

		setpointSlider = new JSlider(0, 1800);
		setpointSlider.setMajorTickSpacing(300);
		setpointSlider.setPaintTicks(true);
		setpointSlider.setValue(1800);
		setpointSlider.addChangeListener(this);
		
//		settingBtn.addActionListener(new ActionListener() {
//			
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				settingsDialog.setVisible(true);
//			}
//		});
		
//		showDataBtn.addActionListener(new ActionListener() {
//			
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				dataFrame.setVisible(true);
//			}
//		});

		JPanel settingsPanel = new JPanel(new MigLayout());
		settingsPanel.add(settingsDialog, "cell 0 0");
		settingsPanel.add(setpointSlider, "cell 1 0");
		settingsPanel.add(dataFrame, "cell 2 0");
		
		JPanel panel = new JPanel(new MigLayout());
		panel.add(cp1,"wrap");
		panel.add(settingsPanel,"wrap");

        //setTitle(title);        
        add(panel);
	}

	/**
	 * Handle a change in the slider by updating the dataset value. This
	 * automatically triggers a chart repaint.
	 * 
	 * @param e
	 *            the event.
	 */
	public void stateChanged(ChangeEvent e) {
		if( ignoreUpdate ) {
			return;
		}
		
		double value = (double) (1800-setpointSlider.getValue())/10;
		setSetPoint(value);
		sim.setSetPoint(value);
	}
	
	public void setPosition(double value) {		
		if(value > 180) {
			value = 180;
		}
		
		if(value < 0) {
			value = 0;
		}
		dataset1.setValue(new Double(value));
		dataFrame.addPosition(value, sim.getTime());
	}

	public void setSetPoint(double value) {
		if(value > 180) {
			value = 180;
		}
		
		if(value < 0) {
			value = 0;
		}
		
		ignoreUpdate = true;
		dataset2.setValue(new Double(value));
		setpointSlider.setValue(1800 - (int) value * 10);
		dataFrame.setSetpoint(value);
		ignoreUpdate = false;
	}
}
