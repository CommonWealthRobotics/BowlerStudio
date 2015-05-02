package com.neuronrobotics.nrconsole.plugin.DyIO.hexapod;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.basic.BasicArrowButton;

import net.miginfocom.swing.MigLayout;

import com.neuronrobotics.sdk.addons.walker.BasicWalker;

public class HexapodTester extends JPanel implements KeyListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3919587326215507835L;
	JTextField typingArea;
    private int key;
    private boolean ctrl = false;
    private HexapodConfigPanel display;
    private boolean running = false;
    private JButton stop =new JButton("Stop Test");
    private BasicArrowButton up = new BasicArrowButton(BasicArrowButton.NORTH);
    private BasicArrowButton down = new BasicArrowButton(BasicArrowButton.SOUTH);
    private BasicArrowButton turnLeft = new BasicArrowButton(BasicArrowButton.WEST);
    private BasicArrowButton turnRight = new BasicArrowButton(BasicArrowButton.EAST);
    private JTextField loopTime = new JTextField(5);
    private JTextField fwdInc = new JTextField(5);
    private JTextField degInc = new JTextField(5);
    public HexapodTester(){
    	init();
    	setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
    }
    public void setup(HexapodConfigPanel display){
    	this.display=display;
    }
    
	public double getLoopTime() {
		try{
			return new Double(loopTime.getText());
		}catch(Exception e){
			return .2;
		}
	}
    private double getTurnDeg(){
    	try{
			return new Double(degInc.getText());
		}catch(Exception e){
			return 5;
		}
    }
    private double getXinc(){
    	return .2;
    }
    private double getYinc(){
    	try{
			return new Double(fwdInc.getText());
		}catch(Exception e){
			return .5;
		}
    }
    public void run(){
    	if(isRunning())
    		return;
    	new Thread(){
    		public void run(){
    			//System.out.println("Starting test thread..");
    			setRunning(true);
    			getWalker().initialize();
    			//System.out.println("Default: "+BasicWalkerConfig.getDefaultConfiguration());
    			display.redisplay();
    			long time;
    			
    			while (isRunning()) {
    				time = System.currentTimeMillis();
    				switch(key) {
    				case 0:
    					updateWindow("stop");
    					break;
    				case 38:
    					updateWindow("forward");
    					getWalker().incrementAllY(-1*getYinc(), getLoopTime());
    					break;
    				case 40:
    					updateWindow("backward");
    					getWalker().incrementAllY(getYinc(), getLoopTime());
    					break;
    				case 37:
    					if(ctrl) {
    						updateWindow("straif left");
    						getWalker().incrementAllX(-1*getXinc(), getLoopTime());
    					}
    					else {
    						updateWindow("turn left");
    						getWalker().turnBody(getTurnDeg(), getLoopTime());
    					}
    					break;
    				case 39:
    					if(ctrl) {
    						updateWindow("straif right");
    						getWalker().incrementAllX(getXinc(), getLoopTime());
    					}
    					else {
    						updateWindow("turn right");
    						getWalker().turnBody(-1*getTurnDeg(), getLoopTime());
    					}
    					break;
    				}
    				
    				
					try {
						long sleepTime=(long) ((getLoopTime()*1000)-(System.currentTimeMillis()-time));
						if(sleepTime>0){
							Thread.sleep(sleepTime);
						}
					} catch (InterruptedException e) {
    							
    				}
    			}
    			setRunning(false);
    			//System.out.println("Stopping test thread");
    			typingArea.setText("inactave..");
    			getWalker().initialize();
    			display.redisplay();
    		}
    	}.start();
    }
	public HexapodTester(BasicWalker walker, HexapodConfigPanel display){
		setup(display);

	}
	private void updateWindow(String text){
		typingArea.setText(text);
		if(!text.contains("stop"))
			display.redisplay();
	}
	private void init() {
		typingArea = new JTextField(13);
		typingArea.setText("inactave..");
		typingArea.addKeyListener(this);
		loopTime.setText(".2");
		fwdInc.setText(".2");
		degInc.setText("5");
		
		stop.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				setRunning(false);
				//System.out.println("Stopping test");
			}
		});
		setLayout(new MigLayout());
		add(typingArea,"wrap");
		add(new JLabel("Loop Time Increment"));
		add(loopTime);
		add(new JLabel("Seconds"),"wrap");
		add(new JLabel("Fwd/Bkd Increment"));
		add(fwdInc);
		add(new JLabel("Inches"),"wrap");
		add(new JLabel("Turn Increment"));
		add(degInc);
		add(new JLabel("Degrees"),"wrap");
		JPanel buttons = new JPanel(new MigLayout());
		up.addMouseListener(new MouseListener() {
			
			
			public void mouseReleased(MouseEvent arg0) {
				key = 0;
			}
			
			
			public void mousePressed(MouseEvent arg0) {
				key = 38;
			}
			
			
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
//		up.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				System.out.println(e.getActionCommand());
//				key = 38;
//			}
//		});
		down.addMouseListener(new MouseListener() {
			
			
			public void mouseReleased(MouseEvent arg0) {
				key = 0;
			}
			
			
			public void mousePressed(MouseEvent arg0) {
				key = 40;
			}
			
			
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		turnLeft.addMouseListener(new MouseListener() {
			
			
			public void mouseReleased(MouseEvent arg0) {
				key = 0;
			}
			
			
			public void mousePressed(MouseEvent arg0) {
				key = 37;
			}
			
			
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		turnRight.addMouseListener(new MouseListener() {
			
			
			public void mouseReleased(MouseEvent arg0) {
				key = 0;
			}
			
			
			public void mousePressed(MouseEvent arg0) {
				key = 39;
			}
			
			
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		JPanel turn = new JPanel();
		buttons.add(up,"align center,wrap");
		turn.add(turnLeft);
		turn.add(turnRight);
		buttons.add(turn,"wrap");
		buttons.add(down,"align center,wrap");
		add(buttons,"wrap");
		add(stop,"wrap");
	}
	
	
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode()==17) {
			ctrl=true;
			return;
		}
		key=e.getKeyCode();
	}
	
	public void keyReleased(KeyEvent e) {
		switch(e.getKeyCode()) {
		case 38:
		case 40:
		case 37:
		case 39:
			key=0;
			break;
		case 17:
			ctrl=false;
		}
	}
	
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	public void setRunning(boolean running) {
		this.running = running;
	}
	public boolean isRunning() {
		return running;
	}

	public BasicWalker getWalker() {
		return display.getWalker();
	}


}
