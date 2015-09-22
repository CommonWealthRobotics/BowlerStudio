/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neuronrobotics.bowlerstudio;

import haar.HaarFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.swing.UIManager;

import org.apache.commons.io.IOUtils;
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
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.neuronrobotics.bowlerstudio.creature.CreatureLab;
import com.neuronrobotics.bowlerstudio.scripting.IGithubLoginListener;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngineWidget;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingWidgetType;
import com.neuronrobotics.bowlerstudio.tabs.DyIOResourceFactory;
import com.neuronrobotics.imageprovider.CHDKImageProvider;
import com.neuronrobotics.imageprovider.NativeResource;
import com.neuronrobotics.imageprovider.OpenCVJNILoader;
import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.nrconsole.util.GroovyFilter;
import com.neuronrobotics.nrconsole.util.XmlFilter;
import com.neuronrobotics.pidsim.LinearPhysicsEngine;
import com.neuronrobotics.replicator.driver.NRPrinter;
import com.neuronrobotics.replicator.driver.Slic3r;
import com.neuronrobotics.sdk.pid.VirtualGenericPIDDevice;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.gui.*;
import com.sun.crypto.provider.DHParameterGenerator;
import com.sun.speech.freetts.VoiceManager;

import javafx.scene.control.Menu;
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
    @FXML
    private MenuBar menuBar;
    @FXML
    private MenuItem logoutGithub;
    
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
		DyIOResourceFactory.load();

	}
	
	private static void updateLog(){
		if(logViewRef!=null){
			String current;
			String finalStr;
			if(out.size()==0){
				newString=null;
			}else{
				newString = out.toString();
				out.reset();
			}
			if(newString!=null){
				current = logViewRef.getText()+newString;
				try{
					finalStr =new String(current.substring(current.getBytes().length-sizeOfTextBuffer));
				}catch (StringIndexOutOfBoundsException ex){
					finalStr =current;
				}

				logViewRef.setText(finalStr);
				logViewRef.setScrollTop(Double.MAX_VALUE);
			}
			
		}	
		FxTimer.runLater(
				Duration.ofMillis(50) ,() -> {

					updateLog();					
		});
	}


    //private final CodeArea codeArea = new CodeArea();


    @FXML
    private Pane logView;

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
	private ScriptingEngineWidget cmdLine;
	@FXML Menu CreatureLabMenue;
	
    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

    	jfx3dmanager = new Jfx3dManager();
        setApplication(new BowlerStudioController(jfx3dmanager, this));
        editorContainer.setContent(getApplication());
        
        
        subScene = jfx3dmanager.getSubScene();
        subScene.widthProperty().bind(viewContainer.widthProperty());
        subScene.heightProperty().bind(viewContainer.heightProperty());

        viewContainer.getChildren().add(subScene);

        System.out.println("Welcome to BowlerStudio!");
		new Thread(){
			public void run(){
				setName("Load Haar Thread");
				try{
					HaarFactory.getStream(null);
				}catch (Exception ex){}
			}
		}.start();
		
