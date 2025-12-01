package com.nova.logic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Welcome {
	// Save .env file in project root
	public static void saveEnv(String apiKey, String portfolio) throws IOException {
		saveEnv(apiKey, portfolio, "");
	}

	public static void saveEnv(String apiKey, String portfolio, String aiUrl) throws IOException {
		String content = "GEMINI_API_KEY=" + (apiKey == null ? "" : apiKey) + System.lineSeparator()
				+ "PORTFOLIO_TICKERS=" + (portfolio == null ? "" : portfolio) + System.lineSeparator()
				+ "AI_ENDPOINT=" + (aiUrl == null ? "" : aiUrl) + System.lineSeparator();
		Files.writeString(Path.of(".env"), content, StandardCharsets.UTF_8);
	}

	// Simple CLI usage
	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.out.println("Usage: java com.nova.logic.Welcome <API_KEY> <AAPL,MSFT,...>");
			return;
		}
		saveEnv(args[0], args[1]);
		System.out.println(".env saved");
	}
}
