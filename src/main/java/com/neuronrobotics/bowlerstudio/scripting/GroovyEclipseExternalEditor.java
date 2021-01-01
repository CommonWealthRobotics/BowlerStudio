package com.neuronrobotics.bowlerstudio.scripting;

import java.io.File;

import com.neuronrobotics.sdk.addons.kinematics.JavaFXInitializer;
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
import java.util.HashMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class GroovyEclipseExternalEditor extends EclipseExternalEditor {
	@Override
	public boolean isSupportedByExtention(File file) {
		if (OSSupportsEclipse())
			if (GroovyHelper.class.isInstance(ScriptingEngine.getLangaugeByExtention(file.getAbsolutePath()))) {
				return true;
			}
		return false;
	}


	
	protected void setUpEclipseProjectFiles(File dir , File project, String name) throws IOException, MalformedURLException {
		
		File classpath = new File(dir.getAbsolutePath() + "/.classpath");
		String ProjectContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<projectDescription>\n"
				+ "	<name>" + name + "</name>\n" + "	<comment></comment>\n" + "	<projects>\n"
				+ "	</projects>\n" + "	<buildSpec>\n" + "		<buildCommand>\n"
				+ "			<name>org.eclipse.jdt.core.javabuilder</name>\n" + "			<arguments>\n"
				+ "			</arguments>\n" + "		</buildCommand>\n" + "	</buildSpec>\n" + "	<natures>\n"
				+ "		<nature>org.eclipse.jdt.groovy.core.groovyNature</nature>\n"
				+ "		<nature>org.eclipse.jdt.core.javanature</nature>\n" + "	</natures>\n"
				+ "</projectDescription>";
		Files.write(Paths.get(project.getAbsolutePath()), ProjectContent.getBytes());
		String latestVersionString = "1.12.0";
		InputStream is = new URL(
				"https://api.github.com/repos/CommonWealthRobotics/BowlerStudio/releases/latest")
						.openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			// Create the type, this tells GSON what datatypes to instantiate when parsing
			// and saving the json
			Type TT_mapStringString = new TypeToken<HashMap<String, Object>>() {
			}.getType();
			// chreat the gson object, this is the parsing factory
			Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
			HashMap<String, Object> database = gson.fromJson(jsonText, TT_mapStringString);
			latestVersionString = (String) database.get("tag_name");
		} finally {
			is.close();
		}
		String jar = System.getProperty("user.home") + "/bin/BowlerStudioInstall/" + latestVersionString
				+ "/BowlerStudio.jar";
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
		ScriptingEngine.pull("https://github.com/madhephaestus/SVGBowlerExtrude.git");
		File f = ScriptingEngine.fileFromGit("https://gist.github.com/e4b0d8e95d6b3dc83c334a9950753a53.git", "jabber.groovy");

		new GroovyEclipseExternalEditor().launch(f);
	}

}