/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neuronrobotics.bowlerstudio;

import gnu.io.NRSerialPort;
import haar.HaarFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.text.DefaultCaret;

import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.reactfx.util.FxTimer;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SubScene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.neuronrobotics.addons.driving.HokuyoURGDevice;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngineWidget;
import com.neuronrobotics.bowlerstudio.tabs.CameraTab;
import com.neuronrobotics.jniloader.CHDKImageProvider;
import com.neuronrobotics.jniloader.JavaCVImageProvider;
import com.neuronrobotics.jniloader.OpenCVImageProvider;
import com.neuronrobotics.jniloader.OpenCVJNILoader;
import com.neuronrobotics.jniloader.StaticFileProvider;
import com.neuronrobotics.jniloader.URLImageProvider;
import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.nrconsole.util.GroovyFilter;
import com.neuronrobotics.sdk.pid.VirtualGenericPIDDevice;
import com.neuronrobotics.sdk.util.ThreadUtil;
import com.neuronrobotics.sdk.addons.gamepad.BowlerJInputDevice;
import com.neuronrobotics.sdk.addons.kinematics.gui.*;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
/**
 * FXML Controller class
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class MainController implements Initializable {
    private static int sizeOfTextBuffer = 40000;
	static ByteArrayOutputStream out = new ByteArrayOutputStream();
	static boolean opencvOk=true;
    private static TextArea logViewRef=null;
    private static String newString=null;
	static{
        System.setOut(new PrintStream(out));
        updateLog();
		try{
			OpenCVJNILoader.load();              // Loads the JNI (java native interface)
		}catch(Exception e){
			//e.printStackTrace();
			opencvOk=false;
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("OpenCV missing");
			alert.setHeaderText("Opencv library is missing");
			alert.setContentText(e.getMessage());
			alert .initModality(Modality.APPLICATION_MODAL);
			alert.show();
			e.printStackTrace();
		}
		try{
			// Preload the opencv_objdetect module to work around a known bug.
		    Loader.load(opencv_objdetect.class);
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			HaarFactory.getStream(null);
		}catch (Exception ex){}
	}
	
	private static void updateLog(){
		FxTimer.runLater(
				Duration.ofMillis(100) ,() -> {
					if(logViewRef!=null){
						if(out.size()==0){
							newString=null;
						}else{
							newString = out.toString();
							out.reset();
						}
						if(newString!=null){
							Platform.runLater(() -> {	
								String current = logViewRef.getText()+newString;
								if(current.getBytes().length>sizeOfTextBuffer){
									current=new String(current.substring(current.getBytes().length-sizeOfTextBuffer));
									logViewRef.setText(current);
								}else
									logViewRef.appendText(newString);
								FxTimer.runLater(
										Duration.ofMillis(10) ,() -> {
											logViewRef.setScrollTop(Double.MAX_VALUE);
										});
							});
						}
					}	
					updateLog();					
		});
	}


    //private final CodeArea codeArea = new CodeArea();


    @FXML
    private TextArea logView;

    @FXML
    private ScrollPane editorContainer;

    @FXML
    private Pane viewContainer;

    private SubScene subScene;
    private Jfx3dManager jfx3dmanager ;

	private File openFile;

	private BowlerStudioController application;
	private Stage primaryStage;
	
    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    	logViewRef=logView;

    	jfx3dmanager = new Jfx3dManager();
        application = new BowlerStudioController(jfx3dmanager, this);
        editorContainer.setContent(application);
        
        
        subScene = jfx3dmanager.getSubScene();
        subScene.widthProperty().bind(viewContainer.widthProperty());
        subScene.heightProperty().bind(viewContainer.heightProperty());

        viewContainer.getChildren().add(subScene);

        System.out.println("Welcome to BowlerStudio!");
    }


    /**
     * Returns the location of the Jar archive or .class file the specified
     * class has been loaded from. <b>Note:</b> this only works if the class is
     * loaded from a jar archive or a .class file on the locale file system.
     *
     * @param cls class to locate
     * @return the location of the Jar archive the specified class comes from
     */
    public static File getClassLocation(Class<?> cls) {

//        VParamUtil.throwIfNull(cls);
        String className = cls.getName();
        ClassLoader cl = cls.getClassLoader();
        URL url = cl.getResource(className.replace(".", "/") + ".class");

        String urlString = url.toString().replace("jar:", "");

        if (!urlString.startsWith("file:")) {
            throw new IllegalArgumentException("The specified class\""
                    + cls.getName() + "\" has not been loaded from a location"
                    + "on the local filesystem.");
        }

        urlString = urlString.replace("file:", "");
        urlString = urlString.replace("%20", " ");

        int location = urlString.indexOf(".jar!");

        if (location > 0) {
            urlString = urlString.substring(0, location) + ".jar";
        } else {
            //System.err.println("No Jar File found: " + cls.getName());
        }

        return new File(urlString);
    }
    
    @FXML
    private void onLoadFile(ActionEvent e) {
    	openFile = FileSelectionFactory.GetFile(ScriptingEngineWidget.getLastFile(),
				new GroovyFilter());

        if (openFile == null) {
            return;
        }
        application.createFileTab(openFile);
    }

    @FXML
    private void onConnect(ActionEvent e) {
    	application.getConnectionManager().addConnection();
    }
    
    @FXML
    private void onConnectVirtual(ActionEvent e) {
    	application.getConnectionManager().addConnection(new VirtualGenericPIDDevice(10000),"virtual");
    }

  
    @FXML
    private void onClose(ActionEvent e) {
        System.exit(0);
    }

    public TextArea getLogView() {
        return logView;
    }

	public void disconnect() {
		jfx3dmanager.disconnect();
		application.disconnect();
	}


	@FXML public void onConnectCHDKCamera(ActionEvent event) {
		try{
			application.getConnectionManager().addConnection(new CHDKImageProvider(),"cameraCHDK");
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@FXML public void onAddDefaultRightArm() {

		application.onAddDefaultRightArm();
	}



	@FXML public void onAddVRCamera() {
		application.onAddVRCamera();
	}

	
	@FXML public void onConnectCVCamera(ActionEvent event) {
		application.getConnectionManager().onConnectCVCamera(event);
		
	}


	@FXML public void onConnectJavaCVCamera() {
		application.getConnectionManager().onConnectJavaCVCamera();

	}


	@FXML public void onConnectFileSourceCamera() {
		application.getConnectionManager().onConnectFileSourceCamera();
	}


	@FXML public void onConnectURLSourceCamera() {
		application.getConnectionManager().onConnectURLSourceCamera();
	}


	@FXML public void onConnectHokuyoURG(ActionEvent event) {
		application.getConnectionManager().onConnectHokuyoURG(event);
		
	}


	@FXML public void onConnectGamePad(ActionEvent event) {
		application.getConnectionManager().onConnectGamePad(event);
		
	}
	


}
