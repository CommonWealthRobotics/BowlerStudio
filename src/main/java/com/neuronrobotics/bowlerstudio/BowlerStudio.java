package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.bowlerkernel.BowlerKernelBuildInfo;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.assets.BowlerStudioResourceFactory;
import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;
import com.neuronrobotics.bowlerstudio.scripting.ArduinoLoader;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingFileWidget;
import com.neuronrobotics.bowlerstudio.scripting.StlLoader;
import com.neuronrobotics.bowlerstudio.threed.MobileBaseCadManager;
import com.neuronrobotics.imageprovider.NativeResource;
import com.neuronrobotics.imageprovider.OpenCVJNILoader;
import com.neuronrobotics.javacad.JavaCadBuildInfo;
import com.neuronrobotics.replicator.driver.Slic3r;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.FirmataLink;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.ByteList;
import com.neuronrobotics.sdk.common.DeviceManager;
import com.neuronrobotics.sdk.common.IDeviceAddedListener;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.config.SDKBuildInfo;
import com.neuronrobotics.sdk.util.ThreadUtil;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.parametrics.CSGDatabase;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHRepository;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.hid4java.*;
import org.hid4java.event.HidServicesEvent;

public class BowlerStudio extends Application {

	private static TextArea log;
	private static Stage primaryStage;
	private static Scene scene;
	private static FXMLLoader fxmlLoader;
	private static boolean hasnetwork;
	private static Console out;
	private static TextArea logViewRefStatic = null;
	private static CreatureLab3dController creatureLab3dController;
	private BowlerStudioModularFrame modularFrame;
	private static String firstVer = "";
	private static Graphics2D splashGraphics;
	final static SplashScreen splash = SplashScreen.getSplashScreen();

	private static class Console extends OutputStream {
		private static final int LengthOfOutputLog = 5000;
		ByteList incoming = new ByteList();
		Thread update = new Thread() {
			public void run() {
				while (true) {
					ThreadUtil.wait(150);
					String text = incoming.asString();
					incoming.clear();
					if (text != null && text.length() > 0)
						appendText(text);
				}
			}

		};

		public Console() {
			update.start();
		}

		public void appendText(String valueOf) {
			try {
				BowlerStudioModularFrame.getBowlerStudioModularFrame().showTerminal();
			} catch (NullPointerException ex) {
				// frame not open yet
			}
			if (getLogViewRefStatic() != null) {
				String text = getLogViewRefStatic().getText();
				if (text.length() > LengthOfOutputLog) {

					Platform.runLater(() -> {

						getLogViewRefStatic().deleteText(0, text.length() - LengthOfOutputLog);

						getLogViewRefStatic().appendText(valueOf);

					});
				} else {
					Platform.runLater(() -> {
						getLogViewRefStatic().appendText(valueOf);

					});
				}
			}
			System.err.print(valueOf);
		}

		public void write(int b) throws IOException {
			incoming.add(b);
			// appendText(String.valueOf((char)b));
			// if(b=='[')
			// new RuntimeException().printStackTrace();
		}
	}

	public static OutputStream getOut() {
		if (out == null)
			out = new Console();
		return out;
	}

	static {
		// These must be changed before anything starts
		PrintStream ps = new PrintStream(getOut());
		// System.setErr(ps);
		System.setOut(ps);

	}

	public static void select(MobileBase base) {
		ArrayList<CSG> csg = MobileBaseCadManager.get(base).getBasetoCadMap().get(base);
		BowlerStudioModularFrame.getBowlerStudioModularFrame().getJfx3dmanager().setSelectedCsg(csg.get(0));
		BowlerStudioModularFrame.getBowlerStudioModularFrame().getJfx3dmanager().setSelectedCsg(csg);
	}

