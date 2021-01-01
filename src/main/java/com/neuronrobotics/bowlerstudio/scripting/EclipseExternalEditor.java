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

public abstract class EclipseExternalEditor implements IExternalEditor {
	
	protected abstract void setUpEclipseProjectFiles(File dir , File project, String name) throws IOException, MalformedURLException;



	protected static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}	
	
	protected boolean OSSupportsEclipse() {
		return OSUtil.isLinux();
	}

	@Override
	public void launch(File file) {
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
			} else {
				System.out.println("OS is not supported!");
				return;
			}
			try {
				Repository repository = ScriptingEngine.locateGit(file).getRepository();
				File dir = repository.getWorkTree();
				String remoteURL= ScriptingEngine.locateGitUrlString(file);
				String branch=ScriptingEngine.getBranch(remoteURL);
				String ws = ScriptingEngine.getWorkspace().getAbsolutePath() + "/eclipse";
				File ignore = new File(dir.getAbsolutePath() + "/.gitignore");
				
				File project = new File(dir.getAbsolutePath() + "/.project");
				String name = dir.getName();
				if(dir.getAbsolutePath().contains("gist.github.com")) {
					String name2 = file.getName();
					String[] split = name2.split("\\.");
					name=split[0];
				}
				if (!ignore.exists() ||  !project.exists()) {
					String content = "";
					if (ignore.exists())
						try {
							content = new String(Files.readAllBytes(Paths.get(ignore.getAbsolutePath())));
						} catch (IOException e) {
							e.printStackTrace();
						}

					content += "/.project\n" + "/.classpath\n"+ "/.cproject\n"  + "/cache/\n" + "/*.class";
					ScriptingEngine.pushCodeToGit(remoteURL, branch, ".gitignore", content, "Ignore the project files");


					setUpEclipseProjectFiles( dir,project, name);

				}
				File lock = new File(ws + "/.metadata/.lock");
				RandomAccessFile raFile = new RandomAccessFile(lock.getAbsoluteFile(), "rw");
				try {
					FileLock fileLock = raFile.getChannel().tryLock(0, 1, false);
					fileLock.release();
					raFile.close();

					if (OSUtil.isLinux()) run(dir, "bash", eclipseEXE, "-data", ws);
					try {
						Thread.sleep(30000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("Adding project to eclipse..");

				} catch (Exception ex) {
					// lock failed eclipse is open already
					System.out.println("Eclipse is already open");
				}
				File projects = new File(ws + "/" + ".metadata/.plugins/org.eclipse.core.resources/.projects/");
				// For each pathname in the pathnames array
				for (String pathname : projects.list()) {
					if (pathname.endsWith(name)||pathname.endsWith(dir.getName())) {
						System.out.println("Project is already in the workspace!");
						return;
					}
				}
				if (OSUtil.isLinux()) run(dir, "bash", eclipseEXE, dir.getAbsolutePath() + "/");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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

}
