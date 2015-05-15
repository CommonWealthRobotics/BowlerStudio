package com.neuronrobotics.nrconsole.plugin.PID;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import net.miginfocom.swing.MigLayout;

import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.pid.IPIDEventListener;
import com.neuronrobotics.sdk.pid.PDVelocityConfiguration;
import com.neuronrobotics.sdk.pid.PIDConfiguration;
import com.neuronrobotics.sdk.pid.PIDEvent;
import com.neuronrobotics.sdk.pid.PIDLimitEvent;

public class PIDControlWidget extends JPanel implements IPIDEventListener,ActionListener {
	private static final long serialVersionUID = 3L;
	private final int retry = 5;
	private JTextField kp=new JTextField(10);
	private JTextField ki=new JTextField(10);
	private JTextField kd=new JTextField(10);
	private JTextField vkp=new JTextField(10);
	private JTextField vkd=new JTextField(10);
	private JTextField upHys=new JTextField(10);
	private JTextField lowHys=new JTextField(10);
	
	private JTextField indexLatch=new JTextField(10);
	private JCheckBox  inverted =new JCheckBox("Invert control");
	private JCheckBox  useLatch =new JCheckBox("Use Latch");
	private JCheckBox  stopOnLatch =new JCheckBox("Stop On Latch");
	private JPanel latchPanel = new JPanel(new MigLayout());
	private JButton  pidSet = new JButton("Configure");
	private JButton  pidStop = new JButton("Stop");
	private JTextField setpoint=new JTextField(new Double(0).toString(),5);
	private JButton  setSetpoint = new JButton("Set Setpoint");
	private JButton  zero = new JButton("Zero PID");
	private JLabel   currentPos = new JLabel("0");
	private AdvancedPIDWidget advanced =null;
	
	private JPanel pidRunning = new JPanel(new MigLayout());
	
	private PIDGraph graph;
	
	private boolean set = false;
	
	private PIDControl tab;

