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
        @SuppressWarnings("UseSpecificCatch")
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);

		System.out.print("Enter tickers (AAPL,MSFT,...): ");
		String portfolio = sc.nextLine().trim();

		System.out.print("Enter API key (optional): ");
		String apiKey = sc.nextLine().trim();

		// Save .env for next run (API key + tickers only)
		try { Welcome.saveEnv(apiKey, portfolio); } catch (Exception ignored) {}

		System.out.println("Fetching...");
		Map<String, Map<String, String>> articles = Scraper.runAll();

		Map<String,String> nova = articles.get("NovaNews");
		Map<String,String> cnbc = articles.get("CNBC");

		// simple debug: show source URLs and sizes
		System.out.println("[Debug] NovaNews url=" + nova.get("url")
				+ " headlineChars=" + (nova.get("headline") == null ? 0 : nova.get("headline").length())
				+ " contentChars=" + (nova.get("content") == null ? 0 : nova.get("content").length()));
		System.out.println("[Debug] CNBC url=" + cnbc.get("url")
				+ " headlineChars=" + (cnbc.get("headline") == null ? 0 : cnbc.get("headline").length())
				+ " contentChars=" + (cnbc.get("content") == null ? 0 : cnbc.get("content").length()));

		// EXTRA: show date, paragraph count, and a short preview for each
		String nContent = nova.get("content");
		String cContent = cnbc.get("content");
		int nParas = nContent.isEmpty() ? 0 : nContent.split("\\n\\n").length;
		int cParas = cContent.isEmpty() ? 0 : cContent.split("\\n\\n").length;
		String nPrev = nContent.length() > 300 ? nContent.substring(0, 300) + "..." : nContent;
		String cPrev = cContent.length() > 300 ? cContent.substring(0, 300) + "..." : cContent;

		System.out.println("[NovaNews] date=" + nova.get("date") + " paras=" + nParas);
		System.out.println("[NovaNews] headline=" + nova.get("headline"));
		System.out.println("[NovaNews] preview=" + nPrev);
		System.out.println("[CNBC] date=" + cnbc.get("date") + " paras=" + cParas);
		System.out.println("[CNBC] headline=" + cnbc.get("headline"));
		System.out.println("[CNBC] preview=" + cPrev);

		String title = nova.get("headline") + " / " + cnbc.get("headline");
		String content = nContent + "\n\n" + cContent;

		String json = PortfolioEffect.analyze(title, "mix", new String[]{"n/a"}, content, portfolio, apiKey);
		System.out.println(json);

		try {
			Path dir = Path.of(System.getProperty("user.dir"), "data");
			Files.createDirectories(dir);
			Files.writeString(dir.resolve("analysis.json"), json, StandardCharsets.UTF_8);

			// Write full scraped texts for manual inspection
			String novaTxt = "URL: " + nova.get("url") + "\nDate: " + nova.get("date") + "\nHeadline: " + nova.get("headline") + "\n\n" + nContent + "\n";
			String cnbcTxt = "URL: " + cnbc.get("url") + "\nDate: " + cnbc.get("date") + "\nHeadline: " + cnbc.get("headline") + "\n\n" + cContent + "\n";
			Files.writeString(dir.resolve("nova.txt"), novaTxt, StandardCharsets.UTF_8);
			Files.writeString(dir.resolve("cnbc.txt"), cnbcTxt, StandardCharsets.UTF_8);
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
