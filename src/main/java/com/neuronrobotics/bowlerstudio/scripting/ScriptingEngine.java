package com.neuronrobotics.bowlerstudio.scripting;

import eu.mihosoft.vrl.v3d.CSG;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Map.Entry;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHGistFile;
import org.kohsuke.github.GitHub;
import org.python.util.PythonInterpreter;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.ConnectionManager;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.DeviceManager;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.util.ThreadUtil;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;


public class ScriptingEngine extends BorderPane{// this subclasses boarder pane for the widgets sake, because multiple inheritance is TOO hard for java...
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public enum ShellType {
		GROOVY, JYTHON
	}

	static ShellType activeType = ShellType.GROOVY;

	
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
			"javafx.scene.text", "javafx.scene",
			"com.neuronrobotics.sdk.addons.kinematics",
			"com.neuronrobotics.sdk.addons.kinematics.math", "java.util",
			"com.neuronrobotics.sdk.addons.kinematics.gui",
			"javafx.scene.transform", "javafx.scene.shape",
			"java.awt.image.BufferedImage" };

	private static GitHub github;



	private static File creds=null;

	private static GHGist gist;
	protected File currentFile = null;
	
	private static File workspace;
	private static File lastFile;
	private static String loginID=null;
	private static ArrayList<IGithubLoginListener> loginListeners = new ArrayList<IGithubLoginListener>();


	private static String pw;
	
 	static{
 		File scriptingDir = new File(System.getProperty("user.home")+"/git/BowlerStudio/src/main/resources/com/neuronrobotics/bowlerstudio/");
		workspace = new File(System.getProperty("user.home")+"/bowler-workspace/");
		if(!workspace.exists()){
			workspace.mkdir();
		}
		if(scriptingDir.exists()){
			workspace=scriptingDir;
		}
		if(loginID == null && getCreds().exists()){
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
	}
 	
 	public static void addIGithubLoginListener(IGithubLoginListener l){
 		if(!loginListeners.contains(l)){
 			loginListeners.add(l);
 		}
 	}
 	public static void removeIGithubLoginListener(IGithubLoginListener l){
 		if(loginListeners.contains(l)){
 			loginListeners.remove(l);
 		}
 	}
 	
	public static File getWorkspace() {
		System.err.println("Workspace: "+workspace.getAbsolutePath());
		return workspace;
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
		

		return loginID;
	}
	
	public static void login() throws IOException{
		loginID=null;

		Platform.runLater(() -> {
			GithubLoginDialog myDialog = new GithubLoginDialog(BowlerStudio.getPrimaryStage());
			do{
		        myDialog.sizeToScene();
		        myDialog.showAndWait();
		        loginID = myDialog.getUsername();
		        pw=myDialog.getPw();
			}while(loginID==null);
		});
        while(loginID==null)ThreadUtil.wait(100);
        
        String content= "login="+loginID+"\n";
        content+= "password="+pw+"\n";
        pw=null;
        PrintWriter out = new PrintWriter(getCreds().getAbsoluteFile());
        out.println(content);
        out.flush();
        out.close();
        github = GitHub.connect();
        for(IGithubLoginListener l:loginListeners){
        	l.onLogin(loginID);
        }
	}

	public static void logout(){
		new RuntimeException("Logout callsed").printStackTrace();
		if(getCreds()!= null)
		try {
			Files.delete(getCreds().toPath());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		github=null;
        for(IGithubLoginListener l:loginListeners){
        	l.onLogout(loginID);
        }
        loginID=null;
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
					Platform.runLater(() -> {
						Alert alert = new Alert(AlertType.CONFIRMATION);
						alert.setTitle("GitHub Login Missing");
						alert.setHeaderText("To use BowlerStudio at full speed login with github");
						alert.setContentText("What would you like to do?");
		
						ButtonType buttonTypeOne = new ButtonType("Use Anonymously");
						ButtonType buttonTypeTwo = new ButtonType("Login");
						
						alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo);
						Optional<ButtonType> result = alert.showAndWait();
						new Thread(){
							public void run(){
								if (result.get() == buttonTypeOne){
									try {
										github = GitHub.connectAnonymously();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								} else  {
									logout();
									try {
										login();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								} 
							}
						}.start();

					});

				}
				
			}
			while(github==null){
				ThreadUtil.wait(100);
			}
			
			Log.debug("Loading Gist: " + id);
			try{
				gist = github.getGist(id);
			}catch(IOException ex){
				//ex.printStackTrace();
				
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
	
	public static String getText(URL website) throws Exception {

        URLConnection connection = website.openConnection();
        BufferedReader in = new BufferedReader(
                                new InputStreamReader(
                                    connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) 
            response.append(inputLine+"\n");

        in.close();

        return response.toString();
    }

	
	public static Object inlineUrlScriptRun(URL gistID,
			ArrayList<Object> args) throws Exception {
		
		return inlineScriptRun(getText(gistID), args);
	}
	
	public static File getLastFile() {
		if(lastFile==null)
			return getWorkspace();
		return lastFile;
	}

	public static void setLastFile(File lastFile) {
		ScriptingEngine.lastFile = lastFile;
	}

	public static File getCreds() {
		if(creds == null)
			setCreds(new File(System.getProperty("user.home")+"/.github"));
		return creds;
	}

	public static void setCreds(File creds) {
		ScriptingEngine.creds = creds;
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
	private static Object runGroovy(String code, ArrayList<Object> args) {
		CompilerConfiguration cc = new CompilerConfiguration();
		cc.addCompilationCustomizers(new ImportCustomizer()
				.addStarImports(imports)
				.addStaticStars(
						"com.neuronrobotics.sdk.util.ThreadUtil",
						"com.neuronrobotics.bowlerstudio.scripting.ScriptingEngineWidget",
						"eu.mihosoft.vrl.v3d.Transform"));

		Binding binding = new Binding();
		for (String pm : DeviceManager.listConnectedDevice(null)) {
			BowlerAbstractDevice bad = DeviceManager.getSpecificDevice(null, pm);
			try {
				// groovy needs the objects cas to thier actual type befor
				// passing into the scipt
				
				binding.setVariable(bad.getScriptingName(),
						Class.forName(bad.getClass().getName())
								.cast(bad));
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.err.println("Device " + bad.getScriptingName() + " is "
					+ bad);
		}
		binding.setVariable("args", args);

		GroovyShell shell = new GroovyShell(ConnectionManager.class
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

		for (String pm : DeviceManager.listConnectedDevice(null)) {
			BowlerAbstractDevice bad = DeviceManager.getSpecificDevice(null, pm);
				// passing into the scipt
			try{
				interp.set(bad.getScriptingName(),
						Class.forName(bad.getClass().getName())
								.cast(bad));
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.err.println("Device " + bad.getScriptingName() + " is "
					+ bad);
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



}
