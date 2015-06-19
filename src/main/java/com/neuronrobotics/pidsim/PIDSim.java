package com.neuronrobotics.pidsim;


public class PIDSim {
	
	private GraphingPanel graphingPanel;
	private LinearPhysicsEngine phy;
	
	private double sp;
    private double position;
    private double mass = .01;
    private double length = 1;
    private double stFriction = .5;
    private double dyFriction = .3;
    private long time = 0;
	private double maxTorque = 20;// Newton meters
	private PIDConstantsDialog constants = new PIDConstantsDialog(	.1,//kp
			0,//ki
			0);//kd
    
    public PIDSim(LinearPhysicsEngine eng) {
    	graphingPanel = new GraphingPanel(this,constants, "Neuron Robotics PIDSim");
    	phy=eng;
    }
    
    public double getSetPoint() {
    	
    	return sp;
    }
    
    public void setSetPoint(double value) {
    	sp = value;
    	getGraphingPanel().setSetPoint(sp);
    }
    /**
     * setting a torque in kg/m
     * @param value
     * @throws Exception if max torque is exceded
     */
    public void setTorque(double value) throws Exception {
    	phy.setTorque(value);
    }
    
    public long getTime() {
    	return time;
    }
    
    protected void setTime(long t) {
    	time = t;
    }
    
    protected void setPosition(double value) {
    	position = value;
    	getGraphingPanel().setPosition(position);
    }
    
    public double getPosition() {
    	return position;
    }
    
	public double getMass() {
		return mass;
	}

	public void setMass(double value) {
		phy.setMass(mass);
		mass = value;
	}
	
	public double getLength() {
		return length;
	}

	public void setLength(double value) {
		phy.setLinkLen(value);
		length = value;
	}
	
	public double getStaticFriction() {
		return stFriction;
	}
	
	public void setStaticFriction(double value) {
		phy.setMuStatic(value);
		stFriction = value;
	}
	
	public double getDynamicFriction() {
		return dyFriction;
	}

	public void setDynamicFriction(double value) {
		phy.setMuDynamic(value);
		dyFriction = value;
	}

	public double getMaxTorque() {
		return maxTorque;
	}

	public void setMaxTorque(double maxTorque) {
		this.maxTorque = maxTorque;
	}

	public GraphingPanel getGraphingPanel() {
		return graphingPanel;
	}



	public PIDConstantsDialog getConstants() {
		return constants;
	}

	public void setConstants(PIDConstantsDialog constants) {
		this.constants = constants;
	}
}
