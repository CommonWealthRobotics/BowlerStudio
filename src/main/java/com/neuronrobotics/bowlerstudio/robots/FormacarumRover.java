package com.neuronrobotics.bowlerstudio.robots;

import java.util.ArrayList;

import com.neuronrobotics.addons.driving.AbstractRobotDrive;
import com.neuronrobotics.addons.driving.AckermanBotDriveData;
import com.neuronrobotics.addons.driving.AckermanBotVelocityData;
import com.neuronrobotics.addons.driving.AckermanConfiguration;
import com.neuronrobotics.addons.driving.AckermanDefaultKinematics;
import com.neuronrobotics.addons.driving.HokuyoURGDevice;
import com.neuronrobotics.addons.driving.IAckermanBotKinematics;
import com.neuronrobotics.sdk.addons.kinematics.ServoRotoryLink;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.dyio.peripherals.DigitalOutputChannel;
import com.neuronrobotics.sdk.pid.PIDChannel;
import com.neuronrobotics.sdk.pid.PIDCommandException;
import com.neuronrobotics.sdk.pid.PIDEvent;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class FormacarumRover extends AbstractRobotDrive {
	
	
	/**
	 * steeringAngle in radians
	 */
	protected double steeringAngle=0;
	ServoRotoryLink steering;
	PIDChannel drive;
	PIDChannel lSteer;
	PIDChannel rSteer;
	PIDChannel bSteer;
	
	boolean complexSteering=false;
	
	private IAckermanBotKinematics ak = new AckermanDefaultKinematics();
	private DigitalOutputChannel driveEnable;
	private DigitalOutputChannel driveDirection;
	private int currentEncoderReading;
	private DigitalOutputChannel driveThree;
	private HokuyoURGDevice laser;
	private ServoRotoryLink noding;
	private AckermanConfiguration akermanConfigs;
	
	public FormacarumRover(	PIDChannel drive,
						PIDChannel lSteer,
						PIDChannel rSteer,
						PIDChannel bSteer, 
						DigitalOutputChannel driveEnable, 
						DigitalOutputChannel driveDirection,
						DigitalOutputChannel driveThree,
						HokuyoURGDevice laser,
						ServoRotoryLink noding,
						AckermanConfiguration akermanConfigs) {
		this.driveThree = driveThree;
		this.laser = laser;
		this.noding = noding;
		this.akermanConfigs = akermanConfigs;
		ak=new AckermanDefaultKinematics(akermanConfigs);
		this.driveEnable = driveEnable;
		this.driveDirection = driveDirection;
		setPIDChanel(drive);

		this.lSteer=lSteer;
		this.rSteer=rSteer;
		this.bSteer=bSteer;
		complexSteering=true;
		SetDriveVelocity(0);
	}
	
	public void setNodAngle(double  angle){
		noding.setTargetAngle(angle);
	}
	
	public double getNodAngle(){
		return noding.getTargetAngle();
	}
	
	
	protected void setPIDChanel(PIDChannel d){
		drive=d;
		drive.addPIDEventListener(this);
	}
	
	public void setSteeringHardwareAngle(double s) {
		if(complexSteering==false){
			steering.setTargetAngle(s);
			steering.flush(0);
			
		}else{
			this.lSteer.SetPIDSetPoint(0, 0);
			this.rSteer.SetPIDSetPoint(0, 0);
			this.bSteer.SetPIDSetPoint((int)(s* akermanConfigs.getSteerAngleToServo()), 0);
		}
	}
	
	public void setSteeringAngle(double s) {
		steeringAngle = s;
		setSteeringHardwareAngle(s);
	}
	
	public void setDriveData(AckermanBotDriveData d) {
		ResetDrivePosition();
		
		setSteeringAngle(d.getSteerAngle());
		SetDriveDistance(d.getTicksToTravil(), d.getSecondsToTravil());
	}
	public void setVelocityData(AckermanBotVelocityData d) {
		ResetDrivePosition();
		setSteeringAngle(d.getSteerAngle());
		SetDriveVelocity((int) d.getTicksPerSecond());
	}
	
	
	public double getSteeringAngle() {
		return steeringAngle;
	}
	protected void SetDriveDistance(int ticks, double seconds){
		Log.debug("Seting PID set point of= "+ticks+" currently at "+currentEncoderReading);
		drive.SetPIDSetPoint(ticks, seconds);
//		SetDriveVelocity((int) (ticks/seconds));
//		ThreadUtil.wait((int) (seconds*1000));
//		SetDriveVelocity(0);
//		Log.debug("Arrived at= "+currentEncoderReading);
	}
	
	public void SetDriveVelocity(int ticksPerSecond){
		Log.debug("Seting PID Velocity set point of="+ticksPerSecond);
		if(ticksPerSecond>0){
			driveDirection.setHigh(false);
			driveEnable.setHigh(false);
		}if(ticksPerSecond==0){
			driveDirection.setHigh(false);//doesnt matter, stopped
			driveEnable.setHigh(true);
		}else{
			driveDirection.setHigh(true);
			driveEnable.setHigh(false);
		}
		try {
			drive.SetPDVelocity(ticksPerSecond, 0);
		} catch (PIDCommandException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	protected void ResetDrivePosition(){
		//Log.enableDebugPrint(true);
		
		drive.ResetPIDChannel(0);
		ThreadUtil.wait((200));
		//Log.enableDebugPrint(false);
	}
	
	@Override
	public void DriveStraight(double cm, double seconds) {
		setDriveData(ak.DriveStraight(cm, seconds));
	}
	@Override
	public void DriveArc(double cmRadius, double degrees, double seconds) {
		setDriveData(ak.DriveArc(cmRadius, degrees, seconds));
	}
	@Override
	public void DriveVelocityStraight(double cmPerSecond) {
		setVelocityData(ak.DriveVelocityStraight(cmPerSecond));
	}
	@Override
	public void DriveVelocityArc(double degreesPerSecond, double cmRadius) {
		setVelocityData(ak.DriveVelocityArc(degreesPerSecond, cmRadius));
	}

	
	public double getMaxTicksPerSecond() {
		return ak.getMaxTicksPerSeconds();
	}

	@Override
	public void onPIDEvent(PIDEvent e) {
		currentEncoderReading = e.getValue();
		setRobotLocationUpdate(ak.onPIDEvent(e,getSteeringAngle()));
	}

	@Override
	public void onPIDReset(int group, int currentValue) {
		System.out.println("Resetting PID");
		ak.onPIDReset(currentValue);
	}

	@Override
	public boolean isAvailable() {
		// TODO Auto-generated method stub
		return drive.isAvailable();
	}

	public void setIAckermanKinematics(IAckermanBotKinematics ak) {
		this.ak = ak;
	}

	public IAckermanBotKinematics getAckermanKinematics() {
		return ak;
	}

	@Override
	public void disconnectDeviceImp() {
		// TODO Auto-generated method stub
		drive.removePIDEventListener(this);
	}

	@Override
	public boolean connectDeviceImp() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ArrayList<String> getNamespacesImp() {
		// TODO Auto-generated method stub
		return null;
	}



}
