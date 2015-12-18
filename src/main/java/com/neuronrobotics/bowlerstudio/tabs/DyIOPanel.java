package com.neuronrobotics.bowlerstudio.tabs;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.management.RuntimeErrorException;
import javax.swing.ImageIcon;

import org.reactfx.util.FxTimer;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.utils.BowlerStudioResourceFactory;
import com.neuronrobotics.nrconsole.plugin.DyIO.DyIOConsole;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.DyIOChannel;
import com.neuronrobotics.sdk.dyio.DyIOChannelMode;
import com.neuronrobotics.sdk.util.ThreadUtil;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
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
import javafx.scene.layout.AnchorPane;

public class DyIOPanel  implements Initializable {
	
	private ArrayList<ComboBox<String>> channelTypeSelectors = new ArrayList<>() ;
	private ArrayList<ImageView> channelButtonSelectors = new ArrayList<>() ;
	private ArrayList<Label> channelValue = new ArrayList<>() ;
	private ArrayList<Parent> controlWidgets = new ArrayList<>() ;
	private ArrayList<Boolean> displayFlash = new ArrayList<>() ;
	
	private DyIO dyio;
	private boolean initialized=false;

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
	

	@FXML Circle centerled;
	@FXML Label channelValue0;
	@FXML Label channelValue1;
	@FXML Label channelValue2;
	@FXML Label channelValue3;
	@FXML Label channelValue4;
	@FXML Label channelValue5;
	@FXML Label channelValue6;
	@FXML Label channelValue7;
	@FXML Label channelValue8;
	@FXML Label channelValue9;
	@FXML Label channelValue10;
	@FXML Label channelValue11;
	@FXML Label channelValue12;
	@FXML Label channelValue13;
	@FXML Label channelValue14;
	@FXML Label channelValue15;
	@FXML Label channelValue16;
	@FXML Label channelValue17;
	@FXML Label channelValue18;
	@FXML Label channelValue19;
	@FXML Label channelValue20;
	@FXML Label channelValue21;
	@FXML Label channelValue22;
	@FXML Label channelValue23;
	@FXML AnchorPane controlWidgetPanel;
	
	
	
