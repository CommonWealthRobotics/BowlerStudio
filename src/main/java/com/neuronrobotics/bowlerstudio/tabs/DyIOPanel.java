package com.neuronrobotics.bowlerstudio.tabs;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.management.RuntimeErrorException;
import javax.swing.ImageIcon;

import com.neuronrobotics.nrconsole.plugin.DyIO.DyIOConsole;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.DyIOChannel;
import com.neuronrobotics.sdk.dyio.DyIOChannelMode;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

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
	
	@FXML ComboBox<Label> channelType23;
	@FXML ComboBox<Label> channelType22;
	@FXML ComboBox<Label> channelType21;
	@FXML ComboBox<Label> channelType20;
	@FXML ComboBox<Label> channelType19;
	@FXML ComboBox<Label> channelType18;
	@FXML ComboBox<Label> channelType17;
	@FXML ComboBox<Label> channelType16;
	@FXML ComboBox<Label> channelType15;
	@FXML ComboBox<Label> channelType14;
	@FXML ComboBox<Label> channelType13;
	@FXML ComboBox<Label> channelType12;
	@FXML ComboBox<Label> channelType11;
	@FXML ComboBox<Label> channelType10;
	@FXML ComboBox<Label> channelType9;
	@FXML ComboBox<Label> channelType8;
	
	@FXML ComboBox<Label> channelType7;
	@FXML ComboBox<Label> channelType6;
	@FXML ComboBox<Label> channelType5;
	@FXML ComboBox<Label> channelType4;
	@FXML ComboBox<Label> channelType3;
	@FXML ComboBox<Label> channelType2;
	@FXML ComboBox<Label> channelType1;
	@FXML ComboBox<Label> channelType0;
	
	private ArrayList<ComboBox<Label>> channelTypeSelectors = new ArrayList<>() ;
	private ArrayList<ImageView> channelButtonSelectors = new ArrayList<>() ;
	private DyIO dyio;
	private boolean initialized=false;
	@FXML Circle centerled;
	@SuppressWarnings("unchecked")
	public void setDyIO(DyIO d, Parent p){
		
		channelTypeSelectors.add( channelType0);
		channelTypeSelectors.add(  channelType1);
		channelTypeSelectors.add(  channelType2);
		channelTypeSelectors.add(  channelType3);
		channelTypeSelectors.add(  channelType4);
		channelTypeSelectors.add(  channelType5);
		channelTypeSelectors.add(  channelType6);
		channelTypeSelectors.add(  channelType7);
		channelTypeSelectors.add(  channelType8);
		channelTypeSelectors.add(  channelType9);
		channelTypeSelectors.add(  channelType10);
		channelTypeSelectors.add(  channelType11);
		channelTypeSelectors.add(  channelType12);
		channelTypeSelectors.add(  channelType13);
		channelTypeSelectors.add(  channelType14);
		channelTypeSelectors.add(  channelType15);
		channelTypeSelectors.add(  channelType16);
		channelTypeSelectors.add(  channelType17);
		channelTypeSelectors.add(  channelType18);
		channelTypeSelectors.add(  channelType19);
		channelTypeSelectors.add(  channelType20);
		channelTypeSelectors.add(  channelType21);
		channelTypeSelectors.add(  channelType22);
		channelTypeSelectors.add(  channelType23);
		
		channelButtonSelectors.add(  chanButton0);
		channelButtonSelectors.add(  chanButton1);
		channelButtonSelectors.add(  chanButton2);
		channelButtonSelectors.add(  chanButton3);
		channelButtonSelectors.add(  chanButton4);
		channelButtonSelectors.add(  chanButton5);
		channelButtonSelectors.add(  chanButton6);
		channelButtonSelectors.add(  chanButton7);
		channelButtonSelectors.add(  chanButton8);
		channelButtonSelectors.add(  chanButton9);
		channelButtonSelectors.add(  chanButton10);
		channelButtonSelectors.add(  chanButton11);
		channelButtonSelectors.add(  chanButton12);
		channelButtonSelectors.add(  chanButton13);
		channelButtonSelectors.add(  chanButton14);
		channelButtonSelectors.add(  chanButton15);
		channelButtonSelectors.add(  chanButton16);
		channelButtonSelectors.add(  chanButton17);
		channelButtonSelectors.add(  chanButton18);
		channelButtonSelectors.add(  chanButton19);
		channelButtonSelectors.add(  chanButton20);
		channelButtonSelectors.add(  chanButton21);
		channelButtonSelectors.add(  chanButton22);
		channelButtonSelectors.add(  chanButton23);
		
		
		this.dyio = d;
		for(int i=0;i<24;i++){
			int index=i;

			setChannelModeList( index);
			dyio.getChannel(index).addChannelModeChangeListener(newMode -> {
					setChannelModeList( index);
			});
		}
		
		initialized=true;
	}
	
	@FXML public void channelClicked(MouseEvent event) {
		System.err.println("Channel was clicked: "+getIndex( event));
		if(!initialized)
			return;
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

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		

	}
	
	private void setChannelModeList(int index){
		ComboBox<Label> selector = channelTypeSelectors.get(index);
		ArrayList<DyIOChannelMode> modesAvailible = dyio.getAvailibleChannelModes(index);
		Platform.runLater(()->{
			ObservableList<Label> items = FXCollections.observableArrayList();
			DyIOChannel chan =dyio.getChannel(index);
			Label current =null;
			for(DyIOChannelMode m:modesAvailible){
				Image image;
				//
				try {
					image = new Image(DyIOConsole.class.getResourceAsStream("images/icon-" + m.toSlug() + ".png"));
				}catch (NullPointerException e) {
					image = new Image(DyIOConsole.class.getResourceAsStream("images/icon-off.png"));
				}
				//
				Label lbl = new Label(m.toSlug());
				lbl.setTextFill(Color.BLACK);
				lbl.setGraphic(new ImageView(image));
				items.add(lbl);
				if(chan.getMode() == m){
					current=lbl;
				}
			}
			selector.setItems(items);
			Label tmp =current;
			Platform.runLater(()->{
				selector.setValue(tmp);
			});
		});

	}
	
