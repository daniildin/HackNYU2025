package com.nova.parsing;

// Use Jackson via reflection at runtime so compilation doesn't require Jackson on the classpath.

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 Replace file-based-only behavior with live scraper aggregation.
*/
public class Scraper {
    private static final Logger LOGGER = Logger.getLogger(Scraper.class.getName());

    // Returns a map like {"NovaNews": {headline:..., content:...}, "CNBC": {...}}
    public static Map<String, Map<String, String>> runAll() {
        Map<String, Map<String, String>> combined = new HashMap<>();
        // Create an ObjectMapper instance reflectively so this class can compile without Jackson on the classpath.
        Object mapper;
        try {
            Class<?> omClass = Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
            mapper = omClass.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException cnf) {
            mapper = null;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | java.lang.reflect.InvocationTargetException ex) {
            mapper = null;
        }

        try {
            // Try live scrapers first (they return empty map if scraper class is missing)
            Map<String, String> nova = reflectiveFetch("com.nova.parsing.NovaNewsScraper");
            Map<String, String> cnbc = reflectiveFetch("com.nova.parsing.CNBCScraper");

            if (nova != null && !nova.isEmpty()) combined.put("NovaNews", nova);
            if (cnbc != null && !cnbc.isEmpty()) combined.put("CNBC", cnbc);

            Path base = Path.of(System.getProperty("user.dir"));
            File articlesFile = base.resolve("data").resolve("articles.json").toFile();

            // If nothing fetched, fall back to data/articles.json
            if (combined.isEmpty()) {
                if (articlesFile.exists() && mapper != null) {
                    try {
                        java.lang.reflect.Method readValue = mapper.getClass().getMethod("readValue", File.class, Class.class);
                        Object raw = readValue.invoke(mapper, articlesFile, Map.class);
                        if (raw instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Map<String, String>> data = (Map<String, Map<String, String>>) raw;
                            return data;
                        }
                    } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException | IllegalArgumentException | SecurityException reflEx) {
                        LOGGER.log(Level.WARNING, "Error reading articles.json via reflection", reflEx);
                    }
                }

                Map<String, Map<String, String>> placeholder = new HashMap<>();
                Map<String, String> sample = new HashMap<>();
                sample.put("headline", "No articles available");
                sample.put("content", "No content fetched and no data/articles.json found.");
                placeholder.put("Placeholder", sample);
                return placeholder;
            } else {
                // persist combined to data/articles.json for debugging
                try {
                    File dataDir = base.resolve("data").toFile();
                    if (!dataDir.exists()) dataDir.mkdirs();
                    File out = base.resolve("data").resolve("articles.json").toFile();
                    if (mapper != null) {
                            try {
                                java.lang.reflect.Method writerMethod = mapper.getClass().getMethod("writerWithDefaultPrettyPrinter");
                                Object writer = writerMethod.invoke(mapper);
                                java.lang.reflect.Method writeValue = writer.getClass().getMethod("writeValue", File.class, Object.class);
                                writeValue.invoke(writer, out, combined);
                            } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException | IllegalArgumentException | SecurityException reflEx) {
                                LOGGER.log(Level.WARNING, "Error writing articles.json via reflection", reflEx);
                            }
                        }
                } catch (Exception writeEx) {
                    LOGGER.log(Level.WARNING, "Error persisting articles.json", writeEx);
                }
                return combined;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error in runAll", e);
            return Collections.emptyMap();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> reflectiveFetch(String className) {
        try {
            Class<?> cls = Class.forName(className);
            java.lang.reflect.Method m = cls.getMethod("fetch");
            Object res = m.invoke(null);
            if (res instanceof Map) {
                return (Map<String, String>) res;
            }
        } catch (ClassNotFoundException cnf) {
            // scraper not present — ignore and return empty map
        } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException ex) {
            LOGGER.log(Level.WARNING, "Error invoking scraper method", ex);
        }
        return Collections.emptyMap();
    }
}
