package com.neuronrobotics.nrconsole.plugin.BowlerCam;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

import com.neuronrobotics.sdk.bowlercam.device.BowlerCamDevice;
import com.neuronrobotics.sdk.bowlercam.device.IWebcamImageListener;
import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class BowlerCamPanel extends JPanel implements IWebcamImageListener {
	/**
	 * long 
	 */
	private static final long serialVersionUID = 8710018539364791015L;
	private BowlerCamDevice cam = new BowlerCamDevice();
	private JPanel directPanel = new JPanel(new MigLayout());
	private JPanel bcPanel = new JPanel(new MigLayout());
	private JPanel images = new JPanel(new MigLayout());
	private JPanel controls = new JPanel(new MigLayout());
	private JPanel sliders = new JPanel(new MigLayout());
	private JLabel fps	= new JLabel("FPS: ");
	private JLabel thr	= new JLabel("");
	
	private RGBSlider target = new RGBSlider("Target Color");
	//private RGBSlider vector = new RGBSlider("Vector Color");
	private JSlider threshhold = new JSlider(SwingConstants.HORIZONTAL, 0, 255, 104);
	private JFormattedTextField min = new JFormattedTextField(NumberFormat.getNumberInstance());
	private JFormattedTextField max = new JFormattedTextField(NumberFormat.getNumberInstance());
	private JFormattedTextField scale = new JFormattedTextField(NumberFormat.getNumberInstance());
	private JCheckBox within = new JCheckBox("Within Threshhold");
	private JButton update = new JButton("Update Processor");
	private BufferedImage unaltered=null;
	private BufferedImage processedIm=null;
	private long time;
	double scaleSet = 1;
	
	public BowlerCamPanel() {
		setName("Bowler Camera");
		images.add(directPanel);
		images.add(bcPanel);
		bcPanel.addMouseListener(new MouseListener() {
			
			public void mouseReleased(MouseEvent arg0) {}
			
			public void mousePressed(MouseEvent arg0) {}
			
			public void mouseExited(MouseEvent arg0) {}
			
			public void mouseEntered(MouseEvent arg0) {}
			
			public void mouseClicked(MouseEvent arg0) {
				Color cl = new Color(unaltered.getRGB(arg0.getX(), arg0.getY()));
				getTargetColor().setColor(cl);
			}
		});
		
		sliders.add(fps,"wrap");
		target.setColor(33,240, 246);
		sliders.add(target);
		controls.add(new JLabel("Threshhold"),"wrap");
		controls.add(threshhold);
		controls.add(thr,"wrap");
		thr.setText(new Integer(threshhold.getValue()).toString());
		controls.add(within,"wrap");
		within.setSelected(true);
		
		controls.add(new JLabel("Image Scale"));
		controls.add(scale,"wrap");
		scale.setText(new Double(scaleSet).toString());
		
		min.setText("5");
		max.setText("100000");
		controls.add(new JLabel("Minimum pixles per blob"));
		controls.add(min,"wrap");
		controls.add(new JLabel("Maximum pixles per blob"));
		controls.add(max,"wrap");
		update.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				int minimum= Integer.parseInt(min.getText());
				int maximum= Integer.parseInt(max.getText());
				cam.updateFilter(target.getColor(), threshhold.getValue(), within.isSelected(),minimum, maximum);
			}
		});
		controls.add(update);
		
		JPanel tmp2 = new JPanel(new MigLayout());
		tmp2.add(sliders);
		tmp2.add(controls);
		add(tmp2,"wrap");
		add(images,"wrap");
	}
	
	public boolean setConnection(BowlerCamDevice connection) {
		try {
			cam=connection;
			cam.addWebcamImageListener(this);
			cam.startHighSpeedAutoCapture(0,scaleSet,5);
			cam.startHighSpeedAutoCapture(1,scaleSet,5);
			new displayThread().start();
		}catch(Exception ex) {
			return false;
		}
		return true;
	}
	
	
	private void displayImage() {
		thr.setText(new Integer(threshhold.getValue()).toString());
		target.getColor();
		if(scaleSet != Double.parseDouble(scale.getText())){
			//System.out.println("Resetting scale : "+scale.getText());
			scaleSet = Double.parseDouble(scale.getText());
			cam.startHighSpeedAutoCapture(0,scaleSet,0);
		}
		updateImage(unaltered	,bcPanel);
		updateImage(processedIm,directPanel);
	}
	
	protected RGBSlider getTargetColor() {
		return target;
	}
	private void updateImage(BufferedImage imageUpdate, JPanel p){
		if(imageUpdate ==null)
			return;
		p.removeAll();
		JLabel l = new JLabel();
		l.setIcon(new ImageIcon(imageUpdate));
		p.add(l);
		p.invalidate();
	}
	
	public void onNewImage(int camera,BufferedImage image) {
		//System.out.println("Got image: "+camera+" at "+System.currentTimeMillis());
		if(camera == 0){
			double s=((double)(System.currentTimeMillis()-time))/1000.0;
			fps.setText("FPS: "+(int)(1/(s)));
			time = System.currentTimeMillis();
			unaltered=image;
			//process();
			//displayImage();
		}
		if(camera == 1){
			processedIm=image;
		}
		
	}
	private class displayThread extends Thread{
		public void run() {
			while(cam.isAvailable()) {
				ThreadUtil.wait(200);
				displayImage();
			}
			cam.disconnect();
			cam.stopAutoCapture(0);
			cam.stopAutoCapture(1);
			//System.out.println("Bowler cam exiting");
		}
	}
}
