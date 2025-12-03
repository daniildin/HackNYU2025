package com.nova;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.nova.logic.PortfolioEffect;
import com.nova.logic.Welcome;
import com.nova.parsing.Scraper;

public class App {
	private static final String ASCII_ART =
				"███╗   ██╗ ██████╗ ██╗   ██╗ █████╗ \n" +
				"████╗  ██║██╔═══██╗██║   ██║██╔══██╗\n" +
				"██╔██╗ ██║██║   ██║██║   ██║███████║\n" +
				"██║╚██╗██║██║   ██║██║   ██║██╔══██║\n" +
				"██║ ╚████║╚██████╔╝╚██████╔╝██║  ██║\n" +
				"╚═╝  ╚═══╝ ╚═════╝  ╚═════╝ ╚═╝  ╚═╝\n" +
				"        N   O   V   A";

        @SuppressWarnings("UseSpecificCatch")
	public static void main(String[] args) {
		System.out.println(ASCII_ART);

		Scanner sc = new Scanner(System.in);

		System.out.print("Enter tickers (AAPL,MSFT,...): ");
		String portfolio = sc.nextLine().trim();

		System.out.print("Enter API key (optional): ");
		String apiKey = sc.nextLine().trim();

		
		try { Welcome.saveEnv(apiKey, portfolio); } catch (Exception ignored) {}

		System.out.println("Fetching...");
		Map<String, Map<String, String>> articles = Scraper.runAll();
		Map<String,String> nova = articles.get("NovaNews");
		Map<String,String> cnbc = articles.get("CNBC");

		String title = nova.get("headline") + " / " + cnbc.get("headline");
		String content = "--- NovaNews ---\n" + nova.get("content") + "\n\n--- CNBC ---\n" + cnbc.get("content");

		String aiJson = PortfolioEffect.analyze(title, "mix", new String[]{"n/a"}, content, portfolio, apiKey);

	
		Map<String, Double> aiMap = parseAnalyzerJson(aiJson);
		Map<String, Double> novaSent = parseNovaSentiment(nova.getOrDefault("sentiment",""));
		Map<String, Double> finalMap = blendMaps(aiMap, novaSent);

		String json = buildJson(finalMap);
		System.out.println(json);

		try {
			Path dir = Path.of(System.getProperty("user.dir"), "data");
			Files.createDirectories(dir);
			Files.writeString(dir.resolve("analysis.json"), json, StandardCharsets.UTF_8);
		} catch (Exception ignored) {}

		
		java.util.List<Map.Entry<String, Double>> sorted = new java.util.ArrayList<>(finalMap.entrySet());
		sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

		double threshold = 0.6;
		StringBuilder buys = new StringBuilder();
		for (Map.Entry<String, Double> e : sorted) {
			if (e.getValue() >= threshold) {
				if (buys.length() > 0) buys.append(", ");
				buys.append(e.getKey()).append(" (").append(e.getValue()).append(")");
			}
		}

		if (buys.length() > 0) {
			System.out.println("=== NOTIFICATION: BUY === " + buys);
		} else {
			System.out.println("=== NOTIFICATION: NO ACTION ===");
		}
	}

	
        @SuppressWarnings("UseSpecificCatch")
	private static Map<String, Double> parseAnalyzerJson(String json) {
		Map<String, Double> out = new HashMap<>();
		if (json == null) return out;
		String s = json.trim();
		if (!s.startsWith("{") || !s.endsWith("}")) return out;
		String inner = s.substring(1, s.length() - 1).trim();
		if (inner.isEmpty()) return out;
		for (String part : inner.split(",")) {
			String p = part.trim();
			int colon = p.indexOf(':');
			if (colon <= 0) continue;
			String k = p.substring(0, colon).trim();
			if (k.startsWith("\"") && k.endsWith("\"") && k.length() >= 2) k = k.substring(1, k.length() - 1);
			try {
				double v = Double.parseDouble(p.substring(colon + 1).trim());
				out.put(k.toUpperCase(), v);
			} catch (Exception ignored) {}
		}
		return out;
	}

        @SuppressWarnings("UseSpecificCatch")
	private static Map<String, Double> parseNovaSentiment(String raw) {
		Map<String, Double> out = new HashMap<>();
		if (raw == null || raw.isBlank()) return out;
		String normalized = raw.replace("·", ",");
		for (String part : normalized.split(",")) {
			String p = part.trim();
			if (p.isEmpty()) continue;
			String[] toks = p.split("\\s+");
			if (toks.length >= 2) {
				String tk = toks[0].trim().toUpperCase();
				try {
					double v = Double.parseDouble(toks[1].trim());
					if (v < 0) v = 0; if (v > 1) v = 1;
					out.put(tk, Math.round(v * 100.0) / 100.0);
				} catch (Exception ignored) {}
			}
		}
		return out;
	}

	private static String buildJson(Map<String, Double> data) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		boolean first = true;
		for (Map.Entry<String, Double> e : data.entrySet()) {
			if (!first) sb.append(",");
			sb.append("\"").append(e.getKey()).append("\":").append(e.getValue());
			first = false;
		}
		sb.append("}");
		return sb.toString();
	}

	private static Map<String, Double> blendMaps(Map<String, Double> aiMap, Map<String, Double> novaMap) {
		Map<String, Double> out = new HashMap<>();
		// union of keys
		for (String tk : aiMap.keySet()) out.put(tk, aiMap.get(tk));
		for (String tk : novaMap.keySet()) {
			Double a = out.get(tk);
			Double n = novaMap.get(tk);
			double val;
			if (a != null && n != null) {
				val = (a + n) / 2.0;
			} else {
				val = (a != null) ? a : n;
			}
			
			if (val < 0) val = 0;
			if (val > 1) val = 1;
			val = Math.round(val * 100.0) / 100.0;
			out.put(tk, val);
		}
		return out;
	}
}
