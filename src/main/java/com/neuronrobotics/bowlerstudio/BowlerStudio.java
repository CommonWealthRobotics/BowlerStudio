package com.neuronrobotics.bowlerstudio;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.UIManager;

import org.opencv.core.Core;

import com.neuronrobotics.bowlerkernel.BowlerKernelBuildInfo;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.assets.BowlerStudioResourceFactory;
import com.neuronrobotics.bowlerstudio.scripting.GithubLoginFX;
import com.neuronrobotics.bowlerstudio.scripting.IGitHubLoginManager;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingFileWidget;
import com.neuronrobotics.imageprovider.NativeResource;
import com.neuronrobotics.imageprovider.OpenCVJNILoader;
import com.neuronrobotics.javacad.JavaCadBuildInfo;
import com.neuronrobotics.replicator.driver.Slic3r;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.config.SDKBuildInfo;
import com.neuronrobotics.sdk.ui.AbstractConnectionPanel;
import com.neuronrobotics.sdk.util.ThreadUtil;
import com.sun.javafx.css.CascadingStyle;

import eu.mihosoft.vrl.v3d.parametrics.CSGDatabase;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Menu;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class BowlerStudio extends Application {

	private static TextArea log;
	private static MainController controller;
	private static Stage primaryStage;
	private static Scene scene;
	private static FXMLLoader fxmlLoader;
	private static boolean hasnetwork;
	private static Console out;
	private static TextArea logViewRefStatic=null;
	private static class Console extends OutputStream {
		
	    public void appendText(String valueOf) {
			Platform.runLater(() -> {
				if (getLogViewRefStatic() != null)
					getLogViewRefStatic().appendText(valueOf);
			});
	    }
	
	    public void write(int b) throws IOException {
	        appendText(String.valueOf((char)b));
	    }
	}
	public static OutputStream getOut() {
		if(out==null)
			out = new Console();
		return out;
	}
	static{
		//These must be changed before anything starts
		PrintStream ps =new PrintStream(getOut());
		//System.setErr(ps);
		System.setOut(ps);
		try {                                                                                                                                                                                                                                 
	        final URL url = new URL("http://github.com");                                                                                                                                                                                 
	        final URLConnection conn = url.openConnection();                                                                                                                                                                                  
	        conn.connect();    
	        conn.getInputStream();                                                                                                                                                                                                               
	        setHasnetwork(true);                                                                                                                                                                                                                      
	    } catch (Exception e) {                                                                                                                                                                                                             
	        // we assuming we have no access to the server and run off of the chached gists.    
	    	setHasnetwork(false);                                                                                                                                                                                                                              
	    }  
	}

	/**
	 * @param args
	 *            the command line arguments
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {

		if (args.length == 0) {

			// ScriptingEngine.logout();
			ScriptingEngine.setLoginManager(new GitHubLoginManager());

			if (ScriptingEngine.getCreds().exists()){
				ScriptingEngine.runLogin();
				if(BowlerStudio.hasNetwork())
					ScriptingEngine.setAutoupdate(true);
			}else
				ScriptingEngine.setupAnyonmous();
			// Download and Load all of the assets
			AssetFactory.loadAsset("BowlerStudio.png");
			BowlerStudioResourceFactory.load();
			// load tutorials repo
			ScriptingEngine.fileFromGit(
					"https://github.com/NeuronRobotics/NeuronRobotics.github.io.git", 
					"index.html");
			ScriptingEngine
			.fileFromGit(
					"https://github.com/madhephaestus/BowlerStudioExampleRobots.git",// git repo, change this if you fork this demo
				"exampleRobots.json"// File from within the Git repo
			);
			CSGDatabase.setDbFile(new File(ScriptingEngine.getWorkspace().getAbsoluteFile() + "/csgDatabase.json"));
//			if (!ScriptingEngine.getCreds().exists()) {
//				ScriptingEngine.logout();
//			}

			// System.out.println("Loading assets ");

			// System.out.println("Done loading assets ");
			String key = "Bowler Initial Version";
			// System.out.println("Loading Main.fxml");

			try {
				OpenCVJNILoader.load(); // Loads the JNI (java native interface)
			} catch (Exception |Error e ) {
				// e.printStackTrace();
				// opencvOk=false;
				Platform.runLater(() -> {
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("OpenCV missing");
					alert.setHeaderText("Opencv library is missing");
					alert.setContentText(e.getMessage());
					alert.initModality(Modality.APPLICATION_MODAL);
					alert.show();
					e.printStackTrace();
				});

			}
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
			
			launch(args);
		} else {
			BowlerKernel.main(args);
		}
	}

	public static void setSelectedTab(Tab tab) {
		controller.getApplication().setSelectedTab(tab);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		setPrimaryStage(primaryStage);
		Parent main = loadFromFXML();

		setScene(new Scene(main, 1250, 768, true));

		// getScene().getStylesheets().add(BowlerStudio.class.getResource("java-keywords.css").
		// toExternalForm());

		PerspectiveCamera camera = new PerspectiveCamera();

		getScene().setCamera(camera);

		primaryStage.setTitle("Bowler Studio");
		primaryStage.setScene(getScene());
		primaryStage.show();
		primaryStage.setOnCloseRequest(arg0 -> {

			controller.disconnect();
			ThreadUtil.wait(100);
			System.exit(0);
		});
		primaryStage.setTitle("Bowler Studio: v " + StudioBuildInfo.getVersion());
		primaryStage.getIcons().add(new Image(AbstractConnectionPanel.class.getResourceAsStream("images/hat.png")));


		
		

		System.out.println("Java-Bowler Version: " + SDKBuildInfo.getVersion());
		System.out.println("Bowler-Scripting-Kernel Version: " + BowlerKernelBuildInfo.getVersion());
		System.out.println("JavaCad Version: " + JavaCadBuildInfo.getVersion());
		System.out.println("Welcome to BowlerStudio!");
		Log.enableSystemPrint(false);
//		new Thread(){
//			public void run(){
//				ThreadUtil.wait(1000);
//				if (!ScriptingEngine.getCreds().exists()) {
//					try {
//						ScriptingEngine.login();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}
//		}.start();

	}

	public static Parent loadFromFXML() {
		// new Exception().printStackTrace();
		fxmlLoader = BowlerStudioResourceFactory.getMainControllerPanel();
		if (controller != null) {
			throw new IllegalStateException("UI already loaded");
		}
		fxmlLoader.setController(new MainController());
		try {
			fxmlLoader.load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		controller = fxmlLoader.getController();

		log = controller.getLogView();

		return fxmlLoader.getRoot();
	}

	public static TextArea getLogView() {

		if (log == null) {
			throw new IllegalStateException("Load the UI first.");
		}

		return log;
	}

	// public static Menu getCreatureLabMenue() {
	// return controller.getCreatureLabMenue();
	// }

	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	public static void setPrimaryStage(Stage primaryStage) {
		BowlerStudio.primaryStage = primaryStage;

		Platform.runLater(() -> {
			try {
				BowlerStudio.primaryStage.getIcons().add(AssetFactory.loadAsset("BowlerStudio.png"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	public static void openUrlInNewTab(URL url) {
		controller.openUrlInNewTab(url);
	}

	public static int speak(String msg) {

		return BowlerKernel.speak(msg);
	}

	public static ScriptingFileWidget createFileTab(File file) {
		return controller.createFileTab(file);
	}

	public static Scene getScene() {
		return scene;
	}

	public static void setScene(Scene s) {
		scene = s;
	}

	public static void clearConsole() {
		Platform.runLater(() -> {
			if(getLogViewRefStatic()!=null)
				getLogViewRefStatic().setText("");
		});
	}
	public static void setOverlayLeft(TreeView<String> tree){
		controller.setOverlayLeft(tree);
	}
	public static  void clearOverlayLeft(){
		controller.clearOverlayLeft();
	}
	
	public  static void setOverlayTop(Group content){
		controller.setOverlayTop(content);;
	}
	public static  void clearOverlayTop(){
		controller.clearOverlayTop();
	}
	public static  void setOverlayTopRight(Group content){
		controller.setOverlayTopRight(content);
	}
	public static  void clearOverlayTopRight(){
		controller.clearOverlayTopRight();
	}
	public  static void setOverlayBottomRight(Group content){
		controller.setOverlayBottomRight(content);
	}
	public static  void clearOverlayBottomRight(){
		controller.clearOverlayBottomRight();
	}
	public  static  void setCadSplit(double value){
		controller.setCadSplit(value);
	}

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
	
}
