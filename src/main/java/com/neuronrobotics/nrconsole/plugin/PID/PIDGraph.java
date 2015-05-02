package com.neuronrobotics.nrconsole.plugin.PID;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import net.miginfocom.swing.MigLayout;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.Log;

import com.neuronrobotics.graphing.CSVWriter;
import com.neuronrobotics.graphing.GraphDataElement;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class PIDGraph extends JPanel  {
	private ArrayList<GraphDataElement> dataTable = new ArrayList<GraphDataElement>();
	private XYSeries setpoints;
	private XYSeries positions;
	private XYSeriesCollection xyDataset=new XYSeriesCollection();
	private JButton save = new JButton("Export Data");
	private JButton clear = new JButton("Clear graph");
	private int channel;
	
	private double startTime=System.currentTimeMillis();
	
	double[] data = {0,0};
	
	public PIDGraph(int channel){
		this.channel=channel;
		setLayout(new MigLayout());
		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		JFreeChart chart = ChartFactory.createXYLineChart(
				"Live Data", 
				"Time", 
				"Value",
				xyDataset, 
				PlotOrientation.VERTICAL, 
				true, 
				false, 
				false);
		
		ChartPanel cp = new ChartPanel(chart);
		
		// set the graph to zoom around the values and not include zero
		NumberAxis rangeAxis = (NumberAxis) chart.getXYPlot().getRangeAxis();
		rangeAxis.setAutoRangeIncludesZero(false);
		
		JPanel buttons = new JPanel();
		buttons.add(save);
		buttons.add(clear);
		add(buttons,"wrap");
		add(cp,"wrap");
		//add(save);
		//add(clear,"wrap");
		save.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				String name ="Graph_Of_Group_"+getChannel()+"_"+System.currentTimeMillis()+".csv";
				CSVWriter.WriteToCSV(dataTable,name);
				dataTable.clear();
				setpoints.clear();
				positions.clear();
			}
		});
		clear.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				dataTable.clear();
				setpoints.clear();
				positions.clear();
			}
		});
		
		setSize(new Dimension(150,100));
		init();
	}
	/**
	 * long 
	 */
	private static final long serialVersionUID = 1L;
	public void addEvent(double setpoint, double position) {
		data[0] =  setpoint;
		data[1] =  position;
	}
	private void init(){
		xyDataset.removeAllSeries();
		setpoints = new  XYSeries("Set Point");
		positions = new  XYSeries("Position");
		xyDataset.addSeries(setpoints);
		xyDataset.addSeries(positions);
		new updaterThread().start();
	}
	private int getChannel() {
		return channel;
	}
	
	private class updaterThread extends Thread{
		double[] lastData= {0,0};
		public void run(){
			while (true){
				ThreadUtil.wait(50);
				if(lastData[0] != data[0] || lastData[1]!=data[1]){
					lastData[0]=data[0];
					lastData[1]=data[1];
					try{
						
						
						double time = (System.currentTimeMillis()-startTime);
						
						dataTable.add(new GraphDataElement((long) time,data));
						XYDataItem s = new XYDataItem(time,data[0]);
						XYDataItem p = new XYDataItem(time,data[1]);

						if(setpoints.getItemCount()>99){
							setpoints.remove(0);
						}
						setpoints.add(s);
						
						if(positions.getItemCount()>99){
							positions.remove(0);
						}
						positions.add(p);

					}catch(Exception e){
						System.err.println("Failed to set a data point");
						e.printStackTrace();
						Log.error(e);
						Log.error(e.getStackTrace());
						init();
					}
				}
			}
		}
		
	}

}
