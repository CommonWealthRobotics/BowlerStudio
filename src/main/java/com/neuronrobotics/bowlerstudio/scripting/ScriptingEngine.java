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
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHGistFile;
import org.kohsuke.github.GitHub;
import org.python.util.PythonInterpreter;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.ConnectionManager;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.DeviceManager;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.dyio.DyIOChannelMode;
import com.neuronrobotics.sdk.util.ThreadUtil;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import clojure.java.api.Clojure;
import clojure.lang.Symbol;
import clojure.lang.Var;
import clojure.lang.RT;


public class ScriptingEngine extends BorderPane{// this subclasses boarder pane for the widgets sake, because multiple inheritance is TOO hard for java...
	private static final int TIME_TO_WAIT_BETWEEN_GIT_PULL = 2000;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Map<String,Long> fileLastLoaded = new HashMap<String,Long>();

	private static boolean hasnetwork=false;
	
	private static final String[] imports = new String[] { "haar",
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
			"com.neuronrobotics.bowlerstudio",
			"com.neuronrobotics.imageprovider",
			"com.neuronrobotics.bowlerstudio.tabs",
			"javafx.scene.text", "javafx.scene",
			"com.neuronrobotics.sdk.addons.kinematics",
			"com.neuronrobotics.sdk.addons.kinematics.math", "java.util",
			"com.neuronrobotics.sdk.addons.kinematics.gui",
			"javafx.scene.transform", "javafx.scene.shape",
			"java.awt.image.BufferedImage",
			"com.neuronrobotics.bowlerstudio.vitamins.Vitamins"};

	private static GitHub github;

	private static File creds=null;

	//private static GHGist gist;
	protected File currentFile = null;
	
	private static File workspace;
	private static File lastFile;
	private static String loginID=null;
	private static String pw =null;
	private static CredentialsProvider cp;// = new UsernamePasswordCredentialsProvider(name, password);
	private static ArrayList<IGithubLoginListener> loginListeners = new ArrayList<IGithubLoginListener>();

	private static ArrayList<IScriptingLanguage> langauges=new ArrayList<>();
 	static{
 		
		try {                                                                                                                                                                                                                                 
	        final URL url = new URL("http://github.com");                                                                                                                                                                                 
	        final URLConnection conn = url.openConnection();                                                                                                                                                                                  
	        conn.connect();    
	        conn.getInputStream();                                                                                                                                                                                                               
	        hasnetwork= true;                                                                                                                                                                                                                      
	    } catch (Exception e) {                                                                                                                                                                                                             
	        // we assuming we have no access to the server and run off of the chached gists.    
	    	hasnetwork= false;                                                                                                                                                                                                                              
	    }  
 		File scriptingDir = new File(System.getProperty("user.home")+"/git/BowlerStudio/src/main/resources/com/neuronrobotics/bowlerstudio/");
		workspace = new File(System.getProperty("user.home")+"/bowler-workspace/");
		if(!workspace.exists()){
			workspace.mkdir();
		}
//		if(scriptingDir.exists()){
//			workspace=scriptingDir;
//		}
		loadLoginData();
		addScriptingLanguage(new ClojureHelper());
		addScriptingLanguage(new GroovyHelper());
		addScriptingLanguage(new JythonHelper());

	}
 	
