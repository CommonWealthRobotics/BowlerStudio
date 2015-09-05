package com.neuronrobotics.bowlerstudio.tabs;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.image.ImageView;
import javafx.scene.control.ComboBox;
import javafx.event.ActionEvent;
import javafx.event.Event;

public class DyIOPanel  implements Initializable {

	@FXML ImageView chanButton23;
	@FXML ImageView chanButton22;
	@FXML ImageView chanButton21;
	@FXML ImageView chanButton20;
	@FXML ImageView chanButton19;
	@FXML ImageView chanButton18;
	@FXML ImageView chanButton17;
	@FXML ImageView chanButton16;
	@FXML ImageView chanButton15;
	@FXML ImageView chanButton14;
	@FXML ImageView chanButton13;
	@FXML ImageView chanButton12;
	
	@FXML ImageView chanButton11;
	@FXML ImageView chanButton10;
	@FXML ImageView chanButton9;
	@FXML ImageView chanButton8;
	@FXML ImageView chanButton7;
	@FXML ImageView chanButton6;
	@FXML ImageView chanButton5;
	@FXML ImageView chanButton4;
	@FXML ImageView chanButton3;
	@FXML ImageView chanButton2;
	@FXML ImageView chanButton1;
	@FXML ImageView chanButton0;
	
	@FXML ComboBox<String> channelType23;
	@FXML ComboBox<String> channelType22;
	@FXML ComboBox<String> channelType21;
	@FXML ComboBox<String> channelType20;
	@FXML ComboBox<String> channelType19;
	@FXML ComboBox<String> channelType18;
	@FXML ComboBox<String> channelType17;
	@FXML ComboBox<String> channelType16;
	@FXML ComboBox<String> channelType15;
	@FXML ComboBox<String> channelType14;
	@FXML ComboBox<String> channelType13;
	@FXML ComboBox<String> channelType12;
	@FXML ComboBox<String> channelType11;
	@FXML ComboBox<String> channelType10;
	@FXML ComboBox<String> channelType9;
	@FXML ComboBox<String> channelType8;
	
	@FXML ComboBox<String> channelType7;
	@FXML ComboBox<String> channelType6;
	@FXML ComboBox<String> channelType5;
	@FXML ComboBox<String> channelType4;
	@FXML ComboBox<String> channelType3;
	@FXML ComboBox<String> channelType2;
	@FXML ComboBox<String> channelType1;
	@FXML ComboBox<String> channelType0;
	
	private ArrayList<ComboBox<String>> channelTypeSelectors = new ArrayList<>() ;
	private ArrayList<ImageView> channelButtonSelectors = new ArrayList<>() ;
	
	 
	
	@FXML public void channelClicked(MouseEvent event) {
		System.err.println(getIndex( event));
	}
	
	private int getIndex(Event event){
		for(int i=0;i<24;i++){
			if(channelTypeSelectors.get(i).equals(event.getSource())){
				return i;
			}
			if(channelButtonSelectors.get(i).equals(event.getSource())){
				return i;
			}
		}
		throw new RuntimeException("This event did not come from this system...");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		//This creates the map of buttons with their list indexes. 
		for(int i=0;i<24;i++){
			channelTypeSelectors.add((ComboBox<String>) resources.getObject("channelType"+i));
			channelButtonSelectors.add((ImageView) resources.getObject("chanButton"+i));
		}
		for(int i=0;i<24;i++){
			channelTypeSelectors.get(i).getItems().clear();
		}
	}

	@FXML public void onChannelSelect(ActionEvent event) {
		int index =getIndex( event);
		String value =channelTypeSelectors.get(index).getValue();
		System.err.println("Challen Select on: "+index+" to value "+value);
	}

}
