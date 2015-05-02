package com.neuronrobotics.graphing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeriesCollection;



public class GraphingWindow extends JPanel {
	private XYSeriesCollection xyDataset;
	private ChartPanel chartPanel;
	private ValueAxis axis;
	private JTextField length = new JTextField(5);
	private JSlider window = new JSlider(1, 100);
	private JSlider scale = new JSlider(1, 100);
	private ArrayList<DataChannel> dataChannels = new ArrayList<DataChannel>();

	/**
	 * long 
	 */
	private static final long serialVersionUID = 2171583604829088880L;
	public GraphingWindow() {
		setName("DyIO Graph");
		xyDataset = new XYSeriesCollection();

		JFreeChart chart = ChartFactory.createXYLineChart(
				"Live Data", 
				"Time", 
				"Value",
				xyDataset, 
				PlotOrientation.VERTICAL, 
				true, 
				false, 
				false);
		
		chartPanel = new ChartPanel(chart);
		
		XYPlot plot = (XYPlot) chart.getPlot();
		axis = plot.getDomainAxis();
		scale.setValue(100);
		setDefaultWindow();
		
        scale.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(window.getValue() ==100) {
					setDefaultWindow();
				}else {
					setMovedWindow(window.getValue());
				}
			}
		});
        window.setValue(100);
        window.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(window.getValue() ==100) {
					setDefaultWindow();
				}else {
					setMovedWindow(window.getValue());
				}
			}
		});
        
        length.addKeyListener(new KeyListener() {
			
			
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar() != '\n' && (e.getKeyChar() < '0' || e.getKeyChar() > '9')) {
					e.consume();
				}
			}
			
			
			public void keyReleased(KeyEvent e) {

			}
			
			
			public void keyPressed(KeyEvent e) {

			}
		});
        length.addActionListener(new ActionListener() {
			
			
			public void actionPerformed(ActionEvent arg0) {
				int value;
				try {
					value = Integer.parseInt(length.getText());
				} catch(Exception e) {
					value = scale.getMaximum();
				}
				
				axis.setFixedAutoRange(value);
				length.setText(value + "");
				scale.setValue(value);
				
				invalidate();
				repaint();
			}
		});
        
        JButton clearBtn = new JButton("Clear Data");
        clearBtn.addActionListener(new ActionListener() {
			
			
			public void actionPerformed(ActionEvent arg0) {
				for(DataChannel dc : dataChannels) {
					dc.clear();
				}
			}
		});
        
        JPanel options = new JPanel(new MigLayout());
        options.add(new JLabel("Range Size:"));
        options.add(scale);
        options.add(length);
        options.add(new JLabel("seconds"));
        options.add(clearBtn, "east");
        
		setLayout(new BorderLayout());
		setSize(new Dimension(500, 400));
		add(chartPanel, BorderLayout.CENTER);
		add(options, BorderLayout.SOUTH);
		
		
		JPanel opt= new JPanel(new MigLayout());
		opt.add(new JLabel("View Window"));
		opt.add(window);
		
		JPanel slidingWindow= new JPanel(new MigLayout());
		slidingWindow.add(options, "wrap");
		slidingWindow.add(opt, "wrap");
		add(slidingWindow, BorderLayout.SOUTH);
	}
	
	private void setDefaultWindow() {
		
        axis.setAutoRange(true);
        axis.setFixedAutoRange(scale.getValue());  
        length.setText("" + scale.getValue());
		//invalidate();
		repaint();
	}
	private void setMovedWindow(double percent) {
		
        axis.setAutoRange(false);  
        Range total = xyDataset.getDomainBounds(true);
        double lower =total.getLowerBound();
        double upper =total.getUpperBound();
        double loc = (upper -lower)*percent/100;
        
        
        double sLower =loc-(scale.getValue()/2);
        double sUpper =loc+(scale.getValue()/2);
        axis.setRange(sLower, sUpper);
        length.setText("" + scale.getValue());
		//invalidate();
		//repaint();
	}
	
	public void addDataset(DataChannel data) {
		if(!dataChannels.contains(data)) {
			dataChannels.add(data);
		}
		
		if(!(xyDataset.indexOf(data.getSeries()) > -1)) {
			showDataChannel(data);
		}
	}
	
	public void removeDataset(DataChannel data) {
		if(!dataChannels.contains(data)) {
			dataChannels.add(data);
		}
		
		if(xyDataset.indexOf(data.getSeries()) > -1) {
			hideDataChannel(data);
		}
	}
	
	public void hideDataChannel(DataChannel data) {
		xyDataset.removeSeries(data.getSeries());
	}

	public void showDataChannel(DataChannel data) {
		xyDataset.addSeries(data.getSeries());
	}
}