	public static void select(MobileBase base, DHParameterKinematics limb) {
		ArrayList<CSG> limCad = MobileBaseCadManager.get(base).getDHtoCadMap().get(limb);
		try {
			BowlerStudioModularFrame.getBowlerStudioModularFrame().getJfx3dmanager()
					.setSelectedCsg(limCad.get(limCad.size() - 1));
		} catch (NullPointerException ex) {
			// initialization has no csgs yet
		}
		BowlerStudioModularFrame.getBowlerStudioModularFrame().getJfx3dmanager().setSelectedCsg(limCad);
	}

	public static void select(MobileBase base, LinkConfiguration limb) {
		ArrayList<CSG> limCad = MobileBaseCadManager.get(base).getLinktoCadMap().get(limb);
		BowlerStudioModularFrame.getBowlerStudioModularFrame().getJfx3dmanager()
				.setSelectedCsg(limCad.get(limCad.size() - 1));
		BowlerStudioModularFrame.getBowlerStudioModularFrame().getJfx3dmanager().setSelectedCsg(limCad);
	}

	public static void select(File script, int lineNumber) {
		BowlerStudioModularFrame.getBowlerStudioModularFrame().getJfx3dmanager().setSelectedCsg(script, lineNumber);
	}

	/**
	 * @param args
	 *            the command line arguments
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "restriction" })
	public static void main(String[] args) throws Exception {
		HidServices hidServices = HidManager.getHidServices();

		hidServices.addHidServicesListener(new HidServicesListener() {

			@Override
			public void hidFailure(HidServicesEvent event) {
				// TODO Auto-generated method stub
				System.err.println(event);

			}

			@Override
			public void hidDeviceDetached(HidServicesEvent event) {
				// TODO Auto-generated method stub
				System.err.println(event);

			}

			@Override
			public void hidDeviceAttached(HidServicesEvent event) {
				// TODO Auto-generated method stub
				System.err.println(event);

			}
		});
		// Provide a list of attached devices
		for (HidDevice hidDevice : hidServices.getAttachedHidDevices()) {
			System.out.println("Device: " + hidDevice);
			if (hidDevice.isVidPidSerial(0x3742, 0x7, null)) {
				System.out.println("Found! " + hidDevice);
				hidDevice.open();
				// Send the Initialise message
				byte[] message = new byte[64];
				message[0] = 0x3f;
				message[1] = 0x23;
				message[2] = 0x23;

				int val = hidDevice.write(message, 64, (byte) 0);

				if (val != -1) {
					System.out.println("down number [" + val + "]");
				} else {
					System.err.println(hidDevice.getLastErrorMessage());
				}
				int read = hidDevice.read(message);
				System.out.println("up number [" + read + "]");

				for (int i = 0; i < read; i++) {
					System.out.println("up val [" + message[i] + "]");

				}
				hidDevice.close();
			}
		}

		// Clean shutdown
		hidServices.shutdown();

		if (splash != null) {
			try {
				splashGraphics = splash.createGraphics();
			} catch (IllegalStateException e) {
			}
		}
		renderSplashFrame(2, "Testing Internet Connection");
		try {
			final URL url = new URL("http://github.com");
			final URLConnection conn = url.openConnection();
			conn.connect();
			conn.getInputStream();
			setHasnetwork(true);
		} catch (Exception e) {
			// we assuming we have no access to the server and run off of the
			// cached gists.
			setHasnetwork(false);
		}
		CSG.setDefaultOptType(CSG.OptType.CSG_BOUND);
		CSG.setProgressMoniter((currentIndex, finalIndex, type, intermediateShape) -> {
			// Remove the default printing

		});

		if (args.length == 0) {
			renderSplashFrame(5, "Attempting to Log In...");
			// ScriptingEngine.logout();
			ScriptingEngine.setLoginManager(new GitHubLoginManager());
			try {
				ScriptingEngine.runLogin();
				renderSplashFrame(10, "Login OK!");
			} catch (Exception e) {
				// e.printStackTrace();
				ScriptingEngine.setupAnyonmous();
				renderSplashFrame(10, "No Login Found");
			}

			String myAssets = AssetFactory.getGitSource();

			if (ScriptingEngine.isLoginSuccess()) {

				if (BowlerStudio.hasNetwork()) {
					ScriptingEngine.setAutoupdate(true);

				}
				renderSplashFrame(15, "Loading Settings");
				firstVer = (String) ConfigurationDatabase.getObject("BowlerStudioConfigs", "firstVersion",
						StudioBuildInfo.getVersion());
				// String lastVersion = (String)
				// ConfigurationDatabase.getObject("BowlerStudioConfigs",
				// "skinBranch",
				// StudioBuildInfo.getVersion());
				myAssets = (String) ConfigurationDatabase.getObject("BowlerStudioConfigs", "skinRepo",
						"https://github.com/madhephaestus/BowlerStudioImageAssets.git");
				String lastVersion = ScriptingEngine.getBranch(myAssets);
				if (lastVersion == null) {
					System.err.println("deleting currupt Asset Repo " + myAssets);
					ScriptingEngine.deleteRepo(myAssets);
					ScriptingEngine.filesInGit(myAssets, StudioBuildInfo.getVersion(), null);
					lastVersion = ScriptingEngine.getBranch(myAssets);
				}
				System.err.println("Asset Repo " + myAssets);
				System.err.println("Asset current ver " + lastVersion);

				System.err.println("Asset intended ver " + StudioBuildInfo.getVersion());

				if (!lastVersion.contains(StudioBuildInfo.getVersion())) {
					renderSplashFrame(20, "Downloading Image Assets");

					System.err.println("\n\nnew version\n\n");
					removeAssets(myAssets);
					ConfigurationDatabase.setObject("BowlerStudioConfigs", "skinBranch", StudioBuildInfo.getVersion());
					// force the mainline in when a version update happens
					// this prevents developers from ending up with unsuable
					// version of BowlerStudio
					ConfigurationDatabase.setObject("BowlerStudioConfigs", "skinRepo", myAssets);
					ConfigurationDatabase.save();

				} else {
					System.err.println("Studio version is the same");
				}

				if (BowlerStudio.hasNetwork()) {
					renderSplashFrame(25, "Populating Menu");

					org.kohsuke.github.GitHub github = ScriptingEngine.getGithub();
					GHMyself self = github.getMyself();
					Map<String, GHRepository> myPublic = self.getAllRepositories();
					for (Map.Entry<String, GHRepository> entry : myPublic.entrySet()) {
						if (entry.getKey().contentEquals(AssetFactory.repo)) {
							GHRepository ghrepo = entry.getValue();
							myAssets = ghrepo.getGitTransportUrl().replaceAll("git://", "https://");

						}

					}

				}

				ScriptingEngine.filesInGit("https://github.com/CommonWealthRobotics/BowlerStudioConfiguration.git");

				// to set a new repo

			}
			renderSplashFrame(50, "Downloading Images");
			AssetFactory.setGitSource(
					(String) ConfigurationDatabase.getObject("BowlerStudioConfigs", "skinRepo", myAssets),
					(String) ConfigurationDatabase.getObject("BowlerStudioConfigs", "skinBranch",
							StudioBuildInfo.getVersion()));
			// Download and Load all of the assets
			try {
				renderSplashFrame(53, "Loading Images");

				AssetFactory.loadAsset("BowlerStudio.png");
			} catch (Exception ex) {
				renderSplashFrame(54, "Re-Loading Images");

				removeAssets(myAssets);

			}
			renderSplashFrame(56, "Loading resources");
			BowlerStudioResourceFactory.load();
			renderSplashFrame(60, "Downloading Vitamins");
			// load the vitimins repo so the demo is always snappy
			ScriptingEngine.pull("https://github.com/CommonWealthRobotics/BowlerStudioVitamins.git", null);
			ScriptingEngine.pull("https://github.com/madhephaestus/DefaultHaarCascade.git", null);
			renderSplashFrame(70, "Downloading tutorials");
			// load tutorials repo
			ScriptingEngine.fileFromGit("https://github.com/CommonWealthRobotics/CommonWealthRobotics.github.io.git",
					"master", // the default branch is source, so this needs to
								// be specified
					"index.html");
			renderSplashFrame(80, "Loding Example Robots");
			ScriptingEngine.fileFromGit("https://github.com/CommonWealthRobotics/BowlerStudioExampleRobots.git", // git
																													// repo,
																													// change
																													// this
																													// if
																													// you
																													// fork
																													// this
																													// demo
					"exampleRobots.json"// File from within the Git repo
			);
			CSGDatabase.setDbFile(new File(ScriptingEngine.getWorkspace().getAbsoluteFile() + "/csgDatabase.json"));

			// System.out.println("Loading assets ");

			// System.out.println("Loading Main.fxml");

			try {
				OpenCVJNILoader.load(); // Loads the JNI (java native interface)
			} catch (Exception | Error e) {
				// e.printStackTrace();
				// opencvOk=false;
				Platform.runLater(() -> {
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("OpenCV missing");
					alert.setHeaderText("Opencv library is missing");
					alert.setContentText(e.getMessage());
					alert.initModality(Modality.APPLICATION_MODAL);
					alert.show();
					e.printStackTrace(System.out);
				});

			}
			String arduino = "arduino";
			if (NativeResource.isLinux()) {

				Slic3r.setExecutableLocation("/usr/bin/slic3r");

			} else if (NativeResource.isWindows()) {
				String basedir = System.getenv("OPENCV_DIR");
				if (basedir == null)
					throw new RuntimeException(
							"OPENCV_DIR was not found, environment variable OPENCV_DIR needs to be set");
				System.err.println("OPENCV_DIR found at " + basedir);
				basedir += "\\..\\..\\..\\Slic3r_X64\\Slic3r\\slic3r.exe";
				Slic3r.setExecutableLocation(basedir);
				arduino = "C:\\Program Files (x86)\\Arduino\\arduino.exe";
				if (!new File(arduino).exists()) {
					arduino = "C:\\Program Files\\Arduino\\arduino.exe";

				}

			} else if (NativeResource.isOSX()) {
				arduino = "/Applications/Arduino.app/Contents/MacOS/Arduino";
			}
			try {
				if (!new File(arduino).exists() && !NativeResource.isLinux()) {
					boolean alreadyNotified = Boolean.getBoolean(ConfigurationDatabase
							.getObject("BowlerStudioConfigs", "notifiedArduinoDep", false).toString());
					if (!alreadyNotified) {
						ConfigurationDatabase.setObject("BowlerStudioConfigs", "notifiedArduinoDep", true);
						String adr = arduino;
						Platform.runLater(() -> {
							Alert alert = new Alert(AlertType.INFORMATION);
							alert.setTitle("Arduino is missing");
							alert.setHeaderText("Arduino expected at: " + adr);
							// alert.initModality(Modality.APPLICATION_MODAL);
							alert.show();

						});
					}
					new Thread() {
						public void run() {
							try {
								openExternalWebpage(new URL("https://www.arduino.cc/en/Main/Software"));
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}.start();

				}
				System.out.println("Arduino exec found at: " + arduino);
				ArduinoLoader.setARDUINOExec(arduino);
			} catch (Exception e) {

			}
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
				// This is a workaround for #8 and is only relavent on osx
				// it causes the SwingNodes not to load if not called way ahead
				// of time
				javafx.scene.text.Font.getFamilies();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			renderSplashFrame(90, "Loading STL Loader");
			// Add the engine handeler for STLs
			ScriptingEngine.addScriptingLanguage(new StlLoader());
			// add a new link provider to the link factory
			FirmataLink.addLinkFactory();
			// Log.enableInfoPrint();
			renderSplashFrame(92, "Done Application");
			// ThreadUtil.wait(100);

			launch(args);

		} else {
			BowlerKernel.main(args);
		}

	}

	private static void removeAssets(String myAssets)
			throws InvalidRemoteException, TransportException, GitAPIException, IOException, Exception {
		System.err.println("Clearing assets");
		ScriptingEngine.deleteRepo(myAssets);
		AssetFactory.setGitSource((String) ConfigurationDatabase.getObject("BowlerStudioConfigs", "skinRepo", myAssets),
				(String) ConfigurationDatabase.getObject("BowlerStudioConfigs", "skinBranch",
						StudioBuildInfo.getVersion()));
	}

	public static void closeSplash() {
		if (splashGraphics != null && splash.isVisible()) {
			splash.close();
			splashGraphics = null;
		}
	}

	public static void renderSplashFrame(int frame, String message) {

		if (splashGraphics != null && splash.isVisible()) {
			System.out.println(" Splash Rendering " + frame + " " + message);
			splashGraphics.setComposite(AlphaComposite.Clear);
			splashGraphics.fillRect(65, 270, 200, 40);
			splashGraphics.setPaintMode();
			splashGraphics.setColor(Color.WHITE);
			splashGraphics.drawString(frame + "% " + message, 65, 280);
			splash.update();
		}
	}

	/**
	 * open an external web page
	 * 
	 * @param uri
	 */
	public static void openExternalWebpage(URL uri) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(uri.toURI());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @author Sainath
	 * @version 1.0
	 * @param url
	 *            - The URL of the tab that needs to be opened
	 * @return None
	 */
	public static void openUrlInNewTab(URL url) {
		BowlerStudioModularFrame.getBowlerStudioModularFrame().openUrlInNewTab(url);
	}

