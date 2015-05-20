/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neuronrobotics.bowlerstudio;

import haar.HaarFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.time.Duration;
import java.util.ResourceBundle;

//import org.bytedeco.javacpp.Loader;
//import org.bytedeco.javacpp.opencv_objdetect;
import org.opencv.core.Core;
import org.reactfx.util.FxTimer;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SubScene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngineWidget;
import com.neuronrobotics.jniloader.CHDKImageProvider;
import com.neuronrobotics.jniloader.NativeResource;
import com.neuronrobotics.jniloader.OpenCVJNILoader;
import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.nrconsole.util.GroovyFilter;
import com.neuronrobotics.replicator.driver.Slic3r;
import com.neuronrobotics.sdk.pid.VirtualGenericPIDDevice;
import com.neuronrobotics.sdk.addons.kinematics.gui.*;
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
//		try{
//			// Preload the opencv_objdetect module to work around a known bug.
//		    Loader.load(opencv_objdetect.class);
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		

			
		
		
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
	
    @FXML
    private CheckMenuItem AddDefaultRightArm;
    @FXML
    private CheckMenuItem AddVRCamera;
	
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
		new Thread(){
			public void run(){
				try{
					HaarFactory.getStream(null);
				}catch (Exception ex){}
			}
		}.start();
		
		getAddDefaultRightArm().setOnAction(event -> {
			
			application.onAddDefaultRightArm(event);
		});
		getAddVRCamera().setOnAction(event -> {
			if(AddVRCamera.isSelected())
				application.onAddVRCamera(event);
		});
		
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



	@FXML public void onConnectCVCamera(ActionEvent event) {
		application.getConnectionManager().onConnectCVCamera();
		
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
		application.getConnectionManager().onConnectHokuyoURG();
		
	}


	@FXML public void onConnectGamePad(ActionEvent event) {
		application.getConnectionManager().onConnectGamePad();
		
	}


	public CheckMenuItem getAddVRCamera() {
		return AddVRCamera;
	}


	public void setAddVRCamera(CheckMenuItem addVRCamera) {
		AddVRCamera = addVRCamera;
	}


	public CheckMenuItem getAddDefaultRightArm() {
		return AddDefaultRightArm;
	}


	public void setAddDefaultRightArm(CheckMenuItem addDefaultRightArm) {
		AddDefaultRightArm = addDefaultRightArm;
	}
	


}
