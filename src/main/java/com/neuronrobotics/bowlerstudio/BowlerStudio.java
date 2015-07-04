package com.neuronrobotics.bowlerstudio;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngineWidget;
import com.neuronrobotics.jniloader.HaarDetector;
import com.neuronrobotics.jniloader.IObjectDetector;
import com.neuronrobotics.jniloader.OpenCVJNILoader;
import com.neuronrobotics.nrconsole.plugin.DyIO.DyIOConsole;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.config.SDKBuildInfo;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.pid.VirtualGenericPIDDevice;
import com.neuronrobotics.sdk.ui.AbstractConnectionPanel;
import com.neuronrobotics.sdk.ui.ConnectionImageIconFactory;
import com.neuronrobotics.sdk.util.ThreadUtil;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class BowlerStudio extends Application {
    
    private static TextArea log;
    private static MainController controller;
	private static Stage primaryStage;

    /**
     * @param args the command line arguments
     */
    @SuppressWarnings("unchecked")
	public static void main(String[] args) {
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
    
//	public static void renderSplashFrame(Graphics2D g, int frame) {
//        final String[] comps = {"OpenCV", "JavaCad", "BowlerEngine"};
//        //g.setComposite(AlphaComposite.Clear);
//        g.fillRect(120,140,200,40);
//        g.setPaintMode();
//        g.setColor(Color.RED);
//        g.drawString("Loading "+comps[(frame/5)%comps.length]+"...", 120, 150);
//    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        setPrimaryStage(primaryStage);
		Parent main = loadFromFXML();

        Scene scene = new Scene(main, 1024, 700,true);

        scene.getStylesheets().add(BowlerStudio.class.getResource("java-keywords.css").
                toExternalForm());
        
        PerspectiveCamera camera = new PerspectiveCamera();
        
        scene.setCamera(camera);

        primaryStage.setTitle("Bowler Studio");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(700);
        primaryStage.show();
        primaryStage.setOnCloseRequest(arg0 -> {
        	
        	controller.disconnect();
        	ThreadUtil.wait(100);
        	System.exit(0);
		});
        primaryStage.setTitle("Bowler Studio: v "+StudioBuildInfo.getVersion());
        primaryStage.getIcons().add(new Image(AbstractConnectionPanel.class.getResourceAsStream( "images/hat.png" ))); 
        Log.enableDebugPrint();
       
        //IObjectDetector detector = new HaarDetector("haarcascade_frontalface_default.xml");
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

	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	public static void setPrimaryStage(Stage primaryStage) {
		BowlerStudio.primaryStage = primaryStage;
	}
}
