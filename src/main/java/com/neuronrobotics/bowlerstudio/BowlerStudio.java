package com.neuronrobotics.bowlerstudio;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import com.neuronrobotics.bowlerstudio.creature.ICadGenerator;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngineWidget;
import com.neuronrobotics.bowlerstudio.vitamins.MicroServo;
import com.neuronrobotics.imageprovider.HaarDetector;
import com.neuronrobotics.imageprovider.IObjectDetector;
import com.neuronrobotics.imageprovider.OpenCVJNILoader;
import com.neuronrobotics.nrconsole.plugin.DyIO.DyIOConsole;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.common.DeviceManager;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.config.SDKBuildInfo;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.pid.VirtualGenericPIDDevice;
import com.neuronrobotics.sdk.ui.AbstractConnectionPanel;
import com.neuronrobotics.sdk.ui.ConnectionImageIconFactory;
import com.neuronrobotics.sdk.util.ThreadUtil;
import com.sun.speech.freetts.FeatureProcessor;
import com.sun.speech.freetts.Item;
import com.sun.speech.freetts.ItemContents;
import com.sun.speech.freetts.ProcessException;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.en.us.FeatureProcessors.WordNumSyls;

import edu.cmu.sphinx.api.Configuration;
import eu.mihosoft.vrl.v3d.CSG;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
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

    /**
     * @param args the command line arguments
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
    	
    	if(args.length==0)
    		launch(args);
    	else{
            new JFXPanel(); // initializes JavaFX environment
            OpenCVJNILoader.load();              // Loads the OpenCV JNI (java native interface)
    		boolean startLoadingScripts=false;
    		for(String s :args){
    			if(startLoadingScripts){
    				try{
    					ScriptingEngine.inlineFileScriptRun(new File(s), null);
    				}catch(Error e)
    				{
    					e.printStackTrace();
    				}
    			}
    			if(s.contains("scripts")){
    				startLoadingScripts=true;
    			}
    		}
    		startLoadingScripts=false;
    		Object ret=null;
    		for(String s :args){

    			if(startLoadingScripts){
    				try{
    					ret=ScriptingEngine.inlineFileScriptRun(new File(s), (ArrayList<Object>)ret);
    				}catch(Error e)
    				{
    					e.printStackTrace();
    				}
    			}
    			if(s.contains("pipe")){
    				startLoadingScripts=true;
    			}
    		}
    	}
    }
   
	public static void setSelectedTab(Tab tab) {
		controller.getApplication().setSelectedTab(tab);
	}

    @Override
    public void start(Stage primaryStage) throws Exception {
        setPrimaryStage(primaryStage);
		Parent main = loadFromFXML();

        Scene scene = new Scene(main, 1024, 768,true);

        scene.getStylesheets().add(BowlerStudio.class.getResource("java-keywords.css").
                toExternalForm());
        
        PerspectiveCamera camera = new PerspectiveCamera();
        
        scene.setCamera(camera);

        primaryStage.setTitle("Bowler Studio");
        primaryStage.setScene(scene);
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
		System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
		VoiceManager voiceManager = VoiceManager.getInstance();
		com.sun.speech.freetts.Voice voice = voiceManager
				.getVoice("kevin16");
		Thread t = new Thread() {
			public void run() {
				setName("Speaking Thread");

				
				if(voice !=null){
					voice.setRate(200f);
					voice.allocate();
					voice.speak(msg);
					voice.deallocate();
				}else{
					System.out.println("All voices available:");
					com.sun.speech.freetts.Voice[] voices=voiceManager.getVoices();
					for (int i=0; i < voices.length; i++) {
					  System.out.println("    " + voices[i].getName() + " ("+ voices[i].getDomain()+ " domain)");
					}
				}
			}
		};
		t.start();
		WordNumSyls feature = (WordNumSyls)voice.getFeatureProcessor("word_numsyls");
		if(feature!=null)
		try {
			
			System.out.println("Syllables# = "+feature.process(null));
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;
	}
	
	public static ScriptingEngineWidget createFileTab(File file){
		return controller.createFileTab(file);
	}
}
