package com.neuronrobotics.bowlerstudio.threed;


import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;

public final class SceneStyleConfig {

	private SceneStyleConfig() {
	}

	public static String getString(String key, String fallback) {
		String value = ConfigurationDatabase.get("CaDoodle", key, "" + fallback).toString();
		if (value == null || value.isBlank())
			return fallback;
		return value.trim();
	}

	public static double getDouble(String key, double fallback) {
		String value = ConfigurationDatabase.get("CaDoodle", key, "" + fallback).toString();
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
		String value = ConfigurationDatabase.get("CaDoodle", key, "" + fallback).toString();
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
