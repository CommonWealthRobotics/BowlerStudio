package com.neuronrobotics.bowlerstudio.scripting;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.ConnectionManager;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.imageprovider.OpenCVImageProvider;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.util.ThreadUtil;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHGistFile;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings({ "unused", "restriction" })
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
				currentFile = ScriptingEngine.fileFromGit(id,file);
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
			ArrayList<String> tmp = ScriptingEngine.filesInGit(currentGit);
			fileList=new ArrayList<>();
			for(String s:tmp){
				if(!s.contains("csgDatabase.json"))// filter out configuration files from the list
					fileList.add(s);
			}
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
				catch (Throwable ex) {
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
//			if (loadGist)
//				loadCodeFromGist(addr, engine);

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
	public void changed(@SuppressWarnings("rawtypes") ObservableValue observable, Object oldValue,
			Object newValue) {
		loadGitLocal(currentGit, (String)newValue);
	}





}
