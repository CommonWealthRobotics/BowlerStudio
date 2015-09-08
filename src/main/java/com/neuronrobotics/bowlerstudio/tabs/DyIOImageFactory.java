package com.neuronrobotics.bowlerstudio.tabs;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.neuronrobotics.nrconsole.plugin.DyIO.DyIOConsole;
import com.neuronrobotics.sdk.dyio.DyIOChannelMode;

import javafx.scene.image.Image;

public class DyIOImageFactory {
	private static final Map<DyIOChannelMode,Image> lookup = new HashMap<DyIOChannelMode,Image>();
	private static Image chanHighlight;
	private static Image chanUpdate;
	private static Image chanDefault;
	static {
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
	
	public static void load(){}//stub to force a load from the static in a specific thread
	
	public static Image getModeImage(DyIOChannelMode mode){
		return lookup.get(mode);
	}

	public static Image getChanHighlight() {
		return chanHighlight;
	}

	public static void setChanHighlight(Image chanHighlight) {
		DyIOImageFactory.chanHighlight = chanHighlight;
	}

	public static Image getChanUpdate() {
		return chanUpdate;
	}

	public static void setChanUpdate(Image chanUpdate) {
		DyIOImageFactory.chanUpdate = chanUpdate;
	}

	public static Image getChanDefault() {
		return chanDefault;
	}

	public static void setChanDefault(Image chanDefault) {
		DyIOImageFactory.chanDefault = chanDefault;
	}

}
