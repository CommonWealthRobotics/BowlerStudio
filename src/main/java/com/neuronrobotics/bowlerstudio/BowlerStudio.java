package com.neuronrobotics.bowlerstudio;

//import com.neuronrobotics.kinematicschef.InverseKinematicsEngine;
import com.neuronrobotics.bowlerkernel.BowlerKernelBuildInfo;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.assets.BowlerStudioResourceFactory;
import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;
import com.neuronrobotics.bowlerstudio.assets.StudioBuildInfo;
import com.neuronrobotics.bowlerstudio.creature.MobileBaseCadManager;
import com.neuronrobotics.bowlerstudio.creature.MobileBaseLoader;
import com.neuronrobotics.bowlerstudio.scripting.ArduinoLoader;
import com.neuronrobotics.bowlerstudio.scripting.GitHubWebFlow;
import com.neuronrobotics.bowlerstudio.scripting.PasswordManager;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingFileWidget;
import com.neuronrobotics.bowlerstudio.scripting.StlLoader;
import com.neuronrobotics.bowlerstudio.util.FileChangeWatcher;
import com.neuronrobotics.imageprovider.NativeResource;
//import com.neuronrobotics.imageprovider.OpenCVJNILoader;
import com.neuronrobotics.javacad.JavaCadBuildInfo;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.FirmataLink;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.*;
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
import javafx.scene.transform.Affine;
import javafx.stage.Stage;

import org.dockfx.DockPane;
import org.reactfx.util.FxTimer;
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
import java.util.List;

@SuppressWarnings("restriction")
public class BowlerStudio extends Application {
	final static SplashScreen splash = null;// = SplashScreen.getSplashScreen();
	private static Scene scene;
	private static boolean hasnetwork;
	private static Console out;
	private static TextArea logViewRefStatic = null;
	private static String firstVer = "";

	private static Stage primaryStage2;
	private static File layoutFile;
	private static boolean deleteFlag = false;
	private static IssueReportingExceptionHandler reporter = new IssueReportingExceptionHandler();
	//private static String lastVersion;

