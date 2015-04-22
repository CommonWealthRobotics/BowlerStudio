package com.neuronrobotics.nrconsole.plugin.DyIO.channelwidgets;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.neuronrobotics.nrconsole.plugin.DyIO.ChannelManager;
import com.neuronrobotics.nrconsole.plugin.DyIO.GettingStartedPanel;
import com.neuronrobotics.sdk.common.BowlerDocumentationFactory;
import com.neuronrobotics.sdk.dyio.peripherals.DyIOAbstractPeripheral;
import com.neuronrobotics.sdk.dyio.peripherals.IPPMReaderListener;
import com.neuronrobotics.sdk.dyio.peripherals.PPMReaderChannel;

public class PPMReaderWidget extends ControlWidget implements IPPMReaderListener{

	private static final long serialVersionUID = 1L;
	private PPMReaderChannel ppmr;
	private JLabel [] ppmLabels = new JLabel[6] ;
	private JComboBox [] ppmLinks = new JComboBox [6] ;
	private int [] cross;
	private JPanel values = new JPanel(new MigLayout());
	public PPMReaderWidget(ChannelManager c) {
		super(c);
		
		try {
			ppmr = new PPMReaderChannel(getChannel());
			cross = ppmr.getCrossLink();
			int [] vals = ppmr.getValues();
			for(int i=0;i<ppmLabels.length;i++){
				ppmLabels[i]=new JLabel(new Integer(vals[i]).toString());
				ppmLinks[i] = new JComboBox();

				ppmLinks[i].addItem("None");
				for(int j=0;j<24;j++){
					ppmLinks[i].addItem(new Integer(j));
				}
				selectChan(i,cross[i]);
				ppmLinks[i].addActionListener(new linkListener(i));
				
				
				values.add(new JLabel("PPM "+new Integer(i)+" : "));
				values.add(ppmLabels[i]);
				values.add(ppmLinks[i],"wrap");
			}
			
			//Button to launch info page for PPMReaderChannel panel
			JButton helpButton = new JButton("Help");
			
			//Label for PPMReaderChannel Panel
			JLabel helpLabel = new JLabel("PPM R/C signal Panel");
			add(helpLabel, "split 2, span 2, align left");
			add(helpButton, "gapleft 200, wrap, align right");
			helpButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						GettingStartedPanel.openPage(BowlerDocumentationFactory.getDocumentationURL(ppmr));
					} catch (Exception exceptE) {}
				}
			});
			
			//Help button formating
			helpButton.setFont((helpButton.getFont()).deriveFont(8f));
			helpButton.setBackground(Color.green);
			
			//PPMReaderChannel Panel label formating
			helpLabel.setHorizontalTextPosition(JLabel.LEFT);
			helpLabel.setForeground(Color.GRAY);
			
			add(values);
			ppmr.addPPMReaderListener(this);
			
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException();
		}
		
	}
	
	private void selectChan(int ppmChan,int dyioChan){
		
		if(dyioChan == PPMReaderChannel.NO_CROSSLINK)
			return;
		for(int i=0;i<25;i++){
			Object o = ppmLinks[ppmChan].getItemAt(i);
			Integer in= new Integer(PPMReaderChannel.NO_CROSSLINK);
			
			try{
				 in= (Integer)o;
			}catch(ClassCastException nx) {
				String s = ((String) o);
				try {
					in = new Integer(s);
				}catch (NumberFormatException nf) {
					
				}
			}
			
			if(in.intValue() == dyioChan){
				ppmLinks[ppmChan].setSelectedItem(o);
				return;
			}
		}
	}
	
	private class linkListener implements ActionListener{
		private int index;
		public linkListener(int i){
			index=i;
		}
		
		public void actionPerformed(ActionEvent e) {
			int [] links=ppmr.getCrossLink();
			
			try{
				Integer val = (Integer)ppmLinks[index].getSelectedItem();
				links[index] = val.intValue();
				
			}catch(Exception ex){
				links[index]=PPMReaderChannel.NO_CROSSLINK;
			}
			
			ppmr.setCrossLink(links);
		}
		
	}
	
	public void onPPMPacket(int[] values) {
		for(int i=0;i<ppmLabels.length;i++){
			ppmLabels[i].setText(new Integer(values[i]).toString());
		}
		repaint();
	}
	
	public DyIOAbstractPeripheral getPerpheral() {
		return ppmr;
	}

}
