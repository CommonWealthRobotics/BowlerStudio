package com.neuronrobotics.bowlerstudio.scripting.external;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.scripting.BashLoader;
import com.neuronrobotics.bowlerstudio.scripting.GroovyHelper;
import com.neuronrobotics.bowlerstudio.scripting.JsonRunner;
import com.neuronrobotics.bowlerstudio.scripting.RobotHelper;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.StlLoader;
import com.neuronrobotics.video.OSUtil;

import eu.mihosoft.vrl.v3d.JavaFXInitializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import static com.neuronrobotics.bowlerstudio.scripting.DownloadManager.*;

public class GroovyEclipseExternalEditor extends EclipseExternalEditor {
	
	public void onProcessExit(int ev) {
		advanced.setDisable(false);
	}

	
	protected void setUpEclipseProjectFiles(File dir , File project, String name) throws IOException, MalformedURLException {
		
		File classpath = new File(dir.getAbsolutePath() + delim()+".classpath");
		String ProjectContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<projectDescription>\n"
				+ "	<name>" + name + "</name>\n" + "	<comment></comment>\n" + "	<projects>\n"
				+ "	</projects>\n" + "	<buildSpec>\n" + "		<buildCommand>\n"
				+ "			<name>org.eclipse.jdt.core.javabuilder</name>\n" + "			<arguments>\n"
				+ "			</arguments>\n" + "		</buildCommand>\n" + "	</buildSpec>\n" + "	<natures>\n"
				+ "		<nature>org.eclipse.jdt.groovy.core.groovyNature</nature>\n"
				+ "		<nature>org.eclipse.jdt.core.javanature</nature>\n" + "	</natures>\n"
				+ "</projectDescription>";
		String java8Prefs="eclipse.preferences.version=1\n"
				+ "org.eclipse.jdt.core.compiler.codegen.inlineJsrBytecode=enabled\n"
				+ "org.eclipse.jdt.core.compiler.codegen.methodParameters=do not generate\n"
				+ "org.eclipse.jdt.core.compiler.codegen.targetPlatform=1.8\n"
				+ "org.eclipse.jdt.core.compiler.codegen.unusedLocal=preserve\n"
				+ "org.eclipse.jdt.core.compiler.compliance=1.8\n"
				+ "org.eclipse.jdt.core.compiler.debug.lineNumber=generate\n"
				+ "org.eclipse.jdt.core.compiler.debug.localVariable=generate\n"
				+ "org.eclipse.jdt.core.compiler.debug.sourceFile=generate\n"
				+ "org.eclipse.jdt.core.compiler.problem.assertIdentifier=error\n"
				+ "org.eclipse.jdt.core.compiler.problem.enablePreviewFeatures=disabled\n"
				+ "org.eclipse.jdt.core.compiler.problem.enumIdentifier=error\n"
				+ "org.eclipse.jdt.core.compiler.problem.reportPreviewFeatures=warning\n"
				+ "org.eclipse.jdt.core.compiler.release=disabled\n"
				+ "org.eclipse.jdt.core.compiler.source=1.8\n"
				+ "\n";
		File file = new File(dir.getAbsolutePath() + delim()+".settings");
		if(!file.exists())
			file.mkdirs();
		Files.write(Paths.get(file.getAbsolutePath()+ delim()+"org.eclipse.jdt.core.prefs"), java8Prefs.getBytes());
		
		Files.write(Paths.get(project.getAbsolutePath()), ProjectContent.getBytes());
		//String latestVersionString = "1.12.0";
//		InputStream is = new URL(
//				"https://api.github.com/repos/CommonWealthRobotics/BowlerStudio/releases/latest")
//						.openStream();
//		try {
//			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
//			String jsonText = readAll(rd);
//			// Create the type, this tells GSON what datatypes to instantiate when parsing
//			// and saving the json
//			Type TT_mapStringString = new TypeToken<HashMap<String, Object>>() {
//			}.getType();
//			// chreat the gson object, this is the parsing factory
//			Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
//			HashMap<String, Object> database = gson.fromJson(jsonText, TT_mapStringString);
//			latestVersionString = (String) database.get("tag_name");
//		} finally {
//			is.close();
//		}
		//latestVersionString = BowlerStudio.getBowlerStudioBinaryVersion();
		
		String jar = System.getProperty("user.home") + delim()+"bin"+delim()+"BowlerStudioInstall"+ delim()+ "latest"
				+delim()+ "BowlerStudio.jar";
		if(!new File(jar).exists()) {
			jar = System.getProperty("user.home") + delim()+"bin"+delim()+"BowlerStudioInstall"+ delim()+ BowlerStudio.getBowlerStudioBinaryVersion()
					+delim()+ "BowlerStudio.jar";
		}
		String classpathContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<classpath>\n"
				+ "	<classpathentry kind=\"src\" path=\"\"/>\n"
				+ "	<classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\">\n"
				+ "		<attributes>\n" + "			<attribute name=\"module\" value=\"true\"/>\n"
				+ "		</attributes>\n" + "	</classpathentry>\n"
				+ "	<classpathentry kind=\"con\" path=\"GROOVY_DSL_SUPPORT\"/>\n"
				+ "	<classpathentry kind=\"lib\" path=\"" + jar + "\"/>\n"
				+ "	<classpathentry kind=\"output\" path=\"\"/>\n" + "</classpath>";
		Files.write(Paths.get(classpath.getAbsolutePath()), classpathContent.getBytes());
		
	}



	public static void main(String[] args) throws Exception {
		JavaFXInitializer.go();
		File f = ScriptingEngine.fileFromGit("https://gist.github.com/e4b0d8e95d6b3dc83c334a9950753a53.git", "jabber.groovy");

		new GroovyEclipseExternalEditor().launch(f, new javafx.scene.control.Button());
	}



	@Override
	protected boolean checkForExistingProjectFiles(File dir ) {
		File classpath = new File(dir.getAbsolutePath() + delim()+".classpath");
		return classpath.exists();
	}

	@Override
	public List<Class> getSupportedLangauge() {
		return Arrays.asList( GroovyHelper.class,BashLoader.class, JsonRunner.class,RobotHelper.class);
	}
}
