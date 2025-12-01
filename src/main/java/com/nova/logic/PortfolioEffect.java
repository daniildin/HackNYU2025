package com.nova.logic;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class PortfolioEffect {
	public static String analyze(String newsTitle, String publisher, String[] writers, String newsContent, String portfolio, String apiKey) {
		String title = newsTitle == null ? "" : newsTitle;
		String content = newsContent == null ? "" : newsContent;
		String tickers = portfolio == null ? "" : portfolio;

		// Try Gemini if API key provided
		if (apiKey != null && !apiKey.isBlank() && !tickers.isBlank()) {
			try {
				String ai = callGemini(tickers, title, content, apiKey);
				if (ai != null && ai.trim().startsWith("{")) return ai.trim();
			} catch (Exception ignored) {}
		}

		// Fallback local
		return localAnalyze(title, content, tickers);
	}

	private static String localAnalyze(String title, String content, String portfolio) {
		String all = (title + " " + content).toLowerCase();
		double base = baseSentiment(all);

		StringBuilder out = new StringBuilder();
		out.append("{");
		boolean first = true;
		for (String raw : portfolio.split(",")) {
			String tk = raw.trim().toUpperCase();
			if (tk.isEmpty()) continue;
			boolean mentioned = all.contains(tk.toLowerCase()) || all.contains("$" + tk.toLowerCase());
			double val = mentioned ? base : 0.5; // neutral if not mentioned
			double rounded = Math.round(val * 100.0) / 100.0;
			if (!first) out.append(",");
			out.append("\"").append(esc(tk)).append("\":").append(rounded);
			first = false;
		}
		out.append("}");
		return out.toString();
	}

	private static double baseSentiment(String text) {
		String[] pos = {"up","gain","gains","rise","rose","surge","beat","strong","growth","profit","record","upgrade"};
		String[] neg = {"down","loss","losses","drop","fell","miss","weak","decline","slump","downgrade"};

		int p=0, n=0;
		for (String w : text.split("\\s+")) {
			for (String x : pos) if (w.equals(x)) p++;
			for (String x : neg) if (w.equals(x)) n++;
		}
		double base = 0.5 + 0.5 * ((double)(p - n) / Math.max(1, p + n));
		if (base < 0) base = 0;
		if (base > 1) base = 1;
		return base;
	}

	private static String callGemini(String portfolio, String title, String content, String apiKey) throws Exception {
		// Minimal prompt: ask for only JSON mapping of provided tickers to [0,1] floats
		String instructions =
				"Return ONLY a JSON object mapping these tickers to sentiment floats in [0,1]. " +
				"1. 1.0=most optimistic (price up), 0.0=most pessimistic (price down). " +
				"2. Use only these tickers: " + portfolio + ". " +
				"Example: {\\\"AAPL\\\":0.73,\\\"MSFT\\\":0.61}. " +
				"Title: " + title + "\\nContent:\\n" + content;

		String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key="
				+ URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
		String body = "{\"contents\":[{\"parts\":[{\"text\":\"" + jsonEsc(instructions) + "\"}]}]}";

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest req = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(body))
				.build();
		HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
		String resp = res.body() == null ? "" : res.body();

		// Naively extract the first JSON object from the model's text field
		String textField = extractBetween(resp, "\"text\":\"", "\"");
		if (textField == null || textField.isEmpty()) return null;
		textField = textField.replace("\\n","\n").replace("\\\"","\"");
		return extractFirstJson(textField);
	}

	private static String extractBetween(String src, String start, String end) {
		int i = src.indexOf(start);
		if (i < 0) return null;
		int j = src.indexOf(end, i + start.length());
		if (j < 0) return null;
		return src.substring(i + start.length(), j);
	}

	private static String extractFirstJson(String s) {
		int start = s.indexOf('{');
		if (start < 0) return null;
		int depth = 0;
		for (int i = start; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '{') depth++;
			if (c == '}') {
				depth--;
				if (depth == 0) return s.substring(start, i + 1).trim();
			}
		}
		return null;
	}

	private static String esc(String s) {
		if (s == null) return "";
		return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r");
	}

	private static String jsonEsc(String s) {
		if (s == null) return "";
		return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r");
	}
}
