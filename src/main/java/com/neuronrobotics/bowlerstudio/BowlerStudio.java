package com.neuronrobotics.bowlerstudio;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.UIManager;

import org.opencv.core.Core;

import com.neuronrobotics.bowlerkernel.BowlerKernelBuildInfo;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingFileWidget;
import com.neuronrobotics.bowlerstudio.utils.BowlerStudioResourceFactory;
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
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Menu;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class BowlerStudio extends Application {
    
    private static TextArea log;
    private static MainController controller;
	private static Stage primaryStage;
	private static Scene scene;
	private static FXMLLoader fxmlLoader;
	static{
		PrintStream ps = new PrintStream(MainController.getOut());
		//System.setErr(ps);
		//System.setOut(ps);
	}
    /**
     * @param args the command line arguments
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
    	
    	if(args.length==0){
    		
    		
    		//System.out.println("Loading assets ");
    	
    		BowlerStudioResourceFactory.load();
    		//System.out.println("Done loading assets ");
    		String key="Bowler Initial Version";
    		//System.out.println("Loading Main.fxml");
    		fxmlLoader = new FXMLLoader(
                    BowlerStudio.class.getClassLoader().getResource("Main.fxml"));
    		Platform.runLater(()->{
    			//System.out.println("Loading the main fxml ");
                try {
                	fxmlLoader.setController(new MainController());
                    fxmlLoader.load();
            		//System.out.println("Done loading main ");

                } catch (IOException ex) {
                	ex.printStackTrace();
                    Logger.getLogger(BowlerStudio.class.getName()).
                            log(Level.SEVERE, null, ex);
                    System.exit(1);
                }
    		});


    		MainController.updateLog();
			try{
				OpenCVJNILoader.load();              // Loads the JNI (java native interface)
			}catch(Exception e){
				//e.printStackTrace();
				//opencvOk=false;
    						Platform.runLater(()->{
    							Alert alert = new Alert(AlertType.INFORMATION);
    							alert.setTitle("OpenCV missing");
    							alert.setHeaderText("Opencv library is missing");
    							alert.setContentText(e.getMessage());
    							alert .initModality(Modality.APPLICATION_MODAL);
    							alert.show();
    							e.printStackTrace();
    						});

			}
			if(NativeResource.isLinux()){
				String [] possibleLocals = new String[]{
						"/usr/local/share/OpenCV/java/lib"+Core.NATIVE_LIBRARY_NAME+".so",
						"/usr/lib/jni/lib"+Core.NATIVE_LIBRARY_NAME+".so"
				};
				Slic3r.setExecutableLocation("/usr/bin/slic3r");
				
			}else if(NativeResource.isWindows()){
				String basedir =System.getenv("OPENCV_DIR");
				if(basedir == null)
					throw new RuntimeException("OPENCV_DIR was not found, environment variable OPENCV_DIR needs to be set");
				System.err.println("OPENCV_DIR found at "+ basedir);
				basedir+="\\..\\..\\..\\Slic3r_X64\\Slic3r\\slic3r.exe";
				Slic3r.setExecutableLocation(basedir);
				
			}
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
				// This is a workaround for #8 and is only relavent on osx
				// it causes the SwingNodes not to load if not called way ahead of time
				javafx.scene.text.Font.getFamilies();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		CSGDatabase.setDbFile(new File(ScriptingEngine.getWorkspace().getAbsoluteFile()+"/csgDatabase.json"));
    		launch(args);
    	}else{
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

        setScene(new Scene(main, 1250, 768,true));

//        getScene().getStylesheets().add(BowlerStudio.class.getResource("java-keywords.css").
//                toExternalForm());
        
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
        primaryStage.setTitle("Bowler Studio: v "+StudioBuildInfo.getVersion());
        primaryStage.getIcons().add(new Image(AbstractConnectionPanel.class.getResourceAsStream( "images/hat.png" ))); 


	     System.out.println("Java-Bowler Version: "+SDKBuildInfo.getVersion()); 
	     System.out.println("Bowler-Scripting-Kernel Version: "+BowlerKernelBuildInfo.getVersion());
	     System.out.println("JavaCad Version: "+JavaCadBuildInfo.getVersion());
	     System.out.println("Welcome to BowlerStudio!");
	     Log.enableWarningPrint();
    }

    public static Parent loadFromFXML() {
        
        if (controller!=null) {
            throw new IllegalStateException("UI already loaded");
        }
        
        


        controller = fxmlLoader.getController();
        
        log = controller.getLogView();
        

        return fxmlLoader.getRoot();
    }
    
    public static TextArea getLogView() {
        
        if (log==null) {
            throw new IllegalStateException("Load the UI first.");
        }
        
        return log;
    }
    
//	public static Menu getCreatureLabMenue() {
//		return controller.getCreatureLabMenue();
//	}

	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	public static void setPrimaryStage(Stage primaryStage) {
		BowlerStudio.primaryStage=primaryStage;
	
		Platform.runLater(()->{
			try {
				BowlerStudio.primaryStage.getIcons().add(AssetFactory.loadAsset("BowlerStudio.png"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}
	
	public static void openUrlInNewTab(URL url){
		controller.openUrlInNewTab(url);
	}
	
	public static int speak(String msg){
		
		return BowlerKernel.speak(msg);
	}
	
	public static ScriptingFileWidget createFileTab(File file){
		return controller.createFileTab(file);
	}

	public static Scene getScene() {
		return scene;
	}

	public static void setScene(Scene s) {
		scene = s;
	}
}