//	private void setSelectorType( int channel,DyIOChannelMode newMode ){
//		
//		ObservableList<Label> items = channelTypeSelectors.get(channel).getItems();
//		for(int i=0;i<items.size();i++){
//			Label l=items.get(i);
//			if(l.getText().contentEquals(newMode.toSlug())){
//				Platform.runLater(()->{
//					channelTypeSelectors.get(channel).setValue(l);
//				});
//				return;
//			}
//		}
//		
////		channelButtonSelectors.get(channel).setImage(
////				getChannelImage(
////						new Image(DyIOConsole.class.getResourceAsStream("images/channel-default.png")), 
////						newMode)
////						);
//	}
	
	private Image getChannelImage(Image image, DyIOChannelMode newMode ){
		Image mode;
		try {
			mode = new Image(DyIOConsole.class.getResourceAsStream("images/icon-" + newMode.toSlug() + ".png"));
		}catch (NullPointerException e) {
			mode = new Image(DyIOConsole.class.getResourceAsStream("images/icon-off.png"));
		}
		 // Obtain PixelReader
        PixelReader pixelReader = image.getPixelReader();
        
        // Create WritableImage
         WritableImage wImage = new WritableImage(
                 (int)image.getWidth(),
                 (int)image.getHeight());
         PixelWriter pixelWriter = wImage.getPixelWriter();
       
        // Determine the color of each pixel in a specified row
        for(int readY=0;readY<image.getHeight();readY++){
            for(int readX=0; readX<image.getWidth();readX++){
                Color color = pixelReader.getColor(readX,readY);               
                // Now write a brighter color to the PixelWriter.
                color = color.brighter();
                pixelWriter.setColor(readX,readY,color);
            }
        }
        pixelReader = mode.getPixelReader();
     // Determine the color of each pixel in a specified row
        for(int readY=0;readY<image.getHeight();readY++){
            for(int readX=0; readX<image.getWidth();readX++){
                Color color = pixelReader.getColor(readX,readY);               
                // Now write a brighter color to the PixelWriter.
                color = color.brighter();
                pixelWriter.setColor(readX,readY,color);
            }
        }
		
		return wImage;
	}

	@FXML public void onChannelSelect(ActionEvent event) {
		int index =getIndex( event);
		Platform.runLater(()->{
			
			ComboBox<Label> comboBox = channelTypeSelectors.get(index);
			Label v = comboBox.getValue();
			if(v==null)
				return;
			String text = v.getText();
			DyIOChannelMode value = DyIOChannelMode.getFromSlug(text);
			
			if(!initialized)
				return;
			dyio.setMode(index, value);
		});
	
	}

}
