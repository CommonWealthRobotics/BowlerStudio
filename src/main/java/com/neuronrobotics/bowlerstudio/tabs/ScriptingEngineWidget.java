package com.neuronrobotics.bowlerstudio.tabs;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHGistFile;
import org.kohsuke.github.GitHub;

import com.neuronrobotics.bowlerstudio.PluginManager;
import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.nrconsole.util.GroovyFilter;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.util.FileChangeWatcher;
import com.neuronrobotics.sdk.util.IFileChangeListener;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class ScriptingEngineWidget extends BorderPane implements IFileChangeListener{
	



	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private File currentFile = null;
	
	private boolean running = false;
	private Thread scriptRunner=null;
	private FileChangeWatcher watcher;
	private BowlerAbstractDevice dyio;
	private Dimension codeDimentions = new Dimension(1168, 768);
	Label fileLabel = new Label();

	private String codeText="println(dyio)\n"
			+ "while(true){\n"
			+ "\tThreadUtil.wait(100)                     // Spcae out the loop\n\n"
			+ "\tlong start = System.currentTimeMillis()  //capture the starting value \n\n"
			+ "\tint value = dyio.getValue(15)            //grab the value of pin 15\n"
			+ "\tint scaled = value/4                     //scale the analog voltage to match the range of the servos\n"
			+ "\tdyio.setValue(0,scaled)                  // set the new value to the servo\n\n"
			+ "\t//Print out this loops values\n"
			+ "\tprint(\" Loop took = \"+(System.currentTimeMillis()-start))\n"
			+ "\tprint(\"ms Value= \"+value)\n"
			+ "\tprintln(\" Scaled= \"+scaled)\n"
			+ "}";
	
	private ArrayList<IScriptEventListener> listeners = new ArrayList<IScriptEventListener>();

	private Button runfx= new Button("Run");;
	private Button runsave= new Button("Save");;
	private WebEngine engine;

	private String addr;
	boolean loadGist=false;
	
	public ScriptingEngineWidget(BowlerAbstractDevice dyio,  File currentFile,String currentGist, WebEngine engine ) throws IOException, InterruptedException{
		this(dyio);
		this.currentFile = currentFile;
		loadCodeFromGist(currentGist,engine);
	}
	public ScriptingEngineWidget(BowlerAbstractDevice dyio, File currentFile) throws IOException{
		this(dyio);
		this.currentFile = currentFile;
		loadCodeFromFile(currentFile);
	}
		
	private ScriptingEngineWidget(BowlerAbstractDevice dyio){
		this.dyio = dyio;
		runfx.setOnAction(e -> {
			if(running)
				stop();
			else
				start();
		});
		runsave.setOnAction(e -> {
			updateFile();
			save();
		});



	    //String ctrlSave = "CTRL Save";
	    fileLabel.setOnMouseEntered(e->{
	    	Platform.runLater(() -> {
				ThreadUtil.wait(10);
				fileLabel.setText(currentFile.getAbsolutePath());
			});
	    });

	    fileLabel.setOnMouseExited(e->{
	    	Platform.runLater(() -> {
				ThreadUtil.wait(10);
				fileLabel.setText(currentFile.getName());
			});
	    });
	    fileLabel.setTextFill(Color.GREEN);
	    
	    //Set up the run controls and the code area
		// The BorderPane has the same areas laid out as the
		// BorderLayout layout manager
		setPadding(new Insets(20, 0, 20, 20));
		final FlowPane controlPane = new FlowPane();
		controlPane.setHgap(100);
		controlPane.getChildren().add(runfx);
		controlPane.getChildren().add(runsave);
		controlPane.getChildren().add(fileLabel);
		// put the flowpane in the top area of the BorderPane
		setTop(controlPane);
	}
	
	private void reset(){
		running = false;
		Platform.runLater(() -> {
			runfx.setText("Run");
		});

	}
	
//	private String getHTMLFromGist(String gist){
//		return "<script src=\"https://gist.github.com/madhephaestus/"+gist+".js\"></script>";
//	}
	
	public void addIScriptEventListener(IScriptEventListener l){
		if(!listeners.contains(l))
			listeners.add(l);
	}
	
	public void removeIScriptEventListener(IScriptEventListener l){
		if(listeners.contains(l))
			listeners.remove(l);
	}

	private void stop() {
		// TODO Auto-generated method stub
		reset();
		while(scriptRunner.isAlive()){

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
	public void loadCodeFromFile(File currentFile) throws IOException{
		setUpFile(currentFile);
		setCode(new String(Files.readAllBytes(currentFile.toPath())));
	}
	public void loadCodeFromGist(String addr,WebEngine engine) throws IOException, InterruptedException{
		this.addr = addr;
		this.engine = engine;
		loadGist=true;
		String currentGist = getCurrentGist(addr,engine);
		GitHub github = GitHub.connectAnonymously();
		Log.debug("Loading Gist: "+currentGist);
		try{
			GHGist gist = github.getGist(currentGist);
			Map<String, GHGistFile> files = gist.getFiles();
			for (Entry<String, GHGistFile> entry : files.entrySet()) { 
				if(entry.getKey().endsWith(".java") || entry.getKey().endsWith(".groovy")){
					GHGistFile ghfile = entry.getValue();	
					Log.debug("Key = " + entry.getKey());
					//Platform.runLater(() -> {
						setCode(ghfile.getContent());
						String fileName = entry.getKey().toString();
	            		fileLabel.setText(fileName);
	            		currentFile = new File(fileName);
	        		//});
					break;
				}
			}
		}catch (InterruptedIOException e){
			System.out.println("Gist Rate limited");
		}catch(MalformedURLException ex){
			//ex.printStackTrace();
		}
		
	}
	
	    public String urlToGist(String in) {
			String domain = in.split("//")[1];
			String [] tokens = domain.split("/");
			if (tokens[0].toLowerCase().contains("gist.github.com") && tokens.length>=2){
				String id = tokens[2].split("#")[0];

				Log.debug("Gist URL Detected "+id);
				return id;
			}
			
			return null;
		}
	    private String returnFirstGist(String html){
	    	//Log.debug(html);
	    	String slug = html.split("//gist.github.com/")[1];
	    	String js=		slug.split(".js")[0];
	    	String id  = js.split("/")[1];
	    	
	    	return id;
	    }

		public String getCurrentGist(String addr,WebEngine engine) {
			String gist = urlToGist(addr);
			if (gist==null){
				try {
					Log.debug("Non Gist URL Detected");
					String html;
					TransformerFactory tf = TransformerFactory.newInstance();
					Transformer t = tf.newTransformer();
					StringWriter sw = new StringWriter();
					t.transform(new DOMSource(engine.getDocument()), new StreamResult(sw));
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
		runfx.setText("Stop");
		scriptRunner = new Thread(){
			public void run() {
				setName("Bowler Script Runner "+currentFile.getName());
				
				//try{
					CompilerConfiguration cc = new CompilerConfiguration();
	
		            cc.addCompilationCustomizers(
		                    new ImportCustomizer().
		                    addStarImports("com.neuronrobotics",
		                    		"com.neuronrobotics.sdk.dyio.peripherals",
		                    		"com.neuronrobotics.sdk.dyio",
		                    		"com.neuronrobotics.sdk.common",
		                    		"com.neuronrobotics.sdk.ui",
		                    		"com.neuronrobotics.sdk.util"
		                    		).addStaticStars("com.neuronrobotics.sdk.util.ThreadUtil")
		                    );
		        	
		            Binding binding = new Binding();

	
		            binding.setVariable("dyio", dyio);
		            GroovyShell shell = new GroovyShell(getClass().getClassLoader(),
		            		binding, cc);
		            System.out.println(getCode()+"\n\nStart\n\n");
		            Script script = shell.parse(getCode());
		            try{
			            Object obj = script.run();
			            for(IScriptEventListener l:listeners){
			            	l.onGroovyScriptFinished(shell, script, obj);
			            }
			            Platform.runLater(() -> {
		            		append("\n"+currentFile+" Completed\n");
		        		});
			            reset();
			            
		            }catch(Exception ex){
		            	Platform.runLater(() -> {
		            		if(!ex.getMessage().contains("sleep interrupted")){
				            	StringWriter sw = new StringWriter();
				            	PrintWriter pw = new PrintWriter(sw);
				            	ex.printStackTrace(pw);
			        			append("\n"+currentFile+" \n"+sw+"\n");
			        			
		            		}else{
		            			append("\n"+currentFile+" Interupted\n");
		            		}
		            		
		            		reset();
		        		});
		            	for(IScriptEventListener l:listeners){
			            	l.onGroovyScriptError(shell, script, ex);
			            }
		            	throw new RuntimeException(ex);
		            }
          
				
			}
		};
		Platform.runLater(() -> {
			try {
				if(loadGist)
					loadCodeFromGist( addr, engine);
				else
					save();
				scriptRunner.start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

	}
	
	private void append(String s){
		System.out.println(s);
	}

	private void setUpFile(File f){
		currentFile = f;
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
	
	private void updateFile(){
		 File last=FileSelectionFactory.GetFile(currentFile, new GroovyFilter());
		 if(last != null){
			 setUpFile(last);
		 }
		
	}

	public void open() {
		
		updateFile();
		try {
			setCode(new String(Files.readAllBytes(currentFile.toPath())));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}

	public void save() {
		// TODO Auto-generated method stub
		try
		{
		    BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile));
		    writer.write (getCode());
		    writer.close();
		} catch(Exception ex)
		{
		   //ex.printStackTrace();
		}
	}

	@Override
	public void onFileChange(File fileThatChanged, @SuppressWarnings("rawtypes") WatchEvent event) {
		// TODO Auto-generated method stub
		if(fileThatChanged.getAbsolutePath().contains(currentFile.getAbsolutePath())){
			System.out.println("Code in "+fileThatChanged.getAbsolutePath()+" changed");
			Platform.runLater(new Runnable() {
	            @Override
	            public void run() {
	            	try {
						setCode(new String(Files.readAllBytes(Paths.get(fileThatChanged.getAbsolutePath())), "UTF-8"));
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

		}else{
			//System.out.println("Othr Code in "+fileThatChanged.getAbsolutePath()+" changed");
		}
	}
	
	public String getCode(){
		return codeText;
	}

	public void setCode(String string) {
		String pervious = codeText;
		codeText=string;
		//System.out.println(codeText);
        for(IScriptEventListener l:listeners){
        	l.onGroovyScriptChanged(pervious, string);
        } 
	}


	public String getFileName() {
		return currentFile.getName();
	}


}
