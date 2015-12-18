package com.neuronrobotics.bowlerstudio.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.nrconsole.plugin.DyIO.DyIOConsole;
import com.neuronrobotics.sdk.dyio.DyIOChannelMode;
import com.neuronrobotics.sdk.util.ThreadUtil;

import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;

public class BowlerStudioResourceFactory {
	private static final Map<DyIOChannelMode,Image> lookup = new HashMap<DyIOChannelMode,Image>();
	private static Image chanHighlight;
	private static Image chanUpdate;
	private static Image chanDefault;
	private static final ArrayList<FXMLLoader>fxmlLoaders=new ArrayList<FXMLLoader>();
	private static FXMLLoader mainPanel = new FXMLLoader(
            BowlerStudio.class.getResource("DyIOPanel.fxml"));
	private static FXMLLoader githubLogin = new FXMLLoader(
            BowlerStudio.class.getResource("githublogin.fxml"));
	static {


		
	}
	
	public static FXMLLoader getLoader(int channelIndex){
		return fxmlLoaders.get(channelIndex);
	}
	
	public static void load(){

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
				try {
					mainPanel.load();
			    } catch (IOException ex) {
			        Logger.getLogger(BowlerStudio.class.getName()).
			                log(Level.SEVERE, null, ex);
			    }
				
				try {
					githubLogin.load();
				} catch (IOException e) {
					Logger.getLogger(BowlerStudio.class.getName()).
		            log(Level.SEVERE, null, e);
				}
			
	}//stub to force a load from the static in a specific thread
	
	public static Image getModeImage(DyIOChannelMode mode){
		return lookup.get(mode);
	}

	public static Image getChanHighlight() {
		return chanHighlight;
	}

	public static void setChanHighlight(Image chanHighlight) {
		BowlerStudioResourceFactory.chanHighlight = chanHighlight;
	}

	public static Image getChanUpdate() {
		return chanUpdate;
	}

	public static void setChanUpdate(Image chanUpdate) {
		BowlerStudioResourceFactory.chanUpdate = chanUpdate;
	}

	public static Image getChanDefault() {
		return chanDefault;
	}

	public static void setChanDefault(Image chanDefault) {
		BowlerStudioResourceFactory.chanDefault = chanDefault;
	}

	public static FXMLLoader getMainPanel() {
		return mainPanel;
	}

	public static void setMainPanel(FXMLLoader mainPanel) {
		BowlerStudioResourceFactory.mainPanel = mainPanel;
	}

	public static FXMLLoader getGithubLogin() {
		return githubLogin;
	}

	public static void setGithubLogin(FXMLLoader githubLogin) {
		BowlerStudioResourceFactory.githubLogin = githubLogin;
	}

}
