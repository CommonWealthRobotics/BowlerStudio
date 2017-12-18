package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.bowlerkernel.BowlerKernelBuildInfo;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.assets.BowlerStudioResourceFactory;
import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;
import com.neuronrobotics.bowlerstudio.assets.StudioBuildInfo;
import com.neuronrobotics.bowlerstudio.creature.MobileBaseCadManager;
import com.neuronrobotics.bowlerstudio.scripting.ArduinoLoader;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingFileWidget;
import com.neuronrobotics.bowlerstudio.scripting.StlLoader;
import com.neuronrobotics.imageprovider.NativeResource;
import com.neuronrobotics.imageprovider.OpenCVJNILoader;
import com.neuronrobotics.javacad.JavaCadBuildInfo;
import com.neuronrobotics.replicator.driver.Slic3r;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.FirmataLink;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.common.*;
import com.neuronrobotics.sdk.config.SDKBuildInfo;
import com.neuronrobotics.sdk.util.ThreadUtil;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.parametrics.CSGDatabase;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.apache.commons.io.IOUtils;
import org.dockfx.DockPane;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.PatchFormatException;
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

@SuppressWarnings("restriction")
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
	private static Stage primaryStage2;
	private static File layoutFile;

	private static class Console extends OutputStream {
		private static final int LengthOfOutputLog = 5000;
		ByteList incoming = new ByteList();
		Thread update = new Thread() {
			public void run() {
				while (true) {
					ThreadUtil.wait(150);
					try{
						String text = incoming.asString();
						incoming.clear();
						if (text != null && text.length() > 0)
							appendText(text);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}

		};

		public Console() {
			update.start();
		}

		@SuppressWarnings("restriction")
		public void appendText(String valueOf) {
			if (BowlerStudioModularFrame.getBowlerStudioModularFrame() == null) {
				return;
			}
			try {
				BowlerStudioModularFrame.getBowlerStudioModularFrame().showTerminal();
			} catch (Exception ex) {
				// frame not open yet
				ex.printStackTrace();
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

	public static MobileBase loadMobileBaseFromGit(String id, String file) throws Exception {
		String xmlContent = ScriptingEngine.codeFromGit(id, file)[0];
		MobileBase mb = new MobileBase(IOUtils.toInputStream(xmlContent, "UTF-8"));

		mb.setGitSelfSource(new String[] { id, file });
		// ConnectionManager.addConnection(mb, mb.getScriptingName());
		return mb;
	}

	public static void select(MobileBase base) {
		if(BowlerStudioModularFrame.getBowlerStudioModularFrame().getJfx3dmanager().isAutoHightlight()){
		  MobileBaseCadManager.get(base).selectCsgByMobileBase(base);
		}
		/*
		try {

			ArrayList<CSG> csg = MobileBaseCadManager.get(base).getBasetoCadMap().get(base);
			BowlerStudioModularFrame.getBowlerStudioModularFrame().getJfx3dmanager().setSelectedCsg(csg.get(0));
			BowlerStudioModularFrame.getBowlerStudioModularFrame().getJfx3dmanager().setSelectedCsg(csg);
		} catch (Exception ex) {
			System.err.println("Base not loaded yet");
		}
		*/

	}

	public static void select(MobileBase base, DHParameterKinematics limb) {
		if(BowlerStudioModularFrame.getBowlerStudioModularFrame().getJfx3dmanager().isAutoHightlight()){
		  MobileBaseCadManager.get(base).selectCsgByLimb(base, limb);
		}
		/*
		try {
	
			ArrayList<CSG> limCad = MobileBaseCadManager.get(base).getDHtoCadMap().get(limb);
			try {
				BowlerStudioModularFrame.getBowlerStudioModularFrame().getJfx3dmanager()
						.setSelectedCsg(limCad.get(limCad.size() - 1));
			} catch (Exception ex) {
				// initialization has no csgs yet
			}
			BowlerStudioModularFrame.getBowlerStudioModularFrame().getJfx3dmanager().setSelectedCsg(limCad);
		} catch (Exception ex) {
			System.err.println("Limb not loaded yet");
		}
		*/
	}

	public static void select(MobileBase base, LinkConfiguration limb) {
		if(BowlerStudioModularFrame.getBowlerStudioModularFrame().getJfx3dmanager().isAutoHightlight()){
		  MobileBaseCadManager.get(base).selectCsgByLink(base, limb);
		}
		/*
		try {

			ArrayList<CSG> limCad = MobileBaseCadManager.get(base).getLinktoCadMap().get(limb);
			BowlerStudioModularFrame.getBowlerStudioModularFrame().getJfx3dmanager()
					.setSelectedCsg(limCad.get(limCad.size() - 1));
			BowlerStudioModularFrame.getBowlerStudioModularFrame().getJfx3dmanager().setSelectedCsg(limCad);
		} catch (Exception ex) {
			System.err.println("Limb not loaded yet");
		}
		*/
	}

	public static void select(File script, int lineNumber) {
		if(BowlerStudioModularFrame.getBowlerStudioModularFrame().getJfx3dmanager().isAutoHightlight())
		try {
			BowlerStudioModularFrame.getBowlerStudioModularFrame().getJfx3dmanager().setSelectedCsg(script, lineNumber);
		} catch (Exception ex) {
			System.err.println("File not found");
		}
	}

	/**
	 * @param args
	 *            the command line arguments
	 * @throws Exception
	 */

	@SuppressWarnings({ "unchecked", "restriction" })
	public static void main(String[] args) throws Exception {
		new JFXPanel();
		Log.enableWarningPrint();
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
		StudioBuildInfo.setBaseBuildInfoClass(BowlerStudio.class);
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
				ScriptingEngine.filesInGit(myAssets, StudioBuildInfo.getVersion(), null);
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

				if (lastVersion==null || !StudioBuildInfo.getVersion().contains(lastVersion)) {
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
				System.err.println("Populating menu");

				if (BowlerStudio.hasNetwork()) {
					renderSplashFrame(25, "Populating Menu");
				}
			}

			renderSplashFrame(50, "Downloading Images");
			Tutorial.getHomeUrl(); // Dowload and launch the Tutorial server
			// force the current version in to the version number
			ConfigurationDatabase.setObject("BowlerStudioConfigs", "skinBranch", StudioBuildInfo.getVersion());
			AssetFactory.setGitSource(
					(String) ConfigurationDatabase.getObject("BowlerStudioConfigs", "skinRepo", myAssets),
					StudioBuildInfo.getVersion());
			// Download and Load all of the assets
			try {
				renderSplashFrame(53, "Loading Images");

				AssetFactory.loadAsset("BowlerStudio.png");
			} catch (Exception ex) {
				renderSplashFrame(54, "Re-Loading Images");

				removeAssets(myAssets);

			}

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
					// alert.show();
					// e.printStackTrace(System.out);
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
							// alert.show();
							// new Thread() {
							// public void run() {
							// try {
							// openExternalWebpage(new
							// URL("https://www.arduino.cc/en/Main/Software"));
							// } catch (Exception e) {
							// // TODO Auto-generated catch block
							// e.printStackTrace();
							// }
							// }
							// }.start();
						});

					}

				}
				System.out.println("Arduino exec found at: " + arduino);
				ArduinoLoader.setARDUINOExec(arduino);
			} catch (Exception e) {
				e.printStackTrace();
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
			layoutFile = AssetFactory.loadFile("layout/default.css");
			if (layoutFile == null || !layoutFile.exists())
				throw new RuntimeException("Style sheet does not exist");
			ScriptingEngine.gitScriptRun("https://github.com/CommonWealthRobotics/HotfixBowlerStudio.git", "hotfix.groovy", null);
			launch();

		} else {
			BowlerKernel.main(args);
		}

	}

	private static void removeAssets(String myAssets)
			throws InvalidRemoteException, TransportException, GitAPIException, IOException, Exception {
		System.err.println("Clearing assets");
		ScriptingEngine.deleteRepo(myAssets);
		AssetFactory.setGitSource((String) ConfigurationDatabase.getObject("BowlerStudioConfigs", "skinRepo", myAssets),
				StudioBuildInfo.getVersion());
	}

	public static void closeSplash() {
		if (splashGraphics != null && splash.isVisible()) {
			splash.close();
			splashGraphics = null;
		}
	}

	public static void renderSplashFrame(int frame, String message) {

		System.err.println(" Splash Rendering " + frame + " " + message);
		if (splashGraphics != null && splash.isVisible()) {
			splashGraphics.setComposite(AlphaComposite.Clear);
			splashGraphics.fillRect(65, 270, 200, 40);
			splashGraphics.setPaintMode();
			splashGraphics.setColor(Color.WHITE);
			splashGraphics.drawString(frame + "% " + message, 65, 280);
			// Platform.runLater(() -> {
			splash.update();
			// });
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

	@SuppressWarnings("restriction")
	public static Scene getScene() {
		return scene;
	}

	@SuppressWarnings("restriction")
	public static void setScene(Scene s) {
		scene = s;
	}

	@SuppressWarnings("restriction")
	public static void clearConsole() {

		Platform.runLater(() -> {
			if (getLogViewRefStatic() != null)
				getLogViewRefStatic().setText("");
		});
	}

	public static boolean hasNetwork() {
		return hasnetwork;
	}

	public static void setHasnetwork(boolean hasnetwork) {
		BowlerStudio.hasnetwork = hasnetwork;
	}

	@SuppressWarnings("restriction")
	public static TextArea getLogViewRefStatic() {
		return logViewRefStatic;
	}

	public static void setLogViewRefStatic(@SuppressWarnings("restriction") TextArea logViewRefStatic) {
		BowlerStudio.logViewRefStatic = logViewRefStatic;
	}

	public static void setCreatureLab3d(CreatureLab3dController creatureLab3dController) {
		BowlerStudio.creatureLab3dController = creatureLab3dController;
	}

	@SuppressWarnings("restriction")
	@Override
	public void start(Stage primaryStage) {
		try { // do this ...
			Thread thread = Thread.currentThread();
			if (thread.getContextClassLoader() == null) {
				// seriously Apple??
				System.err.println("ContextClassLoader Is Missing! (OSX) ");
				thread.setContextClassLoader(getClass().getClassLoader()); // a
																			// valid
																			// ClassLoader
																			// from
																			// somewhere
																			// else
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		System.err.println("Class loader: " + Thread.currentThread().getContextClassLoader());
		new Thread(() -> {
			try {

				String stylesheet = Application.STYLESHEET_MODENA;// "MODENA" or
																	// "CASPIAN"
				// System.setProperty("javax.userAgentStylesheetUrl",
				// stylesheet);
				setUserAgentStylesheet(stylesheet);
			} catch (Exception | Error e) {
				e.printStackTrace();
			}
			// These must be changed before anything starts
			PrintStream ps = new PrintStream(getOut());
			// System.setErr(ps);
			System.setOut(ps);
			renderSplashFrame(93, "Loading resources");
			try {
				BowlerStudioResourceFactory.load();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			primaryStage2 = primaryStage;
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
				renderSplashFrame(94, "Main Controller Loaded");

				Scene scene = new Scene(mainControllerPanel.getRoot(), 1024, 768, true);

				String nwfile = layoutFile.toURI().toString().replace("file:/", "file:///");

				scene.getStylesheets().clear();
				scene.getStylesheets().add(nwfile);
				System.err.println("Loading CSS from " + nwfile);
				Platform.runLater(() -> {

					primaryStage.setScene(scene);
					System.err.println("Showing main applicaiton");
					primaryStage.show();
					// initialize the default styles for the dock pane and
					// undocked
					// nodes using the DockFX
					// library's internal Default.css stylesheet
					// unlike other custom control libraries this allows the
					// user to
					// override them globally
					// using the style manager just as they can with internal
					// JavaFX
					// controls
					// this must be called after the primary stage is shown
					// https://bugs.openjdk.java.net/browse/JDK-8132900
					DockPane.initializeDefaultUserAgentStylesheet();
				});

				primaryStage.setOnCloseRequest(arg0 -> {
					// ThreadUtil.wait(100);
					closeBowlerStudio();

				});
				Platform.runLater(() -> {
					primaryStage.setTitle("Bowler Studio: v " + StudioBuildInfo.getVersion());

					try {
						primaryStage.getIcons().add(AssetFactory.loadAsset("BowlerStudioTrayIcon.png"));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
				;

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
		}).start();

	}

	@SuppressWarnings("restriction")
	public static void closeBowlerStudio() {
		Platform.runLater(() -> {
			primaryStage2.hide();
		});
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
