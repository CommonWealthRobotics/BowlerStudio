package com.neuronrobotics.bowlerstudio.scripting;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.neuronrobotics.sdk.addons.kinematics.JavaFXInitializer;
import com.neuronrobotics.video.OSUtil;

public class EclipseExternalEditor implements IExternalEditor {


	@Override
	public boolean isSupportedByExtention(File file) {
		if (OSUtil.isLinux())
			if (GroovyHelper.class.isInstance(ScriptingEngine.getLangaugeByExtention(file.getAbsolutePath()))) {
				return true;
			}
		return false;
	}

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	@Override
	public void launch(File file) {
		new Thread(() -> {
			if (OSUtil.isLinux()) {
				try {
					ScriptingEngine.pull("https://github.com/WPIRoboticsEngineering/ESP32ArduinoEclipseInstaller.git");
					File launcher = ScriptingEngine.fileFromGit(
							"https://github.com/WPIRoboticsEngineering/ESP32ArduinoEclipseInstaller.git",
							"linux-eclipse-esp32.sh");
					Repository repository = ScriptingEngine.locateGit(file).getRepository();
					File dir = repository.getWorkTree();
					String ws = ScriptingEngine.getWorkspace().getAbsolutePath() + "/eclipse";
					File ignore = new File(dir.getAbsolutePath() + "/.gitignore");
					File classpath = new File(dir.getAbsolutePath() + "/.classpath");
					File project = new File(dir.getAbsolutePath() + "/.project");
					String name = dir.getName();
					if (!ignore.exists() || !classpath.exists() || !project.exists()) {
						String content = "";
						if (ignore.exists())
							try {
								content = new String(Files.readAllBytes(Paths.get(ignore.getAbsolutePath())));
							} catch (IOException e) {
								e.printStackTrace();
							}

						content += "/.project\n" + "/.classpath\n" + "/cache/\n" + "/*.class";
						Files.write(Paths.get(ignore.getAbsolutePath()), content.getBytes());

						String ProjectContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
								+ "<projectDescription>\n" + "	<name>" + name + "</name>\n"
								+ "	<comment></comment>\n" + "	<projects>\n" + "	</projects>\n" + "	<buildSpec>\n"
								+ "		<buildCommand>\n" + "			<name>org.eclipse.jdt.core.javabuilder</name>\n"
								+ "			<arguments>\n" + "			</arguments>\n" + "		</buildCommand>\n"
								+ "	</buildSpec>\n" + "	<natures>\n"
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
					File lock = new File(ws + "/.metadata/.lock");
					RandomAccessFile raFile = new RandomAccessFile(lock.getAbsoluteFile(), "rw");
					try {
						FileLock fileLock = raFile.getChannel().tryLock(0, 1, false);
						fileLock.release();
						raFile.close();

						run(dir, "bash", launcher.getAbsolutePath(), "-data", ws);
						try {
							Thread.sleep(30000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.out.println("Adding project to ");

					} catch (Exception ex) {
						// lock failed eclipse is open already
						System.out.println("Eclipse is already open");
					}
					File projects = new File(ws + "/" + ".metadata/.plugins/org.eclipse.core.resources/.projects/");
					// For each pathname in the pathnames array
					for (String pathname : projects.list()) {
						if (pathname.endsWith(name)) {
							System.out.println("Project is already in the workspace!");
							return;
						}
					}
					run(dir, "bash", launcher.getAbsolutePath(), dir.getAbsolutePath() + "/");
				} catch (GitAPIException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				System.out.println("OS is not supported!");
			}
		}).start();
	}

	@Override
	public String nameOfEditor() {

		return "Eclipse IDE";
	}

	@Override
	public URL getInstallURL() throws MalformedURLException {
		return new URL("https://github.com/WPIRoboticsEngineering/RobotInterfaceBoard/blob/master/InstallEclipse.md");
	}

	public static void main(String[] args) throws Exception {
		JavaFXInitializer.go();
		ScriptingEngine.pull("https://github.com/madhephaestus/SVGBowlerExtrude.git");
		File f = ScriptingEngine.fileFromGit("https://github.com/madhephaestus/SVGBowlerExtrude.git", "test.groovy");

		new EclipseExternalEditor().launch(f);
	}

}
