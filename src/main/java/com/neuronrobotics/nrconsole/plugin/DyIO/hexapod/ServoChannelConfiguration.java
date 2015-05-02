package com.neuronrobotics.nrconsole.plugin.DyIO.hexapod;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import com.neuronrobotics.sdk.addons.walker.BasicWalker;
import com.neuronrobotics.sdk.addons.walker.Leg;
import com.neuronrobotics.sdk.addons.walker.WalkerServoLink;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.peripherals.ServoChannel;

public class ServoChannelConfiguration extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8666950965299555115L;
	HexapodConfigPanel config;
	//private JFrame frame = new JFrame("Configure Servo Channels");
	//private JPanel panel = new JPanel();
	private IntegerComboBox leg = new IntegerComboBox(6);
	private ArrayList<LegWidget> legWidgets=new ArrayList<LegWidget>();
	private int selected = 0;
	private JPanel widgetHolder = new JPanel();
	private JButton homeAll = new JButton("Stand Up");
	public ServoChannelConfiguration(HexapodConfigPanel config){
		setLayout(new MigLayout());
		this.config=config;
		for(int i=0;i<leg.getListSize();i++){
			legWidgets.add(new LegWidget(i));
		}
		leg.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				Integer i = leg.getIntSelected();
				if(i!=null)
					setSelectedLeg(i);
			}
		});
		homeAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getWalker().initialize();
				guiInit();
			}
		});
		JPanel tmp = new JPanel(new MigLayout());
		JPanel tmp2 = new JPanel(new MigLayout());
		tmp.add(new JLabel("Select Leg:"));
		tmp.add(leg);
		tmp2.add(tmp,"wrap");
		tmp2.add(homeAll,"wrap");
		add(tmp2,"wrap");
		add(widgetHolder,"wrap");
		widgetHolder.setBorder(BorderFactory.createRaisedBevelBorder());
		
		setSelectedLeg(0);	
		guiInit();
		
	}
	public void redisplay() {
		new Thread(){
			
			public void run(){
				for(LegWidget l: legWidgets){
					l.redisplay();
				}
			}
			
		}.start();
	}
	private void setSelectedLeg(int legIndex){
		selected = legIndex;
		guiInit();
	}
	private void guiInit(){
		LegWidget w= legWidgets.get(selected);
		//System.out.println("Selected leg: "+selected+" Widget: "+w);
		w.setVisible(true);
		w.redisplay();
		widgetHolder.removeAll();
		widgetHolder.add(w);
		widgetHolder.setVisible(true);
		widgetHolder.revalidate();
		//frame.invalidate();
		//frame.repaint();
		widgetHolder.repaint();
		//frame.pack();
	}
	public void run(){
		guiInit();
		//frame.add(panel);
		//frame.pack();
		//frame.setLocationRelativeTo(null); 
		//frame.setVisible(true);
		
		//config.setConfigEnabled(false);
		//while(frame.isShowing()){
		//	ThreadUtil.wait(100);
		//}
		//config.setConfigEnabled(true);
	}

	public BasicWalker getWalker() {
		return config.getWalker();
	}
	private void setAllScales(double scale){
		for(LegWidget l: legWidgets){
			l.setAllScales(scale);
		}
	}
	private class LegWidget extends JPanel{
		/**
		 * 
		 */
		
		private static final long serialVersionUID = -4544650665476713983L;
		private int index=0;
		JButton home = new JButton("Home All Links");
		ServoLinkWidget hip;
		ServoLinkWidget knee;
		ServoLinkWidget ankle;
		public LegWidget(int val){
			setLayout(new MigLayout());
			index = val;	
			add(new JLabel("Leg Widget "+val),"wrap");
			add(home,"wrap");
			add(new JLabel("Remeber to set the link using the 'Right Hand Rule' to determine if the scale needs to be inverted"),"wrap");
			JPanel positions = new JPanel();
			positions.add(new JLabel("X Position: "+getWalkerLeg().getLexXOffset()+" inches"));
			positions.add(new JLabel("Y Position: "+getWalkerLeg().getLexYOffset()+" inches"));
			positions.add(new JLabel("Theta Offset: "+getWalkerLeg().getLexThetaOffset()+" degrees"),"wrap");
			add(positions,"wrap");
			hip=new ServoLinkWidget("Hip");
			knee=new ServoLinkWidget("Knee");
			ankle=new ServoLinkWidget("Ankle");
			add(hip,"wrap");
			add(knee,"wrap");
			add(ankle,"wrap");
			home.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					hip.goHome();
					knee.goHome();
					ankle.goHome();
				}
			});
			hip.goHome();
			knee.goHome();
			ankle.goHome();
		}
		public void redisplay() {
			hip.redisplay();
			knee.redisplay();
			ankle.redisplay();
		}
		public void setAllScales(double scale) {
			hip.setScale(scale);
			knee.setScale(scale);
			ankle.setScale(scale);
		}
		
		public String toString(){
			return "Leg Widget: " +index;
		}
		public Leg getWalkerLeg() {
			return getWalker().getLegs().get(index);
		}
		private class ServoLinkWidget extends JPanel{
			/**
			 * 
			 */
			private static final long serialVersionUID = -8348572743973649628L;
			private IntegerComboBox chanBox = new IntegerComboBox(24);
			private String name;
			private JTextField ul=new JTextField();
			private JTextField ll=new JTextField();
			private JTextField home=new JTextField();
			private JButton homeButton = new JButton("Use Current as '0.0' Degrees");
			private JButton nintyButton = new JButton("Use Current as '90.0' Degrees");
			private JSlider slider=new JSlider();
			private JLabel sliderValue = new JLabel();
			private JButton updateServoConfigs = new JButton("Update Servo");
			
			private JLabel angle = new JLabel();
			private JTextField scaleUI = new JTextField();
			private JCheckBox invert = new JCheckBox("Invert");
			private JButton updateScaleConfigs = new JButton("Update Scale");
			private ChangeListener slideristener = new ChangeListener() {
				
				public void stateChanged(ChangeEvent e) {
					// TODO Auto-generated method stub
					setSlider(slider.getValue(), true);
				}
			};
			
			public ServoLinkWidget(String name){
				setLayout(new MigLayout());
				this.name = name;
				
				chanBox.setIntSelected( getMyLink().getServoChannel().getChannel().getChannelNumber());
				chanBox.addActionListener(new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						Integer i= chanBox.getIntSelected();
						//System.out.println("Selected Servo channel Integer: "+i);
						if(i != null)
							getMyLink().setServoChannel(new ServoChannel(getDyio().getChannel(i.intValue())));
					}
				});
				updateServoConfigs.setEnabled(false);
				ll.setText(new Integer(getMyLink().getLowerLimit()).toString());
				ul.setText(new Integer(getMyLink().getUpperLimit()).toString());
				home.setText(new Integer(getMyLink().getHome()).toString());
				
				ll.addActionListener(new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						updateServoConfigs.setEnabled(true);
					}
				});
				ul.addActionListener(new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						updateServoConfigs.setEnabled(true);
					}
				});
				home.addActionListener(new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						updateServoConfigs.setEnabled(true);
					}
				});
				
				
				invert.setSelected(getMyLink().getScale()<0);
				updateScaleConfigs.setEnabled(false);
				scaleUI.addActionListener(new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						updateScaleConfigs.setEnabled(true);
					}
				});
				invert.addActionListener(new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						updateScaleConfigs.setEnabled(true);
					}
				});
				updateScaleConfigs.addActionListener(new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						Double d = Double.parseDouble(scaleUI.getText());
						if(invert.isSelected())
							d*=-1;
						getMyLink().setScale(d);
						setSlider(slider.getValue(),false);
					}
				});
				
				
				sliderInit();
				
				setSlider(getMyLink().getHome(), false);
				slider.addChangeListener(slideristener);
				
				updateServoConfigs.addActionListener(new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						updateServoCongiguration();
					}
				});
				homeButton.addActionListener(new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						updateServoConfigs.setEnabled(true);
						home.setText(new Integer(slider.getValue()).toString());
						updateServoCongiguration();
					}
				});
				nintyButton.addActionListener(new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						double val = slider.getValue()-getMyLink().getHome();
						if(val != 0){
							double newScale = (90.0/val);
							if(invert.isSelected())
								newScale*=-1;
							setAllScales(newScale);
						}
					}
				});
				
				
				JPanel control = new JPanel(new MigLayout());
				control.add(new JLabel("Lower Limit:"));
				control.add(ll);
				
				control.add(new JLabel("Home (Zero degrees):"));
				control.add(home);
				
				control.add(new JLabel("Uper Limit:"));
				control.add(ul);
				control.add(updateServoConfigs,"wrap");
				
				JPanel localControl = new JPanel(new MigLayout());
				
				
				
				
				JPanel tmp = new JPanel(new MigLayout());
				tmp.add(sliderValue);
				tmp.add(slider);
				if(name.toLowerCase().contains("ankle")){
					tmp.add(homeButton);
					tmp.add(nintyButton,"wrap");
				}else{
					tmp.add(homeButton,"wrap");
				}
				
				JPanel scaledInterface = new JPanel(new MigLayout());
				scaledInterface.add(angle);
				scaledInterface.add(new JLabel("Scale (servo units to degrees): "));
				scaledInterface.add(scaleUI);
				scaledInterface.add(invert);
				scaledInterface.add(updateScaleConfigs);
				
				localControl.add(control,"wrap");
				localControl.add(tmp,"wrap");
				localControl.add(scaledInterface,"wrap");
				
				
				add(new JLabel(this.name+":"));
				add(chanBox);
				add(localControl);
				redisplay();
				setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
			}
			public void redisplay() {
				setSlider(getMyLink().getTargetValue(), false);
				invert.setSelected(getMyLink().getScale()<0);
				ll.setText(new Integer(getMyLink().getLowerLimit()).toString());
				ul.setText(new Integer(getMyLink().getUpperLimit()).toString());
				home.setText(new Integer(getMyLink().getHome()).toString());
			}
			public void setScale(double scale) {
				scale = Math.abs(scale);
				if(invert.isSelected())
					scale*=-1;
				getMyLink().setScale(scale);
				setSlider(slider.getValue(), false);
			}
			public void goHome(){
				setSlider(getMyLink().getHome(), true);
			}
			private void updateServoCongiguration(){
				int h = Integer.parseInt(home.getText());
				int l = Integer.parseInt(ll.getText());
				int u = Integer.parseInt(ul.getText());
				if(h>=u)
					h=u-1;
				if(h<=l)
					h=l+1;
				getMyLink().setUpperLimit(u);
				getMyLink().setLowerLimit(l);
				getMyLink().setHome(h);
				sliderInit();
				
			}
			private void sliderInit(){
				slider.setMajorTickSpacing(50);
				slider.setMinorTickSpacing(10);
				slider.setPaintTicks(true);
				slider.setMinimum(getMyLink().getLowerLimit());
				slider.setMaximum(getMyLink().getUpperLimit());
			}

			
			private void setSlider(int val, boolean updateServo){
				int u = getMyLink().getUpperLimit();
				int l = getMyLink().getLowerLimit();
				int h = getMyLink().getHome();
				if(val == h)
					homeButton.setEnabled(false);
				else
					homeButton.setEnabled(true);
				if(val>u)
					val=u;
				if(val<l)
					val=l;
				//System.out.println("Setting slider val: "+ val);
				slider.removeChangeListener(slideristener);
				slider.setValue(val);
				slider.addChangeListener(slideristener);
				
				if(updateServo){
					getMyLink().getServoChannel().SetPosition(slider.getValue());
					getMyLink().getServoChannel().flush();
				}
				
				
				sliderValue.setText(String.format("%03d", val));

				scaleUI.setText(String.format("%05f", Math.abs(getMyLink().getScale())));
				angle.setText("Angel: "+String.format("%05f", getMyLink().getTargetAngle())+" degrees");
				angle.invalidate();
			}
			public WalkerServoLink getMyLink() {
				if(name.toLowerCase().contains("hip"))
					return getWalkerLeg().getHipLink();
				if(name.toLowerCase().contains("knee"))
					return getWalkerLeg().getKneeLink();
				if(name.toLowerCase().contains("ankle"))
					return getWalkerLeg().getAnkleLink();
				return null;
			}
			public DyIO getDyio() {
				return  getMyLink().getServoChannel().getChannel().getDevice();
			}
		}
	}
	
	private class IntegerComboBox extends JComboBox{
		/**
		 * 
		 */
		private static final long serialVersionUID = -5066020944623842112L;
		private int size;
		public IntegerComboBox(int size){
			this.size=size;
			//addItem(new String("None"));
			for(int i=0;i<size;i++){
				addItem(new Integer(i));
			}
		}
		public int getListSize(){
			return size;
		}
		public Integer getIntSelected(){
			try{
				return (Integer)getSelectedItem();
			}catch(Exception e){
				return null;
			}
		}
		public void setIntSelected(int value){
			for(int i=0;i<getItemCount();i++){
				try{
					Integer selected = (Integer)(getItemAt(i));
					if(selected != null){
						if(selected.intValue() == value){
							setSelectedItem(getItemAt(i));
						}
					}
				}catch(Exception e){
					
				}
			}
		}
	}

}