	/**
	 * @author Sainath
	 * @version 1.0
	 * @param msg
	 *            - message that needs to be spoken
	 * @return an integer
	 */
	public static int speak(String msg) {

		return BowlerKernel.speak(msg);
	}

	public static ScriptingFileWidget createFileTab(File file) {
		return BowlerStudioModularFrame.getBowlerStudioModularFrame().createFileTab(file);
	}

	public static Scene getScene() {
		return scene;
	}

	public static void setScene(Scene s) {
		scene = s;
	}

	public static void clearConsole() {

		Platform.runLater(() -> {
			if (getLogViewRefStatic() != null)
				getLogViewRefStatic().setText("");
		});
	}

	// public static void setOverlayLeft(Node tree) {
	// BowlerStudio.creatureLab3dController.setOverlayLeft(tree);
	// }

	// public static void clearOverlayLeft() {
	// BowlerStudio.creatureLab3dController.clearOverlayLeft();
	// }

	// public static void setOverlayTop(Group content) {
	// BowlerStudio.creatureLab3dController.setOverlayTop(content);
	// ;
	// }
	//
	// public static void clearOverlayTop() {
	// BowlerStudio.creatureLab3dController.clearOverlayTop();
	// }

	// public static void setOverlayTopRight(Group content) {
	// BowlerStudio.creatureLab3dController.setOverlayTopRight(content);
	// }