 	private static void loadLoginData(){
 		if(loginID == null && getCreds().exists() && hasnetwork){
			try {
				String line;
			
			    InputStream fis = new FileInputStream(getCreds().getAbsolutePath());
			    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
			    @SuppressWarnings("resource")
				BufferedReader br = new BufferedReader(isr);
			
			    while ((line = br.readLine()) != null) {
			        if(line.contains("login")){
			        	loginID = line.split("=")[1];
			        }
			        if(line.contains("password")){
			        	pw = line.split("=")[1];
			        }
			    }
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
 	}
 	
 	
 	public static void addScriptingLanguage(IScriptingLanguage lang){
 		langauges.add(lang);
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
		//System.err.println("Workspace: "+workspace.getAbsolutePath());
		return workspace;
	}

	public static ShellType setFilename(String name) {
		for (IScriptingLanguage l:langauges){
			if(l.isSupportedFileExtenetion(name))
				return l.getShellType();
		}

		return ShellType.GROOVY;
	}
	
	public static String getLoginID(){
		

		return loginID;
	}
	
	public static void login() throws IOException{
		if(! hasnetwork)
			return;
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

	private static void waitForLogin(String id) throws IOException, InvalidRemoteException, TransportException, GitAPIException{
		try {                                                                                                                                                                                                                                 
	        final URL url = new URL("http://github.com");                                                                                                                                                                                 
	        final URLConnection conn = url.openConnection();                                                                                                                                                                                  
	        conn.connect();   
	        conn.getInputStream();
	        hasnetwork= true;                                                                                                                                                                                                                      
	    } catch (Exception e) {                                                                                                                                                                                                             
	        // we assuming we have no access to the server and run off of the chached gists.    
	    	hasnetwork= false;                                                                                                                                                                                                                              
	    }  
		if(!hasnetwork)
			return;
		if(github == null){

			if (getCreds().exists()){
				try{
					github = GitHub.connect();
				}catch(IOException ex){
					ex.printStackTrace();
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
		loadLoginData();
		if(cp == null){
			cp = new UsernamePasswordCredentialsProvider(loginID, pw);

		}
		File gistDir=new File(getWorkspace().getAbsolutePath()+"/gistcache/"+id);
		if(!gistDir.exists()){
			gistDir.mkdir();
		}
		
		

		
		GHGist gist;
		try{
			gist = github.getGist(id);
		}catch(IOException ex){
			//ex.printStackTrace();
			
			return;
		}
		String localPath=gistDir.getAbsolutePath();
		String remotePath = gist.getGitPullUrl();
		File gitRepoFile = new File(localPath + "/.git");
		
	
		if(!gitRepoFile.exists()){
			System.out.println("Cloning files to: "+localPath);
			 //Clone the repo
		    Git.cloneRepository().setURI(remotePath).setDirectory(new File(localPath)).call();
		}
	    Repository localRepo = new FileRepository(gitRepoFile.getAbsoluteFile());
	    Git git = new Git(localRepo);
	    for(int i=0;i<10;i++){
		    try{
		    	git.pull().setCredentialsProvider(cp).call();// updates to the latest version
		    	//git.commit().setMessage("Updates any changes").call();
		    	//git.push().setCredentialsProvider(cp).call();
		    	return;
		    }catch(Exception ex){
		    	try {
		    	    Files.delete(gitRepoFile.toPath());
		    	} catch (Exception x) {
		    		x.printStackTrace();
		    	} 
		    	ex.printStackTrace();
		    }
	    }
	}
	

	public static ArrayList<String> filesInGist(String gistcode, String extnetion) {
		ArrayList<String> f=new ArrayList<>();
		try {
			
			waitForLogin(gistcode);
			File gistDir=new File(getWorkspace().getAbsolutePath()+"/gistcache/"+gistcode);
			for (final File fileEntry : gistDir.listFiles()) {
				if(!fileEntry.getName().endsWith(".git"))
					if(extnetion==null)
						f.add(fileEntry.getName());
					else if(fileEntry.getName().endsWith(extnetion))
						f.add(fileEntry.getName());
		    }
			return f;
		} catch (InterruptedIOException e) {
			System.out.println("Gist Rate limited, you realy should login to github");
		} catch (MalformedURLException ex) {
			// ex.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidRemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	
	public static ArrayList<String> filesInGist(String id){
		return filesInGist(id, null);
	}
	
	public static String getUserIdOfGist(String id){
		try {
			waitForLogin(id);
			Log.debug("Loading Gist: " + id);
			GHGist gist;
			try{
				gist = github.getGist(id);
				return gist.getOwner().getLogin();
			}catch(IOException ex){
				ex.printStackTrace();
			}
		} catch (IOException | GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return null;
		
	}
	
	public static void pushCodeToGistID(String id, String FileName, String content )  throws Exception{
		File gistDir=new File(getWorkspace().getAbsolutePath()+"/gistcache/"+id);
		File desired = new File(gistDir.getAbsoluteFile()+"/"+FileName);
		if(!gistDir.exists()){
			gistDir.mkdir();
		}
		FileUtils.writeStringToFile(desired, content);
		if(!hasnetwork){
			return;
		}
		try {	
			waitForLogin(id);	
			Log.debug("Loading Gist: " + id);
			GHGist gist;
			try{
				gist = github.getGist(id);
			}catch(IOException ex){
				//ex.printStackTrace();
				
				return;
			}
			
			
			
			
			String localPath=gistDir.getAbsolutePath();
			String remotePath = gist.getGitPullUrl();
			File gitRepoFile = new File(localPath + "/.git");
			if(!gitRepoFile.exists()){
				System.out.println("Cloning files to: "+localPath);
				 //Clone the repo
			    Git.cloneRepository().setURI(remotePath).setDirectory(new File(localPath)).call();
			}
		    Repository localRepo = new FileRepository(gitRepoFile.getAbsoluteFile());
		    Git git = new Git(localRepo);
		    try{
		    	git.pull().setCredentialsProvider(cp).call();// updates to the latest version
		    	if(!desired.exists()){
		    		desired.createNewFile();
		    		git.add().addFilepattern(FileName).call();
		    	}
		    	FileUtils.writeStringToFile(desired, content);
		    	git.commit().setAll(true).setMessage("Updates any changes").call();
		    	git.push().setCredentialsProvider(cp).call();
		    }catch(Exception ex){
		    	ex.printStackTrace();
		    }
		} catch (InterruptedIOException e) {
			System.out.println("Gist Rate limited");
		} catch (MalformedURLException ex) {
			// ex.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ;
	}
	
	

	public static String[] codeFromGistID(String id, String FileName)  throws Exception{
		try {	
			if(fileLastLoaded.get(id) ==null ){
				// forces the first time the files is accessed by the application tou pull an update
				fileLastLoaded.put(id, System.currentTimeMillis()-TIME_TO_WAIT_BETWEEN_GIT_PULL);
			}
			long lastTime =fileLastLoaded.get(id);
			File gistDir=new File(getWorkspace().getAbsolutePath()+"/gistcache/"+id);
			if(System.currentTimeMillis()>lastTime+TIME_TO_WAIT_BETWEEN_GIT_PULL || !gistDir.exists())// wait 2 seconds before re-downloading the file
				waitForLogin(id);
			

		    if(FileName==null||FileName.length()<1){
		    	if(gistDir.listFiles().length>0){
		    		FileName=gistDir.listFiles()[0].getAbsolutePath();
		    	}
		    }
		    
		    File targetFile = new File(gistDir.getAbsolutePath()+"/"+FileName);
			if(targetFile.exists()){
				//System.err.println("Loading file: "+targetFile.getAbsoluteFile());
				//Target file is ready to go
				 String text = new String(Files.readAllBytes(Paths.get(targetFile.getAbsolutePath())), StandardCharsets.UTF_8);
				 return new String[] { text, FileName , targetFile.getAbsolutePath()};
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

	public static Object inlineFileScriptRun(File f, ArrayList<Object> args) throws Exception{
		byte[] bytes;

		try {
			bytes = Files.readAllBytes(f.toPath());
			String s = new String(bytes, "UTF-8");
			return inlineScriptRun(s, args,setFilename(f.getName()) );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static Object inlineGistScriptRun(String gistID, String Filename ,ArrayList<Object> args)  throws Exception{
		String[] gistData = codeFromGistID(gistID,Filename);
		return inlineScriptRun(gistData[0], args,setFilename(gistData[1]));
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
	
	public static Object inlineScriptRun(String code, ArrayList<Object> args,ShellType activeType) {
		
		for (IScriptingLanguage l:langauges){
			if(l.getShellType() == activeType){
				return l.inlineScriptRun(code, args);
			}
		}
		return null;
	}

	public static String[] getImports() {
		return imports;
	}



}
