package com.neuronrobotics.bowlerstudio;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;

//import com.neuronrobotics.kinematicschef.InverseKinematicsEngine;
import com.neuronrobotics.bowlerkernel.BowlerKernelBuildInfo;
import com.neuronrobotics.bowlerkernel.Bezier3d.IInteractiveUIElementProvider;
import com.neuronrobotics.bowlerkernel.Bezier3d.Manipulation;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.assets.BowlerStudioResourceFactory;
import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;
import com.neuronrobotics.bowlerstudio.assets.FontSizeManager;
import com.neuronrobotics.bowlerstudio.assets.StudioBuildInfo;
import com.neuronrobotics.bowlerstudio.creature.MobileBaseCadManager;
import com.neuronrobotics.bowlerstudio.creature.MobileBaseLoader;
import com.neuronrobotics.bowlerstudio.scripting.DownloadManager;
import com.neuronrobotics.bowlerstudio.scripting.GitHubWebFlow;
import com.neuronrobotics.bowlerstudio.scripting.IApprovalForDownload;
import com.neuronrobotics.bowlerstudio.scripting.IDownloadManagerEvents;
import com.neuronrobotics.bowlerstudio.scripting.IURLOpen;
import com.neuronrobotics.bowlerstudio.scripting.PasswordManager;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingFileWidget;
import com.neuronrobotics.bowlerstudio.scripting.StlLoader;
import com.neuronrobotics.bowlerstudio.util.FileChangeWatcher;
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
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
import com.neuronrobotics.video.OSUtil;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Debug3dProvider;
import eu.mihosoft.vrl.v3d.IDebug3dProvider;
import eu.mihosoft.vrl.v3d.parametrics.CSGDatabase;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.transform.Affine;
import javafx.stage.Stage;

import org.dockfx.DockNode;
import org.dockfx.DockPane;
import org.dockfx.IStageModifyer;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;

