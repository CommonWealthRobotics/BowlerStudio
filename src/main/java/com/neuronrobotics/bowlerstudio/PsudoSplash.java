package com.neuronrobotics.bowlerstudio;

import java.net.URISyntaxException;
//import java.awt.AlphaComposite;
//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.EventQueue;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
//import java.awt.RenderingHints;
//import java.awt.image.BufferedImage;
//import java.io.IOException;
import java.net.URL;
//
//import javax.imageio.ImageIO;
//import javax.swing.JFrame;
//import javax.swing.JPanel;
//import javax.swing.SwingUtilities;
//import javax.swing.UIManager;
//import javax.swing.UnsupportedLookAndFeelException;

import com.neuronrobotics.bowlerstudio.assets.StudioBuildInfo;
import com.neuronrobotics.bowlerstudio.scripting.DownloadManager;
import com.neuronrobotics.bowlerstudio.scripting.GitLogProgressMonitor;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.video.OSUtil;

import eu.mihosoft.vrl.v3d.Cube;
import eu.mihosoft.vrl.v3d.JavaFXInitializer;
import javafx.scene.paint.Color;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class PsudoSplash implements GitLogProgressMonitor {
	// private static Color TextColor = Color.WHITE;
	private static int versionX = 65;
	private static int versionY = 45;
	private static int messageX = 65;
	private static int messageY = 280;

	private static int logY = 120;
	private static int logX = 15;

	// JFrame interfaceFrame;
	private String message = "";
	private String log = "";
	private static URL resource = PsudoSplash.class.getResource("splash.png");

	private static PsudoSplash singelton = null;
	private long timeSinceLastUpdate = 0;
	private static URL resource2;
	private static Color TextColor;

	private Stage popupStage;
	private ImageView imageView;
	private Scene popupScene;
	private StackPane popupRoot;

	public static boolean isInitialized() {
		return singelton != null;
	}

	public static PsudoSplash get() {
		if (singelton == null)
			singelton = new PsudoSplash();
		return singelton;
	}

	public static void close() {
		if (singelton != null)
			singelton.closeSplashLocal();
		singelton = null;

	}

	@Override
	public void onUpdate(String update, Exception e) {
		// e.printStackTrace(System.err);
		log = update;
		updateSplash();

		int length = update.length();
		// com.neuronrobotics.sdk.common.Log.error(update.substring(0,
		// length>100?100:length));
	}

	public static int getVersionX() {
		return versionX;
	}

	public static void setVersionX(int x) {
		versionX = x;
	}

	public static int getVersionY() {
		return versionY;
	}

	public static void setVersionY(int y) {
		versionY = y;
	}

	private PsudoSplash() {

		Platform.runLater(() -> {

			try {
				popupStage = new Stage(StageStyle.TRANSPARENT);
			} catch (IllegalStateException ex) {
				JavaFXInitializer.go();
				popupStage = new Stage(StageStyle.TRANSPARENT);
			}
			// Use NONE modality to prevent the window from becoming disabled
			popupStage.initModality(Modality.NONE);

			// Always show on top
			popupStage.setAlwaysOnTop(true);

			popupRoot = new StackPane();

			// Load your image

			String path;
			try {
				path = resource.toURI().toString();
			} catch (URISyntaxException e) {
				e.printStackTrace();
				close();
				return;
			}
			System.out.println("Loading splash image: " + path);
			Image image = new Image(path);
			imageView = new ImageView(image);

			// Add the image to the popup root
			popupRoot.getChildren().add(imageView);

			popupScene = new Scene(popupRoot);
			popupScene.setFill(null); // Make scene background transparent

			popupStage.setScene(popupScene);

			// Optional: Allow the popup to be dragged
			final double[] xOffset = { 0 };
			final double[] yOffset = { 0 };

			popupRoot.setOnMousePressed(event -> {
				xOffset[0] = event.getSceneX();
				yOffset[0] = event.getSceneY();
			});

			popupRoot.setOnMouseDragged(event -> {
				popupStage.setX(event.getScreenX() - xOffset[0]);
				popupStage.setY(event.getScreenY() - yOffset[0]);
			});
			popupStage.show();
			updateSplash();
		});
	}

	boolean isVisableSplash() {
		if (popupStage == null)
			return false;
		return popupStage.isShowing();
	}

	private void closeSplashLocal() {
		Platform.runLater(() -> {
			popupStage.hide();
		});
	}

	void updateSplash() {
		if (popupScene != null)
			Platform.runLater(() -> {
				popupScene.setFill(null);
				popupScene.getStylesheets().clear();
				// Explicitly set an empty style
				popupRoot.setStyle("-fx-background-color: transparent;");
			}); // Make scene background transparent

	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		if (message.length() > 23) {
			this.message = message.subSequence(0, 23).toString();
			// new RuntimeException().printStackTrace();
		} else
			this.message = message;

		ScriptingEngine.addLogListener(this);
		DownloadManager.addLogListener(this);
		log = "";
	}

	public static URL getResource() {
		return resource;
	}

	public static void setResource(URL r) {
		resource = r;
	}

	public static Color getTextColor() {
		return TextColor;
	}

	public static void setTextColor(Color textColor) {
		TextColor = textColor;
	}

	public static int getMessageX() {
		return messageX;
	}

	public static void setMessageX(int messageX) {
		PsudoSplash.messageX = messageX;
	}

	public static int getMessageY() {
		return messageY;
	}

	public static void setMessageY(int messageY) {
		PsudoSplash.messageY = messageY;
	}

	public static int getLogY() {
		return logY;
	}

	public static void setLogY(int logY) {
		PsudoSplash.logY = logY;
	}

	public static int getLogX() {
		return logX;
	}

	public static void setLogX(int logX) {
		PsudoSplash.logX = logX;
	}

	public static void setTrayIcon(URL resource2) {
		PsudoSplash.resource2 = resource2;
	}

}
