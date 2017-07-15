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
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.scripting.GithubLoginFX;
import com.neuronrobotics.bowlerstudio.tabs.DyIOPanel;
import com.neuronrobotics.bowlerstudio.tabs.DyIOchannelWidget;
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
	private static FXMLLoader mainPanel;
	private static FXMLLoader githubLogin;
	private static FXMLLoader mainControllerPanel;

	private BowlerStudioResourceFactory() {
	}

	public static FXMLLoader getLoader(int channelIndex) {
		return fxmlLoaders.get(channelIndex);
	}

	@SuppressWarnings("restriction")
	public static void load() throws Exception {
		try {
			BowlerStudio.renderSplashFrame( 57,"Loading DyIO");
			mainPanel = AssetFactory.loadLayout("layout/DyIOPanel.fxml");
			//mainPanel.setController(new DyIOPanel());
			mainPanel.setClassLoader(DyIOPanel.class.getClassLoader());
			BowlerStudio.renderSplashFrame( 58,"Loading GitHub");

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
		

		for (DyIOChannelMode cm : EnumSet.allOf(DyIOChannelMode.class)) {
			Image image;
			//
			try {
				image = AssetFactory.loadAsset("dyio/icon-" + cm.toSlug() + ".png");
			} catch (NullPointerException e) {
				image = AssetFactory.loadAsset("dyio/icon-off.png");
			}
			lookup.put(cm, image);
		}
		setChanHighlight(AssetFactory.loadAsset("dyio/channel-highlight.png"));
		setChanDefault(AssetFactory.loadAsset("dyio/channel-default.png"));
		setChanUpdate(AssetFactory.loadAsset("dyio/channel-update.png"));

		for (int i = 0; i < 24; i++) {
			// generate the control widgets
			FXMLLoader fxmlLoader;
			try {
				fxmlLoader = AssetFactory.loadLayout("layout/DyIOChannelContorol.fxml", true);
				//fxmlLoader.setController(new DyIOchannelWidget());
				fxmlLoader.setClassLoader(DyIOchannelWidget.class.getClassLoader());
				try {
					fxmlLoader.load();
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
				fxmlLoaders.add(fxmlLoader);
			} catch (InvalidRemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransportException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GitAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
//		try {
//			mainPanel.load();
//		} catch (IOException ex) {
//			Logger.getLogger(BowlerStudio.class.getName()).log(Level.SEVERE, null, ex);
//		}
//
//		try {
//			githubLogin.load();
//		} catch (IOException e) {
//			Logger.getLogger(BowlerStudio.class.getName()).log(Level.SEVERE, null, e);
//		}

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

	public static FXMLLoader getMainControllerPanel() {
		return mainControllerPanel;
	}

	public static void setMainControllerPanel(FXMLLoader mainControllerPanel) {
		BowlerStudioResourceFactory.mainControllerPanel = mainControllerPanel;
	}

}
