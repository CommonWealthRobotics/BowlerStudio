package com.neuronrobotics.nrconsole.plugin.DyIO;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;

import net.miginfocom.swing.MigLayout;

//import com.neuronrobotics.nrconsole.NRConsoleWindow;

public class DyIOControlsPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private ArrayList<ControlPanel> controls = new ArrayList<ControlPanel>();
	private ControlPanelScroller components = new ControlPanelScroller();
	private JScrollPane scroller;
		
	public DyIOControlsPanel() {
		//setBorder(BorderFactory.createRaisedBevelBorder());
		components.setLayout(new MigLayout());

		scroller = new JScrollPane(components);
		add(scroller);
		fix();
	}
	
	public void setChannel(ControlPanel channel) {
		components.removeAll();
		controls.add(channel);
		components.add(channel, "wrap");
		
		fix();
	}
	
	public void addChannel(ControlPanel channel) {
		controls.add(channel);
		components.add(channel, "wrap");
		fix();
	}
	

	public void removeChannel(ControlPanel channel) {
		controls.remove(channel);
		components.remove(channel);
		fix();
	}
	
	private void fix(){
		repaint();
	}
	
	public void repaint(){
		super.repaint();
		if(components != null){
			components.revalidate();
			components.repaint();
		}
		
	}
	
	private class ControlPanelScroller extends JPanel implements Scrollable{
		private int maxUnitIncrement = 50;
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public ControlPanelScroller() {
			setOpaque(false);
		}
		


		public Dimension getPreferredScrollableViewportSize() {
			// TODO Auto-generated method stub
			//return new Dimension(ControlPanel.panelWidth+52,NRConsoleWindow.panelHight-110);
			//Dimension d=NRConsoleWindow.getNRWindowSize();
			return new Dimension(ControlPanel.panelWidth+52,768) ;
		}

		
		public int getScrollableBlockIncrement(Rectangle visibleRect, int arg1,int arg2) {
			// TODO Auto-generated method stub
			return maxUnitIncrement;
		}

		
		public boolean getScrollableTracksViewportHeight() {
			// TODO Auto-generated method stub
			return false;
		}

		
		public boolean getScrollableTracksViewportWidth() {
			// TODO Auto-generated method stub
			return false;
		}

		
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {

	       return maxUnitIncrement;
	    }		
	}
}
