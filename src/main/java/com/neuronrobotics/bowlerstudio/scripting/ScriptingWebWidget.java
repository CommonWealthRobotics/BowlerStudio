package com.neuronrobotics.bowlerstudio.scripting;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import org.python.util.PythonInterpreter;
import org.python.core.*;
import org.reactfx.util.FxTimer;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FilenameUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHGistFile;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;

import com.kenai.jaffl.provider.jffi.SymbolNotFoundError;
import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.ConnectionManager;
import com.neuronrobotics.bowlerstudio.PluginManager;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.imageprovider.AbstractImageProvider;
import com.neuronrobotics.imageprovider.OpenCVImageProvider;
import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.nrconsole.util.GroovyFilter;
import com.neuronrobotics.replicator.driver.BowlerBoardDevice;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.pid.GenericPIDDevice;

import com.neuronrobotics.sdk.util.IFileChangeListener;
import com.neuronrobotics.sdk.util.ThreadUtil;
import com.neuronrobotics.sdk.addons.kinematics.xml.*;

import eu.mihosoft.vrl.v3d.*;
import eu.mihosoft.vrl.v3d.samples.*;

@SuppressWarnings("unused")
public class ScriptingWebWidget extends BorderPane implements ChangeListener<Object>{

	private boolean running = false;
	private Thread scriptRunner = null;
	
	private Dimension codeDimentions = new Dimension(1168, 768);
	//Label fileLabel = new Label();
	private Object scriptResult;
	private String codeText="";

	private ArrayList<IScriptEventListener> listeners = new ArrayList<>();

	private Button runfx = new Button("Run");;
	private Button edit = new Button("Edit...");
	private WebEngine engine;

	private String addr;
	boolean loadGist = false;

	private ScriptingWidgetType type;
	
	final ComboBox<String> fileListBox = new ComboBox<>();
	private File currentFile = null;