	private int group;
	private PIDConfiguration pidconfig; 
	private PDVelocityConfiguration velconfig; 
	private int setpointValue;
	private int positionValue;
	public PIDControlWidget(int group, int startValue, PIDControl tab) {
//		if(group==0)
//			Log.enableDebugPrint(true);
		setBorder(BorderFactory.createRaisedBevelBorder());
		tab.getPidDevice().addPIDEventListener(this);
		currentPos.setText(new Integer(startValue).toString());
		setpointValue=startValue;
		setPositionDisplay(startValue);
		setLayout(new MigLayout("", "[]", "[][][]"));
		setGui(tab);
		setGroup(group);
		getPIDConfiguration();
	    inverted.setSelected(true);
	    
		getPidSet().addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				double p=0,i=0,d=0,l=0,vp,vd,up,low;
				try{
					p=Double.parseDouble(kp.getText());
				}catch(Exception e){
					kp.setText(new Double(1).toString());
					showMessage( "Bad PID values, resetting.",e);
					return;
				}
				try{
					i=Double.parseDouble(ki.getText());
				}catch(Exception e){
					ki.setText(new Double(0).toString());
					showMessage( "Bad PID values, resetting.",e);
					return;
				}
				try{
					d=Double.parseDouble(kd.getText());
				}catch(Exception e){
					kd.setText(new Double(0).toString());
					showMessage( "Bad PID values, resetting.",e);
					return;
				}
				try{
					l=Double.parseDouble(indexLatch.getText());
				}catch(Exception e){
					indexLatch.setText(new Double(0).toString());
					showMessage( "Bad PID values, resetting.",e);
					return;
				}
				try{
					vp=Double.parseDouble(vkp.getText());
				}catch(Exception e){
					vkp.setText(new Double(.1).toString());
					showMessage( "Bad PID values, resetting.",e);
					return;
				}
				try{
					vd=Double.parseDouble(vkd.getText());
				}catch(Exception e){
					vkd.setText(new Double(0).toString());
					showMessage( "Bad PID values, resetting.",e);
					return;
				}
				try{
					up=Double.parseDouble(upHys.getText());
				}catch(Exception e){
					upHys.setText(new Double(0).toString());
					showMessage( "Bad PID values, resetting.",e);
					return;
				}
				try{
					low=Double.parseDouble(lowHys.getText());
				}catch(Exception e){
					lowHys.setText(new Double(0).toString());
					showMessage( "Bad PID values, resetting.",e);
					return;
				}
				setPID(p, i, d,vp,vd,l, useLatch.isSelected(),  stopOnLatch.isSelected(),up,low);
				int cur = GetPIDPosition();
				//System.out.println("Current position="+cur+" group="+getGroup());
				setSetpoint(cur);
				setPositionDisplay(cur);
				//pidRunning.setVisible(true);
			}
		});

		getPidStop().setEnabled(false);
		getPidStop().addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				stopPID(true);
			}
		});
		zero.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				ResetPIDChannel();
				int val = GetPIDPosition();
				setSetpoint(val);
				currentPos.setText(new Integer(val).toString());
			}
		});
		
		useLatch.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(useLatch.isSelected()){
					latchPanel.setVisible(true);
				}else{
					stopOnLatch.setSelected(false);
					latchPanel.setVisible(false);
				}
			}
		});
		
		setpoint.setText(new Integer(startValue).toString());
		setpoint.addActionListener(this);
		setSetpoint.addActionListener(this);
		
		
		populatePID();
	    
		JPanel constants = new JPanel(new MigLayout());
		constants.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
	    constants.setMinimumSize(new Dimension(300, 50));
	    constants.add(new JLabel("PID Position Gain Constants"),"wrap");
		constants.add(new JLabel("proportional (Kp)"));
	    constants.add(kp,"wrap");
	    constants.add(new JLabel("integral (Ki)"));
	    constants.add(ki,"wrap");
	    constants.add(new JLabel("derivitive (Kd)"));
	    constants.add(kd,"wrap");
	    
	    
	    
	    constants.add(new JLabel("PD Velocity Gain Constants"),"wrap");
		constants.add(new JLabel("proportional (Kp)"));
	    constants.add(vkp,"wrap");
	    constants.add(new JLabel("derivitive (Kd)"));
	    constants.add(vkd,"wrap");
	    
	    constants.add(new JLabel("PID Hysterisys"),"wrap");
		constants.add(new JLabel("Upper Bound"));
	    constants.add(upHys,"wrap");
	    constants.add(new JLabel("Lower Bound"));
	    constants.add(lowHys,"wrap");
	    
	    constants.add(useLatch,"wrap");
	    
	    latchPanel .add(stopOnLatch,"wrap");
	    latchPanel .add(new JLabel("Index Latch Value"));
	    latchPanel .add(indexLatch,"wrap");
	    constants.add(latchPanel,"wrap");
	    if(!useLatch.isSelected()){
	    	latchPanel.setVisible(false);
	    	stopOnLatch.setSelected(false);
	    }
	    constants.add(getPidSet());
	    constants.add(inverted);
	    
	    
	    
	    pidRunning.add(new JLabel("PID Running for group "+((int)getGroup())),"wrap");
	    pidRunning.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
	    //pidRunning.add(pidSet);
	    pidRunning.add(getPidStop(),"wrap");
	    //pidRunning.add(inverted);
	    pidRunning.add(zero,"wrap");
	    pidRunning.add(setSetpoint);
	    pidRunning.add(setpoint,"wrap");
	    pidRunning.add(new JLabel("Current Position = "));
	    pidRunning.add(currentPos,"wrap");
	    
	    pidRunning.add(advanced,"wrap");
	    pidRunning.add(new PIDVelocityWidget(this));
	    
	    
	    JPanel uiPanel = new JPanel(new MigLayout());
	    if(getGui().isDyPID()) {
	    	uiPanel.add(new DyPIDControlWidget(this),"wrap");		
		}
	    uiPanel.add(constants,"wrap");
	    
	    JPanel config = new JPanel();
	    config.add(uiPanel);
	    config.add(pidRunning);
		
		
		graph = new PIDGraph(group);
		
		add(config,"cell 0 0");
		add(graph,"cell 0 1");
		
		repaint();
		Updater up = new Updater();
		up.start();
		
		if(getPIDConfiguration().isEnabled()){
			pidStop.setEnabled(true);
			//pidRunning.setVisible(true);
			graphVals();
		}else{
			pidStop.setEnabled(false);
			//pidRunning.setVisible(false);
		}
		
		
	}
	
	private void populatePID() {
		advanced = new  AdvancedPIDWidget(this);
		getPIDConfiguration();
		kp.setText(new Double(getPIDConfiguration().getKP()).toString());
		ki.setText(new Double(getPIDConfiguration().getKI()).toString());
		kd.setText(new Double(getPIDConfiguration().getKD()).toString());
		
		upHys.setText(new Double(getPIDConfiguration().getUpperHystersys()).toString());
		lowHys.setText(new Double(getPIDConfiguration().getLowerHystersys()).toString());
		
		vkp.setText(new Double(getPDVelocityConfiguration().getKP()).toString());
		vkd.setText(new Double(getPDVelocityConfiguration().getKD()).toString());
		indexLatch.setText(new Double(getPIDConfiguration().getIndexLatch()).toString());
	    useLatch.setSelected(getPIDConfiguration().isUseLatch());
	    stopOnLatch.setSelected(getPIDConfiguration().isStopOnIndex());
		inverted.setSelected(getPIDConfiguration().isInverted());

	}
	

	public void setGroup(int group) {
		this.group = group;
	}
	public int getGroup() {
		return group;
	}
	public void setGui(PIDControl tab) {
		this.tab = tab;
	}
	public PIDControl getGui() {
		return tab;
	}
	public void stopPID(boolean b){
		getPidStop().setEnabled(false);
		getPIDConfiguration().setEnabled(false);
		if(b)
			ConfigurePIDController();
		//pidRunning.setVisible(false);
	}
	private void setPID(double p,double i,double d,double vp,double vd, double latch, boolean use, boolean stop, double up, double low){
		//setSet(true);
		getPidStop().setEnabled(true);
		getPIDConfiguration().setEnabled(true);
		getPIDConfiguration().setInverted(inverted.isSelected());
		getPIDConfiguration().setAsync(true);
		getPIDConfiguration().setKP(p);
		getPIDConfiguration().setKI(i);
		getPIDConfiguration().setKD(d);
		getPIDConfiguration().setIndexLatch(latch);
		getPIDConfiguration().setUseLatch(use);
		getPIDConfiguration().setStopOnIndex(stop);
		getPIDConfiguration().setUpperHystersys(up);
		getPIDConfiguration().setLowerHystersys(low);
		getPIDConfiguration().setHystersysStop((up+low)/2);
		getPDVelocityConfiguration().setKP(vp);
		getPDVelocityConfiguration().setKD(vd);
		ConfigurePIDController();

	}
