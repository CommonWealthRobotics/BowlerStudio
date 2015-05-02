package com.neuronrobotics.nrconsole.plugin.BowlerCam;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

public class RGBSlider extends JPanel {
	JSlider r = new JSlider(SwingConstants.VERTICAL, 0, 255, 128);
	JSlider g = new JSlider(SwingConstants.VERTICAL, 0, 255, 128);
	JSlider b = new JSlider(SwingConstants.VERTICAL, 0, 255, 128);
	
	JLabel rl = new JLabel("128");
	JLabel gl = new JLabel("128");
	JLabel bl = new JLabel("128");
	ColorBox c = new ColorBox(Color.gray);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RGBSlider(String name){
		setLayout(new MigLayout());
		r.setMajorTickSpacing(15);
		g.setMajorTickSpacing(15);
		b.setMajorTickSpacing(15);
		
		r.setPaintTicks(true);
		g.setPaintTicks(true);
		b.setPaintTicks(true);
		
		JPanel rc = new JPanel(new MigLayout());
		JPanel gc = new JPanel(new MigLayout());
		JPanel bc = new JPanel(new MigLayout());
		
		rc.add(new JLabel("R"));
		rc.add(rl,"wrap");
		rc.add(r);
		
		gc.add(new JLabel("G"));
		gc.add(gl,"wrap");
		gc.add(g);
		
		bc.add(new JLabel("B"));
		bc.add(bl,"wrap");
		bc.add(b);
		
		JPanel slides = new JPanel(new MigLayout());
		slides.add(new JLabel(name),"wrap");
		slides.add(rc);
		slides.add(gc);
		slides.add(bc);
		
		add(slides,"wrap");
		add(c,"wrap");

	}
	public void setColor(int r,int g,int b){
		this.r.setValue(r);
		this.g.setValue(g);
		this.b.setValue(b);
		getColor();
	}
	public void setColor(Color c){
		this.r.setValue(c.getRed());
		this.g.setValue(c.getGreen());
		this.b.setValue(c.getBlue());
		getColor();
	}
	public Color getColor(){
		rl.setText(new Integer(r.getValue()).toString());
		gl.setText(new Integer(g.getValue()).toString());
		bl.setText(new Integer(b.getValue()).toString());
		Color now =new Color(r.getValue(),g.getValue(),b.getValue());
		c.setColor(now);
		setBackground(now);
		return now;
	}
    private class ColorBox extends JPanel
    {
        /**
		 * long 
		 */
		private static final long serialVersionUID = 1L;
		public ColorBox(Color backColor){
        	setSize(400, 400);
        	setColor(backColor);
       }
       public void setColor(Color c) {
    	   setBackground(c);
       }
        public void paintComponent(Graphics g){
          super.paintComponent(g);     	  
       }
    	
    }
}
