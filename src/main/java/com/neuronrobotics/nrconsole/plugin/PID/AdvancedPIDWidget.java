package com.neuronrobotics.nrconsole.plugin.PID;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

public class AdvancedPIDWidget extends JPanel{

	private static final long serialVersionUID = 1L;
	private PIDControlWidget pid;

	
	private StepLoop looper=null;
	private SineTrack siner=null;
	
	private JTextField loopStart=new JTextField(new Double(0).toString(),5);
	private JTextField loopMiddle=new JTextField(new Double(100).toString(),5);
	private JTextField loopEnd=new JTextField(new Double(200).toString(),5);
	private JTextField loopIterations=new JTextField(new Double(20).toString(),5);
	private JTextField loopTime=new JTextField(new Double(.5).toString(),5);
	private JButton  runLoop = new JButton("Run Step Loop");
	private JButton  stopLoop = new JButton("Stop Loop");
	private JButton  runSin = new JButton("Run Sin wave");
	private JButton  jogP = new JButton("Jog+");
	private JTextField jogV=new JTextField(new Integer(100).toString(),5);
	private JButton  jogM = new JButton("Jog-");
	
	public AdvancedPIDWidget(PIDControlWidget pid) {
		setPid(pid);
		setLayout(new MigLayout());

		initFrame();
	}
	 

	private void initFrame() {
		setLayout(new MigLayout());

		JPanel stepPanel = new JPanel(new MigLayout());
		
		jogP.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				int val = Integer.parseInt(jogV.getText());
				getPid().setSetpoint(getPid().getSetPoint()+val);
			}
		});
		jogM.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				int val = Integer.parseInt(jogV.getText());
				getPid().setSetpoint(getPid().getSetPoint()-val);
			}
		});
	    
	    JPanel loopPanel = new JPanel(new MigLayout());
	    loopPanel.add(new JLabel("Step Control Input"));
	    loopPanel.add(jogP);
	    loopPanel.add(jogV);
	    loopPanel.add(jogM,"wrap");
	    loopPanel.add(new JLabel("Loop Start"));
	    loopPanel.add(loopStart,"wrap");
	    loopPanel.add(new JLabel("Loop Middle"));
	    loopPanel.add(loopMiddle,"wrap");
	    loopPanel.add(new JLabel("Loop End"));
	    loopPanel.add(loopEnd,"wrap");
	    loopPanel.add(new JLabel("Loop Iterations"));
	    loopPanel.add(loopIterations,"wrap");
	    loopPanel.add(new JLabel("Loop Time"));
	    loopPanel.add(loopTime,"wrap");
	    runLoop.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				double s=0,m=0,e=0,i=0,t=0;
				try{
					s=Double.parseDouble(loopStart.getText());
					m=Double.parseDouble(loopMiddle.getText());
					e=Double.parseDouble(loopEnd.getText());
					i=Double.parseDouble(loopIterations.getText());
					t=Double.parseDouble(loopTime.getText());
				}catch(Exception ex){
					JOptionPane.showMessageDialog(null, "Bad Step values", "PID Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				looper = new StepLoop(s,m,e,i,t);
				looper.start();
				stopLoop.setEnabled(true);
				runLoop.setEnabled(false);
				runSin.setEnabled(false);
			}
		});
	    runSin.addActionListener(new ActionListener() {
			@SuppressWarnings("unused")
			
			public void actionPerformed(ActionEvent arg0) {
				double m=0,e=0,i=0,t=0;
				try{
					m=Double.parseDouble(loopMiddle.getText());
					e=Double.parseDouble(loopEnd.getText());
					i=Double.parseDouble(loopIterations.getText());
					t=Double.parseDouble(loopTime.getText());
				}catch(Exception ex){
					JOptionPane.showMessageDialog(null, "Bad Step values", "PID Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				siner= new SineTrack((int)e,.03,80,1);
				siner.start();
				stopLoop.setEnabled(true);
				runSin.setEnabled(false);
				runLoop.setEnabled(false);
			}
		});
	    stopLoop.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				stopLoop.setEnabled(false);
				if(looper!=null){
					looper.kill();
					looper=null;
				}
				if(siner!=null){
					siner.kill();
					siner=null;
				}
			}
		});
	    loopPanel.add(runLoop);
	    loopPanel.add(runSin);
	    loopPanel.add(stopLoop);
	    stopLoop.setEnabled(false);
	    
	    add(stepPanel);
	    add(loopPanel);
	}
	private void setPid(PIDControlWidget pid) {
		this.pid = pid;
	}
	private PIDControlWidget getPid() {
		return pid;
	}
	
	private class SineTrack extends Thread{
		private boolean run=false;
		private int a;
		private double p;
		private int pPC;
		private double cy;
		public SineTrack(int amplitude, double period, int pointsPerCycle,int cycles){
			a=amplitude;
			p=period;
			pPC=pointsPerCycle;
			cy=cycles;
			run=true;
			//System.out.println("Amplitude of sin wave: "+a+" period: "+p+" cycles/sec, points per cycle: "+pPC+" for "+cy+" cycles");
			getPid().setSetpoint(a);
		}
		public void run(){
			double radInc = (Math.PI*2.0)/((double)pPC); 
			double time = (1.0/p)/((double)pPC);
			try {Thread.sleep(1000);} catch (InterruptedException e) {}
			//System.out.println("Sin wave tracking "+time+" seconds per increment");
			for (int i=0;i<cy;i++){
				for (int j=0;j<pPC;j++){
					try {Thread.sleep(5);} catch (InterruptedException e) {}
					int val = (int) (((double)a)*Math.sin(radInc*((double)j)))+a;
					if(run)
						getPid().setSetpoint(val);
					if(run)
						try {Thread.sleep((long) (time*1000));} catch (InterruptedException e) {}
				}
			}
			//System.out.println("Loop Done");
			stopLoop.setEnabled(false);
			runLoop.setEnabled(true);
			runSin.setEnabled(true);
			getPid().setSetpoint(a);
		}
		public void kill() {
			run=false;
		}
	}
	
	private class StepLoop extends Thread{
		double start,middle,end,iter,time;
		private boolean run=false;
		public StepLoop(double s,double m, double e, double i, double t) {
			start=s;
			middle=m;
			end=e;
			iter=i;
			time=t;
			run=true;
		}

		public void kill() {
			run=false;
			iter=0;
		}

		public void run(){
			for(int i=0;i<iter;i++){
				if(run)
					getPid().setSetpoint((int) start);
				if(run)
					try {Thread.sleep((long) (time*1000));} catch (InterruptedException e) {}
				if(run)
					getPid().setSetpoint((int) middle);
				if(run)
					try {Thread.sleep((long) (time*1000));} catch (InterruptedException e) {}
				if(run)
					getPid().setSetpoint((int) end);
				if(run)
					try {Thread.sleep((long) (time*1000));} catch (InterruptedException e) {}
				if(run)
					getPid().setSetpoint((int) middle);
				if(run)
					try {Thread.sleep((long) (time*1000));} catch (InterruptedException e) {}
				if(run)
					getPid().setSetpoint((int) start);
				if(run)
					try {Thread.sleep((long) (time*1000));} catch (InterruptedException e) {}
			}
			System.out.println("Loop Done");
			stopLoop.setEnabled(false);
			runLoop.setEnabled(true);
			runSin.setEnabled(true);
		}
	}
}
