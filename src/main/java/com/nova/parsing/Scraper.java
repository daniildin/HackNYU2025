package com.nova.parsing;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Scraper {
	public static Map<String, Map<String, String>> runAll() {
		Map<String, Map<String, String>> combined = new HashMap<>();
		Map<String, String> nova = NovaNewsScraper.fetch();
		Map<String, String> cnbc = CNBCScraper.fetch();
		combined.put("NovaNews", nova);
		combined.put("CNBC", cnbc);

		try {
			Path dir = Path.of(System.getProperty("user.dir"), "data");
			Files.createDirectories(dir);
			String json = toJson(combined);
			Files.writeString(dir.resolve("articles.json"), json, StandardCharsets.UTF_8);
		} catch (Exception ignored) {}

		return combined;
	}

	private static String esc(String s) {
		if (s == null) return "";
		return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r");
	}

	private static String toJson(Map<String, Map<String,String>> data) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		boolean firstS = true;
		for (Map.Entry<String, Map<String,String>> e : data.entrySet()) {
			if (!firstS) sb.append(",");
			sb.append("\"").append(esc(e.getKey())).append("\":{");
			boolean firstF = true;
			for (Map.Entry<String,String> f : e.getValue().entrySet()) {
				if (!firstF) sb.append(",");
				sb.append("\"").append(esc(f.getKey())).append("\":\"").append(esc(f.getValue())).append("\"");
				firstF = false;
			}
			sb.append("}");
			firstS = false;
		}
		sb.append("}");
		return sb.toString();
	}
}
