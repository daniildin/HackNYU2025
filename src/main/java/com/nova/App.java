package com.nova;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;

import com.nova.logic.PortfolioEffect;
import com.nova.logic.Welcome;
import com.nova.parsing.Scraper;

public class App {
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);

		System.out.print("Enter tickers (AAPL,MSFT,...): ");
		String portfolio = sc.nextLine().trim();

		System.out.print("Enter API key (optional): ");
		String apiKey = sc.nextLine().trim();

		System.out.print("Enter AI endpoint URL (optional): ");
		String aiUrl = sc.nextLine().trim();

		try { Welcome.saveEnv(apiKey, portfolio, aiUrl); } catch (Exception ignored) {}

		System.out.println("Fetching...");
		Map<String, Map<String, String>> articles = Scraper.runAll();

		Map<String,String> nova = articles.get("NovaNews");
		Map<String,String> cnbc = articles.get("CNBC");

		String title = nova.get("headline") + " / " + cnbc.get("headline");
		String content = nova.get("content") + "\n\n" + cnbc.get("content");

		String json = (aiUrl != null && !aiUrl.isBlank())
				? PortfolioEffect.analyzeViaUrl(title, "mix", new String[]{"n/a"}, content, portfolio, aiUrl, apiKey)
				: PortfolioEffect.analyze(title, "mix", new String[]{"n/a"}, content, portfolio, apiKey);
		System.out.println(json);

		try {
			Path dir = Path.of(System.getProperty("user.dir"), "data");
			Files.createDirectories(dir);
			Files.writeString(dir.resolve("analysis.json"), json, StandardCharsets.UTF_8);
		} catch (Exception ignored) {}

		double best = -1.0;
		String bestTk = null;
		String s = json.trim();
		if (s.startsWith("{") && s.endsWith("}")) {
			String inner = s.substring(1, s.length()-1).trim();
			for (String part : inner.split(",")) {
				String p = part.trim();
				int colon = p.indexOf(':');
				String k = p.substring(0, colon).trim();
				if (k.startsWith("\"") && k.endsWith("\"") && k.length() >= 2) k = k.substring(1, k.length()-1);
				double v = Double.parseDouble(p.substring(colon+1).trim());
				if (v > best) { best = v; bestTk = k; }
			}
		}

		if (best >= 0.6) System.out.println("=== NOTIFICATION: BUY " + bestTk + " ===");
		else System.out.println("=== NOTIFICATION: NO ACTION ===");
	}
}
