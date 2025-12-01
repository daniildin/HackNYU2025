package com.nova.parsing;

import java.util.HashMap;
import java.util.Map;

/*
 Replace file-based-only behavior with live scraper aggregation.
*/
public class Scraper {
    // Returns a map like {"NovaNews": {headline:..., content:...}, "CNBC": {...}}
    public static Map<String, Map<String, String>> runAll() {
        Map<String, Map<String, String>> combined = new HashMap<>();
        try {
            Map<String, String> nova = NovaNewsScraper.fetch();
            Map<String, String> cnbc = CNBCScraper.fetch();
            if (!nova.isEmpty()) combined.put("NovaNews", nova);
            if (!cnbc.isEmpty()) combined.put("CNBC", cnbc);
            return combined;
        } catch (Exception e) {
            return Map.of();
        }
    }
}
