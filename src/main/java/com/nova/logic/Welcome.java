package com.nova.logic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

public class Welcome {
    // Save .env file in project root
    public static void saveEnv(String apiKey, String portfolio) throws IOException {
        String content = "GEMINI_API_KEY=" + apiKey + System.lineSeparator()
                + "PORTFOLIO_TICKERS=" + portfolio + System.lineSeparator();
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
