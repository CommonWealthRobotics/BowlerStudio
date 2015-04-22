package com.neuronrobotics.nrconsole.plugin.DyIO;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import net.miginfocom.swing.MigLayout;

import com.neuronrobotics.sdk.dyio.DyIOChannelEvent;
import com.neuronrobotics.sdk.dyio.DyIOChannelMode;
import com.neuronrobotics.sdk.dyio.IChannelEventListener;

public class ChannelPanel extends JPanel implements IChannelEventListener, MouseListener, ActionListener {

	
	private static final long serialVersionUID = 1L;

	private boolean selected;
	private ImageIcon image;
	private DyIOChannelMode mode = DyIOChannelMode.OFF;
	private ChannelPanelStatus status = ChannelPanelStatus.DEFAULT;
	private Timer timer = new Timer(150, this);
	private ChannelManager manager;
	private boolean leftAligned = true;
	
	public ChannelPanel(ChannelManager m) {
		setManager(m);
	    initPanel();
	}

	private void initPanel() {
		setLayout(new MigLayout());
		
		setStatus(ChannelPanelStatus.DEFAULT);
		
		addMouseListener(this);
		
		Dimension size = new Dimension(image.getIconWidth(), image.getIconHeight());
	    setSize(size);
	    setMaximumSize(size);
	    setMinimumSize(size);
	    setPreferredSize(size);
	    
	    timer.stop();
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public void setSelected(boolean isSelected) {
		selected = isSelected;
		setStatus(selected?ChannelPanelStatus.SELECTED:ChannelPanelStatus.DEFAULT);
	}
	
	public ChannelPanelStatus getStatus() {
		return status;
	}
	
	public void setStatus(ChannelPanelStatus mode) {
		image = new ImageIcon(DyIOPanel.class.getResource(mode.getPath()));
		
		revalidate();
		repaint();
	}
	
	public DyIOChannelMode getMode() {
		return mode;
	}
	
	public void setMode(DyIOChannelMode mode) {	
		removeAll();
		ImageIcon image; 
		try {
			image = new ImageIcon(DyIOPanel.class.getResource("images/icon-" + mode.toSlug() + ".png"));
		}catch (NullPointerException e) {
			image = new ImageIcon(DyIOPanel.class.getResource("images/icon-off.png"));
		}
		add(new JLabel(image), leftAligned ? "pos 52 2" : "pos 3 2");
		revalidate();
		repaint();
	}
	
	public void setAlignedLeft(boolean leftAligned) {
		this.leftAligned = leftAligned;
		setMode(manager.getChannel().getCurrentMode());
	}
	
	
	public void paintComponent (Graphics g) {
    	super.paintComponent(g);
    	
    	try {
    		g.drawImage(image.getImage(), 0,0,this.getWidth(),this.getHeight(),this);
    	} catch (Exception e) {
    		
    	}
    }

	
	public void onChannelEvent(DyIOChannelEvent e) {
		setStatus(ChannelPanelStatus.UPDATE);
		timer.restart();
	}
	
	
	public void mouseClicked(MouseEvent e) {
		selected=true;
		setStatus(ChannelPanelStatus.HIGHLIGHT);
		getManager().fireOnClick(e);
	}

	
	public void mouseEntered(MouseEvent e) {
		if(!selected) {
			setStatus(ChannelPanelStatus.HIGHLIGHT);
		}
	}

	
	public void mouseExited(MouseEvent e) {
		if(!selected) {
			setStatus(ChannelPanelStatus.DEFAULT);
		}
	}
	
	
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	
	public void actionPerformed(ActionEvent e) {
		if(getStatus() != ChannelPanelStatus.UPDATE) {
			if(selected)
				setStatus(ChannelPanelStatus.SELECTED);
			else
				setStatus(ChannelPanelStatus.DEFAULT);
		}
		timer.stop();
	}

	public void setManager(ChannelManager manager) {
		setMode(manager.getChannel().getCurrentMode());
		this.manager = manager;
	}

	public ChannelManager getManager() {
		return manager;
	}
}
