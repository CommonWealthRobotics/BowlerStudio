package com.neuronrobotics.bowlerstudio;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.UIManager;

import com.neuronrobotics.bowlerkernel.BowlerKernelBuildInfo;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.assets.BowlerStudioResourceFactory;
import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingFileWidget;
import com.neuronrobotics.imageprovider.NativeResource;
import com.neuronrobotics.imageprovider.OpenCVJNILoader;
import com.neuronrobotics.javacad.JavaCadBuildInfo;
import com.neuronrobotics.replicator.driver.Slic3r;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.DeviceManager;
import com.neuronrobotics.sdk.common.IDeviceAddedListener;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.config.SDKBuildInfo;
import com.neuronrobotics.sdk.ui.AbstractConnectionPanel;
import com.neuronrobotics.sdk.util.ThreadUtil;
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
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class BowlerStudio extends Application {

	private static TextArea log;
	private static Stage primaryStage;
	private static Scene scene;
	private static FXMLLoader fxmlLoader;
	private static boolean hasnetwork;
	private static Console out;
	private static TextArea logViewRefStatic=null;
	private static CreatureLab3dController creatureLab3dController;
	private BowlerStudioModularFrame modularFrame;
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
			
			String firstVer = "";
			if (ScriptingEngine.getCreds().exists())
				firstVer = (String) ConfigurationDatabase.getObject("BowlerStudioConfigs", "firstVersion",
						StudioBuildInfo.getVersion());
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
					e.printStackTrace(System.out);
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
			System.out.println("BowlerStudio First Version: " + firstVer);
			launch(args);
			
		} else {
			BowlerKernel.main(args);
		}
	}

	

	public static void openUrlInNewTab(URL url) {
		BowlerStudioModularFrame.getBowlerStudioModularFrame().openUrlInNewTab(url);
	}

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
			if(getLogViewRefStatic()!=null)
				getLogViewRefStatic().setText("");
		});
	}
	public static void setOverlayLeft(TreeView<String> tree){
		BowlerStudio.creatureLab3dController.setOverlayLeft(tree);
	}
	public static  void clearOverlayLeft(){
		BowlerStudio.creatureLab3dController.clearOverlayLeft();
	}
	
	public  static void setOverlayTop(Group content){
		BowlerStudio.creatureLab3dController.setOverlayTop(content);;
	}
	public static  void clearOverlayTop(){
		BowlerStudio.creatureLab3dController.clearOverlayTop();
	}
	public static  void setOverlayTopRight(Group content){
		BowlerStudio.creatureLab3dController.setOverlayTopRight(content);
	}
	public static  void clearOverlayTopRight(){
		BowlerStudio.creatureLab3dController.clearOverlayTopRight();
	}
	public  static void setOverlayBottomRight(Group content){
		BowlerStudio.creatureLab3dController.setOverlayBottomRight(content);
	}
	public static  void clearOverlayBottomRight(){
		BowlerStudio.creatureLab3dController.clearOverlayBottomRight();
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



	public static void setCreatureLab3d(CreatureLab3dController creatureLab3dController) {
		BowlerStudio.creatureLab3dController = creatureLab3dController;
	}



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

			Scene scene = new Scene(mainControllerPanel.getRoot(), 1024, 768, true);
			File f = AssetFactory.loadFile("layout/default.css");
			scene.getStylesheets().clear();
			scene.getStylesheets().add("file:///" + f.getAbsolutePath().replace("\\", "/"));

			primaryStage.setTitle("Bowler Studio");
			primaryStage.setScene(scene);
			primaryStage.show();
			primaryStage.setOnCloseRequest(arg0 -> {
				// ThreadUtil.wait(100);
				new Thread(){
					public void run(){
						ConnectionManager.disconnectAll();
						if (ScriptingEngine.getCreds().exists()) 
							ConfigurationDatabase.save();
						System.exit(0);
					}
				}.start();
				
			});

			primaryStage.setResizable(true);

		
			DeviceManager.addDeviceAddedListener(new IDeviceAddedListener() {

				@Override
				public void onNewDeviceAdded(BowlerAbstractDevice arg0) {
					System.err.println("Device connected: "+arg0);
					BowlerStudioModularFrame.getBowlerStudioModularFrame().showConectionManager();
				}

				@Override
				public void onDeviceRemoved(BowlerAbstractDevice arg0) {}
			});
			
			System.out.println("Java-Bowler Version: " + SDKBuildInfo.getVersion());
			System.out.println("Bowler-Scripting-Kernel Version: " + BowlerKernelBuildInfo.getVersion());
			System.out.println("JavaCad Version: " + JavaCadBuildInfo.getVersion());
			System.out.println("Welcome to BowlerStudio!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}




	
}
