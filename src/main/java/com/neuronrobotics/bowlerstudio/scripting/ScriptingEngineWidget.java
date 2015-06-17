package com.neuronrobotics.bowlerstudio.scripting;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import org.python.util.PythonInterpreter;
import org.python.core.*;

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
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
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
import com.neuronrobotics.bowlerstudio.ConnectionManager;
import com.neuronrobotics.bowlerstudio.PluginManager;
import com.neuronrobotics.jniloader.AbstractImageProvider;
import com.neuronrobotics.jniloader.OpenCVImageProvider;
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
public class ScriptingEngineWidget extends BorderPane implements
		IFileChangeListener {

	public enum ShellType {
		GROOVY, JYTHON
	}

	static ShellType activeType = ShellType.GROOVY;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private File currentFile = null;
	
	private static File workspace;
	private static File lastFile;
	static{
		workspace = new File(System.getProperty("user.home")+"/bowler-workspace/");
		if(!workspace.exists()){
			workspace.mkdir();
		}
	}
	
	private boolean running = false;
	private Thread scriptRunner = null;
	private FileChangeWatcher watcher;
	private static ConnectionManager connectionmanager;
	private Dimension codeDimentions = new Dimension(1168, 768);
	Label fileLabel = new Label();
	private Object scriptResult;
	private String codeText = "println(dyio)\n"
			+ "while(true){\n"
			+ "\tThreadUtil.wait(100)                     // Spcae out the loop\n\n"
			+ "\tlong start = System.currentTimeMillis()  //capture the starting value \n\n"
			+ "\tint value = dyio.getValue(15)            //grab the value of pin 15\n"
			+ "\tint scaled = value/4                     //scale the analog voltage to match the range of the servos\n"
			+ "\tdyio.setValue(0,scaled)                  // set the new value to the servo\n\n"
			+ "\t//Print out this loops values\n"
			+ "\tprint(\" Loop took = \"+(System.currentTimeMillis()-start))\n"
			+ "\tprint(\"ms Value= \"+value)\n"
			+ "\tprintln(\" Scaled= \"+scaled)\n" + "}";

	private ArrayList<IScriptEventListener> listeners = new ArrayList<IScriptEventListener>();
	private ArrayList<String> history = new ArrayList<>();
	private int historyIndex=0;

	private Button runfx = new Button("Run");;
	private Button runsave = new Button("Save");
	private TextField cmdLineInterface = new TextField ();
	private WebEngine engine;

	private String addr;
	boolean loadGist = false;

	private ScriptingWidgetType type;

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
		if (getConnectionmanager() == null)
			throw new RuntimeException(
					"Connection manager needs to be added to the Scripting engine");
		runfx.setOnAction(e -> {
			startStopAction();
		});
		runsave.setOnAction(e -> {
			if(type!= ScriptingWidgetType.FILE)
				updateFile();
			save();
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
		fileLabel.setTextFill(Color.GREEN);
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
			Platform.runLater(() -> {
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
			});
		});
		history.add("println dyio");
		history.add("dyio.setValue(0,1)//sets the value of channel 0 to 1");
		history.add("dyio.setValue(0,0)//sets the value of channel 0 to 0");
		history.add("dyio.setValue(0,dyio.getValue(1))//sets the value of channel 0 to the value of channel 1");
		history.add("println dyio");
		history.add("ThreadUtil.wait(10000)");
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
			controlPane.getChildren().add(fileLabel);
		}
		
		// put the flowpane in the top area of the BorderPane
		setTop(controlPane);

		addIScriptEventListener(getConnectionmanager()
				.getBowlerStudioController());
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

	// private String getHTMLFromGist(String gist){
	// return
	// "<script src=\"https://gist.github.com/madhephaestus/"+gist+".js\"></script>";
	// }

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

	public void loadCodeFromGist(String addr, WebEngine engine)
			throws IOException, InterruptedException {
		this.addr = addr;
		this.engine = engine;
		loadGist = true;
		String currentGist = getCurrentGist(addr, engine);
		String[] code = codeFromGistID(currentGist,"");
		if (code != null) {
			setCode(code[0]);
			fileLabel.setText(code[1]);
			currentFile = new File(code[1]);
		}

	}

	public String urlToGist(String in) {
		String domain = in.split("//")[1];
		String[] tokens = domain.split("/");
		if (tokens[0].toLowerCase().contains("gist.github.com")
				&& tokens.length >= 2) {
			String id = tokens[2].split("#")[0];

			Log.debug("Gist URL Detected " + id);
			return id;
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
		if(type ==ScriptingWidgetType.CMDLINE )
			runfx.setText("Kill");
		else
			runfx.setText("Stop");
		runfx.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
		scriptRunner = new Thread() {

			public void run() {
				if(type!= ScriptingWidgetType.CMDLINE)
					setName("Bowler Script Runner " + currentFile.getName());

				try {
					Object obj = inlineScriptRun(getCode(), null);
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
							connectionmanager.addConnection();
						else if(stackTrace.contains("camera"))
							connectionmanager.addConnection(new OpenCVImageProvider(0),"camera0");
						else if(stackTrace.contains("gamepad"))
							connectionmanager.onConnectGamePad();
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
		Platform.runLater(() -> {
			try {
				if (loadGist)
					loadCodeFromGist(addr, engine);
				else
					save();
				scriptRunner.start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

	}

	private void append(String s) {
		System.out.println(s);
	}

	private void setUpFile(File f) {
		currentFile = f;
		setLastFile(f);
		Platform.runLater(() -> {
			fileLabel.setText(f.getName());
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
				new GroovyFilter());
		if (last != null) {
			setUpFile(last);
		}

	}

	public static File getWorkspace() {
		System.err.println("Workspace: "+workspace.getAbsolutePath());
		return workspace;
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
						fileLabel.setTextFill(Color.RED);
						Platform.runLater(() -> {
							ThreadUtil.wait(750);
							fileLabel.setTextFill(Color.GREEN);
						});
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
		return currentFile.getName();
	}

	public static ConnectionManager getConnectionmanager() {
	
		return connectionmanager;
	}

	public static void setConnectionmanager(ConnectionManager connectionmanager) {
		ScriptingEngineWidget.connectionmanager = connectionmanager;
	}

	private static void setFilename(String name) {
		if (name.toString().toLowerCase().endsWith(".java")
				|| name.toString().toLowerCase().endsWith(".groovy")) {
			activeType = ShellType.GROOVY;
			//System.out.println("Setting up Groovy Shell");
		}
		if (name.toString().toLowerCase().endsWith(".py")
				|| name.toString().toLowerCase().endsWith(".jy")) {
			activeType = ShellType.JYTHON;
			//System.out.println("Setting up Python Shell");
		}
	}
	
	public static String getLoginID(){
		
		if(loginID == null){
			try {
				String line;
				try (
				    InputStream fis = new FileInputStream(getCreds().getAbsolutePath());
				    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
				    BufferedReader br = new BufferedReader(isr);
				) {
				    while ((line = br.readLine()) != null) {
				        if(line.contains("login")){
				        	loginID = line.split("=")[1];
				        }
				    }
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return loginID;
	}
	
	public static void login() throws IOException{
		GithubLoginDialog myDialog = new GithubLoginDialog(BowlerStudio.getPrimaryStage());
        myDialog.sizeToScene();
        myDialog.showAndWait();
        loginID = myDialog.getUsername();
        String content= "login="+loginID+"\n";
        content+= "password="+myDialog.getPw()+"\n";
        PrintWriter out = new PrintWriter(getCreds().getAbsoluteFile());
        out.println(content);
        out.flush();
        out.close();
        github = GitHub.connect();
	}

	public static void logout(){
		if(getCreds()!= null)
		try {
			Files.delete(getCreds().toPath());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		github=null;
	}


	public static String[] codeFromGistID(String id, String FileName) {
		try {
			if(github == null){

				if (getCreds().exists()){
					try{
						github = GitHub.connect();
					}catch(IOException ex){
						
					}
				}else{
					getCreds().createNewFile();
				}
				
				if(github==null){
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setTitle("GitHub Login Missing");
					alert.setHeaderText("To use BowlerStudio at full speed login with github");
					alert.setContentText("What would you like to do?");
	
					ButtonType buttonTypeOne = new ButtonType("Use Anonymously");
					ButtonType buttonTypeTwo = new ButtonType("Login");
					
					alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo);
					
					Optional<ButtonType> result = alert.showAndWait();
					if (result.get() == buttonTypeOne){
						github = GitHub.connectAnonymously();
					} else  {
						logout();
						login();
					} 
				}
				
			}
			
			Log.debug("Loading Gist: " + id);
			try{
				gist = github.getGist(id);
			}catch(IOException ex){
				logout();
				
				return null;
			}
			Map<String, GHGistFile> files = gist.getFiles();
					
			
			for (Entry<String, GHGistFile> entry : files.entrySet()) {
				if (((entry.getKey().endsWith(".py")
						|| entry.getKey().endsWith(".jy")
						|| entry.getKey().endsWith(".java")
						|| entry.getKey().endsWith(".groovy"))&&(FileName.length()==0))
						||entry.getKey().contains(FileName)) {

					GHGistFile ghfile = entry.getValue();
					Log.debug("Key = " + entry.getKey());
					String code = ghfile.getContent();
					String fileName = entry.getKey().toString();
					setFilename(fileName);
					return new String[] { code, fileName };
				}
			}
		} catch (InterruptedIOException e) {
			System.out.println("Gist Rate limited");
		} catch (MalformedURLException ex) {
			// ex.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static Object inlineFileScriptRun(File f, ArrayList<Object> args) {
		byte[] bytes;
		setFilename(f.getName());
		try {
			bytes = Files.readAllBytes(f.toPath());
			String s = new String(bytes, "UTF-8");
			return inlineScriptRun(s, args);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static Object inlineGistScriptRun(String gistID,
			ArrayList<Object> args) {
		return inlineScriptRun(codeFromGistID(gistID,"")[0], args);
	}

	static final String[] imports = new String[] { "haar",
			"java.awt",
			"eu.mihosoft.vrl.v3d",
			"eu.mihosoft.vrl.v3d.samples",
			"com.neuronrobotics.sdk.addons.kinematics.xml",
			"com.neuronrobotics.sdk.dyio.peripherals",
			"com.neuronrobotics.sdk.dyio",
			"com.neuronrobotics.sdk.common",
			"com.neuronrobotics.sdk.ui",
			"com.neuronrobotics.sdk.util",
			"javafx.scene.control",
			"com.neuronrobotics.bowlerstudio.scripting",
			"com.neuronrobotics.jniloader",
			"com.neuronrobotics.bowlerstudio.tabs",
			"org.opencv.core",
			// "org.opencv.features2d",
			"javafx.scene.text", "javafx.scene",
			"com.neuronrobotics.sdk.addons.kinematics",
			"com.neuronrobotics.sdk.addons.kinematics.math", "java.util",
			"com.neuronrobotics.sdk.addons.kinematics.gui",
			"javafx.scene.transform", "javafx.scene.shape",
			"java.awt.image.BufferedImage" };

	private static GitHub github;

	private static String loginID=null;

	private static File creds=null;

	private static GHGist gist;

	private HBox controlPane;

	private static Object runGroovy(String code, ArrayList<Object> args) {
		CompilerConfiguration cc = new CompilerConfiguration();
		cc.addCompilationCustomizers(new ImportCustomizer()
				.addStarImports(imports)
				.addStaticStars(
						"com.neuronrobotics.sdk.util.ThreadUtil",
						"com.neuronrobotics.bowlerstudio.scripting.ScriptingEngineWidget",
						"eu.mihosoft.vrl.v3d.Transform"));

		Binding binding = new Binding();
		for (PluginManager pm : getConnectionmanager().getPlugins()) {
			try {
				// groovy needs the objects cas to thier actual type befor
				// passing into the scipt
				binding.setVariable(pm.getName(),
						Class.forName(pm.getDevice().getClass().getName())
								.cast(pm.getDevice()));
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.err.println("Device " + pm.getName() + " is "
					+ pm.getDevice());
		}
		binding.setVariable("args", args);

		GroovyShell shell = new GroovyShell(connectionmanager.getClass()
				.getClassLoader(), binding, cc);
		System.out.println(code + "\n\nStart\n\n");
		Script script = shell.parse(code);

		return script.run();
	}

	private static Object runJython(String code, ArrayList<Object> args) {

		Properties props = new Properties();
		PythonInterpreter.initialize(System.getProperties(), props,
				new String[] { "" });
		PythonInterpreter interp = new PythonInterpreter();

		interp.exec("import sys");
		for (String s : imports) {

			// s = "import "+s;
			System.err.println(s);
			if(!s.contains("mihosoft")&&
					!s.contains("haar")&&
					!s.contains("com.neuronrobotics.sdk.addons.kinematics")
					) {
				interp.exec("import "+s);
			} else {
				//from http://stevegilham.blogspot.com/2007/03/standalone-jython-importerror-no-module.html
				try {
					String[] names = s.split("\\.");
					String packname = (names.length>0?names[names.length-1]:s);
					Log.error("Forcing "+s+" as "+packname);
					interp.exec("sys.packageManager.makeJavaPackage(" + s
							+ ", " +packname + ", None)");

					interp.exec("import "+packname);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		for (PluginManager pm : getConnectionmanager().getPlugins()) {
			try {
				// passing into the scipt
				interp.set(pm.getName(),
						Class.forName(pm.getDevice().getClass().getName())
								.cast(pm.getDevice()));
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.err.println("Device " + pm.getName() + " is "
					+ pm.getDevice());
		}
		interp.set("args", args);

		interp.exec(code);
		ArrayList<Object> results = new ArrayList<>();
		try{
			results.add(interp.get("csg",CSG.class));
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			results.add(interp.get("tab",Tab.class));
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			results.add(interp.get("device",BowlerAbstractDevice.class));
		}catch(Exception e){
			e.printStackTrace();
		}

		Log.debug("Jython return = "+results);
		return results;
	}

	public static Object inlineScriptRun(String code, ArrayList<Object> args) {
		switch (activeType) {
		case JYTHON:
			return runJython(code, args);
		case GROOVY:
		default:
			return runGroovy(code, args);
		}
	}

	public static File getLastFile() {
		if(lastFile==null)
			return getWorkspace();
		return lastFile;
	}

	public static void setLastFile(File lastFile) {
		ScriptingEngineWidget.lastFile = lastFile;
	}

	public static File getCreds() {
		if(creds == null)
			setCreds(new File(System.getProperty("user.home")+"/.github"));
		return creds;
	}

	public static void setCreds(File creds) {
		ScriptingEngineWidget.creds = creds;
	}

}