//		getAddDefaultRightArm().setOnAction(event -> {
//			
//			application.onAddDefaultRightArm(event);
//		});
//		getAddVRCamera().setOnAction(event -> {
//			if(AddVRCamera.isSelected())
//				application.onAddVRCamera(event);
//		});
		
		FxTimer.runLater(
				Duration.ofMillis(100) ,() -> {
					if(ScriptingEngineWidget.getLoginID()!=null){
						setToLoggedIn(ScriptingEngineWidget.getLoginID());
					}else{
						setToLoggedOut();
					}
												
		});
		ScriptingEngine.addIGithubLoginListener(new IGithubLoginListener() {
			
			@Override
			public void onLogout(String oldUsername) {
				setToLoggedOut();
			}
			
			@Override
			public void onLogin(String newUsername) {
				setToLoggedIn(newUsername);
			}
		});
		//logView.resize(250, 300);
		// after connection manager set up, add scripting widget
    	logViewRef=new TextArea();
    	logViewRef.prefWidthProperty().bind( logView.widthProperty().divide(2));
    	logViewRef.prefHeightProperty().bind( logView.heightProperty().subtract(40));
    	
    	
    	cmdLine = new ScriptingEngineWidget(ScriptingWidgetType.CMDLINE);
    	VBox box = new VBox();
    	box.getChildren().add(logViewRef);
    	box.getChildren().add(cmdLine);
    	VBox.setVgrow(logViewRef, Priority.ALWAYS);
    	box.prefWidthProperty().bind( logView.widthProperty().subtract(10));
    	
    	logView.getChildren().addAll(box);
    	

    	
		
        //BowlerStudio.speak("Welcome to Bowler Studio");
    }
    
    private void setToLoggedIn(final String name){
		FxTimer.runLater(
				Duration.ofMillis(100) ,() -> {
			logoutGithub.disableProperty().set(false);
			logoutGithub.setText("Log out "+name);
		});
    }
    
    private void setToLoggedOut(){
		Platform.runLater(() -> {
			logoutGithub.disableProperty().set(true);
			logoutGithub.setText("Anonymous");
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
    	new Thread(){
    		public void run(){
    			setName("Load File Thread");
    	    	openFile = FileSelectionFactory.GetFile(ScriptingEngineWidget.getLastFile(),
    					new ExtensionFilter("Groovy Scripts","*.groovy","*.java","*.txt"));

    	        if (openFile == null) {
    	            return;
    	        }
    	        getApplication().createFileTab(openFile);
    		}
    	}.start();
    }

    @FXML
    private void onConnect(ActionEvent e) {
    	new Thread(){
    		public void run(){
    			setName("Load BowlerDevice Dialog Thread");
    	    	ConnectionManager.addConnection();
    		}
    	}.start();
    }
    
    @FXML
    private void onConnectVirtual(ActionEvent e) {
    	
    	ConnectionManager.addConnection(new VirtualGenericPIDDevice(10000),"virtual");
    }

  
    @FXML
    private void onClose(ActionEvent e) {
        System.exit(0);
    }

    public TextArea getLogView() {
        return logViewRef;
    }

	public void disconnect() {
		jfx3dmanager.disconnect();
		getApplication().disconnect();
	}
	
	public void openUrlInNewTab(URL url){
		getApplication().openUrlInNewTab(url);
	}
	


	@FXML public void onConnectCHDKCamera(ActionEvent event) {
		Platform.runLater(()->{
			try {
				ConnectionManager.addConnection(new CHDKImageProvider(),"cameraCHDK");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}



	@FXML public void onConnectCVCamera(ActionEvent event) {
   
		Platform.runLater(()->ConnectionManager.onConnectCVCamera());
  
		
	}


	@FXML public void onConnectJavaCVCamera() {

		Platform.runLater(()->ConnectionManager.onConnectJavaCVCamera());
    
	}


	@FXML public void onConnectFileSourceCamera() {
    	Platform.runLater(()->ConnectionManager.onConnectFileSourceCamera());

	}


	@FXML public void onConnectURLSourceCamera() {

    	Platform.runLater(()->ConnectionManager.onConnectURLSourceCamera());

	}


	@FXML public void onConnectHokuyoURG(ActionEvent event) {
		Platform.runLater(()->ConnectionManager.onConnectHokuyoURG());
		
	}


	@FXML public void onConnectGamePad(ActionEvent event) {
		Platform.runLater(()->ConnectionManager.onConnectGamePad("gamepad"));
		
	}


//	public CheckMenuItem getAddVRCamera() {
//		return AddVRCamera;
//	}
//
//
//	public void setAddVRCamera(CheckMenuItem addVRCamera) {
//		AddVRCamera = addVRCamera;
//	}
//
//
//	public CheckMenuItem getAddDefaultRightArm() {
//		return AddDefaultRightArm;
//	}
//
//
//	public void setAddDefaultRightArm(CheckMenuItem addDefaultRightArm) {
//		AddDefaultRightArm = addDefaultRightArm;
//	}


	@FXML public void onLogin() {
    	new Thread(){
    		public void run(){
    			setName("Login Gist Thread");
    			try {
    				ScriptingEngineWidget.login();
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
    	}.start();
	
	}


	@FXML public void onLogout() {
		ScriptingEngineWidget.logout();
	}


	@FXML public void onConnectPidSim() {
		LinearPhysicsEngine eng =new LinearPhysicsEngine();
		eng.connect();
		ConnectionManager.addConnection(eng,"engine");
	}



	@FXML public void onPrint(ActionEvent event) {
		NRPrinter printer =(NRPrinter) ConnectionManager.pickConnectedDevice(NRPrinter.class);
		if(printer!=null){
			// run a print here
		}
		
	}



	@FXML public void onMobileBaseFromFile() {
    	new Thread(){
    		public void run(){
    			setName("Load Mobile Base Thread");
    	    	openFile = FileSelectionFactory.GetFile(ScriptingEngineWidget.getLastFile(),
    	    			new ExtensionFilter("MobileBase XML","*.xml","*.XML"));

    	        if (openFile == null) {
    	            return;
    	        }
    	        Platform.runLater(()->{
    				try {
    					MobileBase mb = new MobileBase(new FileInputStream(openFile));
    					ConnectionManager.addConnection(mb,mb.getScriptingName());
    				} catch (Exception e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    			});
    		}
    	}.start();
		
	}
	
	@FXML public void onRobotArm(ActionEvent event) {
		loadMobilebaseFromGist("2b0cff20ccee085c9c36","TrobotLinks.xml");
	}
	@FXML public void onHexapod() {
		loadMobilebaseFromGist("bcb4760a449190206170","CarlTheRobot.xml");
	}
	@FXML public void onGrasshopper() {
		loadMobilebaseFromGist("a6cbefc11693162cf9d4","GrassHopper.xml");
	}

	@FXML public void onInputArm() {
		loadMobilebaseFromGist("98892e87253005adbe4a","TrobotMaster.xml");
	}

	@FXML public void onAddElephant() {
		loadMobilebaseFromGist("aef13d65093951d13235","Elephant.xml");
	}

	public Menu getCreatureLabMenue() {
		return CreatureLabMenue;
	}

	public void setCreatureLabMenue(Menu creatureLabMenue) {
		CreatureLabMenue = creatureLabMenue;
	}
	
	public void loadMobilebaseFromGist(String id,String file){
		new Thread(){
    		public void run(){
				try {
					BowlerStudio.openUrlInNewTab(new URL("https://gist.github.com/"+id));
					String xmlContent = ScriptingEngineWidget.codeFromGistID(id,file)[0];
					MobileBase mb = new MobileBase(IOUtils.toInputStream(xmlContent, "UTF-8"));
					
					mb.setSelfSource(new String[]{id,file});
					ConnectionManager.addConnection(mb,mb.getScriptingName());
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}.start();
	}

	@FXML public void onMobileBaseFromGist() {
		TextInputDialog dialog = new TextInputDialog("https://gist.github.com/madhephaestus/bcb4760a449190206170");
		dialog.setTitle("Select a Creature From a Gist");
		dialog.setHeaderText("Enter the URL (Link from the browser)");
		dialog.setContentText("Link to Gist: ");

		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
		   
		    String gistcode = ScriptingEngineWidget.urlToGist(result.get());
		    System.out.println("Creature Gist " + gistcode);
		    ArrayList<String> choices =ScriptingEngineWidget.filesInGist(gistcode,".xml");
		    String suggestedChoice="";
		    int numXml=0;
		    for(int i=0;i<choices.size();i++){
		    	String s = choices.get(i);
		    	if(s.toLowerCase().endsWith(".xml")){
		    		suggestedChoice=s;
		    		numXml++;
		    	}
		    }
		    
		    if(numXml ==1){
		    	System.out.println("Found just one file at  " + suggestedChoice);
		    	//loadMobilebaseFromGist(gistcode,suggestedChoice);
		    	//return;
		    	
		    }

		    ChoiceDialog<String> d = new ChoiceDialog<>(suggestedChoice, choices);
		    d.setTitle("Choose a file in the gist");
		    d.setHeaderText("Select from the files in the gist to pick the Creature File");
		    d.setContentText("Choose A Creature:");

		    // Traditional way to get the response value.
		    Optional<String> r = d.showAndWait();
		    if (r.isPresent()){
		        System.out.println("Your choice: " + r.get());
		        loadMobilebaseFromGist(gistcode,r.get());
		    }
		}
	
	}
	
	public ScriptingEngineWidget createFileTab(File file){
		return getApplication().createFileTab(file);
	}

	public BowlerStudioController getApplication() {
		return application;
	}

	public void setApplication(BowlerStudioController application) {
		this.application = application;
	}


}
