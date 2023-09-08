package com.neuronrobotics.bowlerstudio.scripting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

import com.neuronrobotics.video.OSUtil;

import javafx.scene.control.Button;

public abstract class EclipseExternalEditor implements IExternalEditor {

	protected Button advanced;

	protected abstract void setUpEclipseProjectFiles(File dir, File project, String name)
			throws IOException, MalformedURLException;

	protected abstract boolean checkForExistingProjectFiles(File dir);

	protected static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	protected boolean OSSupportsEclipse() {
		return OSUtil.isLinux() || OSUtil.isWindows()|| OSUtil.isOSX();
	}



	@Override
	public void launch(File file, Button advanced) {
		this.advanced = advanced;
		new Thread(() -> {
			String eclipseEXE = "eclipse";
			if (OSUtil.isLinux()) {
				try {
					ScriptingEngine.cloneRepo("https://github.com/CommonWealthRobotics/ESP32ArduinoEclipseInstaller.git",null);
					ScriptingEngine.pull("https://github.com/CommonWealthRobotics/ESP32ArduinoEclipseInstaller.git");
					eclipseEXE = ScriptingEngine
							.fileFromGit("https://github.com/CommonWealthRobotics/ESP32ArduinoEclipseInstaller.git",
									"eclipse")
							.getAbsolutePath();

				} catch (GitAPIException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else if (OSUtil.isOSX()) {
				try {
					ScriptingEngine.cloneRepo("https://github.com/CommonWealthRobotics/ESP32ArduinoEclipseInstaller.git",null);
					ScriptingEngine.pull("https://github.com/CommonWealthRobotics/ESP32ArduinoEclipseInstaller.git");
					eclipseEXE = ScriptingEngine
							.fileFromGit("https://github.com/CommonWealthRobotics/ESP32ArduinoEclipseInstaller.git",
									"eclipse-mac")
							.getAbsolutePath();

				} catch (GitAPIException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (OSUtil.isWindows()) {
				eclipseEXE = "\"C:\\RBE\\sloeber\\eclipse.exe\"";
			} else {
				System.out.println("OS is not supported!");
				return;
			}
			try {
				Git locateGit = ScriptingEngine.locateGit(file);
				Repository repository = locateGit.getRepository();
				File dir = repository.getWorkTree();
				ScriptingEngine.closeGit(locateGit);
				String remoteURL = ScriptingEngine.locateGitUrlString(file);
				String branch = ScriptingEngine.getBranch(remoteURL);
				String ws = ScriptingEngine.getWorkspace().getAbsolutePath() + delim() + "eclipse";

				File ignore = new File(dir.getAbsolutePath() + delim() + ".gitignore");
				File project = new File(dir.getAbsolutePath() + delim() + ".project");
				String name = dir.getName();
				if (dir.getAbsolutePath().contains("gist.github.com")) {
					String name2 = file.getName();
					String[] split = name2.split("\\.");
					name = split[0];
				}
				if (!ignore.exists() || !project.exists() || !checkForExistingProjectFiles(dir)) {
					String content = "";
					String toIgnore = "\n/.settings\n" +"/.project\n" + "/.classpath\n" + "/.cproject\n" + "/cache/\n" + "/*.class";
					
					if (ignore.exists())
						try {
							content = new String(Files.readAllBytes(Paths.get(ignore.getAbsolutePath())));
						} catch (IOException e) {
							e.printStackTrace();
						}
					if (!content.contains(toIgnore)) {
						content += toIgnore;
						if(ScriptingEngine.checkOwner(remoteURL)) {
							ScriptingEngine.pushCodeToGit(remoteURL, branch, ".gitignore", content,
								"Ignore the project files");
						}else {
							content+="\n.gitignore\n";
							File gistDir = ScriptingEngine.cloneRepo(remoteURL, branch);
							File desired = new File(gistDir.getAbsoluteFile() + "/" + ".gitignore");
							OutputStream out = null;
							try {
								out = FileUtils.openOutputStream(desired, false);
								IOUtils.write(content, out);
								out.close(); // don't swallow close Exception if copy completes
								// normally
							} finally {
								IOUtils.closeQuietly(out);
							}
						}
					}
					setUpEclipseProjectFiles(dir, project, name);

				}
				
				File currentws = null;
				if (OSUtil.isOSX())
					currentws= new File(System.getProperty("user.home")+"/bin/eclipse/Eclipse.app/Contents/Eclipse/configuration/.settings/org.eclipse.ui.ide.prefs");
				
				if (OSUtil.isLinux())
					currentws= new File(System.getProperty("user.home")+"/bin/eclipse-slober-rbe/eclipse/configuration/.settings/org.eclipse.ui.ide.prefs");
				if (OSUtil.isWindows())
					currentws= new File("C:\\RBE\\sloeber\\configuration\\.settings\\org.eclipse.ui.ide.prefs");
				
				try {
					BufferedReader reader = new BufferedReader(new FileReader(currentws.getAbsolutePath()));
					String line = reader.readLine();
					while (line != null) {
						//System.out.println(line);
						// read next line
						line = reader.readLine();
						if(line.startsWith("RECENT_WORKSPACES=")) {
							String[] split = line
									.split("=");
							String[] split2 = split[1].split("\\\\n");
							if(OSUtil.isWindows()) {
								String replaceAll = split2[0]
										.replace("\\:", ":");
								String replaceAll2 = replaceAll
										.replace("\\\\", "\\");
								
								ws=replaceAll2;
							}else {
								ws= split2[0];
							}
							System.out.println("Using workspace config: "+line);
							
							break;
						}
					}
					reader.close();
				

				}catch(Exception ex) {
					//ex.printStackTrace();
					System.out.println("Workspace missing, opening eclipse");
				}
				
				System.out.println("Opening workspace "+ws);
				if(!isEclipseOpen( ws)) {
					if (OSUtil.isOSX())
						run( ScriptingEngine.getWorkspace(),System.out, "bash", eclipseEXE, "-data", ws);
					if (OSUtil.isLinux())
						run( ScriptingEngine.getWorkspace(),System.out, "bash", eclipseEXE, "-data", ws);
					if (OSUtil.isWindows())
						run(ScriptingEngine.getWorkspace() ,System.out, eclipseEXE, "-data", ws);
					while (!isEclipseOpen( ws)) {
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.out.println("Waiting for workspace "+ws);
						
					}
					
				}else {
					System.out.println("Eclipse is already open at "+ws);
				}
				
				File projects = new File(ws + delim() + ".metadata" + delim() + ".plugins" + delim()
						+ "org.eclipse.core.resources" + delim() + ".projects" + delim());
				// For each pathname in the pathnames array
				if (projects.exists()) {
					for (String pathname : projects.list()) {
						if (pathname.endsWith(name) || pathname.endsWith(dir.getName())) {
							System.out.println("Project "+name+" is already in the workspace!");
							advanced.setDisable(false);
							return;
						}
					}
				}
				if (OSUtil.isOSX())
					run(dir,System.err, "bash", eclipseEXE, dir.getAbsolutePath() + delim());
				if (OSUtil.isLinux())
					run(dir,System.err, "bash", eclipseEXE, dir.getAbsolutePath() + delim());
				if (OSUtil.isWindows())
					run(dir,System.err, eclipseEXE, dir.getAbsolutePath() + delim());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}).start();
	}
	
	private boolean isEclipseOpen(String ws) {
		if(!new File(ws).exists())
			return false;
		String lockFile = ws + delim() + ".metadata" + delim() + ".lock";
		System.out.println("Checking WS "+lockFile);
		try {
			File lock = new File(lockFile);
			if (lock.exists()) {
				RandomAccessFile raFile = new RandomAccessFile(lock.getAbsoluteFile(), "rw");

				FileLock fileLock = raFile.getChannel().tryLock(0, 1, false);
				fileLock.release();
				raFile.close();
			}

		} catch (Exception ex) {
			// lock failed eclipse is open already
			return true;
		}
		return false;
	}

	@Override
	public String nameOfEditor() {

		return "Eclipse";
	}

	@Override
	public URL getInstallURL() throws MalformedURLException {
		return new URL("https://github.com/CommonWealthRobotics/ESP32ArduinoEclipseInstaller/blob/master/README.md");
	}

}
