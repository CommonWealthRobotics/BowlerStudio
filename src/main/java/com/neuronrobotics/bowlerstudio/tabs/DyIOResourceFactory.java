package com.neuronrobotics.bowlerstudio.tabs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.nrconsole.plugin.DyIO.DyIOConsole;
import com.neuronrobotics.sdk.dyio.DyIOChannelMode;

import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;

public class DyIOResourceFactory {
	private static final Map<DyIOChannelMode,Image> lookup = new HashMap<DyIOChannelMode,Image>();
	private static Image chanHighlight;
	private static Image chanUpdate;
	private static Image chanDefault;
	private static final ArrayList<FXMLLoader>fxmlLoaders=new ArrayList<FXMLLoader>();
	
	static {
		for(int i=0;i<24;i++){
			// generate the control widgets
			FXMLLoader fxmlLoader = new FXMLLoader(
		            BowlerStudio.class.getResource("DyIOChannelContorol.fxml"));
	        try {
	            fxmlLoader.load();
	        } catch (IOException ex) {
	            throw new RuntimeException(ex);
	        }
			fxmlLoaders.add(fxmlLoader);
		}
		for(DyIOChannelMode cm : EnumSet.allOf(DyIOChannelMode.class)) {
			Image image;
			//
			try {
				image = new Image(
						DyIOConsole.class
								.getResourceAsStream("images/icon-"
										+ cm.toSlug()+ ".png"));
			} catch (NullPointerException e) {
				image = new Image(
						DyIOConsole.class
								.getResourceAsStream("images/icon-off.png"));
			}
			lookup.put( cm, image);
		}
		setChanHighlight(new Image(
				DyIOConsole.class
				.getResourceAsStream("images/channel-highlight.png")));
		setChanDefault(new Image(
												DyIOConsole.class
																.getResourceAsStream("images/channel-default.png")));
		setChanUpdate(new Image(
						DyIOConsole.class
						.getResourceAsStream("images/channel-update.png")));
	}
	
	public static FXMLLoader getLoader(int channelIndex){
		return fxmlLoaders.get(channelIndex);
	}
	
	public static void load(){}//stub to force a load from the static in a specific thread
	
	public static Image getModeImage(DyIOChannelMode mode){
		return lookup.get(mode);
	}

	public static Image getChanHighlight() {
		return chanHighlight;
	}

	public static void setChanHighlight(Image chanHighlight) {
		DyIOResourceFactory.chanHighlight = chanHighlight;
	}

	public static Image getChanUpdate() {
		return chanUpdate;
	}

	public static void setChanUpdate(Image chanUpdate) {
		DyIOResourceFactory.chanUpdate = chanUpdate;
	}

	public static Image getChanDefault() {
		return chanDefault;
	}

	public static void setChanDefault(Image chanDefault) {
		DyIOResourceFactory.chanDefault = chanDefault;
	}

}
