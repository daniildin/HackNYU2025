package com.nova.logic;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PortfolioEffect {
	public static String analyze(String newsTitle, String publisher, String[] writers, String newsContent, String portfolio, String apiKey) {
		return localAnalyze(newsTitle, newsContent, portfolio);
	}

	public static String analyzeViaUrl(String newsTitle, String publisher, String[] writers, String newsContent,
	                                   String portfolio, String aiUrl, String apiKey) {
		try {
			String ai = callCustomAi(aiUrl, apiKey, portfolio, newsTitle, newsContent);
			return (ai == null) ? localAnalyze(newsTitle, newsContent, portfolio) : ai.trim();
		} catch (Exception e) {
			return localAnalyze(newsTitle, newsContent, portfolio);
		}
	}

	private static String localAnalyze(String title, String content, String portfolio) {
		String all = (title + " " + content).toLowerCase();
		String[] pos = {"up","gain","gains","rise","rose","surge","beat","strong","growth","profit","record","upgrade"};
		String[] neg = {"down","loss","losses","drop","fell","miss","weak","decline","slump","downgrade"};

		int p=0, n=0;
		for (String w : all.split("\\s+")) {
			for (String x : pos) if (w.equals(x)) p++;
			for (String x : neg) if (w.equals(x)) n++;
		}
		double base = 0.5 + 0.5 * ((double)(p - n) / Math.max(1, p + n));
		if (base < 0) base = 0;
		if (base > 1) base = 1;

		StringBuilder out = new StringBuilder();
		out.append("{");
		boolean first = true;
		for (String raw : portfolio.split(",")) {
			String tk = raw.trim().toUpperCase();
			if (tk.isEmpty()) continue;
			boolean mentioned = all.contains(tk.toLowerCase()) || all.contains("$" + tk.toLowerCase());
			double val = mentioned ? base : 0.5 + (base - 0.5) * 0.5;
			double rounded = Math.round(val * 100.0) / 100.0;
			if (!first) out.append(",");
			out.append("\"").append(tk).append("\":").append(rounded);
			first = false;
		}
		out.append("}");
		return out.toString();
	}

	private static String callCustomAi(String aiUrl, String apiKey, String portfolio, String title, String content) throws Exception {
		String body = "{"
				+ "\"tickers\":\"" + esc(portfolio) + "\","
				+ "\"title\":\"" + esc(title) + "\","
				+ "\"content\":\"" + esc(content) + "\""
				+ "}";

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest.Builder b = HttpRequest.newBuilder()
				.uri(URI.create(aiUrl))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(body));
		if (apiKey != null && !apiKey.isBlank()) b.header("Authorization", "Bearer " + apiKey.trim());
		HttpResponse<String> res = client.send(b.build(), HttpResponse.BodyHandlers.ofString());
		return res.body() == null ? "" : res.body().trim();
	}

	private static String esc(String s) {
		if (s == null) return "";
		return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r");
	}
}