	private HBox controlPane;
	private String currentGit;
	private String currentGist;
	private boolean isOwnedByLoggedInUser;
	private ImageView image=new ImageView();

	
	public ScriptingWebWidget(File currentFile, String currentGist,
			WebEngine engine) throws IOException, InterruptedException {
		this(ScriptingWidgetType.GIST);
		runfx.setGraphic(AssetFactory.loadIcon("Run.png"));
		edit.setGraphic(AssetFactory.loadIcon("Edit-Script.png"));
		this.currentFile = currentFile;
		try {
			loadCodeFromGist(currentGist, engine);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	
	private void startStopAction(){
		runfx.setDisable(true);
		if (running)
			stop();
		else
			start();
		runfx.setDisable(false);
	}

	public ScriptingWebWidget(ScriptingWidgetType type) {
		this.type = type;

		runfx.setOnAction(e -> {
	    	new Thread(){
	    		public void run(){

	    			startStopAction();
	    		}
	    	}.start();
		});
		edit.setOnAction(e -> {
	    	new Thread(){
	    		public void run(){
	    			if(isOwnedByLoggedInUser)
	    				BowlerStudio.createFileTab(currentFile);
	    			else{
	    				// todo fork git repo
	    				System.out.println("Making Fork...");
	    				GHGist newGist;
						try {
							newGist = ScriptingEngine.fork(currentGist);
							Map<String, GHGistFile> fileMap = newGist.getFiles();
							if(fileMap.size()==1){
								String filename = (String) fileMap.keySet().toArray()[0];
								String url =newGist.getGitPullUrl();
								File file = ScriptingEngine.fileFromGit(url, filename);
								BowlerStudio.createFileTab(file);
							}else{
			    				String webURL = newGist.getHtmlUrl();
			    				try {
									BowlerStudio.openUrlInNewTab(new URL(webURL));
								} catch (MalformedURLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						} catch (Exception e1) {
							BowlerStudioController.highlightException(currentFile, e1);
						}

	    			}
	    					
	    		}
	    	}.start();

		});
		


		setPadding(new Insets(1, 0, 3, 10));

		controlPane = new HBox(20);

		controlPane.getChildren().add(runfx);
		controlPane.getChildren().add(image);
		controlPane.getChildren().add(edit);
		
		controlPane.getChildren().add(fileListBox);
		
		// put the flowpane in the top area of the BorderPane
		setTop(controlPane);

		addIScriptEventListener(BowlerStudioController.getBowlerStudio());
		reset();
	}

	private void reset() {
		running = false;
		Platform.runLater(() -> {
			runfx.setText("Run");
			runfx.setGraphic(AssetFactory.loadIcon("Run.png"));
			runfx.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
			
		});

	}


	public void addIScriptEventListener(IScriptEventListener l) {
		if (!listeners.contains(l))
			listeners.add(l);
	}

	public void removeIScriptEventListener(IScriptEventListener l) {
		if (listeners.contains(l))
			listeners.remove(l);
	}

	public void stop() {
		// TODO Auto-generated method stub

		reset();
		if (scriptRunner != null)
			while (scriptRunner.isAlive()) {

				Log.debug("Interrupting");
				ThreadUtil.wait(10);
				try {
					scriptRunner.interrupt();
					scriptRunner.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

	}

//	public void loadCodeFromFile(File currentFile) throws IOException {
//		if (!currentFile.exists()) {
//			currentFile.createNewFile();
//		}
//		setUpFile(currentFile);
//		setCode(new String(Files.readAllBytes(currentFile.toPath())));
//	}
	
	private void loadGitLocal(String id, String file){
		//System.out.println("Loading "+file+" from "+id);
		String[] code;
		try {
			code = ScriptingEngine.codeFromGit(id,file);
			if (code != null) {
				setCode(code[0]);
				currentFile = new File(code[2]);
			}
			isOwnedByLoggedInUser = ScriptingEngine.checkOwner(currentFile);
			Platform.runLater(() -> {
				if(isOwnedByLoggedInUser){
					edit.setText("Edit...");
					edit.setGraphic(AssetFactory.loadIcon("Edit-Script.png"));
				}else{
					edit.setText("Make Copy");
					edit.setGraphic(AssetFactory.loadIcon("Make-Copy-Script.png"));
				}
			});
			try {
				image.setImage(AssetFactory.loadAsset("Script-Tab-"+ScriptingEngine.getShellType(currentFile.getName())+".png"));
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

		} catch (Exception e) {
			  StringWriter sw = new StringWriter();
		      PrintWriter pw = new PrintWriter(sw);
		      e.printStackTrace(pw);
		      System.out.println(sw.toString());
		}
	}

	public void loadCodeFromGist(String a, WebEngine e)
			throws Exception {
		//new Thread(()->{
			addr = a;
			engine = e;
			loadGist = true;
			fileListBox.valueProperty().removeListener(this);
			Platform.runLater(()->runfx.setDisable(true));
			Platform.runLater(()->edit.setDisable(true));
			Platform.runLater(()->fileListBox.getItems().clear());
			List<String> gists = ScriptingEngine.getCurrentGist(addr, engine);
			ArrayList<String> fileList;
			if(!gists.isEmpty()){
				currentGist=gists.get(0);
				currentGit = "https://gist.github.com/" + currentGist+".git";
			}else if(addr.contains("https://github.com/")){
				
				if (a.endsWith("/")) {
				    a = a.substring(0, a.length() - 1);
				}
				currentGit =a+".git";
				
			}
			else{
				return;
			}
			fileList = ScriptingEngine.filesInGit(currentGit);
			
//			for(String s:fileList){
//				System.out.println("GITS: "+s);
//			}
			if(!fileList.isEmpty())
				loadGitLocal(currentGit, fileList.get(0));
			
			Platform.runLater(()->{
				
				for(String s:fileList){
					fileListBox.getItems().add(s);
				}
				if(!fileList.isEmpty()){
					fileListBox.setValue(fileList.get(0));
					fileListBox.valueProperty().addListener(this);
					Platform.runLater(()->runfx.setDisable(false));
					Platform.runLater(()->edit.setDisable(false));
				}
			});
		//}).start();
		
	}



	private void start() {
		BowlerStudio.clearConsole();
		try {
			ScriptingEngine.setAutoupdate(true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		running = true;
		Platform.runLater(()->{
			runfx.setText("Stop");
			runfx.setGraphic(AssetFactory.loadIcon("Stop.png"));
			runfx.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
			
		});
		scriptRunner = new Thread() {

			public void run() {
				String name;
				try{
					name = currentFile.getName();
				}catch (NullPointerException e){
					name="";
				}
				try {
					Object obj = ScriptingEngine.inlineScriptRun(currentFile, null,ScriptingEngine.getShellType(name));
					for (IScriptEventListener l : listeners) {
						l.onScriptFinished(obj, scriptResult,currentFile);
					}
					Platform.runLater(() -> {
						append("\n" + currentFile + " Completed\n");
					});
					scriptResult = obj;
					reset();

				} 
				catch (groovy.lang.MissingPropertyException |org.python.core.PyException d){
					Platform.runLater(() -> {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Device missing error");
						String message = "This script needs a device connected: ";
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						d.printStackTrace(pw);
						
						String stackTrace = sw.toString();
						
						if(stackTrace.contains("dyio"))
							message+="dyio";
						else if(stackTrace.contains("camera"))
							message+="camera";
						else if(stackTrace.contains("gamepad"))
							message+="gamepad";
						else
							message+=stackTrace;
						alert.setHeaderText(message);
						alert.setContentText("You need to connect it before running again");
						alert.showAndWait();
						if(stackTrace.contains("dyio"))
							ConnectionManager.addConnection();
						else if(stackTrace.contains("camera"))
							ConnectionManager.addConnection(new OpenCVImageProvider(0),"camera0");
						else if(stackTrace.contains("gamepad"))
							ConnectionManager.onConnectGamePad("gamepad");
						reset();
					});
					
				}
				catch (Exception ex) {
					System.err.println("Script exception of type= "+ex.getClass().getName());
					Platform.runLater(() -> {
						try{
							if (ex.getMessage().contains("sleep interrupted")) {
								append("\n" + currentFile + " Interupted\n");
							} else{
								throw new RuntimeException(ex);
							}
						}catch(Exception e){
							StringWriter sw = new StringWriter();
							PrintWriter pw = new PrintWriter(sw);
							ex.printStackTrace(pw);
							append("\n" + currentFile + " \n" + sw + "\n");
						}

						reset();
					});
					for (IScriptEventListener l : listeners) {
						l.onScriptError(ex,currentFile);
					}
					throw new RuntimeException(ex);
				}

			}
		};

		try {
			if (loadGist)
				loadCodeFromGist(addr, engine);

			scriptRunner.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void append(String s) {
		System.out.println(s);
	}

	public String getCode() {
		return codeText;
	}

	public void setCode(String string) {
		String pervious = codeText;
		codeText = string;
		// System.out.println(codeText);
		for (IScriptEventListener l : listeners) {
			l.onScriptChanged(pervious, string,currentFile);
		}
	}

	public String getFileName() {
		if(currentFile!=null)
			return currentFile.getName();
		else
			return "Web";
	}



	@Override
	public void changed(ObservableValue observable, Object oldValue,
			Object newValue) {
		loadGitLocal(currentGit, (String)newValue);
	}





}
