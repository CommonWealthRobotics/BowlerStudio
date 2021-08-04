package com.neuronrobotics.bowlerstudio.assets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.scripting.GithubLoginFX;
//import com.neuronrobotics.nrconsole.plugin.DyIO.DyIOConsole;
import com.neuronrobotics.sdk.dyio.DyIOChannelMode;

import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;

public class BowlerStudioResourceFactory {
	private static final Map<DyIOChannelMode, Image> lookup = new HashMap<>();
	private static Image chanHighlight;
	private static Image chanUpdate;
	private static Image chanDefault;
	private static final ArrayList<FXMLLoader> fxmlLoaders = new ArrayList<>();
	private static FXMLLoader githubLogin;
	private static FXMLLoader mainControllerPanel;
	private static boolean loaded=false;
	private BowlerStudioResourceFactory() {
	}

	public static FXMLLoader getLoader(int channelIndex) {
		return fxmlLoaders.get(channelIndex);
	}

	@SuppressWarnings("restriction")
	public static void load() throws Exception {
		if(loaded)
			return;
		loaded=true;
		try {
			//mainPanel.setController(new DyIOPanel());
			BowlerStudio.renderSplashFrame( 95,"Loading GitHub");

			githubLogin = AssetFactory.loadLayout("layout/githublogin.fxml");
			//githubLogin.setController(new GithubLoginFX());
			githubLogin.setClassLoader(GithubLoginFX.class.getClassLoader());
		} catch (InvalidRemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransportException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (GitAPIException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		



		try {
			githubLogin.load();
		} catch (IOException e) {
			Logger.getLogger(BowlerStudio.class.getName()).log(Level.SEVERE, null, e);
		}

	}// stub to force a load from the static in a specific thread

	public static Image getModeImage(DyIOChannelMode mode) {
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


	public static FXMLLoader getGithubLogin() {
		return githubLogin;
	}

	public static void setGithubLogin(FXMLLoader githubLogin) {
		BowlerStudioResourceFactory.githubLogin = githubLogin;
	}

	public static FXMLLoader getMainControllerPanel() {
		return mainControllerPanel;
	}

	public static void setMainControllerPanel(FXMLLoader mainControllerPanel) {
		BowlerStudioResourceFactory.mainControllerPanel = mainControllerPanel;
	}

}
