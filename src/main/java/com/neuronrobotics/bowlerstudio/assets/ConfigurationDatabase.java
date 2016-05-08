package com.neuronrobotics.bowlerstudio.assets;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHRepository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;

public class ConfigurationDatabase {
	private static final String repo = "BowlerStudioConfiguration";
	private static String gitSource = "https://github.com/madhephaestus/" + repo + ".git"; // madhephaestus
	private static String dbFile= "database.json";
	private static boolean checked;
	private static HashMap<String,HashMap<String,Object>> database=null;
	private static Type TT_mapStringString = new TypeToken<HashMap<String,HashMap<String,Object>>>(){}.getType();
	//chreat the gson object, this is the parsing factory
	private static Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
	static Thread shutDownHook = null;
	public static HashMap<String,Object> getParams(String paramsKey){
		return getDatabase().get(paramsKey);
	}
	
	public static Object getObject(String paramsKey,String objectKey , Object defaultValue){
		if(getDatabase().get(paramsKey).get(objectKey)==null){
			setObject( paramsKey, objectKey,  defaultValue );
		}
		return getDatabase().get(paramsKey).get(objectKey);
	}
	
	public static Object setObject(String paramsKey,String objectKey, Object value ){
		return getDatabase().get(paramsKey).put(objectKey,value);
	}
	
	public static Object removeObject(String paramsKey,String objectKey ){
		return getDatabase().get(paramsKey).remove(objectKey);
	}
	
	
	public static void save(){
		String writeOut=null;
		getDatabase();
		synchronized(database){
			 writeOut  =gson.toJson(database, TT_mapStringString); 
		}
		try {
			ScriptingEngine.pushCodeToGit( getGitSource(),"master", getDbFile(), writeOut, "Saving database");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static HashMap<String,HashMap<String,Object>> getDatabase(){
		if(database!=null)
			return database;
		try {
			database= (HashMap<String, HashMap<String, Object>>) ScriptingEngine.inlineFileScriptRun(loadFile(), null);
			setShutDowHook();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return database;
	}

	private static void setShutDowHook() {
		if(shutDownHook==null){
			shutDownHook =new Thread() {
				@Override
				public void run() {
					save();
				}
			};
			
			Runtime.getRuntime().addShutdownHook(shutDownHook);
		}
	}

	public static File loadFile() throws Exception {
		return ScriptingEngine.fileFromGit(getGitSource(), // git repo, change
															getDbFile()
		);
	}

	public static String getGitSource() throws Exception {
		if (BowlerStudio.hasNetwork())
			if (!checked && ScriptingEngine.getCreds().exists()) {
				checked = true;
				ScriptingEngine.setAutoupdate(true);
				org.kohsuke.github.GitHub github = ScriptingEngine.getGithub();
				GHMyself self = github.getMyself();
				Map<String, GHRepository> myPublic = self.getAllRepositories();
				for (Map.Entry<String, GHRepository> entry : myPublic.entrySet()) {
					if (entry.getKey().contentEquals(repo)) {
						GHRepository ghrepo = entry.getValue();
						String myAssets = ghrepo.getGitTransportUrl().replaceAll("git://", "https://");
						System.out.println("Using my version of assets: " + myAssets);
						setGitSource(myAssets);
					}
				}
			}
		return gitSource;

	}
	
	private static void setGitSource(String myAssets) {
		database=null;
		gitSource=myAssets;
		getDatabase();
	}

	public static String getDbFile() {
		return dbFile;
	}

	public static void setDbFile(String dbFile) {
		ConfigurationDatabase.dbFile = dbFile;
		setGitSource(gitSource);
	}
}