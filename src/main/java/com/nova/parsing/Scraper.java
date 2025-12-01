package main.java.com.nova.parsing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/*
 Replace file-based-only behavior with live scraper aggregation.
*/
public class Scraper {
    // Returns a map like {"NovaNews": {headline:..., content:...}, "CNBC": {...}}
    public static Map<String, Map<String, String>> runAll() {
        Map<String, Map<String, String>> combined = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            // If Jsoup (and related classes) are missing, fall back to data/articles.json
            try {
                Class.forName("org.jsoup.Jsoup");
            } catch (ClassNotFoundException | NoClassDefFoundError cnf) {
                // Inform user and fall back to snapshot
                System.err.println("Jsoup not found on classpath — falling back to data/articles.json. To enable live scraping run: ./gradlew build");
                Path base = Path.of(System.getProperty("user.dir"));
                File articlesFile = base.resolve("data").resolve("articles.json").toFile();
                if (articlesFile.exists()) {
                    Map<String, Map<String, String>> data = mapper.readValue(articlesFile, new TypeReference<>() {});
                    return data != null ? data : Collections.emptyMap();
                } else {
                    Map<String, Map<String, String>> placeholder = new HashMap<>();
                    Map<String, String> sample = new HashMap<>();
                    sample.put("headline", "No articles available (Jsoup missing)");
                    sample.put("content", "Jsoup not on classpath and no data/articles.json found.");
                    placeholder.put("Placeholder", sample);
                    return placeholder;
                }
            }

            // Try live scrapers first (Jsoup available)
            Map<String, String> nova = NovaNewsScraper.fetch();
            Map<String, String> cnbc = CNBCScraper.fetch();

            if (nova != null && !nova.isEmpty()) combined.put("NovaNews", nova);
            if (cnbc != null && !cnbc.isEmpty()) combined.put("CNBC", cnbc);

            // If nothing fetched, fall back to data/articles.json
            if (combined.isEmpty()) {
                Path base = Path.of(System.getProperty("user.dir"));
                File articlesFile = base.resolve("data").resolve("articles.json").toFile();
                if (articlesFile.exists()) {
                    Map<String, Map<String, String>> data = mapper.readValue(articlesFile, new TypeReference<>() {});
                    return data != null ? data : Collections.emptyMap();
                } else {
                    // minimal placeholder
                    Map<String, Map<String, String>> placeholder = new HashMap<>();
                    Map<String, String> sample = new HashMap<>();
                    sample.put("headline", "No articles available");
                    sample.put("content", "No content fetched and no data/articles.json found.");
                    placeholder.put("Placeholder", sample);
                    return placeholder;
                }
            } else {
                // persist combined to data/articles.json for debugging
                try {
                    Path base = Path.of(System.getProperty("user.dir"));
                    File dataDir = base.resolve("data").toFile();
                    if (!dataDir.exists()) dataDir.mkdirs();
                    File out = base.resolve("data").resolve("articles.json").toFile();
                    mapper.writerWithDefaultPrettyPrinter().writeValue(out, combined);
                } catch (Exception writeEx) {
                    writeEx.printStackTrace();
                }
                return combined;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }
}
