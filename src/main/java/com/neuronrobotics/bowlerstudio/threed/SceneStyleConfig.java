package com.neuronrobotics.bowlerstudio.threed;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import com.neuronrobotics.sdk.common.Log;

public final class SceneStyleConfig {

	private static final Properties PROPERTIES = load();

	private SceneStyleConfig() {
	}

	private static Properties load() {
		Properties properties = new Properties();

		String configuredPath = System.getProperty("cadoodle.sceneStyle");
		Path path;

		if (configuredPath != null && !configuredPath.isBlank()) {
			path = Paths.get(configuredPath);
		} else {
			path = Paths.get(System.getProperty("user.dir"), "scene-style.properties");
		}

		if (Files.isRegularFile(path)) {
			try (InputStream in = Files.newInputStream(path)) {
				properties.load(in);
				Log.debug("Loaded scene style " + path.toAbsolutePath());
			} catch (Exception ex) {
				Log.error("Failed to load scene style " + path + ": " + ex.getMessage());
			}
		}

		return properties;
	}

	public static String getString(String key, String fallback) {
		String value = PROPERTIES.getProperty(key);
		if (value == null || value.isBlank())
			return fallback;
		return value.trim();
	}

	public static double getDouble(String key, double fallback) {
		String value = PROPERTIES.getProperty(key);
		if (value == null)
			return fallback;

		try {
			double parsed = Double.parseDouble(value.trim());
			return Double.isFinite(parsed) ? parsed : fallback;
		} catch (NumberFormatException ex) {
			return fallback;
		}
	}

	public static boolean getBoolean(String key, boolean fallback) {
		String value = PROPERTIES.getProperty(key);
		if (value == null)
			return fallback;

		value = value.trim();

		if ("true".equalsIgnoreCase(value))
			return true;
		if ("false".equalsIgnoreCase(value))
			return false;

		return fallback;
	}

	public static double clamp01(double value) {
		return Math.max(0.0, Math.min(1.0, value));
	}
}