import static com.neuronrobotics.bowlerstudio.scripting.DownloadManager.delim;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.awt.SplashScreen;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

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
	// private static String lastVersion;

	@SuppressWarnings({ "unchecked", "restriction" })
	public static void main(String[] args) throws Exception {
		if (args.length != 0) {
			//System.err.println("Arguments detected, starting Kernel mode.");
			//SplashManager.closeSplash();
			BowlerKernel.runArgumentsAfterStartup(args, System.currentTimeMillis());
			return;
		}
		try {
			makeSymLinkOfCurrentVersion();
		}catch(Throwable t) {
			//t.printStackTrace();
			System.err.println("Symlink not creaded");
		}
		System.setOut(System.err);// send all prints to err until replaced with the terminal
		net.java.games.input.ControllerEnvironment.getDefaultEnvironment();

		Thread.currentThread().setUncaughtExceptionHandler(new IssueReportingExceptionHandler());
		if (!StudioBuildInfo.isOS64bit()) {

			BowlerStudio.runLater(() -> {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("32 Bit Java Detected");
				alert.setHeaderText("Insuffient Ram Capibilities in 32 bit mode");
				alert.setContentText("This applications uses more that 4gb of ram\nA 32 bit JVM mode detected: "
						+ System.getProperty("os.arch"));
				Node root = alert.getDialogPane();
				Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
				stage.setOnCloseRequest(ev -> alert.hide());
				FontSizeManager.addListener(fontNum -> {
					int tmp = fontNum - 10;
					if (tmp < 12)
						tmp = 12;
					root.setStyle("-fx-font-size: " + tmp + "pt");
					alert.getDialogPane().applyCss();
					alert.getDialogPane().layout();
					stage.sizeToScene();
				});
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
			e.printStackTrace();

		}
		CSG.setDefaultOptType(CSG.OptType.CSG_BOUND);
		Debug3dProvider.setProvider(new IDebug3dProvider() {

			@Override
			public void clearScreen() {
				BowlerStudioController.clearCSG();
				BowlerStudioController.clearUserNodes();
			}

			@Override
			public void addObject(Object o) {
				BowlerStudioController.addObject(o, null);
			}
		});
		StudioBuildInfo.setBaseBuildInfoClass(BowlerStudio.class);
		Manipulation.setUi(new IInteractiveUIElementProvider() {
			public void runLater(Runnable r) {
				BowlerStudio.runLater(r);
			}

			public TransformNR getCamerFrame() {
				return BowlerStudio.getCamerFrame();
			}

			public double getCamerDepth() {
				return BowlerStudio.getCamerDepth();
			}
		});

		renderSplashFrame(5, "Loging In...");
		// ScriptingEngine.logout();
		// switching to Web Flow auth
		List<String> listOfScopes = Arrays.asList("repo", "gist", "user", "admin:org", "admin:org_hook", "workflow");
		if (OSUtil.isOSX())
			GitHubWebFlow.setOpen(new IURLOpen() {
				public void open(URI toOpe) {
					try {
						BowlerStudio.openExternalWebpage(toOpe.toURL());
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		PasswordManager.setListOfScopes(listOfScopes);
		GitHubWebFlow.setMyAPI(() -> {
			String line = System.getProperty("API-ID");
			if (line != null)
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

//					if (BowlerStudio.hasNetwork()) {
//						ScriptingEngine.setAutoupdate(true);
//
//					}
				renderSplashFrame(15, "Load Configs");
				try {
					firstVer = (String) ConfigurationDatabase.getObject("BowlerStudioConfigs", "firstVersion",
							StudioBuildInfo.getVersion());
				} catch (Throwable t) {
					System.err.println("Resetting the configs repo...");
					// clear the configs repo
					firstVer = (String) ConfigurationDatabase.getObject("BowlerStudioConfigs", "firstVersion",
							StudioBuildInfo.getVersion());
				}
				ConfigurationDatabase.setObject("BowlerStudioConfigs", "currentVersion", StudioBuildInfo.getVersion());
				renderSplashFrame(16, "Done Load Configs");
//					myAssets = (String) ConfigurationDatabase.getObject("BowlerStudioConfigs", "assetRepo",
//							myAssets);
				renderSplashFrame(20, "DL'ing Image Assets");
				System.err.println("Asset Repo " + myAssets);

				System.err.println("Asset intended ver " + StudioBuildInfo.getVersion());
				ScriptingEngine.cloneRepo(myAssets, null);
				try {
					ScriptingEngine.pull(myAssets, "main");
					System.err.println("Studio version is the same");
				} catch (Exception e) {
					e.printStackTrace();
					ScriptingEngine.deleteRepo(myAssets);
					ScriptingEngine.cloneRepo(myAssets, null);
				}

				if (ScriptingEngine.checkOwner(myAssets)) {
					if (!ScriptingEngine.tagExists(myAssets, StudioBuildInfo.getVersion())) {
						System.out.println("Tagging Assets at " + StudioBuildInfo.getVersion());
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
			ScriptingEngine.cloneRepo(myAssets, null);
			layoutFile = AssetFactory.loadFile("layout/default.css");
		}
		// SplashManager.setIcon(AssetFactory.loadAsset("BowlerStudioTrayIcon.png"));
		renderSplashFrame(50, "DL'ing Tutorials...");
		// load tutorials repo

		Tutorial.getHomeUrl(); // Dowload and launch the Tutorial server
		// force the current version in to the version number
		// Download and Load all of the assets

		renderSplashFrame(60, "Vitamins...");
		// load the vitimins repo so the demo is always snappy
		ScriptingEngine.cloneRepo("https://github.com/CommonWealthRobotics/BowlerStudioVitamins.git", null);

		renderSplashFrame(80, "Example Robots");
		ScriptingEngine.cloneRepo("https://github.com/CommonWealthRobotics/BowlerStudioExampleRobots.git", null);
		ScriptingEngine.pull("https://github.com/CommonWealthRobotics/BowlerStudioExampleRobots.git");
		renderSplashFrame(81, "CSG database");
		CSGDatabase.setDbFile(new File(ScriptingEngine.getWorkspace().getAbsoluteFile() + "/csgDatabase.json"));

		// System.err.println("Loading assets ");

		// System.err.println("Loading Main.fxml");
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
		// add a new link provider to the link factory
		FirmataLink.addLinkFactory();
		// Log.enableInfoPrint();
		renderSplashFrame(91, "DL'ing Devices...");
		// ThreadUtil.wait(100);

		try {
			ensureUpdated("https://github.com/CommonWealthRobotics/DHParametersCadDisplay.git",
					"https://github.com/CommonWealthRobotics/HotfixBowlerStudio.git",
					"https://github.com/CommonWealthRobotics/DeviceProviders.git",
					"https://github.com/OperationSmallKat/Katapult.git",
					"https://github.com/CommonWealthRobotics/ExternalEditorsBowlerStudio.git",
					"https://github.com/CommonWealthRobotics/freecad-bowler-cli.git",
					"https://github.com/CommonWealthRobotics/blender-bowler-cli.git");
			ScriptingEngine.gitScriptRun("https://github.com/CommonWealthRobotics/HotfixBowlerStudio.git",
					"hotfix.groovy", null);
			ScriptingEngine.gitScriptRun("https://github.com/CommonWealthRobotics/DeviceProviders.git",
					"loadAll.groovy", null);
			renderSplashFrame(92, "Vitamin Scripts...");
			HashSet<String> urls = new HashSet<>();
			for (String type : Vitamins.listVitaminTypes()) {
				String url = Vitamins.getScriptGitURL(type);
				urls.add(url);
			}
			new Thread(() -> {
				boolean wasState = ScriptingEngine.isPrintProgress();
				ScriptingEngine.setPrintProgress(false);
				for (Iterator<String> iterator = urls.iterator(); iterator.hasNext();) {
					String url = iterator.next();

					ensureUpdated(url);

				}
				ScriptingEngine.setPrintProgress(wasState);
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
			reporter.uncaughtException(Thread.currentThread(), e);

		}
		DownloadManager.setDownloadEvents(new IDownloadManagerEvents() {
			
			@Override
			public void startDownload() {
				SplashManager.renderSplashFrame(0, "Downloading...");
			}
			
			@Override
			public void finishDownload() {
				SplashManager.closeSplash();
			}
		});
		DownloadManager.setApproval(new IApprovalForDownload() {
			private ButtonType buttonType = null;

			@Override
			public boolean get(String name, String url) {
				buttonType = null;

				BowlerKernel.runLater(() -> {
					Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
					alert.setTitle("Message");
					alert.setHeaderText("Would you like add the " + name + " plugin?" );
					Node root = alert.getDialogPane();
					Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
					stage.setOnCloseRequest(ev -> alert.hide());
					FontSizeManager.addListener(fontNum -> {
						int tmp = fontNum - 10;
						if (tmp < 12)
							tmp = 12;
						root.setStyle("-fx-font-size: " + tmp + "pt");
						alert.getDialogPane().applyCss();
						alert.getDialogPane().layout();
						stage.sizeToScene();
					});
					Optional<ButtonType> result = alert.showAndWait();
					buttonType = result.get();
					alert.close();
				});

				while (buttonType == null) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				return buttonType.equals(ButtonType.OK);
			}
		});
		renderSplashFrame(92, "Launching UI");
		launch();
		

	}


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
		public void appendText(String v) {
			if(v.length()>LengthOfOutputLog) {
				v=v.substring(v.length()-LengthOfOutputLog, v.length());
			}
			String valueOf=v;
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

					BowlerStudio.runLater(() -> {
						try {
							getLogViewRefStatic().deleteText(0, text.length() - LengthOfOutputLog);

							getLogViewRefStatic().appendText(valueOf);
						} catch (Throwable t) {
						}

					});
				} else {
					BowlerStudio.runLater(() -> {
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

	public static void runLater(java.time.Duration delay, Runnable action) {
		Throwable t = new Exception("Delayed UI Thread Exception here!");
		// t.printStackTrace();
		new Thread() {
			public void run() {
				setName("UI Delay Thread ");
				try {
					Thread.sleep(delay.getSeconds() * 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				runLater(action, t);
			}
		}.start();
	}

	public static void runLater(Runnable r) {
		if (Platform.isFxApplicationThread())
			try {
				r.run();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		else
			runLater(r, new Exception("UI Thread Exception here!"));
	}

	public static void runLater(Runnable r, Throwable ex) {
		if (Platform.isFxApplicationThread())
			try {
				r.run();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		else
			Platform.runLater(() -> {
				try {
					r.run();
				} catch (Throwable t) {
					t.printStackTrace();
					ex.printStackTrace();
				}

			});
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
	/**
	 * Select a provided affine that is in a given global pose
	 * @param startingLocation the starting pose
	 * @param rootListener what affine to attach to 
	 */
	public static void select(TransformNR startingLocation,Affine rootListener) {
		if (CreatureLab3dController.getEngine().isAutoHightlight()) {
			CreatureLab3dController.getEngine().setSelected(startingLocation,rootListener);
		}
	}
	/**
	 * Select a provided affine that is in a given global pose
	 * @param rootListener what affine to attach to 
	 */
	public static void select(Affine rootListener) {
		if (CreatureLab3dController.getEngine().isAutoHightlight()) {
			CreatureLab3dController.getEngine().setSelected(new TransformNR(),rootListener);
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



	/**
	 * @param args the command line arguments
	 * @throws Exception
	 */
	public static String getBowlerStudioBinaryVersion() throws FileNotFoundException {
		String latestVersionString;
		File currentVerFile = new File(System.getProperty("user.home") + delim() + "bin" + delim()
				+ "BowlerStudioInstall" + delim() + "currentversion.txt");
		String s = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(currentVerFile)));
		String line;
		try {
			while (null != (line = br.readLine())) {
				s += line;
			}
		} catch (IOException e) {
		}
		latestVersionString = s.trim();
		return latestVersionString;
	}
	private static void makeSymLinkOfCurrentVersion() throws Exception {
		String version = getBowlerStudioBinaryVersion();
		File installDir = new File(System.getProperty("user.home") + delim() + "bin" + delim()+ "BowlerStudioInstall" + delim());
		File link = new File(installDir.getAbsolutePath()+delim()+"latest");
		File latest = new File(installDir.getAbsolutePath()+delim()+version);
		if(link.exists())
			link.delete();
		try {

			Files.createSymbolicLink( link.toPath(),latest.toPath());
		}catch(Throwable t) {
			//t.printStackTrace();
			//link = new File("\""+link.getAbsolutePath()+"\"");
			Path ret = Files.createSymbolicLink( link.toPath(), Paths.get(".", version));
			System.out.println("Path created "+ret);
		}
	}
	
	private static void ensureUpdated(String ... urls) {
		for(String s:urls) {

			ScriptingEngine.cloneRepo(s, null);
			try {
				ScriptingEngine.pull(s);
			} catch (RefAlreadyExistsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RefNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidRefNameException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidRemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransportException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GitAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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

		runLater(() -> {
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

				Parent root = mainControllerPanel.getRoot();
				FontSizeManager.addListener(fontNum->{
					BowlerStudioController.getBowlerStudio().setFontSize(fontNum);
					double tmp = FontSizeManager.getImageScale()*9;

					root.setStyle("-fx-font-size: "+((int)tmp)+"pt");
				});
				
				double sw = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
						.getDisplayMode().getWidth();
				double sh = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
						.getDisplayMode().getHeight();
				Rectangle2D primaryScreenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
				double scalew = primaryScreenBounds.getWidth();
				double screenZoom = sw/scalew;

				if (FontSizeManager.getDefaultSize() == FontSizeManager.systemDefaultFontSize) {
					double newSize= sw/2256.0*(2*FontSizeManager.systemDefaultFontSize)/screenZoom;
					if(newSize<FontSizeManager.systemDefaultFontSize)
						newSize=FontSizeManager.systemDefaultFontSize;
					FontSizeManager.setFontSize((int)Math.round(newSize));
					System.out.println("Screen "+sw+"x"+sh);
				}
				sw=primaryScreenBounds.getWidth();
				sh=primaryScreenBounds.getHeight();
				double w ;
				double h ;
				w=sw-40;
				h=sh-40;
				
				Scene scene = new Scene(root, w, h, true);
				setBowlerStudioCSS(scene);
				BowlerStudio.runLater(() -> {

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
				BowlerStudio.runLater(() -> {
					setTitle(null);

					try {
						
						Image loadAsset = new Image(PsudoSplash.getResource().toString());
						primaryStage.getIcons().add(loadAsset);
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
				BowlerStudio.runLater(java.time.Duration.ofMillis((int) 2000), () -> {
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

	public static void setTitle(String title) {
		if(title==null)
			title=StudioBuildInfo.getAppName()+" v " + StudioBuildInfo.getVersion();
		if(primaryStage2!=null)
			primaryStage2.setTitle(title);
	}

	public static void setBowlerStudioCSS(Scene scene) {
		String nwfile = layoutFile.toURI().toString().replace("file:/", "file:///");

		scene.getStylesheets().clear();
		scene.getStylesheets().add(nwfile);
		
		System.err.println("Loading CSS from " + nwfile);
	}
	
	public static void setToRunButton(Button b) {
		b.setText("Run");
		b.setGraphic(AssetFactory.loadIcon("Run.png"));
		b.getStyleClass().clear();
		b.getStyleClass().add("button-run");
		b.getStyleClass().add("button");
		b.setMinWidth(80);
	}
	public static void setToStopButton(Button b) {
		b.setText("Stop");
		b.setGraphic(AssetFactory.loadIcon("Stop.png"));
		b.getStyleClass().clear();
		b.getStyleClass().add("button");
		b.getStyleClass().add("button-stop");
		b.setMinWidth(80);
	}
	
	@SuppressWarnings("restriction")
	public static void closeBowlerStudio() {
		BowlerStudio.runLater(() -> {
			primaryStage2.hide();
		});
		new Thread() {

			public void run() {
				FileChangeWatcher.clearAll();
				Thread.currentThread().setUncaughtExceptionHandler(new IssueReportingExceptionHandler());

				renderSplashFrame(100, "Saving state..");
				ConnectionManager.disconnectAll();
				if (PasswordManager.hasNetwork()) {
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

	public static void invokeLater(Runnable object) {
		RuntimeException ex = new RuntimeException("SwingUtilities called from here");
		SwingUtilities.invokeLater(() -> {
			try {
				object.run();
			}catch(Throwable t) {
				t.printStackTrace();
				System.err.println("Swing method that failed called from: ");
				ex.printStackTrace();
			}
		});
	}
	
	public static void moveCamera(TransformNR tf) {
		runLater(()->{
			CreatureLab3dController.getEngine().moveCamera(tf);
		});
	}
	public static void setCamera(TransformNR tf) {
		TransformNR current = getCamerFrame();
		TransformNR tfupde=current.inverse().times(tf);
		runLater(()->{
			CreatureLab3dController.getEngine().moveCamera(tfupde);
		});
	}
	public static TransformNR getCamerFrame() {
		return CreatureLab3dController.getEngine().getFlyingCamera().getCamerFrame();
	}

	public static double getCamerDepth() {
		return CreatureLab3dController.getEngine().getFlyingCamera().getZoomDepth();
	}
	public static void zoomCamera(double increment) {
		runLater(()->{
			CreatureLab3dController.getEngine().zoomIncrement(increment);
		});
	}
	public static TransformNR getTargetFrame() {
		return CreatureLab3dController.getEngine().getTargetNR();
	}
	public static void loadMobilBaseIntoUI(MobileBase base) {
		BowlerStudioController.getBowlerStudio().onScriptFinished(base, base, null);
	}
	public static void showExceptionAlert(Exception ex, String message) {
	    Alert alert = new Alert(Alert.AlertType.ERROR);
	    alert.setTitle("Error");
	    alert.setHeaderText(message);
	    alert.setContentText(ex.getMessage());

	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    ex.printStackTrace(pw);
	    String stackTrace = sw.toString();

	    TextArea textArea = new TextArea(stackTrace);
	    textArea.setEditable(false);
	    textArea.setWrapText(true);

	    textArea.setMaxWidth(Double.MAX_VALUE);
	    textArea.setMaxHeight(Double.MAX_VALUE);
	    GridPane.setVgrow(textArea, Priority.ALWAYS);
	    GridPane.setHgrow(textArea, Priority.ALWAYS);

	    GridPane expContent = new GridPane();
	    expContent.setMaxWidth(Double.MAX_VALUE);
	    expContent.add(textArea, 0, 0);

	    alert.getDialogPane().setExpandableContent(expContent);

	    alert.showAndWait();
	}
	public static boolean checkValidURL(String url) {
		try {
			if(url==null)
				throw new NullPointerException();
			if(url.length()<5)
				throw new NullPointerException();
			if(url.startsWith("http"))
				new URL(url);// check that the URL string contains a valid URL
			else
				if(url.startsWith("git@")) {
					// assume this is a URL 
				}
		}catch(Exception e) {
			// not a url
			//
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
