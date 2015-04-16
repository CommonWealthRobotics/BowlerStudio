package com.neuronrobotics.nrconsole.util;

import javax.swing.JComboBox;

public class IntegerComboBox extends JComboBox{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2439771311089831575L;
	
	private final String noneString = "None";
	private int noneValue=0xff;
	
	public IntegerComboBox(){}
	
	public IntegerComboBox(boolean withNoneOption, int noneVal){
		if(withNoneOption){
			addItem(noneString);
			noneValue=noneVal;
		}
	}

	public void removeInteger(int in){
		for(int i=0;i<getItemCount();i++){
			try{
				Integer selected = (Integer)( getItemAt(i));
				if(selected != null){
					if(selected.intValue() == in){
						removeItemAt(i);
						return;
					}
				}
			}catch(ClassCastException ex){
				//ingnore the None case
			}
		}
	}
	
	public void addInteger(int in){
		for(int i=0;i<getItemCount();i++){
			try{
				Integer selected = (Integer)( getItemAt(i));
				if(selected != null){
					if(selected.intValue() == in){
						return;
					}
				}
			}catch(ClassCastException ex){
				//ingnore the None case
			}
		}
		addItem(new Integer(in));
	}
	
	public void setNoneItemSelected(){
		for(int i=0;i<getItemCount();i++){
			try{
				Integer selected = (Integer)( getItemAt(i));
			}catch(ClassCastException ex){
				setSelectedItem(getItemAt(i));
			}
		}
		setSelectedItem(getItemAt(0));
	}
	
	public void setSelectedInteger(int in){
		for(int i=0;i<getItemCount();i++){
			try{
				Integer selected = (Integer)( getItemAt(i));
				if(selected != null){
					if(selected.intValue() == in){
						setSelectedItem(getItemAt(i));
						return;
					}
				}
			}catch(ClassCastException ex){
				//ignore
			}
		}
		if(in == noneValue){
			setNoneItemSelected();
			return;
		}
		addInteger(in);
		setSelectedInteger(in);
	}
	
	public int getSelectedInteger(){
		try{
			return Integer.parseInt(getSelectedItem().toString());
		}catch(NumberFormatException ex){
			return noneValue;
		}
	}
}