//	public void setSet(boolean set) {
//		this.set = set;
//	}
//	public boolean isReady() {
//		return set;
//	}
//	
	
	public String toString() {
		return "GROUP # "+(int)getGroup();
	}

	
	public void onPIDEvent(PIDEvent e) {
		if(e.getGroup()==getGroup()){
			Log.info("From PID control widget: "+e);
			setPositionDisplay(e.getValue());	
		}
	}
	
	public void setInternalSetpoint(int setPoint){
		//System.out.println("Setting setpoint on group="+getGroup()+" value="+setPoint);
		setpointValue=setPoint;
		setpoint.setText(new Integer(setPoint).toString());
		graphVals();
		getPidStop().setEnabled(true);
	}
	
	public void setSetpoint(int setPoint){
		SetPIDSetPoint(setPoint,0);
		setInternalSetpoint(setPoint);
	}
	private class Updater extends Thread{
		long lastSet;
		long lastPos;
		public void run() {
			while(true) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
				if(lastSet != setpointValue || lastPos !=getPositionValue() ) {
					graphVals();
					lastSet = setpointValue ;
					lastPos = getPositionValue();
				}
			}
		}
	}
	private void graphVals() {
		if(graph!=null)
			graph.addEvent(setpointValue,getPositionValue());
	}

	public void setPositionDisplay(int positionValue) {
		currentPos.setText(new Integer(positionValue).toString());
		this.positionValue = positionValue;
		graphVals();
	}
	public int getSetPoint() {
		return Integer.parseInt(setpoint.getText());
	}
	public int getPositionValue() {
		return positionValue;
	}

	
	public void onPIDReset(int group, int currentValue) {
		// TODO Auto-generated method stub
		if(group==getGroup()){
			setPositionDisplay(currentValue);
			setInternalSetpoint(currentValue);
		}
	}

	
	public void onPIDLimitEvent(PIDLimitEvent e) {
		if(e.getGroup() == getGroup()){
			System.out.println("Limit event: "+e);
			//if(e.getLimitType() == PIDLimitEventType.INDEXEVENT){
				setInternalSetpoint(e.getValue());
				setPositionDisplay(e.getValue());
			//}
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		try{
			setSetpoint(getSetPoint());
		}catch(Exception e){
			setpoint.setText(new Integer(0).toString());
			return;
		}
	}
	
	private class messageShower extends Thread{
		String message;
		public messageShower (String s){
			message=s;
		}
		public void run(){
			JOptionPane.showMessageDialog(null,  message, "PID Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	private void showMessage(String s,Exception e){
		new messageShower(s+", Message: "+e.getMessage()).start();
	}

	private void ResetPIDChannel(){
		Exception ex = new Exception();
		for(int i=0;i<retry;i++){
			try{
				getGui().getPidDevice().ResetPIDChannel(getGroup(), 0);
				
				return;
			}catch(Exception e){
				ex=e;
			}
		}
		showMessage("Setpoint reset failed "+retry+"times on group #"+getGroup(),ex);
        ex.printStackTrace();
		return;
		
	}
	private int GetPIDPosition(){
		Exception ex = new Exception();
		for(int i=0;i<retry;i++){
			try{
				return getGui().getPidDevice().GetPIDPosition(getGroup());
			}catch(Exception e){
				ex=e;
			}
		}
		showMessage("Setpoint get failed "+retry+"times on group #"+getGroup(),ex);
        ex.printStackTrace();
		return 0;
	}
	private PDVelocityConfiguration getPDVelocityConfiguration(){
		Exception ex = new Exception();
		for(int i=0;i<retry;i++){
			try{
				if(velconfig==null){
					velconfig = getGui().getPidDevice().getPDVelocityConfiguration(getGroup());
				}
				velconfig.setGroup(getGroup());
				return velconfig;
			}catch(Exception e){
				ex=e;
			}
		}
		showMessage("Configuration get failed "+retry+"times on group #"+getGroup(),ex);
        ex.printStackTrace();
        velconfig =new PDVelocityConfiguration();
        velconfig.setGroup(getGroup());
		return velconfig;
	}
	private PIDConfiguration getPIDConfiguration(){
		Exception ex = new Exception();
		for(int i=0;i<retry;i++){
			try{
				if(pidconfig==null){
					pidconfig = getGui().getPidDevice().getPIDConfiguration(getGroup());
				}
				pidconfig.setGroup(getGroup());
				return pidconfig;
			}catch(Exception e){
				ex=e;
			}
		}
		showMessage("Configuration get failed "+retry+"times on group #"+getGroup(),ex);
        ex.printStackTrace();
        pidconfig =new PIDConfiguration();
        pidconfig.setGroup(getGroup());
		return pidconfig;
	}
	private void ConfigurePIDController(){
		Exception ex = new Exception();
		for(int i=0;i<retry;i++){
			try{
				getGui().getPidDevice().ConfigurePIDController(getPIDConfiguration());
				getGui().getPidDevice().ConfigurePDVelovityController(getPDVelocityConfiguration());
				//throw new RuntimeException("");
				return;
			}catch(Exception e){
				ex=e;
			}
		}
		showMessage( "Configuration Set failed "+retry+"times on group #"+getGroup(),ex);
        ex.printStackTrace();
	}
	private void SetPIDSetPoint(int setPoint,int velocity){

		Exception ex = new Exception();
		for(int i=0;i<retry;i++){
			try{
				getGui().getPidDevice().SetPIDSetPoint(getGroup(), setPoint,velocity);
				return;
			}catch(Exception e){
				ex=e;
			}
		}
		showMessage( "Setpoint set failed "+retry+"times on group #"+getGroup(),ex);
        ex.printStackTrace();
	}
	
	public void SetPIDVel(int velocity,double seconds){
		Exception ex = new Exception();
		for(int i=0;i<retry;i++){
			try{
				getGui().getPidDevice().SetPDVelocity(getGroup(),velocity,seconds);
				return;
			}catch(Exception e){
				ex=e;
			}
		}
		showMessage( "Velocity set failed "+retry+"times on group #"+getGroup(),ex);
        ex.printStackTrace();
	}

	public void setPidSet(JButton pidSet) {
		this.pidSet = pidSet;
	}

	public JButton getPidSet() {
		return pidSet;
	}

	public void setPidStop(JButton pidStop) {
		this.pidStop = pidStop;
	}

	public JButton getPidStop() {
		return pidStop;
	}
	

}
