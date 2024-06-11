package com.neuronrobotics.bowlerstudio.scripting.external;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FilenameUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;

public class DownloadManager {
	private static String editorsURL= "https://github.com/CommonWealthRobotics/ExternalEditorsBowlerStudio.git";
	private static String 		bindir = System.getProperty("user.home") + "/bin/BowlerStudioInstall/";

	public static void main(String[] args) {
		File f = getExecutable("eclipse",null);
		if(f.exists())
			System.out.println("Executable retrived:\n"+f.getAbsolutePath());
		else
			System.out.println("Failed to load file!\n"+f.getAbsolutePath());
	}
	public static File getExecutable(String exeType,ProgressBar progress) {
		String key = discoverKey();
		
		try {
			for(String f:ScriptingEngine.filesInGit(editorsURL)) {
				File file = ScriptingEngine.fileFromGit(editorsURL, f);
				if( file.getName().toLowerCase().startsWith(exeType.toLowerCase())&&
					file.getName().toLowerCase().endsWith(".json")) {
					String jsonText = new String(Files.readAllBytes(file.toPath()));
					Type TT_mapStringString = new TypeToken<HashMap<String, Object>>() {
					}.getType();
					Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
					HashMap<String, Object> database = gson.fromJson(jsonText, TT_mapStringString);
					Map<String, Object> vm = (Map<String, Object>) database.get(key);
					if(vm!=null) {
						System.out.println("Configuration found for "+exeType+" on "+key);
						String baseURL = vm.get("url").toString();
						String type = vm.get("type").toString();
						String name = vm.get("name").toString();
						String jvmURL = baseURL + name + "." + type;
						File jvmArchive = download("", jvmURL, 400000000, progress, bindir, name + "." + type);
						File dest = new File(bindir + name);
						if (!dest.exists()) {
							if (type.toLowerCase().contains("zip")) {
								unzip(jvmArchive, bindir+name);
							}
							if (type.toLowerCase().contains("tar.gz")) {
								untar(jvmArchive, bindir+name);
							}
						} else {
							System.out.println("Not extraction, VM exists " + dest.getAbsolutePath());
						}
						String cmd = bindir + name + "/bin/eclipse" + (isWin() ? ".exe" : "") + " ";
						return new File(cmd);
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		throw new RuntimeException("Executable for OS: "+key+" has no entry for "+exeType);
	}
	
	public static boolean isExecutable(ZipArchiveEntry entry) {
		int unixMode = entry.getUnixMode();
		// Check if any of the executable bits are set for user, group, or others.
		// User executable: 0100 (0x40), Group executable: 0010 (0x10), Others
		// executable: 0001 (0x01)
		return (unixMode & 0x49) != 0;
	}
	public static void unzip(File path, String dir) throws Exception {
		System.out.println("Unzipping "+path.getName()+" into "+dir);
		String fileBaseName = FilenameUtils.getBaseName(path.getName().toString());
		Path destFolderPath = new File(dir).toPath();

		try (ZipFile zipFile = ZipFile.builder().setFile(path).get()) {
			Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
			while (entries.hasMoreElements()) {
				ZipArchiveEntry entry = entries.nextElement();
				Path entryPath = destFolderPath.resolve(entry.getName());
				if (entryPath.normalize().startsWith(destFolderPath.normalize())) {
					if (entry.isDirectory()) {
						Files.createDirectories(entryPath);
					} else {
						Files.createDirectories(entryPath.getParent());
						try (InputStream in = zipFile.getInputStream(entry)) {
							try {
								//ar.setExternalAttributes(entry.extraAttributes);
								if (entry.isUnixSymlink()) {
									String text = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
											.lines().collect(Collectors.joining("\n"));
									Path target = Paths.get(".", text);
									System.out.println("Creating symlink "+entryPath+" with "+target);

									Files.createSymbolicLink(entryPath, target);
									continue;
								}
							} catch (Exception ex) {
								ex.printStackTrace();
							}
							try (OutputStream out = new FileOutputStream(entryPath.toFile())) {
								IOUtils.copy(in, out);
							}
							if(isExecutable(entry)) {
								entryPath.toFile().setExecutable(true);
							}
						}
					}
				}
			}
		}
	}

	public static void untar(File tarFile, String dir) throws Exception {
		System.out.println("Untaring "+tarFile.getName()+" into "+dir);

		File dest = new File(dir);
		dest.mkdir();
		TarArchiveInputStream tarIn = null;
		try {
		tarIn = new TarArchiveInputStream(
				new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(tarFile))));
		}catch (java.io.IOException ex) {
			tarFile.delete();
			return;
		}
		TarArchiveEntry tarEntry = tarIn.getNextTarEntry();
		// tarIn is a TarArchiveInputStream
		while (tarEntry != null) {// create a file with the same name as the tarEntry
			File destPath = new File(dest.toString() + System.getProperty("file.separator") + tarEntry.getName());
			System.out.println("working: " + destPath.getCanonicalPath());
			if (tarEntry.isDirectory()) {
				destPath.mkdirs();
			} else {
				destPath.createNewFile();
				FileOutputStream fout = new FileOutputStream(destPath);
				byte[] b = new byte[(int) tarEntry.getSize()];
				tarIn.read(b);
				fout.write(b);
				fout.close();
				int mode = tarEntry.getMode();
				b = new byte[5];
				TarUtils.formatUnsignedOctalString(mode, b, 0, 4);
				if (bits(b[1]).endsWith("1")) {
					destPath.setExecutable(true);
				}
			}
			tarEntry = tarIn.getNextTarEntry();
		}
		tarIn.close();
	}

	private static String bits(byte b) {
		return String.format("%6s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
	}
	public static boolean isWin() {
		return System.getProperty("os.name").toLowerCase().contains("windows");
	}
	public static boolean isLin() {
		return System.getProperty("os.name").toLowerCase().contains("linux");
	}
	public static boolean isMac() {
		return System.getProperty("os.name").toLowerCase().contains("mac");
	}
	public static boolean isArm() {
		return System.getProperty("os.arch").toLowerCase().contains("aarch64")||System.getProperty("os.arch").toLowerCase().contains("arm");
	}
	public static String discoverKey() {
		String key = "UNKNOWN";
		if (isLin()) {
			if (isArm()) {
				key = "Linux-aarch64";
			} else {
				key = "Linux-x64";
			}
		}

		if (isMac()) {
			if (isArm()) {
				key = "Mac-aarch64";
			} else {
				key = "Mac-x64";
			}
		}
		if (isWin()) {
			if (isArm()) {
				key = "UNKNOWN";
			} else {
				key = "Windows-x64";
			}
		}
		if(key.contentEquals("UNKNOWN")) {
			throw new RuntimeException("Unsupported OS/Arch "+System.getProperty("os.name")+" "+System.getProperty("os.arch"));
		}
		return key;
	}


	public static File download(String version, String downloadJsonURL, long sizeOfJson, ProgressBar progress,
			String bindir, String filename) throws MalformedURLException, IOException, FileNotFoundException {
		URL url = new URL(downloadJsonURL);
		URLConnection connection = url.openConnection();
		InputStream is = connection.getInputStream();
		ProcessInputStream pis = new ProcessInputStream(is, (int) sizeOfJson);
		pis.addListener(new Listener() {
			@Override
			public void process(double percent) {
				System.out.println("Download percent " + percent);
				if(progress!=null)
					Platform.runLater(() -> {
						progress.setProgress(percent);
					});
			}
		});
		File folder = new File(bindir + version + "/");
		File exe = new File(bindir + version + "/" + filename);

		if (!folder.exists() || !exe.exists()) {
			System.out.println("Start Downloading " + filename);
			folder.mkdirs();
			exe.createNewFile();
			byte dataBuffer[] = new byte[1024];
			int bytesRead;
			FileOutputStream fileOutputStream = new FileOutputStream(exe.getAbsoluteFile());
			while ((bytesRead = pis.read(dataBuffer, 0, 1024)) != -1) {
				fileOutputStream.write(dataBuffer, 0, bytesRead);
			}
			fileOutputStream.close();
			pis.close();
			System.out.println("Finished downloading " + filename);
		} else {
			System.out.println("Not downloadeing, it existst " + filename);
		}
		return exe;
	}

	/**
	 * @return the editorsURL
	 */
	public static String getEditorsURL() {
		return editorsURL;
	}

	/**
	 * @param editorsURL the editorsURL to set
	 */
	public static void setEditorsURL(String editorsURL) {
		DownloadManager.editorsURL = editorsURL;
	}

}