	// public static void clearOverlayTopRight() {
	// BowlerStudio.creatureLab3dController.clearOverlayTopRight();
	// }
	//
	// public static void setOverlayBottomRight(Group content) {
	// BowlerStudio.creatureLab3dController.setOverlayBottomRight(content);
	// }

	// public static void clearOverlayBottomRight() {
	// BowlerStudio.creatureLab3dController.clearOverlayBottomRight();
	// }

	public static boolean hasNetwork() {
		return hasnetwork;
	}

	public static void setHasnetwork(boolean hasnetwork) {
		BowlerStudio.hasnetwork = hasnetwork;
	}

	public static TextArea getLogViewRefStatic() {
		return logViewRefStatic;
	}

	public static void setLogViewRefStatic(TextArea logViewRefStatic) {
		BowlerStudio.logViewRefStatic = logViewRefStatic;
	}

	public static void setCreatureLab3d(CreatureLab3dController creatureLab3dController) {
		BowlerStudio.creatureLab3dController = creatureLab3dController;
	}

	@SuppressWarnings("restriction")
	@Override
	public void start(Stage primaryStage) throws Exception {

		BowlerStudioModularFrame.setPrimaryStage(primaryStage);
		// Initialize your logic here: all @FXML variables will have been
		// injected
		FXMLLoader mainControllerPanel;

		try {
			mainControllerPanel = AssetFactory.loadLayout("layout/BowlerStudioModularFrame.fxml");
			BowlerStudioModularFrame.setBowlerStudioModularFrame(new BowlerStudioModularFrame());
			mainControllerPanel.setController(BowlerStudioModularFrame.getBowlerStudioModularFrame());
			mainControllerPanel.setClassLoader(BowlerStudioModularFrame.class.getClassLoader());
			try {
				mainControllerPanel.load();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			renderSplashFrame(93, "Main Controller Loaded");

			Scene scene = new Scene(mainControllerPanel.getRoot(), 1024, 768, true);

			File f = AssetFactory.loadFile("layout/default.css");
			if (f == null || !f.exists())
				throw new RuntimeException("Style sheet does not exist");
			scene.getStylesheets().clear();
			scene.getStylesheets().add("file:///" + f.getAbsolutePath().replace("\\", "/"));

			primaryStage.setTitle("Bowler Studio");
			primaryStage.setScene(scene);
			primaryStage.show();
			primaryStage.setOnCloseRequest(arg0 -> {
				// ThreadUtil.wait(100);
				closeBowlerStudio();

			});
			primaryStage.setTitle("Bowler Studio: v " + StudioBuildInfo.getVersion());
			primaryStage.getIcons().add(AssetFactory.loadAsset("BowlerStudioTrayIcon.png"));

			primaryStage.setResizable(true);

			DeviceManager.addDeviceAddedListener(new IDeviceAddedListener() {

				@Override
				public void onNewDeviceAdded(BowlerAbstractDevice arg0) {
					System.err.println("Device connected: " + arg0);
					BowlerStudioModularFrame.getBowlerStudioModularFrame().showConectionManager();
				}

				@Override
				public void onDeviceRemoved(BowlerAbstractDevice arg0) {
				}
			});
			Log.enableDebugPrint(false);
			// Log.enableWarningPrint();
			// Log.enableDebugPrint();
			// Log.enableErrorPrint();
			System.out.println("BowlerStudio First Version: " + firstVer);
			System.out.println("Java-Bowler Version: " + SDKBuildInfo.getVersion());
			System.out.println("Bowler-Scripting-Kernel Version: " + BowlerKernelBuildInfo.getVersion());
			System.out.println("JavaCad Version: " + JavaCadBuildInfo.getVersion());
			System.out.println("Welcome to BowlerStudio!");
			closeSplash();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void closeBowlerStudio() {
		new Thread() {
			public void run() {
				System.err.println("Closing application");
				ConnectionManager.disconnectAll();
				if (ScriptingEngine.isLoginSuccess())
					ConfigurationDatabase.save();
				System.exit(0);
			}
		}.start();
	}

	public static void printStackTrace(Exception e) {
		printStackTrace(e, null);
	}

	public static void printStackTrace(Exception e, File sourceFile) {
		BowlerStudioController.highlightException(sourceFile, e);
	}

	public static void println(CSG... toDisplay) {
		BowlerStudioController.setCsg(Arrays.asList(toDisplay));
	}

	public static void println(ArrayList<CSG> toDisplay) {
		BowlerStudioController.setCsg(toDisplay);
	}

	public static void print(CSG... toDisplay) {
		for (CSG c : Arrays.asList(toDisplay))
			BowlerStudioController.addCsg(c);
	}

	public static void print(ArrayList<CSG> toDisplay) {
		for (CSG c : toDisplay)
			BowlerStudioController.addCsg(c);
	}
}
