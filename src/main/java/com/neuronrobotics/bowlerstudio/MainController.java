/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neuronrobotics.bowlerstudio;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import org.reactfx.util.FxTimer;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SubScene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;

import com.neuronrobotics.bowlerstudio.tabs.CameraTab;
import com.neuronrobotics.jniloader.CHDKImageProvider;
import com.neuronrobotics.jniloader.JavaCVImageProvider;
import com.neuronrobotics.jniloader.OpenCVImageProvider;
import com.neuronrobotics.jniloader.OpenCVJNILoader;
import com.neuronrobotics.sdk.util.ThreadUtil;
import com.neuronrobotics.sdk.addons.kinematics.gui.*;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
/**
 * FXML Controller class
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class MainController implements Initializable {
    
	static ByteArrayOutputStream out = new ByteArrayOutputStream();
	static boolean opencvOk=true;

	static{
        System.setOut(new PrintStream(out));
		Platform.runLater(() -> {
			handlePrintUpdate();
		});
		try{
			OpenCVJNILoader.load();              // Loads the JNI (java native interface)
		}catch(Exception e){
			//e.printStackTrace();
			opencvOk=false;
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("OpenCV missing");
			alert.setHeaderText("Opencv library is missing");
			alert.setContentText("You should install OpenCV");
			alert .initModality(Modality.APPLICATION_MODAL);
			alert.show();
		}
	}
	
	static void handlePrintUpdate() {


		FxTimer.runLater(
				Duration.ofMillis(100) ,() -> {
			if(out.size()>0){
				Platform.runLater(() -> {
					String newString = out.toString();
					out.reset();
					if(logViewRef!=null){
						String current = logViewRef.getText()+newString;
						if(current.getBytes().length>2000)
							current=new String(current.substring(current.getBytes().length-2000));
						final String toSet=current;
						logViewRef.setText(toSet);
						logViewRef.setScrollTop(Double.MAX_VALUE);	
					}
				});
			}
		});
		Platform.runLater(() -> {
			// TODO Auto-generated method stub
			handlePrintUpdate();
		});
	}



    //private final CodeArea codeArea = new CodeArea();

    private static TextArea logViewRef=null;
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
	
    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    	logViewRef=logView;
        //
//        codeArea.textProperty().addListener(
//                (ov, oldText, newText) -> {
//                    Matcher matcher = KEYWORD_PATTERN.matcher(newText);
//                    int lastKwEnd = 0;
//                    StyleSpansBuilder<Collection<String>> spansBuilder
//                    = new StyleSpansBuilder<>();
//                    while (matcher.find()) {
//                        spansBuilder.add(Collections.emptyList(),
//                                matcher.start() - lastKwEnd);
//                        spansBuilder.add(Collections.singleton("keyword"),
//                                matcher.end() - matcher.start());
//                        lastKwEnd = matcher.end();
//                    }
//                    spansBuilder.add(Collections.emptyList(),
//                            newText.length() - lastKwEnd);
//                    codeArea.setStyleSpans(0, spansBuilder.create());
//                });
//
//        EventStream<Change<String>> textEvents
//                = EventStreams.changesOf(codeArea.textProperty());

//        textEvents.reduceSuccessions((a, b) -> b, Duration.ofMillis(500)).
//                subscribe(code -> {
//                    if (autoCompile) {
//                        compile(code.getNewValue());
//                    }
//                });

//        codeArea.replaceText(
//                "\n"
//                + "CSG cube = new Cube(20).toCSG()\n"
//                + "CSG sphere = new Sphere(12.5).toCSG()\n"
//                + "\n"
//                + "cube.difference(sphere)");
    	jfx3dmanager = new Jfx3dManager();
        application = new BowlerStudioController(jfx3dmanager);
        editorContainer.setContent(application);
        
        
        subScene = jfx3dmanager.getSubScene();
        subScene.widthProperty().bind(viewContainer.widthProperty());
        subScene.heightProperty().bind(viewContainer.heightProperty());

        viewContainer.getChildren().add(subScene);

        System.out.println("Starting Application");
    }

 

//	private void setCode(String code) {
//        codeArea.replaceText(code);
//    }
//
//    private String getCode() {
//        return codeArea.getText();
//    }

//    private void clearLog() {
//        logView.setText("");
//    }
//
//    private void compile(String code) {
//      	
//    }

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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Script File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Script files",
                        "*.jfxscad", "*.groovy", "*.java"));
        

        openFile = fileChooser.showOpenDialog(null);

        if (openFile == null) {
            return;
        }
        application.createFileTab(openFile);
    }

    @FXML
    private void onConnect(ActionEvent e) {
    	application.addConnection();
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



	@FXML public void onConnectCVCamera(ActionEvent event) {
		List<String> choices = new ArrayList<>();
		choices.add("0");
		choices.add("1");
		choices.add("2");
		choices.add("3");
		choices.add("4");
		
		ChoiceDialog<String> dialog = new ChoiceDialog<>("0", choices);
		dialog.setTitle("OpenCV Camera Index Chooser");
		dialog.setHeaderText("Choose an OpenCV camera");
		dialog.setContentText("Camera Index:");

		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		
		// The Java 8 way to get the response value (with lambda expression).
		result.ifPresent(letter -> {
			OpenCVImageProvider p = new OpenCVImageProvider(Integer.parseInt(letter));
			String name = "camera"+letter;
			application.addConnection(p,name);
			//application.addTab(new CameraTab(p, name), true);
		});
		
		
		
	}

	@FXML public void onConnectCHDKCamera(ActionEvent event) {
		try{
			application.addConnection(new CHDKImageProvider(),"cameraCHDK");
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



	@FXML public void onConnectJavaCVCamera() {
		List<String> choices = new ArrayList<>();
		choices.add("0");
		choices.add("1");
		choices.add("2");
		choices.add("3");
		choices.add("4");
		
		ChoiceDialog<String> dialog = new ChoiceDialog<>("0", choices);
		dialog.setTitle("OpenCV Camera Index Chooser");
		dialog.setHeaderText("Choose an OpenCV camera");
		dialog.setContentText("Camera Index:");

		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		
		// The Java 8 way to get the response value (with lambda expression).
		result.ifPresent(letter -> {
			JavaCVImageProvider p;
			try {
				p = new JavaCVImageProvider(Integer.parseInt(letter));
				String name = "camera"+letter;
				application.addConnection(p,name);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});
	}
	


}
