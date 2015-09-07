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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.time.Duration;
import java.util.ArrayList;
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
import com.neuronrobotics.imageprovider.AbstractImageProvider;
import com.neuronrobotics.imageprovider.OpenCVImageProvider;
import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.nrconsole.util.GroovyFilter;
import com.neuronrobotics.replicator.driver.BowlerBoardDevice;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.pid.GenericPIDDevice;
import com.neuronrobotics.sdk.util.FileChangeWatcher;
import com.neuronrobotics.sdk.util.IFileChangeListener;
import com.neuronrobotics.sdk.util.ThreadUtil;
import com.neuronrobotics.sdk.addons.kinematics.xml.*;

import eu.mihosoft.vrl.v3d.*;
import eu.mihosoft.vrl.v3d.samples.*;

@SuppressWarnings("unused")
public class ScriptingEngineWidget extends ScriptingEngine implements
		IFileChangeListener, ChangeListener {

	private boolean running = false;
	private Thread scriptRunner = null;
	private FileChangeWatcher watcher;
	private Dimension codeDimentions = new Dimension(1168, 768);
	//Label fileLabel = new Label();
	private Object scriptResult;
	private String codeText="";

	private ArrayList<IScriptEventListener> listeners = new ArrayList<IScriptEventListener>();
	private ArrayList<String> history = new ArrayList<>();
	private int historyIndex=0;

	private Button runfx = new Button("Run");;
	private Button runsave = new Button("Save");
	private Button runsaveAs = new Button("Save As..");
	private TextField cmdLineInterface = new TextField ();
	private WebEngine engine;

	private String addr;
	boolean loadGist = false;

	private ScriptingWidgetType type;
	
	final ComboBox fileListBox = new ComboBox();

	public ScriptingEngineWidget(File currentFile, String currentGist,
			WebEngine engine) throws IOException, InterruptedException {
		this(ScriptingWidgetType.GIST);
		this.currentFile = currentFile;
		loadCodeFromGist(currentGist, engine);
	}

	public ScriptingEngineWidget(File currentFile) throws IOException {
		this(ScriptingWidgetType.FILE);
		this.currentFile = currentFile;
		loadCodeFromFile(currentFile);
	}
	
	private void startStopAction(){
		runfx.setDisable(true);
		if (running)
			stop();
		else
			start();
		runfx.setDisable(false);
	}

	public ScriptingEngineWidget(ScriptingWidgetType type) {
		this.type = type;

		runfx.setOnAction(e -> {
	    	new Thread(){
	    		public void run(){

	    			startStopAction();
	    		}
	    	}.start();
		});
		runsave.setOnAction(e -> {
	    	new Thread(){
	    		public void run(){
	    			save();
	    		}
	    	}.start();

		});
		
		runsaveAs.setOnAction(e -> {
	    	new Thread(){
	    		public void run(){
	    			updateFile();
	    			save();
	    		}
	    	}.start();

		});

		// String ctrlSave = "CTRL Save";
//		fileLabel.setOnMouseEntered(e -> {
//			Platform.runLater(() -> {
//				ThreadUtil.wait(10);
//				fileLabel.setText(currentFile.getAbsolutePath());
//			});
//		});
//
//		fileLabel.setOnMouseExited(e -> {
//			Platform.runLater(() -> {
//				ThreadUtil.wait(10);
//				fileLabel.setText(currentFile.getName());
//			});
//		});

		cmdLineInterface.setOnAction(event -> {
			String text = cmdLineInterface.getText();
			text+="\r\n";
			Platform.runLater(() -> {
				cmdLineInterface.setText("");
			});
			System.out.println(text);
			history.add(text);
			historyIndex=0;
			setCode(text);
			startStopAction();
		});
		cmdLineInterface.setPrefWidth(80*4);
		cmdLineInterface.addEventFilter( KeyEvent.KEY_PRESSED, event -> {
			//Platform.runLater(() -> {
			    if( (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN) ) {
			    	System.err.println("Key pressed "+event.getCode()+" history index = "+historyIndex+" history size= "+history.size());
			    	if(historyIndex==0){
					       String text = cmdLineInterface.getText();
					       if(text.length()>0){
					    	   // store what was in the box into he history
					    	   history.add(text);
					       }
			    	}
			       
			       if(event.getCode() == KeyCode.UP)
			    	   historyIndex++;
			       else
			    	   historyIndex--;
			       if(history.size()>0){
				       if(historyIndex>history.size()){
				    	   historyIndex =  history.size();
				       }
				       if(historyIndex<0)
				    	   historyIndex=0;
				       //History index established
				       if(historyIndex>0)
					       Platform.runLater(() -> {
								cmdLineInterface.setText(history.get(history.size()-historyIndex));
					       });
				       else
				    	   Platform.runLater(() -> {
								cmdLineInterface.setText("");
					       }); 
			       }
			       event.consume();
			    } 
			//});
		});
		history.add("println dyio");
		history.add("dyio.setValue(0,1)//sets the value of channel 0 to 1");
		history.add("dyio.setValue(0,0)//sets the value of channel 0 to 0");
		history.add("dyio.setValue(0,dyio.getValue(1))//sets the value of channel 0 to the value of channel 1");
		history.add("println dyio");
		history.add("ThreadUtil.wait(10000)");
		history.add("BowlerStudio.speak(\"I can speak!\")");
		history.add("println 'Hello World Command line'");
		// Set up the run controls and the code area
		// The BorderPane has the same areas laid out as the
		// BorderLayout layout manager
		setPadding(new Insets(1, 0, 3, 10));
		
		
		
		if(type ==ScriptingWidgetType.CMDLINE ){
			controlPane = new HBox(10);
			controlPane.getChildren().add(new Label("Bowler CMD:"));
			controlPane.getChildren().add(cmdLineInterface);
		}else{
			controlPane = new HBox(20);
		}
		controlPane.getChildren().add(runfx);
		if(type !=ScriptingWidgetType.CMDLINE ){
			controlPane.getChildren().add(runsave);
			controlPane.getChildren().add(runsaveAs);
			controlPane.getChildren().add(fileListBox);
		}
		
		// put the flowpane in the top area of the BorderPane
		setTop(controlPane);

		addIScriptEventListener(BowlerStudioController.getBowlerStudio());
		reset();
	}

	private void reset() {
		running = false;
		Platform.runLater(() -> {
			if(type ==ScriptingWidgetType.CMDLINE ){
				runfx.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
				runfx.setText("Go");
			}else{
				runfx.setText("Run");
				runfx.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
			}
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

	public void loadCodeFromFile(File currentFile) throws IOException {
		if (!currentFile.exists()) {
			currentFile.createNewFile();
		}
		setUpFile(currentFile);
		setCode(new String(Files.readAllBytes(currentFile.toPath())));
	}
	
	private void loadGistLocal(String id, String file){
		//System.out.println("Loading "+file+" from "+id);
		String[] code;
		try {
			code = codeFromGistID(id,file);
			if (code != null) {
				setCode(code[0]);
				
				
				currentFile = new File(code[2]);
			}
		} catch (Exception e) {
			  StringWriter sw = new StringWriter();
		      PrintWriter pw = new PrintWriter(sw);
		      e.printStackTrace(pw);
		      System.out.println(sw.toString());
		}
	}

	@SuppressWarnings("unchecked")
	public void loadCodeFromGist(String addr, WebEngine engine)
			throws IOException, InterruptedException {
		this.addr = addr;
		this.engine = engine;
		loadGist = true;
		currentGist = getCurrentGist(addr, engine);
		
		ArrayList<String> fileList = ScriptingEngineWidget.filesInGist(currentGist);
		
		if(fileList.size()==1)
			loadGistLocal(currentGist, fileList.get(0));
		Platform.runLater(()->{
			fileListBox.getItems().clear();
			for(String s:fileList){
				fileListBox.getItems().add(s);
			}
			fileListBox.setValue(fileList.get(0));
			fileListBox.valueProperty().addListener(this);
		});
	}

	public static String urlToGist(String in) {
		String domain = in.split("//")[1];
		String[] tokens = domain.split("/");
		if (tokens[0].toLowerCase().contains("gist.github.com")
				&& tokens.length >= 2) {
			try{
				String id = tokens[2].split("#")[0];
				Log.debug("Gist URL Detected " + id);
				return id;
			}catch(ArrayIndexOutOfBoundsException e){
				return "d4312a0787456ec27a2a";
			}
		}

		return null;
	}

	private String returnFirstGist(String html) {
		// Log.debug(html);
		String slug = html.split("//gist.github.com/")[1];
		String js = slug.split(".js")[0];
		String id = js.split("/")[1];

		return id;
	}

	public String getCurrentGist(String addr, WebEngine engine) {
		String gist = urlToGist(addr);
		if (gist == null) {
			try {
				Log.debug("Non Gist URL Detected");
				String html;
				TransformerFactory tf = TransformerFactory.newInstance();
				Transformer t = tf.newTransformer();
				StringWriter sw = new StringWriter();
				t.transform(new DOMSource(engine.getDocument()),
						new StreamResult(sw));
				html = sw.getBuffer().toString();
				return returnFirstGist(html);
			} catch (TransformerConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return gist;
	}

	private void start() {

		running = true;
		Platform.runLater(()->{
			if(type ==ScriptingWidgetType.CMDLINE )
				runfx.setText("Kill");
			else
				runfx.setText("Stop");
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
				if(type!= ScriptingWidgetType.CMDLINE)
					setName("Bowler Script Runner " + name);
	
				try {
					Object obj = inlineScriptRun(getCode(), null,setFilename(name));
					for (IScriptEventListener l : listeners) {
						l.onGroovyScriptFinished(obj, scriptResult);
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
						l.onGroovyScriptError(ex);
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

	private void setUpFile(File f) {
		currentFile = f;
		setLastFile(f);
		Platform.runLater(() -> {
			fileListBox.valueProperty().removeListener(this);
			fileListBox.getItems().clear();
			fileListBox.getItems().add(f.getName());
			fileListBox.setValue(f.getName());
		});
		if (watcher != null) {
			watcher.close();
		}
		 try {
		 watcher = new FileChangeWatcher(currentFile);
		 watcher.addIFileChangeListener(this);
		 watcher.start();
		 } catch (IOException e) {
		 // TODO Auto-generated catch block
		 e.printStackTrace();
		 }
	}

	private void updateFile() {
		
		File last = FileSelectionFactory.GetFile(currentFile==null?getWorkspace():new File(getWorkspace().getAbsolutePath()+"/"+currentFile.getName()),
				new ExtensionFilter("Save Script","*"));
		if (last != null) {
			setUpFile(last);
		}

	}



	public void open() {

		updateFile();
		try {
			setCode(new String(Files.readAllBytes(currentFile.toPath())));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
	}

	public void save() {
		// TODO Auto-generated method stub
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					currentFile));
			writer.write(getCode());
			writer.close();
		} catch (Exception ex) {
			// ex.printStackTrace();
		}
	}

	@Override
	public void onFileChange(File fileThatChanged,
			@SuppressWarnings("rawtypes") WatchEvent event) {
		// TODO Auto-generated method stub
		if (fileThatChanged.getAbsolutePath().contains(
				currentFile.getAbsolutePath())) {
			System.out.println("Code in " + fileThatChanged.getAbsolutePath()
					+ " changed");
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					try {
						setCode(new String(Files.readAllBytes(Paths
								.get(fileThatChanged.getAbsolutePath())),
								"UTF-8"));
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

		} else {
			// System.out.println("Othr Code in "+fileThatChanged.getAbsolutePath()+" changed");
		}
	}

	public String getCode() {
		return codeText;
	}

	public void setCode(String string) {
		String pervious = codeText;
		codeText = string;
		// System.out.println(codeText);
		for (IScriptEventListener l : listeners) {
			l.onGroovyScriptChanged(pervious, string);
		}
	}

	public String getFileName() {
		if(currentFile!=null)
			return currentFile.getName();
		else
			return "Web";
	}



	private HBox controlPane;
	private String currentGist;

	@Override
	public void changed(ObservableValue observable, Object oldValue,
			Object newValue) {
		loadGistLocal(currentGist, (String)newValue);
	}





}
