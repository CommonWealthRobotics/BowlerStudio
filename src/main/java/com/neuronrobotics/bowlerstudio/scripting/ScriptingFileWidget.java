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
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
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
import javafx.util.Pair;
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
import org.eclipse.jgit.api.Git;
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
import com.neuronrobotics.nrconsole.util.CommitWidget;
import com.neuronrobotics.nrconsole.util.FileChangeWatcher;
import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.nrconsole.util.FileWatchDeviceWrapper;
import com.neuronrobotics.nrconsole.util.GroovyFilter;
import com.neuronrobotics.replicator.driver.BowlerBoardDevice;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.pid.GenericPIDDevice;
import com.neuronrobotics.sdk.util.IFileChangeListener;
import com.neuronrobotics.sdk.util.ThreadUtil;
import com.neuronrobotics.sdk.addons.kinematics.xml.*;



@SuppressWarnings("unused")
public class ScriptingFileWidget extends BorderPane implements
		IFileChangeListener {

	private boolean running = false;
	private Thread scriptRunner = null;
	private FileChangeWatcher watcher;
	private Dimension codeDimentions = new Dimension(1168, 768);
	//Label fileLabel = new Label();
	private Object scriptResult;
	private String codeText="";

	private ArrayList<IScriptEventListener> listeners = new ArrayList<>();

	private Button runfx = new Button("Run");
	private Button publish = new Button("Publish");

	private String addr;
	boolean loadGist = false;

	private ScriptingWidgetType type;
	
	final TextField fileListBox = new TextField();
	final TextField fileNameBox = new TextField();
	private File currentFile = null;

	private HBox controlPane;
	private String currentGist;
	private boolean updateneeded = false;
	private IScriptingLanguage langaugeType;
	private ImageView image=new ImageView();

	public ScriptingFileWidget(File currentFile) throws IOException {
		this(ScriptingWidgetType.FILE);
		this.currentFile = currentFile;
		loadCodeFromFile(currentFile);
		boolean isOwnedByLoggedInUser= ScriptingEngine.checkOwner(currentFile);
		publish.setDisable(!isOwnedByLoggedInUser);
		runfx.setGraphic(AssetFactory.loadIcon("Run.png"));
		publish.setGraphic(AssetFactory.loadIcon("Publish.png"));
		try {
			image.setImage(AssetFactory.loadAsset("Script-Tab-"+ScriptingEngine.getShellType(currentFile.getName())+".png"));
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

	private ScriptingFileWidget(ScriptingWidgetType type) {
		this.type = type;

		runfx.setOnAction(e -> {
	    	new Thread(){
	    		public void run(){
	    			
	    			if(langaugeType.getIsTextFile())
	    				save();
	    			//do not attempt to save no binary files
	    			startStopAction();
	    		}
	    	}.start();
		});
		
		publish.setOnAction(e -> {
			new Thread(()->{
				save();
				CommitWidget.commit(currentFile, getCode());
			}).start();

		});
		
		
//		runsaveAs.setOnAction(e -> {
//	    	new Thread(){
//	    		public void run(){
//	    			updateFile();
//	    			save();
//	    		}
//	    	}.start();
//
//		});

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


		// Set up the run controls and the code area
		// The BorderPane has the same areas laid out as the
		// BorderLayout layout manager
		setPadding(new Insets(1, 0, 3, 10));

		controlPane = new HBox(20);
		

		controlPane.getChildren().add(runfx);
		controlPane.getChildren().add(image);
		controlPane.getChildren().add(publish);
		controlPane.getChildren().add(new Label("file:"));
		controlPane.getChildren().add(fileNameBox);
		fileNameBox.setMaxWidth(Double.MAX_VALUE);
		controlPane.getChildren().add(new Label("git:"));
		controlPane.getChildren().add(fileListBox);
		fileListBox.setMaxWidth(Double.MAX_VALUE);
		controlPane.setMaxWidth(Double.MAX_VALUE);

		
		
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

	public void loadCodeFromFile(File currentFile) throws IOException {
		if (!currentFile.exists()) {
			currentFile.createNewFile();
		}
		setUpFile(currentFile);
		if(!langaugeType.getIsTextFile())
			setCode("Binary File");
		else
			setCode(new String(Files.readAllBytes(currentFile.toPath())));

	}
	



	private void start() {
		BowlerStudio.clearConsole();
		BowlerStudioController.clearHighlight();
		try {
			ScriptingEngine.setAutoupdate(false);
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
//				String name;
//				try{
//					name = currentFile.getName();
//				}catch (NullPointerException e){
//					name="";
//				}
				try {
					Object obj = ScriptingEngine.inlineFileScriptRun(currentFile, null);
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
						alert.setTitle("Variable missing error");
						String message = "This script needs a variable defined before you use it: ";
					
						String stackTrace = d.getMessage();
						
						if(stackTrace.contains("dyio"))
							message+="dyio";
						else if(stackTrace.contains("camera"))
							message+="camera";
						else if(stackTrace.contains("gamepad"))
							message+="gamepad";
						else
							message+=stackTrace;
						alert.setHeaderText(message);
						alert.showAndWait();
						if(stackTrace.contains("dyio"))
							ConnectionManager.addConnection();
						else if(stackTrace.contains("camera"))
							ConnectionManager.addConnection(new OpenCVImageProvider(0),"camera0");
						else if(stackTrace.contains("gamepad"))
							ConnectionManager.onConnectGamePad("gamepad");
						reset();
					});
					BowlerStudioController.highlightException(currentFile, d);
				}
				catch (Exception|Error ex) {
					System.err.println("Script exception of type= "+ex.getClass().getName());

					try{
						if (ex.getMessage().contains("sleep interrupted")) {
							append("\n" + currentFile + " Interupted\n");
						} else{
							BowlerStudioController.highlightException(currentFile, new Exception(ex));
						}
					}catch(Exception e){
						BowlerStudioController.highlightException(currentFile, new Exception(ex));
					}

					reset();
		
					for (IScriptEventListener l : listeners) {
						l.onScriptError(new Exception(ex),currentFile);
					}
				}

			}
		};

		try {

			scriptRunner.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void append(String s) {
		System.out.println(s);
	}
	
	public String getGitRepo(){
		return fileListBox.getText();
	}
	public String getGitFile(){
		return fileNameBox.getText();
	}
	private void setUpFile(File f) {
		currentFile = f;
		String langType = ScriptingEngine.getShellType(currentFile.getName());
		try {
			image.setImage(AssetFactory.loadAsset("Script-Tab-"+ScriptingEngine.getShellType(currentFile.getName())+".png"));
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		langaugeType = ScriptingEngine.getLangaugesMap().get(langType);
		//ScriptingEngine.setLastFile(f);
		Git git;
		try {
			git = ScriptingEngine.locateGit(currentFile);
			String remote= git.getRepository().getConfig().getString("remote", "origin", "url");
			Platform.runLater(() -> {
				//fileListBox.setMinWidth(remote.getBytes().length*10);
				fileListBox.setText(remote);
				//fileListBox.res
				fileNameBox.setText(ScriptingEngine.findLocalPath(f, git));
				// These values are display only, so if hte user tries to change them, they reset
				// the use of text field for static dats is so the user cna copy the vlaues and use them in their scritpts
				fileNameBox.textProperty().addListener((observable, oldValue, newValue) -> {
					fileNameBox.setText(ScriptingEngine.findLocalPath(f, git));
				});
				fileListBox.textProperty().addListener((observable, oldValue, newValue) -> {
					fileListBox.setText(remote);
				});
				
				git.close();
			});
		} catch (Exception e1) {
			Platform.runLater(() -> {
				fileListBox.setText("none");
				fileListBox.setMinWidth(40);
				fileNameBox.setText(f.getAbsolutePath());
				// These values are display only, so if hte user tries to change them, they reset
				// the use of text field for static dats is so the user cna copy the vlaues and use them in their scritpts
				fileNameBox.textProperty().addListener((observable, oldValue, newValue) -> {
					fileNameBox.setText(f.getAbsolutePath());
				});
				fileListBox.textProperty().addListener((observable, oldValue, newValue) -> {
					fileListBox.setText("none");
				});
				
			});
			e1.printStackTrace();
		}
		if(!langaugeType.getIsTextFile())
			return;
		 try {
			 watcher = FileChangeWatcher.watch(currentFile);
			 watcher.addIFileChangeListener(this);
		 } catch (IOException e) {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
		 }
		 
	}

	private void updateFile() {
		
		File last = FileSelectionFactory.GetFile(	currentFile==null?
													ScriptingEngine.getWorkspace():
													new File(ScriptingEngine.getWorkspace().getAbsolutePath()+"/"+currentFile.getName()),
													true,
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
			System.out.println("Writing file contents");
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
		if(updateneeded)
			return;
		updateneeded=true;
		watcher.removeIFileChangeListener(this);
		FxTimer.runLater(
				Duration.ofMillis(500) ,() -> {
					updateneeded=false;
					// TODO Auto-generated method stub
					if (fileThatChanged.getAbsolutePath().contains(
							currentFile.getAbsolutePath())) {
						System.out.println("Code in " + fileThatChanged.getAbsolutePath()
								+ " changed");
						Platform.runLater(() -> {
							try {
								String content = new String(Files.readAllBytes(Paths
										.get(fileThatChanged.getAbsolutePath())));
								if(content.length()>2)// ensures tha the file contents never get wiped out on the user
									setCode(content);
							} catch (UnsupportedEncodingException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (IOException e2) {
								// TODO Auto-generated catch block
								e2.printStackTrace();
							}
							watcher.addIFileChangeListener(this);
						});

					} else {
						// System.out.println("Othr Code in "+fileThatChanged.getAbsolutePath()+" changed");
					}
		});

		
	}

	public String getCode() {
		return codeText;
	}

	public void setCode(String string) {
		String pervious = codeText;
		codeText = string;
		// System.out.println(codeText);
		for (int i=0;i<listeners.size();i++ ) {
			listeners.get(i).onScriptChanged(pervious, string,currentFile);
		}
	}

	public String getFileName() {
		if(currentFile!=null)
			return currentFile.getName();
		else
			return "Web";
	}

}