	private static class Console extends OutputStream {
		private static final int LengthOfOutputLog = 5000;
		ByteList incoming = new ByteList();
		Thread update = new Thread() {
			public void run() {
				Thread.currentThread().setUncaughtExceptionHandler(new IssueReportingExceptionHandler());

				while (true) {
					ThreadUtil.wait(150);
					if (incoming.size() > 0)
						try {
							String text = incoming.asString();
							incoming.clear();
							if (text != null && text.length() > 0)
								appendText(text);
							text = null;
						} catch (Exception e) {
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
						try {
							getLogViewRefStatic().deleteText(0, text.length() - LengthOfOutputLog);

							getLogViewRefStatic().appendText(valueOf);
						} catch (Throwable t) {
						}

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
		return MobileBaseLoader.fromGit(id, file);
	}

	public static void select(MobileBase base) {
		if (CreatureLab3dController.getEngine().isAutoHightlight()) {
			MobileBaseCadManager.get(base).selectCsgByMobileBase(base);
		}
		/*
		 * try {
		 * 
		 * ArrayList<CSG> csg =
		 * MobileBaseCadManager.get(base).getBasetoCadMap().get(base);
		 * CreatureLab3dController.getEngine(). setSelectedCsg(csg.get(0));
		 * CreatureLab3dController.getEngine(). setSelectedCsg(csg); } catch (Exception
		 * ex) { System.err.println("Base not loaded yet"); }
		 */

	}

	public static void select(MobileBase base, DHParameterKinematics limb) {
		if (CreatureLab3dController.getEngine().isAutoHightlight()) {
			MobileBaseCadManager.get(base).selectCsgByLimb(base, limb);
		}
		/*
		 * try {
		 * 
		 * ArrayList<CSG> limCad =
		 * MobileBaseCadManager.get(base).getDHtoCadMap().get(limb); try {
		 * CreatureLab3dController.getEngine() .setSelectedCsg(limCad.get(limCad.size()
		 * - 1)); } catch (Exception ex) { // initialization has no csgs yet }
		 * CreatureLab3dController.getEngine(). setSelectedCsg(limCad); } catch
		 * (Exception ex) { System.err.println("Limb not loaded yet"); }
		 */
	}
	public static void select(Affine globalPositionListener) {
		if (CreatureLab3dController.getEngine().isAutoHightlight()) {
			CreatureLab3dController.getEngine().setSelected(globalPositionListener);
		}
	}
	public static void select(MobileBase base, LinkConfiguration limb) {
		if (CreatureLab3dController.getEngine().isAutoHightlight()) {
			MobileBaseCadManager.get(base).selectCsgByLink(base, limb);
		}
		/*
		 * try {
		 * 
		 * ArrayList<CSG> limCad =
		 * MobileBaseCadManager.get(base).getLinktoCadMap().get(limb);
		 * CreatureLab3dController.getEngine() .setSelectedCsg(limCad.get(limCad.size()
		 * - 1)); CreatureLab3dController.getEngine(). setSelectedCsg(limCad); } catch
		 * (Exception ex) { System.err.println("Limb not loaded yet"); }
		 */
	}

	public static void select(File script, int lineNumber) {
		if (CreatureLab3dController.getEngine().isAutoHightlight())
			try {
				CreatureLab3dController.getEngine().setSelectedCsg(script, lineNumber);
			} catch (Exception ex) {
				System.err.println("File not found");
			}
	}
	
	public static TransformNR getCamerFrame() {
		return CreatureLab3dController.getEngine().getFlyingCamera().getCamerFrame();
	}
	
	public static double getCamerDepth() {
		return CreatureLab3dController.getEngine().getFlyingCamera().getZoomDepth();
	}
	
	/**
	 * @param args the command line arguments
	 * @throws Exception
	 */

	@SuppressWarnings({ "unchecked", "restriction" })
	public static void main(String[] args) throws Exception {
		System.setOut(System.err);// send all prints to err until replaced with the terminal
		net.java.games.input.ControllerEnvironment.getDefaultEnvironment();

		Thread.currentThread().setUncaughtExceptionHandler(new IssueReportingExceptionHandler());
		if (!StudioBuildInfo.isOS64bit()) {

			Platform.runLater(() -> {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("32 Bit Java Detected");
				alert.setHeaderText("Insuffient Ram Capibilities in 32 bit mode");
				alert.setContentText("This applications uses more that 4gb of ram\nA 32 bit JVM mode detected: "
						+ System.getProperty("os.arch"));
				alert.showAndWait();
				System.exit(1);
			});
		}
		Log.enableWarningPrint();

		renderSplashFrame(2, "Testing Internet");

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
		eu.mihosoft.vrl.v3d.svg.SVGLoad.getProgressDefault();
//		eu.mihosoft.vrl.v3d.svg.SVGLoad.setProgressDefault(new ISVGLoadProgress() {
//			@Override
//			public void onShape(CSG newShape) {
//				BowlerStudioController.addCsg(newShape);
//			}
//		});
		StudioBuildInfo.setBaseBuildInfoClass(BowlerStudio.class);
		if (args.length != 0) {
			System.err.println("Arguments detected, starting Kernel mode.");
			SplashManager.closeSplash();
			BowlerKernel.main(args);
		}else
		{
			renderSplashFrame(5, "Loging In...");
			// ScriptingEngine.logout();
			// switching to Web Flow auth
			List<String> listOfScopes = Arrays.asList("repo", "gist", 
					"user","admin:org","delete_repo","workflow");
			PasswordManager.setListOfScopes(listOfScopes);
			GitHubWebFlow.setMyAPI(()->{
				String line = System.getProperty("API-ID");
				if(line!=null)
					return line;
				return "1edf79fae494c232d4d2";
			});
			NameGetter mykey = new NameGetter();
			GitHubWebFlow.setName(mykey);
			String myAssets = AssetFactory.getGitSource();
			if (PasswordManager.hasNetwork()) {
				System.err.println("Attempt to log in with disk credentials");
				ScriptingEngine.waitForLogin();
				if (ScriptingEngine.isLoginSuccess()) {

					if (BowlerStudio.hasNetwork()) {					
						ScriptingEngine.setAutoupdate(true);

					}
					renderSplashFrame(15, "Load Configs");
					try {
						firstVer = (String) ConfigurationDatabase.getObject("BowlerStudioConfigs", "firstVersion",
								StudioBuildInfo.getVersion());
					} catch (Throwable t) {
						System.err.println("Resetting the configs repo...");
						// clear the configs repo
						ScriptingEngine.deleteRepo(ConfigurationDatabase.getGitSource());
						firstVer = (String) ConfigurationDatabase.getObject("BowlerStudioConfigs", "firstVersion",
								StudioBuildInfo.getVersion());
					}
					ConfigurationDatabase.setObject("BowlerStudioConfigs", "currentVersion",
							StudioBuildInfo.getVersion());
					renderSplashFrame(16, "Done Load Configs");
					myAssets = (String) ConfigurationDatabase.getObject("BowlerStudioConfigs", "skinRepo",
							"https://github.com/madhephaestus/BowlerStudioImageAssets.git");
					renderSplashFrame(20, "DL'ing Image Assets");
					System.err.println("Asset Repo " + myAssets);

					System.err.println("Asset intended ver " + StudioBuildInfo.getVersion());
					ScriptingEngine.cloneRepo(myAssets, null);
					try {
						ScriptingEngine.pull(myAssets, "main");
						System.err.println("Studio version is the same");
					}catch(Exception e) {
						e.printStackTrace();
						ScriptingEngine.deleteRepo(myAssets);
						ScriptingEngine.cloneRepo(myAssets, null);
					}
					
					if(ScriptingEngine.checkOwner(myAssets)) {
						if(!ScriptingEngine.tagExists(myAssets, StudioBuildInfo.getVersion())) {
							System.out.println("Tagging Assets at "+StudioBuildInfo.getVersion());
							ScriptingEngine.tagRepo(myAssets, StudioBuildInfo.getVersion());
						}
					}

					if (BowlerStudio.hasNetwork()) {
						renderSplashFrame(25, "Populating Menu");
					}
				} else {
					renderSplashFrame(20, "DL'ing Image Assets");
					ScriptingEngine.cloneRepo(myAssets, null);
					ScriptingEngine.pull(myAssets, "main");
				}
			}
			layoutFile = AssetFactory.loadFile("layout/default.css");
			if (layoutFile == null || !layoutFile.exists()) {
				ScriptingEngine.deleteRepo(myAssets);

				throw new RuntimeException("Style sheet does not exist");
			}
			// SplashManager.setIcon(AssetFactory.loadAsset("BowlerStudioTrayIcon.png"));
			renderSplashFrame(50, "DL'ing Tutorials...");
			// load tutorials repo

			Tutorial.getHomeUrl(); // Dowload and launch the Tutorial server
			// force the current version in to the version number
			ConfigurationDatabase.setObject("BowlerStudioConfigs", "skinBranch", StudioBuildInfo.getVersion());
			renderSplashFrame(53, "Loading Images");
			AssetFactory.setGitSource(
					(String) ConfigurationDatabase.getObject("BowlerStudioConfigs", "skinRepo", myAssets),
					ScriptingEngine.getBranch(myAssets));
			renderSplashFrame(54, "Load Assets");
			// Download and Load all of the assets

			renderSplashFrame(60, "Vitamins...");
			// load the vitimins repo so the demo is always snappy
			ScriptingEngine.cloneRepo("https://github.com/CommonWealthRobotics/BowlerStudioVitamins.git", null);

			renderSplashFrame(80, "Example Robots");
			ScriptingEngine.pull("https://github.com/CommonWealthRobotics/BowlerStudioExampleRobots.git", // git
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
			renderSplashFrame(81, "CSG database");
			CSGDatabase.setDbFile(new File(ScriptingEngine.getWorkspace().getAbsoluteFile() + "/csgDatabase.json"));

			// System.err.println("Loading assets ");

			// System.err.println("Loading Main.fxml");
			renderSplashFrame(81, "Find arduino");
			String arduino = "arduino";
			if (NativeResource.isLinux()) {

				//Slic3r.setExecutableLocation("/usr/bin/slic3r");

			} else if (NativeResource.isWindows()) {
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

					}

				}
				System.err.println("Arduino exec found at: " + arduino);
				ArduinoLoader.setARDUINOExec(arduino);
			} catch (Exception e) {
				reporter.uncaughtException(Thread.currentThread(), e);

			}
			renderSplashFrame(82, "Set up UI");
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
				// This is a workaround for #8 and is only relavent on osx
				// it causes the SwingNodes not to load if not called way ahead
				// of time
				javafx.scene.text.Font.getFamilies();
			} catch (Exception e) {
				reporter.uncaughtException(Thread.currentThread(), e);

			}
			renderSplashFrame(90, "Loading STL Loader");
			// Add the engine handeler for STLs
			ScriptingEngine.addScriptingLanguage(new StlLoader());
			// add a new link provider to the link factory
			FirmataLink.addLinkFactory();
			// Log.enableInfoPrint();
			renderSplashFrame(91, "DL'ing Devices...");
			// ThreadUtil.wait(100);

			try {
				ScriptingEngine.pull("https://github.com/CommonWealthRobotics/HotfixBowlerStudio.git");
				ScriptingEngine.pull("https://github.com/CommonWealthRobotics/DeviceProviders.git");
				ScriptingEngine.gitScriptRun("https://github.com/CommonWealthRobotics/HotfixBowlerStudio.git",
						"hotfix.groovy", null);
				ScriptingEngine.gitScriptRun("https://github.com/CommonWealthRobotics/DeviceProviders.git",
						"loadAll.groovy", null);
			} catch (Exception e) {
				reporter.uncaughtException(Thread.currentThread(), e);

			}
			renderSplashFrame(92, "Launching UI");
			launch();

		}

	}

//	private static void removeAssets(String myAssets)
//			throws InvalidRemoteException, TransportException, GitAPIException, IOException, Exception {
//		System.err.println("Clearing assets");
//		ScriptingEngine.deleteRepo(myAssets);
//		AssetFactory.setGitSource((String) ConfigurationDatabase.getObject("BowlerStudioConfigs", "skinRepo", myAssets),
//				StudioBuildInfo.getVersion());
//	}

	public static void closeSplash() {
		SplashManager.closeSplash();
	}

	public static void renderSplashFrame(int frame, String message) {
		SplashManager.renderSplashFrame(frame, message);
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
	 * @param url - The URL of the tab that needs to be opened
	 */
	public static void openUrlInNewTab(URL url) {
		BowlerStudioModularFrame.getBowlerStudioModularFrame().openUrlInNewTab(url);
	}

	/**
	 * @author Sainath
	 * @version 1.0
	 * @param msg - message that needs to be spoken
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
			reporter.uncaughtException(Thread.currentThread(), e);

		}
		System.err.println("Class loader: " + Thread.currentThread().getContextClassLoader());
		new Thread(() -> {
			Thread.currentThread().setUncaughtExceptionHandler(new IssueReportingExceptionHandler());

			try {

				String stylesheet = Application.STYLESHEET_MODENA;// "MODENA" or
																	// "CASPIAN"
				// System.setProperty("javax.userAgentStylesheetUrl",
				// stylesheet);
				setUserAgentStylesheet(stylesheet);
			} catch (Exception | Error e) {
				reporter.uncaughtException(Thread.currentThread(), e);

			}
			// These must be changed before anything starts
			PrintStream ps = new PrintStream(getOut());
			// System.setErr(ps);
			System.setOut(ps);
			renderSplashFrame(93, "Loading resources");
			try {
				BowlerStudioResourceFactory.load();
			} catch (Exception e1) {
				reporter.uncaughtException(Thread.currentThread(), e1);

			}

			primaryStage2 = primaryStage;
			BowlerStudioModularFrame.setPrimaryStage(primaryStage);
			// Initialize your logic here: all @FXML variables will have been
			// injected
			FXMLLoader mainControllerPanel;

			try {
				mainControllerPanel = AssetFactory.loadLayout("layout/BowlerStudioModularFrame.fxml");

				renderSplashFrame(96, "Setting controller");
				mainControllerPanel.setController(new BowlerStudioModularFrame());
				// renderSplashFrame(96, "Class loader");
				// mainControllerPanel.setClassLoader(BowlerStudioModularFrame.class.getClassLoader());
				try {
					renderSplashFrame(96, "Controller load");

					mainControllerPanel.load();
				} catch (Exception e) {
					reporter.uncaughtException(Thread.currentThread(), e);

				}
				renderSplashFrame(96, "UI Launch...");

				Scene scene = new Scene(mainControllerPanel.getRoot(), 1174, 768, true);

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
						reporter.uncaughtException(Thread.currentThread(), e);

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
				FxTimer.runLater(java.time.Duration.ofMillis((int) 2000), () -> {
					String javaVersion = System.getProperty("java.version");
					String javafxVersion = System.getProperty("javafx.version");
					System.out.println("Java Version : " + javaVersion);
					System.out.println("JavaFX Version : " + javafxVersion);
					System.out.println("BowlerStudio First Version: " + firstVer);
					System.out.println("Java-Bowler Version: " + SDKBuildInfo.getVersion());
					System.out.println("Bowler-Scripting-Kernel Version: " + BowlerKernelBuildInfo.getVersion());
					System.out.println("JavaCad Version: " + JavaCadBuildInfo.getVersion());
					System.out.println("Welcome to BowlerStudio!");

				});
				closeSplash();
				if (!ScriptingEngine.isLoginSuccess() || PasswordManager.isAnonMode())
					BowlerStudioModularFrame.getBowlerStudioModularFrame().menueController.onLogin(null);

			} catch (Throwable e) {
				reporter.uncaughtException(Thread.currentThread(), e);

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
				FileChangeWatcher.clearAll();
				Thread.currentThread().setUncaughtExceptionHandler(new IssueReportingExceptionHandler());

				renderSplashFrame(100, "Saving state..");
				ConnectionManager.disconnectAll();
				if(PasswordManager.hasNetwork()) {
					if (ScriptingEngine.isLoginSuccess() && !PasswordManager.isAnonMode())
						ConfigurationDatabase.save();
				}
				if (isDeleteFlag())
					ScriptingEngine.deleteCache();
				System.exit(0);
			}
		}.start();

	}

	public static void printStackTrace(Throwable e) {
		printStackTrace(e, null);
	}

	public static void printStackTrace(Throwable e, File sourceFile) {
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

	public static boolean isDeleteFlag() {
		return deleteFlag;
	}

	public static void setDeleteFlag(boolean deleteFlag) {
		BowlerStudio.deleteFlag = deleteFlag;
	}

	public static void exit() {
		closeBowlerStudio();
	}


}
