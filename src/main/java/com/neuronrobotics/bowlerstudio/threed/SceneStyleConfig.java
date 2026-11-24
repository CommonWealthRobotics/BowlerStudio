package com.neuronrobotics.bowlerstudio.threed;

import java.util.Map;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;

public final class SceneStyleConfig {

	private static final Map<String, String> DEFAULTS = createDefaults();

	private static Map<String, String> createDefaults() {
		Map<String, String> defaults = new LinkedHashMap<>();
		defaults.put("camera.orbit.fallMs", "120.0");
		defaults.put("camera.orbit.half", "700.0");
		defaults.put("camera.orbit.max", "0.08");
		defaults.put("camera.orbit.min", "0.012");
		defaults.put("camera.orbit.riseMs", "250.0");
		defaults.put("edges.angleDegrees", "35.0");
		defaults.put("edges.brightness", "0.0");
		defaults.put("edges.cameraSettleDelayMs", "120.0");
		defaults.put("edges.enabled", "true");
		defaults.put("edges.highlightWidthMultiplier", "1.8");
		defaults.put("edges.radius", "0.15");
		defaults.put("edges.sceneSettleDelayMs", "500.0");
		defaults.put("edges.stitchTolerance", "0.002");
		defaults.put("edges.topologyPrecision", "1.0E-5");
		defaults.put("edges.widthMode", "screen");
		defaults.put("edges.widthPx", "1.6");
		defaults.put("grid.ground.renderOpacity", "1.0");
		defaults.put("grid.major.brightnessMultiplier", "1.0");
		defaults.put("grid.major.lineWidthPx", "4.0");
		defaults.put("grid.major.opacityMultiplier", "1.0");
		defaults.put("grid.motionMode", "texture");
		defaults.put("grid.small.brightnessMultiplier", "0.85");
		defaults.put("grid.small.lineWidthPx", "2.0");
		defaults.put("grid.small.opacityMultiplier", "1.0");
		defaults.put("grid.small.visibleZoomDepth", "350.0");
		defaults.put("grid.small.zOffsetMm", "-0.05");
		defaults.put("halos.enabled", "true");
		defaults.put("handle.scale", "0.75");
		defaults.put("lighting.ambient", "0.47");
		defaults.put("lighting.fill.enabled", "true");
		defaults.put("lighting.fill.intensity", "0.16");
		defaults.put("lighting.fill.x", "0.65");
		defaults.put("lighting.fill.y", "-0.1");
		defaults.put("lighting.fill.z", "-0.25");
		defaults.put("lighting.key.enabled", "true");
		defaults.put("lighting.key.intensity", "0.52");
		defaults.put("lighting.key.x", "-0.8");
		defaults.put("lighting.key.y", "-1.0");
		defaults.put("lighting.key.z", "-0.45");
		defaults.put("lighting.mode", "stable");
		defaults.put("lighting.rim.enabled", "true");
		defaults.put("lighting.rim.intensity", "0.08");
		defaults.put("lighting.rim.x", "0.2");
		defaults.put("lighting.rim.y", "0.9");
		defaults.put("lighting.rim.z", "-0.15");
		return Collections.unmodifiableMap(defaults);
	}

	private SceneStyleConfig() {
	}

	public static Map<String, String> defaults() {
		return DEFAULTS;
	}

	public static void resetToDefaults() {
		resetToDefaults((key, value) -> ConfigurationDatabase.put("CaDoodle", key, value), ConfigurationDatabase::save);
	}

	static void resetToDefaults(BiConsumer<String, String> writer, Runnable save) {
		DEFAULTS.forEach(writer);
		save.run();
	}

	private static Object read(String key, String fallback) {
		return ConfigurationDatabase.get("CaDoodle", key, fallback);
	}

	public static String getString(String key, String fallback) {
		return getString(key, fallback, SceneStyleConfig::read);
	}

	static String getString(String key, String fallback, BiFunction<String, String, Object> reader) {
		String defaultValue = DEFAULTS.getOrDefault(key, fallback);
		Object saved = reader.apply(key, defaultValue);
		if (saved == null || saved.toString().codePoints().allMatch(Character::isWhitespace))
			return defaultValue;
		return saved.toString().trim();
	}

	public static double getDouble(String key, double fallback) {
		return getDouble(key, fallback, SceneStyleConfig::read);
	}

	static double getDouble(String key, double fallback, BiFunction<String, String, Object> reader) {
		String defaultValue = DEFAULTS.getOrDefault(key, Double.toString(fallback));
		double defaultNumber = Double.parseDouble(defaultValue);
		Object saved = reader.apply(key, defaultValue);
		if (saved == null)
			return defaultNumber;
		try {
			double parsed = Double.parseDouble(saved.toString().trim());
			return Double.isFinite(parsed) ? parsed : defaultNumber;
		} catch (NumberFormatException ex) {
			return defaultNumber;
		}
	}

	public static boolean getBoolean(String key, boolean fallback) {
		return getBoolean(key, fallback, SceneStyleConfig::read);
	}

	static boolean getBoolean(String key, boolean fallback, BiFunction<String, String, Object> reader) {
		String defaultValue = DEFAULTS.getOrDefault(key, Boolean.toString(fallback));
		Object saved = reader.apply(key, defaultValue);
		if (saved != null) {
			String value = saved.toString().trim();
			if ("true".equalsIgnoreCase(value))
				return true;
			if ("false".equalsIgnoreCase(value))
				return false;
		}
		return Boolean.parseBoolean(defaultValue);
	}

	public static double clamp01(double value) {
		return Math.max(0.0, Math.min(1.0, value));
	}
}
