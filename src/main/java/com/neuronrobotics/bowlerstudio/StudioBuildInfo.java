package com.neuronrobotics.bowlerstudio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StudioBuildInfo {
	/** The Constant NAME. */
	private static final String NAME = "Bowler Studio "
			+ getProtocolVersion() + "." + getSDKVersion() + "("
			+ getBuildVersion() + ")";

	public static String getVersion() {
		String s = getTag("app.version");
		if (s == null)
			s = "0.0.0";
		return s;
	}

	public static int getProtocolVersion() {
		return getBuildInfo()[0];
	}

	public static int getSDKVersion() {
		return getBuildInfo()[1];
	}

	public static int getBuildVersion() {
		return getBuildInfo()[2];
	}

	public static int[] getBuildInfo() {
		try{
			String s = getVersion();
			String[] splits = s.split("[.]+");
			int[] rev = new int[3];
			for (int i = 0; i < 3; i++) {
				rev[i] = new Integer(splits[i]);
			}
			return rev;
		}catch(NumberFormatException  e){
			return new int[]{0,0,0};
		}
		
	}

	private static String getTag(String target) {
		try {
			String s = "";
			InputStream is = getBuildPropertiesStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			try {
				while (null != (line = br.readLine())) {
					s += line + "\n";
				}
			} catch (IOException e) {
			}
			String[] splitAll = s.split("[\n]+");
			for (int i = 0; i < splitAll.length; i++) {
				if (splitAll[i].contains(target)) {
					String[] split = splitAll[i].split("[=]+");
					return split[1];
				}
			}
		} catch (NullPointerException e) {
			return null;
		}
		return null;
	}

	public static String getBuildDate() {
		String s = "";
		InputStream is = StudioBuildInfo.class
				.getResourceAsStream("/META-INF/MANIFEST.MF");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		try {
			while (null != (line = br.readLine())) {
				s += line + "\n";
			}
		} catch (IOException e) {
		}
		// System.out.println("Manifest:\n"+s);
		return "";
	}

	private static InputStream getBuildPropertiesStream() {
		return StudioBuildInfo.class.getResourceAsStream("build.properties");
	}

	public static String getSDKVersionString() {
		return NAME;
	}

	public static boolean isOS64bit() {
		return (System.getProperty("os.arch").indexOf("x86_64") != -1);
	}

	public static boolean isARM() {
		return (System.getProperty("os.arch").toLowerCase().indexOf("arm") != -1);
	}

	public static boolean isLinux() {
		return (System.getProperty("os.name").toLowerCase().indexOf("linux") != -1);
	}

	public static boolean isWindows() {
		return (System.getProperty("os.name").toLowerCase().indexOf("win") != -1);
	}

	public static boolean isMac() {
		return (System.getProperty("os.name").toLowerCase().indexOf("mac") != -1);
	}

	public static boolean isUnix() {
		return (isLinux() || isMac());
	}
}
