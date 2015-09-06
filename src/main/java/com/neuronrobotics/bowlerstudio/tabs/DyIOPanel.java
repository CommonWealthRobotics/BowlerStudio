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
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Callback;

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
		for (int i = 0; i < 24; i++) {
			int index = i;
			ObservableList<String> items = FXCollections.observableArrayList();
			DyIOChannel chan = dyio.getChannel(index);
			Label current = null;
			ComboBox<String> selector = channelTypeSelectors.get(index);
			ArrayList<DyIOChannelMode> modesAvailible = dyio
					.getAvailibleChannelModes(index);
			for (DyIOChannelMode m : modesAvailible) {

				items.add(m.toSlug());
			}
			Callback<ListView<String>, ListCell<String>> callback =new Callback<ListView<String>, ListCell<String>>() {
				@Override
				public ListCell<String> call(ListView<String> p) {
					return new ListCell<String>() {
			            Label name = new Label();
			            Label icon = new Label();
			            private final HBox cell;
			            { 
			                setContentDisplay(ContentDisplay.GRAPHIC_ONLY); 
			                cell = new HBox(5);

			                //HERE, ADD YOUR PRE-MADE HBOX CODE
			                name.setTextFill(Color.BLACK);
			                cell.getChildren().add(icon);
			                cell.getChildren().add(name);
			            }

			            @Override protected void updateItem(String item, boolean empty) {
			                super.updateItem(item, empty);

			                if (item == null || empty) {
			                    setGraphic(null);
			                } else {
								Image image;
								//
								try {
									image = new Image(
											DyIOConsole.class
													.getResourceAsStream("images/icon-"
															+ item+ ".png"));
								} catch (NullPointerException e) {
									image = new Image(
											DyIOConsole.class
													.getResourceAsStream("images/icon-off.png"));
								}

			                    name.setText(item);
			                    icon.setGraphic(new ImageView(image));
			                    setGraphic(cell);
			                    //HERE IS WHERE YOU GET THE LABEL AND NAME
			                }
			           }
			      };
					
				}
			};

			selector.setButtonCell(callback.call(null));
			selector.setCellFactory(callback);
			selector.setItems(items);
			setChannelModeList(index);
			dyio.getChannel(index).addChannelModeChangeListener(newMode -> {
				setChannelModeList(index);
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
		ComboBox<String> selector = channelTypeSelectors.get(index);
		Platform.runLater(()->{

			DyIOChannel chan =dyio.getChannel(index);
			String current =null;
			for(String m:selector.getItems()){
				if(chan.getMode().toSlug().contentEquals(m)){
					current=m;
				}
			}
			String tmp =current;
			Platform.runLater(()->{
				selector.setValue(tmp);
			});
		});

	}
	

	
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
			
			ComboBox<String> comboBox = channelTypeSelectors.get(index);
			String v = comboBox.getValue();
			if(v==null)
				return;

			DyIOChannelMode value = DyIOChannelMode.getFromSlug(v);
			
			if(!initialized)
				return;
			dyio.setMode(index, value);
		});
	
	}

}
