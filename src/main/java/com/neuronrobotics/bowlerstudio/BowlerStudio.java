package com.neuronrobotics.bowlerstudio;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.neuronrobotics.bowlerkernel.BowlerKernelBuildInfo;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingFileWidget;
import com.neuronrobotics.bowlerstudio.utils.BowlerStudioResourceFactory;
import com.neuronrobotics.javacad.JavaCadBuildInfo;
import com.neuronrobotics.sdk.config.SDKBuildInfo;
import com.neuronrobotics.sdk.ui.AbstractConnectionPanel;
import com.neuronrobotics.sdk.util.ThreadUtil;
import edu.cmu.sphinx.api.Configuration;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class BowlerStudio extends Application {
    
    private static TextArea log;
    private static MainController controller;
	private static Stage primaryStage;
	private static Scene scene;

    /**
     * @param args the command line arguments
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
    	
    	if(args.length==0){
    		BowlerStudioResourceFactory.load();
    		ThreadUtil.wait(2000);
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

        getScene().getStylesheets().add(BowlerStudio.class.getResource("java-keywords.css").
                toExternalForm());
        
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
        Configuration configuration = new Configuration();
        
	     // Set path to acoustic model.
	     configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
	     // Set path to dictionary.
	     configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
	     // Set language model.
	     configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");
     
        //Log.enableDebugPrint();
		//new MicroServo().toCSG();
        //IObjectDetector detector = new HaarDetector("haarcascade_frontalface_default.xml");
	     
//    	String xmlContent;
//		try {
//			xmlContent = ScriptingEngineWidget.codeFromGistID("2b0cff20ccee085c9c36","TrobotLinks.xml")[0];
//			MobileBase base = new MobileBase(IOUtils.toInputStream(xmlContent, "UTF-8"));
//	    	DHParameterKinematics model = base.getAppendages().get(0); 
//	    	DeviceManager.addConnection(base, "baseTest");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	     System.out.println("Java-Bowler Version: "+SDKBuildInfo.getVersion()); 
	     System.out.println("Bowler-Scripting-Kernel Version: "+BowlerKernelBuildInfo.getVersion());
	     System.out.println("JavaCad Version: "+JavaCadBuildInfo.getVersion());
    }

    public static Parent loadFromFXML() {
        
        if (controller!=null) {
            throw new IllegalStateException("UI already loaded");
        }
        
        FXMLLoader fxmlLoader = new FXMLLoader(
                BowlerStudio.class.getResource("Main.fxml"));
        try {
            fxmlLoader.load();
        } catch (IOException ex) {
            Logger.getLogger(BowlerStudio.class.getName()).
                    log(Level.SEVERE, null, ex);
        }

        Parent root = fxmlLoader.getRoot();
        
        root.getStylesheets().add(BowlerStudio.class.getResource("java-keywords.css").
                toExternalForm());

        controller = fxmlLoader.getController();
        
        log = controller.getLogView();
        

        return root;
    }
    
    public static TextArea getLogView() {
        
        if (log==null) {
            throw new IllegalStateException("Load the UI first.");
        }
        
        return log;
    }
    
	public static Menu getCreatureLabMenue() {
		return controller.getCreatureLabMenue();
	}

	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	public static void setPrimaryStage(Stage primaryStage) {
		BowlerStudio.primaryStage = primaryStage;
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
