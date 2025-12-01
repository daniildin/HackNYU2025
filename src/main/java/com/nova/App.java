package com.nova;

import com.nova.parsing.Scraper;
import com.nova.logic.PortfolioEffect;

import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {
	public static void main(String[] args) {
		String portfolio = args.length > 0 ? args[0] : "";
		String apiKey = args.length > 1 ? args[1] : "";

		if (portfolio.isBlank()) {
			System.out.print("Enter tickers (AAPL,MSFT,...): ");
			Scanner sc = new Scanner(System.in);
			portfolio = sc.hasNextLine() ? sc.nextLine().trim() : "";
			if (portfolio.isBlank()) {
				System.out.println("No tickers given.");
				return;
			}
		}

		System.out.println("Fetching...");
		Map<String, Map<String, String>> articles = Scraper.runAll();

		Map<String,String> nova = articles.get("NovaNews");
		Map<String,String> cnbc = articles.get("CNBC");

		String title = "No article";
		String content = "Nothing.";
		if (nova != null) {
			title = nova.getOrDefault("headline","Nova");
			content = nova.getOrDefault("content","");
		}
		if (cnbc != null) {
			title = title + " / " + cnbc.getOrDefault("headline","CNBC");
			content = content + "\n\n" + cnbc.getOrDefault("content","");
		}

		String json = PortfolioEffect.analyze(title, "mix", new String[]{"n/a"}, content, portfolio, apiKey);
		System.out.println(json);

		// pick highest sentiment >= 0.6
		double best = -1.0;
		String bestTk = null;
		Matcher m = Pattern.compile("\"([^\"]+)\"\\s*:\\s*([0-9.]+)").matcher(json);
		while (m.find()) {
			String tk = m.group(1);
			double val = 0.0;
			try { val = Double.parseDouble(m.group(2)); } catch (Exception ignored) {}
			if (val > best) { best = val; bestTk = tk; }
		}
		if (bestTk != null && best >= 0.6) {
			System.out.println("=== NOTIFICATION: BUY " + bestTk + " ===");
		} else {
			System.out.println("=== NOTIFICATION: NO ACTION ===");
		}
	}
}
