package com.nova;

import java.util.Map;
import java.util.Scanner;
import java.util.HashMap;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

public class App {
	// Minimal single-run app: prompt -> scrape -> analyze -> notify -> exit

	// tiny .env loader (optional fallback)
	private static Map<String, String> loadEnvFile() {
		Map<String, String> map = new HashMap<>();
		Path p = Path.of(".env");
		try {
			if (Files.exists(p)) {
				for (String raw : Files.readAllLines(p, StandardCharsets.UTF_8)) {
					String line = raw.trim();
					if (line.isEmpty() || line.startsWith("#")) continue;
					int eq = line.indexOf('=');
					if (eq <= 0) continue;
					String k = line.substring(0, eq).trim();
					String v = line.substring(eq + 1).trim();
					if ((v.startsWith("\"") && v.endsWith("\"")) || (v.startsWith("'") && v.endsWith("'"))) {
						v = v.substring(1, v.length() - 1);
					}
					map.put(k, v);
				}
			}
		} catch (Exception ignored) {}
		return map;
	}

	private static String argOrPropOrEnvOrDotEnv(String cliVal, String propKey, String envKey, Map<String, String> dotEnv) {
		if (cliVal != null && !cliVal.isBlank()) return cliVal;
		String prop = System.getProperty(propKey);
		if (prop != null && !prop.isBlank()) return prop.trim();
		String env = System.getenv(envKey);
		if (env != null && !env.isBlank()) return env.trim();
		if (dotEnv != null) {
			String v = dotEnv.get(envKey);
			if (v != null && !v.isBlank()) return v.trim();
		}
		return null;
	}

	public static void main(String[] args) throws Exception {
		// parse CLI args: --key <val> | --key=<val>, --tickers <val> | --tickers=<val>
		String cliKey = null, cliTickers = null;
		for (int i = 0; i < args.length; i++) {
			String a = args[i];
			if ("--key".equals(a) && i + 1 < args.length) { cliKey = args[++i]; continue; }
			if (a.startsWith("--key=")) { cliKey = a.substring("--key=".length()); continue; }
			if ("--tickers".equals(a) && i + 1 < args.length) { cliTickers = args[++i]; continue; }
			if (a.startsWith("--tickers=")) { cliTickers = a.substring("--tickers=".length()); continue; }
		}

		Map<String, String> dotEnv = loadEnvFile();
		String apiKey = argOrPropOrEnvOrDotEnv(cliKey, "gemini.key", "GEMINI_API_KEY", dotEnv);
		String portfolio = argOrPropOrEnvOrDotEnv(cliTickers, "portfolio.tickers", "PORTFOLIO_TICKERS", dotEnv);

		// If still missing, prompt only if interactive
		Scanner sc = new Scanner(System.in);
		boolean interactive = System.console() != null;
		if ((apiKey == null || apiKey.isBlank()) && interactive) {
			System.out.print("Enter GEMINI_API_KEY (will not be saved, press Enter to skip): ");
			apiKey = sc.hasNextLine() ? sc.nextLine().trim() : "";
		}
		if ((portfolio == null || portfolio.isBlank()) && interactive) {
			System.out.print("Enter PORTFOLIO_TICKERS (comma-separated, e.g. AAPL,MSFT): ");
			portfolio = sc.hasNextLine() ? sc.nextLine().trim() : "";
		}

		// If non-interactive and still missing, print instructions and exit
		if (portfolio == null || portfolio.isBlank()) {
			System.err.println("No portfolio provided.");
			System.err.println("Provide via one of:");
			System.err.println("  - CLI args: --tickers \"AAPL,MSFT\" [--key YOUR_KEY]");
			System.err.println("  - JVM props: -Dportfolio.tickers=\"AAPL,MSFT\" [-Dgemini.key=YOUR_KEY]");
			System.err.println("  - Env vars: PORTFOLIO_TICKERS=\"AAPL,MSFT\" [GEMINI_API_KEY=YOUR_KEY]");
			System.err.println("  - .env file with PORTFOLIO_TICKERS and GEMINI_API_KEY");
			return;
		}
		if (apiKey == null) apiKey = ""; // optional, analyzer runs locally without it

		System.out.println("Fetching articles from NovaNews and CNBC...");
		Map<String, Map<String, String>> articles = Scraper.runAll();

		String title = "No recent article";
		String content = "No content available";

		Map<String, String> nova = articles.get("NovaNews");
		Map<String, String> cnbc = articles.get("CNBC");

		if (nova != null && cnbc != null) {
			title = (nova.getOrDefault("headline","") + " & " + cnbc.getOrDefault("headline","")).trim();
			content = "NovaNews: " + nova.getOrDefault("content","") + "\n\nCNBC: " + cnbc.getOrDefault("content","");
		} else if (nova != null) {
			title = nova.getOrDefault("headline","Latest Market News");
			content = nova.getOrDefault("content","");
		} else if (cnbc != null) {
			title = cnbc.getOrDefault("headline","Latest Market News");
			content = cnbc.getOrDefault("content","");
		}

		System.out.println("Analyzing portfolio impact...");
		String analysisJson = PortfolioEffect.analyze(title, "aggregated", new String[]{"unknown"}, content, portfolio, apiKey);

		System.out.println("\n=== Analysis ===");
		System.out.println(analysisJson);

		// Simple notification: BUY if any ticker has likelihood >= 0.5 and sentiment > 0
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(analysisJson);
			JsonNode affected = root.path("affectedStocks");
			boolean shouldBuy = false;
			String buyTicker = null;

			if (affected != null && affected.isObject()) {
				for (String tk : iterableFields(affected)) {
					JsonNode entry = affected.path(tk);
					double likelihood = entry.path("likelihood").asDouble(0.0);
					double sentiment = entry.path("sentiment").asDouble(0.0);
					if (likelihood >= 0.5 && sentiment > 0.0) {
						shouldBuy = true;
						buyTicker = tk;
						break;
					}
				}
			}

			if (shouldBuy) {
				System.out.println("\n=== NOTIFICATION: BUY " + buyTicker + " ===");
			} else {
				System.out.println("\n=== NOTIFICATION: NO ACTION ===");
			}
		} catch (Exception e) {
			System.err.println("Failed to parse analysis for notification: " + e.getMessage());
		}
	}

	private static Iterable<String> iterableFields(JsonNode node) {
		return () -> node.fieldNames();
	}
}
