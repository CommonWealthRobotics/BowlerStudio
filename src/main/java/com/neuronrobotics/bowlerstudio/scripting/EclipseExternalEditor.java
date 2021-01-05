package com.neuronrobotics.bowlerstudio.scripting;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Paths;
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
		return OSUtil.isLinux() || OSUtil.isWindows();
	}

	protected String delim() {
		if (OSUtil.isWindows())
			return "\\";
		return "/";
	}

	@Override
	public void launch(File file, Button advanced) {
		this.advanced = advanced;
		new Thread(() -> {
			String eclipseEXE = "eclipse";
			if (OSUtil.isLinux()) {
				try {
					ScriptingEngine.pull("https://github.com/WPIRoboticsEngineering/ESP32ArduinoEclipseInstaller.git");
					eclipseEXE = ScriptingEngine
							.fileFromGit("https://github.com/WPIRoboticsEngineering/ESP32ArduinoEclipseInstaller.git",
									"linux-eclipse-esp32.sh")
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
				Repository repository = ScriptingEngine.locateGit(file).getRepository();
				File dir = repository.getWorkTree();
				String remoteURL = ScriptingEngine.locateGitUrlString(file);
				String branch = ScriptingEngine.getBranch(remoteURL);
				String ws = ScriptingEngine.getWorkspace().getAbsolutePath() + delim() + "eclipse";
				if (OSUtil.isWindows())
					ws = "C:\\RBE\\eclipse-workspace";
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
					String toIgnore = "/.project\n" + "/.classpath\n" + "/.cproject\n" + "/cache/\n" + "/*.class";

					if (ignore.exists())
						try {
							content = new String(Files.readAllBytes(Paths.get(ignore.getAbsolutePath())));
						} catch (IOException e) {
							e.printStackTrace();
						}
					if (!content.contains(toIgnore)) {
						content += toIgnore;
						ScriptingEngine.pushCodeToGit(remoteURL, branch, ".gitignore", content,
								"Ignore the project files");
					}
					setUpEclipseProjectFiles(dir, project, name);

				}

				if(!isEclipseOpen( ws)) {
					if (OSUtil.isLinux())
						run(dir, "bash", eclipseEXE, "-data", ws);
					if (OSUtil.isWindows())
						run(dir, eclipseEXE, "-data", ws);
					while (!isEclipseOpen( ws)) {
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}else {
					System.out.println("Eclipse is already open");
				}
				File projects = new File(ws + delim() + "" + ".metadata" + delim() + ".plugins" + delim()
						+ "org.eclipse.core.resources" + delim() + ".projects" + delim());
				// For each pathname in the pathnames array
				if (projects.exists()) {
					for (String pathname : projects.list()) {
						if (pathname.endsWith(name) || pathname.endsWith(dir.getName())) {
							System.out.println("Project is already in the workspace!");
							advanced.setDisable(false);
							return;
						}
					}
				}
				if (OSUtil.isLinux())
					run(dir, "bash", eclipseEXE, dir.getAbsolutePath() + delim());
				if (OSUtil.isWindows())
					run(dir, eclipseEXE, dir.getAbsolutePath() + delim());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}).start();
	}
	
	private boolean isEclipseOpen(String ws) {
		String lockFile = ws + delim() + ".metadata" + delim() + ".lock";
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
		return new URL("https://github.com/WPIRoboticsEngineering/RobotInterfaceBoard/blob/master/InstallEclipse.md");
	}

}