	public void setDyIO(DyIO d){
		
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
		
		//Values
		channelValue.add(  channelValue0);
		channelValue.add(  channelValue1);
		channelValue.add(  channelValue2);
		channelValue.add(  channelValue3);
		channelValue.add(  channelValue4);
		channelValue.add(  channelValue5);
		channelValue.add(  channelValue6);
		channelValue.add(  channelValue7);
		channelValue.add(  channelValue8);
		channelValue.add(  channelValue9);
		channelValue.add(  channelValue10);
		channelValue.add(  channelValue11);
		channelValue.add(  channelValue12);
		channelValue.add(  channelValue13);
		channelValue.add(  channelValue14);
		channelValue.add(  channelValue15);
		channelValue.add(  channelValue16);
		channelValue.add(  channelValue17);
		channelValue.add(  channelValue18);
		channelValue.add(  channelValue19);
		channelValue.add(  channelValue20);
		channelValue.add(  channelValue21);
		channelValue.add(  channelValue22);
		channelValue.add(  channelValue23);
		
		this.dyio = d;
		for (int i = 0; i < 24; i++) {
			int index = i;
			ObservableList<String> items = FXCollections.observableArrayList();
			DyIOChannel chan = dyio.getChannel(index);
			displayFlash.add(new Boolean(true));
			Platform.runLater(()->channelValue.get(index).setText(new Integer(chan.getValue()).toString()));
			chan.addChannelEventListener(e -> {
				// set the value label text
				if(displayFlash.get(index)){
					displayFlash.set(index, false);
					Platform.runLater(()->{
							channelValue.get(index).setText(new Integer(e.getValue()).toString());
							channelButtonSelectors.get(index).setImage(BowlerStudioResourceFactory.getChanUpdate());
							FxTimer.runLater(
									Duration.ofMillis(200) ,() -> {
										channelButtonSelectors.get(index).setImage(BowlerStudioResourceFactory.getChanDefault());
										displayFlash.set(index, true);
									});
						
					});
				}
			});
			channelButtonSelectors.get(index).setOnMouseEntered(event -> {
				Platform.runLater(()->
				channelButtonSelectors.get(index).setImage(BowlerStudioResourceFactory.getChanHighlight()));
			});
			channelButtonSelectors.get(index).setOnMouseExited(event -> {
				Platform.runLater(()->
				channelButtonSelectors.get(index).setImage(BowlerStudioResourceFactory.getChanDefault()));
			});
			channelTypeSelectors.get(index);
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

			                    name.setText(item);
			                    icon.setGraphic(new ImageView(BowlerStudioResourceFactory.getModeImage(DyIOChannelMode.getFromSlug(item))));
			                    setGraphic(cell);
			                    //HERE IS WHERE YOU GET THE LABEL AND NAME
			                }
			           }
			      };
					
				}
			};
			Platform.runLater(()->{
				channelTypeSelectors.get(index).setButtonCell(callback.call(null));
				channelTypeSelectors.get(index).setCellFactory(callback);
				channelTypeSelectors.get(index).setItems(items);
			});
			setChannelModeList(index);
			dyio.getChannel(index).addChannelModeChangeListener(newMode -> {
				setChannelModeList(index);
			});
			


		}
		new Thread(){
			public void run(){
				setName("DyIOchannelWidget Setting channel value ");
				for (int index = 0; index < 24; index++) {
					int i=index;
					
					FXMLLoader fxmlLoader=  BowlerStudioResourceFactory.getLoader(i);
			        Parent root = fxmlLoader.getRoot();
			        DyIOchannelWidget controller = fxmlLoader.getController();
			        controller.setChannel(dyio.getChannel(i));
			        controller.setVisable(false);
					controlWidgets.add(root);
					
				}
				initialized=true;
				while(dyio.isAvailable()){
					for(int i=0;i<24;i++){
						DyIOchannelWidget controller =BowlerStudioResourceFactory.getLoader(i).getController();
						if(controller.isFireValue()){
							controller.setFireValue(false);
							dyio.setValue(i,controller.getLatestValue());
						}
						controller.updateValue();
					}
					ThreadUtil.wait(50);
				}
			}
		}.start();
		
	}
	
	@FXML public void channelClicked(MouseEvent event) {
		int index=getIndex( event);
		System.err.println("Channel was clicked: "+index);
		if(!initialized)
			return;

		setControlWidget( index);
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
		DyIOChannel chan =dyio.getChannel(index);
		Platform.runLater(()->{
			ComboBox<String> selector = channelTypeSelectors.get(index);
			
			String current =null;
			for(String m:selector.getItems()){
				if(chan.getMode().toSlug().contentEquals(m)){
					current=m;
				}
			}
			String tmp =current;
			selector.setValue(tmp);
		});

	}

	private void setControlWidget(int index){
		for(int i=0;i<24;i++){
			 DyIOchannelWidget controller =BowlerStudioResourceFactory.getLoader(i).getController();
			 controller.setVisable(false);
		}
		DyIOchannelWidget mine =  BowlerStudioResourceFactory.getLoader(index).getController();
		Platform.runLater(()->{
			if(!initialized)
				return;
			synchronized (controlWidgetPanel) {
				controlWidgetPanel.getChildren().clear();
				controlWidgetPanel.getChildren().add(controlWidgets.get(index));
			}
			mine.setVisable(true);
		});
	}

	@FXML public void onChannelSelect(ActionEvent event) {
		int index =getIndex( event);
		ComboBox<String> comboBox = channelTypeSelectors.get(index);
		String v = comboBox.getValue();
		if(v==null)
			return;

		DyIOChannelMode value = DyIOChannelMode.getFromSlug(v);
		
		if(!initialized)
			return;
		new Thread(){
			public void run(){
				setName("Running set mode thread");
				dyio.setMode(index, value);
				setControlWidget( index);
			}
		}.start();

	
	}

}
