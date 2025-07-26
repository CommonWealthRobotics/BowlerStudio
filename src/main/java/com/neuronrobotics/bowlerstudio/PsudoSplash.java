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

import com.neuronrobotics.bowlerstudio.assets.FontSizeManager;
import com.neuronrobotics.bowlerstudio.assets.StudioBuildInfo;
import com.neuronrobotics.bowlerstudio.scripting.DownloadManager;
import com.neuronrobotics.bowlerstudio.scripting.GitLogProgressMonitor;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;

import eu.mihosoft.vrl.v3d.JavaFXInitializer;
import javafx.scene.paint.Color;
import javafx.application.Platform;
import javafx.scene.Scene;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class PsudoSplash implements GitLogProgressMonitor {
	// Static configuration
	private static int versionX = 65;
	private static int versionY = 45;
	private static int messageX = 65;
	private static int messageY = 280;
	private static int logY = 120;
	private static int logX = 15;
	private static PsudoSplash singelton = null;
	private static URL resource = PsudoSplash.class.getResource("splash.png");
	private static URL dockIcon;
	private static Color TextColor = Color.WHITE;

	// Class Variables
	private long timeOfLastUpdate = 0;
	private String message = "";
	private String log = "";
	private Stage popupStage;
	private ImageView imageView;
	private Scene popupScene;
	private AnchorPane popupRoot;
	private Label verL = new Label();
	private Label logL = new Label();
	private Label mesL = new Label();
	private double setWidth;
	private double scale;

	public static boolean isInitialized() {
		return singelton != null;
	}

	public static PsudoSplash get() {
		if (singelton == null)
			singelton = new PsudoSplash();
		if (!singelton.isVisableSplash()) {
			Platform.runLater(() -> {
				singelton.popupStage.show();
			});
			// new Exception("Opening Splash").printStackTrace();
		}
		return singelton;
	}

	public static void close() {
		if (singelton != null)
			singelton.closeSplashLocal();
	}

	@Override
	public void onLogUpdate(String update, Exception e) {
		// e.printStackTrace(System.err);
		log = update;
		if (isVisableSplash())
			updateSplash();
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

			popupRoot = new AnchorPane();

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
			double height = image.getHeight();
			double width = image.getWidth();

			setWidth = 500;

			scale = setWidth / width;
			double caclulatedHeight = scale * height;
			imageView.setFitWidth(setWidth);
			imageView.setFitHeight(caclulatedHeight);

			// Add the image to the popup root
			popupRoot.getChildren().add(imageView);
			popupRoot.getChildren().add(verL);
			popupRoot.getChildren().add(mesL);
			popupRoot.getChildren().add(logL);

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
			try {
				// CADoodle-Icon.png
				if (dockIcon != null) {
					Image loadAsset = new Image(dockIcon.toString());
					popupStage.getIcons().add(loadAsset);
					
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			popupStage.show();
			updateSplash();
		});
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FontSizeManager.addListener(fontNum -> {
			double tmp = FontSizeManager.getImageScale() * 14;
			mesL.setStyle("-fx-font-size: " + ((int) tmp) + "pt");
			logL.setStyle("-fx-font-size: " + ((int) tmp) + "pt");
			verL.setStyle("-fx-font-size: " + ((int) tmp) + "pt");
		});
	}

	public static boolean isVisableSplash() {
		if (singelton.popupStage == null)
			return false;
		return singelton.popupStage.isShowing();
	}

	private void closeSplashLocal() {
		BowlerStudio.runLater(() -> {
			popupStage.hide();
		});
		new Thread(()->{
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			BowlerStudio.runLater(() -> {
				popupStage.hide();
			});
		}).start();
		if(!Platform.isFxApplicationThread())
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	void updateSplash() {
		if (System.currentTimeMillis() - timeOfLastUpdate < 100) {
			return;
		}
		timeOfLastUpdate = System.currentTimeMillis();
		if (popupScene != null) {
			// System.out.println("Updating Splash "+imageView.getFitWidth());
			Platform.runLater(() -> {

				popupScene.setFill(null);
				popupScene.getStylesheets().clear();
				// Explicitly set an empty style
				popupRoot.setStyle("-fx-background-color: transparent;");
				logL.setLayoutX(logX * scale);
				logL.setLayoutY(logY * scale);
				mesL.setLayoutX(messageX * scale);
				mesL.setLayoutY(messageY * scale);
				verL.setLayoutX(versionX * scale);
				verL.setLayoutY(versionY * scale);

				logL.setTextFill(TextColor);
				mesL.setTextFill(TextColor);
				verL.setTextFill(TextColor);

				logL.setText(log);
				mesL.setText(message);
				verL.setText(StudioBuildInfo.getVersion());
			}); // Make scene background transparent
		}
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		if (message.length() > 45) {
			this.message = message.subSequence(0, 45).toString();
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
		PsudoSplash.setDockIconResource(resource2);
	}

	public static URL getDockIconResource() {
		return dockIcon;
	}

	public static void setDockIconResource(URL resource2) {
		PsudoSplash.dockIcon = resource2;
	}

}
