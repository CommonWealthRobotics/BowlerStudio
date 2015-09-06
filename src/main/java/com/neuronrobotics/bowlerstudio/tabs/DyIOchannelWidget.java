package com.neuronrobotics.bowlerstudio.tabs;

import java.time.Duration;

import org.reactfx.util.FxTimer;

import com.neuronrobotics.nrconsole.plugin.DyIO.DyIOConsole;
import com.neuronrobotics.sdk.dyio.DyIOChannel;

import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Slider;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class DyIOchannelWidget {

	@FXML Button setListenerButton;
	@FXML AnchorPane listenerCodeBox;
	@FXML LineChart<Integer,Integer> channelGraph;
	@FXML Slider timeSlider;
	@FXML Slider positionSlider;
	@FXML Label chanValue;
	@FXML Label deviceType;
	@FXML Label deviceNumber;
	@FXML ImageView deviceModeIcon;
	private DyIOChannel channel;
	private  XYChart.Series<Integer, Integer> series = new XYChart.Series<Integer, Integer>();
	private long startTime=0;
	
	public void setChannel(DyIOChannel chan){
		this.channel = chan;

		startTime=System.currentTimeMillis();
		
		channel.addChannelModeChangeListener(newMode -> {
			Image image;
			try {
				image = new Image(
						DyIOConsole.class
								.getResourceAsStream("images/icon-"
										+ newMode.toSlug()+ ".png"));
			} catch (NullPointerException e) {
				image = new Image(
						DyIOConsole.class
								.getResourceAsStream("images/icon-off.png"));
			}
			deviceModeIcon.setImage(image);	
			series.setName(newMode.toSlug()+" values");
		});
		
		channel.addChannelEventListener(dyioEvent -> {
			Platform.runLater(()->{
				chanValue.setText(new Integer(dyioEvent.getValue()).toString());
				positionSlider.setValue(dyioEvent.getValue());
		        //populating the series with data
		        //series.getData().add(new XYChart.Data<Integer, Integer>(1, 23));
			});
			
		});
		
	}

	@FXML public void onListenerButtonClicked(ActionEvent event) {
		
		
	}

}
